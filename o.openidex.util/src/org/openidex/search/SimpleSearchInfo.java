/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.openide.loaders.DataObject;
import org.openidex.search.SearchInfo;

/**
 * Simple implementation of interface <code>SearchInfo</code>.
 * This implementation is supposed to also serve as a building block for
 * creating more complex <code>SearchInfo</code> objects.
 * <p>
 * In its simple form, it provides an iterator for searching a
 * <code>DataObject</code> container. More complex <code>SearchInfo</code>
 * objects may be built by nesting another <code>SearchInfo</code> objects
 * or individual <code>DataObject</code>s.
 * <p>
 * Iterator returned by method {@link #objectsToSearch objectsToSearch()}
 * iterates through the contained <code>DataObject</code>s, ordered according
 * the following rules:
 * <ul>
 *     <li>
 *         If a <code>DataObject</code> container has been passed
 *         to the constructor, <code>DataObject</code>s contained
 *         in the container are returned first. If there are
 *         <code>DataObject</code> containers among the
 *         <code>DataObject</code>s, they are processed recursively, using
 *         depth-search, unless recursiveness was disabled by the second
 *         argument of the two-arguments constructor.
 *     </li>
 *     <li>After all <code>DataObject</code>s from the container
 *         passed to the constructor are processed, objects added by method
 *         add(...) are processed in the order they were added to this
 *         <code>SimpleSearchInfo</code> object.
 *         <code>SearchInfo</code> objects added by method
 *         <code>add(SearchInfo)</code> are always processed recursively, using
 *         depth-search.
 *         <code>DataObject</code>s added by method <code>add(DataObject)</code>
 *         are always processed <em>non-recursively</em>, i.e. only the single
 *         <code>DataObject</code> is processed. If the <code>DataObject</code>
 *         argument is a container and should be processed recursively,
 *         it must be wrapped into another instance
 *         of <code>SimpleDataObject</code> and then added to this instance
 *         using method <code>add(SearchInfo)</code>.
 *     </li>
 * </ul>
 *
 * @see  #add(SearchInfo)  add(SearchInfo)
 * @see  #add(DataObject)  add(DataObject)
 * @author  Marian Petras
 */
public class SimpleSearchInfo implements SearchInfo {

    /**
     * Empty search info object.
     * Its method {@link SearchInfo#canSearch canSearch()}
     * always returns <code>true</code>. Its iterator
     * (returned by method
     * {@link SearchInfo#objectsToSearch objectsToSearch()}) has no elements.
     */
    public static final SearchInfo EMPTY_SEARCH_INFO
        = new SearchInfo() {
            public boolean canSearch() {
                return true;
            }
            public Iterator objectsToSearch() {
                return Collections.EMPTY_LIST.iterator();
            }
        };

    /**
     */
    private DataObject.Container contentsBase;

    /**
     */
    private List extraContents;

    /**
     */
    private boolean recursive;
    
    /**
     * Creates an empty <code>SearchInfo</code> object.
     * This empty object may be then expanded by nesting another
     * <code>SearchInfo</code> objects or individual <code>DataObject</code>s.
     */
    public SimpleSearchInfo() {
    }
    
    /**
     * Creates a <code>SearchInfo</code> object for searching a single
     * <code>DataObject</code> container recursively.
     *
     * @param  container  container of <code>DataObject</code>s to be searched
     */
    public SimpleSearchInfo(DataObject.Container container) {
        this(container, true);
    }
    
    /**
     * Creates a <code>SearchInfo</code> object for searching a single
     * <code>DataObject</code> container recursively or non-recursively.
     *
     * @param  container  container of <code>DataObject</code>s to be searched
     * @param  recursive  whether the objects should be processed recursively
     */
    public SimpleSearchInfo(DataObject.Container container, boolean recursive) {
        contentsBase = container;
        this.recursive = recursive;
    }
    
    /**
     * Adds a single <code>DataObject</code> to the group of objects to be
     * searched.
     *
     * @param  dataObject  <code>DataObject</code> to be searched
     */
    public void add(DataObject dataObject) {
        if (extraContents == null) {
            extraContents = new ArrayList(4);
        }
        extraContents.add(dataObject);
    }
    
