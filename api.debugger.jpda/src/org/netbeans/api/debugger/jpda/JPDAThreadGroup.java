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
 * Represents one Java thread group in debugged process.
 * 
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author Jan Jancura
 */
public interface JPDAThreadGroup  {

    
    /**
     * Getter for the name of thread group property.
     *
     * @return name of thread group
     */
    public abstract String getName ();

    /**
     * Returns parent thread group or null (for root thread group).
     *
     * @return parent thread group or null (for root thread group)
     */
    public abstract JPDAThreadGroup getParentThreadGroup ();
    
    /**
     * Returns this thread group's threads.
     *
     * @return threads from this thread group
     */
    public abstract JPDAThread[] getThreads ();
    
    /**
     * Returns this thread group's thread groups.
     *
     * @return thread groups s from this thread group
     */
    public abstract JPDAThreadGroup[] getThreadGroups ();
    
    /**
     * Suspends all threads and thread groups in this thread group.
     */
    public abstract void suspend ();
    
    /**
     * Unsuspends all threads and thread groups in this thread group.
     */
    public abstract void resume ();
}