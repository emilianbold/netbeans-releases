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
package org.netbeans.modules.web.webkit.tooling.networkmonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage;
import org.netbeans.modules.web.webkit.debugging.api.network.Network;
import org.netbeans.modules.web.webkit.tooling.console.BrowserConsoleLogger;
import static org.netbeans.modules.web.webkit.tooling.console.BrowserConsoleLogger.getProjectPath;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.RetainLocation;

@TopComponent.Description(
        preferredID = "NetworkMonitorTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@RetainLocation(value = "output")
@Messages({
    "CTL_NetworkMonitorTopComponent=Network Monitor",
    "HINT_NetworkMonitorTopComponent=This is a Network Monitor window"
})
public final class NetworkMonitorTopComponent extends TopComponent 
    implements ListDataListener, ChangeListener, PropertyChangeListener {

    private Model model;
    private static final RequestProcessor RP = new RequestProcessor(NetworkMonitorTopComponent.class.getName(), 5);
    private final NetworkMonitor parent;
    private final InputOutput io;
    private MyProvider ioProvider;
    private UIUpdater updater;

    NetworkMonitorTopComponent(NetworkMonitor parent, Model m) {
        initComponents();
        jResponse.setEditorKit(CloneableEditorSupport.getEditorKit("text/plain"));
        setName(Bundle.CTL_NetworkMonitorTopComponent());
        setToolTipText(Bundle.HINT_NetworkMonitorTopComponent());
        updater = new UIUpdater(this);
        setModel(m);
        this.parent = parent;
        jRequestsList.setCellRenderer(new ListRendererImpl());
        jSplitPane.setDividerLocation(NbPreferences.forModule(NetworkMonitorTopComponent.class).getInt("separator", 200));
        selectedItemChanged();
        updateVisibility();
        ioProvider = new MyProvider(jIOContainerPlaceholder);
        IOContainer container = IOContainer.create(ioProvider);
        io = IOProvider.getDefault().getIO("callstack", new Action[0], container);
        OpenProjects.getDefault().addPropertyChangeListener(this);
    }

    private static class UIUpdater implements ActionListener {

        private Timer t;
        private NetworkMonitorTopComponent comp;
        private ModelItem modelItem;

        public UIUpdater(NetworkMonitorTopComponent comp) {
            this.comp = comp;
            t = new Timer(200, this);
            t.setRepeats(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            comp._refreshDetailsView(modelItem);
        }

        public synchronized void showItem(ModelItem mi) {
            t.stop();
            modelItem = mi;
            t.start();
        }

    }
    
    void setModel(Model model) {
        this.model = model;
        ListModel lm = jRequestsList.getModel();
        if (lm != null) {
            lm.removeListDataListener(this);
        }
        jRequestsList.setModel(model);
        model.addListDataListener(this);
        selectedItemChanged();
        updateVisibility();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jRequestsList = new javax.swing.JList();
        jClear = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jHeadersPanel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jHeaders = new javax.swing.JTextPane();
        jRequestPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jRequest = new javax.swing.JEditorPane();
        jRawResponseRequest = new javax.swing.JCheckBox();
        jResponsePanel = new javax.swing.JPanel();
        jRawResponseResponse = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        jResponse = new javax.swing.JEditorPane();
        jFramesPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jFrames = new javax.swing.JTextPane();
        jRawResponseFrames = new javax.swing.JCheckBox();
        jCallStackPanel = new javax.swing.JPanel();
        jIOContainerPlaceholder = new javax.swing.JPanel();
        jNoData = new javax.swing.JLabel();

        jRequestsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jRequestsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jRequestsListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jRequestsList);

        jClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/webkit/tooling/networkmonitor/delete.gif"))); // NOI18N
        jClear.setToolTipText(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jClear.tooltip")); // NOI18N
        jClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jClearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jClear)
                .addGap(0, 21, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jClear)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE))
        );

        jSplitPane.setLeftComponent(jPanel3);

        jHeadersPanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jHeadersPanel.TabConstraints.tabTitle")); // NOI18N

        jHeaders.setEditable(false);
        jScrollPane5.setViewportView(jHeaders);

        javax.swing.GroupLayout jHeadersPanelLayout = new javax.swing.GroupLayout(jHeadersPanel);
        jHeadersPanel.setLayout(jHeadersPanelLayout);
        jHeadersPanelLayout.setHorizontalGroup(
            jHeadersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
        );
        jHeadersPanelLayout.setVerticalGroup(
            jHeadersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jHeadersPanel.TabConstraints.tabTitle"), jHeadersPanel); // NOI18N

        jRequestPanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.Request Data.TabConstraints.tabTitle")); // NOI18N

        jRequest.setEditable(false);
        jScrollPane2.setViewportView(jRequest);

        org.openide.awt.Mnemonics.setLocalizedText(jRawResponseRequest, org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jRawResponseRequest.text")); // NOI18N
        jRawResponseRequest.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRawResponseRequestItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jRequestPanelLayout = new javax.swing.GroupLayout(jRequestPanel);
        jRequestPanel.setLayout(jRequestPanelLayout);
        jRequestPanelLayout.setHorizontalGroup(
            jRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addGroup(jRequestPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jRawResponseRequest))
        );
        jRequestPanelLayout.setVerticalGroup(
            jRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jRequestPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRawResponseRequest))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jRequestPanel.TabConstraints.tabTitle"), jRequestPanel); // NOI18N

        jResponsePanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jResponsePanel.TabConstraints.tabTitle")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jRawResponseResponse, org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jRawResponseResponse.text")); // NOI18N
        jRawResponseResponse.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRawResponseResponseItemStateChanged(evt);
            }
        });

        jResponse.setEditable(false);
        jScrollPane3.setViewportView(jResponse);

        javax.swing.GroupLayout jResponsePanelLayout = new javax.swing.GroupLayout(jResponsePanel);
        jResponsePanel.setLayout(jResponsePanelLayout);
        jResponsePanelLayout.setHorizontalGroup(
            jResponsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jResponsePanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jRawResponseResponse))
            .addComponent(jScrollPane3)
        );
        jResponsePanelLayout.setVerticalGroup(
            jResponsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResponsePanelLayout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRawResponseResponse))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jResponsePanel.TabConstraints.tabTitle"), jResponsePanel); // NOI18N

        jFramesPanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jFramesPanel.TabConstraints.tabTitle")); // NOI18N

        jScrollPane4.setViewportView(jFrames);

        org.openide.awt.Mnemonics.setLocalizedText(jRawResponseFrames, org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jRawResponseFrames.text")); // NOI18N
        jRawResponseFrames.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRawResponseFramesItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jFramesPanelLayout = new javax.swing.GroupLayout(jFramesPanel);
        jFramesPanel.setLayout(jFramesPanelLayout);
        jFramesPanelLayout.setHorizontalGroup(
            jFramesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFramesPanelLayout.createSequentialGroup()
                .addGap(0, 283, Short.MAX_VALUE)
                .addComponent(jRawResponseFrames))
            .addComponent(jScrollPane4)
        );
        jFramesPanelLayout.setVerticalGroup(
            jFramesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFramesPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRawResponseFrames))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jFramesPanel.TabConstraints.tabTitle"), jFramesPanel); // NOI18N

        jCallStackPanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jCallStackPanel.TabConstraints.tabTitle")); // NOI18N

        javax.swing.GroupLayout jIOContainerPlaceholderLayout = new javax.swing.GroupLayout(jIOContainerPlaceholder);
        jIOContainerPlaceholder.setLayout(jIOContainerPlaceholderLayout);
        jIOContainerPlaceholderLayout.setHorizontalGroup(
            jIOContainerPlaceholderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 416, Short.MAX_VALUE)
        );
        jIOContainerPlaceholderLayout.setVerticalGroup(
            jIOContainerPlaceholderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 214, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jCallStackPanelLayout = new javax.swing.GroupLayout(jCallStackPanel);
        jCallStackPanel.setLayout(jCallStackPanelLayout);
        jCallStackPanelLayout.setHorizontalGroup(
            jCallStackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jIOContainerPlaceholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jCallStackPanelLayout.setVerticalGroup(
            jCallStackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jIOContainerPlaceholder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jCallStackPanel.TabConstraints.tabTitle"), jCallStackPanel); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 424, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 183, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane.setRightComponent(jPanel1);

        org.openide.awt.Mnemonics.setLocalizedText(jNoData, org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jNoData.text")); // NOI18N
        jNoData.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jNoData.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 1, 1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane)
            .addComponent(jNoData, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jSplitPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jNoData))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jRequestsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jRequestsListValueChanged
        selectedItemChanged();
    }//GEN-LAST:event_jRequestsListValueChanged

    private void jRawResponseResponseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRawResponseResponseItemStateChanged
        ModelItem mi = lastSelectedItem;
        if (mi != null) {
            refreshDetailsView(mi);
        }
    }//GEN-LAST:event_jRawResponseResponseItemStateChanged

    private void jRawResponseRequestItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRawResponseRequestItemStateChanged
        ModelItem mi = lastSelectedItem;
        if (mi != null) {
            refreshDetailsView(mi);
        }
    }//GEN-LAST:event_jRawResponseRequestItemStateChanged

    private void jRawResponseFramesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRawResponseFramesItemStateChanged
        ModelItem mi = lastSelectedItem;
        if (mi != null) {
            refreshDetailsView(mi);
        }
    }//GEN-LAST:event_jRawResponseFramesItemStateChanged

    private void jClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jClearActionPerformed
        resetModel();
    }//GEN-LAST:event_jClearActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jCallStackPanel;
    private javax.swing.JButton jClear;
    private javax.swing.JTextPane jFrames;
    private javax.swing.JPanel jFramesPanel;
    private javax.swing.JTextPane jHeaders;
    private javax.swing.JPanel jHeadersPanel;
    private javax.swing.JPanel jIOContainerPlaceholder;
    private javax.swing.JLabel jNoData;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JCheckBox jRawResponseFrames;
    private javax.swing.JCheckBox jRawResponseRequest;
    private javax.swing.JCheckBox jRawResponseResponse;
    private javax.swing.JEditorPane jRequest;
    private javax.swing.JPanel jRequestPanel;
    private javax.swing.JList jRequestsList;
    private javax.swing.JEditorPane jResponse;
    private javax.swing.JPanel jResponsePanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentClosed() {
        setReopenNetworkComponent(false);
        model.passivate();
        ioProvider.close();
        OpenProjects.getDefault().removePropertyChangeListener(this);
        NbPreferences.forModule(NetworkMonitorTopComponent.class).putInt("separator", jSplitPane.getDividerLocation());
    }

    static boolean canReopenNetworkComponent() {
        return NbPreferences.forModule(NetworkMonitorTopComponent.class).getBoolean("reopen", true);
    }

    static void setReopenNetworkComponent(boolean b) {
        NbPreferences.forModule(NetworkMonitorTopComponent.class).putBoolean("reopen", b);
    }

    private ModelItem lastSelectedItem = null;

    private void selectedItemChanged() {
        assert SwingUtilities.isEventDispatchThread();
        final ModelItem mi = (ModelItem)jRequestsList.getSelectedValue();
        if (lastSelectedItem == mi) {
            return;
        } else {
            if (lastSelectedItem != null) {
                lastSelectedItem.setChangeListener(null);
            }
            lastSelectedItem = mi;
            if (lastSelectedItem != null) {
                lastSelectedItem.setChangeListener(this);
            }
        }
        refreshDetailsView(lastSelectedItem);
    }

    private void refreshDetailsView(ModelItem mi) {
        updater.showItem(mi);
    }

    private void _refreshDetailsView(ModelItem mi) {
        assert SwingUtilities.isEventDispatchThread();
        if (mi != null) {
            mi.updateHeadersPane(jHeaders);
            mi.updateResponsePane(jResponse, jRawResponseResponse.isSelected());
            mi.updateFramesPane(jFrames, jRawResponseFrames.isSelected());
            mi.updatePostDataPane(jRequest, jRawResponseRequest.isSelected());
            mi.updateCallStack(io);
        }
        updateTabVisibility(mi);
    }

    private void updateVisibility() {
        boolean empty = model.getSize() == 0;
        jSplitPane.setVisible(!empty);
        jNoData.setVisible(empty);
        if (!empty && jRequestsList.getSelectedValue() == null) {
            refreshDetailsView(null);
        }
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        updateVisibility();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        updateVisibility();
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        updateVisibility();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refreshDetailsView(lastSelectedItem);
    }

    void resetModel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.reset();
            }
        });
    }

    private void updateTabVisibility(ModelItem mi) {
        int index = 0;
        
        // Header - always visible
        boolean showHeaders = mi != null;
        index = showHideTab(jHeadersPanel, showHeaders, index);

        // Request Data:
        boolean postDataVisible = mi != null && mi.hasPostData();
        index = showHideTab(jRequestPanel, postDataVisible, index);

        // Response:
        boolean hasResponseData = mi != null && mi.hasResponseData();
        index = showHideTab(jResponsePanel, hasResponseData, index);

        // Frames:
        boolean hasFrames = mi != null && mi.hasFrames();
        index = showHideTab(jFramesPanel, hasFrames, index);

        // Call Stack:
        boolean hasCallStack = mi != null && mi.hasCallStack();
        showHideTab(jCallStackPanel, hasCallStack, index);

    }

    private int showHideTab(JPanel jPanel, boolean show, int index) {
        Component comp = index < jTabbedPane1.getTabCount() ? jTabbedPane1.getComponentAt(index) : null;
        if (show) {
             if (jPanel != comp) {
                 jTabbedPane1.add(jPanel, index);
             }
            index++;
        } else {
             if (jPanel == comp) {
                 jTabbedPane1.remove(index);
             }
        }
        return index;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // NetworkMonitor stays open after debugging session was closed so
        // that user can evaluate the results; when project is closed it is
        // necessary to close NetworkMonitor TC as it holds a reference to Project
        // and that reference would prevent closed project from being garbage
        // collected
        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            Project p = model.getProject();
            if (p != null && !OpenProjects.getDefault().isProjectOpen(p)) {
                OpenProjects.getDefault().removePropertyChangeListener(this);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        close();
                        // reopen automatically NetworkMonitor next time:
                        setReopenNetworkComponent(true);
                    }
                });
            }
        }
    }

    private static class ModelItem implements PropertyChangeListener {
        private final Network.Request request;
        private final Network.WebSocketRequest wsRequest;
        private ChangeListener changeListener;
        private String data = "";
        private String failureCause = null;
        private final BrowserFamilyId browserFamilyId;
        private final Project project;
        private AtomicBoolean dataLoaded = new AtomicBoolean(false);

        public ModelItem(Network.Request request, Network.WebSocketRequest wsRequest,
                BrowserFamilyId browserFamilyId, Project project) {
            this.request = request;
            this.wsRequest = wsRequest;
            this.browserFamilyId = browserFamilyId;
            this.project = project;
            if (this.request != null) {
                this.request.addPropertyChangeListener(this);
            } else {
                this.wsRequest.addPropertyChangeListener(this);
            }
        }

        public boolean canBeShownToUser() {
            if (wsRequest != null) {
                return true;
            }
            if (("script".equals(request.getInitiatorType()) &&
                    request.getResponse() != null && !"Image".equals(request.getResponseType()) ||
                (request.getResponse() != null && "XHR".equals(request.getResponseType())))) {
                return true;
            }

            if (browserFamilyId == BrowserFamilyId.JAVAFX_WEBVIEW) {
                // WebView does not have "script" initiator type:
                if (("other".equals(request.getInitiatorType()) &&
                        request.getResponse() != null && !"Image".equals(request.getResponseType()) &&
                        !"Document".equals(request.getResponseType())) ) {
                    return true;
                }
            }

            if (request.getResponseCode() != -1 && request.getResponseCode() >= 400) {
                return true;
            }
            
            return request.isFailed();
        }

        public boolean hasPostData() {
            return request != null && request.getRequest().get("postData") != null;
        }

        public boolean hasResponseData() {
            return request != null && request.hasData();
        }

        public boolean hasFrames() {
            return wsRequest != null && !wsRequest.getFrames().isEmpty();
        }

        public boolean hasCallStack() {
            return request != null && request.getInitiator() != null &&
                    request.getInitiator().get("stackTrace") != null;
        }

        private String getPostData() {
            return (String)request.getRequest().get("postData");
        }

        @Override
        public String toString() {
            if (request != null) {
                String s = (String)request.getRequest().get("url");
                s = s.replace("http://", "").replace("https://", "").replace("file:///", "");
                int index = s.indexOf("?");
                if (index != -1) {
                    s = s.substring(0, index);
                }
                return (String)request.getRequest().get("method") + " " + s;
            } else {
                String s = String.valueOf(wsRequest.getURL());
                s = s.replace("ws://", "");
                return s;
            }
        }

        void setChangeListener(ChangeListener changeListener) {
            this.changeListener = changeListener;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Network.Request.PROP_RESPONSE_DATA.equals(evt.getPropertyName())) {
                startLoadingData();
            }
            fireChange();
        }

        public  JSONObject getRequestHeaders() {
            if (request != null) {
                JSONObject requestHeaders = (JSONObject)request.getRequest().get("headers");
                JSONObject r = (JSONObject)request.getResponse();
                if (r != null) {
                    r = (JSONObject)r.get("requestHeaders");
                    if (r != null) {
                        for (Object o : r.entrySet()) {
                            Map.Entry m = (Map.Entry)o;
                            requestHeaders.put(m.getKey(), m.getValue());
                        }
                    }
                }
                return requestHeaders;
            } else {
                JSONObject r = (JSONObject)wsRequest.getHandshakeRequest();
                if (r == null) {
                    return null;
                }
                return (JSONObject)r.get("headers");
            }
        }

        public  JSONObject getResponseHeaders() {
            if (request != null) {
                JSONObject r = (JSONObject)request.getResponse();
                if (r == null) {
                    return null;
                }
                return (JSONObject)r.get("headers");
            } else {
                JSONObject r = (JSONObject)wsRequest.getHandshakeResponse();
                if (r == null) {
                    return null;
                }
                return (JSONObject)r.get("headers");
            }
        }

        public void updateHeadersPane(JTextPane pane) {
            try {
                updateTextPaneImpl(pane);
                pane.setCaretPosition(0);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private void updateTextPaneImpl(JTextPane pane) throws BadLocationException {
            Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
            StyledDocument doc = pane.getStyledDocument();
            Style boldStyle = doc.addStyle("bold", defaultStyle);
            StyleConstants.setBold(boldStyle, true);
            Style errorStyle = doc.addStyle("error", defaultStyle);
            StyleConstants.setBold(errorStyle, true);
            StyleConstants.setFontSize(errorStyle, StyleConstants.getFontSize(errorStyle)+6);
            StyleConstants.setForeground(errorStyle, Color.red);
            Style paragraphStyle = doc.addStyle("paragraph", defaultStyle);
            StyleConstants.setFontSize(paragraphStyle, StyleConstants.getFontSize(paragraphStyle)+8);
            StyleConstants.setForeground(paragraphStyle, Color.gray);
            pane.setText("");

            if (request != null) {
                doc.insertString(doc.getLength(), "Request URL: ", boldStyle);
                doc.insertString(doc.getLength(), (String)request.getRequest().get("url")+"\n", defaultStyle);
                doc.insertString(doc.getLength(), "Method: ", boldStyle);
                doc.insertString(doc.getLength(), (String)request.getRequest().get("method")+"\n", defaultStyle);
                JSONObject r = getResponseHeaders();
                if (r != null) {
                    int statusCode = request.getResponseCode();
                    doc.insertString(doc.getLength(), "Status: ", boldStyle);
                    String status = (String)r.get("Status");
                    if (status == null) {
                        status = statusCode == -1 ? "" : ""+statusCode +
                                " " + request.getResponse().get("statusText");
                    }
                    doc.insertString(doc.getLength(), status+"\n",
                            statusCode >= 400 ? errorStyle : defaultStyle);
                    Boolean fromCache = (Boolean)r.get("fromDiskCache");
                    if (Boolean.TRUE.equals(fromCache)) {
                        doc.insertString(doc.getLength(), "From Disk Cache: ", boldStyle);
                        doc.insertString(doc.getLength(), "yes\n", defaultStyle);
                    }
                } else if (request.isFailed()) {
                    doc.insertString(doc.getLength(), "Status: ", boldStyle);
                    if (failureCause != null) {
                        doc.insertString(doc.getLength(), "Request was cancelled. "+failureCause+"\n", errorStyle);
                        doc.insertString(doc.getLength(), "This type of failure is usually caused by the browser's Same Origin Security Policy. "
                                + "There are two ways to comply with the policy:\n"
                                + " - the REST server enables cross-origin requests. This is a preferred solution.\n"
                                + "   (in NetBeans see 'Jersey Cross-Origin Resource Sharing' new file wizard in Web Services category)\n"
                                + " - use 'JSONP' workaround to call REST endpoint\n", defaultStyle);
                    } else {
                        doc.insertString(doc.getLength(), "Request was cancelled.\n", errorStyle);
                    }
                }
            } else {
                doc.insertString(doc.getLength(), "Request URL: ", boldStyle);
                doc.insertString(doc.getLength(), wsRequest.getURL()+"\n", defaultStyle);
                doc.insertString(doc.getLength(), "Status: ", boldStyle);
                if (wsRequest.getErrorMessage() != null) {
                    doc.insertString(doc.getLength(), wsRequest.getErrorMessage()+"\n", errorStyle);
                } else {
                    doc.insertString(doc.getLength(), wsRequest.isClosed() ? "Closed\n" :
                        wsRequest.getHandshakeResponse() == null ? "Opening\n" : "Open\n", defaultStyle);
                }
            }

            JSONObject requestHeaders = getRequestHeaders();
            if (requestHeaders == null) {
                return;
            }
            doc.insertString(doc.getLength(), "\n", defaultStyle);
            doc.insertString(doc.getLength(), "Request Headers\n", paragraphStyle);
            printHeaders(pane, requestHeaders, doc, boldStyle, defaultStyle);

            if (getResponseHeaders() != null) {
                doc.insertString(doc.getLength(), "\n", defaultStyle);
                doc.insertString(doc.getLength(), "Response Headers\n", paragraphStyle);
                printHeaders(pane, getResponseHeaders(), doc, boldStyle, defaultStyle);
            }
        }

        private void printHeaders(JTextPane pane, JSONObject headers,
                StyledDocument doc, Style boldStyle, Style defaultStyle) throws BadLocationException {

            assert headers != null;
            Set keys = new TreeSet(new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    return ((String)o1).compareToIgnoreCase((String)o2);
                }

            });
            keys.addAll(headers.keySet());
            for (Object oo : keys) {
                String key = (String)oo;
                doc.insertString(doc.getLength(), key+": ", boldStyle);
                String value = (String)headers.get(key);
                doc.insertString(doc.getLength(), value+"\n", defaultStyle);
            }
        }

        private void fireChange() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ChangeListener l = changeListener;
                    if (l != null) {
                        l.stateChanged(null);
                    }
                }
            });
        }

        private void setFailureCause(String cause) {
            this.failureCause = cause;
            fireChange();
        }

        private void loadRequestData() {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    assert request.hasData();
                    data = request.getResponseData();
                    fireChange();
                }
            });
        }

        public void updateResponsePane(JEditorPane pane, boolean rawData) {
            if (!hasResponseData()) {
                return;
            }
            try {
                updateResponseDataImpl(pane, rawData);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void updateFramesPane(JEditorPane pane, boolean rawData) {
            if (!hasFrames()) {
                return;
            }
            try {
                updateFramesImpl(pane, rawData);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private void startLoadingData() {
            if (!request.hasData() || !canBeShownToUser() || dataLoaded.getAndSet(true)) {
                return;
            }
            data = "loading...";
            loadRequestData();
        }

        private void updateResponseDataImpl(JEditorPane pane, boolean rawData) throws BadLocationException {
            assert data != null;
            if (rawData || data.isEmpty()) {
                pane.setEditorKit(CloneableEditorSupport.getEditorKit("text/plain"));
                pane.setText(data);
            } else {
                String contentType = stripDownContentType((JSONObject)request.getResponse().get("headers"));
                reformatAndUseRightEditor(pane, data, contentType);
            }
            pane.setCaretPosition(0);
        }

        private void updateFramesImpl(JEditorPane pane, boolean rawData) throws BadLocationException {
            Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
            StyledDocument doc = (StyledDocument)pane.getDocument();
            Style timingStyle = doc.addStyle("timing", defaultStyle);
            StyleConstants.setForeground(timingStyle, Color.lightGray);
            Style infoStyle = doc.addStyle("comment", defaultStyle);
            StyleConstants.setForeground(infoStyle, Color.darkGray);
            StyleConstants.setBold(infoStyle, true);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
            pane.setText("");
            StringBuilder sb = new StringBuilder();
            int lastFrameType = -1;
            for (Network.WebSocketFrame f : wsRequest.getFrames()) {
                int opcode = f.getOpcode();
                if (opcode == 0) { // "continuation frame"
                    opcode = lastFrameType;
                } else {
                    lastFrameType = opcode;
                }
                if (opcode == 1) { // "text frame"
                    if (!rawData) {
                        doc.insertString(doc.getLength(), formatter.format(f.getTimestamp()), timingStyle);
                        doc.insertString(doc.getLength(), f.getDirection() == Network.Direction.SEND ? " SENT " : " RECV ", timingStyle);
                    }
                    doc.insertString(doc.getLength(), f.getPayload()+"\n", defaultStyle);
                } else if (opcode == 2) { // "binary frame"
                    if (!rawData) {
                        doc.insertString(doc.getLength(), formatter.format(f.getTimestamp()), timingStyle);
                        doc.insertString(doc.getLength(), f.getDirection() == Network.Direction.SEND ? " SENT " : " RECV ", timingStyle);
                    }
                    // XXX: binary data???
                    doc.insertString(doc.getLength(), f.getPayload()+"\n", defaultStyle);
                } else if (opcode == 8) { // "close frame"
                    if (!rawData) {
                        doc.insertString(doc.getLength(), formatter.format(f.getTimestamp()), timingStyle);
                        doc.insertString(doc.getLength(), f.getDirection() == Network.Direction.SEND ? " SENT " : " RECV ", timingStyle);
                    }
                    doc.insertString(doc.getLength(), "Frame closed\n", infoStyle);
                }
            }
            data = sb.toString();
            pane.setCaretPosition(0);
        }

        public void updatePostDataPane(JEditorPane pane, boolean rawData) {
            if (hasPostData()) {
                if (rawData) {
                    pane.setEditorKit(CloneableEditorSupport.getEditorKit("text/plain"));
                    pane.setText(getPostData());
                } else {
                    String contentType = stripDownContentType(getRequestHeaders());
                    reformatAndUseRightEditor(pane, getPostData(), contentType);
                }
            }
        }

        private void updateCallStack(InputOutput io) {
            try {
                io.getOut().reset();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (hasCallStack()) {
                List<ConsoleMessage.StackFrame> callStack = request.getInitiatorCallStack();
                for (ConsoleMessage.StackFrame sf : callStack) {
                    String projectUrl = getProjectPath(project, sf.getURLString());
                    io.getOut().print(sf.getFunctionName()+ " ");
                    String text = "(" +
                            projectUrl+":"+sf.getLine()+":"+sf.getColumn()+")";
                    BrowserConsoleLogger.MyListener l = new BrowserConsoleLogger.MyListener(project, sf.getURLString(), sf.getLine(), sf.getColumn());
                    if (l.isValidHyperlink()) {
                        try {
                            io.getOut().println(text, l);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else {
                        io.getOut().println(text);
                    }
                }
                
            }
        }

        private boolean isError() {
            if (wsRequest != null) {
                return wsRequest.getErrorMessage() != null;
            } else {
                return request.isFailed() || request.getResponseCode() >= 400;
            }
        }

        private boolean inactive = false;
        
        void deactivateItem(Project p) {
            if (project == p) {
                inactive = true;
            }
        }

        boolean isInactive() {
            return inactive;
        }

        private boolean isLive() {
            return wsRequest != null && !wsRequest.isClosed();
        }

    }

    private static String stripDownContentType(JSONObject o) {
        assert o != null;
        String contentType = (String)o.get("Content-Type");
        if (contentType == null) {
            contentType = (String)o.get("content-type");
        }
        if (contentType == null) {
            return null;
        }
        int index = contentType.indexOf(";");
        if (index != -1) {
            contentType = contentType.substring(0, index);
        }
        return contentType;
    }

    private static void reformatAndUseRightEditor(JEditorPane pane, String data, String contentType) {
        if ("application/javascript".equals(contentType)) {
            // check whether this JSONP response, that is a JS method call returning JSON:
            String json = getJSONPResponse(data);
            if (json != null) {
                data = json;
                contentType = "application/json";
            }
        }
        if ("application/json".equals(contentType) || "text/x-json".equals(contentType)) {
            data = reformatJSON(data);
            contentType = "text/x-json";
        }
        if ("application/xml".equals(contentType)) {
            contentType = "text/xml";
        }
        if (contentType == null) {
            contentType = "text/plain";
        }
        pane.setEditorKit(CloneableEditorSupport.getEditorKit(contentType));
        pane.setText(data);
    }

    private static String reformatJSON(String data) {
        Object o = JSONValue.parse(data);
        StringBuilder sb = new StringBuilder();
        if (o instanceof JSONArray) {
            jsonPrettyPrintArray((JSONArray)o, sb, 0);
        } else if (o instanceof JSONObject) {
            jsonPrettyPrintObject((JSONObject)o, sb, 0);
        }
        return sb.toString();
    }

    private static void jsonPrettyPrintObject(JSONObject jsonObject, StringBuilder sb, int indent) {
        print(sb, "{\n", indent);
        boolean first = true;
        for (Object o : jsonObject.entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            Map.Entry en = (Map.Entry)o;
            Object value = en.getValue();
            String key = "\"" + en.getKey() + "\"";
            if (value instanceof JSONObject) {
                print(sb, key+": ", indent+2);
                jsonPrettyPrintObject((JSONObject)value, sb, indent+2);
            } else if (value instanceof JSONArray) {
                print(sb, key+": ", indent+2);
                jsonPrettyPrintArray((JSONArray)value, sb, indent+2);
            } else if (value instanceof String) {
                print(sb, key+": \""+ ((String)value).replace("\"", "\\\"")+"\"", indent+2);
            } else {
                print(sb, key+": "+ value, indent+2);
            }
            first = false;
        }
        sb.append("\n");
        print(sb, "}", indent);
    }

    private static void jsonPrettyPrintArray(JSONArray jsonObject, StringBuilder sb, int indent) {
        print(sb, "[\n", indent);
        boolean first = true;
        for (Object value : jsonObject) {
            if (!first) {
                sb.append(",\n");
            }
            if (value instanceof JSONObject) {
                jsonPrettyPrintObject((JSONObject)value, sb, indent+4);
            } else if (value instanceof JSONArray) {
                jsonPrettyPrintArray((JSONArray)value, sb, indent+4);
            } else if (value instanceof String) {
                print(sb, "\""+((String)value).replace("\"", "\\\"")+"\"", indent+2);
            } else {
                print(sb, value.toString(), indent+2);
            }
            first = false;
        }
        sb.append("\n");
        print(sb, "]", indent);
    }

    private static void print(StringBuilder sb, String text, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(" ");
        }
        sb.append(text);
    }

    static String getJSONPResponse(String data) {
        Pattern p = Pattern.compile("([0-9a-zA-Z_$]+?\\()([\\{\\[].*?[\\}\\]])(\\)[\\;]?[\n\r]?)", Pattern.DOTALL);
        Matcher m = p.matcher(data);
        if (m.matches()) {
            return m.group(2);
        }
        return null;
    }

    static class Model extends AbstractListModel implements PropertyChangeListener {

        // synchronized by this:
        private List<ModelItem> allRequests = new ArrayList<>();
        // synchronized by AWT thread:
        private List<ModelItem> visibleRequests = new ArrayList<>();
        private boolean passive = true;

        public Model() {
        }

        synchronized Project getProject() {
            if (!allRequests.isEmpty()) {
                return allRequests.get(0).project;
            }
            return null;
        }

        void passivate() {
            passive = true;
        }

        void activate() {
            passive = false;
        }

        public void add(Network.Request r, BrowserFamilyId browserFamilyId, Project project) {
            if (passive) {
                return;
            }
            add(new ModelItem(r, null, browserFamilyId, project));
            r.addPropertyChangeListener(this);
            // with regular request do not call updateVisibleItems() here as we need
            // to receive response headers first and they will fire Network.Request.PROP_RESPONSE event
        }

        public void add(Network.WebSocketRequest r, BrowserFamilyId browserFamilyId, Project project) {
            if (passive) {
                return;
            }
            add(new ModelItem(null, r, browserFamilyId, project));
            r.addPropertyChangeListener(this);
            updateVisibleItems();
        }

        private synchronized void add(ModelItem item) {
            allRequests.add(item);
        }

        @Override
        public int getSize() {
            return visibleRequests.size();
        }

        @Override
        public Object getElementAt(int index) {
            return visibleRequests.get(index);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Network.Request.PROP_RESPONSE.equals(evt.getPropertyName())) {
                updateVisibleItems();
            }
        }

        private synchronized void updateVisibleItems() {
            List<ModelItem> res = new ArrayList<>();
            for (ModelItem mi : allRequests) {
                if (mi.canBeShownToUser()) {
                    res.add(mi);
                }
            }
            updateInAWT(res);
        }

        private void updateInAWT(final List<ModelItem> res) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    visibleRequests = res;
                    fireContentsChanged(this, 0, visibleRequests.size());
                }
            });
        }

        private synchronized void reset() {
            assert SwingUtilities.isEventDispatchThread();
            int size = allRequests.size();
            allRequests = new ArrayList<>();
            visibleRequests = new ArrayList<>();
            fireIntervalRemoved(this, 0, size);
        }

        synchronized void console(ConsoleMessage message) {
            if (passive) {
                return;
            }
            // handle case of following message:
            //
            // event {"method":"Console.messageAdded","params":{"message":{"text":
            //   "XMLHttpRequest cannot load http:\/\/localhost:8080\/SampleDBrest
            //   \/resources\/aaa.manXXXufacturer\/. Origin http:\/\/localhost:8383
            //   is not allowed by Access-Control-Allow-Origin.","level":"error",
            //   "source":"javascript","line":0,"repeatCount":1,"type":"log","url"
            //   :"http:\/\/localhost:8383\/nb-rest-test\/knockout-approach\/index-ko.html"}}}

            if (message.getText().contains("Access-Control-Allow-Origin") && !allRequests.isEmpty()) {
                ModelItem mi = allRequests.get(allRequests.size()-1);
                // XXX: perhaps I should match requests here with a timestamp???
                if (mi.request != null) {
                    mi.setFailureCause(message.getText());
                }
            }
        }

        synchronized void close(Project project) {
            for (ModelItem mi : allRequests) {
                mi.deactivateItem(project);
            }
        }

        public synchronized boolean canResetModel() {
            boolean allDeactivated = true;
            for (ModelItem mi : allRequests) {
                if (!mi.isInactive()) {
                    allDeactivated = false;
                    break;
                }
            }
            // if model contains only deactivated items they can be reset:
            return allDeactivated;
        }

    }

    public static class JTextPaneNonWrapping extends JTextPane {

        public JTextPaneNonWrapping() {
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            Component parent = getParent();

            return parent != null ? (getUI().getPreferredSize(this).width <= parent
                    .getSize().width) : true;
        }

    }

    private static class ListRendererImpl extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ModelItem) {
                ModelItem mi = (ModelItem)value;
                if (mi.isError()) {
                    c.setForeground(Color.red);
                } else if (mi.isLive()) {
                    c.setForeground(Color.blue);
                }
            }
            return c;
        }

    }

    private static class MyProvider implements IOContainer.Provider {

        private JPanel parent;

        public MyProvider(JPanel parent) {
            this.parent = parent;
        }

        @Override
        public void open() {
        }

        @Override
        public void requestActive() {
        }

        @Override
        public void requestVisible() {
        }

        @Override
        public boolean isActivated() {
            return false;
        }

        @Override
        public void add(JComponent comp, IOContainer.CallBacks cb) {
            assert parent != null;
            parent.setLayout(new BorderLayout());
            parent.add(comp, BorderLayout.CENTER);
        }

        @Override
        public void remove(JComponent comp) {
            assert parent != null;
            parent.remove(comp);
        }

        @Override
        public void select(JComponent comp) {
        }

        @Override
        public JComponent getSelected() {
            return null;
        }

        @Override
        public void setTitle(JComponent comp, String name) {
        }

        @Override
        public void setToolTipText(JComponent comp, String text) {
        }

        @Override
        public void setIcon(JComponent comp, Icon icon) {
        }

        @Override
        public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
        }

        @Override
        public boolean isCloseable(JComponent comp) {
            return false;
        }

        private void close() {
            parent = null;
        }

    }
}
