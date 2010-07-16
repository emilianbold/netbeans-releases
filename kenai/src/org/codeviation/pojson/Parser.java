/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.codeviation.pojson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.Stack;

/** Good for parsing JSON files folowing RFC 4627. See
 *  http://www.ietf.org/rfc/rfc4627.txt
 *
 * @author Petr Hrebejk
 */
class Parser {
    
    private static final int OBJECT_START = '{';
    private static final int OBJECT_END = '}';
    private static final int ARRAY_START = '[';
    private static final int ARRAY_END = ']';
    private static final int COLON = ':';
    private static final int COMMA = ',';
    private static final int PLUS = '+';
    private static final int MINUS = '-';
    private static final int DOUBLE_QUOTE = '"';            
    private static final int COMMENT_START = '/';
    private static final int LINE_COMMENT = COMMENT_START;
    private static final int BLOCK_COMMENT = '*';        
    
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String NULL = "null";
    
    private static final int TRUE_START = TRUE.charAt(0);
    private static final int FALSE_START = FALSE.charAt(0);
    private static final int NULL_START = NULL.charAt(0);
        
    private Stack<Where> where;
    private Error error;
    private Handler handler;
    private PushbackReader reader;
    
    
    public static void parse( InputStream stream, Handler handler ) throws IOException {
        parse( new InputStreamReader(stream), handler );
    }
    
    public static void parse( Reader reader, Handler handler ) throws IOException {
        Parser jp = new Parser(reader, handler);
        jp.parse(false);
    }
     
    
    public static Iterator<Void> parseArray( InputStream stream, Handler handler ) throws IOException {
        return parseArray( new InputStreamReader(stream), handler );
    }
    
    public static Iterator<Void> parseArray( Reader reader, Handler handler ) throws IOException {
        Parser jp = new Parser(reader, handler);
        return null; //new ParsingIterator(jp);
    }

    private Parser( Reader reader, Handler handler ) {
        this.where = new Stack<Parser.Where>();
        this.where.push(Where.OUT);
        this.handler = handler;
        this.reader = new PushbackReader(reader, 1);
    }
        
    private void parse(boolean incremental) throws IOException {
    
        int cc = 0; // current character
               
        while( ( cc = reader.read() ) != -1 ) {
                                    
            if ( Character.isWhitespace(cc)) {
                continue;
            }
            
            switch( where.peek() ) {
                case OUT:
                    if ( cc == OBJECT_START ) {
                        handler.objectStart();
                        where.push(Where.OBJECT);
                    }
                    else if ( cc == ARRAY_START ) {
                        handler.arrayStart();
                        where.push(Where.ARRAY);                        
                    }
                    else if ( cc == COMMENT_START) {
                        where.push(Where.COMMENT);
                    }
                    else {
                        error = Error.OBJECT_OR_ARRAY_EXPECTED;
                    }
                    break;
                case COMMENT:
                    if (cc == LINE_COMMENT) {
                        handleComment(true);
                        where.pop();
                    }
                    else if (cc == BLOCK_COMMENT) {
                        handleComment(false);
                        where.pop();
                    }
                    else {
                        error = Error.WRONG_COMMENT;
                    }
                    break;
                case OBJECT:
                    if ( cc == DOUBLE_QUOTE ) {
                        String s = handleStringValue(); 
                        if ( s != null) {
                            handler.field(s);
                            where.push(Where.OBJECT_COLON);
                        }
                    }
                    else if ( cc == OBJECT_END ) {
                        handler.objectEnd();
                        where.pop();
                    }
                    else if ( cc == COMMENT_START) {
                        where.push(Where.COMMENT);
                    }
                    else {
                        error = Error.FIELD_NAME_EXPECTED;
                    }
                    break;
                case OBJECT_COLON:
                    if ( cc == COLON ) {
                        where.pop();
                        where.push(Where.OBJECT_VALUE);
                    }
                    else if ( cc == COMMENT_START) {
                        where.push(Where.COMMENT);
                    }
                    else {
                        error = Error.COLON_EXPECTED;
                    }
                    break;
                case OBJECT_VALUE:
                    if ( cc == OBJECT_START ) {
                        handler.objectStart();   
                        where.pop();
                        where.push(Where.OBJECT_COMMA_OR_END);
                        where.push(Where.OBJECT);
                    }
                    else if ( cc == ARRAY_START ) {
                        handler.arrayStart();
                        where.pop();
                        where.push(Where.OBJECT_COMMA_OR_END);
                        where.push(Where.ARRAY);
                    }
                    else if ( cc == COMMENT_START) {
                        where.push(Where.COMMENT);
                    }
                    else { 
                        handleSimpleValue(cc);
                        where.pop();
                        where.push(Where.OBJECT_COMMA_OR_END);
                    }
                    break;
                case OBJECT_COMMA_OR_END:
                    if ( cc == COMMA ) {
                        where.pop();
                       // where.push(Where.OBJECT);
                    }
                    else if ( cc == OBJECT_END ) {
                        where.pop();
                        where.pop();
                        handler.objectEnd();
                    }
                    else if ( cc == COMMENT_START) {
                        where.push(Where.COMMENT);
                    }
                    else {
                        error = Error.COMMA_OR_END_OF_OBJECT_EXPECTED;
                    }
                    break;
                case ARRAY:                                        
                    if ( cc == OBJECT_START ) {
                        handler.objectStart();
                        where.push(Where.ARRAY_COMMA_OR_END);
                        where.push(Where.OBJECT);
                    }
                    else if ( cc == ARRAY_START ) {
                        handler.arrayStart();
                        where.push(Where.ARRAY_COMMA_OR_END);
                        where.push(Where.ARRAY);
                    }                    
                    else if ( cc == ARRAY_END ) {
                        handler.arrayEnd();
                        where.pop();
                    }
                    else if ( cc == COMMENT_START) {
                        where.push(Where.COMMENT);
                    }
                    else { 
                        handleSimpleValue(cc);
                        where.push(Where.ARRAY_COMMA_OR_END);
                    }
                    break;
                case ARRAY_COMMA_OR_END:
                    if ( cc == COMMA ) {
                        where.pop();
                    }
                    else if ( cc == ARRAY_END ) {
                        where.pop();
                        where.pop();
                        handler.arrayEnd();
                    }
                    else if ( cc == COMMENT_START) {
                        where.push(Where.COMMENT);
                    }
                    else {
                        error = Error.COMMA_OR_END_OF_ARRAY_EXPECTED;
                    }
                    break;
            }
            
            if ( error != null ) {
                handler.error(error);
                break;
            }
        }
        
        if( cc == -1 && where.size() != 1) {
            handler.error(Error.UNCLOSED_OBJECT_OR_ARRAY);
        }
    }
    
