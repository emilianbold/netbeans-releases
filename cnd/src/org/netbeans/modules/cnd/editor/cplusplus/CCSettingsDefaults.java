/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.cplusplus;


import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.ext.ExtSettingsDefaults;

/** Default settings values for C and C++ */
public class CCSettingsDefaults extends ExtSettingsDefaults {

    public static final Boolean defaultCaretSimpleBracketMatching = Boolean.FALSE;
    public static final Boolean defaultHighlightMatchingBracket = Boolean.TRUE;
    public static final Boolean defaultCCDocAutoPopup = Boolean.FALSE;
    public static final Boolean defaultPairCharactersCompletion = Boolean.TRUE;
    public static final Acceptor defaultCCIdentifierAcceptor = AcceptorFactory.JAVA_IDENTIFIER;
    
    // Formatting
    public static final Boolean defaultCCFormatSpaceBeforeParenthesis = Boolean.FALSE;
    public static final Boolean defaultCCFormatSpaceAfterComma = Boolean.TRUE;
    public static final Boolean defaultCCFormatNewlineBeforeBrace = Boolean.FALSE;
    public static final Boolean defaultCCFormatLeadingSpaceInComment = Boolean.FALSE;
    public static final Boolean defaultCCFormatLeadingStarInComment = Boolean.TRUE;
    public static final Integer defaultCCFormatStatementContinuationIndent = new Integer(8);
    public static final Boolean defaulCCtFormatPreprocessorAtLineStart = Boolean.FALSE;

    // Code Folding
    public static final Boolean defaultCCCodeFoldingEnable = Boolean.TRUE;

    public static final Acceptor defaultIndentHotCharsAcceptor = new Acceptor() {
            public boolean accept(char ch) {
                switch (ch) {
                case '{':
                case '}':
                    return true;
                }

                return false;
            }
        };

    // DO WE NEED IT ?
    public static final String defaultWordMatchStaticWords = 
            "Exception IntrospectionException FileNotFoundException IOException" //NOI18N
          + " ArrayIndexOutOfBoundsException ClassCastException ClassNotFoundException" //NOI18N
          + " CloneNotSupportedException NullPointerException NumberFormatException" //NOI18N
          + " SQLException IllegalAccessException IllegalArgumentException"; //NOI18N


}
