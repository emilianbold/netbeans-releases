/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.util;

import java.util.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;


/**
 * Factory methods for various types of {@link Enumeration}.
 * Allows composition of existing enumerations, filtering their contents, and/or modifying them.
 * All of this is designed to be done lazily, i.e. elements created on demand.
 * @since 4.37
 * @author Jaroslav Tulach
 */
public final class Enumerations extends Object {
    /** No instances */
    private Enumerations() {
    }

    /**
     * An empty enumeration.
     * Always returns <code>false</code> from
     * <code>empty().hasMoreElements()</code> and throws <code>NoSuchElementException</code>
     * from <code>empty().nextElement()</code>.
     * @return the enumeration
     */
    public static final Enumeration empty() {
        return Collections.enumeration(Collections.EMPTY_LIST);
    }

    /**
     * Creates an enumeration with one element.
     * @param obj the element to be present in the enumeration.
     * @return enumeration
     */
    public static Enumeration singleton(Object obj) {
        return Collections.enumeration(Collections.singleton(obj));
    }

    /**
     * Concatenates the content of two enumerations into one.
     * Until the
     * end of <code>en1</code> is reached its elements are being served.
     * As soon as the <code>en1</code> has no more elements, the content
     * of <code>en2</code> is being returned.
     *
     * @param en1 first enumeration
     * @param en2 second enumeration
     * @return enumeration
     */
    public static Enumeration concat(Enumeration en1, Enumeration en2) {
        return new SeqEn(en1, en2);
    }

    /**
     * Concatenates the content of many enumerations.
     * The input value
     * is enumeration of Enumeration elements and the result is composed
     * all their content. Each of the provided enumeration is fully read
     * and their content returned before the next enumeration is asked for
     * their elements.
     *
     * @param enumOfEnums Enumeration of Enumeration elements
     * @return enumeration
     */
    public static Enumeration concat(Enumeration enumOfEnums) {
        return new SeqEn(enumOfEnums);
    }

    /**
     * Filters the input enumeration to new one that should contain
     * each of the provided elements just once.
     * The elements are compared
     * using their default <code>equals</code> and <code>hashCode</code> methods.
     *
     * @param en enumeration to filter
     * @return enumeration without duplicated items
     */
    public static Enumeration removeDuplicates(Enumeration en) {
        class RDupls implements Processor {
            private Set set = new HashSet();

            public Object process(Object o, Collection nothing) {
                return set.add(o) ? o : null;
            }
        }

        return filter(en, new RDupls());
    }

    /**
     * Returns an enumeration that iterates over provided array.
     * @param arr the array of object
     * @return enumeration of those objects
     */
    public static Enumeration array(Object[] arr) {
        return Collections.enumeration(Arrays.asList(arr));
    }

    /**
     * Removes all <code>null</code>s from the input enumeration.
     * @param en enumeration that can contain nulls
     * @return new enumeration without null values
     */
    public static Enumeration removeNulls(Enumeration en) {
        return filter(en, new RNulls());
    }

    /**
     * For each element of the input enumeration <code>en</code> asks the
     * {@link Processor} to provide a replacement.
     * The <code>toAdd</code> argument of the processor is always null.
     * <p>
     * Example to convert any objects into strings:
     * <pre>
     * Processor convertToString = new Processor() {
     *     public Object process(Object obj, Collection alwaysNull) {
     *         return obj.toString(); // converts to string
     *     }
     * };
     * Enumeration strings = Enumerations.convert(elems, convertToString);
     * </pre>
     *
     * @param en enumeration of any objects
     * @param processor a callback processor for the elements (its toAdd arguments is always null)
     * @return new enumeration where all elements has been processed
     */
    public static Enumeration convert(Enumeration en, Processor processor) {
        return new AltEn(en, processor);
    }

    /**
     * Filters some elements out from the input enumeration.
     * Just make the
     * {@link Processor} return <code>null</code>. Please notice the <code>toAdd</code>
     * argument of the processor is always <code>null</code>.
     * <p>
     * Example to remove all objects that are not strings:
     * <pre>
     * Processor onlyString = new Processor() {
     *     public Object process(Object obj, Collection alwaysNull) {
     *         if (obj instanceof String) {
     *             return obj;
     *         } else {
     *             return null;
     *         }
     *     }
     * };
     * Enumeration strings = Enumerations.filter(elems, onlyString);
     * </pre>
     *
     * @param en enumeration of any objects
     * @param filter a callback processor for the elements (its toAdd arguments is always null)
     * @return new enumeration which does not include non-processed (returned null from processor) elements
     */
    public static Enumeration filter(Enumeration en, Processor filter) {
        return new FilEn(en, filter);
    }