    /**
     * Nests another <code>SearchInfo</code> object to this object.
     * The nested <code>SearchInfo</code> will be processed recursively.
     *
     * @param  nestedInfo  <code>SearchInfo</code> object to be nested
     */
    public void add(SearchInfo nestedInfo) {
        if (extraContents == null) {
            extraContents = new ArrayList(4);
        }
        extraContents.add(nestedInfo);
    }

    /**
     * Removes a single <code>DataObject</code> from the group of objects to be
     * searched. Only objects added by method
     * {@link #add(DataObject) add(DataObject)} may be removed.
     * If a <code>DataObject</code> which was not added by the
     * <code>add(DataObject)</code> method is passed (or if it has already been
     * removed since then), the call of this method has no effect.
     *
     * @param  dataObject  <code>DataObject</code> to be removed
     */
    public void remove(DataObject dataObject) {
        if (extraContents != null) {
            extraContents.remove(dataObject);
            if (extraContents.isEmpty()) {
                extraContents = null;
            }
        }
    }

    /**
     * Removes a <code>SearchInfo</code> object previously nested to this
     * object. Only objects added by method
     * {@link #add(SearchInfo) add(SearchInfo)} may be removed.
     * If a <code>SearchInfo</code> object which was not added by the
     * <code>add(SearchInfo)</code> method is passed (or if it has already been
     * removed since then), the call of this method has no effect.
     *
     * @param  nestedInfo  <code>SearchInfo</code> object to be removed
     */
    public void remove(SearchInfo nestedInfo) {
        if (extraContents != null) {
            extraContents.remove(nestedInfo);
            if (extraContents.isEmpty()) {
                extraContents = null;
            }
        }
    }

    /**
     *
     * @return  <code>true</code> if this <code>SimpleSearchInfo</code>
     *          is not empty - i.e. if a <code>DataObject</code> container has
     *          been passed to the constructor or if at least one
     *          <code>DataObject</code> or <code>SearchInfo</code> object
     *          was added using either of the two <code>add(...)</code>
     *          methods (and not removed since then);
     *          <code>false</code> otherwise
     */
    public boolean canSearch() {
        return (contentsBase != null) || (extraContents != null);
    }

    /**
     *
     * @return  iterator iterating over all <code>DataObject</code> directly
     *          or indirectly added to this <code>SearchInfo</code> object
     */
    public Iterator objectsToSearch() {

        /* make a copy of contents base: */
        DataObject[] base = null;
        if (contentsBase != null) {
            base = contentsBase.getChildren();
        }

        /* compute the total size of contents: */
        int totalSize = 0;
        if (base != null) {
            totalSize += base.length;
        }
        if (extraContents != null) {
            totalSize += extraContents.size();
        }

        /* return an empty iterator if the search info is empty: */
        if (totalSize == 0) {
            return Collections.EMPTY_LIST.iterator();
        }

        return new SimpleSearchInfoIterator(
                base,
                extraContents != null
                        ? Collections.unmodifiableList(extraContents)
                        : null,
                recursive);
    }
    
    static class SimpleSearchInfoIterator implements Iterator {

        /** */
        DataObject[] contentsBase;
        /** */
        List extraContents;
        /** */
        int contentsBaseIndex = 0;
        /** */
        int contentsBaseSize = 0;
        /** */
        Iterator nestedIterator;
        /** */
        ListIterator extraIterator;

        /**
         * should <code>DataObject.Container</code>s present
         * in the {@link #contentsBase} be handled recursively?
         */
        boolean recursive;

        /**
         */
        SimpleSearchInfoIterator(DataObject[] contentsBase,
                                 List extraContents,
                                 boolean recursive) {
            this.contentsBase = contentsBase;
            this.contentsBaseSize = contentsBase != null ? contentsBase.length
                                                         : 0;
            this.extraContents = extraContents;
            this.recursive = recursive;
        }
        
