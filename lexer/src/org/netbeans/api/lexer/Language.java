/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.lexer;

import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Languages are identified by mimeType
 * and they represent a set of token ids.
 * They also provide a method to instnatiate a lexer
 * that recognizes the language.
 * <BR>The subclass must define the {@link #createLexer()} method
 * and assign the language reasonable mimeType and token ids.
 * <P>The token ids are typically defined as public static final constants
 * in the target language class together with their corresponding
 * interger constants.
 * <P>The language classes are usually generated from the underlying
 * xml description.
 * <P>Initially the language objects contains no token ids.
 * The {@link #getDeclaredIds()} helps to find all the static final
 * fields of TokenId type and {@link #setIds(TokenId[])}
 * allows to override the current ids.
 * <P>The target language classes should be immutable singletons.
 * The set of tokens can be retrieved by {@link #ids()}.
 *
 * @see TokenId
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class Language {
    
    private static final Object LOCK = new Object();

    /** Mime type identification of this language */
    private final String mimeType;

    private Set ids;

    private Map idName2id;
    
    private Set categories;
    
    private Map lang2isSubset;
    
    protected Language(String mimeType) {
        if (mimeType == null) {
            throw new NullPointerException("Null name for language "
                + getClass().getName());
        }

        this.mimeType = mimeType;
    }
    
    /** Get the mimeType of this language.
     */
    public final String getMimeType() {
        return mimeType;
    }
    
    /** Create the lexer that recognizes this language.
     * @return created instance of the lexer for this language.
     */
    public abstract Lexer createLexer();

    /** Create the set of tokenIds that belong to this language.
     * The method is called only once during the language instance lifetime.
     */
    protected abstract Set createIds();

    /** Return unmodifiable set of token-ids contained in this language.
     */
    public final Set ids() {
        /* There is no extra synchronization to ensure that only one
         * ids will be created. The clients operate on the contents
         * of the set rather than storing the reference to the set itself.
         */
        if (ids == null) {
            ids = new TokenCategory.TokenIdSet(createIds());
            idName2id = createName2IdMap(ids.iterator()); // dup name check
        }
        
        return ids;
    }
    
    /** Get the tokenId for the given intId. This method
     * should be used by the lexers to quickly translate the intId
     * to target tokenId.
     * @param intId intId to be translated to corresponding tokenId.
     * @return valid tokenId or null if there's no corresponding
     *  tokenId for the given int-id. It's possible because intIds
     *  of the language's tokenIds do not need to be continuous.
     *  If the intId is &lt;0 or higher than the highest
     *  intId of all the tokenIds of this language the method
     *  throws {@link ArrayIndexOutOfBoundsException}.
     * @throws ArrayIndexOutOfBoundsException if the intId is
     *  &lt;0 or higher than the highest
     *  intId of all the tokenIds of this language.
     */
    public final TokenId getId(int intId) {
        return ((TokenCategory.TokenIdSet)ids()).getIndexedIds()[intId];
    }
    
    /** This method is similar to {@link #getId(int)} however it guarantees
     * that it will always return non-null tokenId. Typically for a lexer 
     * just being developed it's possible that there are some integer
     * token ids defined in the generated lexer for which there is
     * no correspondence in the language. The lexer wrapper should
     * always call this method if it expect to find a valid
     * counterpart for given integer id.
     * @param intId integer id to translate.
     * @return always non-null tokenId that corresponds to the given integer id.
     */
    public final TokenId getValidId(int intId) {
        TokenId id = getId(intId);
        if (id == null) {
            throw new IllegalStateException("No valid tokenId for intId=" + intId);
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

    /** Get the token categories of this language.
     * @return set containing all the {@link TokenCategory}
     *  instances created for this language.
     */
    public final Set categories() {
        /* Sync to ensure creation of only one set of categories.
         * It could be patological to have several sets of token category
         * instances from competing threads.
         */
        synchronized (LOCK) {
            if (categories == null) {
                categories = new TokenCategory.CategorySet(ids());
            }
            
            return categories;
        }
    }
    
    /**
     * @return category with the given name or null if it does not exist.
     */
    public final TokenCategory getCategory(String name) {
        return ((TokenCategory.CategorySet)categories).getCategory(name);
    }
    
    /** 
     * Check whether this language is subset of the given language.
     * @param language language to which this one is compared.
     * @return true if this language contains the subset
     *  of tokenIDs (same instances) of the target language
     *  or false otherwise. The target language can possibly contain
     *  more tokenIds (e.g. JDK13 is subset of JDK14 and the JDK14 contains
     *  one more keyword "assert").
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
                isSub = isIdsSubsetOf(language) ? Boolean.TRUE : Boolean.FALSE;
                lang2isSubset.put(language, isSub);
            }

            return isSub.booleanValue();
        }
    }
    
    /** Check whether tokenIDs of this language
     * are subset of tokenIDs of the given language.
     */
    private boolean isIdsSubsetOf(Language language) {
        return language.ids().containsAll(this.ids());
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

    /** The languages are equal only if they are the same instances. */
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    /** The hashCode of the language is the system identity hashCode. */
    public final int hashCode() {
        return super.hashCode();
    }
        
    public String toString() {
        return getMimeType();
    }

    public String idsToString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = ids().iterator(); it.hasNext();) {
            sb.append(((TokenId)it.next()).toStringDetail());
            sb.append("\n");
        }
        
        return sb.toString();
    }
        
}

