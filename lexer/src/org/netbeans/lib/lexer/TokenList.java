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

package org.netbeans.lib.lexer;

import java.util.Set;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Browsable list of tokens.
 * <br>
 * {@link org.netbeans.api.lexer.TokenSequence} delegates
 * all its operation to this class so any service provider
 * delivering this class will be able to produce token sequences.
 *
 * There are various implementations of the token list:
 * <ul>
 *   <li>BatchTokenList</li> - predecessor of batch token lists
 *   <li>TextTokenList</li> - token list over immutable char sequence
 *   <li>CopyTextTokenList</li> - token list over text input
 *     that needs to be copied. Characters that belong to tokens
 *     skipped due to skipTokenIds do not need to be copied.
 *   <li>SkimTokenList</li> - filter over CopyTextTokenList
 *     to store the token characters in multiple arrays
 *     and to correctly compute the tokens' starting offsets.
 *   <li>IncTokenList</li> - token list for mutable-input environment.
 *   <li>EmbeddedTokenList</li> - token list for a single language embedding
 *     suitable for both batch and incremental environments.
 * </ul>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenList<T extends TokenId> {
    
    /**
     * Language path of this token list.
     */
    LanguagePath languagePath();
    
    /**
     * Get token or {@link EmbeddingContainer} at given index in this list.
     * <br/>
     * The method's implementation may need to be synchronized as multiple
     * threads can access it at the same time.
     * <br/>
     * The requested index value may be arbitrarily high
     * (e.g. when TokenSequence.move(index) is used for too high value).
     *
     * @param &gt;=0 index of the token in this list.
     * @return valid token or null if the index is too high.
     */
    Object tokenOrEmbeddingContainer(int index);

    /**
     * Replace flyweight token at the given index with its non-flyweight copy.
     * <br/>
     * This may be requested by <code>TokenSequence.offsetToken()</code>.
     *
     * @param index &gt;=0 index of the flyweight token in this list.
     * @param flyToken non-null flyweight token. 
     * @param offset >=0 absolute offset where the flyweight token resides.
     * @return non-flyweight token instance.
     */
    AbstractToken<T> replaceFlyToken(int index, AbstractToken<T> flyToken, int offset);
    
    /**
     * Wrap the token by a branch token list due to language embedding
     * that exists for the token.
     *
     * @param index existing index in this token list at which the token
     *  should be wrapped with the embedding info.
     * @param embeddingContainer embedding info that should wrap the token.
     */
    void wrapToken(int index, EmbeddingContainer<T> embeddingContainer);
    
    /**
     * Get absolute offset of the token at the given index in the token list.
     * <br>
     * This method can only be called if the token at the given index
     * was already fetched by {@link tokenOrEmbeddingContainer(int)}.
     * <br/>
     * For branch token lists this method is only expected to be called
     * after {@link #updateStartOffsetShift()} was called so it does not perform
     * any checking whether the start offset of the token list is up-to-date.
     */
    int tokenOffset(int index);
    
    /**
     * Get total count of tokens in the list.
     * <br/>
     * For token lists that create the tokens lazily
     * this will lead to lexing till the end of the input.
     */
    int tokenCount();

    /**
     * Return present number of tokens in the token list but do not create
     * any new tokens (because of possible lazy token creation).
     * <br/>
     * This is necessary e.g. for <code>TokenSequence.move()</code>
     * that needs a binary search for fast positioning 
     * but using {@link #tokenCount()} would lead to unnecessary creation
     * of all tokens.
     */
    int tokenCountCurrent();

    /**
     * Get number of modifications which mutated this token list.
     * <br>
     * Token sequence remembers this number when it gets constructed
     * and checks this number when it moves between tokens
     * and if there is an extra modification performed it throws
     * <code>IllegalStateException</code>.
     *
     * <p>
     * This is also used to check whether this token list corresponds to mutable input
     * or not because unmodifiable lists return -1 from this method.
     *
     * <p>
     * For branch token lists the {@link #updateStartOffsetShift()} ensures
     * that the value returned by this method is most up-to-date
     * (equals to the root list's one).
     *
     * @return number of modifications performed to the list.
     *  <br/>
     *  Returns -1 if this list is constructed for immutable input and cannot be mutated. 
     */
    int modCount();
    
    /**
     * Get absolute offset of the child token with the given raw offset
     * in the underlying input.
     *
     * @param rawOffset raw offset of the child token.
     * @return absolute offset in the input.
     */
    int childTokenOffset(int rawOffset);
    
    /**
     * Get character of a token from the character sequence represented
     * by this support.
     *
     * @param rawOffset raw offset of the child token.
     *  The given offset value may need to be preprocessed before using (it depends
     *  on a nature of the token list).
     * @param index index inside the token's text that should be returned.
     *  This value cannot be simply added to the previous parameter
     *  for mutable token lists as the value could errorneously point
     *  into a middle of the offset gap then.
     * @return appropriate character that the token has requested.
     */
    char childTokenCharAt(int rawOffset, int index);

    /**
     * Get the root token list of the token list hierarchy.
     */
    TokenList<? extends TokenId> root();
    
    /**
     * Get token hierarchy operation for this token list or null
     * if this token list does not have any token hierarchy.
     */
    TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation();
    
    /**
     * Extra attributes related to the input being lexed.
     */
    InputAttributes inputAttributes();

    /**
     * Get lookahead information for the token at the existing token index.
     * <br/>
     * Lookahead is number of characters that the lexer has read
     * past the end of the given token in order to recognize it in the text.
     * <br>
     * This information allows the lexer to know whether modifications
     * past the end of the token can affect its validity.
     *
     * <p>
     * In general only mutable token lists benefit from this information
     * but non-mutable token lists may store the information as well for testing
     * purposes.
     * </p>
     *
     * @param index index of the existing token.
     * @return &gt;=0 number of characters that the lexer has read
     *  in order to recognize this token. Return zero if this token list
     *  does not maintain lookaheads.
     */
    int lookahead(int index);
    
    /**
     * Get state information for the token at the existing token index.
     * <br/>
     * It is an object defining lexer's state after recognition
     * of the given token.
     * <br/>
     * This information allows to restart the lexer at the end of the given token.
     *
     * <p>
     * In general only mutable token lists benefit from this information
     * but non-mutable token lists may store the information as well for testing
     * purposes.
     * </p>
     *
     * @param index index of the existing token.
     * @return lexer's state after recognition of this token
     *  or null for default state. Return null if this token list
     *  does not maintain states.
     */
    Object state(int index);
    
    /**
     * Returns true if the underlying token list does not contain offset ranges
     * that would not be covered by tokens.
     * <br/>
     * This could happen if a batch token list would use token id filter.
     * <br/>
     * If the token list is continuous the TokenSequence
     * can compute token offsets more efficiently.
     */
    boolean isContinuous();

    /**
     * Get set of token ids to be skipped during token creation.
     */
    Set<T> skipTokenIds();

}
