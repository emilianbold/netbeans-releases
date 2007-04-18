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

package org.netbeans.modules.bpel.debugger.spi.plugin.exec;

import org.netbeans.modules.bpel.debugger.spi.plugin.*;
import org.netbeans.modules.bpel.debugger.spi.plugin.def.ProcessType;

/**
 * Represents a process instance, running on the BPEL Engine.
 * It is not required to have a single object
 * of the implementation class for a process instance.
 * Although, equals() and hashCode() should be implemented
 * properly to reflect that those objects are representing the same
 * process instance.
 *
 * @author Alexander Zgursky
 */
public interface ProcessInstance {
    String geUniqueId();
    ProcessType getProcessType();
    State getState();
    ScopeInstance getRootScopeInstance();
    void resume();
    boolean equals(Object obj);
    int hashCode();
    
    enum State {
        UNKNOWN,
        RUNNING,
        SUSPENDED,
        COMPLETED,
        FAILED,
        TERMINATED
    }

    //void suspend() throws PluginException;
    //Date getStartTime(Object piid);
    //public synchronized void setVariable(...
    //void terminate(Object piid);
    //QName getCompletedFault(Object piid)
}
