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

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.datatransfer.*;

import java.io.IOException;


/** Clipboard operation providing one kind of paste action. Used by
* <a href="@org-openide-nodes/org/openide/nodes.Node#getPasteTypes">Node.getPasteTypes</a>.
*
* @author Petr Hamernik
*/
public abstract class PasteType extends Object implements HelpCtx.Provider {
    /** Display name for the paste action. This should be
    * presented as an item in a menu.
    *
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(PasteType.class).getString("Paste");
    }

    /** Help content for the action.
    * @return the help context
    */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Perform the paste action.
    * @return transferable which should be inserted into the clipboard after the
    *         paste action. It can be <code>null</code>, meaning that the clipboard content
    *         is not affected. Use e.g. {@link ExTransferable#EMPTY} to clear it.
    * @throws IOException if something fails
    */
    public abstract Transferable paste() throws IOException;

    /* JST: Originally designed for dnd and it now uses getDropType () of a node.
    *
    * Perform the paste action at an index.
    * @see NewType#createAt(int)
    * @param indx index to insert into, can be ignored if not supported
    * @return new transferable to be inserted into the clipboard
    *  public Transferable pasteAt (int indx) throws IOException {
      return paste ();
    }
    */
}
