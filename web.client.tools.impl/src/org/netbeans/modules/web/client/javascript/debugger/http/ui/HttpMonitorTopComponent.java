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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JComponent;


import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpActivity;
import org.netbeans.modules.web.client.javascript.debugger.http.ui.models.HttpActivitiesModel;
import org.netbeans.modules.web.client.javascript.debugger.http.ui.models.HttpHeaderModel;
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
    private static final Model VALUE_COLUMN = HttpHeaderModel.getColumnModel(HttpHeaderModel.VALUE_COLUMN);
    private static final String PREFERRED_ID = "HttpMonitorTopComponent";
    private final static HttpMonitorPreferences httpMonitorPreferences = HttpMonitorPreferences.getInstance();
    private final ActivitiesPropertyChange activityPropertyChangeListener = new ActivitiesPropertyChange();
    private final DebuggerManagerListenerImpl debuggerManagerListener = new DebuggerManagerListenerImpl();

    private final Logger LOG = Logger.getLogger(HttpMonitorTopComponent.class.getName());

    /* Component for main table */
    private JComponent tableView;

    private HttpMonitorTopComponent() {
        LOG.entering(HttpMonitorTopComponent.class.getName(), "constructor");
        
        initComponents();
        
        setName(NbBundle.getMessage(HttpMonitorTopComponent.class, "CTL_HttpMonitorTopComponent"));
        setToolTipText(NbBundle.getMessage(HttpMonitorTopComponent.class, "HINT_HttpMonitorTopComponent"));
        setIcon(Utilities.loadImage(ICON_PATH, true));
        LOG.exiting(HttpMonitorTopComponent.class.getName(), "constructor");

        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        manager.addDebuggerListener(DebuggerManager.PROP_CURRENT_SESSION, debuggerManagerListener);
        manager.addDebuggerListener(DebuggerManager.PROP_SESSIONS, debuggerManagerListener);

//        if (HttpMonitorUtility.getCurrentHttpMonitorModel() != null) {
            HttpMonitorUtility.setEnabled(true);
//        }
    }
    private Icon StartIcon;
    private Icon StopIcon;

    private final Icon getStartStopIcon() {

        Icon retIcon;
        if (HttpMonitorUtility.isEnabled()) {
            retIcon = (StopIcon != null ? StopIcon : new javax.swing.ImageIcon(getClass().getResource(STOP_ICON_PATH)));
        } else {
            retIcon =  (StartIcon != null ? StopIcon : new javax.swing.ImageIcon(getClass().getResource(START_ICON_PATH)));
        }
        return retIcon;
        
    }
    
    
    public ExplorerManager getActivityExplorerManager() {
        assert tableView instanceof ExplorerManager.Provider;
        ExplorerManager activityExplorerManager = ((ExplorerManager.Provider) tableView).getExplorerManager();
        return activityExplorerManager;
    }

    private JComponent createActivitiesTable() {
        CompoundModel compoundModel = createViewCompoundModel(HttpMonitorUtility.getCurrentHttpMonitorModel());
        tableView = Models.createView(compoundModel);
        getActivityExplorerManager().addPropertyChangeListener(activityPropertyChangeListener);
        return tableView;
    }

    private JComponent createResponseView() {
        Model model = new HttpResponseModel(getActivityExplorerManager());
        JComponent component = Models.createView(createHeaderCompoundModel(model));
        return component;
    }

    private JComponent createRequestView() {
        Model model = new HttpRequestModel(getActivityExplorerManager());
        JComponent component = Models.createView(createHeaderCompoundModel(model));
        return component;
    }
    
    private CompoundModel createHeaderCompoundModel (Model model ) {
        List<Model> models = new ArrayList<Model>();
        if (models != null) {
            models.add(model);
            models.add(VALUE_COLUMN);
        }
        return Models.createCompoundModel(models);
        
    }
    
    
    public class HttpResponseModel extends HttpHeaderModel {

        public HttpResponseModel(ExplorerManager activityExplorerManager) {
            super(activityExplorerManager);
        }

        @Override
        protected Map<String, String> getHeader() {
            HttpActivity activity = getSelectedActivity();
            if( activity == null ) {
                return Collections.emptyMap();
            }
            return getSelectedActivity().getResponseHeader();
        }
    }
    
    public class HttpRequestModel extends HttpHeaderModel {

        public HttpRequestModel(ExplorerManager activityExplorerManager) {
            super(activityExplorerManager);
        }

        @Override
        protected Map<String, String> getHeader() {
            HttpActivity activity = getSelectedActivity();
            if( activity == null ) {
                return Collections.emptyMap();
            }
            return getSelectedActivity().getRequestHeader();
        }
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

                    assert evt.getNewValue() instanceof Node[];
                    Node[] nodes = (Node[]) evt.getNewValue();
                    if (nodes == null || nodes.length < 1) {
                        reqParamTextArea.setText("");
                        resBodyEditorPane.setText("");
                        resBodyEditorPane.setContentType("text/html");
                        return;
                    }

                    assert nodes[0] instanceof Node;
                    Node aNode = (Node) nodes[0];
                    HttpActivity activity = aNode.getLookup().lookup(HttpActivity.class);
                    if (activity != null) {
                        JSHttpRequest request = activity.getRequest();
                        assert request != null;
                        if (request.getMethod().equals(JSHttpRequest.MethodType.POST)) {
                            reqParamTextArea.setText("POST: " + request.getPostText());
                        } else {
                            reqParamTextArea.setText("URL PARAMS: " + request.getUrlParams());
                        }
                        
                        Map<String, String> header = activity.getResponseHeader();
                        if (header != null) {
                            String mime = activity.getMimeType();
                            String contentType = "text/html";
                            if( mime != null ) {
                                if( HttpActivitiesModel.JS_CONTENT_TYPES.contains(mime) ){
                                    contentType = "text/javascript";
                                } else if (HttpActivitiesModel.CSS_CONTENT_TYPES.contains(mime)){
                                    contentType = "text/x-css";
                                } else if ( mime.contains("json") ) {
                                    contentType = "text/x-json";
                                }else if ( mime.contains("xml")){
                                    contentType = "text/xml";
                                }
                            }
                            resBodyEditorPane.setContentType(contentType);
                            resBodyEditorPane.setText(activity.getResponseText());
                        } else {
                            resBodyEditorPane.setText("");
                            resBodyEditorPane.setContentType("text/html");
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


    @Override
    protected void componentClosed() {
        LOG.entering(HttpMonitorTopComponent.class.getName(), "componentClosed");
        super.componentClosed();
        HttpMonitorUtility.setHttpMonitorOpened(false);
        instance = null;
        LOG.exiting(HttpMonitorTopComponent.class.getName(), "componentClosed");
    }


    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
    }

    @Override
    protected void componentOpened() {
        LOG.entering(HttpMonitorTopComponent.class.getName(), "componentOpened");
        super.componentOpened();
        HttpMonitorUtility.setHttpMonitorOpened(true);
        LOG.exiting(HttpMonitorTopComponent.class.getName(), "componentOpened");
    }

    private double getHttpMonitorDividerLoc() {
        double d = NbPreferences.forModule(HttpMonitorTopComponent.class).getDouble(PREF_HttpMonitorSplitPane_DIVIDERLOC, 0.5);  
        if ( d >= 1 || d <=0 ){
            NbPreferences.forModule(HttpMonitorTopComponent.class).putDouble(PREF_HttpMonitorSplitPane_DIVIDERLOC, 0.5);
            d = 0.5;
        }
        return d;
    }

    private double getDetailsDividerLoc() {
        double d = NbPreferences.forModule(HttpMonitorTopComponent.class).getDouble(PREF_DetailsSplitPane_DIVIDERLOC, 0.5);
        if ( d >= 1 || d <=0 ){ 
            NbPreferences.forModule(HttpMonitorTopComponent.class).putDouble(PREF_DetailsSplitPane_DIVIDERLOC, 0.5);
            d = 0.5;
        }
        return d;
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
        reqHeaderPanel = new javax.swing.JPanel();
        reqParamPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        reqParamTextArea = new javax.swing.JTextArea();
        httpResPanel = new javax.swing.JPanel();
        resLabel = new javax.swing.JLabel();
        resTabbedPane = new javax.swing.JTabbedPane();
        resHeaderPanel = new javax.swing.JPanel();
        resBodyPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        resBodyEditorPane = new javax.swing.JEditorPane();

        setLayout(new java.awt.BorderLayout());

        httpMonitorSplitPane.setDividerLocation(getDetailsDividerLoc());
        httpMonitorSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        httpMonitorSplitPane.setOneTouchExpandable(true);

        outerActivitiesPanel.setLayout(new java.awt.BorderLayout());

        activitiesToolbar.setFloatable(false);
        activitiesToolbar.setRollover(true);
        activitiesToolbar.setFocusable(false);
        activitiesToolbar.setNextFocusableComponent(activitiesModelPanel);

        start_stopMonitoring.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/Kill.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(start_stopMonitoring, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.start_stopMonitoring.text")); // NOI18N
        start_stopMonitoring.setToolTipText(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "TT_Start_StopMonitoring")); // NOI18N
        start_stopMonitoring.setEnabled(false);
        start_stopMonitoring.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        start_stopMonitoring.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        start_stopMonitoring.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                StartStopButtonHandler(evt);
            }
        });
        activitiesToolbar.add(start_stopMonitoring);
        start_stopMonitoring.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_STOPSTART")); // NOI18N
        start_stopMonitoring.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_STOPSTART")); // NOI18N

        cleanButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/clean.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cleanButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.cleanButton.text")); // NOI18N
        cleanButton.setToolTipText(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_CLEAN_TT")); // NOI18N
        cleanButton.setEnabled(false);
        cleanButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cleanButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cleanButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cleanButtonMouseClicked(evt);
            }
        });
        activitiesToolbar.add(cleanButton);
        cleanButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_CLEAN")); // NOI18N
        cleanButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_CLEAN_TT")); // NOI18N

        activitiesToolbar.add(jSeparator1);

        all_filterButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(all_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.all_filterButton.text")); // NOI18N
        all_filterButton.setToolTipText(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_All_TT")); // NOI18N
        all_filterButton.setEnabled(false);
        all_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        all_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        all_filterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                all_filterMouseClicked(evt);
            }
        });
        activitiesToolbar.add(all_filterButton);
        all_filterButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_All")); // NOI18N
        all_filterButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_All_TT")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(html_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.html_filterButton.text")); // NOI18N
        html_filterButton.setToolTipText(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_HTML_TT")); // NOI18N
        html_filterButton.setEnabled(false);
        html_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        html_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        html_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(html_filterButton);
        html_filterButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_HTML")); // NOI18N
        html_filterButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_HTML_TT")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(js_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.js_filterButton.text")); // NOI18N
        js_filterButton.setToolTipText(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_JS_TT")); // NOI18N
        js_filterButton.setEnabled(false);
        js_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        js_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        js_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(js_filterButton);
        js_filterButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_JS")); // NOI18N
        js_filterButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_JS_TT")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(xhr_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.xhr_filterButton.text")); // NOI18N
        xhr_filterButton.setToolTipText(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_XHR_TT")); // NOI18N
        xhr_filterButton.setEnabled(false);
        xhr_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        xhr_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        xhr_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(xhr_filterButton);
        xhr_filterButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_XHR")); // NOI18N
        xhr_filterButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_XHR_TT")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(css_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.css_filterButton.text")); // NOI18N
        css_filterButton.setToolTipText(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_CSS_TT")); // NOI18N
        css_filterButton.setEnabled(false);
        css_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        css_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        css_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(css_filterButton);
        css_filterButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_CSS")); // NOI18N
        css_filterButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_CSS_TT")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(images_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.images_filterButton.text")); // NOI18N
        images_filterButton.setToolTipText(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_Images_TT")); // NOI18N
        images_filterButton.setEnabled(false);
        images_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        images_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        images_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(images_filterButton);
        images_filterButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_Images")); // NOI18N
        images_filterButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_Images_TT")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(flash_filterButton, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.flash_filterButton.text")); // NOI18N
        flash_filterButton.setToolTipText(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_Flash_TT")); // NOI18N
        flash_filterButton.setEnabled(false);
        flash_filterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        flash_filterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        flash_filterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filterButtonItemStateChanged(evt);
            }
        });
        activitiesToolbar.add(flash_filterButton);
        flash_filterButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_Flash")); // NOI18N
        flash_filterButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "BTN_HTTPToolbar_Filter_Flash_TT")); // NOI18N

        outerActivitiesPanel.add(activitiesToolbar, java.awt.BorderLayout.NORTH);
        Model model = HttpMonitorUtility.getCurrentHttpMonitorModel();
        start_stopMonitoring.setIcon(getStartStopIcon());

        if( httpMonitorPreferences.isShowAll() ) {
            setFilterButtonsAllSelected();
        } else {
            all_filterButton.setSelected(false);
            resetOtherFilterButtonSelected();
        }
        setToolbarButtonsEnabled(model != null);

        activitiesModelPanel.setLayout(new java.awt.BorderLayout());
        activitiesModelPanel.add(createActivitiesTable(), BorderLayout.CENTER);
        outerActivitiesPanel.add(activitiesModelPanel, java.awt.BorderLayout.CENTER);

        httpMonitorSplitPane.setTopComponent(outerActivitiesPanel);

        detailsPanel.setLayout(new java.awt.BorderLayout());

        httpReqPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(reqLabel, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.reqLabel.text")); // NOI18N
        httpReqPanel.add(reqLabel, java.awt.BorderLayout.NORTH);

        reqHeaderPanel.setLayout(new java.awt.BorderLayout());
        reqHeaderPanel.add(createRequestView(), BorderLayout.CENTER);
        reqTabbedPane.addTab(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, " HttpMonitorTopComponent.headerPanel.TabConstraints.tabTitle"), reqHeaderPanel); // NOI18N
        reqHeaderPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, " HttpMonitorTopComponent.reqHeaderPanel.TabConstraints.tabAsc")); // NOI18N
        reqHeaderPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.reqHeader.TabConstraints.tabDescription")); // NOI18N

        reqParamPanel.setName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.reqHeader.TabConstraints.tabTitle")); // NOI18N
        reqParamPanel.setLayout(new java.awt.BorderLayout());

        reqParamTextArea.setColumns(20);
        reqParamTextArea.setEditable(false);
        reqParamTextArea.setRows(5);
        jScrollPane4.setViewportView(reqParamTextArea);

        reqParamPanel.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        reqTabbedPane.addTab(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.reqParamPanel.TabConstraints.tabTitle"), reqParamPanel); // NOI18N
        reqParamPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "TABBED_PANE_Param")); // NOI18N

        httpReqPanel.add(reqTabbedPane, java.awt.BorderLayout.CENTER);

        detailsSplitPane.setLeftComponent(httpReqPanel);

        httpResPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(resLabel, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.resLabel.text")); // NOI18N
        httpResPanel.add(resLabel, java.awt.BorderLayout.NORTH);

        resTabbedPane.setName(""); // NOI18N

        resHeaderPanel.setLayout(new java.awt.BorderLayout());

        resHeaderPanel.add(createResponseView(), BorderLayout.CENTER);

        resTabbedPane.addTab(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.headerPanel.TabConstraints.tabTitle"), resHeaderPanel); // NOI18N
        resHeaderPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.resHeaderPanel.TabConstraints.tabAsc")); // NOI18N
        resHeaderPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.resHeaderPanel.TabConstraints.tabDescription")); // NOI18N

        resBodyPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setViewportView(resBodyEditorPane);

        resBodyPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        resTabbedPane.addTab(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.resBodyPanel.TabConstraints.tabTitle"), resBodyPanel); // NOI18N
        resBodyPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "TABBED_PANE_Body")); // NOI18N

        httpResPanel.add(resTabbedPane, java.awt.BorderLayout.CENTER);

        detailsSplitPane.setRightComponent(httpResPanel);

        detailsPanel.add(detailsSplitPane, java.awt.BorderLayout.CENTER);

        httpMonitorSplitPane.setRightComponent(detailsPanel);

        add(httpMonitorSplitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cleanButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cleanButtonMouseClicked
        LOG.entering(HttpMonitorTopComponent.class.getName(), "cleanButtonMouseClicked");
        HttpActivitiesModel model = HttpMonitorUtility.getCurrentHttpMonitorModel();
        if (model != null) {
            model.clearActivities();
        }
        LOG.exiting(HttpMonitorTopComponent.class.getName(), "cleanButtonMouseClicked");
    }//GEN-LAST:event_cleanButtonMouseClicked

    private void StartStopButtonHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_StartStopButtonHandler
        LOG.entering(HttpMonitorTopComponent.class.getName(), "StartStopButtonHandler", !HttpMonitorUtility.isEnabled());
        HttpMonitorUtility.setEnabled(!HttpMonitorUtility.isEnabled());
        start_stopMonitoring.setIcon(getStartStopIcon());
        LOG.exiting(HttpMonitorTopComponent.class.getName(), "StartStopButtonHandler");
    }//GEN-LAST:event_StartStopButtonHandler

    private void all_filterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_all_filterMouseClicked
        if( !all_filterButton.isSelected() ){
            setOtherFilterButtonsSelected(false);
        } else {
            setOtherFilterButtonsSelected(true);
            all_filterButton.setSelected(true);
        }
    }//GEN-LAST:event_all_filterMouseClicked

    private void filterButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_filterButtonItemStateChanged
        LOG.entering(HttpMonitorTopComponent.class.getName(), "filterButtonItemStateChanged", evt);
        Object source = evt.getItem();
        int state = evt.getStateChange();
        if (source.equals(html_filterButton)) {
            httpMonitorPreferences.setShowHTML(state == ItemEvent.SELECTED);
        } else if (source.equals(js_filterButton)) {
            httpMonitorPreferences.setShowJS(state == ItemEvent.SELECTED);
        } else if (source.equals(xhr_filterButton)) {
            httpMonitorPreferences.setShowXHR(state == ItemEvent.SELECTED);
        } else if (source.equals(css_filterButton)) {
            httpMonitorPreferences.setShowCSS(state == ItemEvent.SELECTED);
        } else if (source.equals(images_filterButton)) {
            httpMonitorPreferences.setShowImages(state == ItemEvent.SELECTED);
        } else if (source.equals(flash_filterButton)) {
            httpMonitorPreferences.setShowFlash(state == ItemEvent.SELECTED);
        } 
        if (httpMonitorPreferences.isShowAll()) {
            all_filterButton.setSelected(true);
        } else {
            all_filterButton.setSelected(false);
        }
        LOG.exiting(HttpMonitorTopComponent.class.getName(), "filterButtonItemStateChanged");
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
    private javax.swing.JPanel reqHeaderPanel;
    private javax.swing.JLabel reqLabel;
    private javax.swing.JPanel reqParamPanel;
    private javax.swing.JTextArea reqParamTextArea;
    private javax.swing.JTabbedPane reqTabbedPane;
    private javax.swing.JEditorPane resBodyEditorPane;
    private javax.swing.JPanel resBodyPanel;
    private javax.swing.JPanel resHeaderPanel;
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
            Object httpMonitor = HttpMonitorTopComponent.getDefault();
            return httpMonitor;
        }
    }

    /* Purpose: to listen to the session and update the model when the current
     * session has changed.
     */
    private class DebuggerManagerListenerImpl extends DebuggerManagerAdapter {
        private HttpActivitiesModel currentModel = null;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            HttpActivitiesModel model = HttpMonitorUtility.getCurrentHttpMonitorModel();
            if (model != null) {
                setToolbarButtonsEnabled(true);
                
            } else {
                setToolbarButtonsEnabled(false);
            }

            if (currentModel != model) {
                currentModel = model;
                resetHttpActivitesModel(model);
            }
        }
    }

    private void setToolbarButtonsEnabled(boolean b) {
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

    private void setFilterButtonsAllSelected() {
        all_filterButton.setSelected(true);
        setOtherFilterButtonsSelected(true);
    }

    private void setOtherFilterButtonsSelected(boolean b_val) {
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

}
