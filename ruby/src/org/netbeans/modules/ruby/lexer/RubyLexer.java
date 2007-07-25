/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.lexer;

import java.io.IOException;
import java.io.Reader;

import org.jruby.common.NullWarnings;
import org.jruby.lexer.yacc.LexState;
import org.jruby.lexer.yacc.LexerSource;
import org.jruby.lexer.yacc.LexerSource;
import org.jruby.lexer.yacc.RubyYaccLexer;
import org.jruby.lexer.yacc.RubyYaccLexer;
import org.jruby.lexer.yacc.StrTerm;
import org.jruby.lexer.yacc.StringTerm;
import org.jruby.lexer.yacc.SyntaxException;
import org.jruby.parser.Tokens;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.ErrorManager;


/**
 * A scanner for Ruby, which directly uses the JRuby lexer and translates
 * from JRuby tokens to NetBeans lexer-based tokens
 *
 * @todo Should I generate a single large token for composite token types
 *  like strings and regular expressions? Today, I go to a lot of trouble
 *  to do state saving between the opening quote, middle literal string,
 *  and ending quote, for strings. (Ditto for regular expressions, single
 *  quoted strings, other forms of quoted strings, etc.).
 *  This allows me to have for example the / /'s in regular expressions
 *  stay black, and have only the embedded portion be green. But is that
 *  really necessary? If I instead were to eat up a whole String combination
 *  into a single token and return it as a single token, that would ensure
 *  that for incremental parsing, I always get called on a token boundary
 *  where I don't need the extra state saving. All I would need would
 *  be the lexer state (if different than the default state) which is a small
 *  integer (which gets compressed to single bytes by the lexer infrastructure.)
 *
 * @author Tor Norbye
 */
public final class RubyLexer implements Lexer<GsfTokenId> {
    /** This is still not working; I wonder if release() is called correctly at all times...*/
    private static final boolean REUSE_LEXERS = false;
    private static RubyLexer cached;
    private final RubyYaccLexer lexer;
    private LexerSource lexerSource;
    private boolean inRegexp;
    private LexerInput input;
    private TokenFactory<GsfTokenId> tokenFactory;
    private boolean substituting;
    private boolean inSymbol;
    private boolean inEmbedded;

    private RubyLexer(LexerRestartInfo<GsfTokenId> info) {
        lexer = new RubyYaccLexer();
        // XXX Do something at scan time about illegal characters?
        lexer.setWarnings(new NullWarnings());
        lexer.setPreserveSpaces(true);
    }

    public static synchronized RubyLexer create(LexerRestartInfo<GsfTokenId> info) {
        RubyLexer rubyLexer = cached;

        if (rubyLexer == null) {
            rubyLexer = new RubyLexer(info);
        }

        rubyLexer.restart(info);

        return rubyLexer;
    }

    void restart(LexerRestartInfo<GsfTokenId> info) {
        inRegexp = substituting = inSymbol = inEmbedded = false;
        lexer.reset();

        input = info.input();
        tokenFactory = info.tokenFactory();

        String fileName = "unknown";
        Reader lexerReader = new LexerInputReader(input);
        lexerSource = new LexerSource(fileName, lexerReader, 0);
        lexer.setSource(lexerSource);

        Object state = info.state();

        if (state instanceof JRubyLexerRestartInfo) {
            ((JRubyLexerRestartInfo)state).initializeState(this);
        } else if (state instanceof Integer) {
            int stateValue = ((Integer)state).intValue();
            lexer.setState(LexState.fromOrdinal(stateValue));
        }
    }

    public void release() {
        if (REUSE_LEXERS) {
            // Possibly reset the structures that could cause memory leaks
            synchronized (RubyLexer.class) {
                cached = this;
            }
        }
    }

    public Object state() {
        if (JRubyLexerRestartInfo.needsStateStorage(this)) {
            return new JRubyLexerRestartInfo(this);
        }

        // We only need to store the state of the lexer when it's in a significant state,
        // e.g. outside of normal expressions and with no string processing in progress
        LexState state = lexer.getLexState();

        if (state == null) {
            return null;
        }

        if (lexer.getStrTerm() != null) {
            return new JRubyLexerRestartInfo(this);
        }

        // The lexer can store integer states very efficiently
        // (besides, Integer.valueOf will cache all these values since they are < 128)
        return Integer.valueOf(state.getOrdinal());
    }

    private Token<GsfTokenId> token(GsfTokenId id, int length) {
        String fixedText = id.fixedText();

        return (fixedText != null) ? tokenFactory.getFlyweightToken(id, fixedText)
                                   : tokenFactory.createToken(id, length);
    }

