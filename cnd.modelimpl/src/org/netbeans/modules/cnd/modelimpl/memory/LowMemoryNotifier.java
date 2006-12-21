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

/**
 * Allows to register listener that are notified in the case of low memory.
 * Singleton.
 * @author Vladimir Kvashin
 */
public abstract class LowMemoryNotifier {
    
    /** proetcted constructor to prevent external creation */
    protected LowMemoryNotifier() {
    }
    
    public static LowMemoryNotifier instance() {
        return instance;
    }
    
    /** Registers low memory lstener */
    public abstract void addListener(LowMemoryListener listener);
    
    /** Unregisters low memory lstener */
    public abstract void removeListener(LowMemoryListener listener);
    
    /** 
     * Sets the memory usage percentage threshold to the given value
     * @param percentage the new threshold value in percents
     */
    public abstract void setThresholdPercentage(double percentage);
    
    
    private static LowMemoryNotifier instance = new LowMemoryNotifierImpl();
}
