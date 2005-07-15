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

import java.awt.Window;
import java.util.*;
import java.beans.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.HashSet;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.Breakpoint.*;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.spi.debugger.ui.Controller;


/**
* New Breakpint Dialog panel.
*
* @author  Jan Jacura
*/
public class AddBreakpointPanel extends javax.swing.JPanel {

    public static final String PROP_TYPE = "type";
    
    
    // variables ...............................................................
    
    private boolean                 doNotRefresh = false;
    /** List of cathegories. */
    private HashSet                 cathegories = new HashSet ();
    /** Types in currently selected cathegory. */
    private ArrayList               types = new ArrayList ();
    /** Currently selected type. */
    private BreakpointType          type;
    private JComponent              customizer;

    private javax.swing.JLabel      jLabel1;
    private javax.swing.JLabel      jLabel2;
    private javax.swing.JComboBox   cbCathegory;
    private javax.swing.JComboBox   cbEvents;
    private javax.swing.JPanel      pEvent;
    /** <CODE>HelpCtx</CODE> of the selected breakpoint type's customizer */
    private HelpCtx                 helpCtx;
    private List                    breakpointTypes;


    // init ....................................................................

    /** Creates new form AddBreakpointPanel */
    public AddBreakpointPanel () {
        breakpointTypes = DebuggerManager.getDebuggerManager ().lookup 
            (null, BreakpointType.class);
        int i, k = breakpointTypes.size ();
        String def = null;
        cbCathegory = new javax.swing.JComboBox ();
        for (i = 0; i < k; i++) {
            BreakpointType bt = (BreakpointType) breakpointTypes.get (i);
            String dn = bt.getCategoryDisplayName ();
            if (!cathegories.contains (dn)) {
                cathegories.add (dn);
                cbCathegory.addItem (dn);
            }
            if (bt.isDefault ())
                def = dn;
        }
        
        initComponents ();
        if (def != null) 
            selectCathegory (def);
        else
        if (breakpointTypes.size () > 0)
            selectCathegory (((BreakpointType) breakpointTypes.get (0)).
                getCategoryDisplayName ());
    }


    // public interface ........................................................

    public BreakpointType getType () {
        return type;
    }
    
    public Controller getController () {
        return (Controller) customizer;
    }
    
    
    // other methods ...........................................................
    
