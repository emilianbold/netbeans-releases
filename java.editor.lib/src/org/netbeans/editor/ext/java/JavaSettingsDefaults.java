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

package org.netbeans.editor.ext.java;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSettingsDefaults;

/**
* Default settings values for Java.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaSettingsDefaults extends ExtSettingsDefaults {

    public static final Boolean defaultCaretSimpleMatchBrace = Boolean.FALSE;
    public static final Boolean defaultHighlightMatchingBracket = Boolean.TRUE;

    public static final Acceptor defaultIdentifierAcceptor = AcceptorFactory.JAVA_IDENTIFIER;
    public static final Acceptor defaultAbbrevResetAcceptor = AcceptorFactory.NON_JAVA_IDENTIFIER;
    public static final Boolean defaultWordMatchMatchCase = Boolean.TRUE;

    // Formatting
    public static final Boolean defaultJavaFormatSpaceBeforeParenthesis = Boolean.FALSE;
    public static final Boolean defaultJavaFormatSpaceAfterComma = Boolean.TRUE;
    public static final Boolean defaultJavaFormatNewlineBeforeBrace = Boolean.FALSE;
    public static final Boolean defaultJavaFormatLeadingSpaceInComment = Boolean.FALSE;
    public static final Boolean defaultJavaFormatLeadingStarInComment = Boolean.TRUE;
    public static final Integer defaultJavaFormatStatementContinuationIndent = new Integer(8);

    public static final Boolean defaultPairCharactersCompletion = Boolean.TRUE;

    /** @deprecated */
    public static final Boolean defaultFormatSpaceBeforeParenthesis = defaultJavaFormatSpaceBeforeParenthesis;
    /** @deprecated */
    public static final Boolean defaultFormatSpaceAfterComma = defaultJavaFormatSpaceAfterComma;
    /** @deprecated */
    public static final Boolean defaultFormatNewlineBeforeBrace = defaultJavaFormatNewlineBeforeBrace;
    /** @deprecated */
    public static final Boolean defaultFormatLeadingSpaceInComment = defaultJavaFormatLeadingSpaceInComment;
    
    public static final Boolean defaultCodeFoldingEnable = Boolean.TRUE;
    public static final Boolean defaultCodeFoldingCollapseMethod = Boolean.FALSE;
    public static final Boolean defaultCodeFoldingCollapseInnerClass = Boolean.FALSE;
    public static final Boolean defaultCodeFoldingCollapseImport = Boolean.FALSE;
    public static final Boolean defaultCodeFoldingCollapseJavadoc = Boolean.FALSE;
    public static final Boolean defaultCodeFoldingCollapseInitialComment = Boolean.FALSE;

    public static final Boolean defaultGotoClassCaseSensitive = Boolean.FALSE;
    public static final Boolean defaultGotoClassShowInnerClasses = Boolean.FALSE;
    public static final Boolean defaultGotoClassShowLibraryClasses = Boolean.TRUE;


    public static final Acceptor defaultIndentHotCharsAcceptor
        = new Acceptor() {
            public boolean accept(char ch) {
                switch (ch) {
                    case '{':
                    case '}':
                        return true;
                }

                return false;
            }
        };


    public static final String defaultWordMatchStaticWords
    = "Exception IntrospectionException FileNotFoundException IOException" // NOI18N
      + " ArrayIndexOutOfBoundsException ClassCastException ClassNotFoundException" // NOI18N
      + " CloneNotSupportedException NullPointerException NumberFormatException" // NOI18N
      + " SQLException IllegalAccessException IllegalArgumentException"; // NOI18N

    /**
     * @deprecated Use Editor Settings API instead.
     */
    public static Map getJavaAbbrevMap() {
        return Collections.EMPTY_MAP;
    }

    /**
     * @deprecated Use Editor Settings API instead.
     */
    public static MultiKeyBinding[] getJavaKeyBindings() {
        return new MultiKeyBinding[0];
    }
    
    public static Map getJavaMacroMap() {
        Map javaMacroMap = new HashMap();
        javaMacroMap.put( "debug-var", "select-identifier copy-to-clipboard " + // NOI18N
                "caret-up caret-end-line insert-break \"System.err.println(\\\"\"" + 
                "paste-from-clipboard \" = \\\" + \" paste-from-clipboard \" );" ); // NOI18N
        
        return javaMacroMap;
    }
}
