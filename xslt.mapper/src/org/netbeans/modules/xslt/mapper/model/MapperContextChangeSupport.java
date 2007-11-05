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
    
    /**
     * Invoked when something changed. 
     * In complex cases ChangeEvent could be passed as newObject. 
     * In this case the oldObject is null.
     * @param oldValue
     * @param newValue
     */
    public void fireMapperContextChanged(Object oldValue, 
            Object newValue) 
    {
        MapperContextChangeListener[] tmp = new MapperContextChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (MapperContextChangeListener listener : tmp) {
            listener.mapperContextChanged(oldValue, newValue);
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
            listener.mapperContextChanged(oldValue, newValue);
                    
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
            listener.mapperContextChanged(oldValue, newValue);
                    
            listener.tMapModelStateChanged(oldValue, newValue);
        }
    }    
    
    public void fireSourceTypeChanged(AXIComponent oldComponent, AXIComponent newComponent) {
        MapperContextChangeListener[] tmp = new MapperContextChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (MapperContextChangeListener listener : tmp) {
            listener.mapperContextChanged(oldComponent, newComponent);
                    
            listener.sourceTypeChanged(oldComponent, newComponent);
        }
    }  
    
    public void fireTargetTypeChanged(AXIComponent oldComponent, AXIComponent newComponent) {
        MapperContextChangeListener[] tmp = new MapperContextChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (MapperContextChangeListener listener : tmp) {
            listener.mapperContextChanged(oldComponent, newComponent);
                    
            listener.targetTypeChanged(oldComponent, newComponent);
        }
    }  

    public void fireMapperContextChanged(MapperContext oldContext, MapperContext newContext) {
        throw new UnsupportedOperationException();
    }
}
