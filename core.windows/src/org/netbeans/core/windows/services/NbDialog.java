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

package org.netbeans.core.windows.services;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

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
