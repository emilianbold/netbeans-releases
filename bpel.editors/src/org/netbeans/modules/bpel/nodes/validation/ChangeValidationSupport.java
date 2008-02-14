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
package org.netbeans.modules.bpel.nodes.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class ChangeValidationSupport {

    private Lock writeLock;
    private Lock readLock;
    private List<ChangeValidationListener> myListeners = new ArrayList<ChangeValidationListener>();

    public ChangeValidationSupport() {
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        writeLock = rwLock.writeLock();
        readLock = rwLock.readLock();
    }
    
    public void addChangeValidationListener(ChangeValidationListener listener) {
        assert listener != null : "Try to add null listerner."; // NOI18N
        writeLock.lock();
        try {
            myListeners.add(listener);
        } finally {
            writeLock.unlock();
        }
    }

    public void removeChangeValidationListener(ChangeValidationListener listener) {
        assert listener != null : "Try to remove null listerner."; // NOI18N
        writeLock.lock();
        try {
            myListeners.remove(listener);
        } finally {
            writeLock.unlock();
        }
    }

    public void fireChangeValidation(Map<Component, Validator.ResultType> changedItems) {
        assert changedItems != null;
        // TODO m
        for (Component elem : changedItems.keySet()) {
            fireChangeValidation(elem,changedItems.get(elem));
        }
    }

//    public void fireChangeValidation(List<ResultItem> updatedItems) {
//        assert updatedItems != null;
//        List<ResultItem>  curUpdatedItems = 
//                new ArrayList<ResultItem>(updatedItems);
//        Collections.copy(curUpdatedItems, updatedItems);
//        for (ResultItem resultItem : curUpdatedItems) {
//            fireChangeValidation(resultItem);
//        }
//    }

    public void fireRemoveValidation(List<Component> removedComponents) {
        assert removedComponents != null;

        Component[] curRemovedComponents = new Component[removedComponents.size()];
        synchronized (removedComponents){
            curRemovedComponents = removedComponents.toArray(curRemovedComponents);
        }

//        List<Component>  curRemovedComponents = 
//                new ArrayList<Component>(removedComponents);
//        Collections.copy(curRemovedComponents, removedComponents);
        for (Component component : curRemovedComponents) {
            fireRemoveValidation(component);
        }
    }

//    public void fireRemoveValidation(List<ResultItem> removedItems) {
//        assert removedItems != null;
//        List<ResultItem>  curRemovedItems = 
//                new ArrayList<ResultItem>(removedItems);
//        Collections.copy(curRemovedItems, removedItems);
//        for (ResultItem resultItem : curRemovedItems) {
//            fireRemoveValidation(resultItem);
//        }
//    }

    public void fireChangeValidation(Component component
            , Validator.ResultType resultType)
    {

        ChangeValidationListener[] tmp = null;
        readLock.lock();
        try {
            tmp = myListeners.toArray(new ChangeValidationListener[myListeners.size()]);
        } finally {
            readLock.unlock();
        }

        for (ChangeValidationListener listener : tmp) {
            listener.validationUpdated(component, resultType);
        }
    }

//    public void fireChangeValidation(ResultItem updatedItem) {
//        writeLock.lock();
//        try {
//            for (ChangeValidationListener listener : myListeners) {
//                listener.validationUpdated(updatedItem);
//            }
//        } finally {
//            writeLock.unlock();
//        }
//    }

    public void fireRemoveValidation(Component component) {
        ChangeValidationListener[] tmp = null;
        readLock.lock();
        try {
            tmp = myListeners.toArray(new ChangeValidationListener[myListeners.size()]);
        } finally {
            readLock.unlock();
        }

        for (ChangeValidationListener listener : tmp) {
            listener.validationRemoved(component);
        }
//        writeLock.lock();
//        try {
//            for (ChangeValidationListener listener : myListeners) {
//                listener.validationRemoved(component);
//            }
//        } finally {
//            writeLock.unlock();
//        }
    }

    public void fireAddAnnotation(UniqueId entity, String annotationType) {
        ChangeValidationListener[] tmp = null;
        readLock.lock();
        try {
            tmp = myListeners.toArray(new ChangeValidationListener[myListeners.size()]);
        } finally {
            readLock.unlock();
        }

        for (ChangeValidationListener listener : tmp) {
            listener.annotationAdded(entity, annotationType);
        }

//        writeLock.lock();
//        try {
//            for (ChangeValidationListener listener : myListeners) {
//                listener.annotationAdded(entity, annotationType);
//            }
//        } finally {
//            writeLock.unlock();
//        }
    }

    public void fireRemoveAnnotation(UniqueId entity, String annotationType) {
        ChangeValidationListener[] tmp = null;
        readLock.lock();
        try {
            tmp = myListeners.toArray(new ChangeValidationListener[myListeners.size()]);
        } finally {
            readLock.unlock();
        }

        for (ChangeValidationListener listener : tmp) {
            listener.annotationRemoved(entity, annotationType);
        }

//        writeLock.lock();
//        try {
//            for (ChangeValidationListener listener : myListeners) {
//                listener.annotationRemoved(entity, annotationType);
//            }
//        } finally {
//            writeLock.unlock();
//        }
    }
//    public void fireRemoveValidation(ResultItem removedItem) {
//        writeLock.lock();
//        try {
//            for (ChangeValidationListener listener : myListeners) {
//                listener.validationRemoved(removedItem);
//            }
//        } finally {
//            writeLock.unlock();
//        }
//    }
}
