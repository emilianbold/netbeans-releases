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

import java.io.File;

/**
 * Factory for model impls.
 * @author Jesse Glick
 */
public class Phadhails {
    
    private Phadhails() {}
    
    public static Phadhail synchronous(File f) {
        return DefaultPhadhail.forFile(f);
    }
    
    public static Phadhail locked(File f) {
        return LockedPhadhail.forPhadhail(synchronous(f));
    }
    
    public static Phadhail spun(File f) {
        return SpunPhadhail.forPhadhail(synchronous(f));
    }
    
}
