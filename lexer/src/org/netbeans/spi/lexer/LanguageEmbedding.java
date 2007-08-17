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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LanguageOperation;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.lib.lexer.LexerUtilsConstants;

/**
 * Description of a particular language embedding including
 * starting and ending skipped regions of a token containing this embedding
 * and a definition of an embedded language hierarchy.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class LanguageEmbedding<T extends TokenId> {
    
    /**
     * Create language embedding that does not join embedded sections.
     *
     * @see #create(Language, int, int, boolean)
     */
    public static <T extends TokenId> LanguageEmbedding<T> create(
    Language<T> language, int startSkipLength, int endSkipLength) {
        return create(language, startSkipLength, endSkipLength, false);
    }

    /**
     * Construct new language embedding for the given parameters
     * or get an existing cached one.
     *
     * @param language non-null language.
     * @param startSkipLength &gt;=0 number of characters in an initial part of the token
     *  for which the language embedding is defined that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     * @param endSkipLength &gt;=0 number of characters at the end of the token
     *  for which the language embedding is defined that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     * @param joinSections whether sections with this embedding should be joined
     *  across the input source or whether they should stay separate.
     *  <br/>
     *  For example for HTML sections embedded in JSP this flag should be true:
     *  <pre>
     *   &lt;!-- HTML comment start
     *       &lt;% System.out.println("Hello"); %&gt;
            still in HTML comment --&lt;
     *  </pre>
     *  <br/>
     *  Only the embedded sections with the same language path can be joined.
     */
    public static <T extends TokenId> LanguageEmbedding<T> create(
    Language<T> language, int startSkipLength, int endSkipLength, boolean joinSections) {
        if (language == null) {
            throw new IllegalArgumentException("language may not be null"); // NOI18N
        }
        if (startSkipLength < 0) {
            throw new IllegalArgumentException("startSkipLength=" + startSkipLength + " < 0");
        }
        if (endSkipLength < 0) {
            throw new IllegalArgumentException("endSkipLength=" + endSkipLength + " < 0");
        }

        LanguageOperation<T> op = LexerApiPackageAccessor.get().languageOperation(language);
        return op.getEmbedding(startSkipLength, endSkipLength, joinSections);
    }
    
    private final Language<T> language;
    
    private final int startSkipLength;
    
    private final int endSkipLength;
    
    private final boolean joinSections;
    
    /**
     * Package-private constructor used by lexer spi package accessor.
     */
    LanguageEmbedding(Language<T> language,
    int startSkipLength, int endSkipLength, boolean joinSections) {
        assert (language != null) : "Embedded language may not be null."; // NOI18N
        assert (startSkipLength >= 0 && endSkipLength >= 0);
        this.language = language;
        this.startSkipLength = startSkipLength;
        this.endSkipLength = endSkipLength;
        this.joinSections = joinSections;
    }
    
    /**
     * Get the embedded language.
     *
     * @return non-null embedded language.
     */
    public Language<T> language() {
        return language;
    }

    /**
     * Get length of the initial part of the token (for which the embedding
     * is being created) that should be skipped
     * so it will be excluded from lexing and no tokens will be created for it.
     *
     * @return &gt;=0 number of characters in an initial part of the token
     *  (for which the language embedding is defined) that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     */
    public int startSkipLength() {
        return startSkipLength;
    }
    
    /**
     * Get length of the ending part of the token (for which the embedding
     * is being created) that should be skipped
     * so it will be excluded from lexing and no tokens will be created for it.
     *
     * @return &gt;=0 number of characters at the end of the token
     *  (for which the language embedding is defined) that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     */
    public int endSkipLength() {
        return endSkipLength;
    }

    /**
     * Whether sections with this embedding should be joined with the other
     * sections with this embedding at the same level.
     *
     * @return joinSections whether sections with this embedding should be joined
     *  across the input source or whether they should stay separate.
     */
    public boolean joinSections() {
        return joinSections;
    }
    
    public String toString() {
        return "language: " + language() + ", skip[" + startSkipLength() // NOI18N
            + ", " + endSkipLength + "]"; // NOI18N
    }
    
}
