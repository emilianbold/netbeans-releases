/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;


/**
 * Represents some variable in debugged JVM. 
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @see LocalVariable
 * @see Field
 * @see This
 * @see Super
 * @see JPDAThread#getContendedMonitor
 * @see JPDAThread#getOwnedMonitors
 *
 * @author   Jan Jancura
 */
public interface Variable {

    /**
     * Type of this instance.
     *
     * @return type of this instance
     */
    public abstract String getType ();

    /**
     * Text representation of current value of "this" variable.
     *
     * @return text representation of current value of "this" variable
     */
    public abstract String getValue ();
}
