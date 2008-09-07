/*
 ***** BEGIN LICENSE BLOCK *****
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
 * Copyright (C) 2004-2007 Thomas E Enebo <enebo@acm.org>
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
package org.jruby.nb.lexer.yacc;

import org.jruby.nb.ast.StrNode;
import org.jruby.nb.lexer.yacc.SyntaxException.PID;
import org.jruby.nb.parser.Tokens;
import org.jruby.util.ByteList;


/**
 * A lexing unit for scanning a heredoc element.
 * Example:
 * <pre>
 * foo(<<EOS, bar)
 * This is heredoc country!
 * EOF
 * 
 * Where:
 * EOS = marker
 * ',bar)\n' = lastLine
 * </pre>
 *  
 */
public class HeredocTerm extends StrTerm {
    // Marker delimiting heredoc boundary
    private final ByteList marker;

    // Expand variables, Indentation of final marker
    private final int flags;

    // Portion of line right after beginning marker
    private final ByteList lastLine;
    
    public HeredocTerm(ByteList marker, int func, ByteList lastLine) {
        this.marker = marker;
        this.flags = func;
        this.lastLine = lastLine;
    }
    
    public int parseString(RubyYaccLexer lexer, LexerSource src) throws java.io.IOException {
        boolean indent = (flags & RubyYaccLexer.STR_FUNC_INDENT) != 0;

        if (src.peek(RubyYaccLexer.EOF)) syntaxError(src);

        // BEGIN NETBEANS MODIFICATIONS
        if (lexer.getPreserveSpaces()) {
            boolean done = src.matchMarker(marker, indent, true);
            if (done) {
                lexer.yaccValue = new StrNode(lexer.getPosition(), new ByteList());
                lexer.setStrTerm(new StringTerm(-1, '\0', '\0'));
                src.setIsANewLine(true);
                return Tokens.tSTRING_END;
            }
        }
        // END NETBEANS MODIFICATIONS
        
        // Found end marker for this heredoc
        if (src.lastWasBeginOfLine() && src.matchMarker(marker, indent, true)) {
            // Put back lastLine for any elements past start of heredoc marker
            // BEGIN NETBEANS MODIFICATIONS
            if (lastLine != null)
            // END NETBEANS MODIFICATIONS
              src.unreadMany(lastLine);
            
            lexer.yaccValue = new Token(marker, lexer.getPosition());
            return Tokens.tSTRING_END;
        }

        ByteList str = new ByteList();
        //ISourcePosition position;
        
        if ((flags & RubyYaccLexer.STR_FUNC_EXPAND) == 0) {
            do {
                str.append(src.readLineBytes());
                str.append('\n');
                if (src.peek(RubyYaccLexer.EOF)) syntaxError(src);
                //position = lexer.getPosition();
            } while (!src.matchMarker(marker, indent, true));
        } else {
            int c = src.read();
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
                str.append('#');
            }

            src.unread(c);

            // MRI has extra pointer which makes our code look a little bit
            // more strange in
            // comparison
            do {
                // BEGIN NETBEANS MODIFICATIONS
                //if ((c = new StringTerm(flags, '\0', '\n').parseStringIntoBuffer(lexer, src, str)) == RubyYaccLexer.EOF) {
                StringTerm stringTerm = new StringTerm(flags, '\0', '\n');
                stringTerm.processingEmbedded = processingEmbedded;
                if ((c = stringTerm.parseStringIntoBuffer(lexer, src, str)) == RubyYaccLexer.EOF) {
                // END NETBEANS MODIFICATIONS    
                    syntaxError(src);
                }
                // BEGIN NETBEANS MODIFICATIONS
                // Completed expansion token
                if (processingEmbedded == EMBEDDED_DVAR || processingEmbedded == EMBEDDED_DEXPR) {
                    processingEmbedded = LOOKING_FOR_EMBEDDED;
                }
                // END NETBEANS MODIFICATIONS    
                if (c != '\n') {
                    lexer.yaccValue = new StrNode(lexer.getPosition(), str);
                    return Tokens.tSTRING_CONTENT;
                }
                str.append(src.read());
                
                if (src.peek(RubyYaccLexer.EOF)) syntaxError(src);
                //position = lexer.getPosition();
            } while (!src.matchMarker(marker, indent, true));
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
            src.unreadMany(marker+"\n"); // \r?
            //done = true;
        } else {
        // END NETBEANS MODIFICATIONS
        lexer.setStrTerm(new StringTerm(-1, '\0', '\0'));
        // BEGIN NETBEANS MODIFICATIONS
        }
        // END NETBEANS MODIFICATIONS
        lexer.yaccValue = new StrNode(lexer.getPosition(), str);
        //lexer.yaccValue = new StrNode(position, str);
        return Tokens.tSTRING_CONTENT;
    }
    
    private void syntaxError(LexerSource src) {
        throw new SyntaxException(PID.STRING_MARKER_MISSING, src.getPosition(), "can't find string \"" + marker
                + "\" anywhere before EOF", marker);
    }
    
    // BEGIN NETBEANS MODIFICATIONS
    /** 
     * Report whether this string should be substituting things like \n into newlines.
     * E.g. are we dealing with a "" string or a '' string (or their alternate representations)
     */
    public boolean isSubstituting() {
        return (flags & RubyYaccLexer.STR_FUNC_EXPAND) != 0;
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

        if (this.marker != other.marker &&
            (this.marker == null || !this.marker.equals(other.marker)))
            return false;
        if (this.flags != other.flags)
            return false;
        if (this.lastLine != other.lastLine &&
            (this.lastLine == null || !this.lastLine.equals(other.lastLine)))
            return false;
        return true;
    }

    public int hashCode() {
        int hash = 7;

        hash = 83 * hash + (this.marker != null ? this.marker.hashCode()
                                             : 0);
        hash = 83 * hash + this.flags;
        hash = 83 * hash + (this.lastLine != null ? this.lastLine.hashCode()
                                                  : 0);
        return hash;
    }

    
    public String toString() {
        return "HeredocTerm[" + flags + "," + marker + "," + lastLine + "," + processingEmbedded + "]";
    }

    // END NETBEANS MODIFICATIONS
}
