/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.lib.nbjshell;

import jdk.jshell.spi.ExecutionControl;

/**
 * Carries out non-agent tasks for the JShell. Its actual implementation
 * may vary depending on the launch mode of the JShell
 * 
 * @author sdedic
 */
public interface RemoteJShellService extends ExecutionControl {
    /**
     * Requests shutdown of the target process. The implementation may ignore
     * the request, but JShell should terminate at the local side anyway.
     * @return true, if the request was accepted
     */
    public boolean requestShutdown();
    
    /**
     * Closes the supplied I/O streams. If the streams are not yet opened or created
     * the method does not even attempt to initiate the target VM. Further requests
     * to get streams will result in an IOException.
     */
    public void closeStreams();
    
    public String getTargetSpec();
}
