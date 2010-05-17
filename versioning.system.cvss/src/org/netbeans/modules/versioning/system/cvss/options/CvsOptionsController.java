/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        int wrapLength = CvsModuleConfig.getDefault().getWrapCommitMessagelength();
        panel.getWrapCommitMessages().setSelected(wrapLength > 0);
        panel.getWrapCharCount().setText(wrapLength > 0 ? Integer.toString(wrapLength) : "");
        panel.getPruneDirectories().setSelected(CvsModuleConfig.getDefault().getAutoPruneDirectories());
    }

    public void applyChanges() {
        if (!isValid()) return;
        CvsModuleConfig.getDefault().getPreferences().putBoolean(CvsModuleConfig.PROP_EXCLUDE_NEW_FILES, panel.getExcludeNewFiles().isSelected());
        CvsModuleConfig.getDefault().getPreferences().put(CvsModuleConfig.PROP_ANNOTATIONS_FORMAT, panel.getStatusLabelFormat().getText());
        int wrapLength = panel.getWrapCommitMessages().isSelected() ? Integer.parseInt(panel.getWrapCharCount().getText().trim()) : 0;
        CvsModuleConfig.getDefault().setWrapCommitMessagelength(wrapLength);
        CvsModuleConfig.getDefault().setAutoPruneDirectories(panel.getPruneDirectories().isSelected());
    }

    public void cancel() {
        // nothing to do
    }

    public boolean isValid() {
        try {
            return !panel.getWrapCommitMessages().isSelected() || Integer.parseInt(panel.getWrapCharCount().getText().trim()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isChanged() {
        if (panel.getExcludeNewFiles().isSelected() != CvsModuleConfig.getDefault().getPreferences().getBoolean(CvsModuleConfig.PROP_EXCLUDE_NEW_FILES, false)) return true;
        if (!panel.getStatusLabelFormat().getText().equals(CvsModuleConfig.getDefault().getPreferences().get(CvsModuleConfig.PROP_ANNOTATIONS_FORMAT, CvsModuleConfig.DEFAULT_ANNOTATIONS_FORMAT))) return true;
        int originalWrapLength = CvsModuleConfig.getDefault().getWrapCommitMessagelength();
        boolean originalWrapMessages = originalWrapLength > 0;
        if (panel.getWrapCommitMessages().isSelected() != originalWrapMessages) return true;
        if (originalWrapMessages && !panel.getWrapCharCount().getText().equals(Integer.toString(originalWrapLength))) return true;
        if (panel.getPruneDirectories().isSelected() != CvsModuleConfig.getDefault().getAutoPruneDirectories()) return true;
        return false;
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
