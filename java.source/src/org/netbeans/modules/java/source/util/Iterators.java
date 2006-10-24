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

package org.netbeans.modules.java.source.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
//import org.netbeans.api.jogurt.patterns.ParametrizedFactory;

/** XXX - tests (some still missing)
 *  XXX - javadoc
 *
 * @author Petr Hrebejk
 */
public final class Iterators {

    private static final String NULL_AS_PARAMETER_MESSAGE = "Iterator(s) passed in as parameter must NOT be null."; // NOI18N
    
    /** Singleton */
    private Iterators() {}

    public static <T> Iterator<T> empty() {
        return new EmptyIterator();
    }
    
    public static <T> Iterator<T> unmodifiable( Iterator<T> iterator ) {
        return new UnmodifiableIterator<T>( iterator );
    }
    
    public static <T> Iterator<T> chained( Iterator<T>... iterators ) {
        return new ChainedIterator<T>( iterators );
    }
    
    public static <T> Iterator<T> chained( Collection<Iterator<T>> iterators ) {        
        return new ChainedIterator<T>( iterators );
    }
    
    public static <T> Iterator<T> colating( Iterator<? extends T>... iterators ) {
        return new CollatingIterator<T>( iterators );
    }
    
    public static <T> Iterator<T> colating( Comparator<? super T> comparator, Iterator<? extends T>... iterators ) {
        return new CollatingIterator<T>( comparator, iterators );
    }
    
    public static <T,P> Iterator<T> translating( Iterator<? extends P> delegate, Factory<? extends T,P> factory ) { 
        return new TranslatingIterator<T,P>( delegate, factory );
    }        

    /**
     * Todo: Probably wrong
     */
    public static <T> Iterable<T> toIterable( Iterator<T> iterator ) {
        return new IteratorIterable( iterator );
    }
    
    
    public static <T,P> Iterable<T> translating (final Iterable<? extends P> delegate, final Factory<? extends T,P> factory) {        
        return new TranslatingIterable (delegate, factory);
    }
        
    // Innerclasses ------------------------------------------------------------
    
    
    private static class TranslatingIterable<T,P> implements Iterable<T> {
        
        final Iterable<? extends P> delegate;
        final Factory<? extends T,P> factory;
        
        
        public TranslatingIterable (final Iterable<? extends P> delegate, final Factory<? extends T,P> factory) {
            assert delegate != null;
            assert factory != null;
            this.delegate = delegate;
            this.factory = factory;
        }
        
        public Iterator<T> iterator() {
            return translating(this.delegate.iterator(),this.factory);
        }        
    }
        
    private static class EmptyIterator<T> implements Iterator<T> {
                
        public void remove() {
            throw new UnsupportedOperationException( "Can't remove elements from emptu iterator");
        }

        public boolean hasNext() {
            return false;
        }

        public T next() {
            throw new NoSuchElementException( "Empty Iterator has no elements");
        }
                
    }
    
    private static class TranslatingIterator<T,P> implements Iterator<T> {
        
        private Iterator<? extends P> delegate;
        private Factory<? extends T,P> factory;
        
        public TranslatingIterator( Iterator<? extends P> delegate, Factory<? extends T,P> factory ) {
            if ( delegate == null ) {
                throw new IllegalArgumentException( NULL_AS_PARAMETER_MESSAGE );
            }
            this.delegate = delegate;
            this.factory = factory;
        }
        
        public void remove() {
            delegate.remove();
        }

        public T next() {
            return factory.create( delegate.next() );            
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }
                        
    }
   
    
    private static class UnmodifiableIterator<T> implements Iterator<T> {
        
        private Iterator<T> delegate;
        
        public UnmodifiableIterator( Iterator<T> delegate ) {
            if ( delegate == null ) {
                throw new IllegalArgumentException( NULL_AS_PARAMETER_MESSAGE );
            }
            this.delegate = delegate;
        }
        
        public void remove() {
            throw new UnsupportedOperationException(); 
        }

        public T next() {
            return delegate.next();            
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }
                        
    }       
        
    private static class ChainedIterator<E> implements Iterator<E> {

        /** The chain of iterators */
        protected final List<Iterator<E>> iteratorChain = 
            new ArrayList<Iterator<E>>();

        /** The index of the current iterator */
        protected int currentIteratorIndex = 0;

        /** The current iterator */
        protected Iterator<E> currentIterator = null;

        /**
         * The "last used" <code>Iterator</code> is the <code>Iterator</code> upon
         * which <code>next()</code> or <code>hasNext()</code> was most recently
         * called used for the <code>remove()</code> operation only.
         */
        protected Iterator<E> lastUsedIterator = null;

        /**
         * <code>IteratorChain</code> is "locked" after the first time
         * <code>next()</code> is called.
         */
        protected boolean isLocked = false;


