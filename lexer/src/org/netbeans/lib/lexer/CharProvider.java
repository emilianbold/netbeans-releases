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

/**
 * Provides characters to the clients.
 * <br/>
 * It's implemented either by lexer input operation or preprocessor operation.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface CharProvider {

    /**
     * Read next char from input.
     */
    int read();

    /**
     * Read char that was already read.
     */
    char readExisting(int index);

    /**
     * Present read index.
     */
    int readIndex();

    /**
     * Get cumulative length in the parent character providers
     * that corresponds to the length in this provider.
     */
    int deepRawLength(int length);
    
    /**
     * Get cumulative length shift in the parent character providers
     * that corresponds to the given index.
     */
    int deepRawLengthShift(int index);
    
    /**
     * Backup given number of characters.
     * <br/>
     * The EOF cannot be backup-ed.
     */
    void backup(int count);

    /**
     * Retrieve present token length assigned
     * during tokenRecognized() method.
     */
    int tokenLength();
    
    /**
     * Notify this provider that a token with the given length was recognized.
     * <br/>
     * The token length for this particular provider may differ from the real
     * token length in the root lexer input operation due to character
     * preprocessing.
     * <br/>
     * The tokenLength should be cached by this provider.
     * @param skip whether the token will be skipped due to filtering of its id.
     * @return true if the token is preprocessed or false otherwise.
     */
    void tokenRecognized(int tokenLength);
    
    /**
     * Notify this provider that the token was approved and
     * that the tokenLength number of characters should be skipped
     * (tokenLength should be cached by the provider).
     */
    void tokenApproved();
    
    /**
     * Collect extra preprocessed characters from the parent providers.
     * <br/>
     * They can consist from extra characters before the preprocessed characters
     * in the top provider and the extra characters after 
     * the preprocessed characters in the top provider.
     *
     * @param epc non-null extra preprocessed characters.
     * @param prepStartIndex first preprocessed index in the parent provider.
     * @param prepEndIndex first non-preprocessed index in the parent provider.
     * @param topPrepEndIndex top provider's prep end index - needed for proper
     *  computation of raw length shift.
     */
    void collectExtraPreprocessedChars(ExtraPreprocessedChars epc,
    int prepStartIndex, int prepEndIndex, int topPrepEndIndex);
    
    
    /**
     * Storage of the extra preprocessed characters in parent providers.
     */
    public static final class ExtraPreprocessedChars {
        
        private int preStartIndex;
        
        private int postEndIndex;
        
        private char[] extraPrepChars = ArrayUtilities.emptyCharArray();
        
        private int[] extraRawLengthShifts;
        
        public void ensureExtraLength(int length) {
            int preLength = extraPrepChars.length - preStartIndex;
            length += postEndIndex + preLength;
            if (length > extraPrepChars.length) {
                length <<= 1;
                extraPrepChars = ArrayUtilities.charArray(extraPrepChars, length,
                        postEndIndex, preStartIndex - postEndIndex);
                extraRawLengthShifts = ArrayUtilities.intArray(extraRawLengthShifts,
                        length, postEndIndex, preStartIndex - postEndIndex);
                preStartIndex = extraPrepChars.length - preLength;  
            }
        }
        
        public void insert(char ch, int rawLengthShift) {
            preStartIndex--;
            extraPrepChars[extraPrepChars.length - preStartIndex] = ch;
            extraRawLengthShifts[extraPrepChars.length - preStartIndex] = rawLengthShift;
        }
        
        public void append(char ch, int rawLengthShift) {
            extraPrepChars[postEndIndex] = ch;
            extraRawLengthShifts[postEndIndex] = rawLengthShift;
            postEndIndex++;
        }
        
        public void clear() {
            preStartIndex = extraPrepChars.length;
            postEndIndex = 0;
        }
        
        public int preStartIndex() {
            return preStartIndex;
        }
        
        public int postEndIndex() {
            return postEndIndex;
        }
        
        public char[] extraPrepChars() {
            return extraPrepChars;
        }
        
        public int[] extraRawLengthShifts() {
            return extraRawLengthShifts;
        }
        
    }
    
}
