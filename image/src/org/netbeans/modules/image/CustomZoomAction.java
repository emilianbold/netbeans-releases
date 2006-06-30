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


package org.netbeans.modules.image;


import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.DialogDisplayer;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/** Action that can always be invoked and work procedurally.
 *
 * @author  Lukas Tadial
 */
public class CustomZoomAction extends CallableSystemAction {

    
    /** Generated serial version UID. */
    static final long serialVersionUID = 8247068408606777895L;
    
    
    /** Actually performs action. */
    public void performAction () {
        final Dialog[] dialogs = new Dialog[1];
        final CustomZoomPanel zoomPanel = new CustomZoomPanel();

        zoomPanel.setEnlargeFactor(1);
        zoomPanel.setDecreaseFactor(1);
        
        DialogDescriptor dd = new DialogDescriptor(
            zoomPanel,
            NbBundle.getBundle(CustomZoomAction.class).getString("LBL_CustomZoomAction"),
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (ev.getSource() == DialogDescriptor.OK_OPTION) {
                        int enlargeFactor = 1, decreaseFactor = 1;
                        
                        try {
                            enlargeFactor = zoomPanel.getEnlargeFactor();
                            decreaseFactor = zoomPanel.getDecreaseFactor();
                        } catch (NumberFormatException nfe) {
                            notifyInvalidInput();
                            return;
                        }
                        
                        // Invalid values.
                        if(enlargeFactor == 0 || decreaseFactor == 0) {
                            notifyInvalidInput();
                            return;
                        }
                        
                        performZoom(enlargeFactor, decreaseFactor);
                        
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    } else {
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    }
                }        
                
                private void notifyInvalidInput() {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getBundle(CustomZoomAction.class).getString("MSG_InvalidValues"),
                        NotifyDescriptor.ERROR_MESSAGE
                    ));
                }
                
            } // End of annonymnous ActionListener.
        );
        dialogs[0] = DialogDisplayer.getDefault().createDialog(dd);
        dialogs[0].setVisible(true);
        
    }

    /** Performs customized zoom. */
    private void performZoom(int enlargeFactor, int decreaseFactor) {
        TopComponent currentComponent = TopComponent.getRegistry().getActivated();
        if(currentComponent instanceof ImageViewer)
            ((ImageViewer)currentComponent).customZoom(enlargeFactor, decreaseFactor);
    }

    /** Gets action name. Implements superclass abstract method. */
    public String getName () {
        return NbBundle.getBundle(CustomZoomAction.class).getString("LBL_CustomZoom");
    }
    
    /** Gets action help context. Implemenets superclass abstract method.*/
    public HelpCtx getHelpCtx () {
        return new HelpCtx(CustomZoomAction.class);
    }
    
    /** Gets icon resource. Overrides superclass method. */
    protected String iconResource() {
        return "org/netbeans/modules/image/customZoom.gif"; // NOI18N
    }
    
}
