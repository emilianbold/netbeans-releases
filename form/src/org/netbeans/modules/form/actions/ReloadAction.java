/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.windows.WindowManager;
import org.openide.windows.TopComponent;
import org.netbeans.modules.form.FormEditorSupport;

/**
 * Action that invokes reloading of the currently active form. Presented only
 * in contextual menus within the Form Editor.
 *
 * @author Tomas Pavek
 */

public class ReloadAction extends CallableSystemAction {

    private static String name;

    public ReloadAction() {
        setEnabled(true);
    }

    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(ReloadAction.class)
                     .getString("ACT_ReloadForm"); // NOI18N
        return name;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.modes"); // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }

    public void performAction() {
        WindowManager wm = WindowManager.getDefault();
        TopComponent activeTC = wm.getRegistry().getActivated();
        FormEditorSupport fes = FormEditorSupport.getFormEditor(activeTC);
        if (fes != null)
            fes.reloadForm();
    }
}
