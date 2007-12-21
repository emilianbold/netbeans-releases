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

import org.netbeans.modules.bpel.debugger.spi.plugin.exec.ScopeInstance;
import org.netbeans.modules.bpel.debugger.spi.plugin.def.Position;

/**
 * Abstract superinterface of events that have both
 * position and scope instance.
 *
 * @author Alexander Zgursky
 */
public interface LocatableEvent extends Event {

    /**
     * Returns the scope instance in which this event has occured.
     *
     * @return the scope instance in which this event has occured
     */
    ScopeInstance getScopeInstance();
    
    /**
     * Returns the position at which this event has occured.
     *
     * @return the position at which this event has occured
     */
    Position getPosition();
}
