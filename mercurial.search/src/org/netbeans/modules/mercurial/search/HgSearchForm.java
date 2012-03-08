/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial.search;

import java.io.File;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.ui.ComponentUtils;
import org.netbeans.api.search.ui.FileNameController;
import org.netbeans.api.search.ui.ScopeController;
import org.netbeans.api.search.ui.ScopeOptionsController;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider;
import org.openide.filesystems.FileUtil;

class HgSearchForm extends JPanel implements ChangeListener {

    static final String PROP_USABLE = "usable";
    private ScopeController scopeController;
    private FileNameController fileNameController;
    private ScopeOptionsController scopeOptionsController;

    HgSearchForm() {
        initComponents();
        scopeController = ComponentUtils.adjustComboForScope(scopeCombo, null);
        fileNameController = ComponentUtils.adjustComboForFileName(fileCombo);
        scopeOptionsController = ComponentUtils.adjustPanelForOptions(
                scopePanel, false, fileNameController);
        scopeController.addChangeListener(this);
        fileNameController.addChangeListener(this);
        scopeOptionsController.addChangeListener(this);
        patternField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                stateChanged(null);
            }
            @Override public void removeUpdate(DocumentEvent e) {
                stateChanged(null);
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        });
    }

    @Override public void stateChanged(ChangeEvent e) {
        firePropertyChange(PROP_USABLE, null, null);
    }

    boolean isUsable() {
        if (patternField.getText().isEmpty()) {
            return false;
        }
        SearchInfo searchInfo = scopeController.getSearchInfo();
        if (!searchInfo.canSearch()) {
            return false;
        }
        File repo = null;
        for (SearchRoot r : searchInfo.getSearchRoots()) {
            File _repo = findRepo(FileUtil.toFile(r.getFileObject()));
            if (_repo == null) {
                return false;
            } else if (repo == null) {
                repo = _repo;
            } else if (!repo.equals(_repo)) {
                return false;
            }
        }
        if (repo == null) {
            return false;
        }
        // XXX validate fileCombo(), scopePanel()
        return true;
    }

    SearchComposition<?> composeSearch(SearchProvider.Presenter presenter) {
        // XXX pick out the actual root substrings and prepend them to pattern
        File repo = findRepo(FileUtil.toFile(scopeController.getSearchInfo().getSearchRoots().get(0).getFileObject()));
        // XXX pay attention to SearchRoot.filters
        // XXX check for regexp vs. basic, and map to Hg pattern syntax
        //String pattern = fileCombo().getFileNamePattern();
        String pattern = "**.xsl"; // XXX pending JG25
        // XXX pay attention to scopePanel()
        String text = patternField.getText();
        return new HgGrep(presenter, repo, pattern, text);
    }

    private static File findRepo(File f) {
        if (f == null) {
            return null;
        }
        if (new File(f, ".hg").isDirectory()) {
            return f;
        } else {
            return findRepo(f.getParentFile());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scopeCombo = new javax.swing.JComboBox();
        fileCombo = new javax.swing.JComboBox();
        scopePanel = new javax.swing.JPanel();
        patternField = new javax.swing.JTextField();

        javax.swing.GroupLayout scopePanelLayout = new javax.swing.GroupLayout(scopePanel);
        scopePanel.setLayout(scopePanelLayout);
        scopePanelLayout.setHorizontalGroup(
            scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        scopePanelLayout.setVerticalGroup(
            scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scopeCombo, 0, 376, Short.MAX_VALUE)
                    .addComponent(fileCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scopePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(patternField))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scopeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scopePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(patternField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(97, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox fileCombo;
    private javax.swing.JTextField patternField;
    private javax.swing.JComboBox scopeCombo;
    private javax.swing.JPanel scopePanel;
    // End of variables declaration//GEN-END:variables

}