    public Token<GsfTokenId> nextToken() {
        int token = 0;
        int tokenLength = 0;
        int oldOffset = lexerSource.getOffset();

        while (tokenLength == 0) {
            try {
                lexer.advance();
                token = lexer.token();

                StrTerm strTerm = lexer.getStrTerm();

                if (strTerm != null) {
                    strTerm.splitEmbeddedTokens();
                }
            } catch (StringTerm.UnterminatedStringException use) {
                token = Tokens.yyErrorCode;
                // TODO: Compute the position of the FIRST newline in the
                // output - how can I do that?

                // Update lexer input to make sure it records the right
                // character boundaries for the tokens (since incremental lexing
                // will restart at token boundaries, and we want to make sure
                // it knows in the character stream where those boundaries truly are
                int readAhead = lexerSource.chompReadAhead();

                if (readAhead > 0) {
                    input.backup(readAhead);
                }
                
                input.backup(input.readLengthEOF());
                tokenLength = 0;
                    
                // Read forward and stop at the first newline
                while (true) {
                    int ch = input.read();

                    if (ch == LexerInput.EOF) {
                        break;
                    }

                    tokenLength++;

                    if (ch == '\n') {
                        break;
                    }
                }
                
                lexerSource.setOffset(oldOffset+tokenLength);
                
                if (tokenLength > 0) {
                    return token(RubyTokenId.ERROR, tokenLength);
                } else {
                    // Buffer ends with an unterminated string constant - nothing to do
                    return null;
                }
                
            } catch (SyntaxException ex) {
                token = Tokens.yyErrorCode; // TODO - generate incomplete tokens?
                tokenLength = lexerSource.getOffset() - oldOffset;

                if (tokenLength == 0) {
                    if (input.readLength() > 0) {
                        return token(RubyTokenId.IDENTIFIER, input.readLength()); // XXX?
                    } else {
                        return null;
                    }
                }

                break;
            } catch (Throwable ex) { // includes SyntaxException
                ErrorManager.getDefault().notify(ex);

                break;
            }

            if (token == 0) { // EOF

                if (input.readLength() > 0) {
                    return token(RubyTokenId.IDENTIFIER, input.readLength()); // XXX?
                } else {
                    return null;
                }
            }

            int offset = lexerSource.getOffset();
            tokenLength = offset - oldOffset;
        }

        // Update lexer input to make sure it records the right
        // character boundaries for the tokens (since incremental lexing
        // will restart at token boundaries, and we want to make sure
        // it knows in the character stream where those boundaries truly are
        int readAhead = lexerSource.chompReadAhead();

        if (readAhead > 0) {
            input.backup(readAhead);
        }

        // Map to IDE types
        GsfTokenId id = getTokenId(token, oldOffset);

        // Fix #102082
        if (inSymbol) {
            // A type symbol in front of a keyword, literal or constant
            // should be lexed as a symbol
            String category = id.primaryCategory();
            boolean isString = "string".equals(category); // NOI18N

            if (!isString || (id == RubyTokenId.STRING_END || id == RubyTokenId.QUOTED_STRING_END)) {
                inSymbol = (token == Tokens.tSYMBEG);
            }

            if (isString || id == RubyTokenId.IDENTIFIER || id == RubyTokenId.CONSTANT ||
                    "keyword".equals(category)) { // NOI18N
                id = RubyTokenId.TYPE_SYMBOL;
            }
        } else {
            inSymbol = (token == Tokens.tSYMBEG);
        }

        if (tokenLength <= 0) {
            // XXX this is not right but better than asserting in the lexer!
            // Just assign some default text attributes to unexpected text, one character at a time
            return token(GsfTokenId.IDENTIFIER, 1);
        }

        return token(id, tokenLength);
    }

