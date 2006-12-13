/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JComponent;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;

import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.FunctionBreakpointPanel;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpointPanel;


/**
 * @author   Jan Jancura and Gordon Prieur
 */
public class BreakpointsActionsProvider implements NodeActionsProviderFilter {
    
    private static final Action GO_TO_SOURCE_ACTION = Models.createAction(
        loc("CTL_Breakpoint_GoToSource_Label"), // NOI18N
        new Models.ActionPerformer() {
            public boolean isEnabled(Object node) {
                return true;
            }
            public void perform(Object[] nodes) {
                goToSource((LineBreakpoint) nodes[0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    private static final Action CUSTOMIZE_ACTION = Models.createAction(
        loc("CTL_Breakpoint_Customize_Label"), // NOI18N
        new Models.ActionPerformer() {
            public boolean isEnabled(Object node) {
                return true;
            }
            public void perform(Object[] nodes) {
                customize((Breakpoint) nodes[0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
    private static String loc(String key) {
        return NbBundle.getBundle(BreakpointsActionsProvider.class).getString(key);
    }

    public Action[] getActions(NodeActionsProvider original, Object node) 
    throws UnknownTypeException {
        if (!(node instanceof GdbBreakpoint)) 
            return original.getActions(node);
        
        Action[] oas = original.getActions(node);
        if (node instanceof LineBreakpoint) {
            Action[] as = new Action[oas.length + 3];
            as [0] = GO_TO_SOURCE_ACTION;
            as [1] = null;
            System.arraycopy(oas, 0, as, 2, oas.length);
            as[as.length - 1] = CUSTOMIZE_ACTION;
            return as;
        }
        Action[] as = new Action[oas.length + 1];
        System.arraycopy(oas, 0, as, 0, oas.length);
        as[as.length - 1] = CUSTOMIZE_ACTION;
        return as;
    }
    
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof LineBreakpoint) 
            goToSource((LineBreakpoint) node);
        else if (node instanceof GdbBreakpoint) 
            customize((Breakpoint) node);
        else
            original.performDefaultAction(node);
    }

    public void addModelListener(ModelListener l) {
    }

    public void removeModelListener(ModelListener l) {
    }

    public static void customize(Breakpoint b) {
        JComponent c = null;
        if (b instanceof LineBreakpoint) {
            c = new LineBreakpointPanel((LineBreakpoint) b);
        } else if (b instanceof FunctionBreakpoint) {
            c = new FunctionBreakpointPanel((FunctionBreakpoint) b);
        } else {
	    return; // should never happen (ie, its a developer error)
	}

        c.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(BreakpointsActionsProvider.class, "ACSD_Breakpoint_Customizer_Dialog")); // NOI18N
        HelpCtx helpCtx = HelpCtx.findHelp(c);
        if (helpCtx == null) {
            helpCtx = new HelpCtx("debug.add.breakpoint");  // FIXUP - Whats our help ID?
        }
        final Controller[] cPtr = new Controller[] { (Controller) c };
        final DialogDescriptor[] descriptorPtr = new DialogDescriptor[1];
        final Dialog[] dialogPtr = new Dialog[1];
        ActionListener buttonsActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (descriptorPtr[0].getValue() == DialogDescriptor.OK_OPTION) {
                    boolean ok = cPtr[0].ok();
                    if (ok) {
                        dialogPtr[0].setVisible(false);
                    }
                } else {
                    dialogPtr[0].setVisible(false);
                }
            }
        };
        DialogDescriptor descriptor = new DialogDescriptor(c, NbBundle.getMessage (
                BreakpointsActionsProvider.class, "CTL_Breakpoint_Customizer_Title"), // NOI18N
            
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN,
            helpCtx,
            buttonsActionListener
        );
        descriptor.setClosingOptions(new Object[] {});
        Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        d.pack();
        descriptorPtr[0] = descriptor;
        dialogPtr[0] = d;
        d.setVisible(true);
    }
    
    private static void goToSource(LineBreakpoint b) {
        EditorContextBridge.showSource(b, null);
    }
}
