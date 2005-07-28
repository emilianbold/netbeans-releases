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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.Dialog;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;

import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.*;
import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
public class BreakpointsActionsProvider implements NodeActionsProviderFilter {
    
    private static final Action GO_TO_SOURCE_ACTION = Models.createAction (
        loc("CTL_Breakpoint_GoToSource_Label"), // NOI18N
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                goToSource ((LineBreakpoint) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    private static final Action CUSTOMIZE_ACTION = Models.createAction (
        loc("CTL_Breakpoint_Customize_Label"), // NOI18N
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                customize ((Breakpoint) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
    private static String loc(String key) {
        return NbBundle.getBundle(BreakpointsActionsProvider.class).getString(key);
    }

    public Action[] getActions (NodeActionsProvider original, Object node) 
    throws UnknownTypeException {
        if (!(node instanceof JPDABreakpoint)) 
            return original.getActions (node);
        
        Action[] oas = original.getActions (node);
        if (node instanceof LineBreakpoint) {
            Action[] as = new Action [oas.length + 3];
            as [0] = GO_TO_SOURCE_ACTION;
            as [1] = null;
            System.arraycopy (oas, 0, as, 2, oas.length);
            as [as.length - 1] = CUSTOMIZE_ACTION;
            return as;
        }
        Action[] as = new Action [oas.length + 1];
        System.arraycopy (oas, 0, as, 0, oas.length);
        as [as.length - 1] = CUSTOMIZE_ACTION;
        return as;
    }
    
    public void performDefaultAction (NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof LineBreakpoint) 
            goToSource ((LineBreakpoint) node);
        else
        if (node instanceof JPDABreakpoint) 
            customize ((Breakpoint) node);
        else
            original.performDefaultAction (node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }

    private static void customize (Breakpoint b) {
        JComponent c = null;
        if (b instanceof LineBreakpoint)
            c = new LineBreakpointPanel ((LineBreakpoint) b);
        else
        if (b instanceof FieldBreakpoint)
            c = new FieldBreakpointPanel ((FieldBreakpoint) b);
        else
        if (b instanceof ClassLoadUnloadBreakpoint)
            c = new ClassBreakpointPanel ((ClassLoadUnloadBreakpoint) b);
        else
        if (b instanceof MethodBreakpoint)
            c = new MethodBreakpointPanel ((MethodBreakpoint) b);
        else
        if (b instanceof ThreadBreakpoint)
            c = new ThreadBreakpointPanel ((ThreadBreakpoint) b);
        else
        if (b instanceof ExceptionBreakpoint)
            c = new ExceptionBreakpointPanel ((ExceptionBreakpoint) b);

        c.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(BreakpointsActionsProvider.class, "ACSD_Breakpoint_Customizer_Dialog")); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor (
            c,
            NbBundle.getMessage (
                BreakpointsActionsProvider.class,
                "CTL_Breakpoint_Customizer_Title" // NOI18N
            )
        );

        JButton bOk = null;
        JButton bClose = null;
        descriptor.setOptions (new JButton[] {
            bOk = new JButton (NbBundle.getMessage (
                BreakpointsActionsProvider.class,
                "CTL_Ok" // NOI18N
            )),
            bClose = new JButton (NbBundle.getMessage (
                BreakpointsActionsProvider.class,
                "CTL_Cancel" // NOI18N
            ))
        });
        HelpCtx helpCtx = HelpCtx.findHelp (c);
        if (helpCtx == null)
            helpCtx = new HelpCtx ("debug.add.breakpoint");  // NOI18N
        descriptor.setHelpCtx (helpCtx);
        bOk.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (
                BreakpointsActionsProvider.class,
                "ACSD_CTL_Ok" // NOI18N
            )
        );
        bClose.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (
                BreakpointsActionsProvider.class,
                "ACSD_CTL_Cancel" // NOI18N
            )
        );
        descriptor.setClosingOptions (null);
        Dialog d = DialogDisplayer.getDefault ().createDialog (descriptor);
        d.pack ();
        d.setVisible (true);
        if (descriptor.getValue () == bOk) {
            ((Controller) c).ok ();
        }
    }
    
    private static void goToSource (LineBreakpoint b) {
        EditorContextBridge.showSource (b, null);
    }
}
