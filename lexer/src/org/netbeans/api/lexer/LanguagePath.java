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

package org.netbeans.api.lexer;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Language path describes a complete embedding
 * of the languages starting from the root (top-level) language
 * till the most embedded language.
 * <br/>
 * Language path consists of one root language
 * and zero or more embedded languages.
 * <br/>
 * E.g. for javadoc embedded in java that is embedded in jsp
 * then the language path <code>lp</code> would return the following:<pre>
 *  lp.size() == 3
 *  lp.language(0) == JspTokenId.language()
 *  lp.language(1) == JavaTokenId.language()
 *  lp.language(2) == JavadocTokenId.language()
 * </pre>
 *
 * <p>
 * The two language paths for the same languages in the same order
 * represent a single object. Therefore language paths can be compared
 * by using == operator.
 * </p>
 *
 * <p>
 * <b>Lifetime:</b>
 * Once a particular language path is created
 * it is held by a soft reference from its "parent" language path.
 * </p>
 *
 * <p>
 * This class may safely be used by multiple threads.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.0
 */
public final class LanguagePath {
    
    /**
     * Empty language path for internal use and referencing the top-level language paths.
     */
    private static final LanguagePath EMPTY = new LanguagePath();

    /**
     * Get language path that contains a single language.
     *
     * @param language non-null language.
     * @return non-null language path.
     */
    public static LanguagePath get(Language<? extends TokenId> language) {
        return get(null, language);
    }
    
    /**
     * Get language path corresponding to the language embedded in the given context
     * language path.
     * <br/>
     * This method has the same effect like using {@link #embedded(Language)}.
     * <br/>
     * For example for java scriplet embedded in jsp the prefix would 
     * be a language-path for jsp language and language would be java language.
     * <br/>
     * By using this method language paths with arbitrary depth can be created.
     *
     *
     * @param prefix prefix language path determining the context in which
     *   the language is embedded or null if there is no prefix.
     * @param language non-null language.
     * @return non-null language path.
     */
    public static LanguagePath get(LanguagePath prefix, Language<? extends TokenId> language) {
        if (prefix == null)
            prefix = EMPTY;
        return prefix.embedded(language);
    }
    
    /**
     * Array of component language paths for this language path.
     * <br>
     * The last member of the array is <code>this</code>.
     */
    private final Language<? extends TokenId>[] languages;
    
    /**
     * Mapping of embedded language (or suffix language path) to a weak reference to LanguagePath.
     */
    private Map<Object, Reference<LanguagePath>> language2path;
    
    /**
     * Cached and interned mime-path string.
     */
    private String mimePath;
    
    /**
     * Language path with inner language removed. Null for single-language paths.
     */
    private LanguagePath parent;
    
    
    private LanguagePath(LanguagePath prefix, Language<? extends TokenId> language) {
        int prefixSize = prefix.size();
        this.languages = allocateLanguageArray(prefixSize + 1);
        System.arraycopy(prefix.languages, 0, this.languages, 0, prefixSize);
        this.languages[prefixSize] = language;
        this.parent = (prefix == EMPTY) ? null : prefix;
    }
    
    /** Build EMPTY LanguagePath */
    private LanguagePath() {
        this.languages = allocateLanguageArray(0);
    }
    
    /**
     * Get total number of languages in this language path.
     *
     * @return >=1 number of languages contained in this language path.
     */
    public int size() {
        return languages.length;
    }
    
    /**
     * Get language of this language path at the given index.
     * <br>
     * Index zero corresponds to the root language.
     *
     * @param index >=0 && < {@link #size()}.
     * @return non-null language at the given index.
     * @throws IndexOutOfBoundsException in case the index is not within
     *   required bounds.
     */
    public Language<? extends TokenId> language(int index) {
        return languages[index];
    }
    
    /**
     * Get embedded path of this language path.
     * <br/>
     * This method has the same effect like using {@link #get(LanguagePath,Language)}
     * but this one is usually preferred as it supports more readable code.
     * <br/>
     * For example for java scriplet embedded in jsp the prefix would 
     * be a language-path for jsp language and language would be java language.
     * <br/>
     * By using this method language paths with arbitrary depth can be created.
     *
     * @param language non-null language.
     * @return non-null language path.
     */
    public LanguagePath embedded(Language<? extends TokenId> language) {
        if (language == null) {
            throw new IllegalArgumentException("language cannot be null");
        }
        // Attempt to retrieve from the cache first
        synchronized (languages) {
            initLanguage2path();
            Reference<LanguagePath> lpRef = language2path.get(language);
            LanguagePath lp;
            if (lpRef == null || (lp = lpRef.get()) == null) {
                // Construct the LanguagePath
                lp = new LanguagePath(this, language);
                language2path.put(language, new SoftReference<LanguagePath>(lp));
            }
        
            return lp;
        }
    }

