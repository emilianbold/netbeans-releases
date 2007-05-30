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
package org.netbeans.modules.xslt.tmap.model.xsltmap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ChangeXsltMapSupport {
    
    private Lock writeLock = new ReentrantReadWriteLock().writeLock();
    private List<XsltMapPropertyChangeListener> myListeners = new ArrayList<XsltMapPropertyChangeListener>();
    
    public ChangeXsltMapSupport() {
    }

    void addPropertyChangeListener(XsltMapPropertyChangeListener changeListener) {
        assert changeListener != null : "Try to add null listener."; // NOI18N
        writeLock.lock();
        try {
            myListeners.add(changeListener);
        } finally {
            writeLock.unlock();
        }
    }

    void removePropertyChangeListener(XsltMapPropertyChangeListener changeListener) {
        assert changeListener != null : "Try to remove null listener."; // NOI18N
        writeLock.lock();
        try {
            myListeners.remove(changeListener);
        } finally {
            writeLock.unlock();
        }
    }
    
    public void fireTransformationDescChanged(TransformationDesc oldDesc, TransformationDesc newDesc) {
        XsltMapPropertyChangeListener[] tmp = new XsltMapPropertyChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (XsltMapPropertyChangeListener listener : tmp) {
            listener.transformationDescChanged(oldDesc, newDesc);
        }
    }
}
