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

package org.netbeans.core.ui.warmup;

import java.awt.dnd.DragSource;

/** DnD pre-heat task. Initializes drag and drop by calling
 * DragSource.getDefaultDragSource();, which is expensive because of loading 
 * of fonts.
 * May be executed by the core after startup to speed-up initialization 
 * of explorer or other DnD sources and targets.
 *
 * @author  Dafe Simonek
 */
public final class DnDWarmUpTask implements Runnable {
    
    /** Performs DnD pre-heat.
     */
    public void run() {
        DragSource.getDefaultDragSource();
    }
    
}
