/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.client.javascript.debugger.http.ui;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JComponent;


import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpActivity;
import org.netbeans.modules.web.client.javascript.debugger.http.ui.models.HttpActivitiesModel;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpRequest;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.CompoundModel;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
final class HttpMonitorTopComponent extends TopComponent {

    private static HttpMonitorTopComponent instance;
    
    /** path to the icon used by the component and its open action */
    // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
    static final String ICON_PATH = "org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/HttpMonitor.png";
    static final String START_ICON_PATH = "/org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/Continue.gif";
    static final String STOP_ICON_PATH = "/org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/Kill.gif";

    /* COLUMN MODELS */
    private static final Model METHOD_COLUMN = HttpActivitiesModel.getColumnModel(HttpActivitiesModel.METHOD_COLUMN);
    private static final Model SENT_COLUMN = HttpActivitiesModel.getColumnModel(HttpActivitiesModel.SENT_COLUMN);
    private static final Model RESPONSE_COLUMN = HttpActivitiesModel.getColumnModel(HttpActivitiesModel.RESPONSE_COLUMN);


    private static final String PREFERRED_ID = "HttpMonitorTopComponent";
    private final static HttpMonitorPreferences httpMonitorPreferences = HttpMonitorPreferences.getInstance();
    private final ActivitiesPropertyChange activityPropertyChangeListener = new ActivitiesPropertyChange();
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    /* Table Models for Request and Response Details */
    private final MapTableModel reqHeaderTableModel = new MapTableModel(EMPTY_MAP);
    private final MapTableModel resHeaderTableModel = new MapTableModel(EMPTY_MAP);

    /* Component for main table */
    private JComponent tableView;

    private HttpMonitorTopComponent() {
        if ( HttpMonitorUtility.getCurrentHttpMonitorModel() != null) {
           HttpMonitorUtility.setEnabled(true);
        }
        initComponents();
        setName(NbBundle.getMessage(HttpMonitorTopComponent.class, "CTL_HttpMonitorTopComponent"));
        setToolTipText(NbBundle.getMessage(HttpMonitorTopComponent.class, "HINT_HttpMonitorTopComponent"));
        setIcon(Utilities.loadImage(ICON_PATH, true));
    }
    
    private Icon StartIcon;
    private Icon StopIcon;
    private final Icon getStartStopIcon() {
        if (HttpMonitorUtility.isEnabled()) {
            return (StopIcon != null ? StopIcon : new javax.swing.ImageIcon(getClass().getResource(STOP_ICON_PATH)));
        }
        return (StartIcon != null ? StopIcon : new javax.swing.ImageIcon(getClass().getResource(START_ICON_PATH)));
    }


    private JComponent createActivitiesTable() {
        CompoundModel compoundModel = createViewCompoundModel(HttpMonitorUtility.getCurrentHttpMonitorModel());

        tableView = Models.createView(compoundModel);
        assert tableView instanceof ExplorerManager.Provider;

        ExplorerManager activityExplorerManager = ((ExplorerManager.Provider) tableView).getExplorerManager();
        activityExplorerManager.addPropertyChangeListener(activityPropertyChangeListener);

        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_CURRENT_SESSION, new DebuggerManagerListenerImpl());

