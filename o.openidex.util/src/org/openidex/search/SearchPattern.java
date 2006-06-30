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
 * Software is Sun Microsystems, Inc. Portions Copyright 2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

/**
 * Pattern describes the search conditions
 *
 * @since  org.openidex.util/3 3.5, NB 4.1
 * @author  Martin Roskanin
 */
public final class SearchPattern {

    /** SearchExpression - a text to search */
    private String searchExpression;

    /** if true, only whole words were searched */
    private boolean wholeWords;

    /** if true, case sensitive search was preformed */
    private boolean matchCase;

    /** if true, regular expression search was performed */
    private boolean regExp;

    /** Creates a new instance of SearchPattern 
     *  @param searchExpression a searched text
     *  @param wholeWords if true, only whole words were searched
     *  @param matchCase if true, case sensitive search was preformed
     *  @param regExp if true, regular expression search was performed
     */
    private SearchPattern(String searchExpression, boolean wholeWords,
            boolean matchCase, boolean regExp) {
        this.searchExpression = searchExpression;
        this.wholeWords = wholeWords;
        this.matchCase = matchCase;
        this.regExp = regExp;
    }
 
    /** Creates a new SearchPattern in accordance with given parameters 
     *  @param searchExpression non-null String of a searched text
     *  @param wholeWords if true, only whole words were searched
     *  @param matchCase if true, case sensitive search was preformed
     *  @param regExp if true, regular expression search was performed
     *  @return a new SearchPattern in accordance with given parameters
     */
    public static SearchPattern create(String searchExpression, boolean wholeWords,
            boolean matchCase, boolean regExp){
        return new SearchPattern(searchExpression, wholeWords, matchCase, regExp);
    }
    
    /** @return searchExpression */
    public String getSearchExpression(){
        return searchExpression;
    }
    
    /** @return true if the wholeWords parameter was used during search performing */
    public boolean isWholeWords(){
        return wholeWords;
    }
    
    /** @return true if the matchCase parameter was used during search performing */
    public boolean isMatchCase(){
        return matchCase;
    }
    
    /** @return true if the regExp parameter was used during search performing */
    public boolean isRegExp(){
        return regExp;
    }

    public boolean equals(Object obj){
        if (!(obj instanceof SearchPattern)){
            return false;
        }
        SearchPattern sp = (SearchPattern)obj;
        return (this.searchExpression.equals(sp.getSearchExpression()) &&
                this.wholeWords == sp.isWholeWords() &&
                this.matchCase == sp.isMatchCase() &&
                this.regExp == sp.isRegExp());
    }
    
    public int hashCode() {
        int result = 17;
        result = 37*result + (this.wholeWords ? 1:0);
        result = 37*result + (this.matchCase ? 1:0);
        result = 37*result + (this.regExp ? 1:0);
        result = 37*result + this.searchExpression.hashCode();
        return result;
    }
}
