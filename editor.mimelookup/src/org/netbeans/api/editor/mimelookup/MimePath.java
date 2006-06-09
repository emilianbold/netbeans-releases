/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.editor.mimelookup;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Mime path is in fact a string path consisting of a root mime-type
 * and zero or more embedded mime-types.
 * <br/>
 * For example "text/x-jsp/text/x-java" represents java scriplet
 * embedded in jsp root document. As the mime-type specification
 * requires that a mime-type should contain one and only one '/'
 * character there should be no ambiguity introduced.
 *
 * <p>
 * <b>Identity:</b> Two mime path instances having the same string path
 * are the same object. The mim path is suitable for usage in maps.
 *
 * <p>
 * <b>Lifetime:</b>
 * Once a particular mime-path is obtained (for the particular mime-type)
 * it is held by a soft reference from the <code>MimePath.EMPTY</code>.
 * <br/>
 * Mime-paths for embedded mime-types are softly referenced from enclosing
 * mime-paths.
 * <br/>
 * The mime path should be hardly referenced
 * by an object of the corresponding mime-type (or mime-path).
 * For example java document should hold the mime-path
 * of "text/x-java" during its whole lifetime.
 * <br/>
 * Settings providers should hold the mime paths
 * weakly (e.g. in a WeakHashMap) to allow for a reasonable settings caching.
 * <br/>
 * Note: Weak hashmaps hold their values strongly so if the values of the weak
 * hashmap would reference the mime-path then the values should be softly held
 * in the particular weak hashmap.
 *
 *  @author Miloslav Metelka
 */
public final class MimePath {
    
    /**
     * Empty mime path containing "" path and has zero size.
     * <br>
     * Useful for global settings being base for all the mime-types and mime-paths.
     */
    public static final MimePath EMPTY = new MimePath();

    /** Internal lock to manage the cache maps. */
    private static final Object LOCK = new Object();

    /**
     * Get root mime-path for the given mime-type.
     * <br>
     * This method delegates to {@link #get(MimePath, String)}.
     *
     * @param mimeType non-null and non-empty root mime-type e.g. "text/x-java".
     * @return non-null mime path.
     */
    public static MimePath get(String mimeType) {
        if ("".equals(mimeType)){
            return EMPTY;
        }
        return get(EMPTY, mimeType);
    }
    
    /**
     * Get mime-path corresponding to the mime-type used in the given context
     * mime-path.
     * <br>
     * For example for java scriplet embedded in jsp the prefix would 
     * be a mime-path for "text/x-jsp" and mimeType would be "text/x-java".
     *
     * @param prefix non-null prefix mime-path determining the context in which
     *   the mime-type is used.
     *   <br>
     *   It can be {@link #EMPTY} in case of constructing the root mime-path.
     * @param mimeType non-null and non-empty mime-type string representation,
     *   e.g. "text/x-java".
     * @return non-null mime path.
     */
    public static MimePath get(MimePath prefix, String mimeType) {
        return prefix.getEmbedded(mimeType);
    }
    
    /**
     * Parse the given mime-path string
     * e.g. "text/x-jsp/text/x-java" and get the corresponding mime-path.
     *
     * @param path string mime-path representation with an arbitrary number
     *  of mime-type components.
     *  <br>
     *  It must contain an odd number of slashes.
     *  <br>
     *  It may be an empty string "" in which case the {@link #EMPTY} is returned.
     *
     * @return non-null mime-path corresponding to the given string path.
     */
    public static MimePath parse(String path) {
        return parseImpl(path);
    }
    
    /**
     * Array of component mime paths for this mime path.
     * <br>
     * The last member of the array is <code>this</code>.
     */
    private final MimePath[] mimePaths;
    
    /**
     * Complete string path of this mimePath.
     */
    private final String path;

    /**
     * Mime type string represented by this mime path component.
     */
    private final String mimeType;
    
    /**
     * Mapping of embedded mimeType to a weak reference to mimePath.
     */
    private Map mimeType2mimePathRef;
    
    
    private MimePath(MimePath prefix, String mimeType) {
        int prefixSize = prefix.size();
        this.mimePaths = new MimePath[prefixSize + 1];
        System.arraycopy(prefix.mimePaths, 0, this.mimePaths, 0, prefixSize);
        this.mimePaths[prefixSize] = this;
        String prefixPath = prefix.path;
        this.path = (prefixPath != null && prefixPath.length() > 0 ) ? 
            (prefixPath + '/' + mimeType).intern() :
            mimeType.intern();
        this.mimeType = mimeType;
    }
    
