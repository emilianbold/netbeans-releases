// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2012-02-02 13:27:48

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "AND", "ONLY", "NOT", "GEN", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH", "DOT", "LBRACKET", "DCOLON", "STAR", "PIPE", "NAME", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "PERCENTAGE", "LENGTH", "EMS", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "WS", "NL", "COMMENT", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "'#'"
    };
    public static final int EOF=-1;
    public static final int T__114=114;
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
    public static final int MOZ_DOCUMENT_SYM=19;
    public static final int MOZ_URL_PREFIX=20;
    public static final int MOZ_DOMAIN=21;
    public static final int MOZ_REGEXP=22;
    public static final int PAGE_SYM=23;
    public static final int COUNTER_STYLE_SYM=24;
    public static final int FONT_FACE_SYM=25;
    public static final int TOPLEFTCORNER_SYM=26;
    public static final int TOPLEFT_SYM=27;
    public static final int TOPCENTER_SYM=28;
    public static final int TOPRIGHT_SYM=29;
    public static final int TOPRIGHTCORNER_SYM=30;
    public static final int BOTTOMLEFTCORNER_SYM=31;
    public static final int BOTTOMLEFT_SYM=32;
    public static final int BOTTOMCENTER_SYM=33;
    public static final int BOTTOMRIGHT_SYM=34;
    public static final int BOTTOMRIGHTCORNER_SYM=35;
    public static final int LEFTTOP_SYM=36;
    public static final int LEFTMIDDLE_SYM=37;
    public static final int LEFTBOTTOM_SYM=38;
    public static final int RIGHTTOP_SYM=39;
    public static final int RIGHTMIDDLE_SYM=40;
    public static final int RIGHTBOTTOM_SYM=41;
    public static final int COLON=42;
    public static final int SOLIDUS=43;
    public static final int PLUS=44;
    public static final int GREATER=45;
    public static final int TILDE=46;
    public static final int MINUS=47;
    public static final int HASH=48;
    public static final int DOT=49;
    public static final int LBRACKET=50;
    public static final int DCOLON=51;
    public static final int STAR=52;
    public static final int PIPE=53;
    public static final int NAME=54;
    public static final int OPEQ=55;
    public static final int INCLUDES=56;
    public static final int DASHMATCH=57;
    public static final int BEGINS=58;
    public static final int ENDS=59;
    public static final int CONTAINS=60;
    public static final int RBRACKET=61;
    public static final int LPAREN=62;
    public static final int RPAREN=63;
    public static final int IMPORTANT_SYM=64;
    public static final int NUMBER=65;
    public static final int PERCENTAGE=66;
    public static final int LENGTH=67;
    public static final int EMS=68;
    public static final int EXS=69;
    public static final int ANGLE=70;
    public static final int TIME=71;
    public static final int FREQ=72;
    public static final int RESOLUTION=73;
    public static final int DIMENSION=74;
    public static final int WS=75;
    public static final int NL=76;
    public static final int COMMENT=77;
    public static final int HEXCHAR=78;
    public static final int NONASCII=79;
    public static final int UNICODE=80;
    public static final int ESCAPE=81;
    public static final int NMSTART=82;
    public static final int NMCHAR=83;
    public static final int URL=84;
    public static final int A=85;
    public static final int B=86;
    public static final int C=87;
    public static final int D=88;
    public static final int E=89;
    public static final int F=90;
    public static final int G=91;
    public static final int H=92;
    public static final int I=93;
    public static final int J=94;
    public static final int K=95;
    public static final int L=96;
    public static final int M=97;
    public static final int N=98;
    public static final int O=99;
    public static final int P=100;
    public static final int Q=101;
    public static final int R=102;
    public static final int S=103;
    public static final int T=104;
    public static final int U=105;
    public static final int V=106;
    public static final int W=107;
    public static final int X=108;
    public static final int Y=109;
    public static final int Z=110;
    public static final int CDO=111;
    public static final int CDC=112;
    public static final int INVALID=113;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "slAttribute", "simpleSelectorSequence", "margin_sym", 
        "namespacePrefixName", "combinator", "declaration", "selector", 
        "nsPred", "function", "slAttributeName", "elementSubsequent", "bodyItem", 
        "syncTo_IDENT_RBRACE", "mediaType", "synpred3_Css3", "counterStyle", 
        "pseudo", "synpred1_Css3", "mediaQuery", "functionName", "mediaFeature", 
        "mediaQueryOperator", "elementName", "fontFace", "mediaQueryList", 
        "importItem", "cssClass", "propertyValue", "expr", "rule", "charSet", 
        "ws", "fnAttributeValue", "declarations", "typeSelector", "moz_document", 
        "namespaces", "body", "selectorsGroup", "syncToFollow", "charSetValue", 
        "margin", "moz_document_function", "media", "page", "slAttributeValue", 
        "term", "styleSheet", "property", "cssId", "operator", "esPred", 
        "hexColor", "fnAttributeName", "namespace", "prio", "fnAttribute", 
        "synpred2_Css3", "resourceIdentifier", "unaryOperator", "namespacePrefix", 
        "imports", "pseudoPage", "mediaExpression"
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
            false, false, false, false, true, true, false, true, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false
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
        
        



    // $ANTLR start "styleSheet"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:255:1: styleSheet : ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(255, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:256:5: ( ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:257:6: ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF
            {
            dbg.location(257,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:257:6: ( ws )?
            int alt1=2;
            try { dbg.enterSubRule(1);
            try { dbg.enterDecision(1, decisionCanBacktrack[1]);

            int LA1_0 = input.LA(1);

            if ( ((LA1_0>=WS && LA1_0<=COMMENT)) ) {
                alt1=1;
            }
            } finally {dbg.exitDecision(1);}

            switch (alt1) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:257:6: ws
                    {
                    dbg.location(257,6);
                    pushFollow(FOLLOW_ws_in_styleSheet125);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(1);}

            dbg.location(258,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:6: ( charSet ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:8: charSet ( ws )?
                    {
                    dbg.location(258,8);
                    pushFollow(FOLLOW_charSet_in_styleSheet135);
                    charSet();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(258,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:16: ( ws )?
                    int alt2=2;
                    try { dbg.enterSubRule(2);
                    try { dbg.enterDecision(2, decisionCanBacktrack[2]);

                    int LA2_0 = input.LA(1);

                    if ( ((LA2_0>=WS && LA2_0<=COMMENT)) ) {
                        alt2=1;
                    }
                    } finally {dbg.exitDecision(2);}

                    switch (alt2) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:16: ws
                            {
                            dbg.location(258,16);
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

            dbg.location(259,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:9: ( imports )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:9: imports
                    {
                    dbg.location(259,9);
                    pushFollow(FOLLOW_imports_in_styleSheet151);
                    imports();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(4);}

            dbg.location(260,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:260:9: ( namespaces )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:260:9: namespaces
                    {
                    dbg.location(260,9);
                    pushFollow(FOLLOW_namespaces_in_styleSheet162);
                    namespaces();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(5);}

            dbg.location(261,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:261:9: ( body )?
            int alt6=2;
            try { dbg.enterSubRule(6);
            try { dbg.enterDecision(6, decisionCanBacktrack[6]);

            int LA6_0 = input.LA(1);

            if ( (LA6_0==IDENT||LA6_0==MEDIA_SYM||(LA6_0>=GEN && LA6_0<=MOZ_DOCUMENT_SYM)||(LA6_0>=PAGE_SYM && LA6_0<=FONT_FACE_SYM)||LA6_0==COLON||(LA6_0>=HASH && LA6_0<=PIPE)||LA6_0==114) ) {
                alt6=1;
            }
            } finally {dbg.exitDecision(6);}

            switch (alt6) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:261:9: body
                    {
                    dbg.location(261,9);
                    pushFollow(FOLLOW_body_in_styleSheet174);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(6);}

            dbg.location(262,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet182); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(263, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:1: namespaces : ( namespace ( ws )? )+ ;
    public final void namespaces() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespaces");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(265, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:266:2: ( ( namespace ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:2: ( namespace ( ws )? )+
            {
            dbg.location(267,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:2: ( namespace ( ws )? )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:4: namespace ( ws )?
            	    {
            	    dbg.location(267,4);
            	    pushFollow(FOLLOW_namespace_in_namespaces199);
            	    namespace();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(267,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:14: ( ws )?
            	    int alt7=2;
            	    try { dbg.enterSubRule(7);
            	    try { dbg.enterDecision(7, decisionCanBacktrack[7]);

            	    int LA7_0 = input.LA(1);

            	    if ( ((LA7_0>=WS && LA7_0<=COMMENT)) ) {
            	        alt7=1;
            	    }
            	    } finally {dbg.exitDecision(7);}

            	    switch (alt7) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:14: ws
            	            {
            	            dbg.location(267,14);
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
        dbg.location(268, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:270:1: namespace : NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';' ;
    public final void namespace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(270, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:3: ( NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:5: NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';'
            {
            dbg.location(271,5);
            match(input,NAMESPACE_SYM,FOLLOW_NAMESPACE_SYM_in_namespace217); if (state.failed) return ;
            dbg.location(271,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:19: ( ws )?
            int alt9=2;
            try { dbg.enterSubRule(9);
            try { dbg.enterDecision(9, decisionCanBacktrack[9]);

            int LA9_0 = input.LA(1);

            if ( ((LA9_0>=WS && LA9_0<=COMMENT)) ) {
                alt9=1;
            }
            } finally {dbg.exitDecision(9);}

            switch (alt9) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:19: ws
                    {
                    dbg.location(271,19);
                    pushFollow(FOLLOW_ws_in_namespace219);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(9);}

            dbg.location(271,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:23: ( namespacePrefixName ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:24: namespacePrefixName ( ws )?
                    {
                    dbg.location(271,24);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespace223);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(271,44);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:44: ( ws )?
                    int alt10=2;
                    try { dbg.enterSubRule(10);
                    try { dbg.enterDecision(10, decisionCanBacktrack[10]);

                    int LA10_0 = input.LA(1);

                    if ( ((LA10_0>=WS && LA10_0<=COMMENT)) ) {
                        alt10=1;
                    }
                    } finally {dbg.exitDecision(10);}

                    switch (alt10) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:44: ws
                            {
                            dbg.location(271,44);
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

            dbg.location(271,50);
            pushFollow(FOLLOW_resourceIdentifier_in_namespace230);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(271,69);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:69: ( ws )?
            int alt12=2;
            try { dbg.enterSubRule(12);
            try { dbg.enterDecision(12, decisionCanBacktrack[12]);

            int LA12_0 = input.LA(1);

            if ( ((LA12_0>=WS && LA12_0<=COMMENT)) ) {
                alt12=1;
            }
            } finally {dbg.exitDecision(12);}

            switch (alt12) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:69: ws
                    {
                    dbg.location(271,69);
                    pushFollow(FOLLOW_ws_in_namespace232);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(12);}

            dbg.location(271,73);
            match(input,SEMI,FOLLOW_SEMI_in_namespace235); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(272, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:274:1: namespacePrefixName : IDENT ;
    public final void namespacePrefixName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefixName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(274, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:275:3: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:275:5: IDENT
            {
            dbg.location(275,5);
            match(input,IDENT,FOLLOW_IDENT_in_namespacePrefixName248); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(276, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:278:1: resourceIdentifier : ( STRING | URI );
    public final void resourceIdentifier() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "resourceIdentifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(278, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:279:3: ( STRING | URI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(279,3);
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
        dbg.location(280, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:282:1: charSet : CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI ;
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(282, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:283:5: ( CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:283:9: CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI
            {
            dbg.location(283,9);
            match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet286); if (state.failed) return ;
            dbg.location(283,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:283:21: ( ws )?
            int alt13=2;
            try { dbg.enterSubRule(13);
            try { dbg.enterDecision(13, decisionCanBacktrack[13]);

            int LA13_0 = input.LA(1);

            if ( ((LA13_0>=WS && LA13_0<=COMMENT)) ) {
                alt13=1;
            }
            } finally {dbg.exitDecision(13);}

            switch (alt13) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:283:21: ws
                    {
                    dbg.location(283,21);
                    pushFollow(FOLLOW_ws_in_charSet288);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(13);}

            dbg.location(283,25);
            pushFollow(FOLLOW_charSetValue_in_charSet291);
            charSetValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(283,38);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:283:38: ( ws )?
            int alt14=2;
            try { dbg.enterSubRule(14);
            try { dbg.enterDecision(14, decisionCanBacktrack[14]);

            int LA14_0 = input.LA(1);

            if ( ((LA14_0>=WS && LA14_0<=COMMENT)) ) {
                alt14=1;
            }
            } finally {dbg.exitDecision(14);}

            switch (alt14) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:283:38: ws
                    {
                    dbg.location(283,38);
                    pushFollow(FOLLOW_ws_in_charSet293);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(14);}

            dbg.location(283,42);
            match(input,SEMI,FOLLOW_SEMI_in_charSet296); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(284, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:286:1: charSetValue : STRING ;
    public final void charSetValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSetValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(286, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:2: ( STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:4: STRING
            {
            dbg.location(287,4);
            match(input,STRING,FOLLOW_STRING_in_charSetValue310); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(288, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:290:1: imports : ( importItem ( ws )? )+ ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(290, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:291:2: ( ( importItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:292:2: ( importItem ( ws )? )+
            {
            dbg.location(292,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:292:2: ( importItem ( ws )? )+
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:292:4: importItem ( ws )?
            	    {
            	    dbg.location(292,4);
            	    pushFollow(FOLLOW_importItem_in_imports324);
            	    importItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(292,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:292:15: ( ws )?
            	    int alt15=2;
            	    try { dbg.enterSubRule(15);
            	    try { dbg.enterDecision(15, decisionCanBacktrack[15]);

            	    int LA15_0 = input.LA(1);

            	    if ( ((LA15_0>=WS && LA15_0<=COMMENT)) ) {
            	        alt15=1;
            	    }
            	    } finally {dbg.exitDecision(15);}

            	    switch (alt15) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:292:15: ws
            	            {
            	            dbg.location(292,15);
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
        dbg.location(293, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:295:1: importItem : IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI ;
    public final void importItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "importItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(295, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:5: ( IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:9: IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI
            {
            dbg.location(296,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_importItem347); if (state.failed) return ;
            dbg.location(296,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:20: ( ws )?
            int alt17=2;
            try { dbg.enterSubRule(17);
            try { dbg.enterDecision(17, decisionCanBacktrack[17]);

            int LA17_0 = input.LA(1);

            if ( ((LA17_0>=WS && LA17_0<=COMMENT)) ) {
                alt17=1;
            }
            } finally {dbg.exitDecision(17);}

            switch (alt17) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:20: ws
                    {
                    dbg.location(296,20);
                    pushFollow(FOLLOW_ws_in_importItem349);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(17);}

            dbg.location(296,24);
            pushFollow(FOLLOW_resourceIdentifier_in_importItem352);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(296,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:43: ( ws )?
            int alt18=2;
            try { dbg.enterSubRule(18);
            try { dbg.enterDecision(18, decisionCanBacktrack[18]);

            int LA18_0 = input.LA(1);

            if ( ((LA18_0>=WS && LA18_0<=COMMENT)) ) {
                alt18=1;
            }
            } finally {dbg.exitDecision(18);}

            switch (alt18) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:43: ws
                    {
                    dbg.location(296,43);
                    pushFollow(FOLLOW_ws_in_importItem354);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(18);}

            dbg.location(296,47);
            pushFollow(FOLLOW_mediaQueryList_in_importItem357);
            mediaQueryList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(296,62);
            match(input,SEMI,FOLLOW_SEMI_in_importItem359); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(297, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:298:1: media : MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(298, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:5: ( MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:7: MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE
            {
            dbg.location(299,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media375); if (state.failed) return ;
            dbg.location(299,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:17: ( ws )?
            int alt19=2;
            try { dbg.enterSubRule(19);
            try { dbg.enterDecision(19, decisionCanBacktrack[19]);

            int LA19_0 = input.LA(1);

            if ( ((LA19_0>=WS && LA19_0<=COMMENT)) ) {
                alt19=1;
            }
            } finally {dbg.exitDecision(19);}

            switch (alt19) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:17: ws
                    {
                    dbg.location(299,17);
                    pushFollow(FOLLOW_ws_in_media377);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(19);}

            dbg.location(299,21);
            pushFollow(FOLLOW_mediaQueryList_in_media380);
            mediaQueryList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(300,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media390); if (state.failed) return ;
            dbg.location(300,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:300:16: ( ws )?
            int alt20=2;
            try { dbg.enterSubRule(20);
            try { dbg.enterDecision(20, decisionCanBacktrack[20]);

            int LA20_0 = input.LA(1);

            if ( ((LA20_0>=WS && LA20_0<=COMMENT)) ) {
                alt20=1;
            }
            } finally {dbg.exitDecision(20);}

            switch (alt20) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:300:16: ws
                    {
                    dbg.location(300,16);
                    pushFollow(FOLLOW_ws_in_media392);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(20);}

            dbg.location(301,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:13: ( ( rule | page ) ( ws )? )*
            try { dbg.enterSubRule(23);

            loop23:
            do {
                int alt23=2;
                try { dbg.enterDecision(23, decisionCanBacktrack[23]);

                int LA23_0 = input.LA(1);

                if ( (LA23_0==IDENT||LA23_0==GEN||LA23_0==PAGE_SYM||LA23_0==COLON||(LA23_0>=HASH && LA23_0<=PIPE)||LA23_0==114) ) {
                    alt23=1;
                }


                } finally {dbg.exitDecision(23);}

                switch (alt23) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:15: ( rule | page ) ( ws )?
            	    {
            	    dbg.location(301,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:15: ( rule | page )
            	    int alt21=2;
            	    try { dbg.enterSubRule(21);
            	    try { dbg.enterDecision(21, decisionCanBacktrack[21]);

            	    int LA21_0 = input.LA(1);

            	    if ( (LA21_0==IDENT||LA21_0==GEN||LA21_0==COLON||(LA21_0>=HASH && LA21_0<=PIPE)||LA21_0==114) ) {
            	        alt21=1;
            	    }
            	    else if ( (LA21_0==PAGE_SYM) ) {
            	        alt21=2;
            	    }
            	    else {
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:17: rule
            	            {
            	            dbg.location(301,17);
            	            pushFollow(FOLLOW_rule_in_media411);
            	            rule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:24: page
            	            {
            	            dbg.location(301,24);
            	            pushFollow(FOLLOW_page_in_media415);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(21);}

            	    dbg.location(301,31);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:31: ( ws )?
            	    int alt22=2;
            	    try { dbg.enterSubRule(22);
            	    try { dbg.enterDecision(22, decisionCanBacktrack[22]);

            	    int LA22_0 = input.LA(1);

            	    if ( ((LA22_0>=WS && LA22_0<=COMMENT)) ) {
            	        alt22=1;
            	    }
            	    } finally {dbg.exitDecision(22);}

            	    switch (alt22) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:31: ws
            	            {
            	            dbg.location(301,31);
            	            pushFollow(FOLLOW_ws_in_media419);
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

            dbg.location(302,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media433); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(303, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:305:1: mediaQueryList : ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? ;
    public final void mediaQueryList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(305, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:2: ( ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            {
            dbg.location(306,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:6: mediaQuery ( COMMA ( ws )? mediaQuery )*
                    {
                    dbg.location(306,6);
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList449);
                    mediaQuery();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(306,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:17: ( COMMA ( ws )? mediaQuery )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:19: COMMA ( ws )? mediaQuery
                    	    {
                    	    dbg.location(306,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList453); if (state.failed) return ;
                    	    dbg.location(306,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:25: ( ws )?
                    	    int alt24=2;
                    	    try { dbg.enterSubRule(24);
                    	    try { dbg.enterDecision(24, decisionCanBacktrack[24]);

                    	    int LA24_0 = input.LA(1);

                    	    if ( ((LA24_0>=WS && LA24_0<=COMMENT)) ) {
                    	        alt24=1;
                    	    }
                    	    } finally {dbg.exitDecision(24);}

                    	    switch (alt24) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:25: ws
                    	            {
                    	            dbg.location(306,25);
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList455);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(24);}

                    	    dbg.location(306,29);
                    	    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList458);
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
        dbg.location(307, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:309:1: mediaQuery : ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* );
    public final void mediaQuery() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQuery");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(309, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:2: ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:4: ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(310,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:4: ( mediaQueryOperator ( ws )? )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:5: mediaQueryOperator ( ws )?
                            {
                            dbg.location(310,5);
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery477);
                            mediaQueryOperator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(310,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:24: ( ws )?
                            int alt27=2;
                            try { dbg.enterSubRule(27);
                            try { dbg.enterDecision(27, decisionCanBacktrack[27]);

                            int LA27_0 = input.LA(1);

                            if ( ((LA27_0>=WS && LA27_0<=COMMENT)) ) {
                                alt27=1;
                            }
                            } finally {dbg.exitDecision(27);}

                            switch (alt27) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:24: ws
                                    {
                                    dbg.location(310,24);
                                    pushFollow(FOLLOW_ws_in_mediaQuery479);
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

                    dbg.location(310,32);
                    pushFollow(FOLLOW_mediaType_in_mediaQuery486);
                    mediaType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(310,42);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:42: ( ws )?
                    int alt29=2;
                    try { dbg.enterSubRule(29);
                    try { dbg.enterDecision(29, decisionCanBacktrack[29]);

                    int LA29_0 = input.LA(1);

                    if ( ((LA29_0>=WS && LA29_0<=COMMENT)) ) {
                        alt29=1;
                    }
                    } finally {dbg.exitDecision(29);}

                    switch (alt29) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:42: ws
                            {
                            dbg.location(310,42);
                            pushFollow(FOLLOW_ws_in_mediaQuery488);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(29);}

                    dbg.location(310,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:46: ( AND ( ws )? mediaExpression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:48: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(310,48);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery493); if (state.failed) return ;
                    	    dbg.location(310,52);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:52: ( ws )?
                    	    int alt30=2;
                    	    try { dbg.enterSubRule(30);
                    	    try { dbg.enterDecision(30, decisionCanBacktrack[30]);

                    	    int LA30_0 = input.LA(1);

                    	    if ( ((LA30_0>=WS && LA30_0<=COMMENT)) ) {
                    	        alt30=1;
                    	    }
                    	    } finally {dbg.exitDecision(30);}

                    	    switch (alt30) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:52: ws
                    	            {
                    	            dbg.location(310,52);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery495);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(30);}

                    	    dbg.location(310,56);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery498);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:4: mediaExpression ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(311,4);
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery506);
                    mediaExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(311,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:20: ( AND ( ws )? mediaExpression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:22: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(311,22);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery510); if (state.failed) return ;
                    	    dbg.location(311,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:26: ( ws )?
                    	    int alt32=2;
                    	    try { dbg.enterSubRule(32);
                    	    try { dbg.enterDecision(32, decisionCanBacktrack[32]);

                    	    int LA32_0 = input.LA(1);

                    	    if ( ((LA32_0>=WS && LA32_0<=COMMENT)) ) {
                    	        alt32=1;
                    	    }
                    	    } finally {dbg.exitDecision(32);}

                    	    switch (alt32) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:26: ws
                    	            {
                    	            dbg.location(311,26);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery512);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(32);}

                    	    dbg.location(311,30);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery515);
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
        dbg.location(312, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:314:1: mediaQueryOperator : ( ONLY | NOT );
    public final void mediaQueryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(314, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:315:3: ( ONLY | NOT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(315,3);
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
        dbg.location(316, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:318:1: mediaType : ( IDENT | GEN );
    public final void mediaType() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(318, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(319,2);
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
        dbg.location(320, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:322:1: mediaExpression : '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expr )? ')' ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(322, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:2: ( '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expr )? ')' ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:4: '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expr )? ')' ( ws )?
            {
            dbg.location(323,4);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression567); if (state.failed) return ;
            dbg.location(323,8);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:8: ( ws )?
            int alt35=2;
            try { dbg.enterSubRule(35);
            try { dbg.enterDecision(35, decisionCanBacktrack[35]);

            int LA35_0 = input.LA(1);

            if ( ((LA35_0>=WS && LA35_0<=COMMENT)) ) {
                alt35=1;
            }
            } finally {dbg.exitDecision(35);}

            switch (alt35) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:8: ws
                    {
                    dbg.location(323,8);
                    pushFollow(FOLLOW_ws_in_mediaExpression569);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(35);}

            dbg.location(323,12);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression572);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(323,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:25: ( ws )?
            int alt36=2;
            try { dbg.enterSubRule(36);
            try { dbg.enterDecision(36, decisionCanBacktrack[36]);

            int LA36_0 = input.LA(1);

            if ( ((LA36_0>=WS && LA36_0<=COMMENT)) ) {
                alt36=1;
            }
            } finally {dbg.exitDecision(36);}

            switch (alt36) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:25: ws
                    {
                    dbg.location(323,25);
                    pushFollow(FOLLOW_ws_in_mediaExpression574);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(36);}

            dbg.location(323,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:29: ( ':' ( ws )? expr )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:31: ':' ( ws )? expr
                    {
                    dbg.location(323,31);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression579); if (state.failed) return ;
                    dbg.location(323,35);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:35: ( ws )?
                    int alt37=2;
                    try { dbg.enterSubRule(37);
                    try { dbg.enterDecision(37, decisionCanBacktrack[37]);

                    int LA37_0 = input.LA(1);

                    if ( ((LA37_0>=WS && LA37_0<=COMMENT)) ) {
                        alt37=1;
                    }
                    } finally {dbg.exitDecision(37);}

                    switch (alt37) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:35: ws
                            {
                            dbg.location(323,35);
                            pushFollow(FOLLOW_ws_in_mediaExpression581);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(37);}

                    dbg.location(323,39);
                    pushFollow(FOLLOW_expr_in_mediaExpression584);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(323,47);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression589); if (state.failed) return ;
            dbg.location(323,51);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:51: ( ws )?
            int alt39=2;
            try { dbg.enterSubRule(39);
            try { dbg.enterDecision(39, decisionCanBacktrack[39]);

            int LA39_0 = input.LA(1);

            if ( ((LA39_0>=WS && LA39_0<=COMMENT)) ) {
                alt39=1;
            }
            } finally {dbg.exitDecision(39);}

            switch (alt39) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:51: ws
                    {
                    dbg.location(323,51);
                    pushFollow(FOLLOW_ws_in_mediaExpression591);
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
        dbg.location(324, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:326:1: mediaFeature : IDENT ;
    public final void mediaFeature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaFeature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(326, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:327:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:327:4: IDENT
            {
            dbg.location(327,4);
            match(input,IDENT,FOLLOW_IDENT_in_mediaFeature604); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(328, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:2: body : ( bodyItem ( ws )? )+ ;
    public final void body() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "body");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(330, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:7: ( ( bodyItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:331:2: ( bodyItem ( ws )? )+
            {
            dbg.location(331,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:331:2: ( bodyItem ( ws )? )+
            int cnt41=0;
            try { dbg.enterSubRule(41);

            loop41:
            do {
                int alt41=2;
                try { dbg.enterDecision(41, decisionCanBacktrack[41]);

                int LA41_0 = input.LA(1);

                if ( (LA41_0==IDENT||LA41_0==MEDIA_SYM||(LA41_0>=GEN && LA41_0<=MOZ_DOCUMENT_SYM)||(LA41_0>=PAGE_SYM && LA41_0<=FONT_FACE_SYM)||LA41_0==COLON||(LA41_0>=HASH && LA41_0<=PIPE)||LA41_0==114) ) {
                    alt41=1;
                }


                } finally {dbg.exitDecision(41);}

                switch (alt41) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:331:4: bodyItem ( ws )?
            	    {
            	    dbg.location(331,4);
            	    pushFollow(FOLLOW_bodyItem_in_body620);
            	    bodyItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(331,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:331:13: ( ws )?
            	    int alt40=2;
            	    try { dbg.enterSubRule(40);
            	    try { dbg.enterDecision(40, decisionCanBacktrack[40]);

            	    int LA40_0 = input.LA(1);

            	    if ( ((LA40_0>=WS && LA40_0<=COMMENT)) ) {
            	        alt40=1;
            	    }
            	    } finally {dbg.exitDecision(40);}

            	    switch (alt40) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:331:13: ws
            	            {
            	            dbg.location(331,13);
            	            pushFollow(FOLLOW_ws_in_body622);
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
        dbg.location(332, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:334:1: bodyItem : ( rule | media | page | counterStyle | fontFace | moz_document );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(334, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:335:5: ( rule | media | page | counterStyle | fontFace | moz_document )
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
            case 114:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:336:6: rule
                    {
                    dbg.location(336,6);
                    pushFollow(FOLLOW_rule_in_bodyItem647);
                    rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:11: media
                    {
                    dbg.location(337,11);
                    pushFollow(FOLLOW_media_in_bodyItem659);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:11: page
                    {
                    dbg.location(338,11);
                    pushFollow(FOLLOW_page_in_bodyItem671);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:11: counterStyle
                    {
                    dbg.location(339,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyItem683);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:11: fontFace
                    {
                    dbg.location(340,11);
                    pushFollow(FOLLOW_fontFace_in_bodyItem695);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:11: moz_document
                    {
                    dbg.location(341,11);
                    pushFollow(FOLLOW_moz_document_in_bodyItem707);
                    moz_document();

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
        dbg.location(343, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "bodyItem");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "bodyItem"


    // $ANTLR start "moz_document"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(345, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:346:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE
            {
            dbg.location(347,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document734); if (state.failed) return ;
            dbg.location(347,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:19: ( ws )?
            int alt43=2;
            try { dbg.enterSubRule(43);
            try { dbg.enterDecision(43, decisionCanBacktrack[43]);

            int LA43_0 = input.LA(1);

            if ( ((LA43_0>=WS && LA43_0<=COMMENT)) ) {
                alt43=1;
            }
            } finally {dbg.exitDecision(43);}

            switch (alt43) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:19: ws
                    {
                    dbg.location(347,19);
                    pushFollow(FOLLOW_ws_in_moz_document736);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(43);}

            dbg.location(347,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:25: moz_document_function ( ws )?
            {
            dbg.location(347,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document741);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(347,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:47: ( ws )?
            int alt44=2;
            try { dbg.enterSubRule(44);
            try { dbg.enterDecision(44, decisionCanBacktrack[44]);

            int LA44_0 = input.LA(1);

            if ( ((LA44_0>=WS && LA44_0<=COMMENT)) ) {
                alt44=1;
            }
            } finally {dbg.exitDecision(44);}

            switch (alt44) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:47: ws
                    {
                    dbg.location(347,47);
                    pushFollow(FOLLOW_ws_in_moz_document743);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(44);}


            }

            dbg.location(347,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
            try { dbg.enterSubRule(47);

            loop47:
            do {
                int alt47=2;
                try { dbg.enterDecision(47, decisionCanBacktrack[47]);

                int LA47_0 = input.LA(1);

                if ( (LA47_0==COMMA) ) {
                    alt47=1;
                }


                } finally {dbg.exitDecision(47);}

                switch (alt47) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(347,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document749); if (state.failed) return ;
            	    dbg.location(347,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:60: ( ws )?
            	    int alt45=2;
            	    try { dbg.enterSubRule(45);
            	    try { dbg.enterDecision(45, decisionCanBacktrack[45]);

            	    int LA45_0 = input.LA(1);

            	    if ( ((LA45_0>=WS && LA45_0<=COMMENT)) ) {
            	        alt45=1;
            	    }
            	    } finally {dbg.exitDecision(45);}

            	    switch (alt45) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:60: ws
            	            {
            	            dbg.location(347,60);
            	            pushFollow(FOLLOW_ws_in_moz_document751);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(45);}

            	    dbg.location(347,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document754);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(347,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:86: ( ws )?
            	    int alt46=2;
            	    try { dbg.enterSubRule(46);
            	    try { dbg.enterDecision(46, decisionCanBacktrack[46]);

            	    int LA46_0 = input.LA(1);

            	    if ( ((LA46_0>=WS && LA46_0<=COMMENT)) ) {
            	        alt46=1;
            	    }
            	    } finally {dbg.exitDecision(46);}

            	    switch (alt46) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:86: ws
            	            {
            	            dbg.location(347,86);
            	            pushFollow(FOLLOW_ws_in_moz_document756);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(46);}


            	    }
            	    break;

            	default :
            	    break loop47;
                }
            } while (true);
            } finally {dbg.exitSubRule(47);}

            dbg.location(348,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document763); if (state.failed) return ;
            dbg.location(348,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:9: ( ws )?
            int alt48=2;
            try { dbg.enterSubRule(48);
            try { dbg.enterDecision(48, decisionCanBacktrack[48]);

            int LA48_0 = input.LA(1);

            if ( ((LA48_0>=WS && LA48_0<=COMMENT)) ) {
                alt48=1;
            }
            } finally {dbg.exitDecision(48);}

            switch (alt48) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:9: ws
                    {
                    dbg.location(348,9);
                    pushFollow(FOLLOW_ws_in_moz_document765);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(48);}

            dbg.location(349,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:3: ( ( rule | page ) ( ws )? )*
            try { dbg.enterSubRule(51);

            loop51:
            do {
                int alt51=2;
                try { dbg.enterDecision(51, decisionCanBacktrack[51]);

                int LA51_0 = input.LA(1);

                if ( (LA51_0==IDENT||LA51_0==GEN||LA51_0==PAGE_SYM||LA51_0==COLON||(LA51_0>=HASH && LA51_0<=PIPE)||LA51_0==114) ) {
                    alt51=1;
                }


                } finally {dbg.exitDecision(51);}

                switch (alt51) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:5: ( rule | page ) ( ws )?
            	    {
            	    dbg.location(349,5);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:5: ( rule | page )
            	    int alt49=2;
            	    try { dbg.enterSubRule(49);
            	    try { dbg.enterDecision(49, decisionCanBacktrack[49]);

            	    int LA49_0 = input.LA(1);

            	    if ( (LA49_0==IDENT||LA49_0==GEN||LA49_0==COLON||(LA49_0>=HASH && LA49_0<=PIPE)||LA49_0==114) ) {
            	        alt49=1;
            	    }
            	    else if ( (LA49_0==PAGE_SYM) ) {
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:7: rule
            	            {
            	            dbg.location(349,7);
            	            pushFollow(FOLLOW_rule_in_moz_document774);
            	            rule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:14: page
            	            {
            	            dbg.location(349,14);
            	            pushFollow(FOLLOW_page_in_moz_document778);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(49);}

            	    dbg.location(349,21);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:21: ( ws )?
            	    int alt50=2;
            	    try { dbg.enterSubRule(50);
            	    try { dbg.enterDecision(50, decisionCanBacktrack[50]);

            	    int LA50_0 = input.LA(1);

            	    if ( ((LA50_0>=WS && LA50_0<=COMMENT)) ) {
            	        alt50=1;
            	    }
            	    } finally {dbg.exitDecision(50);}

            	    switch (alt50) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:21: ws
            	            {
            	            dbg.location(349,21);
            	            pushFollow(FOLLOW_ws_in_moz_document782);
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

            dbg.location(350,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document788); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(351, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(353, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(354,2);
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
        dbg.location(356, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(358, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(359,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page830); if (state.failed) return ;
            dbg.location(359,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:16: ( ws )?
            int alt52=2;
            try { dbg.enterSubRule(52);
            try { dbg.enterDecision(52, decisionCanBacktrack[52]);

            int LA52_0 = input.LA(1);

            if ( ((LA52_0>=WS && LA52_0<=COMMENT)) ) {
                alt52=1;
            }
            } finally {dbg.exitDecision(52);}

            switch (alt52) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:16: ws
                    {
                    dbg.location(359,16);
                    pushFollow(FOLLOW_ws_in_page832);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(52);}

            dbg.location(359,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:20: ( IDENT ( ws )? )?
            int alt54=2;
            try { dbg.enterSubRule(54);
            try { dbg.enterDecision(54, decisionCanBacktrack[54]);

            int LA54_0 = input.LA(1);

            if ( (LA54_0==IDENT) ) {
                alt54=1;
            }
            } finally {dbg.exitDecision(54);}

            switch (alt54) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:22: IDENT ( ws )?
                    {
                    dbg.location(359,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page837); if (state.failed) return ;
                    dbg.location(359,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:28: ( ws )?
                    int alt53=2;
                    try { dbg.enterSubRule(53);
                    try { dbg.enterDecision(53, decisionCanBacktrack[53]);

                    int LA53_0 = input.LA(1);

                    if ( ((LA53_0>=WS && LA53_0<=COMMENT)) ) {
                        alt53=1;
                    }
                    } finally {dbg.exitDecision(53);}

                    switch (alt53) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:28: ws
                            {
                            dbg.location(359,28);
                            pushFollow(FOLLOW_ws_in_page839);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(53);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(54);}

            dbg.location(359,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:35: ( pseudoPage ( ws )? )?
            int alt56=2;
            try { dbg.enterSubRule(56);
            try { dbg.enterDecision(56, decisionCanBacktrack[56]);

            int LA56_0 = input.LA(1);

            if ( (LA56_0==COLON) ) {
                alt56=1;
            }
            } finally {dbg.exitDecision(56);}

            switch (alt56) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:36: pseudoPage ( ws )?
                    {
                    dbg.location(359,36);
                    pushFollow(FOLLOW_pseudoPage_in_page846);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(359,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:47: ( ws )?
                    int alt55=2;
                    try { dbg.enterSubRule(55);
                    try { dbg.enterDecision(55, decisionCanBacktrack[55]);

                    int LA55_0 = input.LA(1);

                    if ( ((LA55_0>=WS && LA55_0<=COMMENT)) ) {
                        alt55=1;
                    }
                    } finally {dbg.exitDecision(55);}

                    switch (alt55) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:47: ws
                            {
                            dbg.location(359,47);
                            pushFollow(FOLLOW_ws_in_page848);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(55);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(56);}

            dbg.location(360,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page861); if (state.failed) return ;
            dbg.location(360,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:16: ( ws )?
            int alt57=2;
            try { dbg.enterSubRule(57);
            try { dbg.enterDecision(57, decisionCanBacktrack[57]);

            int LA57_0 = input.LA(1);

            if ( ((LA57_0>=WS && LA57_0<=COMMENT)) ) {
                alt57=1;
            }
            } finally {dbg.exitDecision(57);}

            switch (alt57) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:16: ws
                    {
                    dbg.location(360,16);
                    pushFollow(FOLLOW_ws_in_page863);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(57);}

            dbg.location(364,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:13: ( declaration | margin ( ws )? )?
            int alt59=3;
            try { dbg.enterSubRule(59);
            try { dbg.enterDecision(59, decisionCanBacktrack[59]);

            int LA59_0 = input.LA(1);

            if ( (LA59_0==IDENT||LA59_0==GEN) ) {
                alt59=1;
            }
            else if ( ((LA59_0>=TOPLEFTCORNER_SYM && LA59_0<=RIGHTBOTTOM_SYM)) ) {
                alt59=2;
            }
            } finally {dbg.exitDecision(59);}

            switch (alt59) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:14: declaration
                    {
                    dbg.location(364,14);
                    pushFollow(FOLLOW_declaration_in_page918);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:26: margin ( ws )?
                    {
                    dbg.location(364,26);
                    pushFollow(FOLLOW_margin_in_page920);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(364,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:33: ( ws )?
                    int alt58=2;
                    try { dbg.enterSubRule(58);
                    try { dbg.enterDecision(58, decisionCanBacktrack[58]);

                    int LA58_0 = input.LA(1);

                    if ( ((LA58_0>=WS && LA58_0<=COMMENT)) ) {
                        alt58=1;
                    }
                    } finally {dbg.exitDecision(58);}

                    switch (alt58) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:33: ws
                            {
                            dbg.location(364,33);
                            pushFollow(FOLLOW_ws_in_page922);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(58);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(59);}

            dbg.location(364,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
            try { dbg.enterSubRule(63);

            loop63:
            do {
                int alt63=2;
                try { dbg.enterDecision(63, decisionCanBacktrack[63]);

                int LA63_0 = input.LA(1);

                if ( (LA63_0==SEMI) ) {
                    alt63=1;
                }


                } finally {dbg.exitDecision(63);}

                switch (alt63) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(364,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page928); if (state.failed) return ;
            	    dbg.location(364,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:45: ( ws )?
            	    int alt60=2;
            	    try { dbg.enterSubRule(60);
            	    try { dbg.enterDecision(60, decisionCanBacktrack[60]);

            	    int LA60_0 = input.LA(1);

            	    if ( ((LA60_0>=WS && LA60_0<=COMMENT)) ) {
            	        alt60=1;
            	    }
            	    } finally {dbg.exitDecision(60);}

            	    switch (alt60) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:45: ws
            	            {
            	            dbg.location(364,45);
            	            pushFollow(FOLLOW_ws_in_page930);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(60);}

            	    dbg.location(364,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:49: ( declaration | margin ( ws )? )?
            	    int alt62=3;
            	    try { dbg.enterSubRule(62);
            	    try { dbg.enterDecision(62, decisionCanBacktrack[62]);

            	    int LA62_0 = input.LA(1);

            	    if ( (LA62_0==IDENT||LA62_0==GEN) ) {
            	        alt62=1;
            	    }
            	    else if ( ((LA62_0>=TOPLEFTCORNER_SYM && LA62_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt62=2;
            	    }
            	    } finally {dbg.exitDecision(62);}

            	    switch (alt62) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:50: declaration
            	            {
            	            dbg.location(364,50);
            	            pushFollow(FOLLOW_declaration_in_page934);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:62: margin ( ws )?
            	            {
            	            dbg.location(364,62);
            	            pushFollow(FOLLOW_margin_in_page936);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(364,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:69: ( ws )?
            	            int alt61=2;
            	            try { dbg.enterSubRule(61);
            	            try { dbg.enterDecision(61, decisionCanBacktrack[61]);

            	            int LA61_0 = input.LA(1);

            	            if ( ((LA61_0>=WS && LA61_0<=COMMENT)) ) {
            	                alt61=1;
            	            }
            	            } finally {dbg.exitDecision(61);}

            	            switch (alt61) {
            	                case 1 :
            	                    dbg.enterAlt(1);

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:69: ws
            	                    {
            	                    dbg.location(364,69);
            	                    pushFollow(FOLLOW_ws_in_page938);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(61);}


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

            dbg.location(365,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page953); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(366, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(368, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(369,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle974); if (state.failed) return ;
            dbg.location(369,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:25: ( ws )?
            int alt64=2;
            try { dbg.enterSubRule(64);
            try { dbg.enterDecision(64, decisionCanBacktrack[64]);

            int LA64_0 = input.LA(1);

            if ( ((LA64_0>=WS && LA64_0<=COMMENT)) ) {
                alt64=1;
            }
            } finally {dbg.exitDecision(64);}

            switch (alt64) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:25: ws
                    {
                    dbg.location(369,25);
                    pushFollow(FOLLOW_ws_in_counterStyle976);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(369,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle979); if (state.failed) return ;
            dbg.location(369,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:35: ( ws )?
            int alt65=2;
            try { dbg.enterSubRule(65);
            try { dbg.enterDecision(65, decisionCanBacktrack[65]);

            int LA65_0 = input.LA(1);

            if ( ((LA65_0>=WS && LA65_0<=COMMENT)) ) {
                alt65=1;
            }
            } finally {dbg.exitDecision(65);}

            switch (alt65) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:35: ws
                    {
                    dbg.location(369,35);
                    pushFollow(FOLLOW_ws_in_counterStyle981);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(65);}

            dbg.location(370,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle992); if (state.failed) return ;
            dbg.location(370,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:16: ( ws )?
            int alt66=2;
            try { dbg.enterSubRule(66);
            try { dbg.enterDecision(66, decisionCanBacktrack[66]);

            int LA66_0 = input.LA(1);

            if ( ((LA66_0>=WS && LA66_0<=COMMENT)) ) {
                alt66=1;
            }
            } finally {dbg.exitDecision(66);}

            switch (alt66) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:16: ws
                    {
                    dbg.location(370,16);
                    pushFollow(FOLLOW_ws_in_counterStyle994);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(66);}

            dbg.location(370,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle997);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(371,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1001);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(372,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1011); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(373, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:375:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(375, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(376,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1032); if (state.failed) return ;
            dbg.location(376,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:21: ( ws )?
            int alt67=2;
            try { dbg.enterSubRule(67);
            try { dbg.enterDecision(67, decisionCanBacktrack[67]);

            int LA67_0 = input.LA(1);

            if ( ((LA67_0>=WS && LA67_0<=COMMENT)) ) {
                alt67=1;
            }
            } finally {dbg.exitDecision(67);}

            switch (alt67) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:21: ws
                    {
                    dbg.location(376,21);
                    pushFollow(FOLLOW_ws_in_fontFace1034);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(67);}

            dbg.location(377,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1045); if (state.failed) return ;
            dbg.location(377,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:16: ( ws )?
            int alt68=2;
            try { dbg.enterSubRule(68);
            try { dbg.enterDecision(68, decisionCanBacktrack[68]);

            int LA68_0 = input.LA(1);

            if ( ((LA68_0>=WS && LA68_0<=COMMENT)) ) {
                alt68=1;
            }
            } finally {dbg.exitDecision(68);}

            switch (alt68) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:16: ws
                    {
                    dbg.location(377,16);
                    pushFollow(FOLLOW_ws_in_fontFace1047);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(68);}

            dbg.location(377,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_fontFace1050);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(378,3);
            pushFollow(FOLLOW_declarations_in_fontFace1054);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(379,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1064); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(380, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(382, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:2: ( margin_sym ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:4: margin_sym ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(383,4);
            pushFollow(FOLLOW_margin_sym_in_margin1079);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(383,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:15: ( ws )?
            int alt69=2;
            try { dbg.enterSubRule(69);
            try { dbg.enterDecision(69, decisionCanBacktrack[69]);

            int LA69_0 = input.LA(1);

            if ( ((LA69_0>=WS && LA69_0<=COMMENT)) ) {
                alt69=1;
            }
            } finally {dbg.exitDecision(69);}

            switch (alt69) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:15: ws
                    {
                    dbg.location(383,15);
                    pushFollow(FOLLOW_ws_in_margin1081);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(383,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1084); if (state.failed) return ;
            dbg.location(383,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:26: ( ws )?
            int alt70=2;
            try { dbg.enterSubRule(70);
            try { dbg.enterDecision(70, decisionCanBacktrack[70]);

            int LA70_0 = input.LA(1);

            if ( ((LA70_0>=WS && LA70_0<=COMMENT)) ) {
                alt70=1;
            }
            } finally {dbg.exitDecision(70);}

            switch (alt70) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:26: ws
                    {
                    dbg.location(383,26);
                    pushFollow(FOLLOW_ws_in_margin1086);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(383,30);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_margin1089);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(383,50);
            pushFollow(FOLLOW_declarations_in_margin1091);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(383,63);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1093); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(384, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:386:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(386, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:387:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(387,2);
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
        dbg.location(404, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(406, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:7: COLON IDENT
            {
            dbg.location(407,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1322); if (state.failed) return ;
            dbg.location(407,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1324); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "pseudoPage");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudoPage"


    // $ANTLR start "operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:1: operator : ( SOLIDUS ( ws )? | COMMA ( ws )? | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(410, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:5: ( SOLIDUS ( ws )? | COMMA ( ws )? | )
            int alt73=3;
            try { dbg.enterDecision(73, decisionCanBacktrack[73]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt73=1;
                }
                break;
            case COMMA:
                {
                alt73=2;
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
            case EXS:
            case ANGLE:
            case TIME:
            case FREQ:
            case RESOLUTION:
            case DIMENSION:
                {
                alt73=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 73, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(73);}

            switch (alt73) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:7: SOLIDUS ( ws )?
                    {
                    dbg.location(411,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator1345); if (state.failed) return ;
                    dbg.location(411,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:15: ( ws )?
                    int alt71=2;
                    try { dbg.enterSubRule(71);
                    try { dbg.enterDecision(71, decisionCanBacktrack[71]);

                    int LA71_0 = input.LA(1);

                    if ( ((LA71_0>=WS && LA71_0<=COMMENT)) ) {
                        alt71=1;
                    }
                    } finally {dbg.exitDecision(71);}

                    switch (alt71) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:15: ws
                            {
                            dbg.location(411,15);
                            pushFollow(FOLLOW_ws_in_operator1347);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(71);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:7: COMMA ( ws )?
                    {
                    dbg.location(412,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator1356); if (state.failed) return ;
                    dbg.location(412,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:13: ( ws )?
                    int alt72=2;
                    try { dbg.enterSubRule(72);
                    try { dbg.enterDecision(72, decisionCanBacktrack[72]);

                    int LA72_0 = input.LA(1);

                    if ( ((LA72_0>=WS && LA72_0<=COMMENT)) ) {
                        alt72=1;
                    }
                    } finally {dbg.exitDecision(72);}

                    switch (alt72) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:13: ws
                            {
                            dbg.location(412,13);
                            pushFollow(FOLLOW_ws_in_operator1358);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(72);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:5: 
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
        dbg.location(414, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(416, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
            int alt77=4;
            try { dbg.enterDecision(77, decisionCanBacktrack[77]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt77=1;
                }
                break;
            case GREATER:
                {
                alt77=2;
                }
                break;
            case TILDE:
                {
                alt77=3;
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
            case 114:
                {
                alt77=4;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:7: PLUS ( ws )?
                    {
                    dbg.location(417,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1386); if (state.failed) return ;
                    dbg.location(417,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:12: ( ws )?
                    int alt74=2;
                    try { dbg.enterSubRule(74);
                    try { dbg.enterDecision(74, decisionCanBacktrack[74]);

                    int LA74_0 = input.LA(1);

                    if ( ((LA74_0>=WS && LA74_0<=COMMENT)) ) {
                        alt74=1;
                    }
                    } finally {dbg.exitDecision(74);}

                    switch (alt74) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:12: ws
                            {
                            dbg.location(417,12);
                            pushFollow(FOLLOW_ws_in_combinator1388);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(74);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:7: GREATER ( ws )?
                    {
                    dbg.location(418,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1397); if (state.failed) return ;
                    dbg.location(418,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:15: ( ws )?
                    int alt75=2;
                    try { dbg.enterSubRule(75);
                    try { dbg.enterDecision(75, decisionCanBacktrack[75]);

                    int LA75_0 = input.LA(1);

                    if ( ((LA75_0>=WS && LA75_0<=COMMENT)) ) {
                        alt75=1;
                    }
                    } finally {dbg.exitDecision(75);}

                    switch (alt75) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:15: ws
                            {
                            dbg.location(418,15);
                            pushFollow(FOLLOW_ws_in_combinator1399);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(75);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:7: TILDE ( ws )?
                    {
                    dbg.location(419,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1408); if (state.failed) return ;
                    dbg.location(419,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:13: ( ws )?
                    int alt76=2;
                    try { dbg.enterSubRule(76);
                    try { dbg.enterDecision(76, decisionCanBacktrack[76]);

                    int LA76_0 = input.LA(1);

                    if ( ((LA76_0>=WS && LA76_0<=COMMENT)) ) {
                        alt76=1;
                    }
                    } finally {dbg.exitDecision(76);}

                    switch (alt76) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:13: ws
                            {
                            dbg.location(419,13);
                            pushFollow(FOLLOW_ws_in_combinator1410);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(76);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:5: 
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
        dbg.location(421, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(423, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:424:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(424,5);
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
        dbg.location(426, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:428:1: property : ( IDENT | GEN ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(428, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:5: ( ( IDENT | GEN ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:7: ( IDENT | GEN ) ( ws )?
            {
            dbg.location(429,7);
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

            dbg.location(429,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:21: ( ws )?
            int alt78=2;
            try { dbg.enterSubRule(78);
            try { dbg.enterDecision(78, decisionCanBacktrack[78]);

            int LA78_0 = input.LA(1);

            if ( ((LA78_0>=WS && LA78_0<=COMMENT)) ) {
                alt78=1;
            }
            } finally {dbg.exitDecision(78);}

            switch (alt78) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:21: ws
                    {
                    dbg.location(429,21);
                    pushFollow(FOLLOW_ws_in_property1478);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(78);}


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
            dbg.exitRule(getGrammarFileName(), "property");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "property"


    // $ANTLR start "rule"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:1: rule : selectorsGroup LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(432, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:5: ( selectorsGroup LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:9: selectorsGroup LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(433,9);
            pushFollow(FOLLOW_selectorsGroup_in_rule1503);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(434,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule1513); if (state.failed) return ;
            dbg.location(434,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:16: ( ws )?
            int alt79=2;
            try { dbg.enterSubRule(79);
            try { dbg.enterDecision(79, decisionCanBacktrack[79]);

            int LA79_0 = input.LA(1);

            if ( ((LA79_0>=WS && LA79_0<=COMMENT)) ) {
                alt79=1;
            }
            } finally {dbg.exitDecision(79);}

            switch (alt79) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:16: ws
                    {
                    dbg.location(434,16);
                    pushFollow(FOLLOW_ws_in_rule1515);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(79);}

            dbg.location(434,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_rule1518);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(435,13);
            pushFollow(FOLLOW_declarations_in_rule1532);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(436,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule1542); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(437, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:1: declarations : ( declaration )? ( SEMI ( ws )? ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(439, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:5: ( ( declaration )? ( SEMI ( ws )? ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:9: ( declaration )? ( SEMI ( ws )? ( declaration )? )*
            {
            dbg.location(442,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:9: ( declaration )?
            int alt80=2;
            try { dbg.enterSubRule(80);
            try { dbg.enterDecision(80, decisionCanBacktrack[80]);

            int LA80_0 = input.LA(1);

            if ( (LA80_0==IDENT||LA80_0==GEN) ) {
                alt80=1;
            }
            } finally {dbg.exitDecision(80);}

            switch (alt80) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:9: declaration
                    {
                    dbg.location(442,9);
                    pushFollow(FOLLOW_declaration_in_declarations1580);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(442,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:22: ( SEMI ( ws )? ( declaration )? )*
            try { dbg.enterSubRule(83);

            loop83:
            do {
                int alt83=2;
                try { dbg.enterDecision(83, decisionCanBacktrack[83]);

                int LA83_0 = input.LA(1);

                if ( (LA83_0==SEMI) ) {
                    alt83=1;
                }


                } finally {dbg.exitDecision(83);}

                switch (alt83) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:23: SEMI ( ws )? ( declaration )?
            	    {
            	    dbg.location(442,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations1584); if (state.failed) return ;
            	    dbg.location(442,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:28: ( ws )?
            	    int alt81=2;
            	    try { dbg.enterSubRule(81);
            	    try { dbg.enterDecision(81, decisionCanBacktrack[81]);

            	    int LA81_0 = input.LA(1);

            	    if ( ((LA81_0>=WS && LA81_0<=COMMENT)) ) {
            	        alt81=1;
            	    }
            	    } finally {dbg.exitDecision(81);}

            	    switch (alt81) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:28: ws
            	            {
            	            dbg.location(442,28);
            	            pushFollow(FOLLOW_ws_in_declarations1586);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(81);}

            	    dbg.location(442,32);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:32: ( declaration )?
            	    int alt82=2;
            	    try { dbg.enterSubRule(82);
            	    try { dbg.enterDecision(82, decisionCanBacktrack[82]);

            	    int LA82_0 = input.LA(1);

            	    if ( (LA82_0==IDENT||LA82_0==GEN) ) {
            	        alt82=1;
            	    }
            	    } finally {dbg.exitDecision(82);}

            	    switch (alt82) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:32: declaration
            	            {
            	            dbg.location(442,32);
            	            pushFollow(FOLLOW_declaration_in_declarations1589);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(82);}


            	    }
            	    break;

            	default :
            	    break loop83;
                }
            } while (true);
            } finally {dbg.exitSubRule(83);}


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
            dbg.exitRule(getGrammarFileName(), "declarations");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "declarations"


    // $ANTLR start "selectorsGroup"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:1: selectorsGroup : selector ( COMMA ( ws )? selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(445, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:5: ( selector ( COMMA ( ws )? selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:7: selector ( COMMA ( ws )? selector )*
            {
            dbg.location(446,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup1613);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(446,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:16: ( COMMA ( ws )? selector )*
            try { dbg.enterSubRule(85);

            loop85:
            do {
                int alt85=2;
                try { dbg.enterDecision(85, decisionCanBacktrack[85]);

                int LA85_0 = input.LA(1);

                if ( (LA85_0==COMMA) ) {
                    alt85=1;
                }


                } finally {dbg.exitDecision(85);}

                switch (alt85) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:17: COMMA ( ws )? selector
            	    {
            	    dbg.location(446,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup1616); if (state.failed) return ;
            	    dbg.location(446,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:23: ( ws )?
            	    int alt84=2;
            	    try { dbg.enterSubRule(84);
            	    try { dbg.enterDecision(84, decisionCanBacktrack[84]);

            	    int LA84_0 = input.LA(1);

            	    if ( ((LA84_0>=WS && LA84_0<=COMMENT)) ) {
            	        alt84=1;
            	    }
            	    } finally {dbg.exitDecision(84);}

            	    switch (alt84) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:23: ws
            	            {
            	            dbg.location(446,23);
            	            pushFollow(FOLLOW_ws_in_selectorsGroup1618);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(84);}

            	    dbg.location(446,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup1621);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop85;
                }
            } while (true);
            } finally {dbg.exitSubRule(85);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(447, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(449, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(450,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector1644);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(450,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(86);

            loop86:
            do {
                int alt86=2;
                try { dbg.enterDecision(86, decisionCanBacktrack[86]);

                int LA86_0 = input.LA(1);

                if ( (LA86_0==IDENT||LA86_0==GEN||LA86_0==COLON||(LA86_0>=PLUS && LA86_0<=TILDE)||(LA86_0>=HASH && LA86_0<=PIPE)||LA86_0==114) ) {
                    alt86=1;
                }


                } finally {dbg.exitDecision(86);}

                switch (alt86) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(450,31);
            	    pushFollow(FOLLOW_combinator_in_selector1647);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(450,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector1649);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop86;
                }
            } while (true);
            } finally {dbg.exitSubRule(86);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(451, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(454, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt89=2;
            try { dbg.enterDecision(89, decisionCanBacktrack[89]);

            int LA89_0 = input.LA(1);

            if ( (LA89_0==IDENT||LA89_0==GEN||(LA89_0>=STAR && LA89_0<=PIPE)) ) {
                alt89=1;
            }
            else if ( (LA89_0==COLON||(LA89_0>=HASH && LA89_0<=DCOLON)||LA89_0==114) ) {
                alt89=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(89);}

            switch (alt89) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(457,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(457,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence1683);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(457,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(87);

                    loop87:
                    do {
                        int alt87=2;
                        try { dbg.enterDecision(87, decisionCanBacktrack[87]);

                        try {
                            isCyclicDecision = true;
                            alt87 = dfa87.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(87);}

                        switch (alt87) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(457,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1690);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop87;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(87);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(459,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(459,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:4: ( ( esPred )=> elementSubsequent )+
                    int cnt88=0;
                    try { dbg.enterSubRule(88);

                    loop88:
                    do {
                        int alt88=2;
                        try { dbg.enterDecision(88, decisionCanBacktrack[88]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA88_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt88=1;
                            }


                            }
                            break;
                        case 114:
                            {
                            int LA88_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt88=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA88_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt88=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA88_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt88=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA88_6 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt88=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(88);}

                        switch (alt88) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(459,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1708);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt88 >= 1 ) break loop88;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(88, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt88++;
                    } while (true);
                    } finally {dbg.exitSubRule(88);}


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
        dbg.location(460, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:1: esPred : ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(467, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:5: ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(468,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH && input.LA(1)<=DCOLON)||input.LA(1)==114 ) {
                input.consume();
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
        dbg.location(469, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:471:1: typeSelector options {k=2; } : ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(471, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:3: ( ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:6: ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(473,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:6: ( ( nsPred )=> namespacePrefix )?
            int alt90=2;
            try { dbg.enterSubRule(90);
            try { dbg.enterDecision(90, decisionCanBacktrack[90]);

            int LA90_0 = input.LA(1);

            if ( (LA90_0==IDENT) ) {
                int LA90_1 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt90=1;
                }
            }
            else if ( (LA90_0==STAR) ) {
                int LA90_2 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt90=1;
                }
            }
            else if ( (LA90_0==PIPE) && (synpred3_Css3())) {
                alt90=1;
            }
            } finally {dbg.exitDecision(90);}

            switch (alt90) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:7: ( nsPred )=> namespacePrefix
                    {
                    dbg.location(473,17);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector1810);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}

            dbg.location(473,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:35: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:37: elementName ( ws )?
            {
            dbg.location(473,37);
            pushFollow(FOLLOW_elementName_in_typeSelector1816);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(473,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:49: ( ws )?
            int alt91=2;
            try { dbg.enterSubRule(91);
            try { dbg.enterDecision(91, decisionCanBacktrack[91]);

            int LA91_0 = input.LA(1);

            if ( ((LA91_0>=WS && LA91_0<=COMMENT)) ) {
                alt91=1;
            }
            } finally {dbg.exitDecision(91);}

            switch (alt91) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:49: ws
                    {
                    dbg.location(473,49);
                    pushFollow(FOLLOW_ws_in_typeSelector1818);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(91);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(474, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:477:1: nsPred : ( IDENT | STAR )? PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(477, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:3: ( ( IDENT | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:3: ( IDENT | STAR )? PIPE
            {
            dbg.location(479,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:3: ( IDENT | STAR )?
            int alt92=2;
            try { dbg.enterSubRule(92);
            try { dbg.enterDecision(92, decisionCanBacktrack[92]);

            int LA92_0 = input.LA(1);

            if ( (LA92_0==IDENT||LA92_0==STAR) ) {
                alt92=1;
            }
            } finally {dbg.exitDecision(92);}

            switch (alt92) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    {
                    dbg.location(479,3);
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
            } finally {dbg.exitSubRule(92);}

            dbg.location(479,19);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred1847); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(480, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(482, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(483,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:5: ( namespacePrefixName | STAR )?
            int alt93=3;
            try { dbg.enterSubRule(93);
            try { dbg.enterDecision(93, decisionCanBacktrack[93]);

            int LA93_0 = input.LA(1);

            if ( (LA93_0==IDENT) ) {
                alt93=1;
            }
            else if ( (LA93_0==STAR) ) {
                alt93=2;
            }
            } finally {dbg.exitDecision(93);}

            switch (alt93) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:7: namespacePrefixName
                    {
                    dbg.location(483,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix1862);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:29: STAR
                    {
                    dbg.location(483,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix1866); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(93);}

            dbg.location(483,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix1870); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(484, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:1: elementSubsequent : ( cssId | cssClass | slAttribute | pseudo ) ( ws )? ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(487, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:5: ( ( cssId | cssClass | slAttribute | pseudo ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:5: ( cssId | cssClass | slAttribute | pseudo ) ( ws )?
            {
            dbg.location(489,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:5: ( cssId | cssClass | slAttribute | pseudo )
            int alt94=4;
            try { dbg.enterSubRule(94);
            try { dbg.enterDecision(94, decisionCanBacktrack[94]);

            switch ( input.LA(1) ) {
            case HASH:
            case 114:
                {
                alt94=1;
                }
                break;
            case DOT:
                {
                alt94=2;
                }
                break;
            case LBRACKET:
                {
                alt94=3;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt94=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 94, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(94);}

            switch (alt94) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:6: cssId
                    {
                    dbg.location(490,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent1904);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:8: cssClass
                    {
                    dbg.location(491,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent1913);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:11: slAttribute
                    {
                    dbg.location(492,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent1925);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:11: pseudo
                    {
                    dbg.location(493,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent1937);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(94);}

            dbg.location(495,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:495:5: ( ws )?
            int alt95=2;
            try { dbg.enterSubRule(95);
            try { dbg.enterDecision(95, decisionCanBacktrack[95]);

            int LA95_0 = input.LA(1);

            if ( ((LA95_0>=WS && LA95_0<=COMMENT)) ) {
                alt95=1;
            }
            } finally {dbg.exitDecision(95);}

            switch (alt95) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:495:5: ws
                    {
                    dbg.location(495,5);
                    pushFollow(FOLLOW_ws_in_elementSubsequent1949);
                    ws();

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
        dbg.location(496, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:1: cssId : ( HASH | ( '#' NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(499, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:5: ( HASH | ( '#' NAME ) )
            int alt96=2;
            try { dbg.enterDecision(96, decisionCanBacktrack[96]);

            int LA96_0 = input.LA(1);

            if ( (LA96_0==HASH) ) {
                alt96=1;
            }
            else if ( (LA96_0==114) ) {
                alt96=2;
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:7: HASH
                    {
                    dbg.location(500,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId1972); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:14: ( '#' NAME )
                    {
                    dbg.location(500,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:14: ( '#' NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:16: '#' NAME
                    {
                    dbg.location(500,16);
                    match(input,114,FOLLOW_114_in_cssId1978); if (state.failed) return ;
                    dbg.location(500,20);
                    match(input,NAME,FOLLOW_NAME_in_cssId1980); if (state.failed) return ;

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
        dbg.location(501, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(507, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:7: DOT ( IDENT | GEN )
            {
            dbg.location(508,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2008); if (state.failed) return ;
            dbg.location(508,11);
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
        dbg.location(509, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:516:1: elementName : ( ( IDENT | GEN ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(516, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:5: ( ( IDENT | GEN ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(517,5);
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
        dbg.location(518, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(520, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(521,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute2079); if (state.failed) return ;
            dbg.location(522,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:6: ( namespacePrefix )?
            int alt97=2;
            try { dbg.enterSubRule(97);
            try { dbg.enterDecision(97, decisionCanBacktrack[97]);

            int LA97_0 = input.LA(1);

            if ( (LA97_0==IDENT) ) {
                int LA97_1 = input.LA(2);

                if ( (LA97_1==PIPE) ) {
                    alt97=1;
                }
            }
            else if ( ((LA97_0>=STAR && LA97_0<=PIPE)) ) {
                alt97=1;
            }
            } finally {dbg.exitDecision(97);}

            switch (alt97) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:6: namespacePrefix
                    {
                    dbg.location(522,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute2086);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(97);}

            dbg.location(522,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:23: ( ws )?
            int alt98=2;
            try { dbg.enterSubRule(98);
            try { dbg.enterDecision(98, decisionCanBacktrack[98]);

            int LA98_0 = input.LA(1);

            if ( ((LA98_0>=WS && LA98_0<=COMMENT)) ) {
                alt98=1;
            }
            } finally {dbg.exitDecision(98);}

            switch (alt98) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:23: ws
                    {
                    dbg.location(522,23);
                    pushFollow(FOLLOW_ws_in_slAttribute2089);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(98);}

            dbg.location(523,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute2100);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(523,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:25: ( ws )?
            int alt99=2;
            try { dbg.enterSubRule(99);
            try { dbg.enterDecision(99, decisionCanBacktrack[99]);

            int LA99_0 = input.LA(1);

            if ( ((LA99_0>=WS && LA99_0<=COMMENT)) ) {
                alt99=1;
            }
            } finally {dbg.exitDecision(99);}

            switch (alt99) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:25: ws
                    {
                    dbg.location(523,25);
                    pushFollow(FOLLOW_ws_in_slAttribute2102);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(99);}

            dbg.location(525,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt102=2;
            try { dbg.enterSubRule(102);
            try { dbg.enterDecision(102, decisionCanBacktrack[102]);

            int LA102_0 = input.LA(1);

            if ( ((LA102_0>=OPEQ && LA102_0<=CONTAINS)) ) {
                alt102=1;
            }
            } finally {dbg.exitDecision(102);}

            switch (alt102) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(526,17);
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

                    dbg.location(534,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:17: ( ws )?
                    int alt100=2;
                    try { dbg.enterSubRule(100);
                    try { dbg.enterDecision(100, decisionCanBacktrack[100]);

                    int LA100_0 = input.LA(1);

                    if ( ((LA100_0>=WS && LA100_0<=COMMENT)) ) {
                        alt100=1;
                    }
                    } finally {dbg.exitDecision(100);}

                    switch (alt100) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:17: ws
                            {
                            dbg.location(534,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2324);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(100);}

                    dbg.location(535,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute2343);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(536,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:17: ( ws )?
                    int alt101=2;
                    try { dbg.enterSubRule(101);
                    try { dbg.enterDecision(101, decisionCanBacktrack[101]);

                    int LA101_0 = input.LA(1);

                    if ( ((LA101_0>=WS && LA101_0<=COMMENT)) ) {
                        alt101=1;
                    }
                    } finally {dbg.exitDecision(101);}

                    switch (alt101) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:17: ws
                            {
                            dbg.location(536,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2361);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(101);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(102);}

            dbg.location(539,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute2390); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(540, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(547, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:4: IDENT
            {
            dbg.location(548,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName2406); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(549, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(551, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:553:2: ( IDENT | STRING )
            {
            dbg.location(553,2);
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
        dbg.location(557, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expr | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(559, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expr | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expr | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(560,7);
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

            dbg.location(561,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expr | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt110=2;
            try { dbg.enterSubRule(110);
            try { dbg.enterDecision(110, decisionCanBacktrack[110]);

            int LA110_0 = input.LA(1);

            if ( (LA110_0==IDENT||LA110_0==GEN) ) {
                alt110=1;
            }
            else if ( (LA110_0==NOT) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expr | '*' )? RPAREN )? )
                    {
                    dbg.location(562,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expr | '*' )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expr | '*' )? RPAREN )?
                    {
                    dbg.location(563,21);
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

                    dbg.location(564,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:21: ( ( ws )? LPAREN ( ws )? ( expr | '*' )? RPAREN )?
                    int alt106=2;
                    try { dbg.enterSubRule(106);
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:25: ( ws )? LPAREN ( ws )? ( expr | '*' )? RPAREN
                            {
                            dbg.location(565,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:25: ( ws )?
                            int alt103=2;
                            try { dbg.enterSubRule(103);
                            try { dbg.enterDecision(103, decisionCanBacktrack[103]);

                            int LA103_0 = input.LA(1);

                            if ( ((LA103_0>=WS && LA103_0<=COMMENT)) ) {
                                alt103=1;
                            }
                            } finally {dbg.exitDecision(103);}

                            switch (alt103) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:25: ws
                                    {
                                    dbg.location(565,25);
                                    pushFollow(FOLLOW_ws_in_pseudo2601);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(103);}

                            dbg.location(565,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2604); if (state.failed) return ;
                            dbg.location(565,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:36: ( ws )?
                            int alt104=2;
                            try { dbg.enterSubRule(104);
                            try { dbg.enterDecision(104, decisionCanBacktrack[104]);

                            int LA104_0 = input.LA(1);

                            if ( ((LA104_0>=WS && LA104_0<=COMMENT)) ) {
                                alt104=1;
                            }
                            } finally {dbg.exitDecision(104);}

                            switch (alt104) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:36: ws
                                    {
                                    dbg.location(565,36);
                                    pushFollow(FOLLOW_ws_in_pseudo2606);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(104);}

                            dbg.location(565,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:40: ( expr | '*' )?
                            int alt105=3;
                            try { dbg.enterSubRule(105);
                            try { dbg.enterDecision(105, decisionCanBacktrack[105]);

                            int LA105_0 = input.LA(1);

                            if ( ((LA105_0>=IDENT && LA105_0<=URI)||LA105_0==GEN||LA105_0==PLUS||(LA105_0>=MINUS && LA105_0<=HASH)||(LA105_0>=NUMBER && LA105_0<=DIMENSION)) ) {
                                alt105=1;
                            }
                            else if ( (LA105_0==STAR) ) {
                                alt105=2;
                            }
                            } finally {dbg.exitDecision(105);}

                            switch (alt105) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:42: expr
                                    {
                                    dbg.location(565,42);
                                    pushFollow(FOLLOW_expr_in_pseudo2611);
                                    expr();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:49: '*'
                                    {
                                    dbg.location(565,49);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo2615); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(105);}

                            dbg.location(565,56);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2620); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(106);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(569,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(569,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo2699); if (state.failed) return ;
                    dbg.location(569,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:23: ( ws )?
                    int alt107=2;
                    try { dbg.enterSubRule(107);
                    try { dbg.enterDecision(107, decisionCanBacktrack[107]);

                    int LA107_0 = input.LA(1);

                    if ( ((LA107_0>=WS && LA107_0<=COMMENT)) ) {
                        alt107=1;
                    }
                    } finally {dbg.exitDecision(107);}

                    switch (alt107) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:23: ws
                            {
                            dbg.location(569,23);
                            pushFollow(FOLLOW_ws_in_pseudo2701);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(107);}

                    dbg.location(569,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2704); if (state.failed) return ;
                    dbg.location(569,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:34: ( ws )?
                    int alt108=2;
                    try { dbg.enterSubRule(108);
                    try { dbg.enterDecision(108, decisionCanBacktrack[108]);

                    int LA108_0 = input.LA(1);

                    if ( ((LA108_0>=WS && LA108_0<=COMMENT)) ) {
                        alt108=1;
                    }
                    } finally {dbg.exitDecision(108);}

                    switch (alt108) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:34: ws
                            {
                            dbg.location(569,34);
                            pushFollow(FOLLOW_ws_in_pseudo2706);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(108);}

                    dbg.location(569,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:38: ( simpleSelectorSequence )?
                    int alt109=2;
                    try { dbg.enterSubRule(109);
                    try { dbg.enterDecision(109, decisionCanBacktrack[109]);

                    int LA109_0 = input.LA(1);

                    if ( (LA109_0==IDENT||LA109_0==GEN||LA109_0==COLON||(LA109_0>=HASH && LA109_0<=PIPE)||LA109_0==114) ) {
                        alt109=1;
                    }
                    } finally {dbg.exitDecision(109);}

                    switch (alt109) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:38: simpleSelectorSequence
                            {
                            dbg.location(569,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo2709);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(109);}

                    dbg.location(569,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2712); if (state.failed) return ;

                    }


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
        dbg.location(571, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:1: declaration : property COLON ( ws )? propertyValue ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(573, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:574:5: ( property COLON ( ws )? propertyValue ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:5: property COLON ( ws )? propertyValue ( prio )?
            {
            dbg.location(576,5);
            pushFollow(FOLLOW_property_in_declaration2756);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(576,14);
            match(input,COLON,FOLLOW_COLON_in_declaration2758); if (state.failed) return ;
            dbg.location(576,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:20: ( ws )?
            int alt111=2;
            try { dbg.enterSubRule(111);
            try { dbg.enterDecision(111, decisionCanBacktrack[111]);

            int LA111_0 = input.LA(1);

            if ( ((LA111_0>=WS && LA111_0<=COMMENT)) ) {
                alt111=1;
            }
            } finally {dbg.exitDecision(111);}

            switch (alt111) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:20: ws
                    {
                    dbg.location(576,20);
                    pushFollow(FOLLOW_ws_in_declaration2760);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(111);}

            dbg.location(576,24);
            pushFollow(FOLLOW_propertyValue_in_declaration2763);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(576,38);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:38: ( prio )?
            int alt112=2;
            try { dbg.enterSubRule(112);
            try { dbg.enterDecision(112, decisionCanBacktrack[112]);

            int LA112_0 = input.LA(1);

            if ( (LA112_0==IMPORTANT_SYM) ) {
                alt112=1;
            }
            } finally {dbg.exitDecision(112);}

            switch (alt112) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:38: prio
                    {
                    dbg.location(576,38);
                    pushFollow(FOLLOW_prio_in_declaration2765);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(112);}


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
        dbg.location(577, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:1: propertyValue : expr ;
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(585, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:2: ( expr )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:4: expr
            {
            dbg.location(586,4);
            pushFollow(FOLLOW_expr_in_propertyValue2789);
            expr();

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
        dbg.location(587, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:591:1: syncTo_IDENT_RBRACE : ;
    public final void syncTo_IDENT_RBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(591, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:595:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:6: 
            {
            }

        }
        finally {
        }
        dbg.location(596, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncTo_IDENT_RBRACE"


    // $ANTLR start "syncToFollow"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:599:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(599, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:6: 
            {
            }

        }
        finally {
        }
        dbg.location(604, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:1: prio : IMPORTANT_SYM ( ws )? ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(606, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:5: ( IMPORTANT_SYM ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:7: IMPORTANT_SYM ( ws )?
            {
            dbg.location(607,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio2864); if (state.failed) return ;
            dbg.location(607,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:21: ( ws )?
            int alt113=2;
            try { dbg.enterSubRule(113);
            try { dbg.enterDecision(113, decisionCanBacktrack[113]);

            int LA113_0 = input.LA(1);

            if ( ((LA113_0>=WS && LA113_0<=COMMENT)) ) {
                alt113=1;
            }
            } finally {dbg.exitDecision(113);}

            switch (alt113) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:21: ws
                    {
                    dbg.location(607,21);
                    pushFollow(FOLLOW_ws_in_prio2866);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(113);}


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
            dbg.exitRule(getGrammarFileName(), "prio");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "prio"


    // $ANTLR start "expr"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:1: expr : term ( operator term )* ;
    public final void expr() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expr");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(610, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:7: term ( operator term )*
            {
            dbg.location(611,7);
            pushFollow(FOLLOW_term_in_expr2888);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(611,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:12: ( operator term )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:13: operator term
            	    {
            	    dbg.location(611,13);
            	    pushFollow(FOLLOW_operator_in_expr2891);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(611,22);
            	    pushFollow(FOLLOW_term_in_expr2893);
            	    term();

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
        dbg.location(612, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "expr");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expr"


    // $ANTLR start "term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:1: term : ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(614, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:5: ( ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:7: ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( ws )?
            {
            dbg.location(615,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:7: ( unaryOperator )?
            int alt115=2;
            try { dbg.enterSubRule(115);
            try { dbg.enterDecision(115, decisionCanBacktrack[115]);

            int LA115_0 = input.LA(1);

            if ( (LA115_0==PLUS||LA115_0==MINUS) ) {
                alt115=1;
            }
            } finally {dbg.exitDecision(115);}

            switch (alt115) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:7: unaryOperator
                    {
                    dbg.location(615,7);
                    pushFollow(FOLLOW_unaryOperator_in_term2916);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(115);}

            dbg.location(616,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function )
            int alt116=7;
            try { dbg.enterSubRule(116);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(617,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:7: STRING
                    {
                    dbg.location(629,7);
                    match(input,STRING,FOLLOW_STRING_in_term3120); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:7: IDENT
                    {
                    dbg.location(630,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term3128); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:7: GEN
                    {
                    dbg.location(631,7);
                    match(input,GEN,FOLLOW_GEN_in_term3136); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:7: URI
                    {
                    dbg.location(632,7);
                    match(input,URI,FOLLOW_URI_in_term3144); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:7: hexColor
                    {
                    dbg.location(633,7);
                    pushFollow(FOLLOW_hexColor_in_term3152);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:7: function
                    {
                    dbg.location(634,7);
                    pushFollow(FOLLOW_function_in_term3160);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(116);}

            dbg.location(636,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:5: ( ws )?
            int alt117=2;
            try { dbg.enterSubRule(117);
            try { dbg.enterDecision(117, decisionCanBacktrack[117]);

            int LA117_0 = input.LA(1);

            if ( ((LA117_0>=WS && LA117_0<=COMMENT)) ) {
                alt117=1;
            }
            } finally {dbg.exitDecision(117);}

            switch (alt117) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:5: ws
                    {
                    dbg.location(636,5);
                    pushFollow(FOLLOW_ws_in_term3172);
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
        dbg.location(637, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:1: function : functionName ( ws )? LPAREN ( ws )? ( expr | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(639, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:2: ( functionName ( ws )? LPAREN ( ws )? ( expr | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:5: functionName ( ws )? LPAREN ( ws )? ( expr | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(640,5);
            pushFollow(FOLLOW_functionName_in_function3188);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(640,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:18: ( ws )?
            int alt118=2;
            try { dbg.enterSubRule(118);
            try { dbg.enterDecision(118, decisionCanBacktrack[118]);

            int LA118_0 = input.LA(1);

            if ( ((LA118_0>=WS && LA118_0<=COMMENT)) ) {
                alt118=1;
            }
            } finally {dbg.exitDecision(118);}

            switch (alt118) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:18: ws
                    {
                    dbg.location(640,18);
                    pushFollow(FOLLOW_ws_in_function3190);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(118);}

            dbg.location(641,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function3195); if (state.failed) return ;
            dbg.location(641,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:10: ( ws )?
            int alt119=2;
            try { dbg.enterSubRule(119);
            try { dbg.enterDecision(119, decisionCanBacktrack[119]);

            int LA119_0 = input.LA(1);

            if ( ((LA119_0>=WS && LA119_0<=COMMENT)) ) {
                alt119=1;
            }
            } finally {dbg.exitDecision(119);}

            switch (alt119) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:10: ws
                    {
                    dbg.location(641,10);
                    pushFollow(FOLLOW_ws_in_function3197);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(119);}

            dbg.location(642,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:3: ( expr | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
            int alt122=2;
            try { dbg.enterSubRule(122);
            try { dbg.enterDecision(122, decisionCanBacktrack[122]);

            try {
                isCyclicDecision = true;
                alt122 = dfa122.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(122);}

            switch (alt122) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:4: expr
                    {
                    dbg.location(643,4);
                    pushFollow(FOLLOW_expr_in_function3208);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:645:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(645,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:645:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(646,5);
                    pushFollow(FOLLOW_fnAttribute_in_function3226);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(646,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(121);

                    loop121:
                    do {
                        int alt121=2;
                        try { dbg.enterDecision(121, decisionCanBacktrack[121]);

                        int LA121_0 = input.LA(1);

                        if ( (LA121_0==COMMA) ) {
                            alt121=1;
                        }


                        } finally {dbg.exitDecision(121);}

                        switch (alt121) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(646,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function3229); if (state.failed) return ;
                    	    dbg.location(646,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:24: ( ws )?
                    	    int alt120=2;
                    	    try { dbg.enterSubRule(120);
                    	    try { dbg.enterDecision(120, decisionCanBacktrack[120]);

                    	    int LA120_0 = input.LA(1);

                    	    if ( ((LA120_0>=WS && LA120_0<=COMMENT)) ) {
                    	        alt120=1;
                    	    }
                    	    } finally {dbg.exitDecision(120);}

                    	    switch (alt120) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:24: ws
                    	            {
                    	            dbg.location(646,24);
                    	            pushFollow(FOLLOW_ws_in_function3231);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(120);}

                    	    dbg.location(646,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function3234);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop121;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(121);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(122);}

            dbg.location(649,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function3255); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(650, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(656, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(660,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:4: ( IDENT COLON )?
            int alt123=2;
            try { dbg.enterSubRule(123);
            try { dbg.enterDecision(123, decisionCanBacktrack[123]);

            int LA123_0 = input.LA(1);

            if ( (LA123_0==IDENT) ) {
                int LA123_1 = input.LA(2);

                if ( (LA123_1==COLON) ) {
                    alt123=1;
                }
            }
            } finally {dbg.exitDecision(123);}

            switch (alt123) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:5: IDENT COLON
                    {
                    dbg.location(660,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName3303); if (state.failed) return ;
                    dbg.location(660,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName3305); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(123);}

            dbg.location(660,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName3309); if (state.failed) return ;
            dbg.location(660,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:25: ( DOT IDENT )*
            try { dbg.enterSubRule(124);

            loop124:
            do {
                int alt124=2;
                try { dbg.enterDecision(124, decisionCanBacktrack[124]);

                int LA124_0 = input.LA(1);

                if ( (LA124_0==DOT) ) {
                    alt124=1;
                }


                } finally {dbg.exitDecision(124);}

                switch (alt124) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:26: DOT IDENT
            	    {
            	    dbg.location(660,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName3312); if (state.failed) return ;
            	    dbg.location(660,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName3314); if (state.failed) return ;

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
        dbg.location(661, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(663, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(664,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute3336);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(664,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:20: ( ws )?
            int alt125=2;
            try { dbg.enterSubRule(125);
            try { dbg.enterDecision(125, decisionCanBacktrack[125]);

            int LA125_0 = input.LA(1);

            if ( ((LA125_0>=WS && LA125_0<=COMMENT)) ) {
                alt125=1;
            }
            } finally {dbg.exitDecision(125);}

            switch (alt125) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:20: ws
                    {
                    dbg.location(664,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute3338);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(125);}

            dbg.location(664,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute3341); if (state.failed) return ;
            dbg.location(664,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:29: ( ws )?
            int alt126=2;
            try { dbg.enterSubRule(126);
            try { dbg.enterDecision(126, decisionCanBacktrack[126]);

            int LA126_0 = input.LA(1);

            if ( ((LA126_0>=WS && LA126_0<=COMMENT)) ) {
                alt126=1;
            }
            } finally {dbg.exitDecision(126);}

            switch (alt126) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:29: ws
                    {
                    dbg.location(664,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute3343);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(126);}

            dbg.location(664,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute3346);
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
        dbg.location(665, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:667:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(667, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:4: IDENT ( DOT IDENT )*
            {
            dbg.location(668,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName3361); if (state.failed) return ;
            dbg.location(668,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:10: ( DOT IDENT )*
            try { dbg.enterSubRule(127);

            loop127:
            do {
                int alt127=2;
                try { dbg.enterDecision(127, decisionCanBacktrack[127]);

                int LA127_0 = input.LA(1);

                if ( (LA127_0==DOT) ) {
                    alt127=1;
                }


                } finally {dbg.exitDecision(127);}

                switch (alt127) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:11: DOT IDENT
            	    {
            	    dbg.location(668,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName3364); if (state.failed) return ;
            	    dbg.location(668,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName3366); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop127;
                }
            } while (true);
            } finally {dbg.exitSubRule(127);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(669, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:1: fnAttributeValue : expr ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(671, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:2: ( expr )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:4: expr
            {
            dbg.location(672,4);
            pushFollow(FOLLOW_expr_in_fnAttributeValue3380);
            expr();

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
        dbg.location(673, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(675, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:7: HASH
            {
            dbg.location(676,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor3398); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(677, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(679, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:7: ( WS | NL | COMMENT )+
            {
            dbg.location(680,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:7: ( WS | NL | COMMENT )+
            int cnt128=0;
            try { dbg.enterSubRule(128);

            loop128:
            do {
                int alt128=2;
                try { dbg.enterDecision(128, decisionCanBacktrack[128]);

                int LA128_0 = input.LA(1);

                if ( ((LA128_0>=WS && LA128_0<=COMMENT)) ) {
                    alt128=1;
                }


                } finally {dbg.exitDecision(128);}

                switch (alt128) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(680,7);
            	    if ( (input.LA(1)>=WS && input.LA(1)<=COMMENT) ) {
            	        input.consume();
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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(681, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:20: esPred
        {
        dbg.location(457,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css31687);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:6: esPred
        {
        dbg.location(459,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css31705);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:8: nsPred
        {
        dbg.location(473,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css31807);
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


    protected DFA87 dfa87 = new DFA87(this);
    protected DFA106 dfa106 = new DFA106(this);
    protected DFA114 dfa114 = new DFA114(this);
    protected DFA116 dfa116 = new DFA116(this);
    protected DFA122 dfa122 = new DFA122(this);
    static final String DFA87_eotS =
        "\21\uffff";
    static final String DFA87_eofS =
        "\21\uffff";
    static final String DFA87_minS =
        "\1\5\7\uffff\5\0\4\uffff";
    static final String DFA87_maxS =
        "\1\162\7\uffff\5\0\4\uffff";
    static final String DFA87_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA87_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\4\uffff}>";
    static final String[] DFA87_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\27\uffff\1\14\1\uffff"+
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

    static final short[] DFA87_eot = DFA.unpackEncodedString(DFA87_eotS);
    static final short[] DFA87_eof = DFA.unpackEncodedString(DFA87_eofS);
    static final char[] DFA87_min = DFA.unpackEncodedStringToUnsignedChars(DFA87_minS);
    static final char[] DFA87_max = DFA.unpackEncodedStringToUnsignedChars(DFA87_maxS);
    static final short[] DFA87_accept = DFA.unpackEncodedString(DFA87_acceptS);
    static final short[] DFA87_special = DFA.unpackEncodedString(DFA87_specialS);
    static final short[][] DFA87_transition;

    static {
        int numStates = DFA87_transitionS.length;
        DFA87_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA87_transition[i] = DFA.unpackEncodedString(DFA87_transitionS[i]);
        }
    }

    class DFA87 extends DFA {

        public DFA87(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 87;
            this.eot = DFA87_eot;
            this.eof = DFA87_eof;
            this.min = DFA87_min;
            this.max = DFA87_max;
            this.accept = DFA87_accept;
            this.special = DFA87_special;
            this.transition = DFA87_transition;
        }
        public String getDescription() {
            return "()* loopback of 457:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA87_8 = input.LA(1);

                         
                        int index87_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index87_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA87_9 = input.LA(1);

                         
                        int index87_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index87_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA87_10 = input.LA(1);

                         
                        int index87_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index87_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA87_11 = input.LA(1);

                         
                        int index87_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index87_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA87_12 = input.LA(1);

                         
                        int index87_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index87_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 87, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA106_eotS =
        "\4\uffff";
    static final String DFA106_eofS =
        "\4\uffff";
    static final String DFA106_minS =
        "\2\5\2\uffff";
    static final String DFA106_maxS =
        "\2\162\2\uffff";
    static final String DFA106_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA106_specialS =
        "\4\uffff}>";
    static final String[] DFA106_transitionS = {
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\27\uffff\1\3\1\uffff"+
            "\3\3\1\uffff\6\3\10\uffff\1\2\1\3\13\uffff\3\1\44\uffff\1\3",
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\27\uffff\1\3\1\uffff"+
            "\3\3\1\uffff\6\3\10\uffff\1\2\1\3\13\uffff\3\1\44\uffff\1\3",
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
            return "564:21: ( ( ws )? LPAREN ( ws )? ( expr | '*' )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA114_eotS =
        "\12\uffff";
    static final String DFA114_eofS =
        "\12\uffff";
    static final String DFA114_minS =
        "\1\5\1\uffff\1\5\1\uffff\4\5\1\61\1\67";
    static final String DFA114_maxS =
        "\1\112\1\uffff\1\115\1\uffff\2\115\1\5\3\115";
    static final String DFA114_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA114_specialS =
        "\12\uffff}>";
    static final String[] DFA114_transitionS = {
            "\3\3\1\uffff\1\1\3\uffff\1\1\1\2\3\uffff\1\3\30\uffff\2\3\2"+
            "\uffff\2\3\16\uffff\2\1\12\3",
            "",
            "\1\5\2\3\12\uffff\1\3\31\uffff\1\3\2\uffff\2\3\20\uffff\12"+
            "\3\3\4",
            "",
            "\1\5\2\3\12\uffff\1\3\31\uffff\1\3\2\uffff\2\3\20\uffff\12"+
            "\3\3\4",
            "\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\27\uffff\3\3\2\uffff"+
            "\2\3\1\6\5\uffff\1\1\6\uffff\15\3\3\7",
            "\1\10",
            "\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\30\uffff\2\3\2\uffff"+
            "\2\3\6\uffff\1\1\6\uffff\15\3\3\7",
            "\1\6\5\uffff\1\1\6\uffff\1\3\14\uffff\3\11",
            "\1\1\6\uffff\1\3\14\uffff\3\11"
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
            return "()* loopback of 611:12: ( operator term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA116_eotS =
        "\12\uffff";
    static final String DFA116_eofS =
        "\12\uffff";
    static final String DFA116_minS =
        "\1\5\2\uffff\1\5\4\uffff\1\5\1\uffff";
    static final String DFA116_maxS =
        "\1\112\2\uffff\1\115\4\uffff\1\115\1\uffff";
    static final String DFA116_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\3";
    static final String DFA116_specialS =
        "\12\uffff}>";
    static final String[] DFA116_transitionS = {
            "\1\3\1\2\1\5\12\uffff\1\4\35\uffff\1\6\20\uffff\12\1",
            "",
            "",
            "\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\27\uffff\1\7\2"+
            "\11\2\uffff\2\11\1\7\14\uffff\1\7\14\11\3\10",
            "",
            "",
            "",
            "",
            "\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\30\uffff\2\11"+
            "\2\uffff\2\11\15\uffff\1\7\14\11\3\10",
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
            return "616:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA122_eotS =
        "\10\uffff";
    static final String DFA122_eofS =
        "\10\uffff";
    static final String DFA122_minS =
        "\1\5\1\uffff\3\5\1\uffff\1\61\1\67";
    static final String DFA122_maxS =
        "\1\112\1\uffff\2\115\1\5\1\uffff\2\115";
    static final String DFA122_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA122_specialS =
        "\10\uffff}>";
    static final String[] DFA122_transitionS = {
            "\1\2\2\1\12\uffff\1\1\31\uffff\1\1\2\uffff\2\1\20\uffff\12\1",
            "",
            "\3\1\6\uffff\1\1\3\uffff\1\1\27\uffff\3\1\2\uffff\2\1\1\4\5"+
            "\uffff\1\5\6\uffff\2\1\1\uffff\12\1\3\3",
            "\3\1\6\uffff\1\1\3\uffff\1\1\30\uffff\2\1\2\uffff\2\1\6\uffff"+
            "\1\5\6\uffff\2\1\1\uffff\12\1\3\3",
            "\1\6",
            "",
            "\1\4\5\uffff\1\5\6\uffff\1\1\14\uffff\3\7",
            "\1\5\6\uffff\1\1\14\uffff\3\7"
    };

    static final short[] DFA122_eot = DFA.unpackEncodedString(DFA122_eotS);
    static final short[] DFA122_eof = DFA.unpackEncodedString(DFA122_eofS);
    static final char[] DFA122_min = DFA.unpackEncodedStringToUnsignedChars(DFA122_minS);
    static final char[] DFA122_max = DFA.unpackEncodedStringToUnsignedChars(DFA122_maxS);
    static final short[] DFA122_accept = DFA.unpackEncodedString(DFA122_acceptS);
    static final short[] DFA122_special = DFA.unpackEncodedString(DFA122_specialS);
    static final short[][] DFA122_transition;

    static {
        int numStates = DFA122_transitionS.length;
        DFA122_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA122_transition[i] = DFA.unpackEncodedString(DFA122_transitionS[i]);
        }
    }

    class DFA122 extends DFA {

        public DFA122(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 122;
            this.eot = DFA122_eot;
            this.eof = DFA122_eof;
            this.min = DFA122_min;
            this.max = DFA122_max;
            this.accept = DFA122_accept;
            this.special = DFA122_special;
            this.transition = DFA122_transition;
        }
        public String getDescription() {
            return "642:3: ( expr | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0x003F0400038C0D30L,0x0004000000000000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0x003F0400038C0C30L,0x0004000000003800L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0x003F0400038C0C30L,0x0004000000000000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0x003F0400038C0830L,0x0004000000000000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0x003F0400038C0820L,0x0004000000000000L});
    public static final BitSet FOLLOW_body_in_styleSheet174 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_in_namespaces199 = new BitSet(new long[]{0x0000000000000012L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_namespaces201 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_NAMESPACE_SYM_in_namespace217 = new BitSet(new long[]{0x00000000000000E0L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_namespace219 = new BitSet(new long[]{0x00000000000000E0L,0x0000000000003800L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespace223 = new BitSet(new long[]{0x00000000000000E0L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_namespace225 = new BitSet(new long[]{0x00000000000000E0L,0x0000000000003800L});
    public static final BitSet FOLLOW_resourceIdentifier_in_namespace230 = new BitSet(new long[]{0x0000000000000200L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_namespace232 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_namespace235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_namespacePrefixName248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_resourceIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet286 = new BitSet(new long[]{0x0000000000000040L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_charSet288 = new BitSet(new long[]{0x0000000000000040L,0x0000000000003800L});
    public static final BitSet FOLLOW_charSetValue_in_charSet291 = new BitSet(new long[]{0x0000000000000200L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_charSet293 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_charSet296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_charSetValue310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_importItem_in_imports324 = new BitSet(new long[]{0x0000000000000402L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_imports326 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_importItem347 = new BitSet(new long[]{0x00000000000000E0L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_importItem349 = new BitSet(new long[]{0x00000000000000E0L,0x0000000000003800L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem352 = new BitSet(new long[]{0x4000000000070220L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_importItem354 = new BitSet(new long[]{0x4000000000070220L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem357 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_importItem359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media375 = new BitSet(new long[]{0x4000000000071020L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_media377 = new BitSet(new long[]{0x4000000000071020L});
    public static final BitSet FOLLOW_mediaQueryList_in_media380 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_media390 = new BitSet(new long[]{0x003F040000842020L,0x0004000000003800L});
    public static final BitSet FOLLOW_ws_in_media392 = new BitSet(new long[]{0x003F040000842020L,0x0004000000000000L});
    public static final BitSet FOLLOW_rule_in_media411 = new BitSet(new long[]{0x003F040000842020L,0x0004000000003800L});
    public static final BitSet FOLLOW_page_in_media415 = new BitSet(new long[]{0x003F040000842020L,0x0004000000003800L});
    public static final BitSet FOLLOW_ws_in_media419 = new BitSet(new long[]{0x003F040000842020L,0x0004000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_media433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList449 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList453 = new BitSet(new long[]{0x4000000000070020L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList455 = new BitSet(new long[]{0x4000000000070020L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList458 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery477 = new BitSet(new long[]{0x0000000000070020L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_mediaQuery479 = new BitSet(new long[]{0x0000000000070020L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery486 = new BitSet(new long[]{0x0000000000008002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_mediaQuery488 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery493 = new BitSet(new long[]{0x4000000000070020L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_mediaQuery495 = new BitSet(new long[]{0x4000000000070020L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery498 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery506 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery510 = new BitSet(new long[]{0x4000000000070020L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_mediaQuery512 = new BitSet(new long[]{0x4000000000070020L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery515 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression567 = new BitSet(new long[]{0x0000000000000020L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_mediaExpression569 = new BitSet(new long[]{0x0000000000000020L,0x0000000000003800L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression572 = new BitSet(new long[]{0x8000040000000000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_mediaExpression574 = new BitSet(new long[]{0x8000040000000000L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression579 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_ws_in_mediaExpression581 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_expr_in_mediaExpression584 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression589 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_mediaExpression591 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body620 = new BitSet(new long[]{0x003F0400038C0822L,0x0004000000003800L});
    public static final BitSet FOLLOW_ws_in_body622 = new BitSet(new long[]{0x003F0400038C0822L,0x0004000000000000L});
    public static final BitSet FOLLOW_rule_in_bodyItem647 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem659 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem683 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem695 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_bodyItem707 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document734 = new BitSet(new long[]{0x0000000000700080L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_moz_document736 = new BitSet(new long[]{0x0000000000700080L,0x0000000000003800L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document741 = new BitSet(new long[]{0x0000000000005000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_moz_document743 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_COMMA_in_moz_document749 = new BitSet(new long[]{0x0000000000700080L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_moz_document751 = new BitSet(new long[]{0x0000000000700080L,0x0000000000003800L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document754 = new BitSet(new long[]{0x0000000000005000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_moz_document756 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document763 = new BitSet(new long[]{0x003F040000842020L,0x0004000000003800L});
    public static final BitSet FOLLOW_ws_in_moz_document765 = new BitSet(new long[]{0x003F040000842020L,0x0004000000000000L});
    public static final BitSet FOLLOW_rule_in_moz_document774 = new BitSet(new long[]{0x003F040000842020L,0x0004000000003800L});
    public static final BitSet FOLLOW_page_in_moz_document778 = new BitSet(new long[]{0x003F040000842020L,0x0004000000003800L});
    public static final BitSet FOLLOW_ws_in_moz_document782 = new BitSet(new long[]{0x003F040000842020L,0x0004000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page830 = new BitSet(new long[]{0x0000040000001020L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_page832 = new BitSet(new long[]{0x0000040000001020L});
    public static final BitSet FOLLOW_IDENT_in_page837 = new BitSet(new long[]{0x0000040000001000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_page839 = new BitSet(new long[]{0x0000040000001000L});
    public static final BitSet FOLLOW_pseudoPage_in_page846 = new BitSet(new long[]{0x0000000000001000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_page848 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_page861 = new BitSet(new long[]{0x000003FFFC042220L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_page863 = new BitSet(new long[]{0x000003FFFC042220L});
    public static final BitSet FOLLOW_declaration_in_page918 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page920 = new BitSet(new long[]{0x0000000000002200L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_page922 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_SEMI_in_page928 = new BitSet(new long[]{0x000003FFFC042220L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_page930 = new BitSet(new long[]{0x000003FFFC042220L});
    public static final BitSet FOLLOW_declaration_in_page934 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page936 = new BitSet(new long[]{0x0000000000002200L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_page938 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_RBRACE_in_page953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle974 = new BitSet(new long[]{0x0000000000000020L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_counterStyle976 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle979 = new BitSet(new long[]{0x0000000000001000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_counterStyle981 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle992 = new BitSet(new long[]{0x0000000000042220L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_counterStyle994 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle997 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1001 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1011 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1032 = new BitSet(new long[]{0x0000000000001000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_fontFace1034 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1045 = new BitSet(new long[]{0x0000000000042220L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_fontFace1047 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_fontFace1050 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_fontFace1054 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1064 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1079 = new BitSet(new long[]{0x0000000000001000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_margin1081 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1084 = new BitSet(new long[]{0x0000000000042220L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_margin1086 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_margin1089 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_margin1091 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1093 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1322 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator1345 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_operator1347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_operator1356 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_operator1358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator1386 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_combinator1388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator1397 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_combinator1399 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator1408 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_combinator1410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property1470 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_property1478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule1503 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_rule1513 = new BitSet(new long[]{0x0000000000042220L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_rule1515 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_rule1518 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_rule1532 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_rule1542 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations1580 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_SEMI_in_declarations1584 = new BitSet(new long[]{0x0000000000040222L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_declarations1586 = new BitSet(new long[]{0x0000000000040222L});
    public static final BitSet FOLLOW_declaration_in_declarations1589 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1613 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup1616 = new BitSet(new long[]{0x003F040000040020L,0x0004000000003800L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup1618 = new BitSet(new long[]{0x003F040000040020L,0x0004000000000000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1621 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1644 = new BitSet(new long[]{0x003F740000040022L,0x0004000000000000L});
    public static final BitSet FOLLOW_combinator_in_selector1647 = new BitSet(new long[]{0x003F040000040020L,0x0004000000000000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1649 = new BitSet(new long[]{0x003F740000040022L,0x0004000000000000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence1683 = new BitSet(new long[]{0x003F040000040022L,0x0004000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1690 = new BitSet(new long[]{0x003F040000040022L,0x0004000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1708 = new BitSet(new long[]{0x003F040000040022L,0x0004000000000000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector1810 = new BitSet(new long[]{0x0030000000040020L});
    public static final BitSet FOLLOW_elementName_in_typeSelector1816 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_typeSelector1818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nsPred1838 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred1847 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix1862 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix1866 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix1870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent1904 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent1913 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent1925 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent1937 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_elementSubsequent1949 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId1972 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_114_in_cssId1978 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId1980 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2008 = new BitSet(new long[]{0x0000000000040020L});
    public static final BitSet FOLLOW_set_in_cssClass2010 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute2079 = new BitSet(new long[]{0x0030000000000020L,0x0000000000003800L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute2086 = new BitSet(new long[]{0x0030000000000020L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_slAttribute2089 = new BitSet(new long[]{0x0030000000000020L,0x0000000000003800L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute2100 = new BitSet(new long[]{0x3F80000000000000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_slAttribute2102 = new BitSet(new long[]{0x3F80000000000000L});
    public static final BitSet FOLLOW_set_in_slAttribute2144 = new BitSet(new long[]{0x0000000000000060L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_slAttribute2324 = new BitSet(new long[]{0x0000000000000060L,0x0000000000003800L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute2343 = new BitSet(new long[]{0x2000000000000000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_slAttribute2361 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute2390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName2406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue2420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo2480 = new BitSet(new long[]{0x0000000000060020L});
    public static final BitSet FOLLOW_set_in_pseudo2544 = new BitSet(new long[]{0x4000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_pseudo2601 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2604 = new BitSet(new long[]{0x80119000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_ws_in_pseudo2606 = new BitSet(new long[]{0x80119000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_expr_in_pseudo2611 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_STAR_in_pseudo2615 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo2699 = new BitSet(new long[]{0x4000000000000000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_pseudo2701 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2704 = new BitSet(new long[]{0x803F040000040020L,0x0004000000003800L});
    public static final BitSet FOLLOW_ws_in_pseudo2706 = new BitSet(new long[]{0x803F040000040020L,0x0004000000000000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo2709 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2712 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration2756 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_COLON_in_declaration2758 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_ws_in_declaration2760 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_propertyValue_in_declaration2763 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_prio_in_declaration2765 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_propertyValue2789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio2864 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_prio2866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expr2888 = new BitSet(new long[]{0x00019800000440E2L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_operator_in_expr2891 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_term_in_expr2893 = new BitSet(new long[]{0x00019800000440E2L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_unaryOperator_in_term2916 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_set_in_term2937 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_STRING_in_term3120 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_IDENT_in_term3128 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_GEN_in_term3136 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_URI_in_term3144 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_hexColor_in_term3152 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_function_in_term3160 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_term3172 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function3188 = new BitSet(new long[]{0x4000000000000000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_function3190 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_function3195 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_ws_in_function3197 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_expr_in_function3208 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_fnAttribute_in_function3226 = new BitSet(new long[]{0x8000000000004000L});
    public static final BitSet FOLLOW_COMMA_in_function3229 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_ws_in_function3231 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_fnAttribute_in_function3234 = new BitSet(new long[]{0x8000000000004000L});
    public static final BitSet FOLLOW_RPAREN_in_function3255 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName3303 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_COLON_in_functionName3305 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_functionName3309 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName3312 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_functionName3314 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute3336 = new BitSet(new long[]{0x0080000000000000L,0x0000000000003800L});
    public static final BitSet FOLLOW_ws_in_fnAttribute3338 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute3341 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_ws_in_fnAttribute3343 = new BitSet(new long[]{0x00019000000400E0L,0x0000000000003FFEL});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute3346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName3361 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName3364 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName3366 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_expr_in_fnAttributeValue3380 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor3398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws3419 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003800L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css31687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css31705 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css31807 = new BitSet(new long[]{0x0000000000000002L});

}