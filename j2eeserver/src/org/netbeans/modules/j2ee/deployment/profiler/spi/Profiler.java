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

package org.netbeans.modules.j2ee.deployment.profiler.spi;

import java.util.Map;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;

/**
 * Profiler has to implement this interface and register it in the default Lookup.
 *
 * @author sherold
 */
public interface Profiler {
    
    /**
     * Inform the profiler that some server is starting in the profile mode. It 
     * allows the Profiler to correctly detect STATE_STARTING.
     */
    void notifyStarting();
    
    /**
     * This method is used from the <code>nbstartprofiledserver</code>
     * task to connect the Profiler to a server ready for profiling.
     *
     * @param projectProperties properties of project the <code>nbstartprofiledserver</code>
     *                          ant task was started from.
     *
     * @return <code>true</code> if the Profiler successfully attached to the server.
     */
    boolean attachProfiler(Map projectProperties);
    
    /**
     * This method is used from the Runtime tab to obtain settings for starting 
     * the server. It displays dialog and let the user choose required mode 
     * (direct/dynamic attach) and other settings for the server startup.
     *
     * @param   serverInstanceID ID of the server instance that is going to be started
     *
     * @return  required settings or <code>null</code> if user cancelled starting 
     *          the server.
     */
    ProfilerServerSettings getSettings(String serverInstanceID);
    
    /**
     * Returns state of Profiler agent instance started from the IDE. It detects 
     * possible response from an unknown (not started from the IDE) Profiler
     * agent, in this case it returns STATE_INACTIVE.
     *
     * @return state of Profiler agent instance.
     */
    int getState();
    
    /**
     * Stops execution of the application (its JVM) currently being profiled.
     * Shutdown is performed by the Profiler agent when in STATE_BLOCKED, STATE_RUNNING
     * or STATE_PROFILING state.
     *
     * @return object used to monitor progress of shutdown.
     */
    ProgressObject shutdown();
}