        /**
         * Construct an <code>IteratorChain</code> with a list of
         * <code>Iterator</code>s.
         * 
         * @param iterators the iterators to add to the <code>IteratorChain</code>
         * 
         * @throws NullPointerException if one of the provided iterator is
         *  <code>null</code>
         */
        public ChainedIterator(final Iterator<E>... iterators) {        
            for (Iterator<E> iterator : iterators) {
                if ( iterator == null ) {
                    throw new IllegalArgumentException( NULL_AS_PARAMETER_MESSAGE );
                }
                iteratorChain.add(iterator);
            }
        }
                
        /**
         * Constructs a new <code>IteratorChain</code> over the collection of
         * iterators.
         *
         * @param iterators the collection of iterators
         * 
         * @throws NullPointerException if iterators collection is or contains
         *  <code>null</code>
         */
        public ChainedIterator(final Collection<Iterator<E>> iterators) {
            for (Iterator<E> iterator : iterators) {
                if ( iterator == null ) {
                    throw new IllegalArgumentException( NULL_AS_PARAMETER_MESSAGE );
                }
                iteratorChain.add(iterator);
            }
        }

        // Public methods
        // -------------------------------------------------------------------------

        /**
         * Number of <code>Iterator</code>s in the current
         * <code>IteratorChain</code>.
         * 
         * @return the <code>Iterator</code> count
         */
        public int size() {
            return iteratorChain.size();
        }

        /**
         * Updates the current iterator field to ensure that the current
         * <code>Iterator</code> is not exhausted
         */
        protected void updateCurrentIterator() {
            if (currentIterator == null) {
                if (iteratorChain.isEmpty()) {
                    // @todo How to manage the EmptyIterator.INSTANCE warning?
                    currentIterator = Collections.<E>emptyList().iterator();
                } else {
                    currentIterator = iteratorChain.get(0);
                }
                // set last used iterator here, in case the user calls remove
                // before calling hasNext() or next() (although they shouldn't)
                lastUsedIterator = currentIterator;
            }

            while (currentIterator.hasNext() == false &&
                   currentIteratorIndex < iteratorChain.size() - 1) {
                currentIteratorIndex++;
                currentIterator = iteratorChain.get(currentIteratorIndex);
            }
        }

        // Iterator interface methods
        // -------------------------------------------------------------------------

        /**
         * Return <code>true</code> if any <code>Iterator</code> in the
         * <code>IteratorChain</code> has a remaining element.
         * 
         * @return <code>true</code> if elements remain
         */
        public boolean hasNext() {
            updateCurrentIterator();
            lastUsedIterator = currentIterator;

            return currentIterator.hasNext();
        }

        /**
         * Returns the next object of the current <code>Iterator</code>.
         * 
         * @return object from the current <code>Iterator</code>
         * 
         * @throws java.util.NoSuchElementException if all the
         *  <code>Iterator</code>s are exhausted
         */
        public E next() {
            updateCurrentIterator();
            lastUsedIterator = currentIterator;

            return currentIterator.next();
        }

        /**
         * Removes from the underlying collection the last element 
         * returned by the <code>Iterator</code>.
         * <p>
         * As with <code>next()</code> and <code>hasNext()</code>, this method calls
         * <code>remove()</code> on the underlying <code>Iterator</code>. Therefore,
         * this method may throw an <code>UnsupportedOperationException</code> if
         * the underlying <code>Iterator</code> does not support this method. 
         * 
         * @throws UnsupportedOperationException if the remove operator is not
         *  supported by the underlying <code>Iterator</code>
         * @throws IllegalStateException if the next method has not yet been called,
         *  or the remove method has already been called after the last call to the
         *  next method
         */
        public void remove() {
            updateCurrentIterator();

            lastUsedIterator.remove();
        }


    }
    
    

    private static class CollatingIterator<E> implements Iterator<E> {

        // Instance fields.
        // -------------------------------------------------------------------------

        /** The {@link Comparator} used to evaluate order. */
        private Comparator<? super E> comparator = null;

        /** The list of {@link Iterator}s to evaluate. */
        private List<Iterator<? extends E>> iterators = null;

        /** {@link Iterator#next() Next} objects peeked from each iterator. */
        private List<E> values = null;

        /** Whether or not each {@link #values} element has been set. */
        private BitSet valueSet = null;

        /**
         * Index of the {@link #iterators iterator} from whom the last returned
         * value was obtained.
         */
        private int lastReturned = -1;

        // Constructors
        // ----------------------------------------------------------------------


        public CollatingIterator( Iterator<? extends E>... iterators) {
            this.iterators = Arrays.asList( iterators );
        }

