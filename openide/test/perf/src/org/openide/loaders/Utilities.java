/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.loaders;

/**
 * A set of utility methods.
 */
public abstract class Utilities {
    
    /** The method is a bridge to package private IDO's method setCustomClassLoader */
    public static final void setCustomClassLoader(InstanceDataObject ido, ClassLoader cl) {
        ido.setCustomClassLoader(cl);
    }
}
