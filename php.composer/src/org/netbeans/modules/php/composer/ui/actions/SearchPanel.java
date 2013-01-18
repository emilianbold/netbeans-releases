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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
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

    private static final SearchResult SEARCHING_SEARCH_RESULT = new SearchResult(null, null);
    private static final SearchResult NO_RESULTS_SEARCH_RESULT = new SearchResult(null, null);
    private static final String DEFAULT_PACKAGE_VERSION = ":*"; // NOI18N

    // @GuardedBy("EDT")
    private static boolean keepOpened = true;

    private final PhpModule phpModule;
    private final List<SearchResult> searchResults = Collections.synchronizedList(new ArrayList<SearchResult>());
    // @GuardedBy("EDT")
    private final ResultsListModel resultsModel = new ResultsListModel(searchResults);
    private final ConcurrentMap<String, String> resultDetails = new ConcurrentHashMap<String, String>();
    // tasks
    private final RequestProcessor postSearchRequestProcessor = new RequestProcessor(SearchPanel.class.getName() + " (POST SEARCH)"); // NOI18N
    private final RequestProcessor postShowRequestProcessor = new RequestProcessor(SearchPanel.class.getName() + " (POST SHOW)"); // NOI18N
    private final List<Future<Integer>> searchTasks = new CopyOnWriteArrayList<Future<Integer>>();
    private final List<Future<Integer>> showTasks = new CopyOnWriteArrayList<Future<Integer>>();


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
        descriptor.setAdditionalOptions(new Object[] {searchPanel.keepOpenCheckBox});
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        handleKeepOpen(dialog, searchPanel);
        setDefaultButton(dialog, searchPanel);
        dialog.setVisible(true);
    }

    private static void setDefaultButton(Dialog dialog, final SearchPanel searchPanel) {
        if (dialog instanceof JDialog) {
            JRootPane rootPane = ((JDialog) dialog).getRootPane();
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search"); // NOI18N
            rootPane.getActionMap().put("search", new AbstractAction() { // NOI18N
                private static final long serialVersionUID = -4568616574687867L;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (searchPanel.tokenTextField.hasFocus()) {
                        if (searchPanel.searchButton.isEnabled()) {
                            searchPanel.searchButton.doClick();
                        }
                    } else if (searchPanel.resultsList.hasFocus()) {
                        if (searchPanel.requireButton.isEnabled()) {
                            searchPanel.requireButton.doClick();
                        }
                    }
                }
            });
        }
    }

    private static void handleKeepOpen(final Dialog dialog, final SearchPanel searchPanel) {
        ActionListener keepOpenActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!searchPanel.keepOpenCheckBox.isSelected()) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            }
        };
        searchPanel.requireButton.addActionListener(keepOpenActionListener);
        searchPanel.requireDevButton.addActionListener(keepOpenActionListener);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        cleanUp();
    }

    private void init() {
        initSearch();
        initResults();
        initActionButons();
    }

    private void initSearch() {
        enableSearchButton();
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

    private void initResults() {
        // results
        resultsList.setModel(resultsModel);
        resultsList.setCellRenderer(new ResultListCellRenderer(resultsList.getCellRenderer()));
        // details
        updateResultDetails(false);
        // listeners
        resultsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                enableRequireButtons();
                updateResultDetails(false);
            }
        });
        resultsModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                processChange();
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
                processChange();
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                processChange();
            }
            private void processChange() {
                enableRequireButtons();
                updateResultDetails(false);
            }
        });
    }

    private void initActionButons() {
        assert EventQueue.isDispatchThread();
        // require buttons
        enableRequireButtons();
        // keep opened checkbox
        keepOpenCheckBox.setSelected(keepOpened);
        // listeners
        keepOpenCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                assert EventQueue.isDispatchThread();
                keepOpened = e.getStateChange() == ItemEvent.SELECTED;
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

    void removeSearchResult(final SearchResult searchResult) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                if (searchResults.remove(searchResult)) {
                    resultsModel.fireContentsChanged();
                }
            }
        });
    }

    void enableRequireButtons() {
        assert EventQueue.isDispatchThread();
        boolean valueSelected = !getSelectedSearchResults().isEmpty();
        requireButton.setEnabled(valueSelected);
        requireDevButton.setEnabled(valueSelected);
    }

    @NbBundle.Messages("SearchPanel.details.nothing=Double click result to see its details.")
    void updateResultDetails(boolean fetchDetails) {
        assert EventQueue.isDispatchThread();
        String msg = Bundle.SearchPanel_details_nothing();
        List<SearchResult> selectedSearchResults = getSelectedSearchResults();
        if (selectedSearchResults.size() == 1) {
            String details = getResultsDetails(selectedSearchResults.get(0).getName(), fetchDetails);
            if (details != null) {
                msg = details;
            }
        }
        detailsTextPane.setText(msg);
        detailsTextPane.setCaretPosition(0);
    }

    @NbBundle.Messages("SearchPanel.details.loading=Loading result details...")
    private String getResultsDetails(final String resultName, boolean fetchDetails) {
        if (resultName == null) {
            return null;
        }
        String details = resultDetails.get(resultName);
        if (details != null) {
            return details;
        }
        if (!fetchDetails) {
            return null;
        }
        final Composer composer = getComposer();
        if (composer == null) {
            return null;
        }
        String loading = Bundle.SearchPanel_details_loading();
        String prev = resultDetails.putIfAbsent(resultName, loading);
        assert prev == null : "Previous message found?!: " + prev;
        postShowRequestProcessor.post(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer(200);
                Future<Integer> task = composer.show(phpModule, resultName, new Composer.OutputProcessor<String>() {
                    @Override
                    public void process(String chunk) {
                        buffer.append(chunk);
                    }
                });
                if (task != null) {
                    showTasks.add(task);
                    runWhenTaskFinish(task, new Runnable() {
                        @Override
                        public void run() {
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    resultDetails.put(resultName, buffer.toString());
                                    updateResultDetails(false);
                                }
                            });
                        }
                    }, null);
                }
            }
        });
        return loading;
    }

    List<SearchResult> getSelectedSearchResults() {
        assert EventQueue.isDispatchThread();
        Object[] selectedValues = resultsList.getSelectedValues();
        if (selectedValues.length == 0) {
            return Collections.emptyList();
        }
        List<SearchResult> selectedResults = new ArrayList<SearchResult>(selectedValues.length);
        for (Object selectedValue : selectedValues) {
            if (selectedValue != null
                    && selectedValue != SEARCHING_SEARCH_RESULT
                    && selectedValue != NO_RESULTS_SEARCH_RESULT) {
                selectedResults.add((SearchResult) selectedValue);
            }
        }
        return selectedResults;
    }

    List<String> getSelectedNamesWithDefaultVersion() {
        List<SearchResult> selectedSearchResults = getSelectedSearchResults();
        List<String> names = new ArrayList<String>(selectedSearchResults.size());
        for (SearchResult searchResult : selectedSearchResults) {
            names.add(searchResult.getName() + DEFAULT_PACKAGE_VERSION);
        }
        return names;
    }

    @CheckForNull
    Composer getComposer() {
        try {
            return Composer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(Bundle.SearchPanel_error_composer_notValid(), ComposerOptionsPanelController.OPTIONS_SUBPATH);
        }
        return null;
    }

    private void cleanUp() {
        postSearchRequestProcessor.shutdownNow();
        postShowRequestProcessor.shutdownNow();
        cancelTasks(searchTasks);
        cancelTasks(showTasks);
    }

    private void cancelTasks(List<Future<Integer>> tasks) {
        for (Future<Integer> task : tasks) {
            assert task != null;
            task.cancel(true);
        }
    }

    void runWhenTaskFinish(Future<Integer> task, Runnable postTask, @NullAllowed Runnable finalTask) {
        try {
            task.get(3, TimeUnit.MINUTES);
            postTask.run();
        } catch (TimeoutException ex) {
            task.cancel(true);
        } catch (CancellationException ex) {
            // noop, dialog is being closed
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, ComposerOptionsPanelController.OPTIONS_SUBPATH);
        } finally {
            if (finalTask != null) {
                finalTask.run();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        requireDevButton = new JButton();
        requireButton = new JButton();
        keepOpenCheckBox = new JCheckBox();
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
        noteLabel = new JLabel();

        Mnemonics.setLocalizedText(requireDevButton, NbBundle.getMessage(SearchPanel.class, "SearchPanel.requireDevButton.text")); // NOI18N
        requireDevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                requireDevButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(requireButton, NbBundle.getMessage(SearchPanel.class, "SearchPanel.requireButton.text")); // NOI18N
        requireButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                requireButtonActionPerformed(evt);
            }
        });

        keepOpenCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(keepOpenCheckBox, NbBundle.getMessage(SearchPanel.class, "SearchPanel.keepOpenCheckBox.text")); // NOI18N

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

        resultsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                resultsListMouseClicked(evt);
            }
        });
        resultsScrollPane.setViewportView(resultsList);

        outputSplitPane.setLeftComponent(resultsScrollPane);

        detailsTextPane.setEditable(false);
        detailsTextPane.setFont(new Font("Monospaced", 0, 12)); // NOI18N
        detailsScrollPane.setViewportView(detailsTextPane);

        outputSplitPane.setBottomComponent(detailsScrollPane);

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(SearchPanel.class, "SearchPanel.noteLabel.text")); // NOI18N

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
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
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
                .addComponent(outputSplitPane, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("SearchPanel.error.composer.notValid=Composer is not valid.")
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        final Composer composer = getComposer();
        if (composer == null) {
            return;
        }
        cancelTasks(searchTasks);
        cancelTasks(showTasks);
        searchButton.setEnabled(false);
        clearSearchResults();
        addSearchResult(SEARCHING_SEARCH_RESULT);
        String token = tokenTextField.getText();
        boolean onlyName = onlyNameCheckBox.isSelected();
        final Future<Integer> task = composer.search(phpModule, token, onlyName, new Composer.OutputProcessor<SearchResult>() {
            private boolean first = true;

            @Override
            public void process(SearchResult item) {
                if (first) {
                    clearSearchResults();
                    first = false;
                }
                addSearchResult(item);
            }
        });
        if (task == null) {
            enableSearchButton();
        } else {
            searchTasks.add(task);
            postSearchRequestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    runWhenTaskFinish(task, new Runnable() {
                        @Override
                        public void run() {
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    removeSearchResult(SEARCHING_SEARCH_RESULT);
                                    if (searchResults.isEmpty()) {
                                        addSearchResult(NO_RESULTS_SEARCH_RESULT);
                                    }
                                }
                            });
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            enableSearchButton();
                        }
                    });
                }
            });
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void requireButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_requireButtonActionPerformed
        final Composer composer = getComposer();
        if (composer == null) {
            return;
        }
        List<String> selectedNames = getSelectedNamesWithDefaultVersion();
        assert !selectedNames.isEmpty();
        composer.require(phpModule, selectedNames.toArray(new String[selectedNames.size()]));
    }//GEN-LAST:event_requireButtonActionPerformed

    private void requireDevButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_requireDevButtonActionPerformed
        final Composer composer = getComposer();
        if (composer == null) {
            return;
        }
        List<String> selectedNames = getSelectedNamesWithDefaultVersion();
        assert !selectedNames.isEmpty();
        composer.requireDev(phpModule, selectedNames.toArray(new String[selectedNames.size()]));
    }//GEN-LAST:event_requireDevButtonActionPerformed

    private void resultsListMouseClicked(MouseEvent evt) {//GEN-FIRST:event_resultsListMouseClicked
        if (evt.getClickCount() == 2) {
            updateResultDetails(true);
        }
    }//GEN-LAST:event_resultsListMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JScrollPane detailsScrollPane;
    private JTextPane detailsTextPane;
    private JCheckBox keepOpenCheckBox;
    private JLabel noteLabel;
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
            try {
                return searchResults.get(index);
            } catch (IndexOutOfBoundsException ex) {
                // can happen while clearing results
                return null;
            }
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

        @NbBundle.Messages({
            "# {0} - name",
            "# {1} - description",
            "SearchPanel.results.result=<html><b>{0}</b>: {1}",
            "SearchPanel.results.searching=<html><i>Searching...</i>",
            "SearchPanel.results.noResults=<html><i>No results found.</i>"
        })
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String label;
            SearchResult result = (SearchResult) value;
            if (result == SEARCHING_SEARCH_RESULT) {
                label = Bundle.SearchPanel_results_searching();
            } else if (result == NO_RESULTS_SEARCH_RESULT) {
                label = Bundle.SearchPanel_results_noResults();
            } else {
                label = Bundle.SearchPanel_results_result(result.getName(), result.getDescription());
            }
            return originalRenderer.getListCellRendererComponent(list, label.toString(), index, isSelected, cellHasFocus);
        }

    }

}
