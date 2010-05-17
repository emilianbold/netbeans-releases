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
    
    /**
     * @deprecated Without any replacement.
     */
    public static Map getJavaMacroMap() {
        return Collections.EMPTY_MAP;
    }
}
