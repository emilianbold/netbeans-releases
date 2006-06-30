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