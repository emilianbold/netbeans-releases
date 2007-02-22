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

package org.netbeans.modules.xml.xam.ui.search;

/**
 * Encapsulates the search query and its parameters.
 *
 * @author Nathan Fiedler
 */
public class Query {
    /** The query string, possibly a regular expression. */
    private String query;
    /** Indicates if search is rooted at selected component, or the
     * XAM model root component. */
    private boolean selected;
    /** Indicates if the query is a regular expression or a plain string. */
    private boolean regex;

    /**
     * Creates a new instance of Query.
     *
     * @param  query     phrase to search for.
     * @param  selected  if true, limit search to selected subtree.
     * @param  regex     if true, query is a regular expression.
     */
    public Query(String query, boolean selected, boolean regex) {
        this.query = query;
        this.selected = selected;
        this.regex = regex;
    }

    /**
     * Returns the query string entered by the user.
     *
     * @return  search phrase.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns true if the query string is a Perl-like regular expression,
     * false if it is a plain string.
     *
     * @return  true if regex, false otherwise.
     */
    public boolean isRegularExpression() {
        return regex;
    }

    /**
     * Returns true if this query should be rooted at the selected component,
     * or if the search should begin at the root component.
     *
     * @return  true if searching from selected component, false if root.
     * @see #getSelectedComponent
     */
    public boolean useSelected() {
        return selected;
    }
}
