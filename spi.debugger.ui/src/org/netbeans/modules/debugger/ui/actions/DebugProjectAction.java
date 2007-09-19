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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;

import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
public class DebugProjectAction implements Action {
    
    private Action delegate;
    
    /** Creates a new instance of DebugMainProjectAction */
    public DebugProjectAction() {
        delegate = ProjectSensitiveActions.projectCommandAction(
                ActionProvider.COMMAND_DEBUG,
                NbBundle.getMessage(DebugMainProjectAction.class, "LBL_DebugProjectActionOnProject_Name" ), null); // NOI18N
    }
    
    public Object getValue(String arg0) {
        return delegate.getValue(arg0);
    }

    public void putValue(String arg0, Object arg1) {
        delegate.putValue(arg0, arg1);
    }

    public void setEnabled(boolean arg0) {
        delegate.setEnabled(arg0);
    }

    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    public void addPropertyChangeListener(PropertyChangeListener arg0) {
        delegate.addPropertyChangeListener(arg0);
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0) {
        delegate.removePropertyChangeListener(arg0);
    }

    public void actionPerformed(ActionEvent arg0) {
        delegate.actionPerformed(arg0);
    }

}
