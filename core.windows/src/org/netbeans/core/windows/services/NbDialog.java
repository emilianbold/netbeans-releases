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

package org.netbeans.core.windows.services;

import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

import java.awt.*;
import java.awt.event.ActionListener;

// XXx Before as org.netbeans.core.NbDialog

/** Default implementation of Dialog created from DialogDescriptor.
*
* @author Ian Formanek
*/
final class NbDialog extends NbPresenter {
    static final long serialVersionUID =-4508637164126678997L;

    /** Creates a new Dialog from specified DialogDescriptor
    * @param d The DialogDescriptor to create the dialog from
    * @param owner Owner of this dialog.
    */
    public NbDialog (DialogDescriptor d, Frame owner) {
        super (d, owner, d.isModal ());
    }

    /** Creates a new Dialog from specified DialogDescriptor
    * @param d The DialogDescriptor to create the dialog from
    * @param owner Owner of this dialog.
    */
    public NbDialog (DialogDescriptor d, Dialog owner) {
        super (d, owner, d.isModal ());
    }

    /** Geter for help.
    */
    protected HelpCtx getHelpCtx () {
        return ((DialogDescriptor)descriptor).getHelpCtx ();
    }

    /** Options align.
    */
    protected int getOptionsAlign () {
        return ((DialogDescriptor)descriptor).getOptionsAlign ();
    }

    /** Getter for button listener or null
    */
    protected ActionListener getButtonListener () {
        return ((DialogDescriptor)descriptor).getButtonListener ();
    }

    /** Closing options.
    */
    protected Object[] getClosingOptions () {
        return ((DialogDescriptor)descriptor).getClosingOptions ();
    }

}
