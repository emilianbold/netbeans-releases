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

package org.netbeans.api.gsf;

import org.netbeans.api.gsf.annotations.CheckForNull;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.api.lexer.Language;


/**
 * Lexical information for a given language.
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public interface GsfLanguage {
    /**
     * <p>Return the prefix used for line comments in this language, or null if this language
     * does not have a line comment. As an example, a Java scanner would return <code>//</code>,
     * a Ruby scanner would return <code>#</code>, a Visual Basic scanner would return <code>'</code>, etc.
     * </p>
     */
    @CheckForNull
    String getLineCommentPrefix();
    
    /**
     * <p>Return true iff the given character is considered to be an identifier character. This
     * is used for example when the user double clicks in the editor to select a "word" or identifier
     * by checking to the left and to the right of the caret position and selecting until a character
     * is not considered an identifier char by the scanner.
     * </p>
     * <p>
     * For a language like Java, just return Character.isJavaIdentifierPart(). For something like
     * Ruby, we also want to include "@" and "$" such that double clicking on a global variable
     * for example will include the global prefix "$".
     */
    boolean isIdentifierChar(char c);
    
    /**
     * <p>Return the Lexer Language associated with this scanner
     *</p>
     */
    @NonNull
    Language getLexerLanguage();
}