    private void initComponents () {
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle (AddBreakpointPanel.class).getString ("ACSD_AddBreakpointPanel")); // NOI18N
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        if (cathegories.size () > 1) {
                jLabel1 = new javax.swing.JLabel ();
                jLabel1.setText (NbBundle.getBundle (AddBreakpointPanel.class).
                    getString ("CTL_Breakpoint_cathegory")); // NOI18N
                jLabel1.setDisplayedMnemonic (
                    NbBundle.getBundle (AddBreakpointPanel.class).
                        getString ("CTL_Breakpoint_cathegory_mnemonic").charAt (0) // NOI18N
                );
                gridBagConstraints1 = new java.awt.GridBagConstraints ();
                gridBagConstraints1.gridwidth = 2;
                gridBagConstraints1.insets = new java.awt.Insets (12, 12, 0, 0);
            add (jLabel1, gridBagConstraints1);

                cbCathegory.addActionListener (new java.awt.event.ActionListener () {
                    public void actionPerformed (java.awt.event.ActionEvent evt) {
                        cbCathegoryActionPerformed (evt);
                    }
                });
                cbCathegory.getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle (AddBreakpointPanel.class).getString ("ACSD_CTL_Breakpoint_cathegory")); // NOI18N
                jLabel1.setLabelFor (cbCathegory);
                gridBagConstraints1 = new java.awt.GridBagConstraints ();
                gridBagConstraints1.gridwidth = 2;
                gridBagConstraints1.insets = new java.awt.Insets (12, 12, 0, 0);
                gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            add (cbCathegory, gridBagConstraints1);
        }

            jLabel2 = new javax.swing.JLabel ();
            jLabel2.setText (NbBundle.getBundle (AddBreakpointPanel.class).
                getString ("CTL_Breakpoint_type"));
            jLabel2.setDisplayedMnemonic (
                NbBundle.getBundle (AddBreakpointPanel.class).
                    getString ("CTL_Breakpoint_type_mnemonic").charAt (0)
            );
            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.gridwidth = 2;
            gridBagConstraints1.insets = new java.awt.Insets (12, 12, 0, 0);
        add (jLabel2, gridBagConstraints1);
            
            cbEvents = new javax.swing.JComboBox ();
            cbEvents.addActionListener (new java.awt.event.ActionListener () {
                public void actionPerformed (java.awt.event.ActionEvent evt) {
                    cbEventsActionPerformed ();
                }
            });
            cbEvents.getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle (AddBreakpointPanel.class).getString ("ACSD_CTL_Breakpoint_type")); // NOI18N
            cbEvents.setMaximumRowCount (12);
            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.insets = new java.awt.Insets (12, 12, 0, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add (cbEvents, gridBagConstraints1);
            jLabel2.setLabelFor (cbEvents);
            pEvent = new javax.swing.JPanel ();
            pEvent.setLayout (new java.awt.BorderLayout ());
            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.insets = new java.awt.Insets (9, 9, 0, 9);
            gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.weighty = 1.0;
        add (pEvent, gridBagConstraints1);
    }

    private void cbEventsActionPerformed () {
        // Add your handling code here:
        if (doNotRefresh) return;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                boolean pv = cbEvents.isPopupVisible ();
                int j = cbEvents.getSelectedIndex ();
                if (j < 0) return;
                update ((BreakpointType) types.get (j));
                if (pv)
                    SwingUtilities.invokeLater (new Runnable () {
                        public void run () {
                            cbEvents.setPopupVisible (true);
                        }
                    });
                    //cbEvents.setPopupVisible (true);
            }
        });
    }

    private void cbCathegoryActionPerformed (java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        if (doNotRefresh) return;
        String c = (String) cbCathegory.getSelectedItem ();
        if (c == null) return;
        selectCathegory (c);
    }
    
    private void selectCathegory (String c) {
        doNotRefresh = true;
        cbEvents.removeAllItems ();
        types = new ArrayList ();
        int i, k = breakpointTypes.size (), defIndex = 0;
        for (i = 0; i < k; i++) {
            BreakpointType bt = (BreakpointType) breakpointTypes.get (i);
            if (!bt.getCategoryDisplayName ().equals (c))
                continue;
            cbEvents.addItem (bt.getTypeDisplayName ());
            types.add (bt);
            if (bt.isDefault ())
                defIndex = cbEvents.getItemCount () - 1;
        }
        doNotRefresh = false;
        if (defIndex < cbEvents.getItemCount ())
            cbEvents.setSelectedIndex (defIndex);
    }
    
    /**
     * Returns <CODE>HelpCtx</CODE> of the selected breakpoint type's customizer.
     * It is used in {@link AddBreakpointAction.AddBreakpointDialogManager}.
     */
    HelpCtx getHelpCtx() {
        return helpCtx;
    }

    private void update (BreakpointType t) {
        pEvent.removeAll ();
        DebuggerManager d = DebuggerManager.getDebuggerManager ();
        BreakpointType old = type;
        type = t;
        customizer = type.getCustomizer ();
        if (customizer == null) return;

        //Set HelpCtx. This method must be called _before_ the customizer
        //is added to some container, otherwise HelpCtx.findHelp(...) would
        //query also the customizer's parents.
        helpCtx = HelpCtx.findHelp (customizer);

        pEvent.add (customizer, "Center"); // NOI18N
        pEvent.getAccessibleContext ().setAccessibleDescription (
            customizer.getAccessibleContext ().getAccessibleDescription ()
        );
        customizer.getAccessibleContext ().setAccessibleName (
            pEvent.getAccessibleContext ().getAccessibleName ()
        );
        revalidate ();
        Window w = SwingUtilities.windowForComponent (this);
        if (w == null) return;
        w.pack ();
        firePropertyChange (PROP_TYPE, old, type);
    }
}

