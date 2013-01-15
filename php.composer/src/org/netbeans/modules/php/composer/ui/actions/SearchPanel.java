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
package org.netbeans.modules.php.composer.ui.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.composer.commands.Composer;
import org.netbeans.modules.php.composer.output.model.SearchResult;
import org.netbeans.modules.php.composer.ui.options.ComposerOptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * UI for Composer search command.
 */
public final class SearchPanel extends JPanel {

    private static final long serialVersionUID = -4572187014657456L;

    private static final RequestProcessor REQUEST_PROCESSOR = new RequestProcessor(SearchPanel.class);

    private final PhpModule phpModule;
    private final List<SearchResult> searchResults = new CopyOnWriteArrayList<SearchResult>();
    // @GuardedBy("EDT")
    private final ResultsListModel resultsModel = new ResultsListModel(searchResults);


    private SearchPanel(PhpModule phpModule) {
        assert phpModule != null;

        this.phpModule = phpModule;

        initComponents();
        init();
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "SearchPanel.panel.title=Composer Search ({0})",
        "SearchPanel.panel.require.label=Require",
        "SearchPanel.panel.requireDev.label=Require (dev)"
    })
    public static void open(PhpModule phpModule) {
        assert EventQueue.isDispatchThread();

        SearchPanel searchPanel = new SearchPanel(phpModule);
        Object[] options = new Object[] {
            searchPanel.requireButton,
            searchPanel.requireDevButton,
            DialogDescriptor.CANCEL_OPTION,
        };

        final DialogDescriptor descriptor = new DialogDescriptor(
                searchPanel,
                Bundle.SearchPanel_panel_title(phpModule.getDisplayName()),
                false,
                options,
                searchPanel.requireButton,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        descriptor.setClosingOptions(new Object[] {DialogDescriptor.CANCEL_OPTION});
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
    }

    private void init() {
        // search
        enableSearchButton();
        // results
        resultsList.setModel(resultsModel);
        resultsList.setCellRenderer(new ResultListCellRenderer(resultsList.getCellRenderer()));
        // listeners
        tokenTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processChange();
            }
            private void processChange() {
                enableSearchButton();
            }
        });
    }

    void enableSearchButton() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                searchButton.setEnabled(StringUtils.hasText(tokenTextField.getText()));
            }
        });
    }

    void clearSearchResults() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                searchResults.clear();
                resultsModel.fireContentsChanged();
            }
        });
    }

    void addSearchResult(final SearchResult searchResult) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                searchResults.add(searchResult);
                resultsModel.fireContentsChanged();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        requireDevButton = new JButton();
        requireButton = new JButton();
        tokenLabel = new JLabel();
        tokenTextField = new JTextField();
        onlyNameCheckBox = new JCheckBox();
        searchButton = new JButton();
        resultsLabel = new JLabel();
        outputSplitPane = new JSplitPane();
        resultsScrollPane = new JScrollPane();
        resultsList = new JList();
        detailsScrollPane = new JScrollPane();
        detailsTextPane = new JTextPane();

        Mnemonics.setLocalizedText(requireDevButton, NbBundle.getMessage(SearchPanel.class, "SearchPanel.requireDevButton.text")); // NOI18N

        Mnemonics.setLocalizedText(requireButton, NbBundle.getMessage(SearchPanel.class, "SearchPanel.requireButton.text")); // NOI18N

        Mnemonics.setLocalizedText(tokenLabel, NbBundle.getMessage(SearchPanel.class, "SearchPanel.tokenLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(onlyNameCheckBox, NbBundle.getMessage(SearchPanel.class, "SearchPanel.onlyNameCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(resultsLabel, NbBundle.getMessage(SearchPanel.class, "SearchPanel.resultsLabel.text")); // NOI18N

        outputSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        resultsScrollPane.setViewportView(resultsList);

        outputSplitPane.setLeftComponent(resultsScrollPane);

        detailsTextPane.setEditable(false);
        detailsScrollPane.setViewportView(detailsTextPane);

        outputSplitPane.setBottomComponent(detailsScrollPane);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(outputSplitPane, GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tokenLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(onlyNameCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tokenTextField)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchButton))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resultsLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(tokenLabel)
                    .addComponent(tokenTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlyNameCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resultsLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputSplitPane, GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("SearchPanel.error.composer.notValid=Composer is not valid.")
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        Composer composer;
        try {
            composer = Composer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(Bundle.SearchPanel_error_composer_notValid(), ComposerOptionsPanelController.OPTIONS_SUBPATH);
            return;
        }
        searchButton.setEnabled(false);
        final Composer composerRef = composer;
        final String token = tokenTextField.getText();
        final boolean onlyName = onlyNameCheckBox.isSelected();
        clearSearchResults();
        final Future<Integer> result = composerRef.search(token, onlyName, new Composer.OutputProcessor<SearchResult>() {
            @Override
            public void process(SearchResult item) {
                addSearchResult(item);
            }
        });
        if (result == null) {
            enableSearchButton();
        } else {
            REQUEST_PROCESSOR.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        result.get();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException ex) {
                        UiUtils.processExecutionException(ex, ComposerOptionsPanelController.OPTIONS_SUBPATH);
                    } finally {
                        enableSearchButton();
                    }
                }
            });
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JScrollPane detailsScrollPane;
    private JTextPane detailsTextPane;
    private JCheckBox onlyNameCheckBox;
    private JSplitPane outputSplitPane;
    private JButton requireButton;
    private JButton requireDevButton;
    private JLabel resultsLabel;
    private JList resultsList;
    private JScrollPane resultsScrollPane;
    private JButton searchButton;
    private JLabel tokenLabel;
    private JTextField tokenTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class ResultsListModel extends AbstractListModel {

        private static final long serialVersionUID = -46871206788954L;

        // @GuardedBy("EDT")
        private final List<SearchResult> searchResults;


        public ResultsListModel(List<SearchResult> searchResults) {
            assert EventQueue.isDispatchThread();
            this.searchResults = searchResults;
        }

        @Override
        public int getSize() {
            assert EventQueue.isDispatchThread();
            return searchResults.size();
        }

        @Override
        public Object getElementAt(int index) {
            assert EventQueue.isDispatchThread();
            return searchResults.get(index);
        }

        public void fireContentsChanged() {
            assert EventQueue.isDispatchThread();
            super.fireContentsChanged(this, 0, searchResults.size());
        }

    }

    private static final class ResultListCellRenderer implements ListCellRenderer {

        private final ListCellRenderer originalRenderer;

        public ResultListCellRenderer(ListCellRenderer originalRenderer) {
            assert originalRenderer != null;
            this.originalRenderer = originalRenderer;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            StringBuilder label = new StringBuilder(100);
            if (value instanceof SearchResult) {
                SearchResult result = (SearchResult) value;
                label.append("<html><b>"); // NOI18N
                label.append(result.getName());
                label.append("</b> : "); // NOI18N
                label.append(result.getDescription());
            } else {
                assert false : "Unexpected value: " + value;
            }
            return originalRenderer.getListCellRendererComponent(list, label.toString(), index, isSelected, cellHasFocus);
        }

    }

}
