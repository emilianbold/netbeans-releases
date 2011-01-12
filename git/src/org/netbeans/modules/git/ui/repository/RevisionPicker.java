/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.repository;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class RevisionPicker implements PropertyChangeListener {
    private final RevisionPickerDialog panel;
    private final JButton okButton;
    private final RevisionInfoPanelController infoPanelController;
    private Revision revision;
    private DialogDescriptor dd;
    private final RepositoryBrowserPanel browserPanel;

    public RevisionPicker (File repository, File[] roots) {
        infoPanelController = new RevisionInfoPanelController(repository);
        browserPanel = new RepositoryBrowserPanel(RepositoryBrowserPanel.OPTIONS_INSIDE_PANEL, repository, roots, null);
        panel = new RevisionPickerDialog(infoPanelController.getPanel(), browserPanel);
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(RevisionPicker.class, "LBL_RevisionPickerDialog.okButton.title")); //NOI18N
    }

    boolean open () {
        dd = new DialogDescriptor(panel, NbBundle.getMessage(RevisionPicker.class, "LBL_RevisionPickerDialog.title"), //NOI18N
                true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(RevisionPickerDialog.class), null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        updateDialogState();
        browserPanel.addPropertyChangeListener(this);
        dialog.setVisible(true);
        browserPanel.removePropertyChangeListener(this);
        return dd.getValue() == okButton;
    }

    Revision getRevision () {
        return revision;
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName() == RepositoryBrowserPanel.PROP_REVISION_CHANGED) {
            revision = (Revision) evt.getNewValue();
            updateDialogState();
        }
    }

    private void updateDialogState () {
        boolean enabled = revision != null;
        dd.setValid(enabled);
        okButton.setEnabled(enabled);
        infoPanelController.loadInfo(revision == null ? null : revision.getName());
    }
}
