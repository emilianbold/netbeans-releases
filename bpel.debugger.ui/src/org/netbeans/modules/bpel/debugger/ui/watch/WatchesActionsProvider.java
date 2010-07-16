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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.watch;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.KeyStroke;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.DialogDisplayer;


/**
 * @author   Alexander Zgursky
 */
public class WatchesActionsProvider implements NodeActionsProvider {    
    
    private static final String NEW_WATCH_ACTION_NAME = NbBundle.getMessage(
                    WatchesActionsProvider.class, 
                    "CTL_WatchAction_AddNew");
    
    private static final String DELETE_ALL_ACTION_NAME = NbBundle.getMessage(
                    WatchesActionsProvider.class, 
                    "CTL_WatchAction_DeleteAll");
    
    private static final String DELETE_ACTION_NAME = NbBundle.getMessage(
                    WatchesActionsProvider.class, 
                    "CTL_WatchAction_Delete");
    
    private static final String CUSTOMIZE_ACTION_NAME = NbBundle.getMessage(
                    WatchesActionsProvider.class, 
                    "CTL_WatchAction_Customize");
    
    private static final Models.ActionPerformer DELETE_ACTION_PERFORMER = 
            new Models.ActionPerformer () {
        
        public boolean isEnabled(
                final Object node) {
            return true;
        }

        public void perform(final Object[] nodes) {
            for (int i = 0; i < nodes.length; i++) {
                ((BpelWatch) nodes[i]).remove();
            }
        }
    };  
    
    private static final Models.ActionPerformer CUSTOMIZE_ACTION_PERFORMER =
            new Models.ActionPerformer () {
        
        public boolean isEnabled(
                final Object node) {
            return true;
        }
        
        public void perform(
                final Object[] nodes) {
            customize((BpelWatch) nodes [0]);
        }
    };
    
    private static final Action NEW_WATCH_ACTION = 
            new AbstractAction(NEW_WATCH_ACTION_NAME) {
        
        public void actionPerformed (ActionEvent e) {
            newWatch();
        }
    };
    
    private static final Action DELETE_ALL_ACTION = 
            new AbstractAction(DELETE_ALL_ACTION_NAME) {
        
        public void actionPerformed (ActionEvent e) {
            DebuggerManager.getDebuggerManager ().removeAllWatches ();
        }
    };
    
    private static final Action DELETE_ACTION = Models.createAction(
            DELETE_ACTION_NAME, 
            DELETE_ACTION_PERFORMER,
            Models.MULTISELECTION_TYPE_ANY
    );
    
    private static final Action CUSTOMIZE_ACTION = Models.createAction (
            CUSTOMIZE_ACTION_NAME,
            CUSTOMIZE_ACTION_PERFORMER,
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    static { 
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
    };
    
    public Action[] getActions(
            final Object node) throws UnknownTypeException {
        
        if (node == TreeModel.ROOT) { 
            return new Action [] {
                NEW_WATCH_ACTION,
                null,
                DELETE_ALL_ACTION
            };
        }

        if (node == WatchesTreeModel.ADD_NEW_WATCH) {
            return new Action [] {
                NEW_WATCH_ACTION,
                null,
                DELETE_ACTION,
                DELETE_ALL_ACTION
            };
        }
        
        if (node instanceof BpelWatch) {
            return new Action [] {
                NEW_WATCH_ACTION,
                null,
                DELETE_ACTION,
                DELETE_ALL_ACTION,
                null,
                CUSTOMIZE_ACTION
            };
        }
        
        throw new UnknownTypeException(node);
    }
    
    public void performDefaultAction(
            final Object node) throws UnknownTypeException {
        
        if (node == TreeModel.ROOT) {
            return;
        }

        if (node == WatchesTreeModel.ADD_NEW_WATCH) {
            newWatch();
            return;
        }

        if (node instanceof BpelWatch) {
            return;
        }
        
        throw new UnknownTypeException(node);
    }
    
    private static void customize(
            final BpelWatch w) {
        
        final WatchPanel watchPanel = new WatchPanel(w.getExpression());
        final JComponent component = watchPanel.getPanel();

        final String dialogTitle = NbBundle.getMessage(
                WatchesActionsProvider.class, 
                "CTL_Edit_Watch_Dialog_Title", // NOI18N
                w.getExpression());
        
        final DialogDescriptor descriptor = new DialogDescriptor(
                component, 
                dialogTitle
        );
        descriptor.setHelpCtx(new HelpCtx("debug.customize.watch")); //NOI18N
        
        final Dialog dialog = 
                DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        dialog.dispose();
        
        if (descriptor.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }
        
        if (component.getClientProperty("WatchCanceled") != null) { //NOI18N
            return;
        }
        
        w.setExpression(watchPanel.getExpression());
    }
    
    private static void newWatch() {
        
        final WatchPanel watchPanel = new WatchPanel("");
        final JComponent component = watchPanel.getPanel();

        final String dialogTitle = NbBundle.getMessage(
                WatchesActionsProvider.class, 
                "CTL_New_Watch_Dialog_Title"); // NOI18N
        
        final DialogDescriptor descriptor = new DialogDescriptor (
            component,
            dialogTitle
        );
        descriptor.setHelpCtx(new HelpCtx("debug.new.watch")); // NOI18N
        
        final Dialog dialog = 
                DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        dialog.dispose();

        if (descriptor.getValue () != DialogDescriptor.OK_OPTION) {
            return;
        }
        
        if (component.getClientProperty("WatchCanceled") != null) { //NOI18N
            return ;
        }
        
        DebuggerManager.getDebuggerManager().createWatch(
                watchPanel.getExpression());
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class WatchPanel {

        private JPanel panel;
        private JTextField textField;
        private String expression;

        public WatchPanel(
                final String expression) {
            this.expression = expression;
        }

        public JComponent getPanel() {
            if (panel != null) {
                return panel;
            }
            
            final ResourceBundle bundle = 
                    NbBundle.getBundle(WatchesActionsProvider.class);
            
            panel = new JPanel();
            textField = new JTextField (25);
            
            textField.getAccessibleContext().setAccessibleDescription(
                    bundle.getString("ACSD_CTL_Watch_Name")); // NOI18N
            textField.setBorder (
                new CompoundBorder(textField.getBorder(),
                new EmptyBorder(2, 0, 2, 0))
            );
            textField.setText(expression);
            
            final JLabel textLabel = new JLabel(
                    bundle.getString("CTL_Watch_Name")); // NOI18N
            textLabel.setBorder (new EmptyBorder (0, 0, 0, 10));
            textLabel.setLabelFor (textField);
            textLabel.setDisplayedMnemonic(
                bundle.getString("CTL_Watch_Name_Mnemonic").charAt(0) // NOI18N
            );
            
            panel.getAccessibleContext().setAccessibleDescription(
                    bundle.getString("ACSD_WatchPanel")); // NOI18N
            panel.setLayout (new BorderLayout ());
            panel.setBorder (new EmptyBorder (11, 12, 1, 11));
            
            panel.add(BorderLayout.WEST, textLabel);
            panel.add(BorderLayout.CENTER, textField);
            
            textField.selectAll();
            textField.requestFocus();
            
            return panel;
        }

        public String getExpression() {
            return textField.getText().trim();
        }
    }
}
