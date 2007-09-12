/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.nodes.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class ChangeValidationSupport {

    private Lock writeLock = new ReentrantReadWriteLock().writeLock();
    private List<ChangeValidationListener> myListeners = new ArrayList<ChangeValidationListener>();

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

        ChangeValidationListener[] tmp = new ChangeValidationListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
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
        writeLock.lock();
        try {
            for (ChangeValidationListener listener : myListeners) {
                listener.validationRemoved(component);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void fireAddAnnotation(UniqueId entity, String annotationType) {
        writeLock.lock();
        try {
            for (ChangeValidationListener listener : myListeners) {
                listener.annotationAdded(entity, annotationType);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void fireRemoveAnnotation(UniqueId entity, String annotationType) {
        writeLock.lock();
        try {
            for (ChangeValidationListener listener : myListeners) {
                listener.annotationRemoved(entity, annotationType);
            }
        } finally {
            writeLock.unlock();
        }
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
