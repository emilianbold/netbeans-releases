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
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.ListSelectionModel;
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
public final class AddDependencyPanel extends JPanel {

    private static final long serialVersionUID = -4572187014657456L;

    private static final SearchResult SEARCHING_SEARCH_RESULT = new SearchResult(null, null);
    private static final SearchResult NO_RESULTS_SEARCH_RESULT = new SearchResult(null, null);

    // @GuardedBy("EDT")
    private static boolean keepOpened = true;

    private final PhpModule phpModule;
    private final List<SearchResult> searchResults = Collections.synchronizedList(new ArrayList<SearchResult>());
    // @GuardedBy("EDT")
    private final ResultsListModel resultsModel = new ResultsListModel(searchResults);
    private final ConcurrentMap<String, String> resultDetails = new ConcurrentHashMap<>();
    // @GuardedBy("EDT")
    private final VersionComboBoxModel versionsModel = new VersionComboBoxModel();
    // tasks
    private final RequestProcessor postSearchRequestProcessor = new RequestProcessor(AddDependencyPanel.class.getName() + " (POST SEARCH)"); // NOI18N
    private final RequestProcessor postShowRequestProcessor = new RequestProcessor(AddDependencyPanel.class.getName() + " (POST SHOW)"); // NOI18N
    private final List<Future<Integer>> searchTasks = new CopyOnWriteArrayList<>();
    private final List<Future<Integer>> showTasks = new CopyOnWriteArrayList<>();


