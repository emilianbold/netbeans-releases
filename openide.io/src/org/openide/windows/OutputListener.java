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

package org.openide.windows;

/** Listener to actions taken on a line in the Output Window.
*
* @author Jaroslav Tulach
* @version 0.11 Dec 01, 1997
*/
public interface OutputListener extends java.util.EventListener {
    /** Called when a line is selected.
    * @param ev the event describing the line
    */
    public void outputLineSelected (OutputEvent ev);

    /** Called when some sort of action is performed on a line.
    * @param ev the event describing the line
    */
    public void outputLineAction (OutputEvent ev);

    /** Called when a line is cleared from the buffer of known lines.
    * @param ev the event describing the line
    */
    public void outputLineCleared (OutputEvent ev);
}
