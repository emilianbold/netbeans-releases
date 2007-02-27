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
package org.netbeans.modules.versioning.system.cvss.options;

import org.netbeans.spi.options.OptionsPanelController;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.openide.util.Lookup;
import org.openide.util.HelpCtx;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * 
 * @author Maros Sandor
 */
class CvsOptionsController extends OptionsPanelController {

    private CvsOptionsPanel panel;
    
    public void update() {
        panel.getExcludeNewFiles().setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean(CvsModuleConfig.PROP_EXCLUDE_NEW_FILES, false));
        panel.getStatusLabelFormat().setText(CvsModuleConfig.getDefault().getPreferences().get(CvsModuleConfig.PROP_ANNOTATIONS_FORMAT, CvsModuleConfig.DEFAULT_ANNOTATIONS_FORMAT));
    }

    public void applyChanges() {
        CvsModuleConfig.getDefault().getPreferences().putBoolean(CvsModuleConfig.PROP_EXCLUDE_NEW_FILES, panel.getExcludeNewFiles().isSelected());
        CvsModuleConfig.getDefault().getPreferences().put(CvsModuleConfig.PROP_ANNOTATIONS_FORMAT, panel.getStatusLabelFormat().getText());
    }

    public void cancel() {
        // nothing to do
    }

    public boolean isValid() {
        return true;
    }

    public boolean isChanged() {
        return panel.getExcludeNewFiles().isSelected() != CvsModuleConfig.getDefault().getPreferences().getBoolean(CvsModuleConfig.PROP_EXCLUDE_NEW_FILES, false)
                || !panel.getStatusLabelFormat().getText().equals(CvsModuleConfig.getDefault().getPreferences().get(CvsModuleConfig.PROP_ANNOTATIONS_FORMAT, CvsModuleConfig.DEFAULT_ANNOTATIONS_FORMAT));
    }

    public JComponent getComponent(Lookup masterLookup) {
        if (panel == null) {
            panel = new CvsOptionsPanel(); 
        }
        return panel;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CvsOptionsController.class);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
}
