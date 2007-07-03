/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.lexer.yacc;

import org.jruby.ast.StrNode;
import org.jruby.parser.Tokens;
import org.jruby.util.ByteList;


public class HeredocTerm extends StrTerm {
	private final String eos;
	private final int func;
	private final String lastLine;
    
    public HeredocTerm(String eos, int func, String lastLine) {
        this.eos = eos;
        this.func = func;
        this.lastLine = lastLine;
    }
    
    public int parseString(RubyYaccLexer lexer, LexerSource src) throws java.io.IOException {
        char c;
        boolean indent = (func & RubyYaccLexer.STR_FUNC_INDENT) != 0;
        ByteList str = new ByteList();
        // BEGIN NETBEANS MODIFICATIONS
        if (lexer.getPreserveSpaces()) {
            boolean done = src.matchString(eos + '\n', indent);
            if (done) {
                lexer.yaccValue = new StrNode(lexer.getPosition(), str);
                lexer.setStrTerm(new StringTerm(-1, '\0', '\0'));
                src.setIsANewLine(true);
                return Tokens.tSTRING_END;
            }
        }
        // END NETBEANS MODIFICATIONS

        if ((c = src.read()) == RubyYaccLexer.EOF) {
            throw new SyntaxException(src.getPosition(), "can't find string \"" + eos + "\" anywhere before EOF");
        }
        if (src.wasBeginOfLine() && src.matchString(eos + '\n', indent)) {
            // BEGIN NETBEANS MODIFICATIONS
            if (lastLine != null)
            // END NETBEANS MODIFICATIONS
                src.unreadMany(lastLine);
            return Tokens.tSTRING_END;
        }

        if ((func & RubyYaccLexer.STR_FUNC_EXPAND) == 0) {
            /*
             * if (c == '\n') { support.unread(c); }
             */

            // Something missing here...
            /*
             * int lastLineLength = here.getLastLineLength();
             * 
             * if (lastLineLength > 0) { // It looks like I needed to append
             * last line as well...
             * support.unreadMany(here.getLastLineLength());
             * str.append(support.readLine()); str.append("\n"); }
             */

            /*
             * c was read above and should be unread before we start
             * to fill the str buffer
             */
            src.unread(c);
            do {
                str.append(src.readLineBytes());
                str.append('\n');

                if (src.peek('\0')) {
                    throw new SyntaxException(src.getPosition(), "can't find string \"" + eos + "\" anywhere before EOF");
                }
            } while (!src.matchString(eos + '\n', indent));
        } else {
            ByteList buffer = new ByteList();
            if (c == '#') {
                switch (c = src.read()) {
                case '$':
                case '@':
                    // BEGIN NETBEANS MODIFICATIONS
                    if (processingEmbedded == LOOKING_FOR_EMBEDDED) {
                        processingEmbedded = EMBEDDED_DVAR;
                    }
                    // END NETBEANS MODIFICATIONS
                    src.unread(c);
                    lexer.setValue(new Token("#" + c, lexer.getPosition()));
                    return Tokens.tSTRING_DVAR;
                case '{':
                    // BEGIN NETBEANS MODIFICATIONS
                    if (processingEmbedded == LOOKING_FOR_EMBEDDED) {
                        processingEmbedded = EMBEDDED_DEXPR;
                    }
                    // END NETBEANS MODIFICATIONS
                    lexer.setValue(new Token("#" + c, lexer.getPosition()));
                    return Tokens.tSTRING_DBEG;
                }
                buffer.append('#');
            }

            src.unread(c);

            do {
                // BEGIN NETBEANS MODIFICATIONS
                //if ((c = new StringTerm(func, '\n', '\0').parseStringIntoBuffer(src, buffer)) == RubyYaccLexer.EOF) {                
                StringTerm stringTerm = new StringTerm(func, '\n', '\0');
                stringTerm.processingEmbedded = processingEmbedded;
                if ((c = stringTerm.parseStringIntoBuffer(src, buffer)) == RubyYaccLexer.EOF) {
                // END NETBEANS MODIFICATIONS    
                    throw new SyntaxException(src.getPosition(), "can't find string \"" + eos + "\" anywhere before EOF");
                }
                // BEGIN NETBEANS MODIFICATIONS
                // Completed expansion token
                if (processingEmbedded == EMBEDDED_DVAR || processingEmbedded == EMBEDDED_DEXPR) {
                    processingEmbedded = LOOKING_FOR_EMBEDDED;
                }
                // END NETBEANS MODIFICATIONS    
                if (c != '\n') {
                    lexer.yaccValue = new StrNode(lexer.getPosition(), buffer);
                    return Tokens.tSTRING_CONTENT;
                }
                buffer.append(src.read());
                if ((c = src.read()) == RubyYaccLexer.EOF) {
                    throw new SyntaxException(src.getPosition(), "can't find string \"" + eos + "\" anywhere before EOF");
                }
                // We need to pushback so when whole match looks it did not
                // lose a char during last EOF
                src.unread(c);
            } while (!src.matchString(eos + '\n', indent));
            str = buffer;
        }

        // BEGIN NETBEANS MODIFICATIONS
        // DVARs last only for a single string token so shut if off here.
        if (processingEmbedded == EMBEDDED_DVAR) {
            processingEmbedded = LOOKING_FOR_EMBEDDED;
//        } else if ((processingEmbedded == EMBEDDED_DEXPR) && (str.length() == 0)) {
//            // Unbalanced expression - see #96485
//            processingEmbedded = LOOKING_FOR_EMBEDDED;
        }
        // END NETBEANS MODIFICATIONS

        // BEGIN NETBEANS MODIFICATIONS
        if (lastLine != null)
        // END NETBEANS MODIFICATIONS
            src.unreadMany(lastLine);
        // BEGIN NETBEANS MODIFICATIONS
        // When handling heredocs in syntax highlighting mode, process the end marker
        // separately
        if (lastLine == null) {
            src.unreadMany(eos+"\n");
            //done = true;
        } else {
        // END NETBEANS MODIFICATIONS
        lexer.setStrTerm(new StringTerm(-1, '\0', '\0'));
        // BEGIN NETBEANS MODIFICATIONS
        }
        // END NETBEANS MODIFICATIONS
        lexer.yaccValue = new StrNode(lexer.getPosition(), str);
        return Tokens.tSTRING_CONTENT;
    }