    /** @todo Move classification of tokens into TokenTypes into JRuby somehow */
    private GsfTokenId getTokenId(int token, int offset) {
        // If you add any new token types here, remember to update #getRelevantTokenTypes below
        switch (token) {
        case Tokens.tCOMMENT:
            return RubyTokenId.LINE_COMMENT;

        case Tokens.tWHITESPACE:
            return RubyTokenId.WHITESPACE;

        case Tokens.tFLOAT:
            return RubyTokenId.FLOAT_LITERAL;

        case Tokens.tINTEGER:
            return RubyTokenId.INT_LITERAL;

        case Tokens.tQWORDS_BEG:
        case Tokens.tWORDS_BEG:
        case Tokens.tSTRING_BEG:
        case Tokens.tXSTRING_BEG:

            if (lexer.getStrTerm() != null) {
                substituting = lexer.getStrTerm().isSubstituting();
            } else {
                substituting = false;
            }

            return substituting ? RubyTokenId.QUOTED_STRING_BEGIN : RubyTokenId.STRING_BEGIN;

        case Tokens.tSTRING_DVAR:
        case Tokens.tSTRING_DBEG:
            inEmbedded = true;

            return inRegexp ? RubyTokenId.REGEXP_LITERAL : RubyTokenId.STRING_LITERAL;

        case Tokens.tSTRING_END:
            return substituting ? RubyTokenId.QUOTED_STRING_END : RubyTokenId.STRING_END;

        case Tokens.tSTRING_CONTENT: // What about tXSTRING??

            if (inEmbedded) {
                inEmbedded = false;

                return RubyTokenId.EMBEDDED_RUBY;
            } else if (inRegexp) {
                return RubyTokenId.REGEXP_LITERAL;
            } else {
                // For heredocs I may not know when I see the opening
                if (lexer.getStrTerm() != null) {
                    substituting = lexer.getStrTerm().isSubstituting();
                    if (substituting) {
                        return RubyTokenId.QUOTED_STRING_LITERAL;
                    } else {
                        return RubyTokenId.STRING_LITERAL;
                    }
                } else {
                    substituting = false;
                    return RubyTokenId.STRING_LITERAL;
                }
            }

        case Tokens.tREGEXP_BEG:
            inRegexp = true;

            return RubyTokenId.REGEXP_BEGIN;

        case Tokens.tREGEXP_END:
            inRegexp = false;

            return RubyTokenId.REGEXP_END;

        case Tokens.tDOCUMENTATION:
            return RubyTokenId.DOCUMENTATION;

        case Tokens.yyErrorCode:
            return RubyTokenId.ERROR;

        case Tokens.tGVAR: // Global variable
            return RubyTokenId.GLOBAL_VAR;

        case Tokens.tIVAR: // Instance variable
            return RubyTokenId.INSTANCE_VAR;

        case Tokens.tCVAR: // Class variable
            return RubyTokenId.CLASS_VAR;

        case Tokens.tCONSTANT: // Constant
            return RubyTokenId.CONSTANT;

        case Tokens.tIDENTIFIER:
            return RubyTokenId.IDENTIFIER;

        case Tokens.tSYMBEG:
            return RubyTokenId.TYPE_SYMBOL;

        case '[': 
            // I sometimes get ascii '[' instead of LBRACK, for example in this expression:
            //  for k, v in sort{|a1, a2| a1[0].id2name <=> a2[0].id2name}
        case Tokens.tLBRACK:
            return RubyTokenId.LBRACKET;

        case Tokens.tRBRACK:
            return RubyTokenId.RBRACKET;

        case Tokens.tLPAREN:
        case Tokens.tLPAREN2: // XXX What is this?
        case Tokens.tLPAREN_ARG: // XXX What is this?
            return RubyTokenId.LPAREN;

        case Tokens.tRPAREN:
            return RubyTokenId.RPAREN;

        case Tokens.tLCURLY: // block (primary)
        case Tokens.tLBRACE: // hash
        case Tokens.tLBRACE_ARG: // block (expr)
            return RubyTokenId.LBRACE;

        case Tokens.tRCURLY:
            return RubyTokenId.RBRACE;

        case Tokens.kDEF:
            return RubyTokenId.DEF;

        case Tokens.kEND:
            return RubyTokenId.END;

        case Tokens.kCLASS:
            return RubyTokenId.CLASS;

        case Tokens.kMODULE:
            return RubyTokenId.MODULE;

        case Tokens.kBEGIN:
            return RubyTokenId.BEGIN;

        case Tokens.kIF:
            return RubyTokenId.IF;

        case Tokens.kUNLESS:
            return RubyTokenId.UNLESS;

        case Tokens.kWHILE:
            return RubyTokenId.WHILE;

        case Tokens.kUNTIL:
            return RubyTokenId.UNTIL;

        case Tokens.kDO_BLOCK:
        case Tokens.kDO_COND:
            return RubyTokenId.ANY_KEYWORD;

        case Tokens.kDO:
            return RubyTokenId.DO;
            
            
        case Tokens.kCASE:
            return RubyTokenId.CASE;

        case Tokens.kFOR:
            return RubyTokenId.FOR;

        case Tokens.kELSE:
            return RubyTokenId.ELSE;

        case Tokens.kELSIF:
            return RubyTokenId.ELSIF;

        case Tokens.kENSURE:
            return RubyTokenId.ENSURE;

        case Tokens.kWHEN:
            return RubyTokenId.WHEN;

        case Tokens.kRESCUE:
            return RubyTokenId.RESCUE;

        case Tokens.kSUPER:
            return RubyTokenId.SUPER;

        case Tokens.kSELF:
            return RubyTokenId.SELF;
            
        case Tokens.tAREF:
        case Tokens.tASET:
            // XXX Change some of these into [, or ] ?
            return RubyTokenId.ANY_OPERATOR;

        case Tokens.kRESCUE_MOD:
            
        case Tokens.kUNDEF:
        case Tokens.kTHEN:
        case Tokens.kBREAK:
        case Tokens.kNEXT:
        case Tokens.kREDO:
        case Tokens.kRETRY:
        case Tokens.kIN:
        case Tokens.kRETURN:
        case Tokens.kYIELD:
        case Tokens.kNIL:
        case Tokens.kTRUE:
        case Tokens.kFALSE:
        case Tokens.kAND:
        case Tokens.kOR:
        case Tokens.kNOT:
        case Tokens.kIF_MOD:
        case Tokens.kUNLESS_MOD:
        case Tokens.kWHILE_MOD:
        case Tokens.kUNTIL_MOD:
        case Tokens.kALIAS:
        case Tokens.kDEFINED:
        case Tokens.klBEGIN: // "BEGIN { }": not matched with END { }
        case Tokens.klEND: // "END { }": not matched with BEGIN { }
        case Tokens.k__LINE__:
        case Tokens.k__FILE__:
            return RubyTokenId.ANY_KEYWORD;
            
        case '=':
        // Commas are most frequently used in argument lists and array declarations
        // where treating "," as a continuation operator causes the first and 
        // subsequent entries to be misaligned - see FormattingTest.testArrayDecl
        // and testHashDecl    
        //case ',':
        case Tokens.tPLUS:
        case Tokens.tMINUS:
        case Tokens.tDIVIDE:
        // Doesn't seem to work
        //case '|':
        //case Tokens.tPIPE:
        case Tokens.tLT:
        case Tokens.tGT:
        case Tokens.tPOW:
        case Tokens.tCMP:
            // start, percent, bang and a few others missing
            
        case Tokens.tANDOP:
        case Tokens.tOROP:
        case Tokens.tEQ:
        case Tokens.tEQQ:
        case Tokens.tNEQ:
        case Tokens.tGEQ:
        case Tokens.tLEQ:
        case Tokens.tMATCH:
        case Tokens.tNMATCH:
        case Tokens.tLSHFT:
        case Tokens.tRSHFT:
        case Tokens.tASSOC:
        case Tokens.tOP_ASGN:
            return RubyTokenId.NONUNARY_OP;

        case Tokens.tDOT:
            return RubyTokenId.DOT;

        case Tokens.tDOT2:
        case Tokens.tDOT3:
            return RubyTokenId.RANGE;

        case Tokens.tCOLON3:
            return RubyTokenId.COLON3;

        default:
            return RubyTokenId.IDENTIFIER;
        }
    }

