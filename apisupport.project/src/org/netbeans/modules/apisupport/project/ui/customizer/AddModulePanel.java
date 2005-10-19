/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
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
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Represents panel for adding new dependency for a module. Shown after
 * <em>Add</em> button on the <code>CustomizerLibraries</code> panel has been
 * pushed.
 *
 * @author  mkrauskopf
 */
final class AddModulePanel extends JPanel {
    
    private ComponentFactory.DependencyListModel universeModules;
    private RequestProcessor.Task filterTask;
    private AddModuleFilter filterer;
    private URL currectJavadoc;
    
    private final SingleModuleProperties props;
    
    AddModulePanel(final SingleModuleProperties props) {
        this.props = props;
        initComponents();
        initAccessibility();
        fillUpUniverseModules();
        moduleList.setCellRenderer(ComponentFactory.getDependencyCellRenderer(true));
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
                search();
            }
        });
        // Make basic navigation commands from the list work from the text field.
        String[] listNavCommands = {
            "selectPreviousRow", // NOI18N
            "selectNextRow", // NOI18N
            "selectFirstRow", // NOI18N
            "selectLastRow", // NOI18N
            "scrollUp", // NOI18N
            "scrollDown", // NOI18N
        };
        InputMap listBindings = moduleList.getInputMap();
        KeyStroke[] listBindingKeys = listBindings.allKeys();
        ActionMap listActions = moduleList.getActionMap();
        InputMap textBindings = filterValue.getInputMap();
        ActionMap textActions = filterValue.getActionMap();
        for (int i = 0; i < listNavCommands.length; i++) {
            String command = listNavCommands[i];
            final Action orig = listActions.get(command);
            if (orig == null) {
                continue;
            }
            textActions.put(command, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    orig.actionPerformed(new ActionEvent(moduleList, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers()));
                }
            });
            for (int j = 0; j < listBindingKeys.length; j++) {
                if (listBindings.get(listBindingKeys[j]).equals(command)) {
                    textBindings.put(listBindingKeys[j], command);
                }
            }
        }
        // XXX would be nice to also bind S-PageDown etc. to scroll the Description area
    }
    
    private void fillUpUniverseModules() {
        filterValue.setEnabled(false);
        filterValue.setText(ComponentFactory.WAIT_VALUE);
        moduleList.setEnabled(false);
        moduleList.setModel(ComponentFactory.LIST_WAIT_MODEL);
        ModuleProperties.RP.post(new Runnable() {
            public void run() {
                final SortedSet universeDeps = props.getUniverseDependencies(true);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        universeModules = ComponentFactory.createDependencyListModel(universeDeps);
                        moduleList.setModel(universeModules);
                        moduleList.setEnabled(true);
                        filterValue.setText("");
                        filterValue.setEnabled(true);
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
            doc.insertString(doc.getLength(), "\n\n", null); // NOI18N
            Style bold = doc.addStyle(null, null);
            bold.addAttribute(StyleConstants.Bold, Boolean.TRUE);
            doc.insertString(doc.getLength(), getMessage("TEXT_matching_filter_contents"), bold);
            doc.insertString(doc.getLength(), "\n", null); // NOI18N
            String filterText = filterValue.getText();
            if (filterText.length() > 0) {
                String filterTextLC = filterText.toLowerCase(Locale.US);
                Style match = doc.addStyle(null, null);
                match.addAttribute(StyleConstants.Background, new Color(246, 248, 139));
                Set/*<String>*/ matches = filterer.getMatchesFor(filterText, deps[0]);
                Iterator it = matches.iterator();
                while (it.hasNext()) {
                    String hit = (String) it.next();
                    int loc = doc.getLength();
                    doc.insertString(loc, hit, null);
                    int start = hit.toLowerCase(Locale.US).indexOf(filterTextLC);
                    if (start != -1) {
                        doc.setCharacterAttributes(loc + start, filterTextLC.length(), match, true);
                    }
                    if (it.hasNext()) {
                        doc.insertString(doc.getLength(), "; ", null); // NOI18N
                    }
                }
            } else {
                Style italics = doc.addStyle(null, null);
                italics.addAttribute(StyleConstants.Italic, Boolean.TRUE);
                doc.insertString(doc.getLength(), getMessage("TEXT_no_filter_specified"), italics);
            }
            descValue.setCaretPosition(0);
        } catch (BadLocationException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    ModuleDependency[] getSelectedDependencies() {
        Object[] objects = moduleList.getSelectedValues();
        ModuleDependency[] deps = new ModuleDependency[objects.length];
        System.arraycopy(objects, 0, deps, 0, objects.length);
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
                    final SortedSet/*<ModuleDependency>*/ matches
                            = new TreeSet(filterer.getMatches(text));
                    filterTask = null;
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
                            moduleList.setModel(ComponentFactory.createDependencyListModel(matches));
                            int index = matches.isEmpty() ? -1 : 0;
                            moduleList.setSelectedIndex(index);
                            moduleList.ensureIndexIsVisible(index);
                        }
                    });
                }
            };
            if (filterer == null) {
                // Slow to create it, so show Please wait...
                moduleList.setModel(ComponentFactory.LIST_WAIT_MODEL);
                filterTask = RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        if (filterer == null) {
                            filterer = new AddModuleFilter(universeModules.getDependencies());
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
    }
    
    private String getMessage(String key) {
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

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setPreferredSize(new java.awt.Dimension(400, 300));
        moduleLabel.setLabelFor(moduleList);
        org.openide.awt.Mnemonics.setLocalizedText(moduleLabel, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Module"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(moduleLabel, gridBagConstraints);

        moduleSP.setViewportView(moduleList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(moduleSP, gridBagConstraints);

        descLabel.setLabelFor(descValue);
        org.openide.awt.Mnemonics.setLocalizedText(descLabel, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Description"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(filterValue, gridBagConstraints);

        descValue.setEditable(false);
        descValue.setPreferredSize(new java.awt.Dimension(6, 100));
        descValueSP.setViewportView(descValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
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
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(showJavadocButton, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void showJavadoc(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showJavadoc
        HtmlBrowser.URLDisplayer.getDefault().showURL(currectJavadoc);
    }//GEN-LAST:event_showJavadoc
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descLabel;
    private javax.swing.JTextPane descValue;
    private javax.swing.JScrollPane descValueSP;
    private javax.swing.JLabel filter;
    private javax.swing.JTextField filterValue;
    private javax.swing.JLabel moduleLabel;
    private javax.swing.JList moduleList;
    private javax.swing.JScrollPane moduleSP;
    private javax.swing.JButton showJavadocButton;
    // End of variables declaration//GEN-END:variables
    
}
