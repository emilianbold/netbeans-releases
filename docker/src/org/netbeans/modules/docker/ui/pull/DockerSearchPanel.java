/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.pull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.docker.DockerInstance;
import org.netbeans.modules.docker.HubImageInfo;
import org.netbeans.modules.docker.remote.DockerRemote;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class DockerSearchPanel extends javax.swing.JPanel {

    private static final Comparator<HubImageInfo> COMPARATOR = new Comparator<HubImageInfo>() {

        @Override
        public int compare(HubImageInfo o1, HubImageInfo o2) {
            if (o1.getStars() > o2.getStars()) {
                return -1;
            }
            if (o1.getStars() < o2.getStars()) {
                return 1;
            }
            // FIXME null values
            return o1.getName().compareTo(o2.getName());
        }
    };

    private final DockerInstance instance;

    private List<HubImageInfo> availableImages = new ArrayList<>();

    /**
     * Creates new form DockerPullPanel
     */
    public DockerSearchPanel(DockerInstance instance) {
        this.instance = instance;

        initComponents();
    }

    private void search(final String searchTerm) {
        assert SwingUtilities.isEventDispatchThread();

        final Runnable runner = new Runnable() {
            @Override
            public void run() {
                DockerRemote facade = new DockerRemote(instance);
                final List<HubImageInfo> images = facade.search(searchTerm);
                Collections.sort(images, COMPARATOR);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        availableImages = new ArrayList<>(images);
                        DefaultListModel model = new DefaultListModel();
                        for (HubImageInfo image : availableImages) {
                            model.addElement(image);
                        }

                        imageList.clearSelection();
                        imageList.setModel(model);
                        imageList.invalidate();
                        imageList.repaint();
                        searchButton.setEnabled(true);
                    }
                });
            }
        };

        searchButton.setEnabled(false);
        final DefaultListModel model = new DefaultListModel();
        model.addElement(NbBundle.getMessage(DockerSearchPanel.class, "DockerSearchPanel.searching"));
        imageList.setModel(model);

        RequestProcessor.getDefault().post(runner);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchTextField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        pullButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        imageList = new javax.swing.JList<>();

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(DockerSearchPanel.class, "DockerSearchPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(pullButton, org.openide.util.NbBundle.getMessage(DockerSearchPanel.class, "DockerSearchPanel.pullButton.text")); // NOI18N

        jScrollPane1.setViewportView(imageList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(pullButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(searchTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pullButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        search(searchTextField.getText().trim());
    }//GEN-LAST:event_searchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> imageList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton pullButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchTextField;
    // End of variables declaration//GEN-END:variables
}
