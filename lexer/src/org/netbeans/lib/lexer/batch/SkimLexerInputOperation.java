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

package org.netbeans.lib.lexer.batch;

import java.io.IOException;
import java.io.Reader;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.spi.lexer.LexerInput;

/**
 * Lexer input operation over a {@link java.io.Reader}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class SkimLexerInputOperation<T extends TokenId> extends LexerInputOperation<T> {
    
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    
    /**
     * Default size for reading char array.
     */
    private static final int DEFAULT_READ_CHAR_ARRAY_SIZE = 4096;
    
    /**
     * Minimum size to be read (to have space for reading).
     */
    private static final int MIN_READ_SIZE = 512;
    
    private static final int DEFAULT_CLUSTER_SIZE = 4096;
    
    /**
     * Maximum fragmentation factor for token character arrays.
     * <br>
     * If there is not enough space in the tokenCharArray
     * to copy a token's characters there then if the token's length
     * will be greater than this threshold then the token will get
     * an extra character buffer just for itself and there will
     * still be chance to use the present tokenCharArray for tokens
     * with lower length.
     */
    private static final int MAX_UNUSED_CLUSTER_SIZE_FRACTION = 50;
    

    /**
     * Reader as a primary source of characters that are further
     * copied and cached.
     */
    private Reader reader;
    
    /**
     * Array holding the read characters.
     */
    private char[] readCharArray;
    
    /**
     * Character sequence holding the characters to be read.
     */
    private CharSequence readCharSequence;
    
    /**
     * Index of a first character in the token being currently recognized.
     */
    private int readStartIndex;
    
    /**
     * End of valid chars in readCharArray (points to first invalid char).
     */
    private int readEndIndex;
    
    /**
     * Whether EOF was read from reader already or not.
     */
    private boolean eofRead;
    
    /**
     * Actual token cluster where the tokens are being placed.
     */
    private SkimTokenList<T> cluster;

    private int clusterTextEndIndex;
    
    private int defaultClusterSize = DEFAULT_CLUSTER_SIZE;
    
    /**
     * Starting offset of the cluster currently being used.
     */
    private int clusterStartOffset;
    
    /**
     * How much the offset is ahead of the token's text offset
     * in the cluster. The tokens that get skipped and flyweight tokens
     * increase this value because their text is not physically copied
     * into the clusters character data but they increase the offset.
     */
    private int offsetShift;
    
    public SkimLexerInputOperation(TokenList<T> tokenList, Reader reader) {
        super(tokenList, 0, null);
        this.reader = reader;
        this.readCharArray = new char[DEFAULT_READ_CHAR_ARRAY_SIZE];
    }
    
    public SkimLexerInputOperation(TokenList<T> tokenList, CharSequence readCharSequence) {
        super(tokenList, 0, null);
        this.readCharSequence = readCharSequence;
        this.readEndIndex = readCharSequence.length();
    }
    
    public int read(int index) { // index >= 0 is guaranteed by contract
        index += readStartIndex;
        if (index < readEndIndex) {
            return (readCharArray != null)
                ? readCharArray[index]
                : readCharSequence.charAt(index);

        } else { // must read next or return EOF
            if (!eofRead) {
                eofRead = (readCharArray != null)
                    ? readNextCharArray()
                    : true; // using readCharSequence -> no more chars

                return read(index);

            } else {
                return LexerInput.EOF;
            }
        }
    }
    
    public char readExisting(int index) {
        return (readCharArray != null)
            ? readCharArray[index]
            : readCharSequence.charAt(index);
    }
    
    public void approveToken(AbstractToken<T> token) {
        int tokenLength = token.length();
        if (isSkipToken(token)) {
            preventFlyToken();
            skipChars(tokenLength());
            
        } else if (token.isFlyweight()) {
            assert isFlyTokenAllowed();
            flyTokenAdded();
            skipChars(tokenLength);

        } else { // non-flyweight token => must be L0Token instance
            if (clusterTextEndIndex != 0) { // valid cluster exists
                // Check whether token fits into cluster's char array
                if (tokenLength + clusterTextEndIndex > cluster.getText().length) {
                    // Cannot fit the token's text into current cluster
                    finishCluster();
                }
            }

            if (clusterTextEndIndex == 0) { // allocate new cluster
                int clusterSize = defaultClusterSize;
                if (clusterSize < tokenLength) { // cluster just for one token
                    clusterSize = tokenLength;
                }
                defaultClusterSize = clusterSize;
                cluster = new SkimTokenList<T>((CopyTextTokenList<T>)tokenList(),
                        clusterStartOffset, new char[clusterSize]);
            }

            // Now it's clear that the token will fit into the cluster's text
            // TODO for DirectCharSequence use more efficient way
            char[] clusterText = cluster.getText();
            if (readCharArray != null) {
                System.arraycopy(readCharArray, readStartIndex, clusterText,
                        clusterTextEndIndex, tokenLength);
            } else { // using readCharSequence
                for (int i = 0; i < tokenLength; i++) {
                    clusterText[clusterTextEndIndex + i]
                            = readCharSequence.charAt(readStartIndex + i);
                }
            }
            
            int rawOffset = (offsetShift << 16) | clusterTextEndIndex;
            token.setTokenList(cluster);
            token.setRawOffset(rawOffset);
            clusterTextEndIndex += tokenLength;
            clearFlySequence();
        }

        readStartIndex += tokenLength;
        tokenApproved();
    }

    private void skipChars(int skipLength) {
        if (clusterTextEndIndex != 0) { // cluster already populated
            if (offsetShift + skipLength > Short.MAX_VALUE) {
                // Cannot advance offset shift without overflowing -> cluster is finished
                finishCluster();
                clusterStartOffset += skipLength;

            } else { // relOffset will fit into current cluster
                offsetShift += skipLength;
            }

        } else { // cluster is null -> can shift cluster's start offset
            clusterStartOffset += skipLength;
        }
    }        
    
    public void finish() {
        if (clusterTextEndIndex != 0) {
            finishCluster();
        }
    }

    private void finishCluster() {
        // If there would be too much unused space in the cluster's char array
        // then it will be reallocated.
        int clusterTextLength = cluster.getText().length;
        if (clusterTextLength / MAX_UNUSED_CLUSTER_SIZE_FRACTION
                > (clusterTextLength - clusterTextEndIndex)
        ) { // Fragmentation -> reallocate cluster's char array
            char[] newText = new char[clusterTextEndIndex];
            System.arraycopy(cluster.getText(), 0, newText, 0, clusterTextEndIndex);
            cluster.setText(newText);
        }
        clusterStartOffset += clusterTextEndIndex + offsetShift;
        clusterTextEndIndex = 0;
        offsetShift = 0;
        cluster = null; // cluster no longer valid
    }
    
    private boolean readNextCharArray() {
        // Copy everything from present readStartIndex till readEndIndex
        int retainLength = readEndIndex - readStartIndex;
        int minReadSize = readCharArray.length - retainLength;
        char[] newReadCharArray = readCharArray; // by default take original one
        if (minReadSize < MIN_READ_SIZE) { // allocate new
            // double the current array's size
            newReadCharArray = new char[readCharArray.length * 2];
        }
        System.arraycopy(readCharArray, readStartIndex, newReadCharArray, 0, retainLength);
        readCharArray = newReadCharArray;
        readStartIndex = 0;
        readEndIndex = retainLength;
        
        boolean eof = false;
        while (readEndIndex < readCharArray.length) {
            int readSize;
            try {
                readSize = reader.read(readCharArray, readEndIndex,
                    readCharArray.length - readEndIndex);
            } catch (IOException e) {
                // The exception is silently ignored here
                // This should generally not happen - a wrapping reader
                // should be used that will catch and process the IO exceptions.
                readSize = -1;
            }
            if (readSize == -1) {
                eof = true;
                try {
                    reader.close();
                } catch (IOException e) {
                    // The exception is silently ignored here
                    // This should generally not happen - a wrapping reader
                    // should be used that will catch and process the IO exceptions.
                }
                break;
            } else {
                readEndIndex += readSize;
            }
        }
        return eof;
    }

}