        /**
         * Checks whether the nested iterator has next element and deletes
         * the iterator if not.
         * This method ensures that:
         * <ul>
         *     <li>either <code>true</code> is returned</li>
         *     <li>or the nested iterator is <code>null</code> upon return
         * </ul>
         *
         * @return  <code>true</code> if the nested iterator contains
         *          at least one more element; <code>false</code> otherwise
         */
        private boolean checkNestedIterator() {
            assert nestedIterator != null;

            if (nestedIterator.hasNext()) {
                return true;
            } else {
                nestedIterator = null;
                return false;
            }
        }
        
        /**
         */
        public boolean hasNext() {

            if (nestedIterator != null && checkNestedIterator()) {
                return true;
            }
            
            /* Look through the contents base for any element: */
            while (contentsBaseIndex < contentsBaseSize) {
                Object dataObject = contentsBase[contentsBaseIndex];
                if (recursive && (dataObject instanceof DataObject.Container)) {
                    contentsBaseIndex++;
                    nestedIterator = new DataObjectContainerIterator(
                            (DataObject.Container) dataObject);
                    if (checkNestedIterator()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }

            /*
             * Contents base is now exhausted. Let's have a look at extra
             * contents...
             */

            /*
             * Check if there is some extra contents and create an iterator for
             * it if it does not exist yet:
             */
            if (extraIterator == null) {
                if (extraContents == null || extraContents.isEmpty()) {
                    return false;
                } else {
                    extraIterator = extraContents.listIterator();
                }
            }

            /* Look through the extra contents for any element: */
            while (extraIterator.hasNext()) {
                Object o = extraIterator.next();
                if (o instanceof SearchInfo) {
                    nestedIterator = ((SearchInfo) o).objectsToSearch();
                    if (checkNestedIterator()) {
                        return true;
                    }
                } else {
                    
                    /*
                     * make sure the next call to Iterator.next() returns
                     * the same element:
                     */
                    extraIterator.previous();
                    return true;
                }
            }
            extraIterator = null;

            /*
             * We found no element in neither contents base nor in extra
             * contents:
             */
            return false;
        }

        /**
         */
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (nestedIterator != null) {

                /*
                 * We know that the nested iterator has next - see method
                 * checkNestedIterator().
                 */
                return nestedIterator.next();
            }

            if (contentsBaseIndex < contentsBaseSize) {

                /*
                 * We know that the next element is not a container to be
                 * processed recursively - nestedIterator would be non-null
                 * in such a case - see method checkNestedIterator().
                 */
                return contentsBase[contentsBaseIndex++];
            }

            /*
             * Method hasNext() ensures that extraIterator is non-null
             * if and only if extra contents exists and has at least
             * one next element.
             */
            return extraIterator.next();
        }
        
        /**
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    /**
     *
     */
    static class DataObjectContainerIterator implements Iterator {

        /** */
        private DataObject[] contents;

        /** */
        private int index = 0;

        /**
         */
        private Iterator nestedIterator;

        /**
         */
        DataObjectContainerIterator(DataObject.Container container) {
            contents = container.getChildren();
            if ((contents != null) && (contents.length == 0)) {
                contents = null;
            }
        }

        /**
         */
        private boolean checkNestedIterator() {
            assert nestedIterator != null;

            if (nestedIterator.hasNext()) {
                return true;
            } else {
                nestedIterator = null;
                return false;
            }
        }
        
        /**
         */
        public boolean hasNext() {
            if (contents == null) {
                return false;
            }

            if (nestedIterator != null && checkNestedIterator()) {
                return true;
            }

            while (index < contents.length) {
                Object dataObject = contents[index];
                if (dataObject instanceof DataObject.Container) {
                    index++;
                    nestedIterator = new DataObjectContainerIterator(
                            (DataObject.Container) dataObject);
                    if (checkNestedIterator()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }

            return false;
        }

        /**
         */
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (nestedIterator != null) {
                return nestedIterator.next();
            }
            return contents[index++];
        }
        
        /**
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
