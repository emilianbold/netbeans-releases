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
package org.netbeans.modules.xslt.mapper.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.xam.Model;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class MapperContextChangeSupport {
    private Lock writeLock = new ReentrantReadWriteLock().writeLock();
    private List<MapperContextChangeListener> myListeners = new ArrayList<MapperContextChangeListener>();
    
    public MapperContextChangeSupport() {
    }

    public void addPropertyChangeListener(MapperContextChangeListener changeListener) {
        assert changeListener != null : "Try to add null listener."; // NOI18N
        writeLock.lock();
        try {
            myListeners.add(changeListener);
        } finally {
            writeLock.unlock();
        }
    }

    public void removePropertyChangeListener(MapperContextChangeListener changeListener) {
        assert changeListener != null : "Try to remove null listener."; // NOI18N
        writeLock.lock();
        try {
            myListeners.remove(changeListener);
        } finally {
            writeLock.unlock();
        }
    }
    
    public void fireXslModelStateChanged(Model.State oldValue, 
            Model.State newValue) 
    {
        MapperContextChangeListener[] tmp = new MapperContextChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (MapperContextChangeListener listener : tmp) {
            listener.xslModelStateChanged(oldValue, newValue);
        }
    }    
    
    public void fireTMapModelStateChanged(Model.State oldValue, 
            Model.State newValue) 
    {
        MapperContextChangeListener[] tmp = new MapperContextChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (MapperContextChangeListener listener : tmp) {
            listener.tMapModelStateChanged(oldValue, newValue);
        }
    }    
    
    public void fireSourceTypeChanged(AXIComponent oldComponent, AXIComponent newComponent) {
        MapperContextChangeListener[] tmp = new MapperContextChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (MapperContextChangeListener listener : tmp) {
            listener.sourceTypeChanged(oldComponent, newComponent);
        }
    }  
    
    public void fireTargetTypeChanged(AXIComponent oldComponent, AXIComponent newComponent) {
        MapperContextChangeListener[] tmp = new MapperContextChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (MapperContextChangeListener listener : tmp) {
            listener.targetTypeChanged(oldComponent, newComponent);
        }
    }  

    public void fireMapperContextChanged(MapperContext oldContext, MapperContext newContext) {
        throw new UnsupportedOperationException();
    }
}