    /**
     * Get language path corresponding to the suffix language path embedded
     * in this path.
     * 
     * @param suffix non-null suffix to be added to this path.
     * @return non-null language path consisting of this path with the
     *  suffix added to the end.
     */
    public LanguagePath embedded(LanguagePath suffix) {
        if (suffix == null) {
            throw new IllegalArgumentException("suffix cannot be null");
        }
        // Attempt to retrieve from the cache first
        synchronized (languages) {
            initLanguage2path();
            Reference<LanguagePath> lpRef = language2path.get(suffix);
            LanguagePath lp;
            if (lpRef == null || (lp = lpRef.get()) == null) {
                // Construct the LanguagePath
                lp = this;
                for (int i = 0; i < suffix.size(); i++) {
                    lp = lp.embedded(suffix.language(i));
                }
                language2path.put(suffix, new SoftReference<LanguagePath>(lp));
            }
        
            return lp;
        }
    }

    /**
     * Returns language path consisting of <code>&lt;0, size() - 1&gt;</code>
     * languages (i.e. the inner language is cut out).
     * <code>
     * If {@link #size()} == 1 then <code>null</code> is returned.
     */
    public LanguagePath parent() {
        return parent;
    }

    /**
     * Return the top-level language of this language path.
     * <br/>
     * It's equivalent to <code>language(0)</code>.
     *
     * @see #language(int)
     */
    public Language<? extends TokenId> topLanguage() {
        return language(0);
    }
    
    /**
     * Return the most inner language of this path.
     * <br/>
     * It's equivalent to <code>language(size() - 1)</code>.
     *
     * @see #language(int)
     */
    public Language<? extends TokenId> innerLanguage() {
        return language(size() - 1);
    }
    
    /**
     * Check whether this language path ends with the given language path.
     * <br/>
     * This may be useful for checking whether a given input contains certain language
     * (or language path) that may possibly be embedded somewhere in the input.
     *
     * @param languagePath non-null language path to be checked.
     * @return true if this language path contains the given language path
     *  at its end (applies for <code>this</code> as well).
     */
    public boolean endsWith(LanguagePath languagePath) {
        if (languagePath == this || languagePath == EMPTY)
            return true;
        int lpSize = languagePath.size();
        if (lpSize <= size()) {
            for (int i = 1; i <= lpSize; i++) {
                if (language(size() - i) != languagePath.language(lpSize - i))
                    return false;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Gets the path starting at the given index and ending after
     * the last language contained in this path.
     *
     * @see #subPath(int, int)
     */
    public LanguagePath subPath(int startIndex) {
        return subPath(startIndex, size());
    }

    /**
     * Gets the path starting at the given index and ending after
     * the last language contained in this path.
     *
     * @param startIndex >=0 starting index of the requested path in this path.
     * @param endIndex >startIndex index after the last item
     *  of the requested path.
     * @return non-null language path containing items between startIndex and endIndex.
     */
    public LanguagePath subPath(int startIndex, int endIndex) {
        if (startIndex < 0) {
	    throw new IndexOutOfBoundsException("startIndex=" + startIndex + " < 0"); // NOI18N
	}
	if (endIndex > size()) {
	    throw new IndexOutOfBoundsException("endIndex=" + endIndex + " > size()=" + size());
	}
	if (startIndex >= endIndex) {
	    throw new IndexOutOfBoundsException("startIndex=" + startIndex + " >= endIndex=" + endIndex);
	}
	if (startIndex == 0 && endIndex == size()) {
            return this;
        }
        LanguagePath lp = LanguagePath.get(language(startIndex++));
        while (startIndex < endIndex) {
            lp = LanguagePath.get(lp, language(startIndex++));
        }
        return lp;
    }

    /**
     * Gets the mime path equivalent of this language path. The mime path is
     * a concatenation of mime types of all the languages in this language path.
     * The mime types are separated by the '/' character.
     *
     * <p>
     * For example the language path of the java language embedded in the
     * JSP language will return 'text/x-jsp/text/x-java' when this method is called.
     * </p>
     *
     * <p>
     * The returned string path can be used in MimeLookup's operation
     * to obtain a corresponding MimePath object by using
     * <code>MimePath.parse(returned-mime-path-string)</code>.
     * </p>
     *
     * @return The mime path string.
     * @see org.netbeans.spi.lexer.LanguageHierarchy#mimeType()
     */
    public String mimePath() {
        synchronized (languages) {
            if (mimePath == null) {
                StringBuilder sb = new StringBuilder(15 * languages.length);
                for (Language<? extends TokenId> language : languages) {
                    if (sb.length() > 0) {
                        sb.append('/');
                    }
                    sb.append(language.mimeType());
                }
                // Intern the mimePath for faster operation of MimePath.parse()
                mimePath = sb.toString().intern();
            }
            return mimePath;
        }
    }
    
    private void initLanguage2path() {
        if (language2path == null) {
            language2path = new WeakHashMap<Object,Reference<LanguagePath>>();
        }
    }

    
    private Language<? extends TokenId>[] allocateLanguageArray(int length) {
        return (Language<? extends TokenId>[])(new Language[length]);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LanguagePath: size=");
        sb.append(size());
        sb.append('\n');
        for (int i = 0; i < size(); i++) {
            sb.append('[').append(i).append("]: "); // NOI18N
            sb.append(language(i)).append('\n');
        }
        return sb.toString();
    }
    
}