    private AddDependencyPanel(PhpModule phpModule) {
        assert phpModule != null;

        this.phpModule = phpModule;

        initComponents();
        init();
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "AddDependencyPanel.panel.title=Composer Packages ({0})",
    })
    public static void open(PhpModule phpModule) {
        assert EventQueue.isDispatchThread();

        AddDependencyPanel searchPanel = new AddDependencyPanel(phpModule);
        Object[] options = new Object[] {
            searchPanel.requireButton,
            searchPanel.requireDevButton,
            DialogDescriptor.CANCEL_OPTION,
        };

        final DialogDescriptor descriptor = new DialogDescriptor(
                searchPanel,
                Bundle.AddDependencyPanel_panel_title(phpModule.getDisplayName()),
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

    private static void setDefaultButton(Dialog dialog, final AddDependencyPanel searchPanel) {
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

    private static void handleKeepOpen(final Dialog dialog, final AddDependencyPanel searchPanel) {
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
        initVersions();
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
        resultsList.setCellRenderer(new ResultListCellRenderer());
        // details
        updateResultDetailsAndVersions(false);
        // listeners
        resultsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                resultsChanged();
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
                resultsChanged();
            }
        });
    }

    private void initVersions() {
        versionComboBox.setModel(versionsModel);
        // listeners
        versionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                versionChanged();
            }
        });
        versionsModel.addListDataListener(new ListDataListener() {
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
                versionChanged();
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

    void resultsChanged() {
        enableRequireButtons();
        updateResultDetailsAndVersions(false);
    }

    void versionChanged() {
        enableRequireButtons();
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
        addSearchResult(searchResult, false);
    }

    void addSearchResult(final SearchResult searchResult, final boolean select) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                searchResults.add(searchResult);
                resultsModel.fireContentsChanged();
                if (select) {
                    resultsList.setSelectedValue(searchResult, true);
                }
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
        boolean validResultSelected = false;
        if (getSelectedSearchResult() != null
                && getSelectedResultVersion() != null) {
            validResultSelected = true;
        }
        requireButton.setEnabled(validResultSelected);
        requireDevButton.setEnabled(validResultSelected);
    }

    void updateResultDetailsAndVersions(boolean fetchDetails) {
        assert EventQueue.isDispatchThread();
        String msg = ""; // NOI18N
        List<String> versions = null;
        SearchResult selectedSearchResult = getSelectedSearchResult();
        if (selectedSearchResult != null) {
            String name = selectedSearchResult.getName();
            String details = getResultsDetails(name, fetchDetails);
            if (details != null) {
                msg = details;
            }
            versions = getResultVersions(name);
        }
        detailsTextPane.setText(msg);
        detailsTextPane.setCaretPosition(0);
        if (versions == null) {
            versionsModel.setNoVersions();
        } else {
            versionsModel.setVersions(versions);
        }
    }

    @NbBundle.Messages("AddDependencyPanel.details.loading=Loading package details...")
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
        String loading = Bundle.AddDependencyPanel_details_loading();
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
                                    updateResultDetailsAndVersions(false);
                                }
                            });
                        }
                    }, null);
                }
            }
        });
        return loading;
    }

    @CheckForNull
    private List<String> getResultVersions(String resultName) {
        if (resultName == null) {
            return null;
        }
        String details = resultDetails.get(resultName);
        if (details == null) {
            // not fetched yet
            return null;
        }
        return VersionsParser.parse(details);
    }

    @CheckForNull
    SearchResult getSelectedSearchResult() {
        assert EventQueue.isDispatchThread();
        Object selectedValue = resultsList.getSelectedValue();
        if (selectedValue == null
                || selectedValue == SEARCHING_SEARCH_RESULT
                || selectedValue == NO_RESULTS_SEARCH_RESULT) {
            return null;
        }
        return (SearchResult) selectedValue;
    }

    @CheckForNull
    String getSelectedResultVersion() {
        assert EventQueue.isDispatchThread();
        String selectedVersion = versionsModel.getSelectedItem();
        if (selectedVersion == VersionComboBoxModel.NO_VERSIONS_AVAILABLE) {
            return null;
        }
        return selectedVersion;
    }

    String getSelectedNameWithVersion() {
        SearchResult selectedSearchResult = getSelectedSearchResult();
        assert selectedSearchResult != null;
        String selectedVersion = getSelectedResultVersion();
        assert selectedVersion != null;
        return selectedSearchResult.getName() + ":" + selectedVersion; // NOI18N
    }

    @NbBundle.Messages("AddDependencyPanel.error.composer.notValid=Composer is not valid.")
    @CheckForNull
    Composer getComposer() {
        try {
            return Composer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(Bundle.AddDependencyPanel_error_composer_notValid(), ComposerOptionsPanelController.OPTIONS_SUBPATH);
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

    private void initComposer(Composer composer, Runnable postTask) {
        Future<Integer> task = composer.initIfNotPresent(phpModule);
        if (task == null) {
            // file exists already
            postTask.run();
            return;
        }
        runWhenTaskFinish(task, postTask, null);
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
        packagesLabel = new JLabel();
        outputSplitPane = new JSplitPane();
        resultsScrollPane = new JScrollPane();
        resultsList = new JList<SearchResult>();
        detailsScrollPane = new JScrollPane();
        detailsTextPane = new JTextPane();
        versionLabel = new JLabel();
        versionComboBox = new JComboBox<String>();

        Mnemonics.setLocalizedText(requireDevButton, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.requireDevButton.text")); // NOI18N
        requireDevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                requireDevButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(requireButton, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.requireButton.text")); // NOI18N
        requireButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                requireButtonActionPerformed(evt);
            }
        });

        keepOpenCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(keepOpenCheckBox, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.keepOpenCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(tokenLabel, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.tokenLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(onlyNameCheckBox, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.onlyNameCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(packagesLabel, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.packagesLabel.text")); // NOI18N

        outputSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        outputSplitPane.setResizeWeight(0.5);

        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

        versionLabel.setLabelFor(versionComboBox);
        Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.versionLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(outputSplitPane, GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
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
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(packagesLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(versionLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(versionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
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
                .addComponent(packagesLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputSplitPane, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
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
                    first = false;
                    clearSearchResults();
                    addSearchResult(item, true);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateResultDetailsAndVersions(true);
                        }
                    });
                } else {
                    addSearchResult(item);
                }
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
        assert EventQueue.isDispatchThread();
        final Composer composer = getComposer();
        if (composer == null) {
            return;
        }
        final String selectedName = getSelectedNameWithVersion();
        initComposer(composer, new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                composer.require(phpModule, selectedName);
            }
        });
    }//GEN-LAST:event_requireButtonActionPerformed

    private void requireDevButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_requireDevButtonActionPerformed
        assert EventQueue.isDispatchThread();
        final Composer composer = getComposer();
        if (composer == null) {
            return;
        }
        final String selectedName = getSelectedNameWithVersion();
        initComposer(composer, new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                composer.requireDev(phpModule, selectedName);
            }
        });
    }//GEN-LAST:event_requireDevButtonActionPerformed

    private void resultsListMouseClicked(MouseEvent evt) {//GEN-FIRST:event_resultsListMouseClicked
        updateResultDetailsAndVersions(true);
    }//GEN-LAST:event_resultsListMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JScrollPane detailsScrollPane;
    private JTextPane detailsTextPane;
    private JCheckBox keepOpenCheckBox;
    private JCheckBox onlyNameCheckBox;
    private JSplitPane outputSplitPane;
    private JLabel packagesLabel;
    private JButton requireButton;
    private JButton requireDevButton;
    private JList<SearchResult> resultsList;
    private JScrollPane resultsScrollPane;
    private JButton searchButton;
    private JLabel tokenLabel;
    private JTextField tokenTextField;
    private JComboBox<String> versionComboBox;
    private JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class ResultsListModel extends AbstractListModel<SearchResult> {

        private static final long serialVersionUID = -897454654321324564L;

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
        public SearchResult getElementAt(int index) {
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

    private static final class ResultListCellRenderer implements ListCellRenderer<SearchResult> {

        private final ListCellRenderer<Object> defaultRenderer = new DefaultListCellRenderer();

        @NbBundle.Messages({
            "# {0} - name",
            "# {1} - description",
            "AddDependencyPanel.results.result=<html><b>{0}</b>: {1}",
            "AddDependencyPanel.results.searching=<html><i>Searching...</i>",
            "AddDependencyPanel.results.noResults=<html><i>No results found.</i>"
        })
        @Override
        public Component getListCellRendererComponent(JList<? extends SearchResult> list, SearchResult value, int index, boolean isSelected, boolean cellHasFocus) {
            String label;
            SearchResult result = value;
            if (result == SEARCHING_SEARCH_RESULT) {
                label = Bundle.AddDependencyPanel_results_searching();
            } else if (result == NO_RESULTS_SEARCH_RESULT) {
                label = Bundle.AddDependencyPanel_results_noResults();
            } else {
                label = Bundle.AddDependencyPanel_results_result(result.getName(), result.getDescription());
            }
            return defaultRenderer.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
        }

    }

    private static final class VersionComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {

        private static final long serialVersionUID = -7546812657897987L;

        @NbBundle.Messages("VersionComboBoxModel.noVersions=<no versions available>")
        static final String NO_VERSIONS_AVAILABLE = Bundle.VersionComboBoxModel_noVersions();


        // @GuardedBy("EDT")
        private final List<String> versions = new ArrayList<>();

        private volatile String selectedVersion = null;


        public VersionComboBoxModel() {
            setNoVersions();
        }

        @Override
        public int getSize() {
            assert EventQueue.isDispatchThread();
            return versions.size();
        }

        @Override
        public String getElementAt(int index) {
            assert EventQueue.isDispatchThread();
            return versions.get(index);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selectedVersion = (String) anItem;
            fireContentsChanged();
        }

        @CheckForNull
        @Override
        public String getSelectedItem() {
            return selectedVersion;
        }

        public void setNoVersions() {
            assert EventQueue.isDispatchThread();
            this.versions.clear();
            versions.add(NO_VERSIONS_AVAILABLE);
            selectedVersion = NO_VERSIONS_AVAILABLE;
            fireContentsChanged();
        }

        public void setVersions(List<String> versions) {
            assert EventQueue.isDispatchThread();
            this.versions.clear();
            this.versions.addAll(versions);
            if (!versions.isEmpty()) {
                selectedVersion = versions.get(0);
            }
            fireContentsChanged();
        }

        private void fireContentsChanged() {
            fireContentsChanged(this, 0, Integer.MAX_VALUE);
        }

    }

    private static final class VersionsParser  {

        private static final String VERSIONS_PREFIX = "versions : "; // NOI18N
        private static final String VERSIONS_DELIMITER = ", "; // NOI18N


        private VersionsParser() {
        }

        @CheckForNull
        public static List<String> parse(String details) {
            String versionsLine = getVersionLine(details);
            if (versionsLine == null) {
                return null;
            }
            return getVersions(versionsLine);
        }

        @CheckForNull
        private static String getVersionLine(String details) {
            for (String line : details.split("\n")) { // NOI18N
                line = line.trim();
                if (line.startsWith(VERSIONS_PREFIX)) {
                    return line.substring(VERSIONS_PREFIX.length());
                }
            }
            return null;
        }

        private static List<String> getVersions(String versionsLine) {
            List<String> versions = new ArrayList<>(StringUtils.explode(versionsLine, VERSIONS_DELIMITER));
            versions.add("*"); // NOI18N
            return versions;
        }

    }

}
