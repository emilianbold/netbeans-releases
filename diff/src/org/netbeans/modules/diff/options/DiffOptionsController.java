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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.diff.options;

import org.netbeans.modules.diff.*;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Lookup;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;

/**
 * Diff module's Options Controller.
 * 
 * @author Maros Sandor
 */
class DiffOptionsController extends OptionsPanelController {

    private DiffOptionsPanel panel;
    
    public void update() {
        panel.getInternalDiff().setSelected(DiffModuleConfig.getDefault().isUseInteralDiff());
        panel.getExternalDiff().setSelected(!DiffModuleConfig.getDefault().isUseInteralDiff());
        panel.getIgnoreWhitespace().setSelected(DiffModuleConfig.getDefault().isIgnoreWhitespace());
        panel.getExternalCommand().setText(DiffModuleConfig.getDefault().getPreferences().get(DiffModuleConfig.PREF_EXTERNAL_DIFF_COMMAND, "diff {0} {1}")); // NOI18N
        panel.setChanged(false);
    }

    public void applyChanges() {
        checkExternalCommand();
        DiffModuleConfig.getDefault().setUseInteralDiff(panel.getInternalDiff().isSelected());
        DiffModuleConfig.getDefault().setIgnoreWhitespace(panel.getIgnoreWhitespace().isSelected());
        DiffModuleConfig.getDefault().getPreferences().put(DiffModuleConfig.PREF_EXTERNAL_DIFF_COMMAND, panel.getExternalCommand().getText());
        panel.setChanged(false);
    }

    private void checkExternalCommand() {
        if (panel.getInternalDiff().isSelected()) return;
        String cmd = panel.getExternalCommand().getText();
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            p.destroy();
        } catch (IOException e) {
            // the command seems invalid
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(DiffOptionsController.class, "MSG_InvalidDiffCommand"), NotifyDescriptor.WARNING_MESSAGE));
            return ;
        }
    }

    public void cancel() {
        // nothing to do
    }

    public boolean isValid() {
        return true;
    }

    public boolean isChanged() {
        return panel.isChanged();
    }

    public JComponent getComponent(Lookup masterLookup) {
        if (panel == null) {
            panel = new DiffOptionsPanel(); 
        }
        return panel;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(DiffOptionsController.class);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
}
