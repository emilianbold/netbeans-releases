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

package org.netbeans.modules.ruby.railsprojects.plugins;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * 
 * @todo Use a table instead of a list for the plugin lists, use checkboxes to choose
 *   items to be uninstalled, and show the installation date (based
 *   on file timestamps)
 * @todo Add a project chooser at the top (with refresh behavior on selection)
 *   and add the Rails Plugins actions to the global menu?
 * @todo If there is an error, remember that and don't allow users to select error output
 *   lines as regular plugins
 *
 * @author  Tor Norbye
 */
public class PluginPanel extends javax.swing.JPanel {
    private PluginManager pluginManager;
    
    private List<Plugin> installedPlugins;
    private List<Plugin> newPlugins;
    private List<String> activeRepositories;
    private boolean installedInitialized;
    private boolean newInitialized;
    private boolean repositoriesInitialized;
    private boolean installedModified;
    private boolean newModified;
    private boolean repositoriesModified;
    private boolean pluginsModified;
    
    /** Creates new form PluginPanel */
    public PluginPanel(PluginManager pluginManager) {
        initComponents();
        this.pluginManager = pluginManager;
    }
    
    /** Return whether any plugins were modified - roots should be recomputed after panel is taken down */
    public boolean isModified() {
        return pluginsModified;
    }

    
    private void refreshInstalled() {
        refreshPluginList(getPluginFilter(true), true, installedList, true);
        pluginsModified = pluginsModified || installedModified;
        installedModified = false;
    }
    
    private void refreshNew() {
        refreshPluginList(getPluginFilter(false), true, newList, false);
        newModified = false;
    }

