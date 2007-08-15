/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
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
    private static Rectangle lastSize;
    
    private CustomizerComponentFactory.DependencyListModel universeModules;
    private RequestProcessor.Task filterTask;
    private AddModuleFilter filterer;
    private URL currectJavadoc;
    
    private final SingleModuleProperties props;
    
    public static ModuleDependency[] selectDependencies(final SingleModuleProperties props) {
        final AddModulePanel addPanel = new AddModulePanel(props);
        final DialogDescriptor descriptor = new DialogDescriptor(addPanel,
                getMessage("CTL_AddModuleDependencyTitle"));
        descriptor.setHelpCtx(new HelpCtx(AddModulePanel.class));
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
        return addPanel.getSelectedDependencies();
    }
    
    public AddModulePanel(final SingleModuleProperties props) {
        this.props = props;
        initComponents();
        initAccessibility();
        filterValue.setText(FILTER_DESCRIPTION);
        fillUpUniverseModules();
        moduleList.setCellRenderer(CustomizerComponentFactory.getDependencyCellRenderer(false));
        moduleList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                showDescription();
                currectJavadoc = null;
                ModuleDependency[] deps = getSelectedDependencies();
                if (deps.length == 1) {
                    NbPlatform platform = props.getActivePlatform();
                    if (platform == null) { // NetBeans.org module
                        currectJavadoc = Util.findJavadocForNetBeansOrgModules(deps[0]);
                    } else {
                        currectJavadoc = Util.findJavadoc(deps[0], platform);
                    }
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
        final String lastFilter = filterValue.getText();
        filterValue.setText(CustomizerComponentFactory.WAIT_VALUE);
        moduleList.setModel(CustomizerComponentFactory.createListWaitModel());
        final boolean nonApiDeps = showNonAPIModules.isSelected();
        ModuleProperties.RP.post(new Runnable() {
            public void run() {
                final Set universeDeps = props.getUniverseDependencies(true, !nonApiDeps);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        universeModules = CustomizerComponentFactory.createSortedDependencyListModel(universeDeps);
                        filterer = null;
                        moduleList.setModel(universeModules);
                        moduleList.setEnabled(true);
                        filterValue.setEnabled(true);
                        showNonAPIModules.setEnabled(true);
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
                    String filterTextLC = filterText.toLowerCase(Locale.US);
                    Style match = doc.addStyle(null, null);
                    match.addAttribute(StyleConstants.Background, new Color(246, 248, 139));
                    Set<String> matches = filterer.getMatchesFor(filterText, deps[0]);
                    Iterator it = matches.iterator();
                    boolean isEven = false;
                    Style even = doc.addStyle(null, null);
                    even.addAttribute(StyleConstants.Background, new Color(235, 235, 235));
                    while (it.hasNext()) {
                        String hit = (String) it.next();
                        int loc = doc.getLength();
                        doc.insertString(loc, hit, (isEven ? even : null));
                        int start = hit.toLowerCase(Locale.US).indexOf(filterTextLC);
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
        if (CustomizerComponentFactory.isWaitModel(moduleList.getModel())) {
            deps = new ModuleDependency[0];
        } else {
            Object[] objects = moduleList.getSelectedValues();
            deps = new ModuleDependency[objects.length];
            System.arraycopy(objects, 0, deps, 0, objects.length);
        }
        return deps;
    }
    
    private void search() {
        if (filterTask != null) {
            filterTask.cancel();
            filterTask = null;
        }
        final String text = filterValue.getText();
        if (text.length() == 0) {
            moduleList.setModel(universeModules);
            moduleList.setSelectedIndex(0);
            moduleList.ensureIndexIsVisible(0);
        } else {
            final Runnable compute = new Runnable() {
                public void run() {
                    final Set<ModuleDependency> matches = filterer.getMatches(text);
                    filterTask = null;
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
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
            if (filterer == null) {
                // Slow to create it, so show Please wait...
                moduleList.setModel(CustomizerComponentFactory.createListWaitModel());
                filterTask = RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        if (filterer == null) {
                            filterer = new AddModuleFilter(universeModules.getDependencies(), props.getCodeNameBase());
                            compute.run();
                        }
                    }
                });
            } else {
                // Pretty fast once we have it, so do right now and avoid flickering.
                compute.run();
            }
        }
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setPreferredSize(new java.awt.Dimension(400, 400));
        moduleLabel.setLabelFor(moduleList);
        org.openide.awt.Mnemonics.setLocalizedText(moduleLabel, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Module"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(moduleLabel, gridBagConstraints);

        moduleSP.setViewportView(moduleList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(moduleSP, gridBagConstraints);

        descLabel.setLabelFor(descValue);
        org.openide.awt.Mnemonics.setLocalizedText(descLabel, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Description"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(descLabel, gridBagConstraints);

        filter.setLabelFor(filterValue);
        org.openide.awt.Mnemonics.setLocalizedText(filter, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Filter"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(filter, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(filterValue, gridBagConstraints);

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

        org.openide.awt.Mnemonics.setLocalizedText(showJavadocButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("CTL_ShowJavadoc"));
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
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(showJavadocButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(showNonAPIModules, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "CTL_ShowNonAPIModules"));
        showNonAPIModules.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showNonAPIModules.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showNonAPIModules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNonAPIModulesActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(showNonAPIModules, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void showNonAPIModulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNonAPIModulesActionPerformed
        fillUpUniverseModules();
    }//GEN-LAST:event_showNonAPIModulesActionPerformed
    
    private void showJavadoc(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showJavadoc
        HtmlBrowser.URLDisplayer.getDefault().showURL(currectJavadoc);
    }//GEN-LAST:event_showJavadoc
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descLabel;
    private javax.swing.JTextPane descValue;
    private javax.swing.JScrollPane descValueSP;
    private javax.swing.JLabel filter;
    javax.swing.JTextField filterValue;
    private javax.swing.JLabel moduleLabel;
    javax.swing.JList moduleList;
    private javax.swing.JScrollPane moduleSP;
    private javax.swing.JButton showJavadocButton;
    private javax.swing.JCheckBox showNonAPIModules;
    // End of variables declaration//GEN-END:variables
    
}
