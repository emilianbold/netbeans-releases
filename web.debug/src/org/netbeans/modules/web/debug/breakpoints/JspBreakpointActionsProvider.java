/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.breakpoints;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.debugger.ui.*;
import org.netbeans.spi.viewmodel.*;

import org.netbeans.modules.web.debug.*;

import org.openide.*;
import org.openide.util.*;


/**
 * @author Martin Grebac
 */
public class JspBreakpointActionsProvider implements NodeActionsProviderFilter, Models.ActionPerformer {
    
    
    public Action[] getActions (NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (!(node instanceof JspLineBreakpoint)) 
            return original.getActions (node);
        
        Action[] oas = original.getActions (node);
        if (node instanceof JspLineBreakpoint) {
            Action[] as = new Action [oas.length + 3];
            as [0] = Models.createAction ("Go to Source", node, this);
            as [1] = null;
            System.arraycopy (oas, 0, as, 2, oas.length);
            as [as.length - 1] = Models.createAction ("Customize", node, this);
            return as;
        }
        Action[] as = new Action [oas.length + 1];
        System.arraycopy (oas, 0, as, 0, oas.length);
        as [as.length - 1] = Models.createAction ("Customize", node, this);
        return as;
    }
    
    public void performDefaultAction (NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof JspLineBreakpoint) 
            goToSource ((JspLineBreakpoint) node);
        else
            original.performDefaultAction (node);
    }

    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    public void perform (String action, Object node) {
        if ("Customize".equals (action)) {
            customize ((Breakpoint) node);
        } else
        if ("Go to Source".equals (action)) {
            goToSource ((JspLineBreakpoint) node);
        }
    }    

    private static void customize (Breakpoint b) {
        JComponent c = null;
        if (b instanceof LineBreakpoint) {
            c = new JspBreakpointPanel((JspLineBreakpoint) b);
        }

        DialogDescriptor descriptor = new DialogDescriptor (
            c,
            NbBundle.getMessage (
                JspBreakpointActionsProvider.class,
                "CTL_Breakpoint_Customizer_Title" // NOI18N
            )
        );

        JButton bOk = null;
        JButton bClose = null;
        descriptor.setOptions (new JButton[] {
            bOk = new JButton (NbBundle.getMessage (
                JspBreakpointActionsProvider.class,
                "CTL_Ok" // NOI18N
            )),
            bClose = new JButton (NbBundle.getMessage (
                JspBreakpointActionsProvider.class,
                "CTL_Close" // NOI18N
            ))
        });
        HelpCtx helpCtx = HelpCtx.findHelp (c);
        if (helpCtx == null)
            helpCtx = new HelpCtx ("debug.add.breakpoint");;
        descriptor.setHelpCtx (helpCtx);
        bOk.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (
                JspBreakpointActionsProvider.class,
                "ACSD_CTL_Ok" // NOI18N
            )
        );
        bClose.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (
                JspBreakpointActionsProvider.class,
                "ACSD_CTL_Close" // NOI18N
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
    
    private static void goToSource (JspLineBreakpoint b) {
        Context.showSource (b);
    }
}