    private void handleSimpleValue(int cc) throws IOException {
        if ( cc == TRUE_START && finishString(reader, TRUE ) ) {
            handler.bool(true);            
        }
        else if ( cc == FALSE_START && finishString(reader, FALSE))  {                        
            handler.bool(false);            
        }
        else if (cc == NULL_START && finishString(reader, NULL)) {
            handler.nul();                                                
        }
        else if (cc == DOUBLE_QUOTE ) {
            String s = handleStringValue(); 
            if ( s != null) {
                handler.string(s);
            }
        }        
        else if ( cc == PLUS || cc == MINUS || isDecNumber((char)cc) ) {
            reader.unread(cc);
            handleNumberValue();
        } 
        else {
            error = Error.UNKNOWN_VALUE_TYPE;
        }
        
    }

    private void handleNumberValue() throws IOException {
        StringBuilder sb = new StringBuilder();
        int cc = -1;
        boolean isFloatingPoint = false;
        while( ( cc = reader.read() ) != -1 ) {
           char c = (char)cc; 
           
           if ( !isFloatingPoint && ( c == '.' || c == 'e' || c == 'E' ) ) {
               isFloatingPoint = true;
           }
           
           if (isFloatChar(c)) {
               sb.append(c);
           }
           else if (c == COMMA || c == ARRAY_END || c == OBJECT_END || c == COMMENT_START ) {
               reader.unread(cc);
               break;
           }
           else if ( Character.isWhitespace(c)) {
                break;
           }
           else {
               error = Error.INVALID_NUMBER;
               return;
           }
        }
        try {
            if ( isFloatingPoint ) {
                handler.number(Double.parseDouble(sb.toString()));
            }
            else {
                handler.number(Long.parseLong(sb.toString()));
            }
        }
        catch( NumberFormatException ex ) {
            error = Error.INVALID_NUMBER;
        }
    }
    
    
    private String handleStringValue() throws IOException {
        int cc = -1;
        StringBuilder sb = new StringBuilder();
        while( ( cc = reader.read() ) != -1 ) {            
            switch (cc) {
            case DOUBLE_QUOTE:
                return sb.toString();
            case '\\':
                cc = reader.read();
                switch (cc) {
                case '\\':
                    sb.append('\\');
                    break;
                case '"':
                    sb.append('"');
                    break;
                case '/':
                    sb.append('/');
                    break;
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;                
                case 'u':
                    int c = handleUnicodeChar(reader);
                    if ( c == -1 ) {
                        error = Error.INVALID_HEX_ENCODED_CHAR;
                        return null;
                    }
                    sb.append((char)c);
                    break;                
                default:
                    error = Error.INVALID_ESCAPE_CHAR;
                    return null;
                }
                break;
            default:
                if( !isJsonStringChar(cc)) {
                    error = Error.INVALID_CHARACTER_IN_STRING_LITERAL;
                }
                sb.append((char)cc);                 
            }
        }
        
        error = Error.UNCLOSED_STRING_VALUE;
        return null;
    }
    
