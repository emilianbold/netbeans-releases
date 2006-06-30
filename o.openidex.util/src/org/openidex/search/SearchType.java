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


package org.openidex.search;

import java.util.Collections;
import java.util.Enumeration;

import org.openide.ServiceType;
import org.openide.nodes.Node;
import org.openide.util.Lookup;


/**
 * Search type is service which provides search functionality on set of nodes.
 * It has to provide GUI presentation so user can have the possibility to
 * set/modify criteria.
 * It performs search according to that.
 *
 * @author  Peter Zavadsky
 */
public abstract class SearchType extends ServiceType implements Cloneable {

    /** Serial version UID. */ // PENDING How to change this silly number? Can be done by using Utilities.translate
    static final long serialVersionUID = 1L;
    
    /** Name of valid property. */
    public static final String PROP_VALID = "valid"; // NOI18N
    
    /** Name of object changed property. */
    protected static final String PROP_OBJECT_CHANGED = "org.openidex.search.objectChanged"; // NOI18N
    
    /** Property valid. */
    private boolean valid;

    
    /** Class types of object on which this search type is able to search. */
    private Class[] searchTypeClasses;
    

    /**
     * Gets class types of objects this search type can search (test) on.
     * The classes are used for associating search types working on the same
     * object types to create <code>SearchGroup</code>. 
     * <em>Note: </em> the order of classes declares also priority.
     */
    public synchronized final Class[] getSearchTypeClasses() {
        if (searchTypeClasses == null) {
            searchTypeClasses = createSearchTypeClasses();
        }
        return searchTypeClasses;
    }

    /**
     * Actually creates array of class types of objects this search type can search.
     * <em>Note: </em> the order of classes declares also priority.
     */
    protected abstract Class[] createSearchTypeClasses();
    

    /**
     * Accepts search root nodes. Subclasses have a chance to exclude some of
     * the non interesting node systems. E.g. CVS search type can exclude non
     * CVS node systems.
     */
    protected Node[] acceptSearchRootNodes(Node[] roots) {
        return roots;
    }

    /**
     * Accepts search object to the search. Subclasses have a chance to exclude
     * the non interesting objects from the search. E.g. Java search type will
     * exclude non Java data objects.
     * <em>Note:</em> the search object instance is of the class type
     * returned by SearchKey.getSearchObjectType method. So there is no necessity
     * to do additional check for that search type. 
     * @return <code>true</code> */
    protected boolean acceptSearchObject(Object searchObject) {
        return true;
    }

    /**
     * Prepares search object. Dummy implementation.
     */
    protected void prepareSearchObject(Object searchObject) {}
    
    /**
     * Checks whether an object matches the criteria defined in this search
     * type.
     *
     * @param  searchObject  object to be tested
     * @return  <code>true</code> if the object matches the criteria,
     *          <code>false</code> it it does not
     */
    protected abstract boolean testObject(Object searchObject);

    /**
     * Creates nodes representing matches found within the specified object.
     * <p>
     * This is a dummy implementation, subclasses should provide a real
     * implementation.
     *
     * @param  resultObject  object to create the nodes for
     * @return  <code>null</code> (subclasses should return the created nodes)
     */
    public Node[] getDetails(Object resultObject) {
        return null;
    }
    
    /**
     * Creates nodes representing matches found withing an object
     * represented by the specified node.
     * <p>
     * This is a dummy implementation, subclasses should provide a real
     * implementation. The typical implementation is that the node is validated,
     * an object is extracted from it and passed to method
     * {@link #getDetails(Object)}.
     *
     * @param  node  node representing object with matches
     * @return <code>null</code> (subclasses should return the created nodes)
     * @see  #getDetails(Object)
     */
    public Node[] getDetails(Node node) {
        return null;
    }

    /**
     * Checks that this search type is able to search the specified set
     * of nodes.
     * <p>
     * This method is usually implemented such that it returns <code>true</code>
     * if it is possible to search at least one of the nodes.
     *
     * @param  nodes  nodes to be searched
     * @return  <code>true</code> if this search type is able to search
     *          the nodes, <code>false</code> otherwise
     */
    public abstract boolean enabled(Node[] nodes);
    
    /** Now the custonized criterion changed validity state. */
    public final void setValid(boolean state) {
        boolean old = valid;
        valid = state;
        firePropertyChange(PROP_VALID, old ? Boolean.TRUE : Boolean.FALSE, state ? Boolean.TRUE : Boolean.FALSE);
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


    /**
     * Enumeration of all SearchTypes in the system.
     *
     * @return enumeration of SearchType instances
     * @deprecated Please use {@link Lookup} instead.
     */
    public static Enumeration enumerateSearchTypes () {
        return Collections.enumeration(Lookup.getDefault().lookup(new Lookup.Template(SearchType.class)).allInstances());
    }
    
}
