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

/**
 * Event object for phadhail name change events.
 * Old and new names may be null.
 * @author Jesse Glick
 */
public final class PhadhailNameEvent extends PhadhailEvent {
    
    /** factory */
    public static PhadhailNameEvent create(Phadhail ph, String oldName, String newName) {
        return new PhadhailNameEvent(ph, oldName, newName);
    }
    
    private final String oldName, newName;
    
    private PhadhailNameEvent(Phadhail ph, String oldName, String newName) {
        super(ph);
        this.oldName = oldName;
        this.newName = newName;
    }
    
    public String getOldName() {
        return oldName;
    }
    
    public String getNewName() {
        return newName;
    }
    
}
