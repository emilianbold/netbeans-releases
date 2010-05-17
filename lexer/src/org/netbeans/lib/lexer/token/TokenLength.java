/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.lexer.token;

import org.netbeans.lib.lexer.LexerUtilsConstants;

/**
 * Improves performance of doing Token.text().toString().
 * by using a cache factor which gets increased by every access to that method.
 * <br/>
 * Once a cache factor exceeds a threshold the result of Token.text().toString()
 * will be cached.
 * <br/>
 * TBD values of constants used by this class should be reviewed to match 
 * a real complexity of the particular operations.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenLength implements CharSequence {
    
    private static final TokenLength[][] CACHE = new TokenLength[
            LexerUtilsConstants.MAX_CACHED_TOKEN_LENGTH + 1][];
    
    
    public static TokenLength get(int length) {
        TokenLength tokenLength;
        if (length <= LexerUtilsConstants.MAX_CACHED_TOKEN_LENGTH) {
            synchronized (CACHE) {
                TokenLength[] tokenLengths = CACHE[length];
                if (tokenLengths == null) {
                    tokenLengths = new TokenLength[1];
                    CACHE[length] = tokenLengths;
                }
                tokenLength = tokenLengths[0];
                if (tokenLength == null) {
                    tokenLength = new TokenLength(length, 
                            LexerUtilsConstants.CACHE_TOKEN_TO_STRING_THRESHOLD, (short)1);
                    tokenLengths[0] = tokenLength;
                }
            }
        } else { // length too high - not cached
            tokenLength = new TokenLength(length,
                    LexerUtilsConstants.CACHE_TOKEN_TO_STRING_THRESHOLD, (short)1);
        }
        return tokenLength;
    }
    
    /**
     * Length of a token.
     */
    private final int length; // 12 bytes (8-super + 4)
    
    /**
     * Cache factor of this item.
     */
    private final short cacheFactor; // 14 bytes
    
    /**
     * Index of a next item in array of token lengths with the same length in CACHE.
     */
    private final short nextArrayIndex; // 16 bytes
    
    TokenLength(int length, short cacheFactor, short nextArrayIndex) {
        this.length = length;
        this.cacheFactor = cacheFactor;
        this.nextArrayIndex = nextArrayIndex;
    }
    
    public int length() {
        return length;
    }
    
    public short cacheFactor() {
        return cacheFactor;
    }
    
    public int nextCacheFactor() {
        return cacheFactor + length + LexerUtilsConstants.TOKEN_LENGTH_STRING_CREATION_FACTOR;
    }
    
    public TokenLength next(int nextCacheFactor) {
        TokenLength tokenLength;
        if (length <= LexerUtilsConstants.MAX_CACHED_TOKEN_LENGTH) {
            synchronized (CACHE) {
                TokenLength[] tokenLengths = CACHE[length];
                if (tokenLengths == null || tokenLengths.length <= nextArrayIndex) {
                    TokenLength[] tmp = new TokenLength[nextArrayIndex + 1];
                    if (tokenLengths != null) {
                        System.arraycopy(tokenLengths, 0, tmp, 0, tokenLengths.length);
                    }
                    tokenLengths = tmp;
                    CACHE[length] = tokenLengths;
                }
                tokenLength = tokenLengths[nextArrayIndex];
                if (tokenLength == null) {
                    tokenLength = new TokenLength(length, (short)nextCacheFactor, (short)(nextArrayIndex + 1));
                    tokenLengths[nextArrayIndex] = tokenLength;
                }
            }
        } else { // length too high - not cached
            tokenLength = new TokenLength(length, (short)nextCacheFactor, (short)(nextArrayIndex + 1));
        }
        return tokenLength;
    }

    public char charAt(int index) {
        throw new IllegalStateException("Should never be called.");
    }

    public CharSequence subSequence(int start, int end) {
        throw new IllegalStateException("Should never be called.");
    }

}