    private void refreshRepositories() {
        refreshRepositoryList(getRepositoryFilter(false), true, repositoryList, true);
        repositoriesModified = false;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        pluginsTab = new javax.swing.JTabbedPane();
        installedPanel = new javax.swing.JPanel();
        instSearchText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        reloadInstalledButton = new javax.swing.JButton();
        uninstallButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        installedList = new javax.swing.JList();
        updateButton = new javax.swing.JButton();
        updateAllButton = new javax.swing.JButton();
        newPanel = new javax.swing.JPanel();
        searchNewText = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        reloadNewButton = new javax.swing.JButton();
        installButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        newList = new javax.swing.JList();
        repositoryPanel = new javax.swing.JPanel();
        searchReposText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        reloadReposButton = new javax.swing.JButton();
        unregisterButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        repositoryList = new javax.swing.JList();
        discoverButton = new javax.swing.JButton();
        addUrlButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        proxyButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        installedPanel.addComponentListener(formListener);

        instSearchText.setColumns(14);
        instSearchText.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.instSearchText.text")); // NOI18N
        instSearchText.addActionListener(formListener);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.jLabel1.text")); // NOI18N

        reloadInstalledButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.reloadInstalledButton.text")); // NOI18N
        reloadInstalledButton.addActionListener(formListener);

        uninstallButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.uninstallButton.text")); // NOI18N
        uninstallButton.setEnabled(false);
        uninstallButton.addActionListener(formListener);

        jScrollPane1.setViewportView(installedList);

        updateButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.updateButton.text")); // NOI18N
        updateButton.setEnabled(false);
        updateButton.addActionListener(formListener);

        updateAllButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.updateAllButton.text")); // NOI18N
        updateAllButton.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout installedPanelLayout = new org.jdesktop.layout.GroupLayout(installedPanel);
        installedPanel.setLayout(installedPanelLayout);
        installedPanelLayout.setHorizontalGroup(
            installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(installedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, installedPanelLayout.createSequentialGroup()
                        .add(reloadInstalledButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 329, Short.MAX_VALUE)
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(instSearchText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 156, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(installedPanelLayout.createSequentialGroup()
                        .add(uninstallButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(updateButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(updateAllButton)))
                .addContainerGap())
        );
        installedPanelLayout.setVerticalGroup(
            installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(installedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(reloadInstalledButton)
                    .add(instSearchText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(uninstallButton)
                    .add(updateAllButton)
                    .add(updateButton))
                .addContainerGap())
        );

        pluginsTab.addTab(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.installedPanel.TabConstraints.tabTitle"), installedPanel); // NOI18N

        newPanel.addComponentListener(formListener);

        searchNewText.setColumns(14);
        searchNewText.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.searchNewText.text")); // NOI18N
        searchNewText.addActionListener(formListener);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.jLabel2.text")); // NOI18N

        reloadNewButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.reloadNewButton.text")); // NOI18N
        reloadNewButton.addActionListener(formListener);

        installButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.installButton.text")); // NOI18N
        installButton.setEnabled(false);
        installButton.addActionListener(formListener);

        jScrollPane2.setViewportView(newList);

        org.jdesktop.layout.GroupLayout newPanelLayout = new org.jdesktop.layout.GroupLayout(newPanel);
        newPanel.setLayout(newPanelLayout);
        newPanelLayout.setHorizontalGroup(
            newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(newPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, newPanelLayout.createSequentialGroup()
                        .add(reloadNewButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 329, Short.MAX_VALUE)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchNewText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 156, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(installButton))
                .addContainerGap())
        );
        newPanelLayout.setVerticalGroup(
            newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(newPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(reloadNewButton)
                    .add(searchNewText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(installButton)
                .addContainerGap())
        );

        pluginsTab.addTab(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.newPanel.TabConstraints.tabTitle"), newPanel); // NOI18N

        repositoryPanel.addComponentListener(formListener);

        searchReposText.setColumns(14);
        searchReposText.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.searchReposText.text")); // NOI18N
        searchReposText.addActionListener(formListener);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.jLabel3.text")); // NOI18N

        reloadReposButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.reloadReposButton.text")); // NOI18N
        reloadReposButton.addActionListener(formListener);

        unregisterButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.unregisterButton.text")); // NOI18N
        unregisterButton.setEnabled(false);
        unregisterButton.addActionListener(formListener);

        jScrollPane3.setViewportView(repositoryList);

        discoverButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.discoverButton.text")); // NOI18N
        discoverButton.addActionListener(formListener);

        addUrlButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.addUrlButton.text")); // NOI18N
        addUrlButton.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout repositoryPanelLayout = new org.jdesktop.layout.GroupLayout(repositoryPanel);
        repositoryPanel.setLayout(repositoryPanelLayout);
        repositoryPanelLayout.setHorizontalGroup(
            repositoryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(repositoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(repositoryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, repositoryPanelLayout.createSequentialGroup()
                        .add(discoverButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(addUrlButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(reloadReposButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 106, Short.MAX_VALUE)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchReposText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 156, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(unregisterButton))
                .addContainerGap())
        );
        repositoryPanelLayout.setVerticalGroup(
            repositoryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(repositoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(repositoryPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(searchReposText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(discoverButton)
                    .add(addUrlButton)
                    .add(reloadReposButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(unregisterButton)
                .addContainerGap())
        );

        pluginsTab.addTab(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.repositoryPanel.TabConstraints.tabTitle"), repositoryPanel); // NOI18N

        proxyButton.setText(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.proxyButton.text")); // NOI18N
        proxyButton.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(proxyButton)
                .addContainerGap(531, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(proxyButton)
                .addContainerGap(376, Short.MAX_VALUE))
        );

        pluginsTab.addTab(org.openide.util.NbBundle.getMessage(PluginPanel.class, "PluginPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(pluginsTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(pluginsTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
                .addContainerGap())
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.ComponentListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == instSearchText) {
                PluginPanel.this.instSearchTextActionPerformed(evt);
            }
            else if (evt.getSource() == reloadInstalledButton) {
                PluginPanel.this.reloadInstalledButtonActionPerformed(evt);
            }
            else if (evt.getSource() == uninstallButton) {
                PluginPanel.this.uninstallButtonActionPerformed(evt);
            }
            else if (evt.getSource() == updateButton) {
                PluginPanel.this.updateButtonActionPerformed(evt);
            }
            else if (evt.getSource() == updateAllButton) {
                PluginPanel.this.updateAllButtonActionPerformed(evt);
            }
            else if (evt.getSource() == searchNewText) {
                PluginPanel.this.searchNewTextActionPerformed(evt);
            }
            else if (evt.getSource() == installButton) {
                PluginPanel.this.installButtonActionPerformed(evt);
            }
            else if (evt.getSource() == searchReposText) {
                PluginPanel.this.searchReposTextActionPerformed(evt);
            }
            else if (evt.getSource() == reloadReposButton) {
                PluginPanel.this.reloadReposButtonActionPerformed(evt);
            }
            else if (evt.getSource() == unregisterButton) {
                PluginPanel.this.unregisterButtonActionPerformed(evt);
            }
            else if (evt.getSource() == discoverButton) {
                PluginPanel.this.discoverButtonActionPerformed(evt);
            }
            else if (evt.getSource() == addUrlButton) {
                PluginPanel.this.addUrlButtonActionPerformed(evt);
            }
            else if (evt.getSource() == proxyButton) {
                PluginPanel.this.proxyButtonActionPerformed(evt);
            }
            else if (evt.getSource() == reloadNewButton) {
                PluginPanel.this.reloadNewButtonActionPerformed(evt);
            }
        }

        public void componentHidden(java.awt.event.ComponentEvent evt) {
        }

        public void componentMoved(java.awt.event.ComponentEvent evt) {
        }

        public void componentResized(java.awt.event.ComponentEvent evt) {
        }

        public void componentShown(java.awt.event.ComponentEvent evt) {
            if (evt.getSource() == installedPanel) {
                PluginPanel.this.installedPanelComponentShown(evt);
            }
            else if (evt.getSource() == newPanel) {
                PluginPanel.this.newPanelComponentShown(evt);
            }
            else if (evt.getSource() == repositoryPanel) {
                PluginPanel.this.repositoryPanelComponentShown(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

private void reloadNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadNewButtonActionPerformed
    refreshNew();
}//GEN-LAST:event_reloadNewButtonActionPerformed

private void addUrlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addUrlButtonActionPerformed
    NewUrlPanel panel = new NewUrlPanel();

    DialogDescriptor descriptor = new DialogDescriptor(panel, NbBundle.getMessage(PluginPanel.class,
            "AddUrl")); // NOI18N
    Dialog dlg = null;
    //descriptor.setModal(true);
    try {
        dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setVisible(true);
    } finally {
        if (dlg != null)
            dlg.dispose();
    }

    if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
        String url = panel.getUrl();
        if (url != null) {
            pluginManager.addRepositories(new String[] { url }, this, null, true, new RepositoryListRefresher(repositoryList, true));
            newModified = true;
            repositoriesModified = true;
        }
    }
}//GEN-LAST:event_addUrlButtonActionPerformed

private void proxyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proxyButtonActionPerformed
    OptionsDisplayer.getDefault().open("General"); // NOI18Nd
}//GEN-LAST:event_proxyButtonActionPerformed

private void discoverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discoverButtonActionPerformed
    // Bring up a selection list
    String wait = getWaitMsg();
    RepositorySelectionPanel panel = new RepositorySelectionPanel();
    final JList list = panel.getList();
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    DialogDescriptor descriptor = new DialogDescriptor(panel, NbBundle.getMessage(PluginPanel.class,
            "SelectRepos")); // NOI18N
    Dialog dlg = null;
    //descriptor.setModal(true);
    try {
        dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        refreshRepositoryList(null, true, list, false);
        
        dlg.setVisible(true);
    } finally {
        if (dlg != null)
            dlg.dispose();
    }

    if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
        Object[] urls = list.getSelectedValues();
        if (urls != null && urls.length > 0 && !wait.equals(urls[0])) {
            String[] reps = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                reps[i] = urls[i].toString();
            }
            pluginManager.addRepositories(reps, this, null, true, new RepositoryListRefresher(repositoryList, true));
            newModified = true;
            repositoriesModified = true;
        }

    }
}//GEN-LAST:event_discoverButtonActionPerformed

private void searchNewTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchNewTextActionPerformed
    refreshPluginList(getPluginFilter(false), false, newList, false);
}//GEN-LAST:event_searchNewTextActionPerformed

private void searchReposTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchReposTextActionPerformed
    refreshRepositoryList(getRepositoryFilter(true), false, repositoryList, true);
}//GEN-LAST:event_searchReposTextActionPerformed

private void reloadReposButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadReposButtonActionPerformed
    refreshRepositories();
}//GEN-LAST:event_reloadReposButtonActionPerformed

private void unregisterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unregisterButtonActionPerformed
    int[] indices = repositoryList.getSelectedIndices();
    List<String> repositories = new ArrayList<String>();
    if (indices != null) {
        String wait = getWaitMsg();
        for (int index : indices) {
            assert index >= 0;
            String o = repositoryList.getModel().getElementAt(index).toString();
            if (!wait.equals(o)) {
                repositories.add(o);
            }
        }
    }

    if (repositories.size() > 0) {
        Runnable completionTask = new RepositoryListRefresher(repositoryList, true);
        pluginManager.removeRepositories(repositories.toArray(new String[repositories.size()]), this, null, true, completionTask);

        repositoriesModified = true;
        newModified = true;
    }
}//GEN-LAST:event_unregisterButtonActionPerformed

private void repositoryPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_repositoryPanelComponentShown
    // Make sure the list is shown
    if (!repositoriesInitialized) {
        repositoriesInitialized = true;
        repositoryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        repositoryList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent ev) {
                if (ev.getValueIsAdjusting()) {
                    return;
                }
                int index = repositoryList.getSelectedIndex();
                unregisterButton.setEnabled(index != -1);
            }
        });
        
