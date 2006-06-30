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

package org.netbeans.api.lexer;

import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Language represents an unmodifiable set of {@link TokenId}s that can be retrieved by {@link #getIds()}.
 * <BR>Subclasses must implement {@link #createIds()} to define tokenIds that
 * belong to the language and {@link #createLexer()} to provide a lexer that
 * recognizes tokens of the language.
 *
 * <P>TokenIds are typically defined as public static final constants
 * in the target language class together with their related
 * interger constants.
 *
 * <P>Target language classes should be immutable singletons
 * defining <CODE>MyLanguage.get()</CODE> static method
 * to obtain the singleton instance.
 *
 * <P>Language classes can be generated as described
 * in <A href="http://lexer.netbeans.org/doc/language.html">language.html</A>.
 *
 * @see TokenId
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class Language {
    
    private static final Object LOCK = new Object();

    private Set ids;

    private Map idName2id;
    
    private Set categories;
    
    private Map lang2isSubset;
    
    protected Language() {
    }
    
    /**
     * Create lexer that recognizes this language.
     * @return instance of the lexer recognizing this language.
     */
    public abstract Lexer createLexer();

    /**
     * Create set of tokenIds that belong to this language.
     *
     * <P>This method can be called multiple times for a single language instance
     * due to intentional lack of synchronization in the <CODE>getIds()</CODE>.
     * <BR>Possible creation of several instances of <CODE>Set</CODE>s
     * (made by several competing threads)
     * should not make any harm assuming they will all contain the same references
     * to tokenIds.
     * <BR>It should be fine to compose the set
     * by non-synchronized collecting of references
     * to <CODE>static final</CODE> fields of tokenIds
     * in the target language class because
     * AFAIK initialization of static final fields of a class should
     * be done synchronously by JVM before making the whole class public
     * to threads and there should be no DCL or other locking issues.
     */
    protected abstract Set createIds();

    /**
     * Get unmodifiable set of tokenIds contained in this language.
     * @return unmodifiable set of tokenIds contained in this language.
     */
    public final Set getIds() {
        /* There is no extra synchronization to ensure that only one
         * set of ids will be created. The clients should operate on the contents
         * of the set rather than storing the reference to the set itself.
         */
        if (ids == null) {
            ids = new TokenCategory.TokenIdSet(createIds());
            idName2id = createName2IdMap(ids.iterator()); // dup name check
        }
        
        return ids;
    }
    
    /**
     * Get tokenId for the given intId. This method
     * can be used by lexers to quickly translate intId
     * to tokenId.
     * @param intId intId to be translated to corresponding tokenId.
     * @return valid tokenId or null if there's no corresponding
     *  tokenId for the given int-id. It's possible because intIds
     *  of the language's tokenIds do not need to be continuous.
     *  If the intId is &lt;0 or higher than the highest
     *  intId of all the tokenIds of this language the method
     *  throws {@link IndexOutOfBoundsException}.
     * @throws IndexOutOfBoundsException if the intId is
     *  &lt;0 or higher than {@link #getMaxIntId()}.
     */
    public final TokenId getId(int intId) {
        return ((TokenCategory.TokenIdSet)getIds()).getIndexedIds()[intId];
    }
    
    /**
     * This method is similar to {@link #getId(int)} however it guarantees
     * that it will always return non-null tokenId. Typically for a lexer 
     * just being developed it's possible that there are some integer
     * token ids defined in the generated lexer for which there is
     * no correspondence in the language. The lexer wrapper should
     * always call this method if it expect to find a valid
     * counterpart for given integer id.
     * @param intId integer id to translate.
     * @return always non-null tokenId that corresponds to the given integer id.
     * @throws IndexOutOfBoundsException if the intId is
     *  &lt;0 or higher than {@link #getMaxIntId()}.
     */
    public final TokenId getValidId(int intId) {
        TokenId id = getId(intId);
        if (id == null) {
            throw new IllegalStateException("No valid tokenId for intId=" + intId
                + " in language " + this);
        }
        return id;
    }
    
    /** Find the tokenId from its name.
     * @param name name of the tokenId to find.
     * @return tokenId with the requested name or null if it does not exist.
     */
    public final TokenId getId(String name) {
        return (TokenId)idName2id.get(name);
    }
    
    /**
     * Get maximum integer id of all the token ids that this language contains.
     * @return maximum integer id of all the token ids that this language contains.
     */
    public final int getMaxIntId() {
        return ((TokenCategory.TokenIdSet)getIds()).getIndexedIds().length - 1;
    }

    /** Get token categories of this language.
     * @return unmodifiable set containing all {@link TokenCategory}
     *  instances created for this language.
     */
    public final Set getCategories() {
        /* Sync to ensure creation of only one set of categories.
         * It could be patological to have several sets of token category
         * instances from competing threads.
         */
        synchronized (LOCK) {
            if (categories == null) {
                categories = new TokenCategory.CategorySet(getIds(), getMaxIntId());
            }
            
            return categories;
        }
    }
    
    /**
     * Get category with given name.
     * @return category with the given name or null if it does not exist.
     */
    public final TokenCategory getCategory(String name) {
        return ((TokenCategory.CategorySet)getCategories()).getCategory(name);
    }
    
    /** 
     * Check whether this language is subset of the given language.
     * @param language language to which this language is being compared.
     * @return true if tokenIds of this language are subset of the tokenIds
     *  of the compared language. False if this language contains
     *  one or more tokenIds not contained in compared language.
     *  <BR><I>Example:</I><pre>
     *    Java13Language.get().isSubsetOf(Java14Language.get())
     *  </pre>is true assuming that Java14Language contains "assert" keyword
     *  tokenId in addition to Java13Language's tokenIds.
     */
    public final boolean isSubsetOf(Language language) {
        if (language == this) {
            return true;
        }
        
        synchronized (LOCK) {
            if (lang2isSubset == null) {
                lang2isSubset = new WeakHashMap(2);
            }
            
            Boolean isSub = (Boolean)lang2isSubset.get(language);
            if (isSub == null) {
                isSub = isSubsetOfImpl(language) ? Boolean.TRUE : Boolean.FALSE;
                lang2isSubset.put(language, isSub);
            }

            return isSub.booleanValue();
        }
    }
    
    /**
     * Check whether tokenIDs of this language
     * are subset of tokenIDs of the given language.
     * @param language language to which this language is being compared
     */
    private boolean isSubsetOfImpl(Language language) {
        return language.getIds().containsAll(this.getIds());
    }
    
    private static Map createName2IdMap(Iterator idsIterator) {
        Map ret = new HashMap(2);
        while (idsIterator.hasNext()) {
            TokenId id = (TokenId)idsIterator.next();
            Object removed = ret.put(id.getName(), id);
            if (removed != null) { // tokenIds with same name
                throw new IllegalStateException(
                    "Duplicate name tokenIds: " + id.getName());
            }
        }
        return ret;
    }

    /** The languages are equal only if they are the same objects. */
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    /** The hashCode of the language is the identity hashCode. */
    public final int hashCode() {
        return super.hashCode();
    }
        
    /**
     * Dump list of tokenIds for this language into string.
     * @return list of tokenIds of this language as <CODE>java.lang.String</CODE>.
     */
    public String idsToString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = getIds().iterator(); it.hasNext();) {
            sb.append(((TokenId)it.next()).toStringDetail());
            sb.append("\n");
        }
        
        return sb.toString();
    }
        
}

