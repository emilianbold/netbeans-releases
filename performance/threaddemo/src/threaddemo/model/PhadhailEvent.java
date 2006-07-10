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

import java.util.EventObject;

/**
 * Event object for phadhail events (suitable as is for children changes).
 * @author Jesse Glick
 */
public class PhadhailEvent extends EventObject {

    /** factory */
    public static PhadhailEvent create(Phadhail ph) {
        return new PhadhailEvent(ph);
    }

    /** cannot be subclassed outside package */
    PhadhailEvent(Phadhail ph) {
        super(ph);
    }

    public Phadhail getPhadhail() {
        return (Phadhail)getSource();
    }

    public String toString() {
        return "PhadhailEvent[" + getPhadhail() + "]";
    }
    
}
