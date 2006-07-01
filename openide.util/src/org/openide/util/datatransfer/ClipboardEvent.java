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
package org.openide.util.datatransfer;

import java.awt.datatransfer.*;


/** Event describing change of clipboard content.
*
* @see ExClipboard
*
* @author Jaroslav Tulach
* @version 0.11, May 22, 1997
*/
public final class ClipboardEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -468077075889138021L;

    /** consumed */
    private boolean consumed = false;

    /**
    * @param c the clipboard
    */
    ClipboardEvent(ExClipboard c) {
        super(c);
    }

    /** Get the clipboard where operation occurred.
    * @return the clipboard
    */
    public ExClipboard getClipboard() {
        return (ExClipboard) getSource();
    }

    /** Marks this event consumed. Can be
    * used by listeners that are sure that their own reaction to the event
    * is really significant, to inform other listeners that they need not do anything.
    */
    public void consume() {
        consumed = true;
    }

    /** Has this event been consumed?
     * @return <code>true</code> if it has
    */
    public boolean isConsumed() {
        return consumed;
    }
}
