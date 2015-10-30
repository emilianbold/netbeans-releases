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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.docker.DockerInstance;
import org.netbeans.modules.docker.DockerHubImage;
import org.netbeans.modules.docker.remote.DockerRemote;
import org.openide.awt.HtmlRenderer;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class DockerHubSearchPanel extends javax.swing.JPanel {

    private static final int SEARCH_DELAY = 1000;

    private final DockerInstance instance;

    private final AtomicReference<String> searchTerm = new AtomicReference<>();

    private final RequestProcessor.Task searchTask = RequestProcessor.getDefault().create(new Runnable() {
        @Override
        public void run() {
            search(searchTerm.get());
        }
    });

    /**
     * Creates new form DockerPullPanel
     */
    public DockerHubSearchPanel(DockerInstance instance) {
        this.instance = instance;

        initComponents();

        HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();
        renderer.setHtml(true);
        this.imageList.setCellRenderer(renderer);
        this.searchTextField.getDocument().addDocumentListener(new SearchListener());
    }

    private void search(final String searchTerm) {
        assert !SwingUtilities.isEventDispatchThread();
        assert searchTerm != null && !searchTerm.isEmpty();

        DockerRemote facade = new DockerRemote(instance);
        final List<DockerHubImage> images = facade.search(searchTerm);

        final List<DockerHubImageItem> fresh = new ArrayList<>(images.size());
        for (DockerHubImage info : images) {
            fresh.add(new DockerHubImageItem(info));
        }
        Collections.sort(fresh);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DefaultListModel model = new DefaultListModel();
                for (DockerHubImageItem image : fresh) {
                    model.addElement(image);
                }

                imageList.clearSelection();
                imageList.setModel(model);
                imageList.invalidate();
                imageList.repaint();
            }
        });
    }

    private class SearchListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }

        private void update(DocumentEvent e) {
            String text = searchTextField.getText().trim();
            if (text.isEmpty()) {
                searchTask.cancel();
            } else {
                searchTerm.set(text);
                searchTask.schedule(SEARCH_DELAY);
            }
        }
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
        pullButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        imageList = new javax.swing.JList<>();
        searchTextLabel = new javax.swing.JLabel();
        imageListLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(pullButton, org.openide.util.NbBundle.getMessage(DockerHubSearchPanel.class, "DockerHubSearchPanel.pullButton.text")); // NOI18N

        jScrollPane1.setViewportView(imageList);

        searchTextLabel.setLabelFor(searchTextField);
        org.openide.awt.Mnemonics.setLocalizedText(searchTextLabel, org.openide.util.NbBundle.getMessage(DockerHubSearchPanel.class, "DockerHubSearchPanel.searchTextLabel.text")); // NOI18N

        imageListLabel.setLabelFor(imageList);
        org.openide.awt.Mnemonics.setLocalizedText(imageListLabel, org.openide.util.NbBundle.getMessage(DockerHubSearchPanel.class, "DockerHubSearchPanel.imageListLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(searchTextField)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 661, Short.MAX_VALUE)
                        .addComponent(pullButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(searchTextLabel)
                            .addComponent(imageListLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchTextLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imageListLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pullButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<org.netbeans.modules.docker.ui.pull.DockerHubImageItem> imageList;
    private javax.swing.JLabel imageListLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton pullButton;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JLabel searchTextLabel;
    // End of variables declaration//GEN-END:variables
}
