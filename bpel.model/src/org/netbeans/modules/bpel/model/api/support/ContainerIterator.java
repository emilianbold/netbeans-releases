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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.model.api.support;

import java.util.Iterator;

import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.impl.BpelEntityImpl;

/**
 * @author ads
 */
public class ContainerIterator<E extends BpelContainer> implements Iterator<E> {

    public ContainerIterator( BpelEntity entity, Class<E> clazz ) {
        ((BpelEntityImpl) entity).checkDeleted();
        isInTree = ((BpelEntityImpl) entity).isInTree();
        myObject = entity;
        next = null;
        myClass = clazz;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        if (!isInTree) {
            return false;
        }

        if (myObject == null) {
            return false;
        }
        
        /*
         * When myObject itself is needed container 
         * then we need to return true.
         * This code should be called only at first time - when myObject is start activity.
         * Otherwise if next is null then myObject should be set to null in method "next()".
         */
        if (myClass.isInstance(myObject)) {
            if (next == null) {
                /*if (myObject instanceof Process) {
                    return false;
                }*/
                next = myClass.cast(myObject);
                return true;
            }
        }
        if (next != null) {
            return true;
        }

        return goToNext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#next()
     */
    public E next() {
        if (!isInTree) {
            return null;
        }

        if (myObject == null) {
            return null;
        }

        // This will be called only when start entity has itself needed container class.
        if (next == null) {
            goToNext();
            if (myClass.isInstance(myObject)) {
                BpelEntity ret = myObject;
                /* if myObject is itself needed container class and next is null,
                 * then we empty myObject for avoiding putting "myObject" into next in "hasNext()" method. 
                 */
                if ( next == null ){
                    myObject = null;
                }
                return myClass.cast(ret);
            }
        }

        myObject = (BpelEntity) next;
        // in the case when we reach root of tree we should empty myObject.
        if (myObject instanceof Process) {
            myObject = null;
            return myClass.cast(next);
        }

        if (myObject != null) {
            goToNext();
            BpelEntity ret = myObject;
            /* if myObject is itself needed container class and next is null,
             * then we empty myObject for avoiding putting "myObject" into next in "hasNext()" method. 
             */
            if ( next == null ){
                myObject = null;
            }
            return myClass.cast(ret);
        }
        else {
            return null;
        }
    }

    private boolean goToNext() {
        ((BpelEntityImpl) myObject).readLock();
        try {
            BpelEntityImpl temp = (BpelEntityImpl) myObject;
            while (!((next = (BpelContainer)temp.getParent()) == null)) {
                if (myClass.isInstance(next)) {
                    return true;
                }
                temp = (BpelEntityImpl) next;
            }
            return false;
        }
        finally {
            ((BpelEntityImpl) myObject).readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private BpelEntity myObject;
    private BpelContainer next;
    private Class<E> myClass;
    private boolean isInTree;
}
