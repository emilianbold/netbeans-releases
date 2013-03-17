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

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractListModel;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage;
import org.netbeans.modules.web.webkit.debugging.api.network.Network;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

@TopComponent.Description(
        preferredID = "NetworkMonitorTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@Messages({
    "CTL_NetworkMonitorTopComponent=Network Monitor",
    "HINT_NetworkMonitorTopComponent=This is a Network Monitor window"
})
public final class NetworkMonitorTopComponent extends TopComponent implements ListDataListener, ChangeListener {

    private Model model;
    private static RequestProcessor RP = new RequestProcessor(NetworkMonitorTopComponent.class.getName(), 5);
    private NetworkMonitor parent;

    NetworkMonitorTopComponent(NetworkMonitor parent, Model m) {
        initComponents();
        jResponse.setEditorKit(CloneableEditorSupport.getEditorKit("text/plain"));
        setName(Bundle.CTL_NetworkMonitorTopComponent());
        setToolTipText(Bundle.HINT_NetworkMonitorTopComponent());
        this.model = m;
        this.parent = parent;
        jRequestsList.setModel(model);
        jSplitPane.setDividerLocation(200);
        model.addListDataListener(this);
        selectedItemChanged();
        updateVisibility();
    }

    Model getModel() {
        return model;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jRequestsList = new javax.swing.JList();
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
        jScrollPane6 = new javax.swing.JScrollPane();
        jCallStack = new JTextPaneNonWrapping();
        jNoData = new javax.swing.JLabel();

        jRequestsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jRequestsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jRequestsListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jRequestsList);

        jSplitPane.setLeftComponent(jScrollPane1);

        jHeadersPanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jHeadersPanel.TabConstraints.tabTitle")); // NOI18N

        jHeaders.setEditable(false);
        jScrollPane5.setViewportView(jHeaders);

