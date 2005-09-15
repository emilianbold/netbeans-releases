/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.profiler.api;

import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler;

/**
 * Allows to determine current state of a Profiler registered in the default Lookup.
 *
 * @author sherold
 */
public final class ProfilerSupport {
    
    /**
     * The Profiler agent isn't running.
     */
    public static int STATE_INACTIVE  = 0;
    
    /**
     * The Profiler agent is starting to STATE_BLOCKING or STATE_RUNNING state,
     * target JVM isn't running.
     */
    public static int STATE_STARTING  = 1;
    
    /**
     * The Profiler agent is running and ready for the Profiler to connect, target
     * JVM is blocked.
     */
    public static int STATE_BLOCKING  = 2;
    
    /**
     * The Profiler agent is running and ready for the Profiler to connect, target
     * JVM is running.
     */
    public static int STATE_RUNNING   = 3;
    
    /**
     * The Profiler agent is running and connected to Profiler, target JVM is running.
     */
    public static int STATE_PROFILING = 4;
    
    /**
     * Returns the current state of a Profiler registered into Lookup.
     *
     * @return the current profiler state or <code>STATE_INACTIVE</code> if no 
     *         Profiler is registered in the default Lookup.
     */
    public static int getState() {
        Profiler profiler = ServerRegistry.getProfiler();
        return profiler == null ? STATE_INACTIVE 
                                : profiler.getState();
    }
}