        repositoriesModified = true;
    }
    
    if (repositoriesModified) {
        refreshRepositories();
    }
}//GEN-LAST:event_repositoryPanelComponentShown

private void newPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_newPanelComponentShown
    // Make sure the list is shown
    if (!newInitialized) {
        newInitialized = true;
        newList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        newList.getSelectionModel().addListSelectionListener(new MyListSelectionListener(newList, new JButton[] { installButton }));
        newModified = true;
    }
    
    if (newModified) {
        refreshNew();
    }
}//GEN-LAST:event_newPanelComponentShown

private void installButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installButtonActionPerformed
    int[] indices = newList.getSelectedIndices();
    List<Plugin> plugins = new ArrayList<Plugin>();
    for (int index : indices) {
        Object o = newList.getModel().getElementAt(index);
        if (o instanceof Plugin) { // Could be "Please Wait..." String
            Plugin plugin = (Plugin)o;
            plugins.add(plugin);
        }
    }
    
    if (plugins.size() > 0) {
        for (Plugin chosen : plugins) {
            // Get some information about the chosen plugin
            InstallationSettingsPanel panel = new InstallationSettingsPanel(chosen);
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(PluginPanel.class, "ChoosePluginSettings"));
            dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
            dd.setModal(true);
            dd.setHelpCtx(new HelpCtx(PluginPanel.class));
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (result.equals(NotifyDescriptor.OK_OPTION)) {
                Plugin plugin = new Plugin(panel.getPluginName(), null);
                // XXX Do I really need to refresh it right way?
                PluginListRefresher completionTask = new PluginListRefresher(newList, true);
                boolean changed = pluginManager.install(new Plugin[] { plugin }, this, null, null, panel.isSvnExternals(), panel.isSvnCheckout(), panel.getRevision(), 
                        true, completionTask);
                installedModified = installedModified || changed;
            }
        }
    }

}//GEN-LAST:event_installButtonActionPerformed

