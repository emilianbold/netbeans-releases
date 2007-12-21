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

import java.util.Set;
import org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequest;

/**
 * A container for one or more
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.Event}
 * objects.
 * Several
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.Event}
 * objects may be generated at a given time by the BPEL Engine.
 * For example, there may be more than one
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.BreakpointReachedRequest}
 * for a given
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.def.Position}
 * or you might single step to the same
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.def.Position}
 * as a
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.BreakpointReachedRequest}.
 * These
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.Event}
 * objects are delivered together as an EventSet.
 * For uniformity, an EventSet is always used to deliver
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.Event}
 * objects.
 * EventSets are delivered by the
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.EventQueue}.
 * EventSets are unmodifiable.
 * <br><br>
 * Associated with the issuance of an event set, process instance suspensions
 * may have occurred in the target BPEL Engine. These suspensions correspond
 * with the
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequest.SuspendPolicy}.
 * <br>
 * The events that are in an EventSet are restricted in the following ways:
 *  <ul>
 *    <li>Always singleton sets:
 *      <ul>
 *      <li>{@link org.netbeans.modules.bpel.debugger.spi.plugin.event.BpelEngineDisconnectedEvent}
 *      </ul>
 *    <li>Only with other ProcessInstanceCreatedEvents for the same process instance:
 *      <ul>
 *      <li>{@link org.netbeans.modules.bpel.debugger.spi.plugin.event.ProcessInstanceCreatedEvent}
 *      </ul>
 *    <li>Only with other ProcessInstanceCompletedEvents for the same process instance:
 *      <ul>
 *      <li>{@link org.netbeans.modules.bpel.debugger.spi.plugin.event.ProcessInstanceCompletedEvent}
 *      </ul>
 *    <li>Only with other members of this group, at the same position and in the same scope instance:
 *      <ul>
 *      <li>{@link org.netbeans.modules.bpel.debugger.spi.plugin.event.BreakpointReachedEvent}
 *      <li>{@link org.netbeans.modules.bpel.debugger.spi.plugin.event.StepCompletedEvent}
 *      </ul>
 *  </ul>
 * 
 * 
 * @author Alexander Zgursky
 */
public interface EventSet extends Set<Event> {
    /**
     * Returns the policy used to suspend process instances in the target
     * BPEL Engine for this event set. This policy is selected from the suspend
     * policies for each event's request; the target BPEL Engine chooses the
     * policy which suspends the most process instances.
     * The target BPEL Engine suspends process instances according to that
     * policy and that policy is returned here, unless target BPEL Engine
     * doesn't support required suspend policy.
     * See
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequest.SuspendPolicy}
     * for the possible policy values.
     *
     * @return the suspend policy applied for this event set
     */
    EventRequest.SuspendPolicy getSuspendPolicy();
}
