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
 * Software is Sun Microsystems, Inc. Portions Copyright 2005-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import org.openidex.search.SearchType;

/**
 * Registry for everything related to the Find dialog.
 * It is <em>not</em> designed to be persistent across invocations
 * of the IDE.
 *
 * @author Marian Petras
 */
public final class FindDialogMemory {

    /** maximum count of stored file name patterns */
    private static final int maxFileNamePatternCount = 10;
    /** */
    private static final String[] noFileNamePatterns = new String[0];
    /** maximum count of stored replacement expressions */
    private static final int maxReplExprCount = 10;
    /** */
    private static final String[] noReplExpressions = new String[0];

    /** singleton instance of this class */
    private static FindDialogMemory singleton;
    
    /**
     * stores the last used <code>SearchType</code>
     */
    private SearchType lastSearchType = null;
    /**
     * storage of last used file name patterns
     * (initially {@code null})
     */
    private String[] fileNamePatterns;
    /**
     * current number of stored
     * {@link #fileNamePatterns file name patterns}
     */
    private int fileNamePatternCount = 0;
    /**
     * storage of last used replacement expressions
     * (initially {@code null})
     */
    private String[] replExpressions;
    /**
     * current number of stored
     * {@link #replExpressions replacement expressions}
     */
    private int replExprCount = 0;
    
    /** Creates a new instance of FindDialogMemory */
    private FindDialogMemory() { }
    
    
    /**
     */
    public static FindDialogMemory getDefault() {
        if (singleton == null) {
            singleton = new FindDialogMemory();
        }
        return singleton;
    }
    
    /**
     */
    public void setLastUsedSearchType(SearchType searchType){
        lastSearchType = searchType;
    }
    
    /**
     */    
    public SearchType getLastSearchType(){
        return lastSearchType;
    }

    /**
     * Stores a file name pattern.
     * If the number of patterns would exceed the maximum
     * number of patterns that can be stored, the oldest
     * pattern is removed prior to storing the new pattern.
     * 
     * @param  pattern  pattern to be stored
     */
    void storeFileNamePattern(String pattern) {
        if (fileNamePatternCount == 0) {
            assert fileNamePatterns == null;
            fileNamePatterns = new String[maxFileNamePatternCount];
        } else if (fileNamePatternCount == maxFileNamePatternCount) {
            System.arraycopy(fileNamePatterns, 1,
                             fileNamePatterns, 0,
                             --fileNamePatternCount);
        }
        fileNamePatterns[fileNamePatternCount++] = pattern;
    }

    /**
     * Returns last used file name patterns in order from the
     * most recently used ones to the oldest ones.
     *
     * @return  last used file name patterns, or an empty array
     *          if no file name patterns are available
     */
    String[] getFileNamePatterns() {
        String[] result;
        if (fileNamePatternCount == 0) {
            result = noFileNamePatterns;
        } else {
            result = new String[fileNamePatternCount];
            int srcIndex = fileNamePatternCount - 1;
            int dstIndex = 0;
            do {
                result[dstIndex++] = fileNamePatterns[srcIndex--];
            } while (srcIndex != -1);
        }
        return result;
    }

    /**
     * Stores a replacement expression.
     * If the number of replacement expressions would exceed the maximum
     * number of replacement expressions that can be stored, the oldest
     * expression is removed prior to storing the new expression.
     * 
     * @param  expression  replacement expression to be stored
     */
    void storeReplacementExpression(String expression) {
        if (replExprCount == 0) {
            assert replExpressions == null;
            replExpressions = new String[maxReplExprCount];
        } else if (replExprCount == maxReplExprCount) {
            System.arraycopy(replExpressions, 1,
                             replExpressions, 0,
                             --replExprCount);
        }
        replExpressions[replExprCount++] = expression;
    }

    /**
     * Returns last used replacement expressions in order from the
     * most recently used ones to the oldest ones.
     *
     * @return  last used replacement expressions, or an empty array
     *          if no replacement expressions are available
     */
    String[] getReplacementExpressions() {
        String[] result;
        if (replExprCount == 0) {
            result = noReplExpressions;
        } else {
            result = new String[replExprCount];
            int srcIndex = replExprCount - 1;
            int dstIndex = 0;
            do {
                result[dstIndex++] = replExpressions[srcIndex--];
            } while (srcIndex != -1);
        }
        return result;
    }

}
