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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.modules.debugger.ui.Utils;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
* Connects debugger to some currently running VM.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class ConnectAction extends AbstractAction {
    
    private Dialog dialog;
    private JButton bOk;
    private JButton bCancel;

    
    public ConnectAction () {
        putValue (
            Action.NAME, 
            NbBundle.getMessage (
                ConnectAction.class, 
                "CTL_Connect"
            )
        );
        putValue (
            Action.SMALL_ICON, 
            Utils.getIcon (
                "org/netbeans/modules/debugger/resources/actions/Attach" // NOI18N
            )
        );
        putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/Attach.gif" // NOI18N
        );
    }
    
    public void actionPerformed (ActionEvent evt) {
        bOk = new JButton (NbBundle.getMessage (ConnectAction.class, "CTL_Ok")); // NOI18N
        bCancel = new JButton (NbBundle.getMessage (ConnectAction.class, "CTL_Cancel")); // NOI18N
        bOk.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage (ConnectAction.class, "ACSD_CTL_Ok")); // NOI18N
        bCancel.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage (ConnectAction.class, "ACSD_CTL_Cancel")); // NOI18N
        ConnectorPanel cp = new ConnectorPanel ();
        DialogDescriptor descr = new DialogDescriptor (
            cp,
            NbBundle.getMessage (ConnectAction.class, "CTL_Connect_to_running_process"),
            true, // modal
            new ConnectListener (cp)
        );
        descr.setOptions (new JButton[] {
            bOk, bCancel
        });
        descr.setClosingOptions (new Object [0]);
        dialog = DialogDisplayer.getDefault ().createDialog (descr);
        dialog.setVisible(true);
    }


    // innerclasses ............................................................
    private class ConnectListener implements ActionListener, PropertyChangeListener {
        
        ConnectorPanel connectorPanel;
        Controller controller;
        
        ConnectListener (ConnectorPanel connectorPanel) {
            this.connectorPanel = connectorPanel;
            startListening();
            setValid();
            connectorPanel.addPropertyChangeListener(this);
        }
        
        public void actionPerformed (ActionEvent e) {
            boolean okPressed = bOk.equals (e.getSource ());
            Controller controller = connectorPanel.getController ();
            boolean close = false;
            if (okPressed) {
                close = controller.ok ();
            } else {
                close = controller.cancel ();
            }
            if (!close) return;
            connectorPanel.removePropertyChangeListener (this);
            stopListening ();
            dialog.setVisible (false);
            dialog.dispose ();
            dialog = null;
        }
        
        void startListening () {
            controller = connectorPanel.getController ();
            if (controller == null) return;
            controller.addPropertyChangeListener (this);
        }
        
        void stopListening () {
            if (controller == null) return;
            controller.removePropertyChangeListener (this);
            controller = null;
        }

        void setValid () {
            Controller controller = connectorPanel.getController ();
            if (controller == null) {
                bOk.setEnabled (false);
                return;
            }
            bOk.setEnabled (controller.isValid ());
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName () == ConnectorPanel.PROP_TYPE) {
                stopListening ();
                setValid ();
                startListening ();
            } else if (evt.getPropertyName () == Controller.PROP_VALID) {
                setValid ();
            }
        }
        
    }
}


