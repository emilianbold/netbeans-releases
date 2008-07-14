/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.groovy.grailsproject.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPlugin;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPluginsManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author David Calavera
 */
public class GrailsPluginsPanel extends javax.swing.JPanel {

    private GrailsProject project;
    private GrailsPluginsManager pluginsManager;
    
    private boolean installedInitialized;
    private boolean installedModified;
    private boolean availablePluginsInitialized;
    private boolean availableModified;
    private List<GrailsPlugin> installedPluginsList = new ArrayList<GrailsPlugin>();
    private List<GrailsPlugin> availablePluginsList = new ArrayList<GrailsPlugin>();

    /** Creates new customizer GrailsPluginsPanel */
    public GrailsPluginsPanel(Project project) {
        initComponents();
        this.project = (GrailsProject) project;
        this.pluginsManager = GrailsPluginsManager.getInstance(this.project);
    }
    
    /** Refresh the installed plugin list */
    private void refreshInstalled() {
        assert SwingUtilities.isEventDispatchThread();
        final DefaultListModel model = new DefaultListModel();
        reloadInstalledButton.setEnabled(false);
        installedPluginsList = pluginsManager.refreshInstalledPlugins();
        for (GrailsPlugin plugin : installedPluginsList) {
            model.addElement(plugin);
        }        

        installedPlugins.clearSelection();
        installedPlugins.setModel(model);
        installedPlugins.invalidate();
        installedPlugins.repaint();
        reloadInstalledButton.setEnabled(true);         
    }
    
    private void refreshAvailable() {   
        assert SwingUtilities.isEventDispatchThread();
        
        final Runnable runner = new Runnable() {
            public void run() {
                synchronized(GrailsPluginsPanel.this) {
                    final DefaultListModel model = new DefaultListModel();
                    availablePluginsList = pluginsManager.refreshAvailablePlugins();
                    
                    for (GrailsPlugin plugin : availablePluginsList) {
                        if (!installedPluginsList.contains(plugin)) {
                            model.addElement(plugin);
                        }
                    }
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            availablePlugins.clearSelection();
                            availablePlugins.setModel(model);
                            availablePlugins.invalidate();
                            availablePlugins.repaint();
                            reloadAvailableButton.setEnabled(true);
                        }
                    });
                }
            }
        };
        
        reloadAvailableButton.setEnabled(false);
        final DefaultListModel model = new DefaultListModel();
        model.addElement(NbBundle.getMessage(GrailsPluginsPanel.class, "FetchingPlugins"));
        availablePlugins.setModel(model);
            
        RequestProcessor.getDefault().post(runner);
    }
    
    private void uninstallPlugins() {
        final Object[] selected = installedPlugins.getSelectedValues();
        
        pluginsManager.uninstallPlugins(toPluginCollection(selected));

        refreshInstalled();
        availableModified = true;
    }
    
    private void installPlugins() {
        installButton.setEnabled(false);
        
        final Object[] selected = availablePlugins.getSelectedValues();
        Collection<GrailsPlugin> selectedColl = toPluginCollection(selected);
        
        if (pluginZipPath.getText() != null && pluginZipPath.getText().trim().length() > 0) {
            GrailsPlugin plugin = pluginsManager.getPluginFromZipFile(pluginZipPath.getText());
            if (plugin != null && !selectedColl.contains(plugin)
                    && !installedPluginsList.contains(plugin)) {
                selectedColl.add(plugin);
            }
        }
        availableModified = pluginsManager.installPlugins(selectedColl);

        refreshInstalled();        

        if (availableModified) {
            pluginsPanel.setSelectedComponent(installedPanel);
            pluginsPanel.invalidate();
            pluginsPanel.repaint();
        }
        installButton.setEnabled(true);
    }
    
    private Collection<GrailsPlugin> toPluginCollection(Object[] selected) {
        Collection<GrailsPlugin> coll = new ArrayList<GrailsPlugin>();
        if (selected != null && selected.length > 0) {                  
            for (Object obj : selected) {
                coll.add((GrailsPlugin) obj);
            }
        }
        return coll;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pluginsPanel = new javax.swing.JTabbedPane();
        installedPanel = new javax.swing.JPanel();
        reloadInstalledButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        installedPlugins = new javax.swing.JList();
        uninstallButton = new javax.swing.JButton();
        newPluginPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        availablePlugins = new javax.swing.JList();
        installButton = new javax.swing.JButton();
        reloadAvailableButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        pluginZipPath = new javax.swing.JTextField();
        pluginBrowseButton = new javax.swing.JButton();

        installedPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                installedPanelComponentShown(evt);
            }
        });

        reloadInstalledButton.setText("Reload Plugins");
        reloadInstalledButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadInstalledButtonActionPerformed(evt);
            }
        });

        installedPlugins.setModel(new javax.swing.AbstractListModel() {
            public int getSize() { return installedPluginsList.size(); }
            public Object getElementAt(int arg0) { return installedPluginsList.get(arg0); }
        });
        installedPlugins.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                installedPluginsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(installedPlugins);

        uninstallButton.setText("Uninstall");
        uninstallButton.setEnabled(false);
        uninstallButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uninstallButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout installedPanelLayout = new org.jdesktop.layout.GroupLayout(installedPanel);
        installedPanel.setLayout(installedPanelLayout);
        installedPanelLayout.setHorizontalGroup(
            installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(installedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(uninstallButton)
                    .add(reloadInstalledButton)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, installedPanelLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE)))
                .addContainerGap())
        );
        installedPanelLayout.setVerticalGroup(
            installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(installedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(reloadInstalledButton)
                .add(17, 17, 17)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 355, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 35, Short.MAX_VALUE)
                .add(uninstallButton)
                .addContainerGap())
        );

        pluginsPanel.addTab("Installed", installedPanel);

        newPluginPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                newPluginPanelComponentShown(evt);
            }
        });

        availablePlugins.setModel(new javax.swing.AbstractListModel() {
            public int getSize() { return availablePluginsList.size(); }
            public Object getElementAt(int arg0) { return availablePluginsList.get(arg0); }
        });
        availablePlugins.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                availablePluginsValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(availablePlugins);

        installButton.setText("Install");
        installButton.setEnabled(false);
        installButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installButtonActionPerformed(evt);
            }
        });

        reloadAvailableButton.setText("Reload Plugins");
        reloadAvailableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadAvailableButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Plugin location:");

        pluginBrowseButton.setText("Browse...");
        pluginBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pluginBrowseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout newPluginPanelLayout = new org.jdesktop.layout.GroupLayout(newPluginPanel);
        newPluginPanel.setLayout(newPluginPanelLayout);
        newPluginPanelLayout.setHorizontalGroup(
            newPluginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(newPluginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(newPluginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE)
                    .add(installButton)
                    .add(reloadAvailableButton)
                    .add(newPluginPanelLayout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pluginZipPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 191, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(pluginBrowseButton)))
                .addContainerGap())
        );
        newPluginPanelLayout.setVerticalGroup(
            newPluginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, newPluginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(reloadAvailableButton)
                .add(17, 17, 17)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 323, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(26, 26, 26)
                .add(newPluginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(pluginZipPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pluginBrowseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 16, Short.MAX_VALUE)
                .add(installButton)
                .addContainerGap())
        );

        pluginsPanel.addTab("New plugins", newPluginPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(pluginsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 757, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(pluginsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 508, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void uninstallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uninstallButtonActionPerformed
    uninstallPlugins();
}//GEN-LAST:event_uninstallButtonActionPerformed

private void reloadAvailableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadAvailableButtonActionPerformed
    refreshAvailable();
}//GEN-LAST:event_reloadAvailableButtonActionPerformed

private void reloadInstalledButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadInstalledButtonActionPerformed
    refreshInstalled();
}//GEN-LAST:event_reloadInstalledButtonActionPerformed