private void instSearchTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instSearchTextActionPerformed
    refreshPluginList(getPluginFilter(true), false, installedList, true);
}//GEN-LAST:event_instSearchTextActionPerformed

private void updateAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateAllButtonActionPerformed
    Runnable completionTask = new PluginListRefresher(installedList, true);
    pluginManager.update(null, null, null, this, null, true, completionTask);
    installedModified = true; 
}//GEN-LAST:event_updateAllButtonActionPerformed

private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
    int[] indices = installedList.getSelectedIndices();
    List<Plugin> plugins = new ArrayList<Plugin>();
    if (indices != null) {
        for (int index : indices) {
            assert index >= 0;
            Object o = installedList.getModel().getElementAt(index);
            if (o instanceof Plugin) { // Could be "Please Wait..." String
                Plugin plugin = (Plugin)o;
                plugins.add(plugin);
            }            
        }
    }
    if (plugins.size() > 0) {
        Runnable completionTask = new PluginListRefresher(installedList, true);
        pluginManager.update(plugins.toArray(new Plugin[plugins.size()]), null, null, this, null, true, completionTask);
        installedModified = true;
    }
}//GEN-LAST:event_updateButtonActionPerformed

private void uninstallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uninstallButtonActionPerformed
    int[] indices = installedList.getSelectedIndices();
    List<Plugin> plugins = new ArrayList<Plugin>();
    if (indices != null) {
        for (int index : indices) {
            assert index >= 0;
            Object o = installedList.getModel().getElementAt(index);
            if (o instanceof Plugin) { // Could be "Please Wait..." String
                Plugin plugin = (Plugin)o;
                plugins.add(plugin);
            }            
        }
    }
    if (plugins.size() > 0) {
        Runnable completionTask = new PluginListRefresher(installedList, true);
        pluginManager.uninstall(plugins.toArray(new Plugin[plugins.size()]), null, this, null, true, completionTask);
        installedModified = true;
    }
}//GEN-LAST:event_uninstallButtonActionPerformed

