/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static Lookup.Result result;

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
    private static Lookup.Result getSearchTypes0() {
        if (result == null) {
            result = Lookup.getDefault().lookup(
                    new Lookup.Template(SearchType.class));
        }
        return result;
    }
    
    /**
     * Returns a list of all registered search types.
     *
     * @return  all instances of {@link SearchType} available via
     *          {@link Lookup}
     */
    static Collection getSearchTypes() {
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
        Set allClasses = getSearchTypes0().allClasses();
        for (Iterator i = allClasses.iterator(); i.hasNext(); ) {
            Class c = (Class) i.next();
            if (c.getName().equals(className)) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Sorts search criteria by search types. Constructs a table which contains
     * pairs
     * <blockquote>
     * (search type, saved criteria for the search type)
     * </blockquote>
     *
     * @param  criteria  criteria to sort
     * @return  map with search type class names as keys and collections of
     *          search criteria as values; or <code>null</code> if no search
     *          criterion is saved
     */
    static Map sortCriteriaBySearchType(SearchCriterion[] criteria) {
        
        if (criteria == null || criteria.length == 0) {
            return null;
        }
        
        Map map = new HashMap(6, 0.75f);
        for (int i = 0; i < criteria.length; i++) {
            SearchCriterion c = criteria[i];
            String className = c.searchTypeClassName;
            Collection criteriaOfType;
            Object o = map.get(className);
            if (o == null) {
                criteriaOfType = new ArrayList(4);
                criteriaOfType.add(c);
                map.put(className, criteriaOfType);
            } else {
                criteriaOfType = (Collection) o;
                criteriaOfType.add(c);
            }
        }
        return map;
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
    static List cloneSearchTypes(List searchTypes) {
        List clonedSearchTypes = new ArrayList(searchTypes.size());
        for (Iterator it = searchTypes.iterator(); it.hasNext(); ) {
            clonedSearchTypes.add(((SearchType) it.next()).clone());
        }
        return clonedSearchTypes;
    }
    
    
}
