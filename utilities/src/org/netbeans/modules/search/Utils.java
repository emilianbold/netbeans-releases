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
 * Software is Sun Microsystems, Inc. Portions Copyright 2003-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.openide.util.Lookup;
import org.openidex.search.SearchType;

/**
 *
 * @author  Marian Petras
 */
final class Utils {

    /**
     * result of lookup for registered search types
     *
     * @see  #getSearchTypes
     */
    private static Lookup.Result<SearchType> result;

    private Utils() { }
    
    /**
     * Finds all registered instances of class <code>SearchType</code>.
     * <p>
     * When this method is called for the first time, a lookup is performed
     * and its result stored. Subsequent calls return the remembered result.
     *
     * @return  result of lookup for instances of class <code>SearchType</code>
     * @see  SearchType
     */
    private static Lookup.Result<SearchType> getSearchTypes0() {
        if (result == null) {
            result = Lookup.getDefault().lookup(
                    new Lookup.Template<SearchType>(SearchType.class));
        }
        return result;
    }
    
    /**
     * Returns a list of all registered search types.
     *
     * @return  all instances of {@link SearchType} available via
     *          {@link Lookup}
     */
    static Collection<? extends SearchType> getSearchTypes() {
        return getSearchTypes0().allInstances();
    }
    
    /**
     * Returns a subclass of <code>SearchType</code>, having the specified name.
     * A search is performed through all registered instances of
     * <code>SearchType</code> (in a {@link Lookup Lookup}).
     *
     * @param  className  class name of the requested search type
     * @return  subclass of <code>SearchType</code>, having the specified name;
     *          or <code>null</code> is none was found
     * @see  SearchType
     */
    static Class searchTypeForName(String className) {
        for (Class c : getSearchTypes0().allClasses()) {
            if (c.getName().equals(className)) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Returns a border for explorer views.
     *
     * @return  border to be used around explorer views
     *          (<code>BeanTreeView</code>, <code>TreeTableView</code>,
     *          <code>ListView</code>).
     */
    static final Border getExplorerViewBorder() {
        Border border;
        border = (Border) UIManager.get("Nb.ScrollPane.border");        //NOI18N
        if (border == null) {
            border = BorderFactory.createEtchedBorder();
        }
        return border;
    }    
    
    /**
     * Clones a list of <code>SearchType</code>s.
     *
     * @param  searchTypes  list of search types to be cloned
     * @return  deep copy of the given list of <code>SearchTypes</code>s
     */
    static List<SearchType> cloneSearchTypes(
                                Collection<? extends SearchType> searchTypes) {
        if (searchTypes.isEmpty()) {
            return Collections.<SearchType>emptyList();
        }
        
        List<SearchType> clonedSearchTypes
                = new ArrayList<SearchType>(searchTypes.size());
        for (SearchType searchType : searchTypes) {
            clonedSearchTypes.add((SearchType) searchType.clone());
        }
        return clonedSearchTypes;
    }
    
    
}
