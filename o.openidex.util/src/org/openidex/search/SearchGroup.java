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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.WeakListener;


/**
 * Class which groups individual search types. It provides several services
 * to provide search on them. The services are scanning node system to 
 * provide search object for group of search types -> efficient search.
 *
 * @author  Peter Zavadsky
 */
public abstract class SearchGroup extends Object {

    /** Property name which is fired when performing search and searched object 
     * passed criteria. */
    public static final String PROP_FOUND = "org.openidex.search.found"; // NOI18N

    /** Property name which is fired for in for the case original <code>node</code>'s has
     * changed the way <code>result</code> was changed based on set criteria.
     * Interested listeners should then get the event with values 
     * <UL>
     * <LI>property change name = PROP_RESULT
     * <LI>property source = this search type instance
     * <LI>old value = detail which was changed or <code>null</code> there wasn't before for the node -> new value has to be non-null
     * for the latter case.
     * <LI>new value = detail which was changed or null if the node was removed from the result -> old value has to be non-null
     * for that case
     * </UL>
     * This allows implementation of the dynamic changing of result suggested
     * by Jesse and Sebastian (at least partially implemented now).
     * */
    public static final String PROP_RESULT = "org.openidex.search.result"; // NOI18N
    
    
    /** Property change support. */
    private PropertyChangeSupport propChangeSupport;

    /** Search types added to this search group. */
    protected SearchType[] searchTypes = new SearchType[0];

    /** Set of nodes on which sub-system to search. */
    protected final Set searchRoots = new HashSet(5);
    
    /** Objects on which to search. Their types has to be one of those returned
     * by getSearchObjectClasses method. Can't contain <code>null</code>.
     * <em>Note:</em> these are objects which could be listen to if their change affect
     * the result -> in that case the property change with PROP_RESULT name has to be fired
     * to interested listeners. */
    protected final Set searchObjects = new HashSet(50);

    /** Set of objects which passed the serach. It's usbset of searchObjects. */
    protected final Set resultObjects = new HashSet(50);

    /** Flag indicating the search should be stopped. */
    protected boolean stopped = false;

    private PropertyChangeListener propListener;   


    /** Adds item to this search group. Implements superclass abstract method.
     * @param item item which has to be of <code>SearchType</code> type
     * to be added. */
    protected void add(SearchType item) {
        List list = new ArrayList(Arrays.asList(searchTypes));
        
        if(!list.contains(item))
            list.add(item);
        
        searchTypes = (SearchType[])list.toArray(new SearchType[list.size()]);
    }

    /** Gets list of search types in this group. */
    public SearchType[] getSearchTypes() {
        return searchTypes;
    }
    
    /** Sets roots of nodes in which its interested to search. 
     * This method is called at the first search type in the possible crated chain of search types. */
    public void setSearchRootNodes(Node[] roots) {
        // Gives a chance for individual search types to exclude some
        // node systems. E.g. CVS search type is not interested
        // in non CVS node systems.
        for(int i = 0; i < searchTypes.length; i++) {
            roots = searchTypes[i].acceptSearchRootNodes(roots);
        }
        
        searchRoots.clear();
        searchRoots.addAll(Arrays.asList(roots));
    }

    /** Gets search root nodes.  */
    public Node[] getSearchRoots() {
        return (Node[])searchRoots.toArray(new Node[searchRoots.size()]);
    }
    
    /** Stops searching. */
    public final void stopSearch() {
        stopped = true;
    }

    /** Does search. */
    public void search() {
        prepareSearch();
        
        doSearch();
    }

    /** Prepares search. Set listerners to individual search types.
     * If some of the underlying search types fires SearchType.PROP_OBJECT proeprty change,
     * the object is examined again and if could affect a result it is fired PROP_RESULT
     * property change.
     * */
    protected void prepareSearch() {
        for(int i = 0; i < searchTypes.length; i++) {
            searchTypes[i].addPropertyChangeListener(
                WeakListener.propertyChange(getSearchTypeListener(), searchTypes[i])
            );
        }
    }

