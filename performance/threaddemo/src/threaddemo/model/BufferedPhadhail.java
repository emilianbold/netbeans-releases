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

package threaddemo.model;

import java.lang.ref.*;

/**
 * Wraps a plain Phadhail and buffers its list of children.
 * @author Jesse Glick
 */
class BufferedPhadhail implements Phadhail {
    
    private final Phadhail ph;
    private Reference kids; // Reference<Phadhail[]>
    
    public BufferedPhadhail(Phadhail ph) {
        this.ph = ph;
    }
    
    public Phadhail[] getChildren() {
        Phadhail[] phs = null;
        if (kids != null) {
            phs = (Phadhail[])kids.get();
        }
        if (phs == null) {
            // Need to (re)calculate the children.
            phs = ph.getChildren();
            kids = new WeakReference(phs);
        }
        return phs;
    }
    
    public String getDisplayName() {
        return ph.getDisplayName();
    }
    
    public boolean hasChildren() {
        return ph.hasChildren();
    }
    
}