        return tableView;
    }

    private void resetHttpActivitesModel(HttpActivitiesModel model) {
        CompoundModel compoundModel = createViewCompoundModel(model);
        Models.setModelsToView(tableView, compoundModel);
    }

    private static CompoundModel createViewCompoundModel(HttpActivitiesModel model) {
        List<Model> models = new ArrayList<Model>();
        if (model != null) {
                models.add(model);
                models.add(METHOD_COLUMN);
                models.add(SENT_COLUMN);
                models.add(RESPONSE_COLUMN);
        }
        CompoundModel compoundModel = Models.createCompoundModel(models);
        return compoundModel;
    }

    private class ActivitiesPropertyChange implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                if (reqHeaderJTable != null) {

                    assert evt.getNewValue() instanceof Node[];
                    Node[] nodes = (Node[]) evt.getNewValue();
                    if (nodes == null || nodes.length < 1) {
                        reqHeaderTableModel.setMap(EMPTY_MAP);
                        reqParamTextArea.setText("");
                        resHeaderTableModel.setMap(EMPTY_MAP);
                        resBodyTextArea.setText("");
                        return;
                    }

                    assert nodes[0] instanceof Node;
                    Node aNode = (Node) nodes[0];
                    HttpActivity activity = aNode.getLookup().lookup(HttpActivity.class);
                    if (activity != null) {
                        JSHttpRequest request = activity.getRequest();
                        assert request != null;
                        reqHeaderTableModel.setMap(activity.getRequestHeader());
                        if (request.getMethod().equals(JSHttpRequest.MethodType.POST)) {
                            reqParamTextArea.setText("POST: " + request.getPostText());
                        } else {
                            reqParamTextArea.setText("URL PARAMS: " + request.getUrlParams());
                        }

                        Map<String,String> header = activity.getResponseHeader();
                        if (header != null) {
                            resHeaderTableModel.setMap(header);
                            resBodyTextArea.setText(activity.getResponseText());
                        } else {
                            resHeaderTableModel.setMap(EMPTY_MAP);
                            resBodyTextArea.setText("");
                        }
                    }
                }
            }

        }
    }
    
    private static final String PREF_HttpMonitorSplitPane_DIVIDERLOC = "HttpMonitorSplitPane_DIVIDERLOC";
    private static final String PREF_DetailsSplitPane_DIVIDERLOC = "DetailsSplitPane_DIVIDERLOC";

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                detailsSplitPane.setDividerLocation(getDetailsDividerLoc());
                httpMonitorSplitPane.setDividerLocation(getHttpMonitorDividerLoc());
            }
        });
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        setDetailsDividerLoc();
        setHttpMonitorDividerLoc();
    }

    private double getHttpMonitorDividerLoc() {
        return NbPreferences.forModule(HttpMonitorTopComponent.class).getDouble(PREF_HttpMonitorSplitPane_DIVIDERLOC, 0.5);
    }

    private double getDetailsDividerLoc() {
        return NbPreferences.forModule(HttpMonitorTopComponent.class).getDouble(PREF_DetailsSplitPane_DIVIDERLOC, 0.5);
    }

    private void setHttpMonitorDividerLoc() {
        double dividerLocPorportional;
        double dividerLoc = httpMonitorSplitPane.getDividerLocation();
        if (dividerLoc > 1) {
            double height = httpMonitorSplitPane.getHeight();
            dividerLocPorportional = dividerLoc / height;
            assert dividerLocPorportional < 1 && dividerLocPorportional > 0;
        } else {
            dividerLocPorportional = dividerLoc;
        }
        NbPreferences.forModule(HttpMonitorTopComponent.class).putDouble(PREF_HttpMonitorSplitPane_DIVIDERLOC, dividerLocPorportional);
    }

    private void setDetailsDividerLoc() {
        double dividerLoc = detailsSplitPane.getDividerLocation();
        double dividerLocPorportional;
        if (dividerLoc > 1) {
            double width = detailsSplitPane.getWidth();
            dividerLocPorportional = dividerLoc / width;
            assert dividerLocPorportional < 1;
        } else {
            dividerLocPorportional = dividerLoc;
        }
        NbPreferences.forModule(HttpMonitorTopComponent.class).putDouble(PREF_DetailsSplitPane_DIVIDERLOC, dividerLocPorportional);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        httpMonitorSplitPane = new javax.swing.JSplitPane();
        outerActivitiesPanel = new javax.swing.JPanel();
        activitiesToolbar = new javax.swing.JToolBar();
        start_stopMonitoring = new javax.swing.JButton();
        cleanButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        all_filterButton = new javax.swing.JToggleButton();
        html_filterButton = new javax.swing.JToggleButton();
        js_filterButton = new javax.swing.JToggleButton();
        xhr_filterButton = new javax.swing.JToggleButton();
        css_filterButton = new javax.swing.JToggleButton();
        images_filterButton = new javax.swing.JToggleButton();
        flash_filterButton = new javax.swing.JToggleButton();
        activitiesModelPanel = new javax.swing.JPanel();
        detailsPanel = new javax.swing.JPanel();
        detailsSplitPane = new javax.swing.JSplitPane();
        httpReqPanel = new javax.swing.JPanel();
        reqLabel = new javax.swing.JLabel();
        reqTabbedPane = new javax.swing.JTabbedPane();
        reqHeaderPanel = new javax.swing.JScrollPane();
        reqHeaderJTable = new javax.swing.JTable();
        reqParamPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        reqParamTextArea = new javax.swing.JTextArea();
        httpResPanel = new javax.swing.JPanel();
        resLabel = new javax.swing.JLabel();
        resTabbedPane = new javax.swing.JTabbedPane();
        resHeaderPanel = new javax.swing.JScrollPane();
        resHeaderJTable = new javax.swing.JTable();
        resBodyPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        resBodyTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        httpMonitorSplitPane.setDividerLocation(getDetailsDividerLoc());
        httpMonitorSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        httpMonitorSplitPane.setOneTouchExpandable(true);

        outerActivitiesPanel.setLayout(new java.awt.BorderLayout());

        activitiesToolbar.setFloatable(false);
        activitiesToolbar.setRollover(true);

        start_stopMonitoring.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/Kill.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(start_stopMonitoring, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.start_stopMonitoring.text")); // NOI18N
        start_stopMonitoring.setToolTipText(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "TT_Start_StopMonitoring")); // NOI18N
        start_stopMonitoring.setEnabled(false);
        start_stopMonitoring.setFocusable(false);
        start_stopMonitoring.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        start_stopMonitoring.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        start_stopMonitoring.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                StartStopButtonHandler(evt);
            }
        });
        activitiesToolbar.add(start_stopMonitoring);

        cleanButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/clean.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cleanButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.cleanButton.text")); // NOI18N
        cleanButton.setEnabled(false);
        cleanButton.setFocusable(false);
        cleanButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cleanButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cleanButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cleanButtonMouseClicked(evt);
            }
        });
        activitiesToolbar.add(cleanButton);
        activitiesToolbar.add(jSeparator1);

        all_filterButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(all_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.all_filterButton.text")); // NOI18N
        all_filterButton.setEnabled(false);
        all_filterButton.setFocusable(false);
        all_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        all_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        all_filterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                all_filterMouseClicked(evt);
            }
        });
        activitiesToolbar.add(all_filterButton);

        org.openide.awt.Mnemonics.setLocalizedText(html_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.html_filterButton.text")); // NOI18N
        html_filterButton.setEnabled(false);
        html_filterButton.setFocusable(false);
        html_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        html_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        html_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(html_filterButton);

        org.openide.awt.Mnemonics.setLocalizedText(js_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.js_filterButton.text")); // NOI18N
        js_filterButton.setEnabled(false);
        js_filterButton.setFocusable(false);
        js_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        js_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        js_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(js_filterButton);

        org.openide.awt.Mnemonics.setLocalizedText(xhr_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.xhr_filterButton.text")); // NOI18N
        xhr_filterButton.setEnabled(false);
        xhr_filterButton.setFocusable(false);
        xhr_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        xhr_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        xhr_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(xhr_filterButton);

        org.openide.awt.Mnemonics.setLocalizedText(css_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.css_filterButton.text")); // NOI18N
        css_filterButton.setEnabled(false);
        css_filterButton.setFocusable(false);
        css_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        css_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        css_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(css_filterButton);

        org.openide.awt.Mnemonics.setLocalizedText(images_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.images_filterButton.text")); // NOI18N
        images_filterButton.setEnabled(false);
        images_filterButton.setFocusable(false);
        images_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        images_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        images_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(images_filterButton);

        org.openide.awt.Mnemonics.setLocalizedText(flash_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.flash_filterButton.text")); // NOI18N
        flash_filterButton.setEnabled(false);
        flash_filterButton.setFocusable(false);
        flash_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        flash_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        flash_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(flash_filterButton);

        outerActivitiesPanel.add(activitiesToolbar, java.awt.BorderLayout.NORTH);
        Model model = HttpMonitorUtility.getCurrentHttpMonitorModel();
        start_stopMonitoring.setIcon(getStartStopIcon());
        start_stopMonitoring.setEnabled(model != null);
        cleanButton.setEnabled(model != null);

        if( httpMonitorPreferences.isShowAll() ) {
            setFilterButtonsAllSelected();
        } else {
            all_filterButton.setSelected(false);
            resetOtherFilterButtonSelected();
        }

        activitiesModelPanel.setLayout(new java.awt.BorderLayout());
        activitiesModelPanel.add(createActivitiesTable(), BorderLayout.CENTER);
        outerActivitiesPanel.add(activitiesModelPanel, java.awt.BorderLayout.CENTER);

        httpMonitorSplitPane.setTopComponent(outerActivitiesPanel);

        detailsPanel.setLayout(new java.awt.BorderLayout());

        httpReqPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(reqLabel, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.reqLabel.text")); // NOI18N
        httpReqPanel.add(reqLabel, java.awt.BorderLayout.NORTH);

        reqHeaderPanel.setAutoscrolls(true);

        reqHeaderJTable.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        reqHeaderJTable.setModel(reqHeaderTableModel);
        reqHeaderJTable.setGridColor(new java.awt.Color(204, 204, 204));
        reqHeaderPanel.setViewportView(reqHeaderJTable);

        reqTabbedPane.addTab(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.reqHeaderPanel.TabConstraints.tabTitle"), reqHeaderPanel); // NOI18N

        reqParamPanel.setName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.reqHeader.TabConstraints.tabTitle")); // NOI18N
        reqParamPanel.setLayout(new java.awt.BorderLayout());

        reqParamTextArea.setColumns(20);
        reqParamTextArea.setRows(5);
        jScrollPane4.setViewportView(reqParamTextArea);

        reqParamPanel.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        reqTabbedPane.addTab(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.reqParamPanel.TabConstraints.tabTitle"), reqParamPanel); // NOI18N

        httpReqPanel.add(reqTabbedPane, java.awt.BorderLayout.CENTER);

        detailsSplitPane.setLeftComponent(httpReqPanel);

        httpResPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(resLabel, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.resLabel.text")); // NOI18N
        httpResPanel.add(resLabel, java.awt.BorderLayout.NORTH);

        resTabbedPane.setName(""); // NOI18N

        resHeaderPanel.setName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "org.netbeans.modules.web.client.javascript.debugger.http.ui.Bundle")); // NOI18N

        resHeaderJTable.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        resHeaderJTable.setModel(resHeaderTableModel);
        resHeaderJTable.setFocusable(false);
        resHeaderJTable.setGridColor(new java.awt.Color(204, 204, 204));
        resHeaderPanel.setViewportView(resHeaderJTable);

        resTabbedPane.addTab(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.resHeaderPanel.TabConstraints.tabTitle"), resHeaderPanel); // NOI18N

        resBodyPanel.setLayout(new java.awt.BorderLayout());

        resBodyTextArea.setColumns(20);
        resBodyTextArea.setRows(5);
        jScrollPane2.setViewportView(resBodyTextArea);

        resBodyPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        resTabbedPane.addTab(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.resBodyPanel.TabConstraints.tabTitle"), resBodyPanel); // NOI18N

        httpResPanel.add(resTabbedPane, java.awt.BorderLayout.CENTER);

        detailsSplitPane.setRightComponent(httpResPanel);

        detailsPanel.add(detailsSplitPane, java.awt.BorderLayout.CENTER);

        httpMonitorSplitPane.setRightComponent(detailsPanel);

        add(httpMonitorSplitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cleanButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cleanButtonMouseClicked
    HttpActivitiesModel model = HttpMonitorUtility.getCurrentHttpMonitorModel();
        if (model != null) {
            model.clearActivities();
        }
    }//GEN-LAST:event_cleanButtonMouseClicked

    private void StartStopButtonHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_StartStopButtonHandler
        HttpMonitorUtility.setEnabled(!HttpMonitorUtility.isEnabled());
        start_stopMonitoring.setIcon(getStartStopIcon());
    }//GEN-LAST:event_StartStopButtonHandler

    private void all_filterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_all_filterMouseClicked
        setOtherFilterButtonsSelected(true);
        all_filterButton.setSelected(true);
    }//GEN-LAST:event_all_filterMouseClicked


    private void filterButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_filterButtonItemStateChanged
        Object source = evt.getItem();
        int state = evt.getStateChange();
        if( source.equals(html_filterButton)){
            httpMonitorPreferences.setShowHTML(state == ItemEvent.SELECTED);
        } else if ( source.equals(js_filterButton)) {
            httpMonitorPreferences.setShowJS(state == ItemEvent.SELECTED);
        }  else if ( source.equals(xhr_filterButton)) {
            httpMonitorPreferences.setShowXHR(state == ItemEvent.SELECTED);
        } else if ( source.equals(css_filterButton)) {
            httpMonitorPreferences.setShowCSS(state == ItemEvent.SELECTED);
        } else if ( source.equals(images_filterButton)) {
            httpMonitorPreferences.setShowImages(state == ItemEvent.SELECTED);
        } else if ( source.equals(flash_filterButton)) {
            httpMonitorPreferences.setShowFlash(state == ItemEvent.SELECTED);
        }
        if (httpMonitorPreferences.isShowAll() ){
            all_filterButton.setSelected(true);
        } else {
            all_filterButton.setSelected(false);
        }
}//GEN-LAST:event_filterButtonItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel activitiesModelPanel;
    private javax.swing.JToolBar activitiesToolbar;
    private javax.swing.JToggleButton all_filterButton;
    private javax.swing.JButton cleanButton;
    private javax.swing.JToggleButton css_filterButton;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JSplitPane detailsSplitPane;
    private javax.swing.JToggleButton flash_filterButton;
    private javax.swing.JToggleButton html_filterButton;
    private javax.swing.JSplitPane httpMonitorSplitPane;
    private javax.swing.JPanel httpReqPanel;
    private javax.swing.JPanel httpResPanel;
    private javax.swing.JToggleButton images_filterButton;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToggleButton js_filterButton;
    private javax.swing.JPanel outerActivitiesPanel;
    private javax.swing.JTable reqHeaderJTable;
    private javax.swing.JScrollPane reqHeaderPanel;
    private javax.swing.JLabel reqLabel;
    private javax.swing.JPanel reqParamPanel;
    private javax.swing.JTextArea reqParamTextArea;
    private javax.swing.JTabbedPane reqTabbedPane;
    private javax.swing.JPanel resBodyPanel;
    private javax.swing.JTextArea resBodyTextArea;
    private javax.swing.JTable resHeaderJTable;
    private javax.swing.JScrollPane resHeaderPanel;
    private javax.swing.JLabel resLabel;
    private javax.swing.JTabbedPane resTabbedPane;
    private javax.swing.JButton start_stopMonitoring;
    private javax.swing.JToggleButton xhr_filterButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized HttpMonitorTopComponent getDefault() {
        if (instance == null) {
            instance = new HttpMonitorTopComponent();

        }
        return instance;
    }

    /**
     * Obtain the HttpMonitorTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HttpMonitorTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(HttpMonitorTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof HttpMonitorTopComponent) {
            return (HttpMonitorTopComponent) win;
        }
        Logger.getLogger(HttpMonitorTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            Object httpMonitor =  HttpMonitorTopComponent.getDefault();
            return httpMonitor;
        }
    }

    /* Purpose: to listen to the session and update the model when the current
     * session has changed.
     */
    private class DebuggerManagerListenerImpl extends DebuggerManagerAdapter {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            assert evt.getPropertyName().equals(DebuggerManager.PROP_CURRENT_SESSION);
            Object obj = evt.getNewValue();
            if ( obj != null && HttpMonitorUtility.isJSDebuggerSession((Session)obj) ){
                HttpActivitiesModel model = HttpMonitorUtility.getCurrentHttpMonitorModel();
                assert model != null;
                setToolbarButtons(true);
                resetHttpActivitesModel(model);
            } else {
                // The session was cleared
                setToolbarButtons(false);
                resetHttpActivitesModel(null);
            }
            return;
        }

        private void setToolbarButtons(boolean b) {
            cleanButton.setEnabled(b);
            start_stopMonitoring.setEnabled(b);
            all_filterButton.setEnabled(b);
            html_filterButton.setEnabled(b);
            js_filterButton.setEnabled(b);
            images_filterButton.setEnabled(b);
            css_filterButton.setEnabled(b);
            flash_filterButton.setEnabled(b);
            xhr_filterButton.setEnabled(b);
        }


    }

    private void setFilterButtonsAllSelected() {
        all_filterButton.setSelected(true);
        setOtherFilterButtonsSelected(true);
    }

    private void setOtherFilterButtonsSelected (boolean b_val) {
        html_filterButton.setSelected(b_val);
        js_filterButton.setSelected(b_val);
        css_filterButton.setSelected(b_val);
        images_filterButton.setSelected(b_val);
        flash_filterButton.setSelected(b_val);
        xhr_filterButton.setSelected(b_val);
    }

    private void resetOtherFilterButtonSelected() {
        html_filterButton.setSelected(httpMonitorPreferences.isShowHTML());
        js_filterButton.setSelected(httpMonitorPreferences.isShowJS());
        css_filterButton.setSelected(httpMonitorPreferences.isShowCSS());
        images_filterButton.setSelected(httpMonitorPreferences.isShowImages());
        flash_filterButton.setSelected(httpMonitorPreferences.isShowFlash());
        xhr_filterButton.setSelected(httpMonitorPreferences.isShowXHR());
    }

    private class MapTableModel extends AbstractTableModel {

        Map<String, String> map;
        private static final int COL_COUNT = 2;
        String[][] arrayOfMap;

        public MapTableModel(Map<String, String> map) {
            loadMapData(map);
        }

        public int getRowCount() {
            return arrayOfMap[0].length;
        }

        public int getColumnCount() {
            return COL_COUNT;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return arrayOfMap[columnIndex][rowIndex];
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Key";
                case 1:
                    return "Value";
                default:
                    throw new IllegalArgumentException("There is no such column id:" + column);
            }
        }

        public void setMap(Map<String, String> map) {
            loadMapData(map);
            fireTableDataChanged();
        }

        public void loadMapData(Map<String, String> map) {
            this.map = map;
            arrayOfMap = new String[COL_COUNT][map.size()];
            int i = 0;
            for (String key : map.keySet()) {
                arrayOfMap[0][i] = key;
                arrayOfMap[1][i] = map.get(key);
                i++;
            }
        }
        List<TableModelListener> localListener = new ArrayList<TableModelListener>();

        @Override
        public void addTableModelListener(TableModelListener l) {
            localListener.add(l);
            super.addTableModelListener(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            localListener.remove(l);
            super.removeTableModelListener(l);
        }
    }
}
