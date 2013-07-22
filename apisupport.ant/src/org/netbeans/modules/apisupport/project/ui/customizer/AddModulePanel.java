/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * 
 * Portions Copyrighted 2013 markiewb@netbeans.org
*/

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.ModuleDependency;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Represents panel for adding new dependency for a module. Shown after
 * <em>Add</em> button on the <code>CustomizerLibraries</code> panel has been
 * pushed.
 *
 * @author Martin Krauskopf, Jesse Glick
 */
public final class AddModulePanel extends JPanel {
    
    private static final String FILTER_DESCRIPTION = getMessage("LBL_FilterDescription");
    private static final RequestProcessor RP = new RequestProcessor(AddModulePanel.class.getName(), 1, true);
    private static Rectangle lastSize;
    
    private CustomizerComponentFactory.DependencyListModel universeModules;
    private RequestProcessor.Task filterTask;
    private AddModuleFilter filterer;
    private URL currectJavadoc;
    
    private final SingleModuleProperties props;
    private Timer timer;
    
    public static ModuleDependency[] selectDependencies(final SingleModuleProperties props) {
        // keep backwards compatibility
        return selectDependencies(props, null);
    }

    /**
     * 
     * @param props
     * @param initialFilterText initial filter text or null if not given
     * @return 
     */
    public static ModuleDependency[] selectDependencies(final SingleModuleProperties props, final String initialFilterText) {
        final AddModulePanel addPanel;
        if (null != initialFilterText) {
            // init dialog with filter text
            addPanel = new AddModulePanel(props, initialFilterText);
        }
        else{
            // keep backwards compatibility
            addPanel = new AddModulePanel(props);
        }
        final DialogDescriptor descriptor = new DialogDescriptor(addPanel,
                getMessage("CTL_AddModuleDependencyTitle"));
        descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.AddModulePanel"));
        descriptor.setClosingOptions(new Object[0]);
        final Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        descriptor.setButtonListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DialogDescriptor.OK_OPTION.equals(e.getSource()) && addPanel.getSelectedDependencies().length == 0) {
                    return;
                }
                d.setVisible(false);
                d.dispose();
            }
        });
        if (lastSize != null) {
            d.setBounds(lastSize);
        }
        d.setVisible(true);
        lastSize = d.getBounds();
        d.dispose();
        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            return addPanel.getSelectedDependencies();
        } else {
            return new ModuleDependency[0]; // #114932
        }
    }
    
    public AddModulePanel(final SingleModuleProperties props) {
        this(props, FILTER_DESCRIPTION);
    }

    private AddModulePanel(final SingleModuleProperties props, String initialString) {
        this.props = props;
        initComponents();
        initAccessibility();
        filterValue.setText(initialString);
        fillUpUniverseModules();
        moduleList.setCellRenderer(CustomizerComponentFactory.getDependencyCellRenderer(false));
        moduleList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                showDescription();
                currectJavadoc = null;
                final ModuleDependency[] deps = getSelectedDependencies();
                if (deps.length == 1) {
                    final NbPlatform platform = props.getActivePlatform();
                    ModuleProperties.RP.post(new Runnable() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    currectJavadoc = deps[0].getModuleEntry().getJavadoc(platform);
                                }
                            });
                        }
                    });
                }
                showJavadocButton.setEnabled(currectJavadoc != null);
            }
        });
        filterValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                if (!FILTER_DESCRIPTION.equals(filterValue.getText())) {
                    search();
                }
            }
        });
        // Make basic navigation commands from the list work from the text field.
        String[][] listNavCommands = {
            { "selectPreviousRow", "selectPreviousRow" }, // NOI18N
            { "selectNextRow", "selectNextRow" }, // NOI18N
            { "selectFirstRow", "selectFirstRow" }, // NOI18N
            { "selectLastRow", "selectLastRow" }, // NOI18N
            { "scrollUp", "scrollUp" }, // NOI18N
            { "scrollDown", "scrollDown" }, // NOI18N
        };
        String[][] areaNavCommands = {
            { "selection-page-up", "page-up" },// NOI18N
            { "selection-page-down", "page-down" },// NOI18N
            { "selection-up", "caret-up" },// NOI18N
            { "selection-down", "caret-down" },// NOI18N
        };
        exchangeCommands(listNavCommands, moduleList, filterValue);
        exchangeCommands(areaNavCommands, descValue, filterValue);
    }
    
    private static void exchangeCommands(String[][] commandsToExchange,
            final JComponent target, final JComponent source) {
        InputMap targetBindings = target.getInputMap();
        KeyStroke[] targetBindingKeys = targetBindings.allKeys();
        ActionMap targetActions = target.getActionMap();
        InputMap sourceBindings = source.getInputMap();
        ActionMap sourceActions = source.getActionMap();
        for (int i = 0; i < commandsToExchange.length; i++) {
            String commandFrom = commandsToExchange[i][0];
            String commandTo = commandsToExchange[i][1];
            final Action orig = targetActions.get(commandTo);
            if (orig == null) {
                continue;
            }
            sourceActions.put(commandTo, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    orig.actionPerformed(new ActionEvent(target, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers()));
                }
            });
            for (int j = 0; j < targetBindingKeys.length; j++) {
                if (targetBindings.get(targetBindingKeys[j]).equals(commandFrom)) {
                    sourceBindings.put(targetBindingKeys[j], commandTo);
                }
            }
        }
    }

    private void fillUpUniverseModules() {
        filterValue.setEnabled(false);
        moduleList.setEnabled(false);
        showNonAPIModules.setEnabled(false);
        matchCaseValue.setEnabled(false);
        showExclModulesCheckBox.setEnabled(false);
        final String lastFilter = filterValue.getText();
        filterValue.setText(UIUtil.WAIT_VALUE);
        moduleList.setModel(UIUtil.createListWaitModel());
        final boolean nonApiDeps = showNonAPIModules.isSelected();
        final boolean exclModules = showExclModulesCheckBox.isSelected();
        ModuleProperties.RP.post(new Runnable() {
            public void run() {
                props.resetUniverseDependencies();  // #165300 refresh in case of added friends/public packages
                final Set<ModuleDependency> universeDeps = props.getUniverseDependencies(!exclModules, !nonApiDeps);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        universeModules = CustomizerComponentFactory.createSortedDependencyListModel(universeDeps);
                        filterer = null;
                        moduleList.setModel(universeModules);
                        moduleList.setEnabled(true);
                        filterValue.setEnabled(true);
                        showNonAPIModules.setEnabled(true);
                        matchCaseValue.setEnabled(true);
                        boolean enableExclModuleChckBox = props.isHasExcludedModules() && props.isSuiteComponent()?true:false;
                        showExclModulesCheckBox.setEnabled(enableExclModuleChckBox);
                        filterValue.setText(lastFilter);
                        if (!FILTER_DESCRIPTION.equals(lastFilter)) {
                            search();
                        } else {
                            filterValue.selectAll();
                        }
                        filterValue.requestFocusInWindow();
                    }
                });
            }
        });
    }
    
    private void showDescription() {
        StyledDocument doc = descValue.getStyledDocument();
        final Boolean matchCase = matchCaseValue.isSelected();
        try {
            doc.remove(0, doc.getLength());
            ModuleDependency[] deps = getSelectedDependencies();
            if (deps.length != 1) {
                return;
            }
            String longDesc = deps[0].getModuleEntry().getLongDescription();
            if (longDesc != null) {
                doc.insertString(0, longDesc, null);
            }
            String filterText = filterValue.getText().trim();
            if (filterText.length() != 0 && !FILTER_DESCRIPTION.equals(filterText)) {
                doc.insertString(doc.getLength(), "\n\n", null); // NOI18N
                Style bold = doc.addStyle(null, null);
                bold.addAttribute(StyleConstants.Bold, Boolean.TRUE);
                doc.insertString(doc.getLength(), getMessage("TEXT_matching_filter_contents"), bold);
                doc.insertString(doc.getLength(), "\n", null); // NOI18N
                if (filterText.length() > 0) {
                    String filterTextLC = matchCase?filterText:filterText.toLowerCase(Locale.US);
                    Style match = doc.addStyle(null, null);
                    match.addAttribute(StyleConstants.Background, new Color(246, 248, 139));
                    boolean isEven = false;
                    Style even = doc.addStyle(null, null);
                    even.addAttribute(StyleConstants.Background, new Color(235, 235, 235));
                    if (filterer == null) {
                        return; // #101776
                    }
                    for (String hit : filterer.getMatchesFor(filterText, deps[0])) {
                        int loc = doc.getLength();
                        doc.insertString(loc, hit, (isEven ? even : null));
                        int start = (matchCase?hit:hit.toLowerCase(Locale.US)).indexOf(filterTextLC);
                        while (start != -1) {
                            doc.setCharacterAttributes(loc + start, filterTextLC.length(), match, true);
                            start = hit.toLowerCase(Locale.US).indexOf(filterTextLC, start + 1);
                        }
                        doc.insertString(doc.getLength(), "\n", (isEven ? even : null)); // NOI18N
                        isEven ^= true;
                    }
                } else {
                    Style italics = doc.addStyle(null, null);
                    italics.addAttribute(StyleConstants.Italic, Boolean.TRUE);
                    doc.insertString(doc.getLength(), getMessage("TEXT_no_filter_specified"), italics);
                }
            }
            descValue.setCaretPosition(0);
        } catch (BadLocationException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public ModuleDependency[] getSelectedDependencies() {
        ModuleDependency[] deps;
        if (UIUtil.isWaitModel(moduleList.getModel())) {
            deps = new ModuleDependency[0];
        } else {
            Object[] objects = moduleList.getSelectedValues();
            deps = new ModuleDependency[objects.length];
            System.arraycopy(objects, 0, deps, 0, objects.length);
        }
        return deps;
    }

    public @Override void removeNotify() {
        super.removeNotify();
        cancelFilterTask();
    }
    
    private synchronized void cancelFilterTask() {
        if (filterTask != null) {
            filterTask.cancel();
            filterTask = null;
            filterer = null;
        }
    }

    private void search() {
        cancelFilterTask();
        final String text = filterValue.getText();
        final Boolean matchCase = matchCaseValue.isSelected();
        if (text.length() == 0) {
            moduleList.setModel(universeModules);
            moduleList.setSelectedIndex(0);
            moduleList.ensureIndexIsVisible(0);
        } else {
            final Runnable compute = new Runnable() {
                public @Override void run() {
                    AddModuleFilter _filterer = filterer;
                    if (_filterer == null) {
                        return;
                    }
                    final Set<ModuleDependency> matches = _filterer.getMatches(text, matchCase);
                    synchronized (AddModulePanel.this) {
                        filterTask = null;
                    }
                    Mutex.EVENT.readAccess(new Runnable() {
                        public @Override void run() {
                            timer.stop();
                            // XXX would be better to have more fine-grained control over the thread
                            if (!text.equals(filterValue.getText())) {
                                return; // no longer valid, don't apply
                            }
                            moduleList.setModel(CustomizerComponentFactory.createDependencyListModel(matches));
                            int index = matches.isEmpty() ? -1 : 0;
                            moduleList.setSelectedIndex(index);
                            moduleList.ensureIndexIsVisible(index);
                        }
                    });
                }
            };
            restartTimer();
            synchronized (this) {
                filterTask = RP.post(new Runnable() {
                    public @Override void run() {
                        if (filterer == null) {
                            filterer = new AddModuleFilter(universeModules.getDependencies(), props.getCodeNameBase());
                        }
                        compute.run();
                    }
                });
            }
        }
    }
    
    private void restartTimer () {
        if (timer == null) {
            timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed (ActionEvent e) {
                    moduleList.setModel(UIUtil.createListWaitModel());
                }
            });
            timer.setRepeats(false);
        }
        timer.restart();
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_AddModuleDependency"));
        filterValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_Filter"));
        moduleList.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ModuleList"));
        moduleSP.getVerticalScrollBar().getAccessibleContext().setAccessibleName(getMessage("ACS_CTL_ModuleListVerticalScroll"));
        moduleSP.getVerticalScrollBar().getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_ModuleListVerticalScroll"));
        moduleSP.getHorizontalScrollBar().getAccessibleContext().setAccessibleName(getMessage("ACS_CTL_ModuleListHorizontalScroll"));
        moduleSP.getHorizontalScrollBar().getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_ModuleListHorizontalScroll"));
        showNonAPIModules.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_ShowNonApiModules"));
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(AddModulePanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        moduleLabel = new javax.swing.JLabel();
        moduleSP = new javax.swing.JScrollPane();
        moduleList = new javax.swing.JList();
        descLabel = new javax.swing.JLabel();
        filter = new javax.swing.JLabel();
        filterValue = new javax.swing.JTextField();
        descValueSP = new javax.swing.JScrollPane();
        descValue = new javax.swing.JTextPane();
        showJavadocButton = new javax.swing.JButton();
        showNonAPIModules = new javax.swing.JCheckBox();
        matchCaseValue = new javax.swing.JCheckBox();
        showExclModulesCheckBox = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setPreferredSize(new java.awt.Dimension(500, 450));
        setLayout(new java.awt.GridBagLayout());

        moduleLabel.setLabelFor(moduleList);
        org.openide.awt.Mnemonics.setLocalizedText(moduleLabel, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Module")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(moduleLabel, gridBagConstraints);

        moduleSP.setPreferredSize(new java.awt.Dimension(450, 116));
        moduleSP.setViewportView(moduleList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(moduleSP, gridBagConstraints);

        descLabel.setLabelFor(descValue);
        org.openide.awt.Mnemonics.setLocalizedText(descLabel, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Description")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(descLabel, gridBagConstraints);

        filter.setLabelFor(filterValue);
        org.openide.awt.Mnemonics.setLocalizedText(filter, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Filter")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        add(filter, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(filterValue, gridBagConstraints);

        descValueSP.setPreferredSize(new java.awt.Dimension(450, 116));

        descValue.setEditable(false);
        descValue.setPreferredSize(new java.awt.Dimension(6, 100));
        descValueSP.setViewportView(descValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(descValueSP, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(showJavadocButton, bundle.getString("CTL_ShowJavadoc")); // NOI18N
        showJavadocButton.setEnabled(false);
        showJavadocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showJavadoc(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(showJavadocButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(showNonAPIModules, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "CTL_ShowNonAPIModules")); // NOI18N
        showNonAPIModules.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showNonAPIModules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNonAPIModulesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(showNonAPIModules, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(matchCaseValue, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "CTL_MatchCase"));
        matchCaseValue.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        matchCaseValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchCaseValueActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 0);
        add(matchCaseValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(showExclModulesCheckBox, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "CTL_ShowExclModules"));
        showExclModulesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showExclModulesCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 0);
        add(showExclModulesCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void showNonAPIModulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNonAPIModulesActionPerformed
        fillUpUniverseModules();
    }//GEN-LAST:event_showNonAPIModulesActionPerformed
    
    private void showJavadoc(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showJavadoc
        HtmlBrowser.URLDisplayer.getDefault().showURL(currectJavadoc);
    }//GEN-LAST:event_showJavadoc

    private void matchCaseValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchCaseValueActionPerformed
        fillUpUniverseModules();
    }//GEN-LAST:event_matchCaseValueActionPerformed

    private void showExclModulesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showExclModulesCheckBoxActionPerformed
        fillUpUniverseModules();
    }//GEN-LAST:event_showExclModulesCheckBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descLabel;
    private javax.swing.JTextPane descValue;
    private javax.swing.JScrollPane descValueSP;
    private javax.swing.JLabel filter;
    javax.swing.JTextField filterValue;
    private javax.swing.JCheckBox matchCaseValue;
    private javax.swing.JLabel moduleLabel;
    javax.swing.JList moduleList;
    private javax.swing.JScrollPane moduleSP;
    private javax.swing.JCheckBox showExclModulesCheckBox;
    private javax.swing.JButton showJavadocButton;
    private javax.swing.JCheckBox showNonAPIModules;
    // End of variables declaration//GEN-END:variables
    
}
