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

package org.netbeans.modules.php.dbgp.models;

import java.awt.Dialog;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.netbeans.modules.php.dbgp.models.nodes.ScriptWatchEvaluating;
import org.netbeans.modules.php.dbgp.models.nodes.VariableNode;
import org.netbeans.modules.php.dbgp.ui.WatchPanel;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author   ads
 */
public class WatchesActionsProvider implements NodeActionsProviderFilter {

    private static final String DEBUG_ADD_WATCH 
                = "debug.add.watch";                            // NOI18N

    private static final String DIALOG_TITLE 
                = "CTL_WatchDialog_Title";                      // NOI18N

    private static final String WATCH_ACTION_CUSTOMIZE 
                = "CTL_WatchAction_Customize";                  // NOI18N

    private static final String WATCH_ACTION_DELETE 
                = "CTL_WatchAction_Delete";                     // NOI18N 

    private static final Action DELETE_ACTION = Models.createAction(
            NbBundle.getBundle(WatchesActionsProvider.class).
            getString(WATCH_ACTION_DELETE),
            new Models.ActionPerformer() {
                @Override
                public boolean isEnabled(Object node) {
                    return true;
                }
                @Override
                public void perform(Object[] nodes) {
                    for( Object node : nodes ) {
                        ((ScriptWatchEvaluating) node).remove();
                    }
                }
            },
            Models.MULTISELECTION_TYPE_ANY
    );
    
    static { 
        DELETE_ACTION.putValue(
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke("DELETE") // NOI18N
        );
    };
    
    private static final Action CUSTOMIZE_ACTION = Models.createAction(
        NbBundle.getBundle(WatchesActionsProvider.class).
            getString(WATCH_ACTION_CUSTOMIZE),
        new Models.ActionPerformer() {
            @Override
            public boolean isEnabled(Object node) {
                return true;
            }
            @Override
            public void perform(Object[] nodes) {
                customize((ScriptWatchEvaluating) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeActionsProviderFilter#getActions(org.netbeans.spi.viewmodel.NodeActionsProvider, java.lang.Object)
     */
    @Override
    public Action[] getActions( NodeActionsProvider original, Object node )
            throws UnknownTypeException
    {
        Action[] actions;
        try {
            actions = original.getActions(node);
        }
        catch (UnknownTypeException e ) {
            actions = new Action[0];
        }
        Action[] varActions = getActions( node);
        Action[] result = new Action[ actions.length + varActions.length ];
        System.arraycopy( actions, 0 , result , 0, actions.length);
        System.arraycopy( varActions, 0 , result , actions.length , 
                varActions.length);
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.NodeActionsProviderFilter#performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider, java.lang.Object)
     */
    @Override
    public void performDefaultAction( NodeActionsProvider original, Object node )
            throws UnknownTypeException
    {
        if ( node instanceof ScriptWatchEvaluating ) {
            performDefaultAction(node);
        }
        else {
            original.performDefaultAction(node);
        }
    }
    
    
    private Action[] getActions(Object node) throws UnknownTypeException {
        if ( node == TreeModel.ROOT ) {
            return new Action[0];
        }
        if(node instanceof ScriptWatchEvaluating) {
            return new Action [] {
                DELETE_ACTION,
                null,
                CUSTOMIZE_ACTION
            };
        }
        
        throw new UnknownTypeException(node);
    }
    
    private void performDefaultAction(Object node) throws UnknownTypeException {
        if(node == TreeModel.ROOT || node instanceof VariableNode) {
            return;
        } 
        throw new UnknownTypeException(node);
    }

    /*
     * Stolen from org.netbeans.modules.debugger.ui.models.WatchesActionsProvider
     */
    private static void customize(ScriptWatchEvaluating watchEvaluating) {
        WatchPanel watchPanel = new WatchPanel(watchEvaluating.getExpression());
        JComponent panel = watchPanel.getPanel();

        DialogDescriptor descriptor = new DialogDescriptor(panel,
            NbBundle.getMessage(WatchesActionsProvider.class, 
                    DIALOG_TITLE, 
                    watchEvaluating.getExpression())
        );
        descriptor.setHelpCtx(new HelpCtx(DEBUG_ADD_WATCH));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        dialog.dispose();

        if (descriptor.getValue() != org.openide.DialogDescriptor.OK_OPTION) {
            return;
        }
        watchEvaluating.setExpression(watchPanel.getExpression());
    }
}