    /**
     * Support for breadth-first enumerating.
     * Before any element is returned
     * for the resulting enumeration it is processed in the {@link Processor} and
     * the processor is allowed to modify it and also add additional elements
     * at the (current) end of the <q>queue</q> by calling <code>toAdd.add</code>
     * or <code>toAdd.addAll</code>. No other methods can be called on the
     * provided <code>toAdd</code> collection.
     * <p>
     * Example of doing breadth-first walk through a tree:
     * <pre>
     * Processor queueSubnodes = new Processor() {
     *     public Object process(Object obj, Collection toAdd) {
     *         Node n = (Node)obj;
     *         toAdd.addAll (n.getChildrenList());
     *         return n;
     *     }
     * };
     * Enumeration strings = Enumerations.queue(elems, queueSubnodes);
     * </pre>
     *
     * @param en initial content of the resulting enumeration
     * @param filter the processor that is called for each element and can
     *        add and addAll elements to its toAdd Collection argument and
     *        also change the value to be returned
     * @return enumeration with the initial and queued content (it can contain
     *       <code>null</code> if the filter returned <code>null</code> from its
     *       {@link Processor#process} method.
     */
    public static Enumeration queue(Enumeration en, Processor filter) {
        QEn q = new QEn(filter);

        while (en.hasMoreElements()) {
            q.put(en.nextElement());
        }

        return q;
    }

    /**
     * Processor interface that can filter out objects from the enumeration,
     * change them or add aditional objects to the end of the current enumeration.
     */
    public static interface Processor {
        /** @param original the object that is going to be returned from the enumeration right now
         * @return a replacement for this object
         * @param toAdd can be non-null if one can add new objects at the end of the enumeration
         */
        public Object process(Object original, Collection toAdd);
    }

    /** Altering enumeration implementation */
    private static final class AltEn extends Object implements Enumeration {
        /** enumeration to filter */
        private Enumeration en;

        /** map to alter */
        private Processor process;

        /**
        * @param en enumeration to filter
        */
        public AltEn(Enumeration en, Processor process) {
            this.en = en;
            this.process = process;
        }

        /** @return true if there is more elements in the enumeration
        */
        public boolean hasMoreElements() {
            return en.hasMoreElements();
        }

        /** @return next object in the enumeration
        * @exception NoSuchElementException can be thrown if there is no next object
        *   in the enumeration
        */
        public Object nextElement() {
            return process.process(en.nextElement(), null);
        }
    }
     // end of AltEn

    /** Sequence of enumerations */
    private static final class SeqEn extends Object implements Enumeration {
        /** enumeration of Enumerations */
        private Enumeration en;

        /** current enumeration */
        private Enumeration current;

        /** is {@link #current} up-to-date and has more elements?
        * The combination <CODE>current == null</CODE> and
        * <CODE>checked == true means there are no more elements
        * in this enumeration.
        */
        private boolean checked = false;

        /** Constructs new enumeration from already existing. The elements
        * of <CODE>en</CODE> should be also enumerations. The resulting
        * enumeration contains elements of such enumerations.
        *
        * @param en enumeration of Enumerations that should be sequenced
        */
        public SeqEn(Enumeration en) {
            this.en = en;
        }

        /** Composes two enumerations into one.
        * @param first first enumeration
        * @param second second enumeration
        */
        public SeqEn(Enumeration first, Enumeration second) {
            this(array(new Enumeration[] { first, second }));
        }

        /** Ensures that current enumeration is set. If there aren't more
        * elements in the Enumerations, sets the field <CODE>current</CODE> to null.
        */
        private void ensureCurrent() {
            while ((current == null) || !current.hasMoreElements()) {
                if (en.hasMoreElements()) {
                    current = (Enumeration) en.nextElement();
                } else {
                    // no next valid enumeration
                    current = null;

                    return;
                }
            }
        }

        /** @return true if we have more elements */
        public boolean hasMoreElements() {
            if (!checked) {
                ensureCurrent();
                checked = true;
            }

            return current != null;
        }

        /** @return next element
        * @exception NoSuchElementException if there is no next element
        */
        public Object nextElement() {
            if (!checked) {
                ensureCurrent();
            }

            if (current != null) {
                checked = false;

                return current.nextElement();
            } else {
                checked = true;
                throw new java.util.NoSuchElementException();
            }
        }
    }
     // end of SeqEn

