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

package org.netbeans.core.execution;

/** allows other classes can check starting/finishing execution
*
* @author Ales Novak
* @version 0.10, Mar 04, 1998
*/
public interface ExecutionListener {
    /** called after begin of new execution */
    public void startedExecution(ExecutionEvent ev);

    /** called after end of execution */
    public void finishedExecution(ExecutionEvent ev);
}
