/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openidex.search;


import java.util.Enumeration;

import org.openide.nodes.Node;
import org.openide.ServiceType;
import org.openide.TopManager;


/**
 * Search type is sevice which provides serch functionality on set of nodes. 
 * It has to provide GUI presentation so user can have the possibility to
 * set/moify criteria.
 * It performs search according to that.
 * And additionaly could provide feature of dynamic change of result for cases 
 * the original nodes were changed the way it affect the result of search.
 *
 * @author  Peter Zavadsky
 */
public abstract class SearchType extends ServiceType implements Cloneable {

    /** Serial version UID. */ // PENDING How to change this silly number?
    static final long serialVersionUID = 1L;
    
    /** Name of valid property. */
    public static final String PROP_VALID = "org.openidex.search.valid"; // NOI18N
    
    /** Name of object changed property. */
    protected static final String PROP_OBJECT_CHANGED = "org.openidex.search.objectChanged"; // NOI18N
    
    /** Property valid. */
    private boolean valid;

    
    /** Class types of object on which this search type is able to search. */
    private Class[] searchTypeClasses;
    

    /** Gets class types of objects this search type can search (test) on.
     * The classes are used for associating search types working on the same
     * object types to create <code>SearchGroup</code>. 
     * <em>Note: </em> the order of classes declares also priority. */
    public synchronized final Class[] getSearchTypeClasses() {
        if(searchTypeClasses == null)
            searchTypeClasses = createSearchTypeClasses();
        
        return searchTypeClasses;
    }

    /** Actually creates array of class types of objects this search type can search.
     * <em>Note: </em> the order of classes declares also priority. */
    protected abstract Class[] createSearchTypeClasses();
    

    /** Accepts search root nodes. Subclasses have a chance to exclude some of
     * the non interesting node systems. E.g. CVS search type can exclude non
     * CVS node systems. */
    protected Node[] acceptSearchRootNodes(Node[] roots) {
        return roots;
    }

    /** Accepts search object to the search. Subclasses have a chance to excluide
     * the non interesting objects from the search. E.g. Java search type will
     * exclude non Java data objects.
     * <em>Note:</em> the search object instance is of the class type
     * returned by SearchKey.getSearchObjectType method. So there is no necessity
     * to do additional check for that search type. 
     * @return <code>true</code> */
    protected boolean acceptSearchObject(Object searchObject) {
        return true;
    }

    /** Prepares search object. Dummy implementation. Gives a chance to subclasses to 
     * provide changes for dynamic changes on search objects.
     * Typically it has to add some listener at the object and in the case some change
     * which could lead to a change of the search result should fire 
     * PROP_OBJECT_CHANGED property change. */
    protected void prepareSearchObject(Object searchObject) {}
    
    /** Provides actual search processing of one node based on concrete search type
     * criteria.
     * @return <code>true</code> if the node satisfies criteria of this search type
     * or <code>false</code> if doesn't */
    protected abstract boolean testObject(Object searchObject);

    /** Gets details for object satisfied the search.
     * Subclasses should override the method to get detail nodes for the
     * specified result object.
     *
     * @param resultObject object which has satisfied this search type
     * @return <code>null</code> */
    public Node[] getDetails(Object resultObject) {
        return null;
    }
    
    /** Gets details for node which represents one result node.
     * Subclasses should override the method to get detail nodes for the
     * specified result object.
     *
     * @param node which represents object which has satisfied this search type
     * @return <code>null</code> */
    public Node[] getDetails(Node node) {
        return null;
    }
    
    /** Tests whether the search type is enabled on certain nodes. */
    public abstract boolean enabled(Node[] nodes);
    
    /** Now the custonized criterion changed validity state. */
    public final void setValid(boolean state) {
        boolean old = valid;
        valid = state;
        firePropertyChange(PROP_VALID, new Boolean(old), new Boolean(state));
    }

    /** @return true if the criterion is currently valid. */
    public final boolean isValid() {
        return valid;
    }
    
    /** Clones seach type. */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("SearchType must be cloneable."); // NOI18N
        }
    }


    /** Enumeration of all SearchTypes in the system.
     * @return enumeration of SearchType instances */
    public static Enumeration enumerateSearchTypes () {
        return TopManager.getDefault().getServices().services(SearchType.class);
    }
    
}
