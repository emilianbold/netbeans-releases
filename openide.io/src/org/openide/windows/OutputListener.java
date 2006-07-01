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
