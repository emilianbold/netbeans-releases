/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

/**
 * Implementation of LowMemoryNotifier.
 * @author Vladimir Kvashin
 */

// package-local
class LowMemoryNotifierImpl extends LowMemoryNotifier implements NotificationListener {
    
    private Logger logger;
    
    public LowMemoryNotifierImpl() {
	
	logger = Logger.getLogger(getClass().getPackage().getName());
	String level = System.getProperty(logger.getName());
	if( level != null ) {
	    try {
		logger.setLevel(Level.parse(level));
	    } catch( IllegalArgumentException e ) {}
	}
	else {
	    logger.setLevel(Level.SEVERE);
	}
	
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        if( mbean instanceof NotificationEmitter ) {
            NotificationEmitter emitter = (NotificationEmitter) mbean;
            emitter.addNotificationListener(this, null, null);
        }
    }
    
    public void addListener(LowMemoryListener listener) {
        synchronized( listeners ) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(LowMemoryListener listener) {
	logger.info("LowMemoryNotifierImpl.removeListener " + listener);
        synchronized( listeners ) {
            listeners.remove(listener);
        }
    }
    
    public void setThresholdPercentage(double percentage) {
	logger.info("LowMemoryNotifierImpl.setThresholdPercentage " + percentage);
        assert(0 < percentage && percentage < 1.0);
        long maxMemory = pool.getUsage().getMax();
        long threshold = (long) (maxMemory * percentage);
        pool.setUsageThreshold(threshold);
    }

    /**
     * Implements NotificationListener.
     *
     * @param notification The notification.
     *
     * @param handback An opaque object which helps the listener to associate information
     * regarding the MBean emitter. This object is passed to the MBean during the
     * addListener call and resent, without modification, to the listener. The MBean object
     * should not use or modify the object.
     */
    public void handleNotification(Notification notification, Object hb) {
	logger.info("LowMemoryNotifierImpl.handleNotification " + notification);
        if (MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(notification.getType())) {
            long maxMemory = pool.getUsage().getMax();
            long usedMemory = pool.getUsage().getUsed();
	    logger.info("LowMemoryNotifierImpl.handleNotification " + maxMemory + '/' + usedMemory);
            fireMemoryLow(maxMemory, usedMemory);
        }
    }
    
    private static MemoryPoolMXBean findHeapPool() {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
                return pool;
            }
        }
        return null;
    }

    private void fireMemoryLow(long maxMemory, long usedMemory) {
        LowMemoryEvent event = new LowMemoryEvent(this, maxMemory, usedMemory);
        LowMemoryListener[] la = getListeners();
        for (int i = 0; i < la.length; i++) {
            la[i].memoryLow(event);
        }
    }
    
    private LowMemoryListener[] getListeners() {
        synchronized( listeners ) {
            LowMemoryListener[] result = new LowMemoryListener[listeners.size()];
            listeners.toArray(result);
            return result;
        }
    }
    
    private Collection/*<LowMemoryListener>*/ listeners = new  LinkedList/*<LowMemoryListener>*/();
    
    private static final MemoryPoolMXBean pool = findHeapPool();

}
