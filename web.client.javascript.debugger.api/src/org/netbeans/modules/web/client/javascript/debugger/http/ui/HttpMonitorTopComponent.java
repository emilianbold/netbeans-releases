/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.web.client.javascript.debugger.http.ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JComponent;


import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpActivity;
import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpRequest;
import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpResponse;
import org.netbeans.modules.web.client.javascript.debugger.http.ui.models.HttpActivitiesModel;
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
    static final String ICON_PATH = "org/netbeans/modules/web/client/javascript/debugger/http/ui/resources/HttpMonitor.png";

    private static final String PREFERRED_ID = "HttpMonitorTopComponent";
    
    private HttpMonitorTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(HttpMonitorTopComponent.class, "CTL_HttpMonitorTopComponent"));
        setToolTipText(NbBundle.getMessage(HttpMonitorTopComponent.class, "HINT_HttpMonitorTopComponent"));
        setIcon(Utilities.loadImage(ICON_PATH, true));
    }

    
    private void customInitiallization() {
        List<Model> models = new ArrayList<Model> ();
        models.add( new HttpActivitiesModel ());
        models.add( HttpActivitiesModel.getColumnModel(HttpActivitiesModel.METHOD_COLUMN));
        models.add( HttpActivitiesModel.getColumnModel(HttpActivitiesModel.SENT_COLUMN));
        models.add( HttpActivitiesModel.getColumnModel(HttpActivitiesModel.RESPONSE_COLUMN));
        CompoundModel compoundModel = Models.createCompoundModel(models);
        JComponent tableView = Models.createView (compoundModel);
        activitiesPanel.add(tableView, BorderLayout.CENTER);
        
        assert tableView instanceof ExplorerManager.Provider;
        ExplorerManager activityExplorerManager = ((ExplorerManager.Provider)tableView).getExplorerManager();
        activityExplorerManager.addPropertyChangeListener( new ActivitiesPropertyChange() );
       
    }
    private class ActivitiesPropertyChange implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            if( evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES) ){
                if( reqHeaderTextArea != null ){
                    
                    assert evt.getNewValue() instanceof Node[];
                    Node[] nodes = (Node[])evt.getNewValue();
                    
                    assert nodes[0] instanceof Node;
                    Node aNode = (Node)nodes[0];
                    HttpActivity activity = aNode.getLookup().lookup(HttpActivity.class);
                    if ( activity != null ){
                        HttpRequest request = activity.getRequest();
                        assert request != null;
                        reqHeaderTextArea.setText(request.getHeader().toString());
                        reqParamTextArea.setText(request.getParams());
                        
                        HttpResponse response = activity.getResponse();
                        if( response != null ){
                            resHeaderTextArea.setText(response.getHeader().toString());
                            resBodyTextArea.setText( response.getBody());
                        } else {
                            resHeaderTextArea.setText("");
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
        httpMonitorSplitPane.setDividerLocation(NbPreferences.forModule(HttpMonitorTopComponent.class).getDouble(PREF_HttpMonitorSplitPane_DIVIDERLOC, 0.5));
        detailsSplitPane.setDividerLocation(NbPreferences.forModule(HttpMonitorTopComponent.class).getDouble(PREF_DetailsSplitPane_DIVIDERLOC, 0.5));
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        double dividerLoc1 = httpMonitorSplitPane.getDividerLocation();
        double height = httpMonitorSplitPane.getHeight();
        double dividerLocPorportional1 = dividerLoc1/height;
        NbPreferences.forModule(HttpMonitorTopComponent.class).putDouble(PREF_HttpMonitorSplitPane_DIVIDERLOC, dividerLocPorportional1);
    
        double dividerLoc2 = detailsSplitPane.getDividerLocation();
        double width = detailsSplitPane.getWidth();
        double dividerLocPorportional2 = dividerLoc2/width;
        NbPreferences.forModule(HttpMonitorTopComponent.class).putDouble(PREF_DetailsSplitPane_DIVIDERLOC, dividerLocPorportional2);
    
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        httpMonitorSplitPane = new javax.swing.JSplitPane();
        activitiesPanel = new javax.swing.JPanel();
        detailsPanel = new javax.swing.JPanel();
        detailsSplitPane = new javax.swing.JSplitPane();
        httpReqPanel = new javax.swing.JPanel();
        reqLabel = new javax.swing.JLabel();
        reqTabbedPane = new javax.swing.JTabbedPane();
        reqHeaderPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        reqHeaderTextArea = new javax.swing.JTextArea();
        reqParamPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        reqParamTextArea = new javax.swing.JTextArea();
        httpResPanel = new javax.swing.JPanel();
        resLabel = new javax.swing.JLabel();
        resTabbedPane = new javax.swing.JTabbedPane();
        resHeaderPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resHeaderTextArea = new javax.swing.JTextArea();
        resBodyPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        resBodyTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        httpMonitorSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        activitiesPanel.setLayout(new java.awt.BorderLayout());
        httpMonitorSplitPane.setTopComponent(activitiesPanel);
        customInitiallization();

        detailsPanel.setLayout(new java.awt.BorderLayout());

        httpReqPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(reqLabel, org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.reqLabel.text")); // NOI18N
        httpReqPanel.add(reqLabel, java.awt.BorderLayout.NORTH);

        reqHeaderPanel.setLayout(new java.awt.BorderLayout());

        reqHeaderTextArea.setColumns(20);
        reqHeaderTextArea.setRows(5);
        jScrollPane3.setViewportView(reqHeaderTextArea);

        reqHeaderPanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        reqTabbedPane.addTab(org.openide.util.NbBundle.getMessage(HttpMonitorTopComponent.class, "HttpMonitorTopComponent.reqHeaderPanel.TabConstraints.tabTitle"), reqHeaderPanel); // NOI18N

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

        resHeaderPanel.setLayout(new java.awt.BorderLayout());

        resHeaderTextArea.setColumns(20);
        resHeaderTextArea.setRows(5);
        jScrollPane1.setViewportView(resHeaderTextArea);

        resHeaderPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel activitiesPanel;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JSplitPane detailsSplitPane;
    private javax.swing.JSplitPane httpMonitorSplitPane;
    private javax.swing.JPanel httpReqPanel;
    private javax.swing.JPanel httpResPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel reqHeaderPanel;
    private javax.swing.JTextArea reqHeaderTextArea;
    private javax.swing.JLabel reqLabel;
    private javax.swing.JPanel reqParamPanel;
    private javax.swing.JTextArea reqParamTextArea;
    private javax.swing.JTabbedPane reqTabbedPane;
    private javax.swing.JPanel resBodyPanel;
    private javax.swing.JTextArea resBodyTextArea;
    private javax.swing.JPanel resHeaderPanel;
    private javax.swing.JTextArea resHeaderTextArea;
    private javax.swing.JLabel resLabel;
    private javax.swing.JTabbedPane resTabbedPane;
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

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
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
            return HttpMonitorTopComponent.getDefault();
        }
    }

}
