/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.mapper.tree.spi;

import java.util.Iterator;

/**
 * The iterator which can be used multiple times. 
 * 
 * @author nk160297
 */
public interface RestartableIterator<T> extends Iterator<T> {
    
    /**
     * Set iterator to initial state. 
     */
    void restart();

    /**
     * The specific implementaion of the iterator. It expand the base iterator
     * by adding new element to the beginning (head). 
     * @param baseItr
     * @param newHead
     */
    class RestartableIteratorExpander<T> implements RestartableIterator<T> {

        private RestartableIterator<T> mBaseIterator;
        private T mExtHead;
        private boolean mBeforeHead;
        
        public RestartableIteratorExpander(RestartableIterator<T> baseItr, T newHead) {
            assert baseItr != null && newHead != null;
            //
            mBaseIterator = baseItr;
            mExtHead = newHead;
            mBeforeHead = true;
        }
        
        public void restart() {
            mBeforeHead = true;
            mBaseIterator.restart();
        }

        public boolean hasNext() {
            if (mBeforeHead) {
                return true;
            } else {
                return mBaseIterator.hasNext();
            }
        }

        public T next() {
            if (mBeforeHead) {
                mBeforeHead = false;
                return mExtHead;
            } else {
                return mBaseIterator.next();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("This iterator is immutable."); // NOI18N
        }

    }

}
