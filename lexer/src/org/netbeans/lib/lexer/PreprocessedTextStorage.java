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

import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.CharSubSequence;

/**
 * Storage of the preprocessed text specific for batch and mutable setups.
 * <br/>
 * For mutable setups the extra length shifts need to be stored.
 * <br/>
 * For storage costs cutting only a maximum lookahead is stored (not individual
 * lookaheads).
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class PreprocessedTextStorage implements CharSequence {
    
    /** Flag for additional correctness checks (may degrade performance). */
    private static final boolean testing = Boolean.getBoolean("netbeans.debug.lexer.test");
    
    /**
     * Create preprocessed characters storage.
     * 
     * @param rawText non-null raw text before preprocessing.
     * @param preprocessedChars non-null array of preprocessed characters
     * @param preprocessedCharsLength >=0 number of valid characters in the preprocessedChars array.
     * @param preprocessedStartIndex index of first preprocessed character in the raw text.
     * @param preprocessedRawLengthShifts non-null array of shifts of the particular
     *  end of a character in the raw text against the preprocessed text.
     *  For example "a\u0062c" will produce preprocessed "abc"
     *  and the raw length shifts would be { 1-1=0, 7-2=5, 8-3=5 }.
     * 
     */
    public static PreprocessedTextStorage create(CharSequence rawText,
    char[] preprocessedChars, int preprocessedCharsLength,
    int preprocessedStartIndex, int[] preprocessedRawLengthShifts) {
        char[] preprocessedCharsCopy = ArrayUtilities.charArray(preprocessedChars, preprocessedCharsLength);
        // Get maximum raw length shift (the one of the last character of preprocessedChars)
        int totalRawLengthShift = preprocessedRawLengthShifts[preprocessedCharsLength - 1];
        // Create appropriate storage according to the max. rawLengthShift
        PreprocessedTextStorage storage;
        if (totalRawLengthShift <= Byte.MAX_VALUE) { // Use bytes
            byte[] arr = new byte[preprocessedCharsLength];
            for (int i = preprocessedCharsLength - 1; i >= 0; i--)
                arr[i] = (byte)preprocessedRawLengthShifts[i];
            storage = new ByteRawIndex(rawText, preprocessedCharsCopy, preprocessedStartIndex,
                    preprocessedCharsLength, totalRawLengthShift, arr);
            
        } else if (totalRawLengthShift <= Short.MAX_VALUE) { // Use shorts
            short[] arr = new short[preprocessedCharsLength];
            for (int i = preprocessedCharsLength - 1; i >= 0; i--)
                arr[i] = (short)preprocessedRawLengthShifts[i];
            storage = new ShortRawIndex(rawText, preprocessedCharsCopy, preprocessedStartIndex,
                    preprocessedCharsLength, totalRawLengthShift, arr);
            
        } else { // Use ints
            int[] arr = new int[preprocessedCharsLength];
            System.arraycopy(preprocessedChars, 0, arr, 0, preprocessedCharsLength);
            storage = new IntRawIndex(rawText, preprocessedCharsCopy, preprocessedStartIndex,
                    preprocessedCharsLength, totalRawLengthShift, arr);
        }

        if (testing)
            storage.consistencyCheck();
        return storage;
    }

    /**
     * Create preprocessed characters storage.
     * 
     * @param rawText non-null raw text before preprocessing.
     * @param preprocessedChars non-null array of preprocessed characters
     * @param preprocessedCharsLength >=0 number of valid characters in the preprocessedChars array.
     * @param preprocessedStartIndex index of first preprocessed character in the raw text.
     * @param preprocessedRawLengthShifts non-null array of shifts of the particular
     *  end of a character in the raw text against the preprocessed text.
     *  For example "a\u0062c" will produce preprocessed "abc"
     *  and the raw length shifts would be { 1-1=0, 7-2=5, 8-3=5 }.
     * 
     */
    public static PreprocessedTextStorage create(CharSequence rawText, char[] preprocessedChars, int preprocessedCharsLength,
    int preprocessedStartIndex, int[] preprocessedRawLengthShifts,
    char[] extraPreprocessedChars, int[] extraRawLengthShifts, int preStartIndex, int postEndIndex) {
        int extraPreCharsLength = (extraPreprocessedChars.length - preStartIndex);
        preprocessedStartIndex -= extraPreCharsLength;
        int length = extraPreCharsLength + preprocessedCharsLength + postEndIndex;
        
        // Create copy of the characters
        char[] preprocessedCharsCopy = new char[length];
        System.arraycopy(extraPreprocessedChars, preStartIndex, preprocessedCharsCopy, 0, extraPreCharsLength);
        System.arraycopy(preprocessedChars, 0, preprocessedCharsCopy, extraPreCharsLength, preprocessedCharsLength);
        System.arraycopy(extraPreprocessedChars, 0, preprocessedCharsCopy, extraPreCharsLength + preprocessedCharsLength, postEndIndex);

        // Get maximum raw length shift (the one of the last character of preprocessedChars)
        int totalRawLengthShift = (postEndIndex > 0)
                ? extraRawLengthShifts[postEndIndex - 1]
                : (preprocessedCharsLength > 0)
                    ? preprocessedRawLengthShifts[preprocessedCharsLength - 1]
                    // Otherwise get last of pre-chars
                    : extraRawLengthShifts[extraPreprocessedChars.length - 1];
        

        
        // Create appropriate storage according to the max. rawLengthShift
        int ind = length - 1;
        PreprocessedTextStorage storage;
        if (totalRawLengthShift <= Byte.MAX_VALUE) { // Use bytes
            byte[] arr = new byte[length];
            for (int i = postEndIndex - 1; i >= 0; i--)
                arr[ind--] = (byte)extraRawLengthShifts[i];
            for (int i = preprocessedCharsLength - 1; i >= 0; i--)
                arr[ind--] = (byte)preprocessedRawLengthShifts[i];
            for (int i = extraPreprocessedChars.length - 1; i >= preStartIndex; i--)
                arr[ind--] = (byte)extraRawLengthShifts[i];
            storage = new ByteRawIndex(rawText, preprocessedCharsCopy, preprocessedStartIndex, length, totalRawLengthShift, arr);
            
        } else if (totalRawLengthShift <= Short.MAX_VALUE) { // Use shorts
            short[] arr = new short[length];
            for (int i = postEndIndex - 1; i >= 0; i--)
                arr[ind--] = (short)extraRawLengthShifts[i];
            for (int i = preprocessedCharsLength - 1; i >= 0; i--)
                arr[ind--] = (short)preprocessedRawLengthShifts[i];
            for (int i = extraPreprocessedChars.length - 1; i >= preStartIndex; i--)
                arr[ind--] = (short)extraRawLengthShifts[i];
            storage = new ShortRawIndex(rawText, preprocessedCharsCopy, preprocessedStartIndex, length, totalRawLengthShift, arr);
            
        } else { // Use ints
            // System.arraycopy() could be used but usually bytes (or shorts for long tokens)
            // would be used anyway so to eliminate errors use the copy-pasted code from above
            int[] arr = new int[length];
            for (int i = postEndIndex - 1; i >= 0; i--)
                arr[ind--] = extraRawLengthShifts[i];
            for (int i = preprocessedCharsLength - 1; i >= 0; i--)
                arr[ind--] = preprocessedRawLengthShifts[i];
            for (int i = extraPreprocessedChars.length - 1; i >= preStartIndex; i--)
                arr[ind--] = extraRawLengthShifts[i];
            storage = new IntRawIndex(rawText, preprocessedCharsCopy, preprocessedStartIndex, length, totalRawLengthShift, arr);
        }
        
        if (testing)
            storage.consistencyCheck();
        return storage;
    }


    /**
     * Reference to original non-preprocessed text.
     */
    private final CharSequence rawText; // 12 bytes (8-Object + 4)
    
    /**
     * Preprocessed characters.
     */
    private final char[] preprocessedChars; // 16 bytes

    /**
     * Index of the first preprocessed char in the rawText.
     */
    private final int preprocessedStartIndex; // 20 bytes
    
    /**
     * Raw index shift of the first char after preprocessedChars characters.
     */
    private final int totalRawLengthShift; // 24 bytes
    
    /**
     * Cache the length of this text storage. Although it could be computed
     * dynamically by using rawText.length() this avoids nested
     * length() calls.
     */
    private final int length; // 28 bytes
    
    protected PreprocessedTextStorage(CharSequence rawText, char[] preprocessedChars,
    int preprocessedStartIndex, int length, int totalRawLengthShift) {
        this.rawText = rawText;
        this.preprocessedChars = preprocessedChars;
        this.preprocessedStartIndex = preprocessedStartIndex;
        // Compute end raw index without delegating to rawText methods e.g. length()
        this.totalRawLengthShift = totalRawLengthShift;
        this.length = length;
    }
    
    protected abstract int prepRawLengthShift(int index);
    
    /**
     * Raw length for the given length.
     *
     * @param length length in this character sequence.
     * @return raw length for the given length.
     */
    public final int rawLength(int length) {
        if (length > preprocessedStartIndex) {
            int prepLength = length - preprocessedStartIndex;
            if (prepLength <= preprocessedChars.length) {
                length += prepRawLengthShift(prepLength - 1);
            } else {
                length += totalRawLengthShift;
            }
        }
        return length;
    }
    
    /**
     * Raw length shift corresponding to the given index.
     *
     * @param index >=0 and <length() index in this text storage.
     * @return raw length shift corresponding to the given index.
     */
    public final int rawLengthShift(int index) {
        if (index < preprocessedStartIndex) {
            return index;
        } else {
            index -= preprocessedStartIndex;
            if (index <= preprocessedChars.length) {
                return prepRawLengthShift(index) ;
            } else { // past the end of preprocessed characters
                return totalRawLengthShift;
            }
        }
    }
    
    public final char charAt(int index) {
        CharSequenceUtilities.checkIndexValid(index, length);
        if (index < preprocessedStartIndex) {
            return rawText.charAt(index);
        } else {
            int prepIndex = index - preprocessedStartIndex;
            if (prepIndex < preprocessedChars.length) {
                return preprocessedChars[prepIndex];
            } else { // past the end of preprocessed characters
                return rawText.charAt(index + totalRawLengthShift);
            }
        }
    }

    public final CharSequence subSequence(int start, int end) {
        return new CharSubSequence(this, start, end);
    }

    public final int length() {
        return length;
    }
    
    private void consistencyCheck() {
        // Check that raw length shifts have increasing order
        int lastRLS = 0;
        for (int i = 0; i < preprocessedChars.length; i++) {
            int rls = prepRawLengthShift(i);
            if (rls < lastRLS) {
                throw new IllegalStateException("rls=" + rls // NOI18N
                        + " < lastRLS=" + lastRLS + " at index=" + i); // NOI18N
            }
            lastRLS = rls;
        }
    }

    private static final class ByteRawIndex extends PreprocessedTextStorage {
        
        private final byte[] preprocessedRawLengthShifts; // 24 bytes
        
        ByteRawIndex(CharSequence rawText, char[] preprocessedChars, int preprocessedStartIndex,
        int length, int totalRawLengthShift, byte[] preprocessedRawLengthShifts) {
            super(rawText, preprocessedChars, preprocessedStartIndex, length, totalRawLengthShift);
            this.preprocessedRawLengthShifts = preprocessedRawLengthShifts;
        }

        protected final int prepRawLengthShift(int index) {
            return preprocessedRawLengthShifts[index];
        }

    }
    
    private static final class ShortRawIndex extends PreprocessedTextStorage {
        
        private final short[] preprocessedRawLengthShifts; // 24 bytes

        ShortRawIndex(CharSequence rawText, char[] preprocessedChars, int preprocessedStartIndex,
        int length, int totalRawLengthShift, short[] preprocessedRawLengthShifts) {
            super(rawText, preprocessedChars, preprocessedStartIndex, length, totalRawLengthShift);
            this.preprocessedRawLengthShifts = preprocessedRawLengthShifts;
        }

        protected final int prepRawLengthShift(int index) {
            return preprocessedRawLengthShifts[index];
        }

    }

    private static final class IntRawIndex extends PreprocessedTextStorage {
        
        private final int[] preprocessedRawLengthShifts; // 24 bytes

        IntRawIndex(CharSequence rawText, char[] preprocessedChars, int preprocessedStartIndex,
        int length, int totalRawLengthShift, int[] preprocessedRawLengthShifts) {
            super(rawText, preprocessedChars, preprocessedStartIndex, length, totalRawLengthShift);
            this.preprocessedRawLengthShifts = preprocessedRawLengthShifts;
        }

        protected final int prepRawLengthShift(int index) {
            return preprocessedRawLengthShifts[index];
        }

    }
    
}
