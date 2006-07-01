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

/** Event fired when something happens to a line in the Output Window.
*
* @author Jaroslav Tulach, Petr Hamernik
* @version 0.11 Dec 01, 1997
*/
public abstract class OutputEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4809584286971828815L;
    /** Create an event.
    * @param src the tab in question
    */
    public OutputEvent (InputOutput src) {
        super (src);
    }

    /** Get the text on the line.
    * @return the text
    */
    public abstract String getLine ();

    /** Get the Output Window tab in question.
    * @return the tab
    */
    public InputOutput getInputOutput() {
        return (InputOutput) getSource();
    }
}