        javax.swing.GroupLayout jHeadersPanelLayout = new javax.swing.GroupLayout(jHeadersPanel);
        jHeadersPanel.setLayout(jHeadersPanelLayout);
        jHeadersPanelLayout.setHorizontalGroup(
            jHeadersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
        );
        jHeadersPanelLayout.setVerticalGroup(
            jHeadersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
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
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jResponsePanelLayout.setVerticalGroup(
            jResponsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResponsePanelLayout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
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
                .addGap(0, 310, Short.MAX_VALUE)
                .addComponent(jRawResponseFrames))
            .addComponent(jScrollPane4)
        );
        jFramesPanelLayout.setVerticalGroup(
            jFramesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFramesPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRawResponseFrames))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jFramesPanel.TabConstraints.tabTitle"), jFramesPanel); // NOI18N

        jCallStackPanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jCallStackPanel.TabConstraints.tabTitle")); // NOI18N

        jScrollPane6.setViewportView(jCallStack);

        javax.swing.GroupLayout jCallStackPanelLayout = new javax.swing.GroupLayout(jCallStackPanel);
        jCallStackPanel.setLayout(jCallStackPanelLayout);
        jCallStackPanelLayout.setHorizontalGroup(
            jCallStackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
        );
        jCallStackPanelLayout.setVerticalGroup(
            jCallStackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jCallStackPanel.TabConstraints.tabTitle"), jCallStackPanel); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 451, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 264, Short.MAX_VALUE)
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
            .addComponent(jNoData)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jSplitPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jNoData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane jCallStack;
    private javax.swing.JPanel jCallStackPanel;
    private javax.swing.JTextPane jFrames;
    private javax.swing.JPanel jFramesPanel;
    private javax.swing.JTextPane jHeaders;
    private javax.swing.JPanel jHeadersPanel;
    private javax.swing.JLabel jNoData;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
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
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        parent.componentClosed();
    }

    @Override
    public void open() {
        Mode m = WindowManager.getDefault().findMode("output");
        if (m != null) {
            m.dockInto(this);
        }
        super.open();
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
        assert SwingUtilities.isEventDispatchThread();
        if (mi != null) {
            mi.updateHeadersPane(jHeaders);
            mi.updateResponsePane(jResponse, jRawResponseResponse.isSelected());
            mi.updateFramesPane(jFrames, jRawResponseFrames.isSelected());
            mi.updatePostDataPane(jRequest, jRawResponseRequest.isSelected());
            mi.updateCallStack(jCallStack);
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

    void resetModel(BrowserFamilyId browserFamilyId) {
        model.reset(browserFamilyId);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jRequestsList.setModel(model);
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

    private static class ModelItem implements PropertyChangeListener {
        private Network.Request request;
        private Network.WebSocketRequest wsRequest;
        private ChangeListener changeListener;
        private String data = null;

        public ModelItem(Network.Request request, Network.WebSocketRequest wsRequest) {
            this.request = request;
            this.wsRequest = wsRequest;
            if (this.request != null) {
                this.request.addPropertyChangeListener(this);
            } else {
                this.wsRequest.addPropertyChangeListener(this);
            }
        }

        public boolean canBeShownToUser(BrowserFamilyId browserFamilyId) {
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
            return false;
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
                    doc.insertString(doc.getLength(), "Status: ", boldStyle);
                    String status = (String)r.get("Status");
                    if (status == null) {
                        status = ((Number)request.getResponse().get("status")).toString() + 
                                " " + request.getResponse().get("statusText");
                    }
                    doc.insertString(doc.getLength(), status+"\n", defaultStyle);
                    Boolean fromCache = (Boolean)r.get("fromDiskCache");
                    if (Boolean.TRUE.equals(fromCache)) {
                        doc.insertString(doc.getLength(), "From Disk Cache: ", boldStyle);
                        doc.insertString(doc.getLength(), "yes\n", defaultStyle);
                    }
                }
            } else {
                doc.insertString(doc.getLength(), "Request URL: ", boldStyle);
                doc.insertString(doc.getLength(), wsRequest.getURL()+"\n", defaultStyle);
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
                updateDataImpl(pane, rawData);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void updateFramesPane(JEditorPane pane, boolean rawData) {
            if (!hasFrames()) {
                return;
            }
            try {
                updateDataImpl(pane, rawData);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private void updateDataImpl(JEditorPane pane, boolean rawData) throws BadLocationException {
            if (request != null) {
                if (!request.hasData()) {
                    data = "";
                }
                if (data == null) {
                    data = "loading...";
                    loadRequestData();
                    pane.setText(data);
                } else {
                    if (rawData || data.isEmpty()) {
                        pane.setEditorKit(CloneableEditorSupport.getEditorKit("text/plain"));
                        pane.setText(data);
                    } else {
                        String contentType = stripDownContentType((String)((JSONObject)request.getResponse().get("headers")).get("Content-Type"));
                        reformatAndUseRightEditor(pane, data, contentType);
                    }
                }
            } else {
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
            }
            pane.setCaretPosition(0);
        }

        public void updatePostDataPane(JEditorPane pane, boolean rawData) {
            if (hasPostData()) {
                if (rawData) {
                    pane.setEditorKit(CloneableEditorSupport.getEditorKit("text/plain"));
                    pane.setText(getPostData());
                } else {
                    String contentType = stripDownContentType((String)getRequestHeaders().get("Content-Type"));
                    reformatAndUseRightEditor(pane, getPostData(), contentType);
                }
            }
        }

        private void updateCallStack(JTextPane pane) {
            pane.setText("");
            StyledDocument doc = pane.getStyledDocument();
            if (hasCallStack()) {
                List<ConsoleMessage.StackFrame> callStack = request.getInitiatorCallStack();
                for (ConsoleMessage.StackFrame sf : callStack) {
                    String text = sf.getFunctionName()+ " (" +
                            sf.getURLString()+":"+sf.getLine()+":"+sf.getColumn()+")\n";
                    try {
                        doc.insertString(doc.getLength(), text, null);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
            }
        }

    }

    private static String stripDownContentType(String contentType) {
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
        if ("application/json".equals(contentType)) {
            data = reformatJSON(data);
            contentType = "text/x-json";
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

    private static String getJSONPResponse(String data) {
        Pattern p = Pattern.compile("([0-9a-zA-Z_$]+?\\()([\\{\\[].*?[\\}\\]])(\\)\\;)");
        Matcher m = p.matcher(data);
        if (m.matches()) {
            return m.group(2);
        }
        return null;
    }

    static class Model extends AbstractListModel implements PropertyChangeListener {

        private BrowserFamilyId browserFamilyId;
        private List<ModelItem> allRequests = new ArrayList<ModelItem>();
        private List<ModelItem> visibleRequests = new ArrayList<ModelItem>();

        public Model(BrowserFamilyId browserFamilyId) {
            this.browserFamilyId = browserFamilyId;
        }
        
        public void add(Network.Request r) {
            add(new ModelItem(r, null));
            r.addPropertyChangeListener(this);
        }

        public void add(Network.WebSocketRequest r) {
            add(new ModelItem(null, r));
            r.addPropertyChangeListener(this);
            updateVisibleItems();
        }

        private void add(ModelItem item) {
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

        private void updateVisibleItems() {
            List<ModelItem> res = new ArrayList<ModelItem>();
            for (ModelItem mi : allRequests) {
                if (mi.canBeShownToUser(browserFamilyId)) {
                    res.add(mi);
                }
            }
            visibleRequests = res;
            updateInAWT();
        }

        private void updateInAWT() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireContentsChanged(this, 0, visibleRequests.size());
                }
            });
        }

        void reset(BrowserFamilyId browserFamilyId) {
            this.browserFamilyId = browserFamilyId;
            allRequests = new ArrayList<ModelItem>();
            visibleRequests = new ArrayList<ModelItem>();
        }

    }

    public static class JTextPaneNonWrapping extends JTextPane {

        public JTextPaneNonWrapping() {
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            Component parent = getParent();
            ComponentUI ui = getUI();

            return parent != null ? (ui.getPreferredSize(this).width <= parent
                    .getSize().width) : true;
        }

    }
}
