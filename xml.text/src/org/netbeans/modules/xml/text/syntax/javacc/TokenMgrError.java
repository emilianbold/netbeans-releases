/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax.javacc;

public class TokenMgrError extends Error
{
    /** Serial Version UID */
    private static final long serialVersionUID = 2660096454861940438L;
    
    /*
     * Ordinals for various reasons why an Error of this type can be thrown.
     */

    /**
     * Lexical error occured.
     */
    static final int LEXICAL_ERROR = 0;

    /**
     * An attempt wass made to create a second instance of a static token manager.
     */
    static final int STATIC_LEXER_ERROR = 1;

    /**
     * Tried to change to an invalid lexical state.
     */
    static final int INVALID_LEXICAL_STATE = 2;

    /**
     * Detected (and bailed out of) an infinite loop in the token manager.
     */
    static final int LOOP_DETECTED = 3;

    /**
     * Indicates the reason why the exception is thrown. It will have
     * one of the above 4 values.
     */
    int errorCode;


    /**
     * Replaces unprintable characters by their espaced (or unicode escaped)
     * equivalents in the given string
     */
    protected static final String addEscapes(String str) {
        StringBuffer retval = new StringBuffer();
        char ch;
        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i))
            {
            case 0 :
                continue;
            case '\b':
                retval.append("\\b"); // NOI18N
                continue;
            case '\t':
                retval.append("\\t"); // NOI18N
                continue;
            case '\n':
                retval.append("\\n"); // NOI18N
                continue;
            case '\f':
                retval.append("\\f"); // NOI18N
                continue;
            case '\r':
                retval.append("\\r"); // NOI18N
                continue;
            case '\"':
                retval.append("\\\""); // NOI18N
                continue;
            case '\'':
                retval.append("\\\'"); // NOI18N
                continue;
            case '\\':
                retval.append("\\\\"); // NOI18N
                continue;
            default:
                if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
                    String s = "0000" + Integer.toString(ch, 16); // NOI18N
                    retval.append("\\u" + s.substring(s.length() - 4, s.length())); // NOI18N
                } else {
                    retval.append(ch);
                }
                continue;
            }
        }
        return retval.toString();
    }

    /**
     * Returns a detailed message for the Error when it is thrown by the
     * token manager to indicate a lexical error.
     * Parameters : 
     *    EOFSeen     : indicates if EOF caused the lexicl error
     *    curLexState : lexical state in which this error occured
     *    errorLine   : line number when the error occured
     *    errorColumn : column number when the error occured
     *    errorAfter  : prefix that was seen before this error occured
     *    curchar     : the offending character
     * Note: You can customize the lexical error message by modifying this method.
     */
    private static final String LexicalError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar) {
        return("Lexical error at line " + // NOI18N
               errorLine + ", column " + // NOI18N
               errorColumn + ".  Encountered: " + // NOI18N
               (EOFSeen ? "<EOF> " : ("\"" + addEscapes(String.valueOf(curChar)) + "\"") + " (" + (int)curChar + "), ") + // NOI18N
               "after : \"" + addEscapes(errorAfter) + "\""); // NOI18N
    }

    /**
     * You can also modify the body of this method to customize your error messages.
     * For example, cases like LOOP_DETECTED and INVALID_LEXICAL_STATE are not
     * of end-users concern, so you can return something like : 
     *
     *     "Internal Error : Please file a bug report .... "
     *
     * from this method for such cases in the release version of your parser.
     */
    public String getMessage() {
        return super.getMessage();
    }

    /*
     * Constructors of various flavors follow.
     */

    public TokenMgrError() {
    }

    public TokenMgrError(String message, int reason) {
        super(message);
        errorCode = reason;
    }

    public TokenMgrError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar, int reason) {
        this(LexicalError(EOFSeen, lexState, errorLine, errorColumn, errorAfter, curChar), reason);
    }
}
