/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui.actions;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;

import org.openide.util.NbBundle;


public class ConnectorPanel extends JPanel implements ActionListener, 
Controller {

    /** Contains list of AttachType names.*/
    private JComboBox             cbAttachTypes;
    /** Switches off listenning on cbAttachTypes.*/
    private boolean               doNotListen;
    /** Contains list of installed AttachTypes.*/
    private List                  attachTypes;
    /** Currentlydisplayed panel.*/
    private JComponent            currentPanel;
//    private DebuggerProjectSettings    connectSettings;


    public ConnectorPanel ()  {
        getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, "ACSD_ConnectorPanel")
        );
        cbAttachTypes = new JComboBox ();
        cbAttachTypes.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, 
                "ACSD_CTL_Connect_through")// NOI18N
        ); 
        attachTypes = DebuggerManager.getDebuggerManager ().lookup (
            null, AttachType.class
        );
//        DebuggerProjectSettings connectSettings = (DebuggerProjectSettings) 
//            DebuggerProjectSettings.findObject 
//            (DebuggerProjectSettings.class, true);
        String defaultAttachTypeName = null; //connectSettings.getLastDebuggerType ();
        int defaultIndex = 0;
        int i, k = attachTypes.size ();
        for (i = 0; i < k; i++) {
            AttachType at = (AttachType) attachTypes.get (i);
            cbAttachTypes.addItem (at.getTypeDisplayName ());
            if ( (defaultAttachTypeName != null) &&
                 (defaultAttachTypeName.equals (at.getClass ().getName ()))
            )
                defaultIndex = i;
        }

        cbAttachTypes.setActionCommand ("SwitchMe!"); // NOI18N
        cbAttachTypes.addActionListener (this);

        setLayout (new GridBagLayout ());
        setBorder (new EmptyBorder (11, 11, 0, 10));
        refresh (defaultIndex);
    }
    
    private void refresh (int index) {
        JLabel cbLabel = new JLabel (
            NbBundle.getMessage (ConnectorPanel.class, "CTL_Connect_through")
        ); // NOI18N
        cbLabel.setDisplayedMnemonic (
            NbBundle.getMessage (ConnectorPanel.class, 
                "CTL_Connect_through_Mnemonic").charAt (0)
        ); // NOI18N
        cbLabel.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, 
                "ACSD_CTL_Connect_through")// NOI18N
        ); 
        cbLabel.setLabelFor (cbAttachTypes);

        GridBagConstraints c = new GridBagConstraints ();
        c.insets = new Insets (0, 0, 6, 6);
        add (cbLabel, c);
        c = new GridBagConstraints ();
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.gridwidth = 0;
        c.insets = new Insets (0, 3, 6, 0);
        doNotListen = true;
        cbAttachTypes.setSelectedIndex (index);
        doNotListen = false;
        add (cbAttachTypes, c);
        c.insets = new Insets (0, 0, 6, 0);
        add (new JSeparator(), c);
        c = new GridBagConstraints ();
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = java.awt.GridBagConstraints.BOTH;
        c.gridwidth = 0;
        AttachType attachType = (AttachType) attachTypes.get (index);
        currentPanel = attachType.getCustomizer ();
        
        add (currentPanel, c);
    }


    /**
     * Called when a user selects debugger type in a combo-box.
     */
    public void actionPerformed (ActionEvent e) {
        if (doNotListen) return;
        if (e.getActionCommand ().equals ("SwitchMe!")); // NOI18N
        removeAll ();
        refresh (((JComboBox) e.getSource ()).getSelectedIndex ());
        Component w = getParent ();
        while (!(w instanceof Window))
            w = w.getParent ();
        if (w != null) ((Window) w).pack (); // ugly hack...
        return;
    }
    
    public boolean cancel () {
        return ((Controller) currentPanel).cancel ();
    }
    
    public boolean ok () {
        return ((Controller) currentPanel).ok ();
    }    
}