    private static class JRubyLexerRestartInfo {
        /** Bit set when we're in regular expressions */
        private static final int IN_REGEXP = 1;

        /** Bit set when we're in symbols */
        private static final int IN_SYMBOL = 2;

        /** Bit set when we're in an embedded ruby context... #{here} */
        private static final int IN_EMBEDDED = 4;

        /** Bit set when we're in a substituting/doublequoted string */
        private static final int IN_SUBSTITUTING = 8;

        /** Bit set when we need to set the spaceSeen flag in RubyYaccLexer */
        private static final int SET_SPACE_SEEN = 16;

        /** Bit set when we need to set commandStart in RubyYaccLexer */
        private static final int SET_COMMAND_START = 32;

        private final StrTerm strTerm;
        private int localState;
        private final LexState lexState;
        private Object strTermState;
        private final RubyYaccLexer.HeredocContext heredocContext;

        JRubyLexerRestartInfo(RubyLexer rubyLexer) {
            strTerm = rubyLexer.lexer.getStrTerm();

            if (strTerm != null) {
                strTermState = strTerm.getMutableState();
            }
            
            heredocContext = rubyLexer.lexer.heredocContext;

            lexState = rubyLexer.lexer.getLexState();

            if (rubyLexer.inRegexp) {
                localState += IN_REGEXP;
            }

            if (rubyLexer.inSymbol) {
                localState += IN_SYMBOL;
            }

            if (rubyLexer.inEmbedded) {
                localState += IN_EMBEDDED;
            }

            if (rubyLexer.substituting) {
                localState += IN_SUBSTITUTING;
            }

            if (rubyLexer.lexer.isSetSpaceSeen()) {
                localState += SET_SPACE_SEEN;
            }

            if (rubyLexer.lexer.isCommandStart()) {
                localState += SET_COMMAND_START;
            }
        }

