// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2012-05-31 10:47:40

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "AND", "ONLY", "NOT", "GEN", "GENERIC_AT_RULE", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH", "DOT", "LBRACKET", "DCOLON", "STAR", "PIPE", "NAME", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "PERCENTAGE", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "'#'"
    };
    public static final int EOF=-1;
    public static final int T__116=116;
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
    public static final int GENERIC_AT_RULE=19;
    public static final int WS=20;
    public static final int MOZ_DOCUMENT_SYM=21;
    public static final int MOZ_URL_PREFIX=22;
    public static final int MOZ_DOMAIN=23;
    public static final int MOZ_REGEXP=24;
    public static final int PAGE_SYM=25;
    public static final int COUNTER_STYLE_SYM=26;
    public static final int FONT_FACE_SYM=27;
    public static final int TOPLEFTCORNER_SYM=28;
    public static final int TOPLEFT_SYM=29;
    public static final int TOPCENTER_SYM=30;
    public static final int TOPRIGHT_SYM=31;
    public static final int TOPRIGHTCORNER_SYM=32;
    public static final int BOTTOMLEFTCORNER_SYM=33;
    public static final int BOTTOMLEFT_SYM=34;
    public static final int BOTTOMCENTER_SYM=35;
    public static final int BOTTOMRIGHT_SYM=36;
    public static final int BOTTOMRIGHTCORNER_SYM=37;
    public static final int LEFTTOP_SYM=38;
    public static final int LEFTMIDDLE_SYM=39;
    public static final int LEFTBOTTOM_SYM=40;
    public static final int RIGHTTOP_SYM=41;
    public static final int RIGHTMIDDLE_SYM=42;
    public static final int RIGHTBOTTOM_SYM=43;
    public static final int COLON=44;
    public static final int SOLIDUS=45;
    public static final int PLUS=46;
    public static final int GREATER=47;
    public static final int TILDE=48;
    public static final int MINUS=49;
    public static final int HASH=50;
    public static final int DOT=51;
    public static final int LBRACKET=52;
    public static final int DCOLON=53;
    public static final int STAR=54;
    public static final int PIPE=55;
    public static final int NAME=56;
    public static final int OPEQ=57;
    public static final int INCLUDES=58;
    public static final int DASHMATCH=59;
    public static final int BEGINS=60;
    public static final int ENDS=61;
    public static final int CONTAINS=62;
    public static final int RBRACKET=63;
    public static final int LPAREN=64;
    public static final int RPAREN=65;
    public static final int IMPORTANT_SYM=66;
    public static final int NUMBER=67;
    public static final int PERCENTAGE=68;
    public static final int LENGTH=69;
    public static final int EMS=70;
    public static final int REM=71;
    public static final int EXS=72;
    public static final int ANGLE=73;
    public static final int TIME=74;
    public static final int FREQ=75;
    public static final int RESOLUTION=76;
    public static final int DIMENSION=77;
    public static final int NL=78;
    public static final int COMMENT=79;
    public static final int HEXCHAR=80;
    public static final int NONASCII=81;
    public static final int UNICODE=82;
    public static final int ESCAPE=83;
    public static final int NMSTART=84;
    public static final int NMCHAR=85;
    public static final int URL=86;
    public static final int A=87;
    public static final int B=88;
    public static final int C=89;
    public static final int D=90;
    public static final int E=91;
    public static final int F=92;
    public static final int G=93;
    public static final int H=94;
    public static final int I=95;
    public static final int J=96;
    public static final int K=97;
    public static final int L=98;
    public static final int M=99;
    public static final int N=100;
    public static final int O=101;
    public static final int P=102;
    public static final int Q=103;
    public static final int R=104;
    public static final int S=105;
    public static final int T=106;
    public static final int U=107;
    public static final int V=108;
    public static final int W=109;
    public static final int X=110;
    public static final int Y=111;
    public static final int Z=112;
    public static final int CDO=113;
    public static final int CDC=114;
    public static final int INVALID=115;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "property", "styleSheet", "fnAttributeName", "rule", 
        "mediaQueryOperator", "generic_at_rule", "mediaExpression", "slAttributeValue", 
        "namespacePrefixName", "function", "fontFace", "selector", "media", 
        "pseudoPage", "prio", "namespace", "cssId", "resourceIdentifier", 
        "synpred1_Css3", "declarations", "combinator", "simpleSelectorSequence", 
        "body", "hexColor", "moz_document", "mediaFeature", "mediaQuery", 
        "synpred2_Css3", "vendorAtRule", "charSetValue", "syncTo_IDENT_RBRACE", 
        "fnAttribute", "term", "elementSubsequent", "synpred3_Css3", "fnAttributeValue", 
        "functionName", "esPred", "page", "pseudo", "mediaQueryList", "propertyValue", 
        "margin_sym", "cssClass", "ws", "slAttribute", "mediaType", "margin", 
        "elementName", "slAttributeName", "namespacePrefix", "operator", 
        "unaryOperator", "syncTo_RBRACE", "counterStyle", "namespaces", 
        "nsPred", "imports", "moz_document_function", "declaration", "bodyItem", 
        "typeSelector", "expression", "selectorsGroup", "charSet", "importItem", 
        "syncToFollow"
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
            true, false, true, false, false, false, false, false, false, 
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

            if ( (LA6_0==IDENT||LA6_0==MEDIA_SYM||(LA6_0>=GEN && LA6_0<=GENERIC_AT_RULE)||LA6_0==MOZ_DOCUMENT_SYM||(LA6_0>=PAGE_SYM && LA6_0<=FONT_FACE_SYM)||LA6_0==COLON||(LA6_0>=HASH && LA6_0<=PIPE)||LA6_0==116) ) {
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:1: media : MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace ) ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(347, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:5: ( MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace ) ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:7: MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace ) ( ws )? )* RBRACE
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:13: ( ( rule | page | fontFace ) ( ws )? )*
            try { dbg.enterSubRule(23);

            loop23:
            do {
                int alt23=2;
                try { dbg.enterDecision(23, decisionCanBacktrack[23]);

                int LA23_0 = input.LA(1);

                if ( (LA23_0==IDENT||LA23_0==GEN||LA23_0==PAGE_SYM||LA23_0==FONT_FACE_SYM||LA23_0==COLON||(LA23_0>=HASH && LA23_0<=PIPE)||LA23_0==116) ) {
                    alt23=1;
                }


                } finally {dbg.exitDecision(23);}

                switch (alt23) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:15: ( rule | page | fontFace ) ( ws )?
            	    {
            	    dbg.location(350,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:15: ( rule | page | fontFace )
            	    int alt21=3;
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
            	    case 116:
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

            	    }
            	    } finally {dbg.exitSubRule(21);}

            	    dbg.location(350,42);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:42: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:42: ws
            	            {
            	            dbg.location(350,42);
            	            pushFollow(FOLLOW_ws_in_media423);
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
            match(input,RBRACE,FOLLOW_RBRACE_in_media437); if (state.failed) return ;

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
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList453);
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
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList457); if (state.failed) return ;
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
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList459);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(24);}

                    	    dbg.location(355,29);
                    	    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList462);
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
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery481);
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
                                    pushFollow(FOLLOW_ws_in_mediaQuery483);
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
                    pushFollow(FOLLOW_mediaType_in_mediaQuery490);
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
                            pushFollow(FOLLOW_ws_in_mediaQuery492);
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
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery497); if (state.failed) return ;
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
                    	            pushFollow(FOLLOW_ws_in_mediaQuery499);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(30);}

                    	    dbg.location(359,56);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery502);
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
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery510);
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
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery514); if (state.failed) return ;
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
                    	            pushFollow(FOLLOW_ws_in_mediaQuery516);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(32);}

                    	    dbg.location(360,30);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery519);
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:2: ( '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:4: '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )?
            {
            dbg.location(372,4);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression571); if (state.failed) return ;
            dbg.location(372,8);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:8: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:8: ws
                    {
                    dbg.location(372,8);
                    pushFollow(FOLLOW_ws_in_mediaExpression573);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(35);}

            dbg.location(372,12);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression576);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(372,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:25: ws
                    {
                    dbg.location(372,25);
                    pushFollow(FOLLOW_ws_in_mediaExpression578);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(36);}

            dbg.location(372,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:29: ( ':' ( ws )? expression )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:31: ':' ( ws )? expression
                    {
                    dbg.location(372,31);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression583); if (state.failed) return ;
                    dbg.location(372,35);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:35: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:35: ws
                            {
                            dbg.location(372,35);
                            pushFollow(FOLLOW_ws_in_mediaExpression585);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(37);}

                    dbg.location(372,39);
                    pushFollow(FOLLOW_expression_in_mediaExpression588);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(372,53);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression593); if (state.failed) return ;
            dbg.location(372,57);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:57: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:57: ws
                    {
                    dbg.location(372,57);
                    pushFollow(FOLLOW_ws_in_mediaExpression595);
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
        dbg.location(373, 2);

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
            match(input,IDENT,FOLLOW_IDENT_in_mediaFeature608); if (state.failed) return ;

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

                if ( (LA41_0==IDENT||LA41_0==MEDIA_SYM||(LA41_0>=GEN && LA41_0<=GENERIC_AT_RULE)||LA41_0==MOZ_DOCUMENT_SYM||(LA41_0>=PAGE_SYM && LA41_0<=FONT_FACE_SYM)||LA41_0==COLON||(LA41_0>=HASH && LA41_0<=PIPE)||LA41_0==116) ) {
                    alt41=1;
                }


                } finally {dbg.exitDecision(41);}

                switch (alt41) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:4: bodyItem ( ws )?
            	    {
            	    dbg.location(380,4);
            	    pushFollow(FOLLOW_bodyItem_in_body624);
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
            	            pushFollow(FOLLOW_ws_in_body626);
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(383, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:5: ( rule | media | page | counterStyle | fontFace | vendorAtRule )
            int alt42=6;
            try { dbg.enterDecision(42, decisionCanBacktrack[42]);

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
            case 116:
                {
                alt42=1;
                }
                break;
            case MEDIA_SYM:
                {
                alt42=2;
                }
                break;
            case PAGE_SYM:
                {
                alt42=3;
                }
                break;
            case COUNTER_STYLE_SYM:
                {
                alt42=4;
                }
                break;
            case FONT_FACE_SYM:
                {
                alt42=5;
                }
                break;
            case GENERIC_AT_RULE:
            case MOZ_DOCUMENT_SYM:
                {
                alt42=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

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
                    pushFollow(FOLLOW_rule_in_bodyItem651);
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
                    pushFollow(FOLLOW_media_in_bodyItem663);
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
                    pushFollow(FOLLOW_page_in_bodyItem675);
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
                    pushFollow(FOLLOW_counterStyle_in_bodyItem687);
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
                    pushFollow(FOLLOW_fontFace_in_bodyItem699);
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
                    pushFollow(FOLLOW_vendorAtRule_in_bodyItem711);
                    vendorAtRule();

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
        dbg.location(391, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:399:1: vendorAtRule : ( moz_document | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(399, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:1: ( moz_document | generic_at_rule )
            int alt43=2;
            try { dbg.enterDecision(43, decisionCanBacktrack[43]);

            int LA43_0 = input.LA(1);

            if ( (LA43_0==MOZ_DOCUMENT_SYM) ) {
                alt43=1;
            }
            else if ( (LA43_0==GENERIC_AT_RULE) ) {
                alt43=2;
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:3: moz_document
                    {
                    dbg.location(400,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule734);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:18: generic_at_rule
                    {
                    dbg.location(400,18);
                    pushFollow(FOLLOW_generic_at_rule_in_vendorAtRule738);
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
        dbg.location(400, 33);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "vendorAtRule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "vendorAtRule"


    // $ANTLR start "generic_at_rule"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:1: generic_at_rule : GENERIC_AT_RULE ( WS )* ( ( IDENT | STRING ) ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(402, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:5: ( GENERIC_AT_RULE ( WS )* ( ( IDENT | STRING ) ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:7: GENERIC_AT_RULE ( WS )* ( ( IDENT | STRING ) ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(403,7);
            match(input,GENERIC_AT_RULE,FOLLOW_GENERIC_AT_RULE_in_generic_at_rule754); if (state.failed) return ;
            dbg.location(403,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:23: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:23: WS
            	    {
            	    dbg.location(403,23);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule756); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);
            } finally {dbg.exitSubRule(44);}

            dbg.location(403,27);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:27: ( ( IDENT | STRING ) ( WS )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:29: ( IDENT | STRING ) ( WS )*
                    {
                    dbg.location(403,29);
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

                    dbg.location(403,48);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:48: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:48: WS
                    	    {
                    	    dbg.location(403,48);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule771); if (state.failed) return ;

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

            dbg.location(404,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule786); if (state.failed) return ;
            dbg.location(405,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule798);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(406,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule808); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "generic_at_rule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "generic_at_rule"


    // $ANTLR start "moz_document"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(408, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE
            {
            dbg.location(410,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document824); if (state.failed) return ;
            dbg.location(410,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:19: ws
                    {
                    dbg.location(410,19);
                    pushFollow(FOLLOW_ws_in_moz_document826);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(47);}

            dbg.location(410,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:25: moz_document_function ( ws )?
            {
            dbg.location(410,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document831);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(410,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:47: ws
                    {
                    dbg.location(410,47);
                    pushFollow(FOLLOW_ws_in_moz_document833);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(48);}


            }

            dbg.location(410,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(410,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document839); if (state.failed) return ;
            	    dbg.location(410,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:60: ws
            	            {
            	            dbg.location(410,60);
            	            pushFollow(FOLLOW_ws_in_moz_document841);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(49);}

            	    dbg.location(410,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document844);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(410,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:86: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:86: ws
            	            {
            	            dbg.location(410,86);
            	            pushFollow(FOLLOW_ws_in_moz_document846);
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

            dbg.location(411,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document853); if (state.failed) return ;
            dbg.location(411,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:9: ws
                    {
                    dbg.location(411,9);
                    pushFollow(FOLLOW_ws_in_moz_document855);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(52);}

            dbg.location(412,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:3: ( ( rule | page ) ( ws )? )*
            try { dbg.enterSubRule(55);

            loop55:
            do {
                int alt55=2;
                try { dbg.enterDecision(55, decisionCanBacktrack[55]);

                int LA55_0 = input.LA(1);

                if ( (LA55_0==IDENT||LA55_0==GEN||LA55_0==PAGE_SYM||LA55_0==COLON||(LA55_0>=HASH && LA55_0<=PIPE)||LA55_0==116) ) {
                    alt55=1;
                }


                } finally {dbg.exitDecision(55);}

                switch (alt55) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:5: ( rule | page ) ( ws )?
            	    {
            	    dbg.location(412,5);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:5: ( rule | page )
            	    int alt53=2;
            	    try { dbg.enterSubRule(53);
            	    try { dbg.enterDecision(53, decisionCanBacktrack[53]);

            	    int LA53_0 = input.LA(1);

            	    if ( (LA53_0==IDENT||LA53_0==GEN||LA53_0==COLON||(LA53_0>=HASH && LA53_0<=PIPE)||LA53_0==116) ) {
            	        alt53=1;
            	    }
            	    else if ( (LA53_0==PAGE_SYM) ) {
            	        alt53=2;
            	    }
            	    else {
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:7: rule
            	            {
            	            dbg.location(412,7);
            	            pushFollow(FOLLOW_rule_in_moz_document864);
            	            rule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:14: page
            	            {
            	            dbg.location(412,14);
            	            pushFollow(FOLLOW_page_in_moz_document868);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(53);}

            	    dbg.location(412,21);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:21: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:21: ws
            	            {
            	            dbg.location(412,21);
            	            pushFollow(FOLLOW_ws_in_moz_document872);
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

            dbg.location(413,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document878); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(414, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(416, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(417,2);
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
        dbg.location(419, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "moz_document_function");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "moz_document_function"


    // $ANTLR start "page"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(421, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(422,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page920); if (state.failed) return ;
            dbg.location(422,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:16: ws
                    {
                    dbg.location(422,16);
                    pushFollow(FOLLOW_ws_in_page922);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(56);}

            dbg.location(422,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:20: ( IDENT ( ws )? )?
            int alt58=2;
            try { dbg.enterSubRule(58);
            try { dbg.enterDecision(58, decisionCanBacktrack[58]);

            int LA58_0 = input.LA(1);

            if ( (LA58_0==IDENT) ) {
                alt58=1;
            }
            } finally {dbg.exitDecision(58);}

            switch (alt58) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:22: IDENT ( ws )?
                    {
                    dbg.location(422,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page927); if (state.failed) return ;
                    dbg.location(422,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:28: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:28: ws
                            {
                            dbg.location(422,28);
                            pushFollow(FOLLOW_ws_in_page929);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(57);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(58);}

            dbg.location(422,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:35: ( pseudoPage ( ws )? )?
            int alt60=2;
            try { dbg.enterSubRule(60);
            try { dbg.enterDecision(60, decisionCanBacktrack[60]);

            int LA60_0 = input.LA(1);

            if ( (LA60_0==COLON) ) {
                alt60=1;
            }
            } finally {dbg.exitDecision(60);}

            switch (alt60) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:36: pseudoPage ( ws )?
                    {
                    dbg.location(422,36);
                    pushFollow(FOLLOW_pseudoPage_in_page936);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(422,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:47: ws
                            {
                            dbg.location(422,47);
                            pushFollow(FOLLOW_ws_in_page938);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(59);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(60);}

            dbg.location(423,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page951); if (state.failed) return ;
            dbg.location(423,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:16: ws
                    {
                    dbg.location(423,16);
                    pushFollow(FOLLOW_ws_in_page953);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(61);}

            dbg.location(427,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:13: ( declaration | margin ( ws )? )?
            int alt63=3;
            try { dbg.enterSubRule(63);
            try { dbg.enterDecision(63, decisionCanBacktrack[63]);

            int LA63_0 = input.LA(1);

            if ( (LA63_0==IDENT||LA63_0==GEN) ) {
                alt63=1;
            }
            else if ( ((LA63_0>=TOPLEFTCORNER_SYM && LA63_0<=RIGHTBOTTOM_SYM)) ) {
                alt63=2;
            }
            } finally {dbg.exitDecision(63);}

            switch (alt63) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:14: declaration
                    {
                    dbg.location(427,14);
                    pushFollow(FOLLOW_declaration_in_page1008);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:26: margin ( ws )?
                    {
                    dbg.location(427,26);
                    pushFollow(FOLLOW_margin_in_page1010);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(427,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:33: ws
                            {
                            dbg.location(427,33);
                            pushFollow(FOLLOW_ws_in_page1012);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(62);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(63);}

            dbg.location(427,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
            try { dbg.enterSubRule(67);

            loop67:
            do {
                int alt67=2;
                try { dbg.enterDecision(67, decisionCanBacktrack[67]);

                int LA67_0 = input.LA(1);

                if ( (LA67_0==SEMI) ) {
                    alt67=1;
                }


                } finally {dbg.exitDecision(67);}

                switch (alt67) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(427,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1018); if (state.failed) return ;
            	    dbg.location(427,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:45: ws
            	            {
            	            dbg.location(427,45);
            	            pushFollow(FOLLOW_ws_in_page1020);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(64);}

            	    dbg.location(427,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:49: ( declaration | margin ( ws )? )?
            	    int alt66=3;
            	    try { dbg.enterSubRule(66);
            	    try { dbg.enterDecision(66, decisionCanBacktrack[66]);

            	    int LA66_0 = input.LA(1);

            	    if ( (LA66_0==IDENT||LA66_0==GEN) ) {
            	        alt66=1;
            	    }
            	    else if ( ((LA66_0>=TOPLEFTCORNER_SYM && LA66_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt66=2;
            	    }
            	    } finally {dbg.exitDecision(66);}

            	    switch (alt66) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:50: declaration
            	            {
            	            dbg.location(427,50);
            	            pushFollow(FOLLOW_declaration_in_page1024);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:62: margin ( ws )?
            	            {
            	            dbg.location(427,62);
            	            pushFollow(FOLLOW_margin_in_page1026);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(427,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:69: ws
            	                    {
            	                    dbg.location(427,69);
            	                    pushFollow(FOLLOW_ws_in_page1028);
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


            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);
            } finally {dbg.exitSubRule(67);}

            dbg.location(428,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1043); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(429, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(431, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(432,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1064); if (state.failed) return ;
            dbg.location(432,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:25: ws
                    {
                    dbg.location(432,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1066);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(68);}

            dbg.location(432,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1069); if (state.failed) return ;
            dbg.location(432,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:35: ws
                    {
                    dbg.location(432,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1071);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(433,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1082); if (state.failed) return ;
            dbg.location(433,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:16: ws
                    {
                    dbg.location(433,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1084);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(433,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle1087);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(434,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1091);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(435,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1101); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(436, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(438, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(439,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1122); if (state.failed) return ;
            dbg.location(439,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:21: ws
                    {
                    dbg.location(439,21);
                    pushFollow(FOLLOW_ws_in_fontFace1124);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(71);}

            dbg.location(440,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1135); if (state.failed) return ;
            dbg.location(440,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:16: ws
                    {
                    dbg.location(440,16);
                    pushFollow(FOLLOW_ws_in_fontFace1137);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(72);}

            dbg.location(440,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_fontFace1140);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(441,3);
            pushFollow(FOLLOW_declarations_in_fontFace1144);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(442,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1154); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(443, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(445, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:2: ( margin_sym ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:4: margin_sym ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(446,4);
            pushFollow(FOLLOW_margin_sym_in_margin1169);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(446,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:15: ws
                    {
                    dbg.location(446,15);
                    pushFollow(FOLLOW_ws_in_margin1171);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(73);}

            dbg.location(446,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1174); if (state.failed) return ;
            dbg.location(446,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:26: ws
                    {
                    dbg.location(446,26);
                    pushFollow(FOLLOW_ws_in_margin1176);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(74);}

            dbg.location(446,30);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_margin1179);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(446,50);
            pushFollow(FOLLOW_declarations_in_margin1181);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(446,63);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1183); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(447, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(449, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(450,2);
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
        dbg.location(467, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(469, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:7: COLON IDENT
            {
            dbg.location(470,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1412); if (state.failed) return ;
            dbg.location(470,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1414); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "pseudoPage");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudoPage"


    // $ANTLR start "operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:1: operator : ( SOLIDUS ( ws )? | COMMA ( ws )? | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(473, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:5: ( SOLIDUS ( ws )? | COMMA ( ws )? | )
            int alt77=3;
            try { dbg.enterDecision(77, decisionCanBacktrack[77]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt77=1;
                }
                break;
            case COMMA:
                {
                alt77=2;
                }
                break;
            case IDENT:
            case STRING:
            case URI:
            case GEN:
            case PLUS:
            case MINUS:
            case HASH:
            case NUMBER:
            case PERCENTAGE:
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
                alt77=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 77, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(77);}

            switch (alt77) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:7: SOLIDUS ( ws )?
                    {
                    dbg.location(474,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator1435); if (state.failed) return ;
                    dbg.location(474,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:15: ws
                            {
                            dbg.location(474,15);
                            pushFollow(FOLLOW_ws_in_operator1437);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(75);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:7: COMMA ( ws )?
                    {
                    dbg.location(475,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator1446); if (state.failed) return ;
                    dbg.location(475,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:13: ws
                            {
                            dbg.location(475,13);
                            pushFollow(FOLLOW_ws_in_operator1448);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(76);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:477:5: 
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
        dbg.location(477, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(479, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
            int alt81=4;
            try { dbg.enterDecision(81, decisionCanBacktrack[81]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt81=1;
                }
                break;
            case GREATER:
                {
                alt81=2;
                }
                break;
            case TILDE:
                {
                alt81=3;
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
            case 116:
                {
                alt81=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 81, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(81);}

            switch (alt81) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:7: PLUS ( ws )?
                    {
                    dbg.location(480,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1476); if (state.failed) return ;
                    dbg.location(480,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:12: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:12: ws
                            {
                            dbg.location(480,12);
                            pushFollow(FOLLOW_ws_in_combinator1478);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(78);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:7: GREATER ( ws )?
                    {
                    dbg.location(481,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1487); if (state.failed) return ;
                    dbg.location(481,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:15: ws
                            {
                            dbg.location(481,15);
                            pushFollow(FOLLOW_ws_in_combinator1489);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(79);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:7: TILDE ( ws )?
                    {
                    dbg.location(482,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1498); if (state.failed) return ;
                    dbg.location(482,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:13: ws
                            {
                            dbg.location(482,13);
                            pushFollow(FOLLOW_ws_in_combinator1500);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(80);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:484:5: 
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
        dbg.location(484, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(486, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(487,5);
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
        dbg.location(489, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:1: property : ( IDENT | GEN ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(491, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:5: ( ( IDENT | GEN ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:7: ( IDENT | GEN ) ( ws )?
            {
            dbg.location(492,7);
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

            dbg.location(492,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:21: ws
                    {
                    dbg.location(492,21);
                    pushFollow(FOLLOW_ws_in_property1568);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(82);}


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
            dbg.exitRule(getGrammarFileName(), "property");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "property"


    // $ANTLR start "rule"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:495:1: rule : selectorsGroup LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(495, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:5: ( selectorsGroup LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:9: selectorsGroup LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(496,9);
            pushFollow(FOLLOW_selectorsGroup_in_rule1593);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(497,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule1603); if (state.failed) return ;
            dbg.location(497,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:16: ws
                    {
                    dbg.location(497,16);
                    pushFollow(FOLLOW_ws_in_rule1605);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(83);}

            dbg.location(497,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_rule1608);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(498,13);
            pushFollow(FOLLOW_declarations_in_rule1622);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(499,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule1632); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well
                    
        }
        finally {
        }
        dbg.location(500, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:1: declarations : ( declaration )? ( SEMI ( ws )? ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(507, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:5: ( ( declaration )? ( SEMI ( ws )? ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:9: ( declaration )? ( SEMI ( ws )? ( declaration )? )*
            {
            dbg.location(510,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:9: ( declaration )?
            int alt84=2;
            try { dbg.enterSubRule(84);
            try { dbg.enterDecision(84, decisionCanBacktrack[84]);

            int LA84_0 = input.LA(1);

            if ( (LA84_0==IDENT||LA84_0==GEN) ) {
                alt84=1;
            }
            } finally {dbg.exitDecision(84);}

            switch (alt84) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:9: declaration
                    {
                    dbg.location(510,9);
                    pushFollow(FOLLOW_declaration_in_declarations1680);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(84);}

            dbg.location(510,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:22: ( SEMI ( ws )? ( declaration )? )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:23: SEMI ( ws )? ( declaration )?
            	    {
            	    dbg.location(510,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations1684); if (state.failed) return ;
            	    dbg.location(510,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:28: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:28: ws
            	            {
            	            dbg.location(510,28);
            	            pushFollow(FOLLOW_ws_in_declarations1686);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(85);}

            	    dbg.location(510,32);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:32: ( declaration )?
            	    int alt86=2;
            	    try { dbg.enterSubRule(86);
            	    try { dbg.enterDecision(86, decisionCanBacktrack[86]);

            	    int LA86_0 = input.LA(1);

            	    if ( (LA86_0==IDENT||LA86_0==GEN) ) {
            	        alt86=1;
            	    }
            	    } finally {dbg.exitDecision(86);}

            	    switch (alt86) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:32: declaration
            	            {
            	            dbg.location(510,32);
            	            pushFollow(FOLLOW_declaration_in_declarations1689);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "declarations");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "declarations"


    // $ANTLR start "selectorsGroup"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:1: selectorsGroup : selector ( COMMA ( ws )? selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(513, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:5: ( selector ( COMMA ( ws )? selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:7: selector ( COMMA ( ws )? selector )*
            {
            dbg.location(514,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup1713);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(514,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:16: ( COMMA ( ws )? selector )*
            try { dbg.enterSubRule(89);

            loop89:
            do {
                int alt89=2;
                try { dbg.enterDecision(89, decisionCanBacktrack[89]);

                int LA89_0 = input.LA(1);

                if ( (LA89_0==COMMA) ) {
                    alt89=1;
                }


                } finally {dbg.exitDecision(89);}

                switch (alt89) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:17: COMMA ( ws )? selector
            	    {
            	    dbg.location(514,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup1716); if (state.failed) return ;
            	    dbg.location(514,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:23: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:23: ws
            	            {
            	            dbg.location(514,23);
            	            pushFollow(FOLLOW_ws_in_selectorsGroup1718);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(88);}

            	    dbg.location(514,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup1721);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop89;
                }
            } while (true);
            } finally {dbg.exitSubRule(89);}


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
            dbg.exitRule(getGrammarFileName(), "selectorsGroup");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "selectorsGroup"


    // $ANTLR start "selector"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(517, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(518,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector1744);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(518,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(90);

            loop90:
            do {
                int alt90=2;
                try { dbg.enterDecision(90, decisionCanBacktrack[90]);

                int LA90_0 = input.LA(1);

                if ( (LA90_0==IDENT||LA90_0==GEN||LA90_0==COLON||(LA90_0>=PLUS && LA90_0<=TILDE)||(LA90_0>=HASH && LA90_0<=PIPE)||LA90_0==116) ) {
                    alt90=1;
                }


                } finally {dbg.exitDecision(90);}

                switch (alt90) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(518,31);
            	    pushFollow(FOLLOW_combinator_in_selector1747);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(518,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector1749);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop90;
                }
            } while (true);
            } finally {dbg.exitSubRule(90);}


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
            dbg.exitRule(getGrammarFileName(), "selector");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "selector"


    // $ANTLR start "simpleSelectorSequence"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(522, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt93=2;
            try { dbg.enterDecision(93, decisionCanBacktrack[93]);

            int LA93_0 = input.LA(1);

            if ( (LA93_0==IDENT||LA93_0==GEN||(LA93_0>=STAR && LA93_0<=PIPE)) ) {
                alt93=1;
            }
            else if ( (LA93_0==COLON||(LA93_0>=HASH && LA93_0<=DCOLON)||LA93_0==116) ) {
                alt93=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 93, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(93);}

            switch (alt93) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(525,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(525,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence1783);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(525,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(91);

                    loop91:
                    do {
                        int alt91=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(525,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1790);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop91;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(91);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(527,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(527,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:4: ( ( esPred )=> elementSubsequent )+
                    int cnt92=0;
                    try { dbg.enterSubRule(92);

                    loop92:
                    do {
                        int alt92=2;
                        try { dbg.enterDecision(92, decisionCanBacktrack[92]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA92_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt92=1;
                            }


                            }
                            break;
                        case 116:
                            {
                            int LA92_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt92=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA92_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt92=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA92_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt92=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA92_6 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt92=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(92);}

                        switch (alt92) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(527,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1808);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt92 >= 1 ) break loop92;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(92, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt92++;
                    } while (true);
                    } finally {dbg.exitSubRule(92);}


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
        dbg.location(528, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:535:1: esPred : ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(535, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:5: ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(536,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH && input.LA(1)<=DCOLON)||input.LA(1)==116 ) {
                input.consume();
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
        dbg.location(537, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:1: typeSelector options {k=2; } : ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(539, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:3: ( ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:6: ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(541,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:6: ( ( nsPred )=> namespacePrefix )?
            int alt94=2;
            try { dbg.enterSubRule(94);
            try { dbg.enterDecision(94, decisionCanBacktrack[94]);

            int LA94_0 = input.LA(1);

            if ( (LA94_0==IDENT) ) {
                int LA94_1 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt94=1;
                }
            }
            else if ( (LA94_0==STAR) ) {
                int LA94_2 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt94=1;
                }
            }
            else if ( (LA94_0==PIPE) && (synpred3_Css3())) {
                alt94=1;
            }
            } finally {dbg.exitDecision(94);}

            switch (alt94) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:7: ( nsPred )=> namespacePrefix
                    {
                    dbg.location(541,17);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector1910);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(94);}

            dbg.location(541,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:35: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:37: elementName ( ws )?
            {
            dbg.location(541,37);
            pushFollow(FOLLOW_elementName_in_typeSelector1916);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(541,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:49: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:49: ws
                    {
                    dbg.location(541,49);
                    pushFollow(FOLLOW_ws_in_typeSelector1918);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(95);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(542, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:1: nsPred : ( IDENT | STAR )? PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(545, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:3: ( ( IDENT | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:3: ( IDENT | STAR )? PIPE
            {
            dbg.location(547,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:3: ( IDENT | STAR )?
            int alt96=2;
            try { dbg.enterSubRule(96);
            try { dbg.enterDecision(96, decisionCanBacktrack[96]);

            int LA96_0 = input.LA(1);

            if ( (LA96_0==IDENT||LA96_0==STAR) ) {
                alt96=1;
            }
            } finally {dbg.exitDecision(96);}

            switch (alt96) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    {
                    dbg.location(547,3);
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
            } finally {dbg.exitSubRule(96);}

            dbg.location(547,19);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred1947); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(548, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(550, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(551,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:5: ( namespacePrefixName | STAR )?
            int alt97=3;
            try { dbg.enterSubRule(97);
            try { dbg.enterDecision(97, decisionCanBacktrack[97]);

            int LA97_0 = input.LA(1);

            if ( (LA97_0==IDENT) ) {
                alt97=1;
            }
            else if ( (LA97_0==STAR) ) {
                alt97=2;
            }
            } finally {dbg.exitDecision(97);}

            switch (alt97) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:7: namespacePrefixName
                    {
                    dbg.location(551,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix1962);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:29: STAR
                    {
                    dbg.location(551,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix1966); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(97);}

            dbg.location(551,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix1970); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(552, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:1: elementSubsequent : ( cssId | cssClass | slAttribute | pseudo ) ( ws )? ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(555, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:5: ( ( cssId | cssClass | slAttribute | pseudo ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:5: ( cssId | cssClass | slAttribute | pseudo ) ( ws )?
            {
            dbg.location(557,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:5: ( cssId | cssClass | slAttribute | pseudo )
            int alt98=4;
            try { dbg.enterSubRule(98);
            try { dbg.enterDecision(98, decisionCanBacktrack[98]);

            switch ( input.LA(1) ) {
            case HASH:
            case 116:
                {
                alt98=1;
                }
                break;
            case DOT:
                {
                alt98=2;
                }
                break;
            case LBRACKET:
                {
                alt98=3;
                }
                break;
            case COLON:
            case DCOLON:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:6: cssId
                    {
                    dbg.location(558,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent2004);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:8: cssClass
                    {
                    dbg.location(559,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent2013);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:11: slAttribute
                    {
                    dbg.location(560,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent2025);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:11: pseudo
                    {
                    dbg.location(561,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent2037);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(98);}

            dbg.location(563,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:5: ws
                    {
                    dbg.location(563,5);
                    pushFollow(FOLLOW_ws_in_elementSubsequent2049);
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
        dbg.location(564, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:1: cssId : ( HASH | ( '#' NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(567, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:5: ( HASH | ( '#' NAME ) )
            int alt100=2;
            try { dbg.enterDecision(100, decisionCanBacktrack[100]);

            int LA100_0 = input.LA(1);

            if ( (LA100_0==HASH) ) {
                alt100=1;
            }
            else if ( (LA100_0==116) ) {
                alt100=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 100, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(100);}

            switch (alt100) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:7: HASH
                    {
                    dbg.location(568,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId2072); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:14: ( '#' NAME )
                    {
                    dbg.location(568,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:14: ( '#' NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:16: '#' NAME
                    {
                    dbg.location(568,16);
                    match(input,116,FOLLOW_116_in_cssId2078); if (state.failed) return ;
                    dbg.location(568,20);
                    match(input,NAME,FOLLOW_NAME_in_cssId2080); if (state.failed) return ;

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
        dbg.location(569, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(575, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:7: DOT ( IDENT | GEN )
            {
            dbg.location(576,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2108); if (state.failed) return ;
            dbg.location(576,11);
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
        dbg.location(577, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:1: elementName : ( ( IDENT | GEN ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(584, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:5: ( ( IDENT | GEN ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(585,5);
            if ( input.LA(1)==IDENT||input.LA(1)==GEN||input.LA(1)==STAR ) {
                input.consume();
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
        dbg.location(586, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(588, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(589,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute2179); if (state.failed) return ;
            dbg.location(590,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:6: ( namespacePrefix )?
            int alt101=2;
            try { dbg.enterSubRule(101);
            try { dbg.enterDecision(101, decisionCanBacktrack[101]);

            int LA101_0 = input.LA(1);

            if ( (LA101_0==IDENT) ) {
                int LA101_1 = input.LA(2);

                if ( (LA101_1==PIPE) ) {
                    alt101=1;
                }
            }
            else if ( ((LA101_0>=STAR && LA101_0<=PIPE)) ) {
                alt101=1;
            }
            } finally {dbg.exitDecision(101);}

            switch (alt101) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:6: namespacePrefix
                    {
                    dbg.location(590,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute2186);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(101);}

            dbg.location(590,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:23: ws
                    {
                    dbg.location(590,23);
                    pushFollow(FOLLOW_ws_in_slAttribute2189);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(102);}

            dbg.location(591,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute2200);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(591,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:25: ws
                    {
                    dbg.location(591,25);
                    pushFollow(FOLLOW_ws_in_slAttribute2202);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(103);}

            dbg.location(593,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt106=2;
            try { dbg.enterSubRule(106);
            try { dbg.enterDecision(106, decisionCanBacktrack[106]);

            int LA106_0 = input.LA(1);

            if ( ((LA106_0>=OPEQ && LA106_0<=CONTAINS)) ) {
                alt106=1;
            }
            } finally {dbg.exitDecision(106);}

            switch (alt106) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(594,17);
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

                    dbg.location(602,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:17: ws
                            {
                            dbg.location(602,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2424);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(104);}

                    dbg.location(603,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute2443);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(604,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:17: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:17: ws
                            {
                            dbg.location(604,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2461);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(105);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(106);}

            dbg.location(607,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute2490); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(608, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(615, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:4: IDENT
            {
            dbg.location(616,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName2506); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(617, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(619, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:2: ( IDENT | STRING )
            {
            dbg.location(621,2);
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
        dbg.location(625, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(627, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:628:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:628:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(628,7);
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

            dbg.location(629,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt114=2;
            try { dbg.enterSubRule(114);
            try { dbg.enterDecision(114, decisionCanBacktrack[114]);

            int LA114_0 = input.LA(1);

            if ( (LA114_0==IDENT||LA114_0==GEN) ) {
                alt114=1;
            }
            else if ( (LA114_0==NOT) ) {
                alt114=2;
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    {
                    dbg.location(630,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
                    {
                    dbg.location(631,21);
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

                    dbg.location(632,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
                    int alt110=2;
                    try { dbg.enterSubRule(110);
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:25: ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN
                            {
                            dbg.location(633,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:25: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:25: ws
                                    {
                                    dbg.location(633,25);
                                    pushFollow(FOLLOW_ws_in_pseudo2701);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(107);}

                            dbg.location(633,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2704); if (state.failed) return ;
                            dbg.location(633,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:36: ws
                                    {
                                    dbg.location(633,36);
                                    pushFollow(FOLLOW_ws_in_pseudo2706);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(108);}

                            dbg.location(633,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:40: ( expression | '*' )?
                            int alt109=3;
                            try { dbg.enterSubRule(109);
                            try { dbg.enterDecision(109, decisionCanBacktrack[109]);

                            int LA109_0 = input.LA(1);

                            if ( ((LA109_0>=IDENT && LA109_0<=URI)||LA109_0==GEN||LA109_0==PLUS||(LA109_0>=MINUS && LA109_0<=HASH)||(LA109_0>=NUMBER && LA109_0<=DIMENSION)) ) {
                                alt109=1;
                            }
                            else if ( (LA109_0==STAR) ) {
                                alt109=2;
                            }
                            } finally {dbg.exitDecision(109);}

                            switch (alt109) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:42: expression
                                    {
                                    dbg.location(633,42);
                                    pushFollow(FOLLOW_expression_in_pseudo2711);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:55: '*'
                                    {
                                    dbg.location(633,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo2715); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(109);}

                            dbg.location(633,62);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2720); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(110);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(637,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(637,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo2799); if (state.failed) return ;
                    dbg.location(637,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:23: ws
                            {
                            dbg.location(637,23);
                            pushFollow(FOLLOW_ws_in_pseudo2801);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(111);}

                    dbg.location(637,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2804); if (state.failed) return ;
                    dbg.location(637,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:34: ws
                            {
                            dbg.location(637,34);
                            pushFollow(FOLLOW_ws_in_pseudo2806);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(112);}

                    dbg.location(637,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:38: ( simpleSelectorSequence )?
                    int alt113=2;
                    try { dbg.enterSubRule(113);
                    try { dbg.enterDecision(113, decisionCanBacktrack[113]);

                    int LA113_0 = input.LA(1);

                    if ( (LA113_0==IDENT||LA113_0==GEN||LA113_0==COLON||(LA113_0>=HASH && LA113_0<=PIPE)||LA113_0==116) ) {
                        alt113=1;
                    }
                    } finally {dbg.exitDecision(113);}

                    switch (alt113) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:38: simpleSelectorSequence
                            {
                            dbg.location(637,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo2809);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(113);}

                    dbg.location(637,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2812); if (state.failed) return ;

                    }


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
        dbg.location(639, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:1: declaration : property COLON ( ws )? propertyValue ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(641, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:5: ( property COLON ( ws )? propertyValue ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:5: property COLON ( ws )? propertyValue ( prio )?
            {
            dbg.location(644,5);
            pushFollow(FOLLOW_property_in_declaration2856);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(644,14);
            match(input,COLON,FOLLOW_COLON_in_declaration2858); if (state.failed) return ;
            dbg.location(644,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:20: ws
                    {
                    dbg.location(644,20);
                    pushFollow(FOLLOW_ws_in_declaration2860);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(115);}

            dbg.location(644,24);
            pushFollow(FOLLOW_propertyValue_in_declaration2863);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(644,38);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:38: ( prio )?
            int alt116=2;
            try { dbg.enterSubRule(116);
            try { dbg.enterDecision(116, decisionCanBacktrack[116]);

            int LA116_0 = input.LA(1);

            if ( (LA116_0==IMPORTANT_SYM) ) {
                alt116=1;
            }
            } finally {dbg.exitDecision(116);}

            switch (alt116) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:38: prio
                    {
                    dbg.location(644,38);
                    pushFollow(FOLLOW_prio_in_declaration2865);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(116);}


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
        dbg.location(645, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:1: propertyValue : expression ;
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(653, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:4: expression
            {
            dbg.location(654,4);
            pushFollow(FOLLOW_expression_in_propertyValue2889);
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
        dbg.location(655, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "propertyValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "propertyValue"


    // $ANTLR start "syncTo_IDENT_RBRACE"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:659:1: syncTo_IDENT_RBRACE : ;
    public final void syncTo_IDENT_RBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(659, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:6: 
            {
            }

        }
        finally {
        }
        dbg.location(664, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncTo_IDENT_RBRACE"


    // $ANTLR start "syncTo_RBRACE"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(666, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:6: 
            {
            }

        }
        finally {
        }
        dbg.location(671, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(674, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:6: 
            {
            }

        }
        finally {
        }
        dbg.location(679, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:681:1: prio : IMPORTANT_SYM ( ws )? ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(681, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:5: ( IMPORTANT_SYM ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:7: IMPORTANT_SYM ( ws )?
            {
            dbg.location(682,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio3001); if (state.failed) return ;
            dbg.location(682,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:21: ws
                    {
                    dbg.location(682,21);
                    pushFollow(FOLLOW_ws_in_prio3003);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(117);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(683, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:1: expression : term ( operator term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(685, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:686:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:686:7: term ( operator term )*
            {
            dbg.location(686,7);
            pushFollow(FOLLOW_term_in_expression3025);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(686,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:686:12: ( operator term )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:686:13: operator term
            	    {
            	    dbg.location(686,13);
            	    pushFollow(FOLLOW_operator_in_expression3028);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(686,22);
            	    pushFollow(FOLLOW_term_in_expression3030);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop118;
                }
            } while (true);
            } finally {dbg.exitSubRule(118);}


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
            dbg.exitRule(getGrammarFileName(), "expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expression"


    // $ANTLR start "term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:1: term : ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(689, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:5: ( ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:7: ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( ws )?
            {
            dbg.location(690,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:7: ( unaryOperator )?
            int alt119=2;
            try { dbg.enterSubRule(119);
            try { dbg.enterDecision(119, decisionCanBacktrack[119]);

            int LA119_0 = input.LA(1);

            if ( (LA119_0==PLUS||LA119_0==MINUS) ) {
                alt119=1;
            }
            } finally {dbg.exitDecision(119);}

            switch (alt119) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:7: unaryOperator
                    {
                    dbg.location(690,7);
                    pushFollow(FOLLOW_unaryOperator_in_term3053);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(119);}

            dbg.location(691,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function )
            int alt120=7;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:692:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(692,9);
                    if ( (input.LA(1)>=NUMBER && input.LA(1)<=DIMENSION) ) {
                        input.consume();
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:7: STRING
                    {
                    dbg.location(705,7);
                    match(input,STRING,FOLLOW_STRING_in_term3273); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:7: IDENT
                    {
                    dbg.location(706,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term3281); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:7: GEN
                    {
                    dbg.location(707,7);
                    match(input,GEN,FOLLOW_GEN_in_term3289); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:7: URI
                    {
                    dbg.location(708,7);
                    match(input,URI,FOLLOW_URI_in_term3297); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:709:7: hexColor
                    {
                    dbg.location(709,7);
                    pushFollow(FOLLOW_hexColor_in_term3305);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:7: function
                    {
                    dbg.location(710,7);
                    pushFollow(FOLLOW_function_in_term3313);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(120);}

            dbg.location(712,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:5: ws
                    {
                    dbg.location(712,5);
                    pushFollow(FOLLOW_ws_in_term3325);
                    ws();

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
        dbg.location(713, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(715, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(716,5);
            pushFollow(FOLLOW_functionName_in_function3341);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(716,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:18: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:18: ws
                    {
                    dbg.location(716,18);
                    pushFollow(FOLLOW_ws_in_function3343);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(122);}

            dbg.location(717,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function3348); if (state.failed) return ;
            dbg.location(717,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:10: ws
                    {
                    dbg.location(717,10);
                    pushFollow(FOLLOW_ws_in_function3350);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(123);}

            dbg.location(718,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
            int alt126=2;
            try { dbg.enterSubRule(126);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:4: expression
                    {
                    dbg.location(719,4);
                    pushFollow(FOLLOW_expression_in_function3361);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(721,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(722,5);
                    pushFollow(FOLLOW_fnAttribute_in_function3379);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(722,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(125);

                    loop125:
                    do {
                        int alt125=2;
                        try { dbg.enterDecision(125, decisionCanBacktrack[125]);

                        int LA125_0 = input.LA(1);

                        if ( (LA125_0==COMMA) ) {
                            alt125=1;
                        }


                        } finally {dbg.exitDecision(125);}

                        switch (alt125) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(722,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function3382); if (state.failed) return ;
                    	    dbg.location(722,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:24: ( ws )?
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

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:24: ws
                    	            {
                    	            dbg.location(722,24);
                    	            pushFollow(FOLLOW_ws_in_function3384);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(124);}

                    	    dbg.location(722,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function3387);
                    	    fnAttribute();

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
                    break;

            }
            } finally {dbg.exitSubRule(126);}

            dbg.location(725,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function3408); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(726, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(732, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(736,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:4: ( IDENT COLON )?
            int alt127=2;
            try { dbg.enterSubRule(127);
            try { dbg.enterDecision(127, decisionCanBacktrack[127]);

            int LA127_0 = input.LA(1);

            if ( (LA127_0==IDENT) ) {
                int LA127_1 = input.LA(2);

                if ( (LA127_1==COLON) ) {
                    alt127=1;
                }
            }
            } finally {dbg.exitDecision(127);}

            switch (alt127) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:5: IDENT COLON
                    {
                    dbg.location(736,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName3456); if (state.failed) return ;
                    dbg.location(736,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName3458); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(127);}

            dbg.location(736,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName3462); if (state.failed) return ;
            dbg.location(736,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:25: ( DOT IDENT )*
            try { dbg.enterSubRule(128);

            loop128:
            do {
                int alt128=2;
                try { dbg.enterDecision(128, decisionCanBacktrack[128]);

                int LA128_0 = input.LA(1);

                if ( (LA128_0==DOT) ) {
                    alt128=1;
                }


                } finally {dbg.exitDecision(128);}

                switch (alt128) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:26: DOT IDENT
            	    {
            	    dbg.location(736,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName3465); if (state.failed) return ;
            	    dbg.location(736,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName3467); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop128;
                }
            } while (true);
            } finally {dbg.exitSubRule(128);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(737, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(739, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(740,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute3489);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(740,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:20: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:20: ws
                    {
                    dbg.location(740,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute3491);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(129);}

            dbg.location(740,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute3494); if (state.failed) return ;
            dbg.location(740,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:29: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:29: ws
                    {
                    dbg.location(740,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute3496);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(130);}

            dbg.location(740,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute3499);
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
        dbg.location(741, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(743, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:4: IDENT ( DOT IDENT )*
            {
            dbg.location(744,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName3514); if (state.failed) return ;
            dbg.location(744,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:10: ( DOT IDENT )*
            try { dbg.enterSubRule(131);

            loop131:
            do {
                int alt131=2;
                try { dbg.enterDecision(131, decisionCanBacktrack[131]);

                int LA131_0 = input.LA(1);

                if ( (LA131_0==DOT) ) {
                    alt131=1;
                }


                } finally {dbg.exitDecision(131);}

                switch (alt131) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:11: DOT IDENT
            	    {
            	    dbg.location(744,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName3517); if (state.failed) return ;
            	    dbg.location(744,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName3519); if (state.failed) return ;

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
        dbg.location(745, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(747, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:4: expression
            {
            dbg.location(748,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue3533);
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
        dbg.location(749, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(751, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:7: HASH
            {
            dbg.location(752,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor3551); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(753, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:755:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(755, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:7: ( WS | NL | COMMENT )+
            {
            dbg.location(756,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:756:7: ( WS | NL | COMMENT )+
            int cnt132=0;
            try { dbg.enterSubRule(132);

            loop132:
            do {
                int alt132=2;
                try { dbg.enterDecision(132, decisionCanBacktrack[132]);

                int LA132_0 = input.LA(1);

                if ( (LA132_0==WS||(LA132_0>=NL && LA132_0<=COMMENT)) ) {
                    alt132=1;
                }


                } finally {dbg.exitDecision(132);}

                switch (alt132) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(756,7);
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


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(757, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "ws");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "ws"

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:20: esPred
        {
        dbg.location(525,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css31787);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:527:6: esPred
        {
        dbg.location(527,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css31805);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:8: nsPred
        {
        dbg.location(541,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css31907);
        nsPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Css3

    // Delegated rules

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


    protected DFA91 dfa91 = new DFA91(this);
    protected DFA110 dfa110 = new DFA110(this);
    protected DFA118 dfa118 = new DFA118(this);
    protected DFA120 dfa120 = new DFA120(this);
    protected DFA126 dfa126 = new DFA126(this);
    static final String DFA91_eotS =
        "\21\uffff";
    static final String DFA91_eofS =
        "\21\uffff";
    static final String DFA91_minS =
        "\1\5\7\uffff\5\0\4\uffff";
    static final String DFA91_maxS =
        "\1\164\7\uffff\5\0\4\uffff";
    static final String DFA91_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA91_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\4\uffff}>";
    static final String[] DFA91_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\31\uffff\1\14\1\uffff"+
            "\3\1\1\uffff\1\10\1\12\1\13\1\14\2\1\11\uffff\1\1\62\uffff\1"+
            "\11",
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
            return "()* loopback of 525:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA91_8 = input.LA(1);

                         
                        int index91_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index91_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA91_9 = input.LA(1);

                         
                        int index91_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index91_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA91_10 = input.LA(1);

                         
                        int index91_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index91_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA91_11 = input.LA(1);

                         
                        int index91_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index91_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA91_12 = input.LA(1);

                         
                        int index91_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index91_12);
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
    static final String DFA110_eotS =
        "\4\uffff";
    static final String DFA110_eofS =
        "\4\uffff";
    static final String DFA110_minS =
        "\2\5\2\uffff";
    static final String DFA110_maxS =
        "\2\164\2\uffff";
    static final String DFA110_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA110_specialS =
        "\4\uffff}>";
    static final String[] DFA110_transitionS = {
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff\1\1\27\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\10\uffff\1\2\1\3\14\uffff\2\1\44"+
            "\uffff\1\3",
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff\1\1\27\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\10\uffff\1\2\1\3\14\uffff\2\1\44"+
            "\uffff\1\3",
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
            return "632:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA118_eotS =
        "\12\uffff";
    static final String DFA118_eofS =
        "\12\uffff";
    static final String DFA118_minS =
        "\1\5\1\uffff\1\5\1\uffff\4\5\2\24";
    static final String DFA118_maxS =
        "\1\115\1\uffff\1\117\1\uffff\2\117\1\5\3\117";
    static final String DFA118_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA118_specialS =
        "\12\uffff}>";
    static final String[] DFA118_transitionS = {
            "\3\3\1\uffff\1\1\3\uffff\1\1\1\2\3\uffff\1\3\32\uffff\2\3\2"+
            "\uffff\2\3\16\uffff\2\1\13\3",
            "",
            "\1\5\2\3\12\uffff\1\3\1\uffff\1\4\31\uffff\1\3\2\uffff\2\3"+
            "\20\uffff\13\3\2\4",
            "",
            "\1\5\2\3\12\uffff\1\3\1\uffff\1\4\31\uffff\1\3\2\uffff\2\3"+
            "\20\uffff\13\3\2\4",
            "\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\1\uffff\1\7\27\uffff"+
            "\3\3\2\uffff\2\3\1\6\5\uffff\1\1\6\uffff\16\3\2\7",
            "\1\10",
            "\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\1\uffff\1\7\30\uffff"+
            "\2\3\2\uffff\2\3\6\uffff\1\1\6\uffff\16\3\2\7",
            "\1\11\36\uffff\1\6\5\uffff\1\1\6\uffff\1\3\15\uffff\2\11",
            "\1\11\44\uffff\1\1\6\uffff\1\3\15\uffff\2\11"
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
            return "()* loopback of 686:12: ( operator term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA120_eotS =
        "\12\uffff";
    static final String DFA120_eofS =
        "\12\uffff";
    static final String DFA120_minS =
        "\1\5\2\uffff\1\5\4\uffff\1\5\1\uffff";
    static final String DFA120_maxS =
        "\1\115\2\uffff\1\117\4\uffff\1\117\1\uffff";
    static final String DFA120_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\3";
    static final String DFA120_specialS =
        "\12\uffff}>";
    static final String[] DFA120_transitionS = {
            "\1\3\1\2\1\5\12\uffff\1\4\37\uffff\1\6\20\uffff\13\1",
            "",
            "",
            "\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\1\uffff\1\10\27"+
            "\uffff\1\7\2\11\2\uffff\2\11\1\7\14\uffff\1\7\15\11\2\10",
            "",
            "",
            "",
            "",
            "\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\1\uffff\1\10\30"+
            "\uffff\2\11\2\uffff\2\11\15\uffff\1\7\15\11\2\10",
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
            return "691:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA126_eotS =
        "\10\uffff";
    static final String DFA126_eofS =
        "\10\uffff";
    static final String DFA126_minS =
        "\1\5\1\uffff\3\5\1\uffff\2\24";
    static final String DFA126_maxS =
        "\1\115\1\uffff\2\117\1\5\1\uffff\2\117";
    static final String DFA126_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA126_specialS =
        "\10\uffff}>";
    static final String[] DFA126_transitionS = {
            "\1\2\2\1\12\uffff\1\1\33\uffff\1\1\2\uffff\2\1\20\uffff\13\1",
            "",
            "\3\1\6\uffff\1\1\3\uffff\1\1\1\uffff\1\3\27\uffff\3\1\2\uffff"+
            "\2\1\1\4\5\uffff\1\5\6\uffff\2\1\1\uffff\13\1\2\3",
            "\3\1\6\uffff\1\1\3\uffff\1\1\1\uffff\1\3\30\uffff\2\1\2\uffff"+
            "\2\1\6\uffff\1\5\6\uffff\2\1\1\uffff\13\1\2\3",
            "\1\6",
            "",
            "\1\7\36\uffff\1\4\5\uffff\1\5\6\uffff\1\1\15\uffff\2\7",
            "\1\7\44\uffff\1\5\6\uffff\1\1\15\uffff\2\7"
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
            return "718:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0x00FC10000E2C0D30L,0x0010000000000000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0x00FC10000E3C0C30L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0x00FC10000E2C0C30L,0x0010000000000000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0x00FC10000E2C0830L,0x0010000000000000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0x00FC10000E2C0820L,0x0010000000000000L});
    public static final BitSet FOLLOW_body_in_styleSheet174 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_in_namespaces199 = new BitSet(new long[]{0x0000000000100012L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_namespaces201 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_NAMESPACE_SYM_in_namespace217 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_namespace219 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespace223 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_namespace225 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_namespace230 = new BitSet(new long[]{0x0000000000100200L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_namespace232 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_namespace235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_namespacePrefixName248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_resourceIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet286 = new BitSet(new long[]{0x0000000000100040L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_charSet288 = new BitSet(new long[]{0x0000000000100040L,0x000000000000C000L});
    public static final BitSet FOLLOW_charSetValue_in_charSet291 = new BitSet(new long[]{0x0000000000100200L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_charSet293 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_charSet296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_charSetValue310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_importItem_in_imports324 = new BitSet(new long[]{0x0000000000100402L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_imports326 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_importItem347 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_importItem349 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem352 = new BitSet(new long[]{0x0000000000170220L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_importItem354 = new BitSet(new long[]{0x0000000000070220L,0x0000000000000001L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem357 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_importItem359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media375 = new BitSet(new long[]{0x0000000000171020L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_media377 = new BitSet(new long[]{0x0000000000071020L,0x0000000000000001L});
    public static final BitSet FOLLOW_mediaQueryList_in_media380 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_media390 = new BitSet(new long[]{0x00FC10000A142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_media392 = new BitSet(new long[]{0x00FC10000A042020L,0x0010000000000000L});
    public static final BitSet FOLLOW_rule_in_media411 = new BitSet(new long[]{0x00FC10000A142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_page_in_media415 = new BitSet(new long[]{0x00FC10000A142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_fontFace_in_media419 = new BitSet(new long[]{0x00FC10000A142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_media423 = new BitSet(new long[]{0x00FC10000A042020L,0x0010000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_media437 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList453 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList457 = new BitSet(new long[]{0x0000000000170020L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList459 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000001L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList462 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery481 = new BitSet(new long[]{0x0000000000170020L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery483 = new BitSet(new long[]{0x0000000000070020L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery490 = new BitSet(new long[]{0x0000000000108002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery492 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery497 = new BitSet(new long[]{0x0000000000170020L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_mediaQuery499 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000001L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery502 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery510 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery514 = new BitSet(new long[]{0x0000000000170020L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_mediaQuery516 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000001L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery519 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression571 = new BitSet(new long[]{0x0000000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression573 = new BitSet(new long[]{0x0000000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression576 = new BitSet(new long[]{0x0000100000100000L,0x000000000000C002L});
    public static final BitSet FOLLOW_ws_in_mediaExpression578 = new BitSet(new long[]{0x0000100000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression583 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_ws_in_mediaExpression585 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_expression_in_mediaExpression588 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression593 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression595 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature608 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body624 = new BitSet(new long[]{0x00FC10000E3C0822L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_body626 = new BitSet(new long[]{0x00FC10000E2C0822L,0x0010000000000000L});
    public static final BitSet FOLLOW_rule_in_bodyItem651 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem675 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vendorAtRule_in_bodyItem711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_vendorAtRule734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_vendorAtRule738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GENERIC_AT_RULE_in_generic_at_rule754 = new BitSet(new long[]{0x0000000000101060L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule756 = new BitSet(new long[]{0x0000000000101060L});
    public static final BitSet FOLLOW_set_in_generic_at_rule761 = new BitSet(new long[]{0x0000000000101000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule771 = new BitSet(new long[]{0x0000000000101000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule786 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule798 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document824 = new BitSet(new long[]{0x0000000001D00080L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document826 = new BitSet(new long[]{0x0000000001D00080L,0x000000000000C000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document831 = new BitSet(new long[]{0x0000000000105000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document833 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_COMMA_in_moz_document839 = new BitSet(new long[]{0x0000000001D00080L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document841 = new BitSet(new long[]{0x0000000001D00080L,0x000000000000C000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document844 = new BitSet(new long[]{0x0000000000105000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document846 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document853 = new BitSet(new long[]{0x00FC100002142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document855 = new BitSet(new long[]{0x00FC100002042020L,0x0010000000000000L});
    public static final BitSet FOLLOW_rule_in_moz_document864 = new BitSet(new long[]{0x00FC100002142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_page_in_moz_document868 = new BitSet(new long[]{0x00FC100002142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document872 = new BitSet(new long[]{0x00FC100002042020L,0x0010000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document878 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page920 = new BitSet(new long[]{0x0000100000101020L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page922 = new BitSet(new long[]{0x0000100000001020L});
    public static final BitSet FOLLOW_IDENT_in_page927 = new BitSet(new long[]{0x0000100000101000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page929 = new BitSet(new long[]{0x0000100000001000L});
    public static final BitSet FOLLOW_pseudoPage_in_page936 = new BitSet(new long[]{0x0000000000101000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page938 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_page951 = new BitSet(new long[]{0x00000FFFF0142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page953 = new BitSet(new long[]{0x00000FFFF0042220L});
    public static final BitSet FOLLOW_declaration_in_page1008 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1010 = new BitSet(new long[]{0x0000000000102200L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page1012 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_SEMI_in_page1018 = new BitSet(new long[]{0x00000FFFF0142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page1020 = new BitSet(new long[]{0x00000FFFF0042220L});
    public static final BitSet FOLLOW_declaration_in_page1024 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1026 = new BitSet(new long[]{0x0000000000102200L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page1028 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_RBRACE_in_page1043 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1064 = new BitSet(new long[]{0x0000000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1066 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1069 = new BitSet(new long[]{0x0000000000101000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1071 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1082 = new BitSet(new long[]{0x0000000000142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1084 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle1087 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1091 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1101 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1122 = new BitSet(new long[]{0x0000000000101000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_fontFace1124 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1135 = new BitSet(new long[]{0x0000000000142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_fontFace1137 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_fontFace1140 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_fontFace1144 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1154 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1169 = new BitSet(new long[]{0x0000000000101000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_margin1171 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1174 = new BitSet(new long[]{0x0000000000142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_margin1176 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_margin1179 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_margin1181 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1412 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1414 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator1435 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_operator1437 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_operator1446 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_operator1448 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator1476 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_combinator1478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator1487 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_combinator1489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator1498 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_combinator1500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property1560 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_property1568 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule1593 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_rule1603 = new BitSet(new long[]{0x0000000000142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_rule1605 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_rule1608 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_rule1622 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_rule1632 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations1680 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_SEMI_in_declarations1684 = new BitSet(new long[]{0x0000000000140222L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_declarations1686 = new BitSet(new long[]{0x0000000000040222L});
    public static final BitSet FOLLOW_declaration_in_declarations1689 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1713 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup1716 = new BitSet(new long[]{0x00FC100000140020L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup1718 = new BitSet(new long[]{0x00FC100000040020L,0x0010000000000000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1721 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1744 = new BitSet(new long[]{0x00FDD00000040022L,0x0010000000000000L});
    public static final BitSet FOLLOW_combinator_in_selector1747 = new BitSet(new long[]{0x00FC100000040020L,0x0010000000000000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1749 = new BitSet(new long[]{0x00FDD00000040022L,0x0010000000000000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence1783 = new BitSet(new long[]{0x00FC100000040022L,0x0010000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1790 = new BitSet(new long[]{0x00FC100000040022L,0x0010000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1808 = new BitSet(new long[]{0x00FC100000040022L,0x0010000000000000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector1910 = new BitSet(new long[]{0x00C0000000040020L});
    public static final BitSet FOLLOW_elementName_in_typeSelector1916 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_typeSelector1918 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nsPred1938 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred1947 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix1962 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix1966 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix1970 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent2004 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent2013 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent2025 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent2037 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_elementSubsequent2049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId2072 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_116_in_cssId2078 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId2080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2108 = new BitSet(new long[]{0x0000000000040020L});
    public static final BitSet FOLLOW_set_in_cssClass2110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute2179 = new BitSet(new long[]{0x00C0000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute2186 = new BitSet(new long[]{0x00C0000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2189 = new BitSet(new long[]{0x00C0000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute2200 = new BitSet(new long[]{0xFE00000000100000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2202 = new BitSet(new long[]{0xFE00000000000000L});
    public static final BitSet FOLLOW_set_in_slAttribute2244 = new BitSet(new long[]{0x0000000000100060L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2424 = new BitSet(new long[]{0x0000000000100060L,0x000000000000C000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute2443 = new BitSet(new long[]{0x8000000000100000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2461 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute2490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName2506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue2520 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo2580 = new BitSet(new long[]{0x0000000000060020L});
    public static final BitSet FOLLOW_set_in_pseudo2644 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_pseudo2701 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2704 = new BitSet(new long[]{0x00464000001400E0L,0x000000000000FFFAL});
    public static final BitSet FOLLOW_ws_in_pseudo2706 = new BitSet(new long[]{0x00464000001400E0L,0x000000000000FFFAL});
    public static final BitSet FOLLOW_expression_in_pseudo2711 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_pseudo2715 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2720 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo2799 = new BitSet(new long[]{0x0000000000100000L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_pseudo2801 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2804 = new BitSet(new long[]{0x00FC100000140020L,0x001000000000C002L});
    public static final BitSet FOLLOW_ws_in_pseudo2806 = new BitSet(new long[]{0x00FC100000040020L,0x0010000000000002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo2809 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2812 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration2856 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_COLON_in_declaration2858 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_ws_in_declaration2860 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_propertyValue_in_declaration2863 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_prio_in_declaration2865 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue2889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio3001 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_prio3003 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression3025 = new BitSet(new long[]{0x00066000001440E2L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_operator_in_expression3028 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_term_in_expression3030 = new BitSet(new long[]{0x00066000001440E2L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_unaryOperator_in_term3053 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_set_in_term3074 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_STRING_in_term3273 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_IDENT_in_term3281 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_GEN_in_term3289 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_URI_in_term3297 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_hexColor_in_term3305 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_function_in_term3313 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_term3325 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function3341 = new BitSet(new long[]{0x0000000000100000L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_function3343 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_LPAREN_in_function3348 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_ws_in_function3350 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_expression_in_function3361 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_fnAttribute_in_function3379 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_function3382 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_ws_in_function3384 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_fnAttribute_in_function3387 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_function3408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName3456 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_COLON_in_functionName3458 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_functionName3462 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName3465 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_functionName3467 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute3489 = new BitSet(new long[]{0x0200000000100000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_fnAttribute3491 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute3494 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_ws_in_fnAttribute3496 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute3499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName3514 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName3517 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName3519 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue3533 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor3551 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws3572 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css31787 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css31805 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css31907 = new BitSet(new long[]{0x0000000000000002L});

}