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

import java.util.List;

import org.netbeans.api.gsf.annotations.CheckForNull;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.api.gsf.annotations.Nullable;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;


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

    /**
     * <p>Return a set of token types that are relevant to the lexer for this language.
     * "Relevant" here
     * means that it's a TokenType that will ever be passed in by the scanner to the
     * {@link TokenResult}.</p>
     * <p>
     * Technically, it's a List, not a Set. The list of TokenTypes will be used for
     * example in the color and font chooser for the editor, such that users can customize
     * the appearance of source text. The order of the tokens in the list will be used
     * in the TokenType list in the editor style chooser.
     * </p>
     * <p>
     * If a language returns null or an empty List, <b>all</b> known TokenTypes will be
     * offered to the user. To avoid this, return only tokens that are relevant to this
     * language.
     * </p>
     * <p>
     * You are strongly encouraged to use existing {@link DefaultTokenType} tokens when
     * possible, such that user's existing color customizations will apply.
     * </p>
     * <p>
     * You should also register a code fragment sample in the layer
     * (at <code>OptionsDialog/PreviewExamples/</code><i>mime/type/</i>) and this code
     * sample should try to use as many of the tokens pertaining to the language such
     * that users can see the effect of customizing the various token types.
     * </p>
     * @todo Make this into a Map interface that is called just once instead.
     */
    @CheckForNull
    List<? extends TokenId> getRelevantTokenTypes();
}