private void reloadInstalledButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadInstalledButtonActionPerformed
    refreshInstalled();
}//GEN-LAST:event_reloadInstalledButtonActionPerformed

private void installedPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_installedPanelComponentShown
    // Make sure the list is shown
    if (!installedInitialized) {
        installedInitialized = true;
        installedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        installedList.getSelectionModel().addListSelectionListener(new MyListSelectionListener(installedList, new JButton[] { updateButton, uninstallButton }));
        
        installedModified = true;
    }
    
    if (installedModified) {
        refreshInstalled();
    }
}//GEN-LAST:event_installedPanelComponentShown

    private String getWaitMsg() {
        return NbBundle.getMessage(PluginPanel.class, "PleaseWait");
    }

    /** Refresh the list of displayed plugins. If refresh is true, refresh the list from the plugin manager, otherwise just refilter list */
    private void refreshPluginList(final String filter, final boolean refresh, final JList list, final boolean local) {        
        boolean showRefreshMessage = true;
        //        if (!pluginManager.hasUptodateAvailableList()) {
        //            // No need to ask for cached version if the full version will be displayed shortly
        //            List<Plugin> cachedList = pluginManager.getCachedAvailablePlugins();
        //            if (cachedList != null && cachedList.size() > 0) {
        //                plugins = cachedList;
        //                DefaultListModel model = new DefaultListModel();
        //                for (Plugin plugin : cachedList) {
        //                    model.addElement(plugin);
        //                }
        //                pluginList.setModel(model);
        //                showRefreshMessage = false;
        //                String cacheMsg = NbBundle.getMessage(PluginPanel.class, "ShowingCached");
        //                //descArea.setText("");
        //                repositoryText.setText(cacheMsg);
        //            }
        //        }
        Runnable runner = new Runnable() {
            public void run() {
                synchronized(this) {
                    DefaultListModel model = new DefaultListModel();
                    List<String> lines = new ArrayList<String>(500);
                    List<Plugin> plugins;
                    if (local) {
                        if (refresh || installedPlugins == null) {
                            installedPlugins = pluginManager.getInstalledPlugins(true, null, lines);
                        }
                        plugins = installedPlugins;
                    } else {
                        if (refresh || newPlugins == null) {
                            newPlugins = pluginManager.getAvailablePlugins(true, null, lines);
                        }
                        plugins = newPlugins;
                    }
                    Pattern pattern = null;
                    if ((filter != null) && (filter.indexOf('*') != -1 || filter.indexOf('^') != -1 || filter.indexOf('$') != -1)) {
                        try {
                            pattern = Pattern.compile(filter);
                        } catch (PatternSyntaxException pse) {
                            // Don't treat the filter as a regexp
                        }
                    }
                    for (Plugin plugin : plugins) {
                        if (filter == null || filter.length() == 0) {
                            model.addElement(plugin);
                        } else if (pattern == null) {
                           if ((plugin.getName().indexOf(filter) != -1) || 
                                   (plugin.getRepository() != null && plugin.getRepository().indexOf(filter) != -1)) {
                                model.addElement(plugin);
                           }
                        } else if (pattern.matcher(plugin.getName()).find() || 
                                (plugin.getRepository() != null && pattern.matcher(plugin.getRepository()).find())) {
                            model.addElement(plugin);
                        }
                    }
                    if (refresh && plugins.size() == 0) {
                        // TODO - don't do this when I'm showing a cached list!!!
                        if (!local) { // having nothing is not unusual - it's how you start out
                            model.addElement(NbBundle.getMessage(PluginPanel.class, "NoNetwork"));
                        }
                        for (String line : lines) {
                            model.addElement("<html><span color=\"red\">" + line + "</span></html>"); // NOI18N
                        }
                    }
                    list.clearSelection();
                    list.setModel(model);
                    list.invalidate();
                    list.repaint();
                    if (plugins.size() > 0) {
                        list.setSelectedIndex(0);
                    }

                    int tabIndex = local ? 0 : 1;
                    String tabTitle = pluginsTab.getTitleAt(tabIndex);
                    String originalTabTitle = tabTitle;
                    int index = tabTitle.lastIndexOf('(');
                    if (index != -1) {
                        tabTitle = tabTitle.substring(0, index);
                    }
                    tabTitle = tabTitle + "(" + plugins.size() + ")";
                    if (!tabTitle.equals(originalTabTitle)) {
                        pluginsTab.setTitleAt(tabIndex, tabTitle);
                    }
                }
            }
        };
        
        if (refresh ||  (local && installedPlugins == null) || (!local && newPlugins == null)) {
            if (showRefreshMessage) {
                DefaultListModel model = new DefaultListModel();
                model.addElement(NbBundle.getMessage(PluginPanel.class, local ? "FetchingLocalPlugins" : "FetchingRemotePlugins"));
                list.setModel(model);
            }
            RequestProcessor.getDefault().post(runner, 50);
        } else {
            // Do immediate
            runner.run();
        }
    }

    /** Refresh the list of displayed plugins. If refresh is true, refresh the list from the plugin manager, otherwise just refilter list */
    private void refreshRepositoryList(final String filter, final boolean refresh, final JList list, final boolean local) {        
        Runnable runner = new Runnable() {
            public void run() {
                synchronized(this) {
                    DefaultListModel model = new DefaultListModel();
                    List<String> lines = new ArrayList<String>(500);
                    List<String> repositories;
                    if (local) {
                        if (refresh || activeRepositories == null) {
                            activeRepositories = pluginManager.getRepositories(local);
                        }
                        repositories = activeRepositories;
                    } else {
                        // No filtering for the remote repository list
                        repositories = pluginManager.getRepositories(local);
                    }
                    Pattern pattern = null;
                    if ((filter != null) && (filter.indexOf('*') != -1 || filter.indexOf('^') != -1 || filter.indexOf('$') != -1)) {
                        try {
                            pattern = Pattern.compile(filter);
                        } catch (PatternSyntaxException pse) {
                            // Don't treat the filter as a regexp
                        }
                    }
                    for (String repository : repositories) {
                        if (filter == null || filter.length() == 0) {
                            model.addElement(repository);
                        } else if (pattern == null) {
                           if (repository.indexOf(filter) != -1) {
                                model.addElement(repository);
                           }
                        } else if (pattern.matcher(repository).find()) {
                            model.addElement(repository);
                        }
                    }
                    if (refresh && repositories.size() == 0) {
                        // TODO - don't do this when I'm showing a cached list!!!
                        model.addElement(NbBundle.getMessage(PluginPanel.class, "NoNetwork"));
                        for (String line : lines) {
                            model.addElement("<html><span color=\"red\">" + line + "</span></html>"); // NOI18N
                        }
                    }
                    list.clearSelection();
                    list.setModel(model);
                    list.invalidate();
                    list.repaint();
                    if (repositories.size() > 0) {
                        list.setSelectedIndex(0);
                    }
                }
            }
        };
        
        if (refresh || (local && activeRepositories == null) || !local) {
            DefaultListModel model = new DefaultListModel();
            model.addElement(NbBundle.getMessage(PluginPanel.class, local ? "FetchingLocalRepos" : "FetchingRemoteRepos"));
            list.setModel(model);
            RequestProcessor.getDefault().post(runner, 50);
        } else {
            // Do immediate
            runner.run();
        }
    }
    
    
    private String getRepositoryFilter(boolean local) {
        String filter = null;
        if (local) {
            filter = searchReposText.getText().trim();
            if (filter.length() == 0) {
                filter = null;
            }
        }
        
        return filter;
    }

    private String getPluginFilter(boolean local) {
        String filter = null;
        JTextField tf = local ? instSearchText : searchNewText;
        filter = tf.getText().trim();
        if (filter.length() == 0) {
            filter = null;
        }
        
        return filter;
    }
    
    private class PluginListRefresher implements Runnable {
        private JList list;
        private boolean local;
        
        public PluginListRefresher(JList list, boolean local) {
            this.list = list;
            this.local = local;
        }

        public void run() {
            refreshPluginList(getPluginFilter(local), true, list, local);
            if (list == newList) {
                newModified = false;
            } else if (list == installedList) {
                pluginsModified = pluginsModified || installedModified;
                installedModified = false;
            }
        }
    }

    private class RepositoryListRefresher implements Runnable {
        private JList list;
        private boolean local;
        
        public RepositoryListRefresher(JList list, boolean local) {
            this.list = list;
            this.local = local;
        }

        public void run() {
            refreshRepositoryList(getRepositoryFilter(local), true, list, local);
            if (list == repositoryList) {
                repositoriesModified = false;
            }
        }
    }
    
    private class MyListSelectionListener implements ListSelectionListener {
        private JButton[] buttons;
        private JList list;
        
        private MyListSelectionListener(JList list, JButton[] buttons) {
            this.list = list;
            this.buttons = buttons;
        }
        public void valueChanged(ListSelectionEvent ev) {
            if (ev.getValueIsAdjusting()) {
                return;
            }
            int index = list.getSelectedIndex();
            if (index != -1) {
                Object o = list.getModel().getElementAt(index);
                if (o instanceof Plugin) { // Could be "Please Wait..." String
                    for (JButton button :buttons) {
                        button.setEnabled(true);
                    }
                    return;
                }
            }
            for (JButton button :buttons) {
                button.setEnabled(index != -1);
            }
        }
    }
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addUrlButton;
    private javax.swing.JButton discoverButton;
    private javax.swing.JTextField instSearchText;
    private javax.swing.JButton installButton;
    private javax.swing.JList installedList;
    private javax.swing.JPanel installedPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList newList;
    private javax.swing.JPanel newPanel;
    private javax.swing.JTabbedPane pluginsTab;
    private javax.swing.JButton proxyButton;
    private javax.swing.JButton reloadInstalledButton;
    private javax.swing.JButton reloadNewButton;
    private javax.swing.JButton reloadReposButton;
    private javax.swing.JList repositoryList;
    private javax.swing.JPanel repositoryPanel;
    private javax.swing.JTextField searchNewText;
    private javax.swing.JTextField searchReposText;
    private javax.swing.JButton uninstallButton;
    private javax.swing.JButton unregisterButton;
    private javax.swing.JButton updateAllButton;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
    
}
