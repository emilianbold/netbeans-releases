/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Font;
import java.util.Collections;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.netbeans.modules.php.project.connections.ui.TransferFileTableChangeListener;
import org.netbeans.modules.php.project.connections.ui.TransferFilter;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class RemoteConfirmationPanelVisual extends JPanel {
    static enum State { FETCHING, NO_FILES, FILES };

    private static final long serialVersionUID = 3753241413078222434L;
    private static final int STEP_INDEX = 2;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final TransferFileTableChangeListener transferFilterListener;

    private TransferFilter transferFilter;
    private State state = null;

    public RemoteConfirmationPanelVisual(RemoteConfirmationPanel wizardPanel, WizardDescriptor descriptor) {
        assert wizardPanel != null;
        assert descriptor != null;

        // Provide a name in the title bar.
        setName(wizardPanel.getSteps()[STEP_INDEX]);
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, STEP_INDEX);
        // Step name (actually the whole list for reference).
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, wizardPanel.getSteps());

        initComponents();

        setFetchingFiles();
        uploadInfoLabel.setText(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "TXT_UploadInfo"));

        transferFilterListener = new TransferFileTableChangeListener() {
            public void updateUnitsChanged() {
                changeSupport.fireChange();
            }

            public void filterChanged() {
            }
        };
    }

    public void addRemoteConfirmationListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeRemoteConfirmationListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void setRemoteFiles(Set<TransferFile> remoteFiles) {
        assert SwingUtilities.isEventDispatchThread() : "Must be run in EDT";
        assert remoteFiles != null;

        state = State.FILES;
        transferFilter = TransferFilter.getEmbeddableDownloadDialog(remoteFiles);
        transferFilter.addUpdateUnitListener(transferFilterListener);

        filesPanel.add(transferFilter);

        statusLabel.setText(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "LBL_Confirmation")); // NOI18N
        setState(true);
    }

    public void setNoFiles(String reason) {
        assert SwingUtilities.isEventDispatchThread() : "Must be run in EDT";

        state = State.NO_FILES;
        resetTransferFilter();

        statusLabel.setText(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "LBL_NoFiles", reason)); // NOI18N
        setState(false);
    }

    public void setFetchingFiles() {
        assert SwingUtilities.isEventDispatchThread() : "Must be run in EDT";

        state = State.FETCHING;
        resetTransferFilter();

        statusLabel.setText(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "LBL_FetchingRemoteFiles")); // NOI18N
        setState(false);
    }

    public Set<TransferFile> getRemoteFiles() {
        if (transferFilter == null) {
            return Collections.emptySet();
        }
        return TransferFilter.getSelectedFiles(transferFilter);
    }

    State getState() {
        return state;
    }

    private void setState(boolean enabled) {
        downloadInfoLabel.setVisible(enabled);
        uploadInfoLabel.setVisible(enabled);
        filesPanel.setVisible(enabled);
    }

    private void resetTransferFilter() {
        if (transferFilter != null) {
            transferFilter.removeUpdateUnitListener(transferFilterListener);
        }
        transferFilter = null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        statusLabel = new JLabel();
        downloadInfoLabel = new JLabel();
        uploadInfoLabel = new JLabel();
        filesPanel = new JPanel();

        statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() | Font.BOLD));
        statusLabel.setText("DUMMY"); // NOI18N


        downloadInfoLabel.setText(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "RemoteConfirmationPanelVisual.downloadInfoLabel.text")); // NOI18N
        uploadInfoLabel.setText("DUMMY"); // NOI18N

        filesPanel.setLayout(new BoxLayout(filesPanel, BoxLayout.LINE_AXIS));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(statusLabel)
                    .add(downloadInfoLabel)
                    .add(uploadInfoLabel))
                .addContainerGap(27, Short.MAX_VALUE))
            .add(filesPanel, GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(statusLabel)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(downloadInfoLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(filesPanel, GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(uploadInfoLabel))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel downloadInfoLabel;
    private JPanel filesPanel;
    private JLabel statusLabel;
    private JLabel uploadInfoLabel;
    // End of variables declaration//GEN-END:variables

}