    // BEGIN NETBEANS MODIFICATIONS
    /** 
     * Report whether this string should be substituting things like \n into newlines.
     * E.g. are we dealing with a "" string or a '' string (or their alternate representations)
     */
    public boolean isSubstituting() {
        return (func & RubyYaccLexer.STR_FUNC_EXPAND) != 0;
    }

    /**
     * Record any mutable state from this StrTerm such that it can
     * be set back to this exact state through a call to {@link setMutableState}
     * later on. Necessary for incremental lexing where we may restart
     * lexing parts of a string (since they can be split up due to
     * Ruby embedding like "Evaluated by Ruby: #{foo}".
     */
    public Object getMutableState() {
        return new MutableTermState(processingEmbedded);
    }

    /**
     * Apply the given state object (earlier returned by {@link getMutableState})
     * to this StringTerm to revert state to the earlier snapshot.
     */
    public void setMutableState(Object o) {
        MutableTermState state = (MutableTermState)o;
        if (state != null) {
            this.processingEmbedded = state.processingEmbedded;
        }
    }
    
    public void splitEmbeddedTokens() {
        if (processingEmbedded == IGNORE_EMBEDDED) {
            processingEmbedded = LOOKING_FOR_EMBEDDED;
        }
    }

    private class MutableTermState {
        private MutableTermState(int embeddedCode) {
            this.processingEmbedded = embeddedCode;
        }
        
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final MutableTermState other = (MutableTermState) obj;

            if (this.processingEmbedded != other.processingEmbedded)
                return false;
            return true;
        }

        public int hashCode() {
            int hash = 7;

            hash = 83 * hash + this.processingEmbedded;
            return hash;
        }
        
        public String toString() {
            return "HeredocTermState[" + processingEmbedded + "]";
        }
        
        private int processingEmbedded;
    }
    
    // Equals - primarily for unit testing (incremental lexing tests
    // where we do full-file-lexing and compare state to incremental lexing)
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final HeredocTerm other = (HeredocTerm) obj;

        if (this.eos != other.eos &&
            (this.eos == null || !this.eos.equals(other.eos)))
            return false;
        if (this.func != other.func)
            return false;
        if (this.lastLine != other.lastLine &&
            (this.lastLine == null || !this.lastLine.equals(other.lastLine)))
            return false;
        return true;
    }

    public int hashCode() {
        int hash = 7;

        hash = 83 * hash + (this.eos != null ? this.eos.hashCode()
                                             : 0);
        hash = 83 * hash + this.func;
        hash = 83 * hash + (this.lastLine != null ? this.lastLine.hashCode()
                                                  : 0);
        return hash;
    }

    
    public String toString() {
        return "HeredocTerm[" + func + "," + eos + "," + lastLine + "," + processingEmbedded + "]";
    }

    // END NETBEANS MODIFICATIONS
}