    /** Build EMPTY mimePath */
    private MimePath() {
        this.mimePaths = new MimePath[0];
        this.path = "";
        this.mimeType = "";
    }
    
    /**
     * Get string path represented by this mime-path.
     * <br/>
     * For example <code>"text/x-jsp/text/x-java"</code>.
     *
     * @return non-null string path.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Get total number of mime-types in the mime-path.
     * <br>
     * {@link #EMPTY} mime-path has zero size.
     * <br>
     * <code>"text/x-jsp/text/x-java"</code> has size 2.
     *
     * @return >=0 number of mime-types contained in this mime-path.
     */
    public int size() {
        return mimePaths.length;
    }
    
    /**
     * Get mime type of this mime-path at the given index.
     * <br>
     * Index zero corresponds to the root mime-type.
     * <br>
     * For <code>"text/x-jsp/text/x-java"</code> 
     * <code>getMimeType(0)</code> returns <code>"text/x-jsp"</code>
     * and <code>getMimeType(1)</code> returns <code>"text/x-java"</code>.
     *
     * @param index >=0 && < {@link #size()}.
     * @return non-null mime-type at the given index.
     * @throws IndexOutOfBoundsException in case the index is not within
     *   required bounds.
     */
    public String getMimeType(int index) {
        return mimePaths[index].mimeType;
    }
    
    /**
     * Return prefix mime-path with the given number of mime-type components
     * ranging from zero till the size of this mime-path.
     *
     * @param size >=0 && <= {@link #size()}.
     *  <br>
     *  For zero size the {@link EMPTY} will be returned.
     *  <br>
     *  For <code>size()</code> <code>this</code> will be returned.
     * @return non-null mime-type of the given size.
     * @throws IndexOutOfBoundsException in case the index is not within
     *   required bounds.
     */
    public MimePath getPrefix(int size) {
        return (size == 0)
            ? EMPTY
            : mimePaths[size - 1];
    }

    private MimePath getEmbedded(String mimeType) {
        // Attempt to retrieve from the cache first
        // It has also an advantage that the mime-type does not need
        // to be tested for correctness
        synchronized (LOCK) {
            if (mimeType2mimePathRef == null) {
                mimeType2mimePathRef = new HashMap();
            }
            Reference mpRef = (Reference)mimeType2mimePathRef.get(mimeType);
            MimePath mimePath;
            if (mpRef == null || (mimePath = (MimePath)mpRef.get()) == null) {
                // Check mimeType correctness
                int slashIndex = mimeType.indexOf('/');
                if (slashIndex == -1) { // no slash
                    throw new IllegalArgumentException("mimeType=\"" + mimeType // NOI18N
                            + "\" does not contain '/' character");
                }
                if (mimeType.indexOf('/', slashIndex + 1) != -1) { // more than one slash
                    throw new IllegalArgumentException("mimeType=\"" + mimeType // NOI18N"
                            + "\" contains more than one '/' character");
                }

                // Construct the mimePath
                mimePath = new MimePath(this, mimeType);
                mimeType2mimePathRef.put(mimeType, new SoftReference(mimePath));
            }
        
            return mimePath;
        }
    }
    
    private static MimePath parseImpl(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null"); // NOI18N
        }
        MimePath mimePath = EMPTY;
        int pathLen = path.length();
        int startIndex = 0;
        while (true) {
            int index = startIndex;
            int slashIndex = -1;
            // Search for first slash
            while (index < pathLen) {
                if (path.charAt(index) == '/') {
                    slashIndex = index;
                    break; // first slash found
                }
                index++;
            }
            if (slashIndex == -1) { // no slash found
                if (index != startIndex) {
                    throw new IllegalArgumentException("mimeType '" // NOI18N
                            + path.substring(startIndex) + "' does not contain '/'."); // NOI18N
                }
                // Empty mimeType
                break;
            }
            index++; // move after slash
            while (index < pathLen) {
                if (path.charAt(index) == '/') {
                    if (index == slashIndex + 1) { // empty second part of mimeType
                        throw new IllegalArgumentException("Two successive slashes in '" // NOI18N
                                + path.substring(startIndex) + "'"); // NOI18N
                    }
                    break;
                }
                index++;
            }
            if (index == slashIndex + 1) { // nothing after first slash
                throw new IllegalArgumentException("Empty string after '/' in '" // NOI18N
                                + path.substring(startIndex) + "'"); // NOI18N
            }
            
            // Mime type found
            String mimeType = path.substring(startIndex, index);
            mimePath = get(mimePath, mimeType);
            
            startIndex = index + 1; // after slash or after end of path
        }
        return mimePath;
    }

}