        /** Return true iff the given lexer needs custom state storage beyond the state integers */
        public static boolean needsStateStorage(RubyLexer rubyLexer) {
            return rubyLexer.inRegexp || rubyLexer.inSymbol || rubyLexer.inEmbedded ||
            rubyLexer.substituting || rubyLexer.lexer.isCommandStart() || rubyLexer.lexer.heredocContext != null||
            rubyLexer.lexer.isSetSpaceSeen();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            final JRubyLexerRestartInfo other = (JRubyLexerRestartInfo)obj;

            if ((this.strTerm != other.strTerm) &&
                    ((this.strTerm == null) || !this.strTerm.equals(other.strTerm))) {
                return false;
            }

            if (this.localState != other.localState) {
                return false;
            }

            if ((this.lexState != other.lexState) &&
                    ((this.lexState == null) ||
                    !(this.lexState.getOrdinal() == other.lexState.getOrdinal()))) {
                return false;
            }

            if ((this.strTermState != other.strTermState) &&
                    ((this.strTermState == null) || !this.strTermState.equals(other.strTermState))) {
                return false;
            }
            
            if ((this.heredocContext != other.heredocContext) &&
                    ((this.heredocContext == null) || !this.heredocContext.equals(other.heredocContext))) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;

            hash = (43 * hash) + this.localState;
            hash = (43 * hash) + ((this.strTerm != null) ? this.strTerm.hashCode() : 0);
            hash = (43 * hash) + ((this.strTermState != null) ? this.strTermState.hashCode() : 0);
            // UGH - what about heredocTerms?
            //hash = (43 * hash) + ((this.heredocTerms != null) ? this.heredocTerms.getMutableState().hashCode() : 0);

            return hash;
        }

        private static String toStateString(int localState) {
            StringBuilder sb = new StringBuilder();
            if ((localState & IN_REGEXP) != 0) {
                sb.append("regexp|");
            }

            if ((localState & IN_SYMBOL) != 0) {
                sb.append("symbol|");
            }

            if ((localState & IN_EMBEDDED) != 0) {
                sb.append("embedded|");
            }

            if ((localState & IN_SUBSTITUTING) != 0) {
                sb.append("substituting|");
            }

            if ((localState & SET_COMMAND_START) != 0) {
                sb.append("commandstart|");
            }

            if ((localState & SET_SPACE_SEEN) != 0) {
                sb.append("spaceseen|");
            }
            
            String s = sb.toString();

            if (s.endsWith("|")) {
                s = s.substring(0, s.length()-1);
            } else if (s.length() == 0) {
                s = "-";
            }

            return s;
        }

        @Override
        public String toString() {
            return "RubyLexerState[" + toStateString(localState) + "," + strTerm + "," + lexState + "," +
            strTermState + "," + heredocContext + "]";
        }

        void initializeState(RubyLexer rubyLexer) {
            rubyLexer.lexer.setStrTerm(strTerm);
            
            rubyLexer.lexer.heredocContext = heredocContext;

            if ((strTermState != null) && (strTerm != null)) {
                strTerm.setMutableState(strTermState);
            }

            if ((localState & IN_REGEXP) != 0) {
                rubyLexer.inRegexp = true;
            }

            if ((localState & IN_SYMBOL) != 0) {
                rubyLexer.inSymbol = true;
            }

            if ((localState & IN_EMBEDDED) != 0) {
                rubyLexer.inEmbedded = true;
            }

            if ((localState & IN_SUBSTITUTING) != 0) {
                rubyLexer.substituting = true;
            }

            if ((localState & SET_COMMAND_START) != 0) {
                rubyLexer.lexer.setCommandStart(true);
            }

            if ((localState & SET_SPACE_SEEN) != 0) {
                rubyLexer.lexer.setSpaceSeen(true);
            }

            rubyLexer.lexer.setLexState(lexState);
        }
    }

    private static class LexerInputReader extends Reader {
        private LexerInput input;

        LexerInputReader(LexerInput input) {
            this.input = input;
        }

        public int read(char[] buf, int off, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                int c = input.read();

                if (c == LexerInput.EOF) {
                    return -1;
                }

                buf[i + off] = (char)c;
            }

            return len;
        }

        public void close() throws IOException {
        }
    }
}
