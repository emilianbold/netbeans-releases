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
import org.netbeans.modules.form.FormEditorSupport;

/**
 * @author Tomas Pavek
 */

public class ReloadAction extends CallableSystemAction {

    private static String name;

    private FormEditorSupport formEditorSupport;

    public ReloadAction() {
        setEnabled(false);
    }

    protected boolean asynchronous() {
        return false;
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

    public void performAction() {
        if (formEditorSupport != null)
            formEditorSupport.reloadForm();
    }

    public void setForm(FormEditorSupport fes) {
        formEditorSupport = fes;
        setEnabled(fes != null);
    }
}
