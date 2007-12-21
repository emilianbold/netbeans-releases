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

package org.netbeans.modules.bpel.debugger.spi.plugin;

import java.util.List;
import java.util.Properties;
import org.netbeans.modules.bpel.debugger.spi.plugin.def.ProcessType;
import org.netbeans.modules.bpel.debugger.spi.plugin.event.EventQueue;
import org.netbeans.modules.bpel.debugger.spi.plugin.exec.ProcessInstance;
import org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequestManager;

/**
 * Represents a target BPEL Engine.
 *
 * @author Alexander Zgursky
 */
public interface BpelEngine {
    List<ProcessInstance> getAllProcessInstances();
    List<ProcessInstance> getProcessInstancesByType(ProcessType processType);
    List<ProcessType> getAllProcessTypes();
    List<ProcessType> getProcessTypesByName(String name, String namespace);
    
    /**
     * Returns the event queue for this BPEL Engine.
     * A BPEL Engine has only one EventQueue object,
     * this method will return the same instance each time it is invoked.
     */
    EventQueue getEventQueue();
    
    /**
     * Returns the event request manager for this BPEL Engine.
     * The EventRequestManager controls user settable events such as
     * breakpoints. A BPEL Engine has only one EventRequestManager object,
     * this method will return the same instance each time it is invoked.
     */
    EventRequestManager getEventRequestManager();
    void resumeAllProcessInstances();
    void disconnect();
}