    /** Gets listener which listens on all search types if some searched object was changed
     * the way it should reflect search result. */
    private synchronized PropertyChangeListener getSearchTypeListener() {
        if(propListener == null) {
            propListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(SearchType.PROP_OBJECT_CHANGED.equals(evt.getPropertyName())) {
                        Object searchObject = evt.getNewValue();
                        for(int i = 0; i < searchTypes.length; i++) {
                            if(!searchTypes[i].testObject(searchObject)) {
                                // Search object didn't satisfied the criteria.
                                if(resultObjects.remove(searchObject)) {
                                    firePropertyChange(PROP_RESULT, searchObject, null);
                                }

                                return;
                            }
                        }

                        // Search object satisfied the criteria.
                        if(resultObjects.add(searchObject))
                            firePropertyChange(PROP_RESULT, null, searchObject);
                    }
                }
            };
        }
        
        return propListener;
    }
    
    /** Provides actual search. The subclasses implmentating this method should scan the node system
     * specified by <code>searchRoots</code>, extract search objects from them, add them
     * to the search object set, test over all search type items in this group,
     * in case if sastisfied all it should fire <code>PROP_FOUND</code> property change and add
     * the object to <code>resultObjects</code> set.
     * The method implemenatation should call {@link #processSearchObject} method for each
     * search object in the node systems. */
    protected abstract void doSearch();

    /** Provides search on one search object instance. The object is added to
     * set of searched objects and passed to all search types encapsulated by
     * this search group. In the case the object passes all search types is added
     * to the result set and fired an event <code>PROP_FOUND</code> about succesful
     * match to interested property change listeners. 
     *
     * @param searchObject object to provide actuall test on it. The actual instance
     * has to be of type returned by all <code>SearachKey.getSearchObjectType</code>
     * returned by <code>SearchType</code> of this <code>SearchGroup</code> */
    protected void processSearchObject(Object searchObject) {
        // Give chance to individual search types to exclude some
        // non interesting search objects from search. E.g. Java data
        // object search will be not interested in non Java data objects. 
        for(int i = 0; i < searchTypes.length; i++) {
            if(!searchTypes[i].acceptSearchObject(searchObject)) {
                return;
            }
        }

        // Add search object to the set og searched set.
        searchObjects.add(searchObject);

        // Give chance to provide additional things to have possibility
        // to listen on changes on object to provide dynamic search result.
        for(int i = 0; i < searchTypes.length; i++) {
            searchTypes[i].prepareSearchObject(searchObject);
        }
        
        // Actually test the search object against all search types.
        for(int i = 0; i < searchTypes.length; i++) {
            if(!searchTypes[i].testObject(searchObject)) {
                return;
            }
        }

        // In case the search object passed the search add it to the result set
        // and fire an evet about successfull search to intersted listeners.
        resultObjects.add(searchObject);
        firePropertyChange(PROP_FOUND, null, searchObject);
    }

    
    /** Gets node for found object. */
    public abstract Node getNodeForFoundObject(Object object);

    /** Getter for result object property. */
    public Set getResultObjects() {
        return new HashSet(resultObjects);
    }

    /** Adds property change listener. */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        getPropertySupport().addPropertyChangeListener(l);
    }
    
    /** Removes property change listener. */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        getPropertySupport().removePropertyChangeListener(l);
    }

    /** Fires property change event. */
    protected void firePropertyChange(String name, Object oldValue, Object newValue) {
        getPropertySupport().firePropertyChange(name, oldValue, newValue);
    }
    
    /** Gets lazy initialized property change support. */
    private synchronized PropertyChangeSupport getPropertySupport() {
        if(propChangeSupport == null)
            propChangeSupport = new PropertyChangeSupport(this);
        
        return propChangeSupport;
    }

    /** Creates search groups which includes all provided search types. */
    public static SearchGroup[] createSearchGroups(SearchType[] items) {
        Set classSet = new HashSet(items.length);
        
        for(int i = 0; i < items.length; i++) {
            List classes = Arrays.asList(items[i].getSearchTypeClasses());
            
            if(i == 0) {
                classSet.addAll(classes);
            } else {
                classSet.retainAll(classes);
            }
        }
        
        if(classSet.isEmpty())
            return new SearchGroup[0];
        
        Set groupSet = new HashSet(classSet.size());
        
        for(Iterator it = classSet.iterator(); it.hasNext(); ) {
            SearchGroup group = Registry.createSearchGroup((Class)it.next());

            if(group == null)
                continue;
            
            for(int i = 0; i < items.length; i++) {
                group.add(items[i]);
            }
            
            groupSet.add(group);
        }
        
        return (SearchGroup[])groupSet.toArray(new SearchGroup[groupSet.size()]);
    }


    /** Factory which creates <code>SearchGroup</code>. It's used in
     * <code>Registry</code> 
     * @see Registry */
    public interface Factory {
        /** Creates new <code>SearchGroup</code> object. */
        public SearchGroup createSearchGroup();
    } // End of interface Factory.

    
    /** Registry which registers search group factories {@link Factory} for
     * search object class type. There is a default factory registered for
     * <code>DataObject</code> and <code>FileObject</code> class types.
     * @see Factory
     * @see DataObjectSearchGroup
     * @see FileObjectSearchGroup */
    public static final class Registry extends Object {

        /** Private constructor so nobody could acces it. */
        private Registry() {}
        
        
        /** Registry of SearchGroup factories search type class -> search group factory. */
        private static final Map registry = new HashMap(2);

        static {
            registry.put(DataObject.class, new Factory() {
                public SearchGroup createSearchGroup() {
                    return new DataObjectSearchGroup();
                }
            });
            
            registry.put(FileObject.class, new Factory() {
                public SearchGroup createSearchGroup() {
                    return new FileObjectSearchGroup();
                }
            });
        }

        
        /** Registers <code>Factory</code> for search object class type.
         * @return <code>true</code> if the registration was successful
         * of <code>false</code> if there is already factory for the search object 
         * class type or if the registration was not successful */
        public static synchronized boolean registerSearchGroupFactory(Class searchObjectClass, Factory factory) {
            // PENDING maybe more sophisticated method would be needed later.
            if(registry.containsKey(searchObjectClass))
                return false;
            
            registry.put(searchObjectClass, factory);
            return true;
        }
        
        /** Creates <code>SearchGroup</code> for search object class type or <code>null</code>
         * if there is no factory for the specified class type. */
        public static SearchGroup createSearchGroup(Class searchObjectType) {
            Factory factory = (Factory)registry.get(searchObjectType);
            
            if(factory == null) 
                return null;
            
            return factory.createSearchGroup();
        }

        /** Tests whether there is a <code>Factory</code> registered for the specified
         * search object class type. */
        public static boolean hasFactory(Class searchTypeClass) {
            return registry.containsKey(searchTypeClass);
        }
        
    } // End of class Registry.
   
}
