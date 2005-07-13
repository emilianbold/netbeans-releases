/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ejbjar;

import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;

/* This class provides access to the {@link EjbJar}'s private constructor 
 * from outside in the way that this class is implemented by an inner class of 
 * {@link EjbJar} and the instance is set into the {@link DEFAULT}.
 */
public abstract class EarAccessor {

    public static EarAccessor DEFAULT;
    
    // force loading of EjbJar class. That will set DEFAULT variable.
    static {
        Class c = Ear.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public abstract Ear createEar(EarImplementation spiEar);

    public abstract EarImplementation getEarImplementation (Ear ear);

}