    /** QueueEnumeration
     */
    private static class QEn extends Object implements Enumeration {
        /** next object to be returned */
        private ListItem next = null;

        /** last object in the queue */
        private ListItem last = null;

        /** processor to use */
        private Processor processor;

        public QEn(Processor p) {
            this.processor = p;
        }

        /** Put adds new object to the end of queue.
        * @param o the object to add
        */
        public void put(Object o) {
            if (last != null) {
                ListItem li = new ListItem(o);
                last.next = li;
                last = li;
            } else {
                next = last = new ListItem(o);
            }
        }

        /** Adds array of objects into the queue.
        * @param arr array of objects to put into the queue
        */
        public void put(Object[] arr) {
            for (int i = 0; i < arr.length; i++) {
                put(arr[i]);
            }
        }

        /** Is there any next object?
        * @return true if there is next object, false otherwise
        */
        public boolean hasMoreElements() {
            return next != null;
        }

        /** @return next object in enumeration
        * @exception NoSuchElementException if there is no next object
        */
        public Object nextElement() {
            if (next == null) {
                throw new NoSuchElementException();
            }

            Object res = next.object;

            if ((next = next.next) == null) {
                last = null;
            }

            ;

            ToAdd toAdd = new ToAdd(this);
            res = processor.process(res, toAdd);
            toAdd.finish();

            return res;
        }

        /** item in linked list of Objects */
        private static final class ListItem {
            Object object;
            ListItem next;

            /** @param o the object for this item */
            ListItem(Object o) {
                object = o;
            }
        }

        /** Temporary collection that supports only add and addAll operations*/
        private static final class ToAdd extends Object implements Collection {
            private QEn q;

            public ToAdd(QEn q) {
                this.q = q;
            }

            public void finish() {
                this.q = null;
            }

            public boolean add(Object o) {
                q.put(o);

                return true;
            }

            public boolean addAll(Collection c) {
                q.put(c.toArray());

                return true;
            }

            private String msg() {
                return "Only add and addAll are implemented"; // NOI18N
            }

            public void clear() {
                throw new UnsupportedOperationException(msg());
            }

            public boolean contains(Object o) {
                throw new UnsupportedOperationException(msg());
            }

            public boolean containsAll(Collection c) {
                throw new UnsupportedOperationException(msg());
            }

            public boolean isEmpty() {
                throw new UnsupportedOperationException(msg());
            }

            public Iterator iterator() {
                throw new UnsupportedOperationException(msg());
            }

            public boolean remove(Object o) {
                throw new UnsupportedOperationException(msg());
            }

            public boolean removeAll(Collection c) {
                throw new UnsupportedOperationException(msg());
            }

            public boolean retainAll(Collection c) {
                throw new UnsupportedOperationException(msg());
            }

            public int size() {
                throw new UnsupportedOperationException(msg());
            }

            public Object[] toArray() {
                throw new UnsupportedOperationException(msg());
            }

            public Object[] toArray(Object[] a) {
                throw new UnsupportedOperationException(msg());
            }
        }
         // end of ToAdd
    }
     // end of QEn

    /** Filtering enumeration */
    private static final class FilEn extends Object implements Enumeration {
        /** marker object stating there is no nexte element prepared */
        private static final Object EMPTY = new Object();

        /** enumeration to filter */
        private Enumeration en;

        /** element to be returned next time or {@link #EMPTY} if there is
        * no such element prepared */
        private Object next = EMPTY;

        /** the set to use as filter */
        private Processor filter;

        /**
        * @param en enumeration to filter
        */
        public FilEn(Enumeration en, Processor filter) {
            this.en = en;
            this.filter = filter;
        }

        /** @return true if there is more elements in the enumeration
        */
        public boolean hasMoreElements() {
            if (next != EMPTY) {
                // there is a object already prepared
                return true;
            }

            while (en.hasMoreElements()) {
                // read next
                next = filter.process(en.nextElement(), null);

                if (next != null) {
                    // if the object is accepted
                    return true;
                }

                ;
            }

            next = EMPTY;

            return false;
        }

        /** @return next object in the enumeration
        * @exception NoSuchElementException can be thrown if there is no next object
        *   in the enumeration
        */
        public Object nextElement() {
            if ((next == EMPTY) && !hasMoreElements()) {
                throw new NoSuchElementException();
            }

            Object res = next;
            next = EMPTY;

            return res;
        }
    }
     // end of FilEn

    /** Returns true from contains if object is not null */
    private static class RNulls implements Processor {
        public Object process(Object original, Collection toAdd) {
            return original;
        }
    }
     // end of RNulls
}
