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

package org.netbeans.modules.xml.xam.ui.search;

import java.util.List;

/**
 * Provides the capability to search a XAM model and display the results
 * in a manner appropriate for the current view of the model.
 *
 * @author Nathan Fiedler
 */
public interface SearchProvider {

    /**
     * Returns the display string for describing this search provider.
     *
     * @return  display name.
     */
    String getDisplayName();

    /**
     * Returns a description of the expected input for this provider.
     * This is used to instruct the user on what should be entered into
     * the search field.
     *
     * @return  description of expected input.
     */
    String getInputDescription();

    /**
     * Returns a brief description of this provider, useful for tooltips.
     *
     * @return  short description.
     */
    String getShortDescription();

    /**
     * Performs the search using the given query string. The query string
     * will match any element that contains the given string, whether in
     * whole or in part (i.e. a "sub-string" search), and will be compared
     * in a case-insensitive fashion.
     *
     * @param  query  encapsulates the search parameters.
     * @return  search results, or empty if none found.
     * @throws  SearchException
     *          if the search resulted in an unexpected error.
     */
    List<Object> search(Query query) throws SearchException;
}
