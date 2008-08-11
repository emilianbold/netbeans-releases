/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
 */

package org.netbeans.modules.ruby.platform.gems;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.PlatformComponentFactory;
import org.netbeans.modules.ruby.platform.RubyPlatformCustomizer;
import org.netbeans.modules.ruby.platform.RubyPreferences;
import org.netbeans.modules.ruby.platform.Util;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @todo Use a table instead of a list for the gem lists, use checkboxes to
 *   choose items to be uninstalled, and show the installation date (based on
 *   file timestamps)
 */
public final class GemPanel extends JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(GemPanel.class.getName());

    /** Preference key for storing lastly used directory when installing new gem. */
    private static final String LAST_GEM_DIRECTORY = "lastLocalGemDirectory"; // NOI18N

    /** Preference key for storing lastly selected platform. */
    private static final String LAST_PLATFORM_ID = "gemPanelLastPlatformID"; // NOI18N

    static enum TabIndex { UPDATED, INSTALLED, NEW; }
    
    private RequestProcessor updateTasksQueue;

    /** Whether this dialog is closed. */
    private boolean closed;

    /** see {@link #isModified} */
    private boolean gemsModified;

    public GemPanel(String availableFilter) {
        this(availableFilter, null);
    }

    /**
     * Creates a new GemPanel.
     * 
     * @param availableFilter the filter to use for displaying gems, e.g. 
     * <code>"generators$"</code> for displaying only generator gems.
     * @param preselected the platform that should be preselected in the panel; 
     * may be <code>null</code> in which case the last selected platform is preselected.
     */
    public GemPanel(String availableFilter, RubyPlatform preselected) {
        updateTasksQueue = new RequestProcessor("Gem Updater", 5); // NOI18N
        initComponents();
        if (preselected == null) {
            Util.preselectPlatform(platforms, LAST_PLATFORM_ID);
        } else {
            platforms.setSelectedItem(preselected);
        }

        GemManager gemManager = getGemManager();
        if (gemManager != null) {
            allVersionsCheckbox.setSelected(!gemManager.hasObsoleteRubyGemsVersion() &&
                    RubyPreferences.shallFetchAllVersions());
        }
        
        descriptionCheckbox.setSelected(RubyPreferences.shallFetchGemDescriptions());
        installedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        installedList.getSelectionModel().addListSelectionListener(new MyListSelectionListener(installedList, installedDesc, uninstallButton));

        newList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        newList.getSelectionModel().addListSelectionListener(new MyListSelectionListener(newList, newDesc, installButton));

        updatedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        updatedList.getSelectionModel().addListSelectionListener(new MyListSelectionListener(updatedList, updatedDesc, updateButton));

        if (availableFilter != null) {
            searchNewText.setText(availableFilter);
            gemsTab.setSelectedIndex(TabIndex.NEW.ordinal());
        }

        PlatformComponentFactory.addPlatformChangeListener(platforms, new PlatformComponentFactory.PlatformChangeListener() {
            public void platformChanged() {
                GemPanel.this.platformChanged();
            }
        });
        platformChanged();
    }

    private void platformChanged() {
        assert EventQueue.isDispatchThread();
        // cancel current update, the platform was changed
        cancelRunningTasks();

        boolean loading = PlatformComponentFactory.isLoadingPlatforms(platforms);
        if (loading || !getSelectedPlatform().hasRubyGemsInstalled()) {
            if (!loading) {
                gemHomeValue.setForeground(PlatformComponentFactory.INVALID_PLAF_COLOR);
                gemHomeValue.setText(GemManager.getNotInstalledMessage());
            }
            updateList(TabIndex.INSTALLED, Collections.<Gem>emptyList());
            updateList(TabIndex.NEW, Collections.<Gem>emptyList());
            updateList(TabIndex.UPDATED, Collections.<Gem>emptyList());
            setEnabledGUI(false);
            hideProgressBars();
        } else {
            GemManager gemManager = getGemManager();
            assert gemManager != null : "gemManager must not be null";
            allVersionsCheckbox.setEnabled(!gemManager.hasObsoleteRubyGemsVersion());

            gemHomeValue.setText(getGemManager().getGemHome());
            gemHomeValue.setForeground(UIManager.getColor("Label.foreground")); // NOI18N
            refreshGemLists();
        }
    }

    public @Override void removeNotify() {
        closed = true;
        cancelRunningTasks();
        RubyPreferences.getPreferences().put(LAST_PLATFORM_ID, getSelectedPlatform().getID());
        super.removeNotify();
    }
    
    private void cancelRunningTasks() {
        LOGGER.finer("Cancelling all running GemPanel tasks");
        // TODO: implement
    }
    
    private static void updateGemDescription(JTextPane pane, Gem gem) {
        assert EventQueue.isDispatchThread();

        if (gem == null) {
            pane.setText("");
            return;
        }

        String htmlMimeType = "text/html"; // NOI18N
        pane.setContentType(htmlMimeType);

        StringBuilder sb = new StringBuilder();
        sb.append("<html>"); // NOI18N
        sb.append("<h2>"); // NOI18N
        sb.append(gem.getName());
        sb.append("</h2>\n"); // NOI18N

        String installedAsString = gem.getInstalledVersionsAsString();
        String availableAsString = gem.getAvailableVersionsAsString();
        if (installedAsString != null && availableAsString != null) {
            // It's an update gem
            sb.append("<h3>"); // NOI18N
            sb.append(getMessage("InstalledVersion"));
            sb.append("</h3>"); // NOI18N
            sb.append(installedAsString);

            sb.append("<h3>"); // NOI18N
            sb.append(getMessage("AvailableVersion"));
            sb.append("</h3>"); // NOI18N
            sb.append(availableAsString);
            sb.append("<br>"); // NOI18N
        } else {
            sb.append("<h3>"); // NOI18N
            String version = installedAsString;
            if (version == null) {
                version = availableAsString;
            }
            if (version.indexOf(',') == -1) {
                sb.append(getMessage("Version"));
            } else {
                sb.append(getMessage("Versions"));
            }
            sb.append("</h3>"); // NOI18N
            sb.append(version);
        }

        if (gem.getDescription() != null) {
            sb.append("<h3>"); // NOI18N
            sb.append(getMessage("Description"));
            sb.append("</h3>"); // NOI18N
            sb.append(gem.getHTMLDescription());
        }

        sb.append("</html>"); // NOI18N

        pane.setText(sb.toString());
        pane.setCaretPosition(0);
    }

    private void setEnabledGUI(boolean enabled) {
        setEnabled(TabIndex.INSTALLED, enabled);
        setEnabled(TabIndex.NEW, enabled);
        setEnabled(TabIndex.UPDATED, enabled);
    }

    private void enableReloadGUI() {
        reloadNewButton.setEnabled(true);
        reloadInstalledButton.setEnabled(true);
        reloadReposButton.setEnabled(true);
        manageButton.setEnabled(true);
    }

    private void setEnabled(TabIndex tab, boolean enabled) {
        switch (tab) {
            case NEW:
                reloadNewButton.setEnabled(enabled);
                if (!enabled) { // decided by list selection
                    installButton.setEnabled(enabled);
                }
                installLocalButton.setEnabled(enabled);
                newPanel.setEnabled(enabled);
                newList.setEnabled(enabled);
                newSP.setEnabled(enabled);
                searchNewLbl.setEnabled(enabled);
                searchNewText.setEnabled(enabled);
                break;
            case UPDATED:
                if (!enabled) { // decided by list selection
                    updateButton.setEnabled(enabled);
                }
                updateAllButton.setEnabled(enabled);
                reloadReposButton.setEnabled(enabled);
                updatedPanel.setEnabled(enabled);
                updatedList.setEnabled(enabled);
                updatedSP.setEnabled(enabled);
                searchUpdatedLbl.setEnabled(enabled);
                searchUpdatedText.setEnabled(enabled);
                break;
            case INSTALLED:
                reloadInstalledButton.setEnabled(enabled);
                if (!enabled) { // decided by list selection
                    uninstallButton.setEnabled(enabled);
                }
                installedPanel.setEnabled(enabled);
                installedList.setEnabled(enabled);
                installedSP.setEnabled(enabled);
                searchInstLbl.setEnabled(enabled);
                searchInstText.setEnabled(enabled);
                break;
            default:
                throw new IllegalArgumentException("Unknonw tab: " + tab); // NOI18N
        }
        boolean everythingDone = newPanel.isEnabled() && updatedPanel.isEnabled() && installedPanel.isEnabled();
        // allow certain actions only when all tabs are updated
        manageButton.setEnabled(everythingDone);
        browseGemHome.setEnabled(everythingDone);
    }

    /**
     * Called when installedGems or availableGems is refreshed.
     * 
     * @return True iff we're done with the updates
     */
    private synchronized void notifyGemsUpdated() {
        assert EventQueue.isDispatchThread();
        GemManager gemManager = getGemManager();
        assert gemManager != null : "gemManager must not be null";
        assert !gemManager.needsReload() : "gemManager is reloaded";
        LOGGER.finer("Updating UI for: " + gemManager);
        
        hideProgressBars();

        List<Gem> installedGems = gemManager.getInstalledGems();
        List<Gem> availableGems = gemManager.getRemoteGems();
        List<Gem> updatedGems = new ArrayList<Gem>();
        List<Gem> newGems = new ArrayList<Gem>();
        
        Map<String, Gem> nameMap = new HashMap<String, Gem>();
        for (Gem gem : installedGems) {
            nameMap.put(gem.getName(), gem);
        }
        Set<String> installedNames = nameMap.keySet();

        for (Gem gem : availableGems) {
            if (installedNames.contains(gem.getName())) {
                String latestAvailable = gem.getLatestAvailable();
                Gem installedGem = nameMap.get(gem.getName());
                String latestInstalled = installedGem.getLatestInstalled();
                if (Util.compareVersions(latestAvailable, latestInstalled) > 0) {
                    Gem update = new Gem(gem.getName(),
                            installedGem.getInstalledVersionsAsString(),
                            latestAvailable);
                    update.setDescription(installedGem.getDescription());
                    updatedGems.add(update);
                }
            } else {
                newGems.add(gem);
            }
        }
        updateList(TabIndex.INSTALLED, installedGems);
        updateList(TabIndex.NEW, newGems);
        updateList(TabIndex.UPDATED, updatedGems);
    }
    
    private void hideProgressBars() {
        updatedProgress.setVisible(false);
        updatedProgressLabel.setVisible(false);
        newProgress.setVisible(false);
        newProgressLabel.setVisible(false);
        installedProgress.setVisible(false);
        installedProgressLabel.setVisible(false);
    }

    private void updateList(final TabIndex tab, final List<Gem> gems) {
        assert EventQueue.isDispatchThread();

        JList list;

        switch (tab) {
            case NEW:
                list = newList;
                break;
            case UPDATED:
                list = updatedList;
                break;
            case INSTALLED:
                list = installedList;
                break;
            default:
                throw new IllegalArgumentException("Unknonw tab: " + tab); // NOI18N
        }

        if (gems == null) {
            // attempting to filter before the list has been fetched - ignore
            return;
        }

        GemListModel model = new GemListModel(gems, getGemFilter());
        list.clearSelection();
        list.setModel(model);
        list.invalidate();
        list.repaint();

        setTabTitle(tab, model);
        setEnabled(tab, true);
    }
    
    private void setTabTitle(TabIndex tab, GemListModel model) {
        String tabTitle = gemsTab.getTitleAt(tab.ordinal());
        String originalTabTitle = tabTitle;
        int index = tabTitle.lastIndexOf('(');
        if (index != -1) {
            tabTitle = tabTitle.substring(0, index);
        }
        int allSize = model.getAllSize();
        int nOfGems = model.getSize();
        String count;
        if (nOfGems < allSize) {
            count =  nOfGems + "/" + allSize; // NOI18N
        } else {
            count = Integer.toString(allSize);
        }
        tabTitle = tabTitle + "(" + count + ")"; // NOI18N
        if (!tabTitle.equals(originalTabTitle)) {
            gemsTab.setTitleAt(tab.ordinal(), tabTitle);
        }
    }

    /** Return whether any gems were modified - roots should be recomputed after panel is taken down */
    public boolean isModified() {
        return gemsModified;
    }

    private static void showProgressBar(JList list, JTextPane description, JProgressBar progress, JLabel progressLabel) {
        assert EventQueue.isDispatchThread();

        if (list.getSelectedIndex() != -1) {
            updateGemDescription(description, null);
        }
        progress.setVisible(true);
        progressLabel.setVisible(true);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gemsTab = new javax.swing.JTabbedPane();
        updatedPanel = new javax.swing.JPanel();
        searchUpdatedText = new javax.swing.JTextField();
        searchUpdatedLbl = new javax.swing.JLabel();
        reloadReposButton = new javax.swing.JButton();
        updatedSP = new javax.swing.JScrollPane();
        updatedList = new javax.swing.JList();
        updateButton = new javax.swing.JButton();
        updateAllButton = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        updatedDesc = new javax.swing.JTextPane();
        updatedProgress = new javax.swing.JProgressBar();
        updatedProgressLabel = new javax.swing.JLabel();
        installedPanel = new javax.swing.JPanel();
        searchInstText = new javax.swing.JTextField();
        searchInstLbl = new javax.swing.JLabel();
        reloadInstalledButton = new javax.swing.JButton();
        uninstallButton = new javax.swing.JButton();
        installedSP = new javax.swing.JScrollPane();
        installedList = new javax.swing.JList();
        jScrollPane5 = new javax.swing.JScrollPane();
        installedDesc = new javax.swing.JTextPane();
        installedProgress = new javax.swing.JProgressBar();
        installedProgressLabel = new javax.swing.JLabel();
        newPanel = new javax.swing.JPanel();
        searchNewText = new javax.swing.JTextField();
        searchNewLbl = new javax.swing.JLabel();
        reloadNewButton = new javax.swing.JButton();
        installButton = new javax.swing.JButton();
        newSP = new javax.swing.JScrollPane();
        newList = new javax.swing.JList();
        jScrollPane4 = new javax.swing.JScrollPane();
        newDesc = new javax.swing.JTextPane();
        newProgress = new javax.swing.JProgressBar();
        newProgressLabel = new javax.swing.JLabel();
        installLocalButton = new javax.swing.JButton();
        settingsPanel = new javax.swing.JPanel();
        proxyButton = new javax.swing.JButton();
        allVersionsCheckbox = new javax.swing.JCheckBox();
        descriptionCheckbox = new javax.swing.JCheckBox();
        rubyPlatformLabel = new javax.swing.JLabel();
        platforms = org.netbeans.modules.ruby.platform.PlatformComponentFactory.getRubyPlatformsComboxBox();
        manageButton = new javax.swing.JButton();
        gemHome = new javax.swing.JLabel();
        gemHomeValue = new javax.swing.JTextField();
        browseGemHome = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        searchUpdatedText.setColumns(14);
        searchUpdatedText.addActionListener(formListener);

        searchUpdatedLbl.setLabelFor(searchUpdatedText);
        org.openide.awt.Mnemonics.setLocalizedText(searchUpdatedLbl, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.searchUpdatedLbl.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(reloadReposButton, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.reloadReposButton.text")); // NOI18N
        reloadReposButton.addActionListener(formListener);

        updatedSP.setViewportView(updatedList);
        updatedList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updatedList.AccessibleContext.accessibleName")); // NOI18N
        updatedList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updatedList.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(updateButton, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updateButton.text")); // NOI18N
        updateButton.setEnabled(false);
        updateButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(updateAllButton, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updateAllButton.text")); // NOI18N
        updateAllButton.addActionListener(formListener);

        updatedDesc.setEditable(false);
        jScrollPane6.setViewportView(updatedDesc);
        updatedDesc.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updatedDesc.AccessibleContext.accessibleName")); // NOI18N
        updatedDesc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updatedDesc.AccessibleContext.accessibleDescription")); // NOI18N

        updatedProgress.setIndeterminate(true);

        org.openide.awt.Mnemonics.setLocalizedText(updatedProgressLabel, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updatedProgressLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout updatedPanelLayout = new org.jdesktop.layout.GroupLayout(updatedPanel);
        updatedPanel.setLayout(updatedPanelLayout);
        updatedPanelLayout.setHorizontalGroup(
            updatedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(updatedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(updatedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, updatedPanelLayout.createSequentialGroup()
                        .add(reloadReposButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 436, Short.MAX_VALUE)
                        .add(searchUpdatedLbl)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchUpdatedText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 156, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(updatedPanelLayout.createSequentialGroup()
                        .add(updateButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(updateAllButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 362, Short.MAX_VALUE)
                        .add(updatedProgressLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(updatedProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, updatedPanelLayout.createSequentialGroup()
                        .add(updatedSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(jScrollPane6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 283, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        updatedPanelLayout.setVerticalGroup(
            updatedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(updatedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(updatedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(searchUpdatedLbl)
                    .add(searchUpdatedText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(reloadReposButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(updatedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                    .add(updatedSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(updatedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(updatedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(updateButton)
                        .add(updateAllButton))
                    .add(updatedProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(updatedProgressLabel))
                .addContainerGap())
        );

        searchUpdatedText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.searchUpdatedText.AccessibleContext.accessibleDescription")); // NOI18N
        searchUpdatedLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.searchUpdatedLbl.AccessibleContext.accessibleDescription")); // NOI18N
        reloadReposButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.reloadReposButton.AccessibleContext.accessibleDescription")); // NOI18N
        updatedSP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.jScrollPane3.AccessibleContext.accessibleDescription")); // NOI18N
        updateButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updateButton.AccessibleContext.accessibleDescription")); // NOI18N
        updateAllButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updateAllButton.AccessibleContext.accessibleDescription")); // NOI18N
        jScrollPane6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.jScrollPane6.AccessibleContext.accessibleDescription")); // NOI18N
        updatedProgress.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updatedProgress.AccessibleContext.accessibleDescription")); // NOI18N
        updatedProgressLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updatedProgressLabel.AccessibleContext.accessibleDescription")); // NOI18N

        gemsTab.addTab(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.updatedPanel.TabConstraints.tabTitle"), updatedPanel); // NOI18N

        searchInstText.setColumns(14);
        searchInstText.addActionListener(formListener);

        searchInstLbl.setLabelFor(searchInstText);
        org.openide.awt.Mnemonics.setLocalizedText(searchInstLbl, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.searchInstLbl.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(reloadInstalledButton, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.reloadInstalledButton.text")); // NOI18N
        reloadInstalledButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(uninstallButton, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.uninstallButton.text")); // NOI18N
        uninstallButton.setEnabled(false);
        uninstallButton.addActionListener(formListener);

        installedSP.setViewportView(installedList);
        installedList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installedList.AccessibleContext.accessibleName")); // NOI18N
        installedList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installedList.AccessibleContext.accessibleDescription")); // NOI18N

        installedDesc.setEditable(false);
        jScrollPane5.setViewportView(installedDesc);
        installedDesc.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installedDesc.AccessibleContext.accessibleName")); // NOI18N
        installedDesc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installedDesc.AccessibleContext.accessibleDescription")); // NOI18N

        installedProgress.setIndeterminate(true);

        org.openide.awt.Mnemonics.setLocalizedText(installedProgressLabel, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installedProgressLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout installedPanelLayout = new org.jdesktop.layout.GroupLayout(installedPanel);
        installedPanel.setLayout(installedPanelLayout);
        installedPanelLayout.setHorizontalGroup(
            installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(installedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, installedPanelLayout.createSequentialGroup()
                        .add(reloadInstalledButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 436, Short.MAX_VALUE)
                        .add(searchInstLbl)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchInstText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 156, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(installedPanelLayout.createSequentialGroup()
                        .add(uninstallButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 455, Short.MAX_VALUE)
                        .add(installedProgressLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(installedProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, installedPanelLayout.createSequentialGroup()
                        .add(installedSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 283, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        installedPanelLayout.setVerticalGroup(
            installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(installedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(searchInstLbl)
                    .add(reloadInstalledButton)
                    .add(searchInstText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(installedSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                    .add(jScrollPane5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(uninstallButton)
                    .add(installedProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(installedProgressLabel))
                .addContainerGap())
        );

        searchInstText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.instSearchText.AccessibleContext.accessibleDescription")); // NOI18N
        searchInstLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.instSearchLbl.AccessibleContext.accessibleDescription")); // NOI18N
        reloadInstalledButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.reloadInstalledButton.AccessibleContext.accessibleDescription")); // NOI18N
        uninstallButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.uninstallButton.AccessibleContext.accessibleDescription")); // NOI18N
        installedSP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.jScrollPane1.AccessibleContext.accessibleDescription")); // NOI18N
        jScrollPane5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.jScrollPane5.AccessibleContext.accessibleDescription")); // NOI18N
        installedProgress.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installedProgress.AccessibleContext.accessibleDescription")); // NOI18N
        installedProgressLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installedProgressLabel.AccessibleContext.accessibleDescription")); // NOI18N

        gemsTab.addTab(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installedPanel.TabConstraints.tabTitle"), installedPanel); // NOI18N

        searchNewText.setColumns(14);
        searchNewText.addActionListener(formListener);

        searchNewLbl.setLabelFor(searchNewText);
        org.openide.awt.Mnemonics.setLocalizedText(searchNewLbl, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.searchNewLbl.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(reloadNewButton, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.reloadNewButton.text")); // NOI18N
        reloadNewButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(installButton, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installButton.text")); // NOI18N
        installButton.setEnabled(false);
        installButton.addActionListener(formListener);

        newSP.setViewportView(newList);
        newList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.newList.AccessibleContext.accessibleName")); // NOI18N
        newList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.newList.AccessibleContext.accessibleDescription")); // NOI18N

        newDesc.setEditable(false);
        jScrollPane4.setViewportView(newDesc);
        newDesc.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.newDesc.AccessibleContext.accessibleName")); // NOI18N
        newDesc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.newDesc.AccessibleContext.accessibleDescription")); // NOI18N

        newProgress.setIndeterminate(true);

        org.openide.awt.Mnemonics.setLocalizedText(newProgressLabel, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.newProgressLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(installLocalButton, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installLocalButton.text")); // NOI18N
        installLocalButton.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout newPanelLayout = new org.jdesktop.layout.GroupLayout(newPanel);
        newPanel.setLayout(newPanelLayout);
        newPanelLayout.setHorizontalGroup(
            newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(newPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, newPanelLayout.createSequentialGroup()
                        .add(reloadNewButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 436, Short.MAX_VALUE)
                        .add(searchNewLbl)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchNewText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 156, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(newPanelLayout.createSequentialGroup()
                        .add(installButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(installLocalButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 348, Short.MAX_VALUE)
                        .add(newProgressLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(newProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, newPanelLayout.createSequentialGroup()
                        .add(newSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 283, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        newPanelLayout.setVerticalGroup(
            newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(newPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(searchNewLbl)
                    .add(reloadNewButton)
                    .add(searchNewText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(newSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                    .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(newPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(installButton)
                        .add(installLocalButton))
                    .add(newProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(newProgressLabel))
                .addContainerGap())
        );

        searchNewText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.searchNewText.AccessibleContext.accessibleDescription")); // NOI18N
        searchNewLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.searchNewLbl.AccessibleContext.accessibleDescription")); // NOI18N
        reloadNewButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.reloadNewButton.AccessibleContext.accessibleDescription")); // NOI18N
        installButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.installButton.AccessibleContext.accessibleDescription")); // NOI18N
        newSP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.jScrollPane2.AccessibleContext.accessibleDescription")); // NOI18N
        jScrollPane4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.jScrollPane4.AccessibleContext.accessibleDescription")); // NOI18N
        newProgress.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.newProgress.AccessibleContext.accessibleDescription")); // NOI18N
        newProgressLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.newProgressLabel.AccessibleContext.accessibleDescription")); // NOI18N

        gemsTab.addTab(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.newPanel.TabConstraints.tabTitle"), newPanel); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(proxyButton, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.proxyButton.text")); // NOI18N
        proxyButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(allVersionsCheckbox, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.allVersionsCheckbox.text")); // NOI18N
        allVersionsCheckbox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(descriptionCheckbox, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.descriptionCheckbox.text")); // NOI18N
        descriptionCheckbox.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout settingsPanelLayout = new org.jdesktop.layout.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(descriptionCheckbox)
                    .add(proxyButton)
                    .add(allVersionsCheckbox))
                .addContainerGap(463, Short.MAX_VALUE))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(proxyButton)
                .add(18, 18, 18)
                .add(allVersionsCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(descriptionCheckbox)
                .addContainerGap(237, Short.MAX_VALUE))
        );

        proxyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.proxyButton.AccessibleContext.accessibleDescription")); // NOI18N

        gemsTab.addTab(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.settingsPanel.TabConstraints.tabTitle"), settingsPanel); // NOI18N

        rubyPlatformLabel.setLabelFor(platforms);
        org.openide.awt.Mnemonics.setLocalizedText(rubyPlatformLabel, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.rubyPlatformLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(manageButton, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.manageButton.text")); // NOI18N
        manageButton.addActionListener(formListener);

        gemHome.setLabelFor(gemHomeValue);
        org.openide.awt.Mnemonics.setLocalizedText(gemHome, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.gemHome.text")); // NOI18N

        gemHomeValue.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(browseGemHome, org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.browseGemHome.text")); // NOI18N
        browseGemHome.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(gemsTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 791, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(gemHome, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(rubyPlatformLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, gemHomeValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, platforms, 0, 591, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, manageButton)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, browseGemHome, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {browseGemHome, manageButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {gemHome, rubyPlatformLabel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rubyPlatformLabel)
                    .add(manageButton)
                    .add(platforms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(gemHome)
                    .add(browseGemHome)
                    .add(gemHomeValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(gemsTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                .addContainerGap())
        );

        gemsTab.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.gemsTab.AccessibleContext.accessibleName")); // NOI18N
        gemsTab.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.gemsTab.AccessibleContext.accessibleDescription")); // NOI18N
        platforms.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.platforms.AccessibleContext.accessibleName")); // NOI18N
        platforms.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.platforms.AccessibleContext.accessibleDescription")); // NOI18N
        manageButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.manageButton.AccessibleContext.accessibleDescription")); // NOI18N
        gemHomeValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.gemHomeValue.AccessibleContext.accessibleName")); // NOI18N
        gemHomeValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.gemHomeValue.AccessibleContext.accessibleDescription")); // NOI18N
        browseGemHome.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.browseGemHome.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GemPanel.class, "GemPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == searchUpdatedText) {
                GemPanel.this.searchUpdatedTextActionPerformed(evt);
            }
            else if (evt.getSource() == reloadReposButton) {
                GemPanel.this.reloadReposButtonActionPerformed(evt);
            }
            else if (evt.getSource() == updateButton) {
                GemPanel.this.updateButtonActionPerformed(evt);
            }
            else if (evt.getSource() == updateAllButton) {
                GemPanel.this.updateAllButtonActionPerformed(evt);
            }
            else if (evt.getSource() == searchInstText) {
                GemPanel.this.searchInstTextActionPerformed(evt);
            }
            else if (evt.getSource() == reloadInstalledButton) {
                GemPanel.this.reloadInstalledButtonActionPerformed(evt);
            }
            else if (evt.getSource() == uninstallButton) {
                GemPanel.this.uninstallButtonActionPerformed(evt);
            }
            else if (evt.getSource() == searchNewText) {
                GemPanel.this.searchNewTextActionPerformed(evt);
            }
            else if (evt.getSource() == reloadNewButton) {
                GemPanel.this.reloadNewButtonActionPerformed(evt);
            }
            else if (evt.getSource() == installButton) {
                GemPanel.this.installButtonActionPerformed(evt);
            }
            else if (evt.getSource() == installLocalButton) {
                GemPanel.this.installLocalButtonActionPerformed(evt);
            }
            else if (evt.getSource() == proxyButton) {
                GemPanel.this.proxyButtonActionPerformed(evt);
            }
            else if (evt.getSource() == allVersionsCheckbox) {
                GemPanel.this.allVersionsCheckboxActionPerformed(evt);
            }
            else if (evt.getSource() == descriptionCheckbox) {
                GemPanel.this.descriptionCheckboxActionPerformed(evt);
            }
            else if (evt.getSource() == manageButton) {
                GemPanel.this.manageButtonActionPerformed(evt);
            }
            else if (evt.getSource() == browseGemHome) {
                GemPanel.this.browseGemHomeActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void reloadNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadNewButtonActionPerformed
        getGemManager().resetRemote();
        refreshGemLists();
    }//GEN-LAST:event_reloadNewButtonActionPerformed

    private void proxyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proxyButtonActionPerformed
        OptionsDisplayer.getDefault().open("General"); // NOI18N
    }//GEN-LAST:event_proxyButtonActionPerformed

    private void searchNewTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchNewTextActionPerformed
        applyFilter(searchNewText.getText());
    }//GEN-LAST:event_searchNewTextActionPerformed

    private void searchUpdatedTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchUpdatedTextActionPerformed
        applyFilter(searchUpdatedText.getText());
    }//GEN-LAST:event_searchUpdatedTextActionPerformed

    private void searchInstTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchInstTextActionPerformed
        applyFilter(searchInstText.getText());
    }//GEN-LAST:event_searchInstTextActionPerformed

    private void applyFilter(final String filter) {
        // keep search filter fields in sync
        searchNewText.setText(filter);
        searchUpdatedText.setText(filter);
        searchInstText.setText(filter);
        
        applyFilter(TabIndex.INSTALLED, installedList);
        applyFilter(TabIndex.UPDATED, updatedList);
        applyFilter(TabIndex.NEW, newList);
    }

    private void applyFilter(final TabIndex tab, final JList list) {
        GemListModel gemModel = (GemListModel) list.getModel();
        gemModel.applyFilter(getGemFilter());
        setTabTitle(tab, gemModel);
    }

    private void reloadReposButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadReposButtonActionPerformed
        getGemManager().reset();
        refreshGemLists();
    }//GEN-LAST:event_reloadReposButtonActionPerformed

    private void installButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installButtonActionPerformed
        assert EventQueue.isDispatchThread();

        int[] indices = newList.getSelectedIndices();
        List<Gem> gems = new ArrayList<Gem>();
        for (int index : indices) {
            Object o = newList.getModel().getElementAt(index);
            if (o instanceof Gem) { // Could be error or please wait string
                Gem gem = (Gem)o;
                gems.add(gem);
            }
        }

        if (!gems.isEmpty()) {
            for (Gem chosen : gems) {
                // Get some information about the chosen gem
                InstallationSettingsPanel panel = new InstallationSettingsPanel(chosen);
                panel.getAccessibleContext().setAccessibleDescription(
                        getMessage("InstallationSettingsPanel.AccessibleContext.accessibleDescription"));

                DialogDescriptor dd = new DialogDescriptor(panel, getMessage("ChooseGemSettings"));
                dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
                dd.setModal(true);
                dd.setHelpCtx(new HelpCtx(GemPanel.class));
                Object result = DialogDisplayer.getDefault().notify(dd);
                if (result.equals(NotifyDescriptor.OK_OPTION)) {
                    Gem gem = new Gem(panel.getGemName(), null, null);
                    GemListRefresher completionTask = new GemListRefresher();
                    getGemManager().install(new Gem[] { gem }, this, false, false, panel.getVersion(),
                            panel.getIncludeDepencies(), true, completionTask);                                             
                }
            }
        }
    }//GEN-LAST:event_installButtonActionPerformed

    private void updateAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateAllButtonActionPerformed
        Runnable completionTask = new GemListRefresher();
        getGemManager().update(null, this, false, false, false, true, completionTask);
    }//GEN-LAST:event_updateAllButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        assert EventQueue.isDispatchThread();

        int[] indices = updatedList.getSelectedIndices();
        List<Gem> gems = new ArrayList<Gem>();
        if (indices != null) {
            for (int index : indices) {
                assert index >= 0;
                Object o = updatedList.getModel().getElementAt(index);
                if (o instanceof Gem) { // Could be error or please wait string
                    Gem gem = (Gem)o;
                    gems.add(gem);
                }
            }
        }
        if (!gems.isEmpty()) {
            Runnable completionTask = new GemListRefresher();
            getGemManager().update(gems.toArray(new Gem[gems.size()]), this, false, false, false, true, completionTask);                                            
        }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void uninstallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uninstallButtonActionPerformed
        assert EventQueue.isDispatchThread();

        int[] indices = installedList.getSelectedIndices();
        List<Gem> gems = new ArrayList<Gem>();
        if (indices != null) {
            for (int index : indices) {
                assert index >= 0;
                Object o = installedList.getModel().getElementAt(index);
                if (o instanceof Gem) { // Could be error or please wait string
                    Gem gem = (Gem)o;
                    gems.add(gem);
                }
            }
        }
        if (!gems.isEmpty()) {
            Runnable completionTask = new GemListRefresher();
            getGemManager().uninstall(gems.toArray(new Gem[gems.size()]), this, true, completionTask);                                               
        }
    }//GEN-LAST:event_uninstallButtonActionPerformed

    private void reloadInstalledButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadInstalledButtonActionPerformed
        getGemManager().resetLocal();
        refreshGemLists();
    }//GEN-LAST:event_reloadInstalledButtonActionPerformed

    private void manageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageButtonActionPerformed
        RubyPlatformCustomizer.manage(platforms);
    }//GEN-LAST:event_manageButtonActionPerformed

    private void browseGemHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseGemHomeActionPerformed
        boolean changed = chooseAndSetGemHome(this, getSelectedPlatform());
        if (changed) {
            platformChanged();
        }
    }//GEN-LAST:event_browseGemHomeActionPerformed

    private void installLocalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installLocalButtonActionPerformed
        JFileChooser chooser = new JFileChooser(RubyPreferences.getPreferences().get(LAST_GEM_DIRECTORY, ""));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || (f.isFile() && f.getName().toLowerCase(Locale.US).endsWith(".gem")); // NOI18N
            }
            public String getDescription() {
                return getMessage("GemPanel.rubygems.files.filter");
            }
        });
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File gem = FileUtil.normalizeFile(chooser.getSelectedFile());
            RubyPreferences.getPreferences().put(LAST_GEM_DIRECTORY, gem.getParentFile().getAbsolutePath());
            GemListRefresher completionTask = new GemListRefresher();
            getGemManager().installLocal(gem, this, false, false, true, completionTask);
        }
    }//GEN-LAST:event_installLocalButtonActionPerformed

    private void allVersionsCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allVersionsCheckboxActionPerformed
        RubyPreferences.setFetchAllVersions(allVersionsCheckbox.isSelected());
    }//GEN-LAST:event_allVersionsCheckboxActionPerformed

    private void descriptionCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descriptionCheckboxActionPerformed
        RubyPreferences.setFetchGemDescriptions(descriptionCheckbox.isSelected());
    }//GEN-LAST:event_descriptionCheckboxActionPerformed

    public static File chooseGemRepository(final Component parent) {
        JFileChooser chooser = new JFileChooser();
        //        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(parent);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File gemHomeF = FileUtil.normalizeFile(chooser.getSelectedFile());
            if (GemManager.isValidGemHome(gemHomeF)) {
                return gemHomeF;
            }
            if (!gemHomeF.exists() || (gemHomeF.isDirectory() && gemHomeF.list().length == 0)) {
                if (Util.confirmLocalized(GemPanel.class, "GemPanel.empty.create.gemrepo", gemHomeF.getAbsolutePath())) { // NOI18N
                    try {
                        GemManager.initializeRepository(gemHomeF);
                        return gemHomeF;
                    } catch (IOException ioe) {
                        LOGGER.log(Level.SEVERE, ioe.getLocalizedMessage(), ioe);
                    }
                }
            } else {
                Util.notifyLocalized(GemPanel.class, "GemPanel.invalid.gemHome", gemHomeF.getAbsolutePath()); // NOI18N
            }
        }
        return null;
    }
    
    public static boolean chooseAndSetGemHome(final Component parent, final RubyPlatform platform) {
        if (platform == null) {
            return false;
        }
        assert platform.hasRubyGemsInstalled() : "has RubyGems installed";
        File gemHomeF = chooseGemRepository(parent);
        if (gemHomeF != null) {
            platform.setGemHome(gemHomeF);
            return true;
        }
        return false;
    }
    
    private void showGemErrors(final List<String> errors) {
        assert EventQueue.isDispatchThread();
        getGemManager().reset();
        StringBuilder sb = new StringBuilder();
        for (String error : errors) {
            sb.append(error).append('\n');
        }
        Util.notifyLocalized(GemPanel.class, "GemPanel.GemsFetchingFailed", // NOI18N
            NotifyDescriptor.ERROR_MESSAGE, sb.toString());
    }

    private void refreshGemLists() {
        assert EventQueue.isDispatchThread();
        setEnabledGUI(false);
        // harmless, but could use gemsTab.getSelectedIndex()
        showProgressBar(newList, newDesc, newProgress, newProgressLabel);
        showProgressBar(updatedList, updatedDesc, updatedProgress, updatedProgressLabel);
        showProgressBar(installedList, installedDesc, installedProgress, installedProgressLabel);
        GemListModel emptyModel = new GemListModel(Collections.<Gem>emptyList(), null);
        newList.setModel(emptyModel);
        updatedList.setModel(emptyModel);
        installedList.setModel(emptyModel);
        setTabTitle(TabIndex.INSTALLED, emptyModel);
        setTabTitle(TabIndex.NEW, emptyModel);
        setTabTitle(TabIndex.UPDATED, emptyModel);
        final GemManager gemManager = getGemManager();
        Runnable updateTask = new Runnable() {
            public void run() {
                LOGGER.finer("Update of " + gemManager + " scheduled");
                assert !EventQueue.isDispatchThread();

                final List<String> errors = new ArrayList<String>();
                gemManager.reloadIfNeeded(errors);

                // Update UI
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        LOGGER.finer("Update of " + gemManager + " finished");
                        if (closed) {
                            return;
                        }
                        boolean platformHasChanged = !gemManager.equals(getGemManager());
                        if (!errors.isEmpty()) {
                            showGemErrors(errors);
                            if (!platformHasChanged) {
                                hideProgressBars();
                                // enable Reload buttons in error state, so user
                                // might trigger reload after attempt to fix the
                                // problem
                                enableReloadGUI();
                            }
                            return;
                        }
                        if (!platformHasChanged) {
                            notifyGemsUpdated();
                        } else { // platform has changed, ignore UI update
                            LOGGER.finer("Gem Manager has changed from " + gemManager
                                    + " to " + getGemManager() + ". Ignoring update."); // NOI18N
                        }
                    }
                });
            }
        };
        LOGGER.finer("Submitting refreshing of gems for: " + gemManager);
        updateTasksQueue.post(updateTask);
    }

    private String getGemFilter() {
        assert EventQueue.isDispatchThread();
        // filter field are kept in sync, does not matter which one is used
        String filter = searchInstText.getText().trim();
        return filter.length() == 0 ? null : filter;
    }

    private RubyPlatform getSelectedPlatform() {
        if (!EventQueue.isDispatchThread()) {
            Exceptions.printStackTrace(new AssertionError("getSelectedPlatform() must be called from EDT"));
        }
        return PlatformComponentFactory.getPlatform(platforms);
    }

    private GemManager getGemManager() {
        if (!EventQueue.isDispatchThread()) {
            Exceptions.printStackTrace(new AssertionError("getGemManager() must be called from EDT"));
        }
        RubyPlatform platform = getSelectedPlatform();
        return platform == null ? null : platform.getGemManager();
    }

    private static class MyListSelectionListener implements ListSelectionListener {
        
        private final JButton button;
        private final JTextPane pane;
        private final JList list;

        private MyListSelectionListener(JList list, JTextPane pane, JButton button) {
            this.list = list;
            this.pane = pane;
            this.button = button;
        }
        
        public void valueChanged(ListSelectionEvent ev) {
            if (ev.getValueIsAdjusting()) {
                return;
            }
            Object o = list.getSelectedValue();
            if (o instanceof Gem) { // Could be "Please Wait..." String
                button.setEnabled(true);
                if (pane != null) {
                    updateGemDescription(pane, (Gem) o);
                }
                return;
            } else {
                if (pane != null) {
                    pane.setText("");
                }
                button.setEnabled(false);
            }
        }
    }

    private class GemListRefresher implements Runnable {
        public void run() {
            gemsModified = true;
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(this);
            } else {
                refreshGemLists();
            }
        }
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(GemPanel.class, key);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allVersionsCheckbox;
    private javax.swing.JButton browseGemHome;
    private javax.swing.JCheckBox descriptionCheckbox;
    private javax.swing.JLabel gemHome;
    private javax.swing.JTextField gemHomeValue;
    private javax.swing.JTabbedPane gemsTab;
    private javax.swing.JButton installButton;
    private javax.swing.JButton installLocalButton;
    private javax.swing.JTextPane installedDesc;
    private javax.swing.JList installedList;
    private javax.swing.JPanel installedPanel;
    private javax.swing.JProgressBar installedProgress;
    private javax.swing.JLabel installedProgressLabel;
    private javax.swing.JScrollPane installedSP;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JButton manageButton;
    private javax.swing.JTextPane newDesc;
    private javax.swing.JList newList;
    private javax.swing.JPanel newPanel;
    private javax.swing.JProgressBar newProgress;
    private javax.swing.JLabel newProgressLabel;
    private javax.swing.JScrollPane newSP;
    private javax.swing.JComboBox platforms;
    private javax.swing.JButton proxyButton;
    private javax.swing.JButton reloadInstalledButton;
    private javax.swing.JButton reloadNewButton;
    private javax.swing.JButton reloadReposButton;
    private javax.swing.JLabel rubyPlatformLabel;
    private javax.swing.JLabel searchInstLbl;
    private javax.swing.JTextField searchInstText;
    private javax.swing.JLabel searchNewLbl;
    private javax.swing.JTextField searchNewText;
    private javax.swing.JLabel searchUpdatedLbl;
    private javax.swing.JTextField searchUpdatedText;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JButton uninstallButton;
    private javax.swing.JButton updateAllButton;
    private javax.swing.JButton updateButton;
    private javax.swing.JTextPane updatedDesc;
    private javax.swing.JList updatedList;
    private javax.swing.JPanel updatedPanel;
    private javax.swing.JProgressBar updatedProgress;
    private javax.swing.JLabel updatedProgressLabel;
    private javax.swing.JScrollPane updatedSP;
    // End of variables declaration//GEN-END:variables
    
}
