/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2005-2007 Sun
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

package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    /** maximum count of stored replacement expressions */
    private static final int maxReplExprCount = 10;

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
    private List<String> fileNamePatterns;
    /**
     * storage of last used replacement expressions
     * (initially {@code null})
     */
    private List<String> replExpressions;

    /**
     * Storage of last used Whole Words option.
     */
    private boolean wholeWords;

    /**
     * Storage of last used Case Sensitive option.
     */
    private boolean caseSensitive;

    /**
     * Storage of last used Regular Expression option.
     */
    private boolean regularExpression;

    /**
     * whether a full text pattern was used last time
     */
    private boolean textPatternSpecified;

    /**
     * whether file name pattern was used last time
     */
    private boolean fileNamePatternSpecified;
    
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
        if (fileNamePatterns == null) {
            fileNamePatterns = new ArrayList<String>(maxFileNamePatternCount);
            fileNamePatterns.add(pattern);
            return;
        }

        assert !fileNamePatterns.isEmpty();

        int index = fileNamePatterns.indexOf(pattern);
        if (index != -1) {
            if (index == fileNamePatterns.size() - 1) {
                return;
            }

            fileNamePatterns.remove(index);
        } else if (fileNamePatterns.size() == maxFileNamePatternCount) {
            fileNamePatterns.remove(0);
        }
        fileNamePatterns.add(pattern);
    }

    /**
     * Returns last used file name patterns in order
     * from the oldest ones to the most recently used ones.
     *
     * @return  list of the last used file name patterns, or an empty list
     *          if no file name patterns are stored
     */
    List<String> getFileNamePatterns() {
        assert fileNamePatterns == null || !fileNamePatterns.isEmpty();

        return (fileNamePatterns != null) ? fileNamePatterns
                                          : Collections.<String>emptyList();
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
        if (replExpressions == null) {
            replExpressions = new ArrayList<String>(maxReplExprCount);
            replExpressions.add(expression);
            return;
        }

        assert !replExpressions.isEmpty();

        int index = replExpressions.indexOf(expression);
        if (index != -1) {
            if (index == replExpressions.size() - 1) {
                return;
            }

            replExpressions.remove(index);
        } else if (replExpressions.size() == maxReplExprCount) {
            replExpressions.remove(0);
        }
        replExpressions.add(expression);
    }

    /**
     * Returns last used replacement expressions in order
     * from the oldest ones to the most recently used ones.
     *
     * @return  list of last used replacement expressions, or an empty list
     *          if no replacement expressions are stored
     */
    List<String> getReplacementExpressions() {
        assert replExpressions == null || !replExpressions.isEmpty();

        return (replExpressions != null) ? replExpressions
                                          : Collections.<String>emptyList();
    }

    public boolean isWholeWords() {
        return wholeWords;
    }

    public void setWholeWords(boolean wholeWords) {
        this.wholeWords = wholeWords;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(boolean regularExpression) {
        this.regularExpression = regularExpression;
    }

    boolean isTextPatternSpecified() {
        return textPatternSpecified;
    }

    void setTextPatternSpecified(boolean specified) {
        textPatternSpecified = specified;
    }

    boolean isFileNamePatternSpecified() {
        return fileNamePatternSpecified;
    }

    void setFileNamePatternSpecified(boolean specified) {
        fileNamePatternSpecified = specified;
    }

}