    private int handleUnicodeChar(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        char chars[] = new char[4];
        
        for(int i = 0; i < 4; i++) {
            int rc = reader.read();
            
            if (rc == -1) {
                return -1;
            }
            
            chars[i] = (char)rc;
            
            if (!isHexaChar(chars[i]) ) {
                return -1;
            }
            
        }
                
        String nr = new String(chars);
        try {
            int r = Integer.parseInt(nr, 16);
            return r;
        }
        catch(NumberFormatException ex) {
            return -1;
        }        
    }
    
    private void handleComment(boolean line) throws IOException {
        int cc = -1;
        StringBuilder sb = new StringBuilder();
        while( ( cc = reader.read() ) != -1 ) {
            if (line && ( cc == '\n' || cc == '\r') ) {
                handler.lineComment(sb.toString());
                return;
            }
            else if (!line && cc == BLOCK_COMMENT) {
                int cc1 = reader.read();
                if ( cc1 == -1 ) {
                    break;
                }
                if ( cc1 == COMMENT_START ) {
                    handler.comment(sb.toString());
                    return;
                }
                else {
                    sb.append((char)cc);
                    sb.append((char)cc1);
                }
            }
            else {
                sb.append((char)cc);
            }
        }
        
        error = Error.UNCLOSED_COMMENT;
    }
    
    
    private static boolean isFloatChar(char c) {
        return isDecNumber(c) || c == '.' || 
               c == PLUS || c == MINUS || 
               c == 'e' || c == 'E';
    }
    
    private static boolean isDecNumber(char c) {
        return ( c >= '0' && c <= '9');
    }
    
    private static boolean isHexaChar(char c) {
        char uc = Character.toUpperCase(c);
        return isDecNumber(c) || ( uc >= 'A' && uc <= 'F');
    }
    
    private static boolean isJsonStringChar(int c) {
        
        return c == 0x20 || c == 0x21 ||
               (c >= 0x23 && c <= 0x5B) ||
               (c >= 0x5D && c <= 0x10FFFF );        
    } 
    
    private static boolean finishString(Reader r, String s) throws IOException {
        
        for( int i = 1; i < s.length(); i++ ) {
            if ( r.read() != s.charAt(i)) {
                return false;
            }
        }
        
        return true;
                
    }
        
    public interface Handler {
        
        public void objectStart();
        
        public void objectEnd();
        
        public void arrayStart();
        
        public void arrayEnd();
        
        public void field(String name);
        
        public void bool(boolean value);
        
        public void string(String value);
        
        public void nul();
        
        public void number(long value);
        
        public void number(double value);
                
        public void error( Error error );
        
        public void comment(String comment);
        
        public void lineComment(String comment);
                
    }
    
    private static enum Where {
        
        
        OUT,            // Waiting for a object or array to start
        
        OBJECT,         // Object started awaiting field name
        OBJECT_COLON,        
        OBJECT_VALUE,
        OBJECT_COMMA_OR_END,
        
        ARRAY,                  // Array before the first item
        ARRAY_COMMA_OR_END,     // Array before neesds "," | "]"
        
        COMMENT,
        
        NAME;
        
    }
    
    public static enum Error {
        OBJECT_OR_ARRAY_EXPECTED,
        UNKNOWN_VALUE_TYPE,
        COMMA_OR_END_OF_ARRAY_EXPECTED,
        COMMA_OR_END_OF_OBJECT_EXPECTED,
        INVALID_ESCAPE_CHAR,
        INVALID_HEX_ENCODED_CHAR,
        INVALID_CHARACTER_IN_STRING_LITERAL,
        UNCLOSED_STRING_VALUE,
        INVALID_NUMBER,
        FIELD_NAME_EXPECTED,
        COLON_EXPECTED,
        WRONG_COMMENT,
        UNCLOSED_COMMENT,
        UNCLOSED_OBJECT_OR_ARRAY;
    }
    
    private static class ParsingIterator implements Iterator<Void> {

        
        
        public boolean hasNext() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Void next() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}
