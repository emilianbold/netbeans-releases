/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.remote.ui.diff;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.remote.ui.repository.HeadRevisionPicker;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 *
 * 
 */
public class DiffToRevision  implements ActionListener {
    
    private final DiffToRevisionPanel panel;
    private final JButton okButton;
    private final JButton cancelButton;
    private final VCSFileProxy repository;
    private final HgRevision baseRevision;
    private final Map<JRadioButton, HgRevision> selectionsFirst = new HashMap<>();
    private final Map<JRadioButton, HgRevision> selectionsSecond = new HashMap<>();
    private JRadioButton selectedOption;
    
    @Messages({
        "CTL_DiffToRevision_okButton.text=&Diff",
        "CTL_DiffToRevision_okButton.ACSD=Diff selected revisions",
        "CTL_DiffToRevision_cancelButton.text=&Cancel",
        "CTL_DiffToRevision_cancelButton.ACSD=Cancel",
        "CTL_DiffToRevision_ACSD=Select revisions to diff"
    })
    public DiffToRevision (VCSFileProxy repository, HgRevision base) {
        this.repository = repository;
        this.baseRevision = base;
        panel = new DiffToRevisionPanel();
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.CTL_DiffToRevision_okButton_text());
        okButton.getAccessibleContext().setAccessibleDescription(Bundle.CTL_DiffToRevision_okButton_ACSD());
        cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, Bundle.CTL_DiffToRevision_cancelButton_text());
        cancelButton.getAccessibleContext().setAccessibleDescription(Bundle.CTL_DiffToRevision_cancelButton_ACSD());
        initializeSelections();
        attachListeners();
        panel.rbLocalToAny.doClick();
    } 

    public boolean showDialog() {
        DialogDescriptor dialogDescriptor;
        dialogDescriptor = new DialogDescriptor(panel, Bundle.CTL_DiffToRevision_ACSD());

        dialogDescriptor.setOptions(new Object[] { okButton, cancelButton });
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.mercurial.remote.ui.diff.DiffToRevisionPanel")); //NOI18N
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(Bundle.CTL_DiffToRevision_ACSD());
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.rbLocalToBase) {
            setEnabled(panel.localToRevisionPanel, false);
            setEnabled(panel.baseToRevisionPanel, false);
            selectedOption = panel.rbLocalToBase;
        } else if (e.getSource() == panel.rbLocalToAny) {
            setEnabled(panel.localToRevisionPanel, true);
            setEnabled(panel.baseToRevisionPanel, false);
            selectedOption = panel.rbLocalToAny;
        } else if (e.getSource() == panel.rbBaseToAny) {
            setEnabled(panel.localToRevisionPanel, false);
            setEnabled(panel.baseToRevisionPanel, true);
            selectedOption = panel.rbBaseToAny;
        } else if (e.getSource() == panel.btnSelectBaseToAny) {
            HeadRevisionPicker picker = new HeadRevisionPicker(repository, null);
            if (picker.showDialog()) {
                HgLogMessage msg = picker.getSelectionRevision();
                selectionsFirst.put(panel.rbBaseToAny, msg.getHgRevision());
                panel.tfSelectedRevisionBaseToAny.setText(msg.toAnnotatedString(baseRevision.getChangesetId()));
            }
        } else if (e.getSource() == panel.btnSelectLocalToAny) {
            HeadRevisionPicker picker = new HeadRevisionPicker(repository, null);
            if (picker.showDialog()) {
                HgLogMessage msg = picker.getSelectionRevision();
                selectionsFirst.put(panel.rbLocalToAny, msg.getHgRevision());
                panel.tfSelectedRevisionLocalToAny.setText(msg.toAnnotatedString(baseRevision.getChangesetId()));
            }
        }
    }
    
    public HgRevision getSelectedTreeFirst () {
        return selectionsFirst.get(selectedOption);
    }
    
    public HgRevision getSelectedTreeSecond () {
        return selectionsSecond.get(selectedOption);
    }

    private void attachListeners () {
        panel.btnSelectBaseToAny.addActionListener(this);
        panel.btnSelectLocalToAny.addActionListener(this);
        panel.rbLocalToBase.addActionListener(this);
        panel.rbLocalToAny.addActionListener(this);
        panel.rbBaseToAny.addActionListener(this);
    }

    private void setEnabled (JPanel panel, boolean enabled) {
        for (int i = 0; i < panel.getComponentCount(); ++i) {
            panel.getComponent(i).setEnabled(enabled);
        }
    }

    private void initializeSelections () {
        selectionsFirst.put(panel.rbLocalToBase, HgRevision.BASE);
        selectionsFirst.put(panel.rbLocalToAny, baseRevision);
        selectionsFirst.put(panel.rbBaseToAny, baseRevision);
        selectionsSecond.put(panel.rbLocalToBase, HgRevision.CURRENT);
        selectionsSecond.put(panel.rbLocalToAny, HgRevision.CURRENT);
        selectionsSecond.put(panel.rbBaseToAny, HgRevision.BASE);
        panel.tfSelectedRevisionLocalToAny.setText(baseRevision.toString());
        panel.tfSelectedRevisionBaseToAny.setText(baseRevision.toString());
    }

}
