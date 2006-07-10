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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    public String toString() {
        return "PhadhailNameEvent[" + getPhadhail() + ":" + oldName + " -> " + newName + "]";
    }
    
}
