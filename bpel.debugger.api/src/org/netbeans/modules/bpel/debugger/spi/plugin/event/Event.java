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

package org.netbeans.modules.bpel.debugger.spi.plugin.event;

import org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequest;

/**
 * An occurrence in a target BPEL Engine that is of interest to a debugger.
 * Event is the common superinterface for all events (examples include
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.BreakpointReachedEvent},
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.ProcessInstanceCreatedEvent}).
 * When an event occurs, an instance of Event as a component of an
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.EventSet}
 * is enqueued in the
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.BpelEngine}'s
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.EventQueue}.
 * 
 * @author Alexander Zgursky
 */
public interface Event {
    /**
     * Returns the event request that requested this event.
     *
     * @return the event request that requested this event. Some events (e.g.
     *  {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.BpelEngineDisconnectedEvent})
     *  may not have a coresponding request and thus will return <code>null</code>.
     */
    EventRequest getEventRequest();
}