private void installButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installButtonActionPerformed
    installPlugins();
}//GEN-LAST:event_installButtonActionPerformed

private void installedPluginsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_installedPluginsValueChanged
    uninstallButton.setEnabled(installedPlugins.getSelectedIndices() != null 
            && installedPlugins.getSelectedIndices().length > 0);    
}//GEN-LAST:event_installedPluginsValueChanged

private void availablePluginsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_availablePluginsValueChanged
    installButton.setEnabled(availablePlugins.getSelectedIndices() != null 
            && availablePlugins.getSelectedIndices().length > 0);
    //waiting the plugin list from the server
    if (availablePlugins.getSelectedIndices().length == 1 && 
            availablePlugins.getSelectedValue().equals(
            NbBundle.getMessage(GrailsPluginsPanel.class, "FetchingPlugins"))) {
        installButton.setEnabled(false);    
    }
}//GEN-LAST:event_availablePluginsValueChanged

private void installedPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_installedPanelComponentShown
    assert SwingUtilities.isEventDispatchThread();

    if (!installedInitialized) {
        installedInitialized = true;
        installedPlugins.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        installedModified = true;
    } else installedModified = false;

    if (installedModified) {
        refreshInstalled();
    }
}//GEN-LAST:event_installedPanelComponentShown

private void newPluginPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_newPluginPanelComponentShown
    assert SwingUtilities.isEventDispatchThread();

    if (!availablePluginsInitialized) {
        availablePluginsInitialized = true;
        availablePlugins.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        availableModified = true;
    }

    if (availableModified) {
        refreshAvailable();
        availableModified = false;
    }
}//GEN-LAST:event_newPluginPanelComponentShown

private void pluginBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pluginBrowseButtonActionPerformed
    final File current = new File(pluginZipPath.getText());
    final JFileChooser chooser = new JFileChooser(current);

    final FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() || file.getPath().toLowerCase().endsWith(".zip");
        }

        @Override
        public String getDescription() {
            return "*.zip";
        }
    };
    
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setFileFilter(fileFilter);            
    chooser.setDialogTitle(NbBundle.getMessage(GrailsPluginsPanel.class, "TITLE_BrowsePluginLocation"));
    chooser.setApproveButtonText(NbBundle.getMessage(GrailsPluginsPanel.class, "LBL_BrowsePluginLocation_OK_Button"));

    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        pluginZipPath.setText(chooser.getSelectedFile().getAbsolutePath());
        installButton.setEnabled(true);
    }
}//GEN-LAST:event_pluginBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList availablePlugins;
    private javax.swing.JButton installButton;
    private javax.swing.JPanel installedPanel;
    private javax.swing.JList installedPlugins;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel newPluginPanel;
    private javax.swing.JButton pluginBrowseButton;
    private javax.swing.JTextField pluginZipPath;
    private javax.swing.JTabbedPane pluginsPanel;
    private javax.swing.JButton reloadAvailableButton;
    private javax.swing.JButton reloadInstalledButton;
    private javax.swing.JButton uninstallButton;
    // End of variables declaration//GEN-END:variables

}
