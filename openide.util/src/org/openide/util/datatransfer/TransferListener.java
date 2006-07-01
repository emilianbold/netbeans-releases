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


/** Allows listening to progress of manipulation with ExTransferable.
* So it is notified when the transferable is accepted/rejected by
* an operation or if it is released from a clipboard.
*
* @author Jaroslav Tulach
*/
public interface TransferListener extends java.util.EventListener {
    /** Accepted by a drop operation.
    * @param action One of java.awt.dnd.DndConstants like ACTION_COPY, ACTION_MOVE,
    * ACTION_LINK.
    */
    public void accepted(int action);

    /** The transfer has been rejected.
    */
    public void rejected();

    /** Released from a clipboard.
    */
    public void ownershipLost();
}
