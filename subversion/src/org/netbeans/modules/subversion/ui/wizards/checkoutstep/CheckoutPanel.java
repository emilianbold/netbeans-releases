/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.subversion.ui.wizards.checkoutstep;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.jdesktop.layout.GroupLayout;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import static org.jdesktop.layout.GroupLayout.BASELINE;
import static org.jdesktop.layout.GroupLayout.DEFAULT_SIZE;
import static org.jdesktop.layout.GroupLayout.LEADING;
import static org.jdesktop.layout.GroupLayout.PREFERRED_SIZE;
import static org.jdesktop.layout.LayoutStyle.RELATED;
import static org.jdesktop.layout.LayoutStyle.UNRELATED;

/**
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 */
public class CheckoutPanel extends JPanel {

    /**
     * Creates new form CheckoutPanel
     */
    public CheckoutPanel() {
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="UI Definition Code">
    private void initComponents() {
        lblSpecifyRepoFolders = new JLabel();
        lblRepoFolders = new JLabel();
        lblWorkingCopy = new JLabel();
        lblRepoRevision = new JLabel();
        lblLocalFolder = new JLabel();

        lblEmptyMeansHead = new JLabel();
        lblSpecifyLocalFolders = new JLabel();

        setName(getString("BK2024")); // NOI18N

        lblRepoFolders.setLabelFor(repositoryPathTextField);
        lblRepoRevision.setLabelFor(revisionTextField);

        repositoryPathTextField.setColumns(30);
        revisionTextField.setColumns(7);

        Mnemonics.setLocalizedText(lblSpecifyRepoFolders, getString("CTL_Checkout_RepositoryHint")); // NOI18N
        Mnemonics.setLocalizedText(lblRepoFolders, getString("CTL_Checkout_RepositoryFolder")); // NOI18N
        Mnemonics.setLocalizedText(browseRepositoryButton, getString("CTL_Checkout_Browse1")); // NOI18N
        Mnemonics.setLocalizedText(lblRepoRevision, getString("CTL_Checkout_Revision")); // NOI18N
        Mnemonics.setLocalizedText(searchRevisionButton, getString("CTL_Checkout_Search")); // NOI18N
        Mnemonics.setLocalizedText(browseRevisionButton, getString("CTL_Checkout_Browse")); // NOI18N
        Mnemonics.setLocalizedText(lblEmptyMeansHead, getString("CTL_Checkout_EmptyHint")); // NOI18N

        atWorkingDirLevelCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exportCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        atWorkingDirLevelCheckBox.setEnabled(false);

        Mnemonics.setLocalizedText(atWorkingDirLevelCheckBox, getString("CTL_Checkout_CheckoutContentEmpty")); // NOI18N
        Mnemonics.setLocalizedText(exportCheckBox, getString("CTL_Checkout_Export")); // NOI18N

        lblLocalFolder.setLabelFor(workdirTextField);

        workdirTextField.setColumns(30);
        workingCopy.setHorizontalAlignment(SwingConstants.TRAILING);

        Mnemonics.setLocalizedText(lblSpecifyLocalFolders, getString("CTL_Checkout_LocalFolderHint")); // NOI18N
        Mnemonics.setLocalizedText(lblLocalFolder, getString("CTL_Checkout_LocalFolder")); // NOI18N
        Mnemonics.setLocalizedText(browseWorkdirButton, getString("CTL_Checkout_Browse2")); // NOI18N
        Mnemonics.setLocalizedText(lblWorkingCopy, getString("CTL_Checkout_WorkingCopy")); // NOI18N

        scanForProjectsCheckBox.setSelected(true);
        scanForProjectsCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        Mnemonics.setLocalizedText(scanForProjectsCheckBox, getString("CTL_Scan_After_Checkout")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(LEADING)
                        .add(lblSpecifyRepoFolders)
                        .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(LEADING)
                                        .add(lblRepoFolders)
                                        .add(lblRepoRevision))
                                .addPreferredGap(RELATED)
                                .add(layout.createParallelGroup(LEADING)
                                        .add(layout.createSequentialGroup()
                                                .add(repositoryPathTextField)
                                                .addPreferredGap(RELATED)
                                                .add(browseRepositoryButton))
                                        .add(layout.createSequentialGroup()
                                                .add(revisionTextField, DEFAULT_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                                .addPreferredGap(RELATED)
                                                .add(searchRevisionButton)
                                                .add(browseRevisionButton))
                                        .add(lblEmptyMeansHead)))
                        .add(atWorkingDirLevelCheckBox)
                        .add(exportCheckBox)
                        .add(lblSpecifyLocalFolders)
                        .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(LEADING)
                                        .add(lblLocalFolder)
                                        .add(lblWorkingCopy))
                                .addPreferredGap(RELATED)
                                .add(layout.createParallelGroup(LEADING)
                                        .add(layout.createSequentialGroup()
                                                .add(workdirTextField)
                                                .addPreferredGap(RELATED)
                                                .add(browseWorkdirButton))
                                        .add(workingCopy)))
                        .add(scanForProjectsCheckBox)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .add(lblSpecifyRepoFolders)
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(BASELINE)
                                .add(lblRepoFolders)
                                .add(repositoryPathTextField)
                                .add(browseRepositoryButton))
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(BASELINE)
                                .add(lblRepoRevision)
                                .add(revisionTextField)
                                .add(searchRevisionButton)
                                .add(browseRevisionButton))
                        .addPreferredGap(RELATED)
                        .add(lblEmptyMeansHead)
                        .addPreferredGap(UNRELATED)
                        .add(atWorkingDirLevelCheckBox)
                        .add(exportCheckBox)
                        .add(28)
                        .add(lblSpecifyLocalFolders)
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(BASELINE)
                                .add(lblLocalFolder)
                                .add(workdirTextField)
                                .add(browseWorkdirButton))
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(BASELINE)
                                .add(lblWorkingCopy)
                                .add(workingCopy))
                        .add(18)
                        .add(scanForProjectsCheckBox)
        );
        layout.linkSize(new Component[] {lblRepoFolders, lblRepoRevision, lblLocalFolder, lblWorkingCopy});

        browseRepositoryButton.getAccessibleContext().setAccessibleDescription(getString("ASCD_Browse_Repository_Foldres")); //NOI18N
        scanForProjectsCheckBox.getAccessibleContext().setAccessibleDescription(getString("ACSD_Scan_After_Checkout")); //NOI18N
        lblRepoFolders.getAccessibleContext().setAccessibleDescription(getString("ASCD_Repository_Folders_to_Checkout"));    //NOI18N
        lblRepoRevision.getAccessibleContext().setAccessibleDescription(getString("ASCD_Repository_Revision"));  //NOI18N
        browseWorkdirButton.getAccessibleContext().setAccessibleDescription(getString("ASCD_Browse_Local_Directory"));   //NOI18N
        searchRevisionButton.getAccessibleContext().setAccessibleDescription(getString("ASCD_Search_Revision_Number"));  //NOI18N
        browseRevisionButton.getAccessibleContext().setAccessibleDescription(getString("ASCD_Browse_Revision_Number"));  //NOI18N
        atWorkingDirLevelCheckBox.getAccessibleContext().setAccessibleDescription(getString("ASCD_Checkout_only_folder_contents"));  //NOI18N
        exportCheckBox.getAccessibleContext().setAccessibleDescription(getString("ASCD_Checkout_Export"));  //NOI18N
    }// </editor-fold>

    private static String getString(String msgKey) {
        return NbBundle.getMessage(CheckoutPanel.class, msgKey);
    }

    final JCheckBox atWorkingDirLevelCheckBox = new JCheckBox();
    final JCheckBox exportCheckBox = new JCheckBox();
    final JButton browseRepositoryButton = new JButton();
    final JButton browseWorkdirButton = new JButton();
    private JLabel lblEmptyMeansHead;
    private JLabel lblLocalFolder;
    private JLabel lblRepoFolders;
    private JLabel lblRepoRevision;
    private JLabel lblSpecifyLocalFolders;
    private JLabel lblSpecifyRepoFolders;
    private JLabel lblWorkingCopy;
    final JTextField repositoryPathTextField = new JTextField();
    final JTextField revisionTextField = new JTextField();
    final JCheckBox scanForProjectsCheckBox = new JCheckBox();
    final JButton searchRevisionButton = new JButton();
    final JButton browseRevisionButton = new JButton();
    final JTextField workdirTextField = new JTextField();
    final JLabel workingCopy = new JLabel();
    
}
