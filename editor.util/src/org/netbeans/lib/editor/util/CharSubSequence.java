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

package org.netbeans.lib.editor.util;

/**
 * Subsequence of the given character sequence. The backing sequence
 * is considered to be stable i.e. does not change length or content over time.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CharSubSequence extends AbstractCharSequence {

    /**
     * Ensure that the given start and end parameters are valid indices
     * of the given text.
     * @throws IndexOutOfBoundsException if the start or end are not within bounds
     *  of the given text.
     * @deprecated use {@link CharSequenceUtilities#checkIndexesValid(CharSequence, int, int)}
     */
    public static void checkIndexesValid(CharSequence text, int start, int end) {
        CharSequenceUtilities.checkIndexesValid(text, start, end);
    }
    
    private int length;
    
    private int start;

    private CharSequence backingSequence;
    
    /**
     * Construct character subsequence with the given backing character sequence.
     *
     * @param backingSequence non-null backing character sequence. It is considered
     * to be stable and not to change over time.
     * @param start &gt;=0 starting index of the subsequence within
     *  the backing character sequence.
     * @param end &gt;= ending index of the subsequence within
     *  the backing character sequence.
     * @throws IndexOutOfBoundsException if the start or end are not within bounds
     *  of backingSequence.
     */
    public CharSubSequence(CharSequence backingSequence, int start, int end) {
        checkIndexesValid(backingSequence, start, end);
        this.backingSequence = backingSequence;
        this.start = start;
        this.length = end - start;
    }
    
    protected CharSequence backingSequence() {
        return backingSequence;
    }
    
    protected int start() {
        return start;
    }

    public int length() {
        return length;
    }

    public char charAt(int index) {
        CharSequenceUtilities.checkIndexValid(index, length);
        return backingSequence.charAt(start() + index);
    }

    /**
     * Subclass providing string-like implementation
     * of <code>hashCode()</code> and <code>equals()</code>
     * method accepting strings with the same content
     * like charsequence has.
     * <br>
     * This makes the class suitable for matching to strings
     * e.g. in maps.
     * <br>
     * <b>NOTE</b>: Matching is just uni-directional
     * i.e. charsequence.equals(string) works
     * but string.equals(charsequence) does not.
     */
    public static class StringLike extends CharSubSequence {


        public StringLike(CharSubSequence backingSequence, int start, int end) {
            super(backingSequence, start, end);
        }
    
        public int hashCode() {
            return CharSequenceUtilities.stringLikeHashCode(this);
        }

        public boolean equals(Object o) {
            return CharSequenceUtilities.equals(this, o);
        }
        
    }

}
