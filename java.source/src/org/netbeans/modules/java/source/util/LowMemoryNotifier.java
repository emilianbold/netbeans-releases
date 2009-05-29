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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.source.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import org.openide.ErrorManager;

/**
 *
 * @author Tomas Zezula
 */
public class LowMemoryNotifier {
    
    private static final float DEFAULT_HEAP_LIMIT = 0.9f;
    private static Logger log = Logger.getLogger(LowMemoryNotifier.class.getName());
    
    private static LowMemoryNotifier instance;
    
    private final NotificationListener notificationListener = new Listener ();
    private final List<LowMemoryListener> listeners;
    private MemoryPoolMXBean pool;
    private MemoryPoolMXBean cachedPool;    //Only initJMX may use this field!
    private float heapLimit;
    
    
    /** Creates a new instance of LowMemoryNotifier */
    private LowMemoryNotifier() {
        this.heapLimit = DEFAULT_HEAP_LIMIT;
        this.listeners = new ArrayList<LowMemoryListener> ();
    }
    
    
    float getHeapLimit () {
        return this.heapLimit;
    }
    
    void setHeapLimit (float heapLimit) {
        this.heapLimit = heapLimit;
        synchronized (this) {
            if (this.pool != null) {
                MemoryUsage mu = pool.getUsage();
                this.pool.setUsageThreshold  ((long)(mu.getMax() * heapLimit));
            }
        }
    }            
    
    
    public synchronized void addLowMemoryListener (final LowMemoryListener listener) {        
        assert listener != null;
        if (this.pool == null) {
            this.pool = initJMX ();
        }
        assert this.pool != null;
        final MemoryUsage usage = this.pool.getUsage();
        assert usage != null : String.format("Pool %s returned null MemoryUsage, Valid: %s\n",this.pool.getName(),this.pool.isValid() ? Boolean.TRUE : Boolean.FALSE);
        if (usage != null && usage.getUsed() >= this.pool.getUsageThreshold()) {
            listener.lowMemory (new LowMemoryEvent (this, this.pool));
        }
        this.listeners.add (listener);
    }
    
    public synchronized void removeLowMemoryListener (final LowMemoryListener listener) {
        assert listener != null;
        this.listeners.remove (listener);
        if (this.listeners.isEmpty() && this.pool != null) {
            finishJMX (this.pool);
            this.pool = null;
        }
    }
    
    private void fireLowMemory () {
        LowMemoryListener[] _listeners;
        MemoryPoolMXBean _pool;
        synchronized (this) {
            _listeners = this.listeners.toArray(new LowMemoryListener[this.listeners.size()]);
            _pool = this.pool;
        }
        if (_listeners.length > 0) {
            assert _pool != null;
            final LowMemoryEvent event = new LowMemoryEvent (this, _pool);
            for (LowMemoryListener l : _listeners) {
                l.lowMemory(event);
            }
        }
    }
    
    private MemoryPoolMXBean initJMX () {                    
        List<MemoryPoolMXBean> pools = null;
        if (this.cachedPool == null || !this.cachedPool.isValid()) {
            pools = ManagementFactory.getMemoryPoolMXBeans();
            for (MemoryPoolMXBean pool : pools) {            
                if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {    //NOI18N                                    
                    this.cachedPool = pool;
                    break;
                }
            }
        }
        assert this.cachedPool != null : dumpMemoryPoolMXBean (pools);
        if (this.cachedPool != null) {
            MemoryUsage mu = this.cachedPool.getUsage();
            this.cachedPool.setUsageThreshold((long)(mu.getMax() * heapLimit));
            MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
            ((NotificationEmitter)mbean).addNotificationListener(this.notificationListener,null,null);
        }
        return this.cachedPool;
    }
    
    private void finishJMX (final MemoryPoolMXBean pool) {
        assert pool != null;
        try {
            MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
            ((NotificationEmitter)mbean).removeNotificationListener(this.notificationListener);
        } catch (ListenerNotFoundException e) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, e.getMessage(), e);
        }
        pool.setUsageThreshold(0);
    }
    
    private class Listener implements NotificationListener {
        public void handleNotification(Notification notification, Object handback) {            
            final String notificationType = notification.getType();
            if (notificationType.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                fireLowMemory ();
            }            
        }
        
    }
    
    private static String dumpMemoryPoolMXBean (List<MemoryPoolMXBean> pools) {
        StringBuilder sb = new StringBuilder ();
        for (MemoryPoolMXBean pool : pools) {
            sb.append(String.format("Pool: %s Type: %s TresholdSupported: %s\n", pool.getName(), pool.getType(), pool.isUsageThresholdSupported() ? Boolean.TRUE : Boolean.FALSE));
        }
        sb.append('\n');
        return sb.toString();
    }
    
    public static synchronized LowMemoryNotifier getDefault () {
        if (instance == null) {
            instance = new LowMemoryNotifier ();
        }
        return instance;
    }
    
}
