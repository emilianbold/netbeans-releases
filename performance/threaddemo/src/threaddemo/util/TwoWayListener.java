/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.util;

import java.util.EventListener;

/**
 * Listener for changes in the status of a two-way support.
 * @author Jesse Glick
 * @see TwoWaySupport
 */
public interface TwoWayListener extends EventListener {
    
    void derived(TwoWayEvent evt);
    
    void invalidated(TwoWayEvent evt);
    
    void recreated(TwoWayEvent evt);
    
    void clobbered(TwoWayEvent evt);
    
}