        public CollatingIterator( Comparator<? super E> comp,
                                  Iterator<? extends E>... iterators) {
            this.iterators = Arrays.asList( iterators );
            this.comparator = comp;
        }

        // Iterator Methods
        // -------------------------------------------------------------------

        /**
         * Returns <code>true</code> if any child iterator has remaining elements.
         *
         * @return <code>true</code> if this iterator has remaining elements
         */
        public boolean hasNext() {
            start();
            return anyValueSet(valueSet) || anyHasNext(iterators);
        }

        /**
         * Returns the next ordered element from a child iterator.
         *
         * @return the next ordered element
         * 
         * @throws NoSuchElementException if no child iterator has any more elements
         */
        public E next() throws NoSuchElementException {
            if (hasNext() == false) {
                throw new NoSuchElementException();
            }
            int leastIndex = least();
            if (leastIndex == -1) {
                throw new NoSuchElementException();
            } else {
                E val = values.get(leastIndex);
                clear(leastIndex);
                lastReturned = leastIndex;
                return val;
            }
        }

        /**
         * Removes the last returned element from the child iterator that 
         * produced it.
         *
         * @throws IllegalStateException if there is no last returned element,
         *  or if the last returned element has already been removed
         */
        public void remove() {
            if (lastReturned == -1) {
                throw new IllegalStateException("No value can be removed at present");
            }
            Iterator<? extends E> it = iterators.get(lastReturned);
            it.remove();
        }

        // Private Methods
        // -------------------------------------------------------------------

        /** 
         * Initializes the collating state if it hasn't been already.
         */
        private void start() {
            if (values == null) {
                values = new ArrayList<E>(iterators.size());
                valueSet = new BitSet(iterators.size());
                for (int i = 0; i < iterators.size(); i++) {
                    values.add(null);
                    valueSet.clear(i);
                }
            }
        }

        /** 
         * Sets the {@link #values} and {@link #valueSet} attributes 
         * at position <i>i</i> to the next value of the 
         * {@link #iterators iterator} at position <i>i</i>, or 
         * clear them if the <i>i</i><sup>th</sup> iterator
         * has no next value.
         *
         * @return <code>false</code> if there was no value to set
         */
        private boolean set(final int i) {
            Iterator<? extends E> it = iterators.get(i);
            if (it.hasNext()) {
                values.set(i, it.next());
                valueSet.set(i);
                return true;
            } else {
                values.set(i, null);
                valueSet.clear(i);
                return false;
            }
        }

        /** 
         * Clears the {@link #values} and {@link #valueSet} attributes 
         * at position <i>i</i>.
         */
        private void clear(final int i) {
            values.set(i, null);
            valueSet.clear(i);
        }

        /** 
         * Throws {@link IllegalStateException} if iteration has started 
         * via {@link #start}.
         * 
         * @throws IllegalStateException if iteration started
         */
        private void checkNotStarted() throws IllegalStateException {
            if (values != null) {
                throw new IllegalStateException(
                            "Can't do that after next or hasNext has been called.");
            }
        }

        /** 
         * Returns the index of the least element in {@link #values},
         * {@link #set(int) setting} any uninitialized values.
         * 
         * @throws IllegalStateException
         */
        private int least() {
            int leastIndex = -1;
            E leastObject = null;                
            for (int i = 0; i < values.size(); i++) {
                if (valueSet.get(i) == false) {
                    set(i);
                }
                if (valueSet.get(i)) {
                    if (leastIndex == -1) {
                        leastIndex = i;
                        leastObject = values.get(i);
                    } else {
                        E curObject = values.get(i);
                        if (comparator.compare(curObject,leastObject) < 0) {
                            leastObject = curObject;
                            leastIndex = i;
                        }
                    }
                }
            }
            return leastIndex;
        }

        /**
         * Returns <code>true</code> if any bit in the given set is 
         * <code>true</code>.
         */
        private boolean anyValueSet(final BitSet set) {
            for (int i = 0; i < set.size(); i++) {
                if (set.get(i)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns <code>true</code> if any {@link Iterator} 
         * in the given list has a next value.
         */
        private boolean anyHasNext(final List<Iterator<? extends E>> iters) {
            for (Iterator<? extends E> it: iters) {
                if (it.hasNext()) {
                    return true;
                }
            }
            return false;
        }
    }
    
    private static class IteratorIterable<T> implements Iterable<T> {
        
        private Iterator<T> iterator;
        
        public IteratorIterable( Iterator<T> iterator ) {
            if ( iterator == null ) {
                throw new IllegalArgumentException( NULL_AS_PARAMETER_MESSAGE );
            }
            this.iterator = iterator;
        }

        public Iterator<T> iterator() {
            return iterator;
        }
    
    }
    
}
