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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.designer.jsf.ui;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerListener;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.designer.jsf.GridHandler;
import org.netbeans.modules.visualweb.designer.jsf.JsfDesignerPreferences;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy;
import org.netbeans.spi.palette.PaletteController;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;
import org.netbeans.modules.visualweb.extension.openide.loaders.SystemFileSystemSupport;
import org.openide.awt.Actions;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ContextAwareAction;
import org.openide.util.actions.Presenter;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;


/**
 * XXX Before as designer/../DesignerTopComp.
 * 
 * This represents the Design View of a page. The actual
 * drawing surface is a DesignerPane, so this class encapsulates
 * everything around that: the scrollbar, focus management,
 * component open/close, save synchronization, etc.
 *
 * @author Tor Norbye
 */
public class JsfTopComponent extends AbstractJsfTopComponent /*SelectionTopComp*/ {

    // XXX Get rid of this.
    private static final boolean SHOW_RENDER_ERRORS = System.getProperty("rave.hideRenderErrors") == null;

//    /** Should we show a Tray (like in Reef 1.0) or not? */
//    static final boolean SHOW_TRAY = (System.getProperty("rave.showTray") != null); // NOI18N

    private static final NavigatorLookupHint NAVIGATOR_HINT = new DesignerNavigatorLookupHint();
    
    private static final NavigatorLookupPanelsPolicy NAVIGATOR_LOOKUP_PANELS_POLICY = new DesignerNavigatorLookupPanelsPolicy();


//    // XXX Get rid of this suspicious impl.
//    private static boolean pendingRefreshAll;

//    private transient boolean initialized;
    private transient JToolBar toolbar;
    private transient boolean needListeners = true;

    // ----- Error Handling -----
    private boolean showingErrors = false;

    /** When we show the error page, take whatever component
     * was showing in the main area and stash it here so we can
     * restore it correctly. */
    private Component hiddenComp = null;

    // ------ Tray handling -----------
//    private boolean trayShowing = false;
//    private JSplitPane splitPanel = null;
//    private Tray tray;
//    long generationSeen = 0;
//    DesignerPane html = null;
    // XXX
    private JComponent html;
    
    // XXX
//    private JComponent html;
    
//    private JScrollPane scroller = null;
//    private boolean showing = false;

    // <multiview>
    private transient MultiViewElementCallback multiViewElementCallback;
//    private JToggleButton showVfButton;
//    private JButton refreshButton;
//    private JButton previewButton;
//    private long lastEscape = -1;

    
//    private final PropertyChangeListener settingsListener = new SettingsListener(this);
    private final PreferenceChangeListener settingsListener = new SettingsListener(this);

    private final PropertyChangeListener activatedNodesListener = new ActivatedNodesListener(this);

    private final DesignerListener designerListener = new JsfDesignerListener(this);
//    private final PaletteController designerPaletteController;
    
    private final JsfLookupProvider jsfLookupProvider/* = new JsfLookupProvider(this)*/; // TEMP

    
    public JsfTopComponent(/*WebForm webform*/ JsfForm jsfForm, Designer designer, DataObject jspDataObject) {
//        super(webform);
        super(jsfForm, designer);

        // XXX Moved to designer/jsf/PaletteControllerFactory.
//        /*
//         * Hack - We have created a dependency on project/jsf to get JsfProjectUtils. 
//         * Ultimately this should be placed into a lookup.
//         */
//        String paletteDirectory;
//        if ( JsfProjectUtils.JAVA_EE_5.equals(JsfProjectUtils.getJ2eePlatformVersion(webform.getProject()))) {
//            paletteDirectory = "CreatorDesignerPalette5";
//        } else {
//            //Later to be renamed with a 1.4
//            paletteDirectory = "CreatorDesignerPalette";
//        }
//        
//        // XXX PaletteController
//        PaletteController controller;
//        try {
//            ComplibService complibService = (ComplibService)Lookup.getDefault().lookup(ComplibService.class);
//            PaletteFilter complibPaletteFilter;
//            if (complibService == null) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new NullPointerException("There is no ComplibService available!")); // NOI18N
//                complibPaletteFilter = null;
//            } else {
//                complibPaletteFilter = complibService.createComplibPaletteFilter(webform.getProject());
//            }
//            controller = PaletteFactory.createPalette(paletteDirectory,
//                    new DesignerPaletteActions(paletteDirectory), complibPaletteFilter, null);
//
//            // XXX #6466711 Listening to changes of complib to refresh the palette.
//            DesignerComplibListener.getDefault().install();
//            DesignerComplibListener.getDefault().setPaletteController(controller);
//        } catch (java.io.IOException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            controller = null;
//        }
//        designerPaletteController = controller;
        
        jsfLookupProvider = new JsfLookupProvider(this, jspDataObject);
        
        setName("Visual Design"); // NOI18N
        setDisplayName(NbBundle.getMessage(JsfTopComponent.class, "LBL_JsfDisplayName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JsfTopComponent.class, "ACSD_DesignerTopComp"));

        initActivatedNodes(jspDataObject);

        if (jsfForm.isValid()) {
            initDesigner();
        } else {
            // XXX Model not available yet.
            initLoadingComponent();
        }
    }

    
    JsfForm getJsfForm() {
        return jsfForm;
    }
    
    Designer getDesigner() {
        return designer;
    }

    
    private void initActivatedNodes(DataObject jspDataObject) {
        // XXX Providing dummy node containing the DataObject in the lookup -> needed by Navigator?!.
        setActivatedNodes(new Node[] { new DummyNode(jspDataObject) });
    }

    
    private static class DummyNode extends AbstractNode {
        public DummyNode(DataObject jspDataObject) {
            super(Children.LEAF, Lookups.fixed(jspDataObject));
        }
    } // End of DummyNode.
    
    
    private void initDesigner() {
        initDesignerComponent();
        initDesignerListeners();
    }
    
    private void initDesignerListeners() {
//        DesignerSettings.getInstance().addPropertyChangeListener(
//                WeakListeners.propertyChange(settingsListener, DesignerSettings.getInstance()));
//        DesignerSettings.getInstance().addWeakPreferenceChangeListener(settingsListener);
//        designer.addWeakPreferenceChangeListener(settingsListener);
        JsfDesignerPreferences.getInstance().addWeakPreferenceChangeListener(settingsListener);
        
        addPropertyChangeListener(WeakListeners.propertyChange(activatedNodesListener, this));
        
        designer.addDesignerListener(WeakListeners.create(DesignerListener.class, designerListener, designer));
    }

    private void initDesignerComponent() {
        removeAll();
        setLayout(new BorderLayout());
        createDesignerPane();

        installActions();

        //#43157 - editor actions need to be accessible from outside using the
        // TopComponent.getLookup(ActionMap.class) call.
        // used in main menu enabling/disabling logic.
        ActionMap am = getActionMap();
//            ActionMap paneMap = html.getActionMap();
        ActionMap paneMap = designer.getPaneActionMap();

        am.setParent(paneMap);
        
        designer.setPaintSizeMask(jsfForm.isFragment() || jsfForm.isPortlet());
    }
    
    private void initLoadingComponent() {
        removeAll();
        setLayout(new BorderLayout());
        add(createLoadingComponent(), BorderLayout.CENTER);
    }
    
    private JComponent createLoadingComponent() {
        return new JLabel(NbBundle.getMessage(JsfTopComponent.class, "LBL_LoadingModel"), JLabel.CENTER); // TEMP
    }
    
    @Override
    protected String preferredID() {
        return "DESIGNER"; //NOI18N
    }

    // Cheating persistence, designer is opened using 'multi_view' hack.
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    @Override
    protected void componentOpened() {
//        if (!initialized) {
//            initialized = true;
////            setLayout(new BorderLayout());
////            createDesignerPane();
////
////            installActions();
////
////            //#43157 - editor actions need to be accessible from outside using the
////            // TopComponent.getLookup(ActionMap.class) call.
////            // used in main menu enabling/disabling logic.
////            ActionMap am = getActionMap();
//////            ActionMap paneMap = html.getActionMap();
////            ActionMap paneMap = designer.getPaneActionMap();
////            
////            am.setParent(paneMap);
//            initComponent();
//        }

//        // The following will also initialize the context listeners,
//        // provided the context is available
//        updateErrors();
//        
//        // Set listening.
////        webform.registerListeners();
//        // XXX
//        designer.registerListeners();
//
////        if (SHOW_TRAY) {
////            refreshTray(hasTrayBeans(), null);
////        }
//
//        openAdditionalWindows();
        if (jsfForm.isValid()) {
            designerOpened();
        }
    }
    
    @Override
    protected void componentClosed() {
//        DesignContext context = webform.getModel().getLiveUnit();
//
//        if ((context != null) && !needListeners) {
//            context.removeDesignContextListener(this);
            
            // XXX Do not recreate the synchronizer (causes errors).
            // TODO Do not recreate synchronizer in getters.
//            if (webform.hasDomSynchronizer()) {
//                webform.getDomSynchronizer().detachContext();
//            }
            
////            webform.detachContext();
//        if (!needListeners) {
//            needListeners = true;
//        }
//        
//        // Stop listening.
////        webform.unregisterListeners();
//        // XXX
//        designer.unregisterListeners();
//
////        webform.clearHtml();
//        jsfForm.clearHtml();
        if (jsfForm.isValid()) {
            designerClosed();
        }
    }
    
    // XXX Bad API, why one needs to override the deprecated API?
    @Override
    public void requestFocus() {
        // XXX TopComponent is not focusable. This call was needless.
//        super.requestFocus();
        
        // XXX see above.
//        DesignerPane designerPane = getDesignerPane();
//        if (designerPane != null) {
//            // XXX NB #90506 Possible NPE.
//            designerPane.requestFocus();
//        }
        designer.paneRequestFocus();
    }

    @Override
    public void requestVisible() {
        if (multiViewElementCallback != null) {
            multiViewElementCallback.requestVisible();
        } else {
            super.requestVisible();
        }
    }

    @Override
    public void requestActive() {
        if (multiViewElementCallback != null) {
            multiViewElementCallback.requestActive();
        } else {
            super.requestActive();
        }
    }

    /**
     * Update the error state for the form - possibly show an error panel or hide one
     * if already showing
     */
    public void updateErrors() {
//        boolean busted = webform.getModel().isBusted();
//        boolean busted = webform.isModelBusted();
        boolean busted = jsfForm.isModelBusted();

        if (!busted && needListeners) {
            needListeners = false;

//            DesignContext context = webform.getModel().getLiveUnit();
//            
//            if(context != null) {
////                webform.getDomSynchronizer().attachContext(context);
//                webform.attachContext(context);
////                context.addDesignContextListener(this);
//            }
//            webform.attachContext();
            jsfForm.attachContext();
        }

//        if (SHOW_RENDER_ERRORS && (webform.hasRenderingErrors() || webform.getHtmlBody(false) == null)) {
//            if (!webform.isRenderFailureShown()) {
        if (SHOW_RENDER_ERRORS && (jsfForm.hasRenderingErrors() || jsfForm.getHtmlBody(false) == null)) {
//            if (!webform.isRenderFailureShown()) {
            if (!jsfForm.isRenderFailureShown()) {
                showErrors(true);
            }
        } else {
            showErrors(busted);
        }
    }

    void showErrors(boolean on) {
        if (on == showingErrors) {
            if (on) {
                Component comp = getComponent(0);

//                if (comp instanceof ErrorPanel) {
//                    ((ErrorPanel)comp).updateErrors();
//                } else {
//                    ((RenderErrorPanel)comp).updateErrors();
//                }
//                if (comp instanceof ErrorPanel) {
//                    ((ErrorPanel)comp).updateErrors();
//                }
                if (comp instanceof JsfForm.ErrorPanel) {
                    ((JsfForm.ErrorPanel)comp).updateErrors();
                }
            }

            return;
        }

        assert SwingUtilities.isEventDispatchThread();

        if (on) {
//            assert getComponentCount() >= 1;
            if (getComponentCount() == 0) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Designer top component is not initialized," +
                        " there is no designer pane or error panel in it")); // NOI18N
                return;
            }
            
            hiddenComp = getComponent(0);
            removeAll();

//            if (webform.getModel().isBusted()) {
//                ErrorPanel errorPanel = new ErrorPanel(webform, webform.getModel().getErrors());
//                add(errorPanel, BorderLayout.CENTER);
//            } else {
//                RenderErrorPanel errorPanel = new RenderErrorPanel(webform);
//                add(errorPanel, BorderLayout.CENTER);
//            }
//            JComponent errorPanel = webform.getErrorPanel();
            JComponent errorPanel = jsfForm.getErrorPanel(new ErrorPanelCallbackImpl(this));
            
            add(errorPanel, BorderLayout.CENTER);
        } else {
            assert getComponentCount() >= 1;
            removeAll();
            assert hiddenComp != null;

            // Add old contents back: could be design scroll pane, or could
            // be split pane showing scrollpane and tray.
            add(hiddenComp, BorderLayout.CENTER);

            // Ensure that the exposed component has dimensions, otherwise
            // it won't receive any paint requests (and I drive layout from
            // the paint request). This can be a problem if you've loaded a
            // project that had errors that you just fixed for example.
            if ((hiddenComp.getWidth() == 0) || (hiddenComp.getHeight() == 0)) {
                hiddenComp.setSize(getWidth(), getHeight());

                // Anticipate the below statement such that if we recurse
                // we bail out
                showingErrors = on;
//                webform.refresh(false);
//                webform.refreshModel(false);
                jsfForm.refreshModel(false);
            }
        }

        showingErrors = on;

        /*
        invalidate();
        if (getParent() != null) {
            getParent().validate();
        }
        */
        revalidate();
        repaint();
    }

//    /** Hide or show the nonvisual component tray in the designer view
//     * @param on When true, show the view, when false hide the view.*/
//    private void refreshTray(final boolean on, final DesignBean highlight) {
//        if (!SHOW_TRAY) {
//            return;
//        }
//
//        if (SwingUtilities.isEventDispatchThread()) {
//            showTrayAWT(on, highlight);
//        } else {
//            SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        showTrayAWT(on, highlight);
//                    }
//                });
//        }
//    }
//
//    /** Return true iff the tray is showing */
//    private boolean isTrayShowing() {
//        return trayShowing;
//    }
//
//    /** Can only be called from the event dispatch thread */
//    private void showTrayAWT(boolean on, DesignBean highlight) {
//        if (!SHOW_TRAY) {
//            return;
//        }
//
//        if (showingErrors) { // Does not apply when errors are showing
//
//            return;
//        }
//
//        if (trayShowing == on) {
//            if (on && (tray != null)) {
//                tray.refresh(highlight);
//
//                Dimension d = tray.getPreferredSize();
//
//                if (tray.getHeight() != d.height) {
//                    splitPanel.resetToPreferredSizes();
//                }
//            }
//
//            return;
//        }
//
//        trayShowing = on;
//
//        if (trayShowing) {
//            // Tray wasn't showing, now should be
//            this.remove(scroller);
//
//            if (splitPanel == null) {
//                splitPanel = new JSplitPane();
//                splitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
//                splitPanel.setResizeWeight(1);
//                splitPanel.setOneTouchExpandable(true);
//
//                // Add tray for nonvisual components
//                tray = new Tray(webform);
//            }
//
//            tray.refresh(highlight);
//            splitPanel.setTopComponent(scroller);
//            splitPanel.setBottomComponent(tray);
//            this.add(splitPanel, BorderLayout.CENTER);
//        } else {
//            // Tray was showing, now shouldn't be
//            this.remove(splitPanel);
//
//            splitPanel.remove(scroller);
//            splitPanel.remove(tray);
//            add(scroller, BorderLayout.CENTER);
//        }
//
//        invalidate();
//
//        if (getParent() != null) {
//            getParent().validate();
//        }
//
//        repaint();
//    }

//    private boolean hasTrayBeans() {
//        if (!SHOW_TRAY) {
//            return false;
//        }
//
//        DesignBean rootbean = webform.getModel().getRootBean();
//
//        if (rootbean == null) {
//            return false;
//        }
//
//        for (int i = 0, n = rootbean.getChildBeanCount(); i < n; i++) {
//            DesignBean bean = rootbean.getChildBean(i);
//
//            if (LiveUnit.isTrayBean(bean)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    Tray getTray() {
//        return tray;
//    }

    // ------ Implements DesignContextListener ---------------------------
//    public void contextActivated(DesignContext context) {
//    }

//    public void contextDeactivated(DesignContext context) {
//    }

//    public void contextChanged(DesignContext context) {
    public void designContextGenerationChanged() {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".contextChanged(DesignContext)");
//        }
//        if(context == null) {
//            throw(new IllegalArgumentException("Null context"));
//        }
////        long currentGeneration = ((LiveUnit)context).getContextGeneration();
//        long currentGeneration = WebForm.getDomProviderService().getContextGenearation(context);
//
//        if (generationSeen == currentGeneration) {
//            return;
//        }
//
//        generationSeen = currentGeneration;

        //webform.getSelection().clearSelection(true);
        // Ensure that the app outline is current before we try to mess
        // with its selection (see 6197251)
//        OutlineTopComp.getInstance().contextChanged(context);

        // XXX #106332 Bad architecture, there were changed beans instanes in the hierarchy,
        // and at this moment the rendered doc is not regenerated.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Caret syncing needs to be delayed until we have updated the rendered dom!
                //webform.getSelection().syncCaret();
        //        webform.getManager().syncSelection(true);
        //        webform.getSelection().syncSelection(true);
                designer.syncSelection(true);

        //        if (SHOW_TRAY) {
        //            // XXX todo sync tray selection!
        //            refreshTray(hasTrayBeans(), null);
        //        }

                if (isShowing()) {
                    updateErrors();
                }
            }
        });
    }

//    public void beanContextActivated(DesignBean designBean) {
//    }

//    public void beanContextDeactivated(DesignBean designBean) {
//    }

//    public void beanCreated(DesignBean bean) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".beanCreated(DesignBean)");
//        }
//        if(bean == null) {
//            throw(new IllegalArgumentException("Null bean"));
//        }
////        if (SHOW_TRAY) {
////            if (LiveUnit.isTrayBean(bean)) {
////                refreshTray(true, bean);
////            }
////        }
//
//        // If the bean corresponds to a DOM element, we'll be
//        // notified from DOM itself when insync adds the
//        // DOM element. So we can ignore this event.
//        // Furthermore, it's difficult to process it here since
//        // we don't know exactly WHERE in the DOM insync is going
//        // to place the element - even if we know it typically is
//        // parented by the <h:form> element, we don't know which
//        // child position it's assigned - and that's important for
//        // static layout (e.g. palette double click instead of
//        // specific drop location).
//        //        Log.err.log("Massive hack in beanCreated!");
//        //        webform.getDocument().notifyDomChanged(true);
//    }

//    public void beanDeleted(DesignBean bean) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".beandeleted(DesignBean)");
//        }
//        if(bean == null) {
//            throw(new IllegalArgumentException("Null bean"));
//        }
////        if (SHOW_TRAY) {
////            if (LiveUnit.isTrayBean(bean)) {
////                refreshTray(hasTrayBeans(), null);
////            }
////        }
//
//        // If the bean corresponds to a DOM element, we'll be
//        // notified from DOM itself when insync deletes the
//        // DOM element. So we can ignore this event.
//        //        Log.err.log("Massive hack in beanDeleted!");
//        //        webform.getDocument().notifyDomChanged(true);
//    }

//    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {
//    }

//    public void beanChanged(DesignBean bean) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".beanChanged(DesignBean)");
//        }
//        if(bean == null) {
//            throw(new IllegalArgumentException("Null bean"));
//        }
////        if (SHOW_TRAY) {
////            if (LiveUnit.isTrayBean(bean)) {
////                DesignBean added = ((tray != null) && tray.isBlinking()) ? bean : null;
////                refreshTray(hasTrayBeans(), added);
////            }
////        }
//    }

//    public void beanMoved(DesignBean lbean, DesignBean oldParent,
//        Position pos) {
//        // XXX What do we do here?
//        // Probably nothing, since we'll be notified of manipulations
//        // to the DOM itself separately via the DOM listeners
//    }

//    public void propertyChanged(final DesignProperty prop, Object oldValue) {
////        DesignBean bean = prop.getDesignBean();
//
////        if (SHOW_TRAY) {
////            if (LiveUnit.isTrayBean(bean)) {
////                DesignBean added = ((tray != null) && tray.isBlinking()) ? bean : null;
////                refreshTray(hasTrayBeans(), added);
////            }
////        }
//    }

//    public void eventChanged(DesignEvent event) {
//        // XXX What do we do here?
//    }

//    public void selectNonTrayBeans(DesignBean[] beans) {
//        // TODO -- blink the Application Outline?
////        OutlineTopComp.getInstance().selectBeans(beans);
//
////        if (SHOW_TRAY) {
////            // The above doesn't set the activated nodes etc. so we do that here
////            ArrayList nodes = new ArrayList(beans.length);
////
////            for (int i = 0, n = beans.length; i < n; i++) {
////                DesignBeanNode node = (DesignBeanNode)DesigntimeIdeBridgeProvider.getDefault().getNodeRepresentation(beans[i]);
////                node.setDataObject(webform.getDataObject());
////                nodes.add(node);
////            }
////
////            Node[] nds = (Node[])nodes.toArray(new Node[nodes.size()]);
////
////            DesignerUtils.setActivatedNodes(this, nds);
////
////            if (isShowing()) { // Make sure tray has focus
////                requestActive();
////            }
////        } else {
////            OutlineTopComp.getInstance().requestActive();
////        }
//    }
    
//    private static void openNewOutline() {
//        TopComponent newOutline = findNewOutline();
//        if (newOutline != null && !newOutline.isOpened()) {
//            // FIXME XXX Just for now to fake the old kind of behaviour. Use TopComponentGroup to fix it.
//            newOutline.open();
//        }
//    }
//    
//    private static void selectNewOutline() {
//        TopComponent newOutline = findNewOutline();
//        if (newOutline != null) {
//            // XXX #6392131 -> NB #62329.
//            if (!Boolean.TRUE.equals(newOutline.getClientProperty("isSliding"))) { // NOI18N
//                // FIXME XXX Just for now to fake the old kind of behaviour. Use TopComponentGroup to fix it.
//                newOutline.requestVisible();
//            }
//        }
//    }
//    
//    private static TopComponent findNewOutline() {
//        TopComponent newOutline = WindowManager.getDefault().findTopComponent("outlineNew");
//        if (newOutline == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("" +
//                    "New Outline TopComponent not found!")); // NOI18N
//        }
//        
//        return newOutline;
//    }
    
//    // XXX Implement TopComponentGroup.
//    private static void openNavigator() {
//        TopComponent navigator = findNavigator();
//        if (navigator != null && !navigator.isOpened()) {
//            // FIXME XXX Just for now to fake the old kind of behaviour. Use TopComponentGroup to fix it.
//            navigator.open();
//        }
//    }
    
//    private static void selectNavigator() {
//        TopComponent navigator = findNavigator();
//        if (navigator != null) {
//            // XXX #6392131 -> NB #62329.
//            if (!Boolean.TRUE.equals(navigator.getClientProperty("isSliding"))) { // NOI18N
//                // FIXME XXX Just for now to fake the old kind of behaviour. Use TopComponentGroup to fix it.
//                navigator.requestVisible();
//            }
//        }
//    }
    
//    private static TopComponent findNavigator() {
//        TopComponent navigator = WindowManager.getDefault().findTopComponent("navigatorTC"); // NOI18N
//        if (navigator == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new NullPointerException("Navigator TopComponent not found!")); // NOI18N
//        }
//        
//        return navigator;
//    }

//    /** Select the given bean in the tray, if it's present.
//     * @param bean The bean you want selected
//     */
//    public void selectTrayBean(DesignBean bean) {
//        if (!SHOW_TRAY || (tray == null)) {
//            return;
//        }
//
//        requestActive();
//        tray.setSelection(bean);
//    }
//
//    /** Clear the selection in the tray */
//    public void clearTraySelection() {
//        if (tray != null) {
//            tray.clearSelection(false);
//        }
//    }

//    /** Return the data object being edited by this view
//     * @return Data object being edited
//     */
//    public DataObject getDataObject() {
//        return webform.getDataObject();
//    }

//    public String getName() {
//        return NbBundle.getMessage(DesignerTopComp.class, "FormView"); // NOI18N
//    }

//    // XXX unused?
//    protected String iconResource() {
//        return "org/netbeans/modules/visualweb/designer/resources/designer.gif"; // NOI18N
//    }

    @Override
    public HelpCtx getHelpCtx() {
        Node[] activatedNodes = getActivatedNodes();
        Node[] nodes = activatedNodes == null ? new Node[0] : activatedNodes;
        return ExplorerUtils.getHelpCtx(
                nodes,
                new HelpCtx("projrave_ui_elements_editors_about_visual_editor")); // NOI18N
    }

    /* Activates copy/cut/paste actions.
    */
    @Override
    protected void componentActivated() {
////        OutlineTopComp.getInstance().setCurrent(webform);
////        OutlineTopComp.getInstance().requestVisible();
////        selectNewOutline();
//        // XXX Replace with TopComponentGroup.
//        selectNavigator();
//        
////        webform.getSelection().updateNodes();
//        designer.updateSelectedNodes();
//        
//        super.componentActivated();
//
////        if (html != null) {
////            html.requestFocus();
////        }
//        designer.paneRequestFocus();
        if (jsfForm.isValid()) {
            designerActivated();
        }
    }
    
//    public PaletteController getPaletteController() {
//        return designerPaletteController;
//    }
    // XXX
    public JComponent getPane() {
        return html;
    }

//    private void sync() {
//        FacesModel model = webform.getModel();
//
//        if (model == null) {
//            return;
//        }
//
//        /*
//        MarkupUnit markup = model.getMarkupUnit();
//        Unit.State oldState = Unit.State.SOURCEDIRTY;
//        if (markup != null) {
//            oldState = markup.getState();
//        }
//        */
//        model.sync();
//
//        /* This was an attempt to preparse the style strings,
//           so that we get error messages for them at sync time - which
//           in turn would mean they could affect whether or not an error page
//           is shown instead of the design view. Still needs some work.
//        markup = model.getMarkup();  // may have changed by syncFromDoc call
//        if (!model.hasErrors() && oldState == Unit.State.SOURCEDIRTY) {
//            model.flushToDoc(false); // if modified
//            try {
//                System.out.println("OLDSTATE=" + oldState);
//                System.out.println("NEWSTATE=" + markup.getState());
//                preparseStyles(markup, markup.getDocument());
//            } catch (org.w3c.css.sac.CSSParseException ex) {
//                //ex.printStackTrace(); // XXX remove!
//            }
//        }
//        */
//    }

    /**
     * CSS styles for elements were initialized lazily. The problem with
     * that is that I don't know if I have a fatal CSS error until I try
     * to render the page. Since we'll need the CSS styles on virtually
     * all the elements anyway, there's no benefit to the lazily initialization
     * to do it here instead.
     */

    /*
    private void preparseStyles(MarkupUnit markup, org.w3c.dom.Node node) {
        if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            Element element = (Element)node;
            markup.getEffectiveStyles(element); // caches result
        }

        org.w3c.dom.NodeList list = node.getChildNodes();
        int len = list.getLength();
        for (int i = 0; i < len; i++) {
            org.w3c.dom.Node child = (org.w3c.dom.Node) list.item(i);
            preparseStyles(markup, child);
        }
    }
    */
    private void createDesignerPane() {
//        sync();
//        webform.syncModel();
        jsfForm.syncModel();

//        Document doc = webform.getDocument();
//        html = new DesignerPane(webform, doc);
//        html = new DesignerPane(webform);
        html = designer.createPaneComponent();

        // Defaults are sensible now so no need to set:
        //html.setDragEnabled(true);
        //html.setContentType(DesignerPane.MIME_TYPE);
        //html.setEditable(true);
        JScrollPane scrollPane = new JScrollPane();

        JViewport vp = scrollPane.getViewport();
        vp.add(html);
        add(scrollPane, BorderLayout.CENTER);
//        html.updateViewport();
        designer.updatePaneViewPort();
    }

//    public DesignerPane getDesignerPane() {
//        return html;
//    }

    
//    /** XXX Set pending refdresh all state - whether we have a pending refresh all that needs to be
//     * processed whenever forms are made visible */
//    public static void setPendingRefreshAll() {
//        pendingRefreshAll = true;
//    }

    @Override
    protected void componentShowing() {
//        // PROJECTTODO: Why is componentShowing occuring before componentOpened, not sure
//        // THIS is a HACK !EAT
//        if (!initialized) {
//            componentOpened();
//        }

        // XXX It was here only because of incorrect impl of old rave window system.
//        if (showing) {
//            return;
//        }
//
//        showing = true;

//        // XXX
//        if (pendingRefreshAll) {
////            Project project = webform.getProject();
////
////            // XXX uh oh, what if I refresh THIS project but not other projects.... and
////            // you edit stylesheets from different projects? Notagood! Do I really need to
////            // refresh ALL projects?
////            if (project != null) {
////                WebForm.refreshAll(project, false);
////
////                // Prevent refreshing all for every update since a refresh could be
////                // sort of expensive. (It doesn't actually update layout on all pages,
////                // but does scan the entire project for pages and clears associated caches
////                // etc.
////            }
//            webform.refreshProject();
//
//            pendingRefreshAll = false;
//        } else 
//        if (webform.isFragment()) {
//        if (jsfForm.isFragment()) {
//            // Ensure that we have current page references
////            webform.refresh(false);
////            webform.refreshModel(false);
//            jsfForm.refreshModel(false);
//        }
//
////        openNewOutline();
//        // XXX Replace with TopComponentGroup.
//        openNavigator();
//        selectAndUpdateOutlineView();
//
//        // XXX #6314795 See below, also this one should be scheduled later.
//        // Remove scheduling when the NB issue fixed.
//        
//        //Removing toolbox from view
////        SwingUtilities.invokeLater(new Runnable() {
////                public void run() {
//////                    // #5047873 Select also Toolbox (with palette component).
//////                    com.sun.rave.toolbox.ToolBox.findDefault().requestVisible();
////                    TopComponent toolbox = WindowManager.getDefault().findTopComponent("toolbox"); // NOI18N
////                    toolbox.requestVisible();
////                }
////            });
//
////        if (SHOW_TRAY) {
////            clearTraySelection();
////        }
//
//        // Insync: parse the backing file such that we incorporate
//        // user changes in the backing file (and don't blow them away
//        // in componentHidden when we later flush the model to the doc)
//        //final DataObject dobj = webform.getDataObject();
////        FacesModel model = webform.getModel();
////
////        try {
////        MarkupUnit markupUnit = model.getMarkupUnit();
////        if (markupUnit != null) {
////            if ((markupUnit.getState() == Unit.State.SOURCEDIRTY)
////        if (webform.isSourceDirty()
////            && (webform.getPane().getCaret() != null)) {
////        if (webform.isSourceDirty() && (webform.getPane().hasCaret())) {
//            if (jsfForm.isSourceDirty() && designer.hasPaneCaret()) {
//                // Remove the caret if we do a sync since the caret could be
//                // pointing into an old DOM which causes various ugliness
////                webform.getPane().setCaretPosition(Position.NONE);
////                webform.getPane().setCaretPosition(DomPosition.NONE);
////                webform.getPane().setCaretDot(DomPosition.NONE);
//                designer.setPaneCaret(DomPosition.NONE);
//            }
//
////            sync();
////            webform.syncModel();
//            jsfForm.syncModel();
//            
//            // XXX #6474723 If sync was alredy synced we need to assure rebuild the boxes.
//            // FIXME If sync() also rebuilds the model then this is redundant. The boxes
//            // will be rebuild twice. Consider suspending listening during sync() call above.
////            webform.getPane().getPaneUI().resetPageBox();
//            designer.resetPanePageBox();
//
//            // It's not enough to do showErrors and showTray in contextChanged
//            // because contextChanged is not run when the form is first opened.
//            // XXX perhaps I can do this in componentOpened instead?
//            updateErrors(); // XXX shouldn't the contextChanged ensure this?
//
//            //if (SHOW_TRAY) {
//            //    showTray(hasTrayBeans()); The contextChanged should ensure this!
//            //}
////        } else {
////            // XXX #6478973 Model could be corrupted, until #6480764 is fixed.
////            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
////                    new IllegalStateException("The FacesModel is corrupted, its markup unit is null, facesModel=" + model)); // NOI18N
////        }
//        
////        } catch (Exception e) {
////            e.printStackTrace();
////
////            // swallow
////            // temporary hack which allows me to deal with documents
////            // outside of projects
////        }
//
//        // Refresh layout for fragments and for pages that contain fragments whenever
//        // they are exposed
////        if (webform.isFragment() || webform.getDocument().hasCachedFrameBoxes()) {
////        if (webform.isFragment() || webform.hasCachedFrameBoxes()) {
//            if (jsfForm.isFragment() || jsfForm.hasCachedExternalFrames()) {
//            // Always refresh fragments on expose since whenever they are
//            // rendered as part of other (including) documents those documents
//            // may style our elements and stash box references on the elements
//            // that point to their own box hierarchies
////            if (html.getPageBox() != null) {
////                html.getPageBox().redoLayout(true);
////            }
//                designer.redoPaneLayout(true);
//        }
//
////        webform.setGridMode(html.getDocument().isGridMode());
////        webform.updateGridMode();
////        designer.updateGridMode();
//            designer.setPaneGrid(jsfForm.isGridMode());
//
//        // We cannot set the caret to the document position yet; we need
//        // to do layout before the mapper functions work... This is done
//        // after page layout instead.
////        html.requestFocus();
//        designer.paneRequestFocus();
        if (jsfForm.isValid()) {
            designerShowing();
        }
    }
    
    @Override
    protected void componentHidden() {
        // XXX It was here only because of incorrect impl of old rave window system.
//        if (!showing) {
//            // For some reason, I'll often get componentHidden
//            // before any componentShowing events. Suppress these.
//            return;
//        }
//
//        showing = false;

        // Now redundant, see the StatusDisplayer.setStatusText javadoc.
//        StatusDisplayer.getDefault().setStatusText("");

        // <TEMP>
        // Just workaround: #5015428
        // Otherwise it could access document with lock from CloneableEditorSupport.
        /*
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                webform.getModel().flush(false, null);
            }
        });*/
        // </TEMP>
//        selectNavigatorWindow();
        if (jsfForm.isValid()) {
            designerHidden();
        }
    }
    
    public CloseOperationState canCloseElement() {
        // XXX I don't understand this yet. Copied from FormDesigner.
        // Returns a placeholder state - to be sure our CloseHandler is called.
        return MultiViewFactory.createUnsafeCloseState("ID_CLOSING_DESIGNER", // dummy ID // NOI18N
            MultiViewFactory.NOOP_CLOSE_ACTION, MultiViewFactory.NOOP_CLOSE_ACTION);
    }
    
//    /** 
//     * Close this designer window - including the JSP and Java tabs.
//     * This will redirect the close to the containing multiview top component.
//     * Calling the normal this.close() doesn't do this. (It appears
//     * to do nothing.)
//     */
//    boolean forceClose() {
//        if (multiViewElementCallback == null || multiViewElementCallback.getTopComponent() == null)
//            return true;
//        return ((CloneableTopComponent)multiViewElementCallback.getTopComponent()).close();
//    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewElementCallback = callback;

        // TODO XXX What the heck of hack is this? Why for deserialization (comes from FormDesigner).
        // Is something like that needed here too? Is this call part of deserialization routine?
        // WebForm wf = getWebForm();
        // if(wf == null) {
        //     return;
        // }
        // DataObject dObj = wf.getDataObject();
        // if(dObj instanceof JSFDataObject) {
        //     JSFEditorSupport jsfEditorSupport = (JSFEditorSupport)dObj.getCookie(JSFEditorSupport.class);
//        if (jsfEditorSupportAccessor != null) {
//            // this is used (or misused?) to obtain the deserialized multiview
//            // topcomponent and set it to FormEditorSupport
//            jsfEditorSupportAccessor.setMultiView((CloneableTopComponent)multiViewElementCallback.getTopComponent());
//        }

        //}
    }

    private static final String PATH_TOOLBAR_FOLDER = "Designer/application/x-designer/Toolbars/Default"; // NOI18N

    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            // TODO -- Look at NbEditorToolBar in the editor - it does stuff
            // with the UI to get better Aqua and Linux toolbar
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorder(new EmptyBorder(0, 0, 0, 0));

//            ToolbarListener listener = new ToolbarListener();

            toolbar.addSeparator();
//            previewButton =
//                new JButton(new ImageIcon(Utilities.loadImage("org/netbeans/modules/visualweb/designer/preview.png"))); // NOI18N
//            previewButton.addActionListener(listener);
//            previewButton.setToolTipText(NbBundle.getMessage(DesignerTopComp.class, "PreviewAction"));
//            toolbar.add(previewButton);
            // XXX TODO For now adding only BrowserPreviewAction, but later all of them.
//            Component[] comps = ToolBarInstancesProvider.getDefault().getToolbarComponentsForDesignerComponent(this);
            Action[] actions = SystemFileSystemSupport.getActions(PATH_TOOLBAR_FOLDER);
            Lookup context = getLookup();
            for (int i = 0; i < actions.length; i++) {
                Action action = actions[i];
                if (action == null) {
                    toolbar.addSeparator();
                } else {
                    if (action instanceof ContextAwareAction) {
                        Action contextAwareAction = ((ContextAwareAction)action).createContextAwareInstance(context);
                        if (contextAwareAction != null) {
                            action = contextAwareAction;
                        }
                    }
                    if (action instanceof Presenter.Toolbar) {
                        Component tbp = ((Presenter.Toolbar)action).getToolbarPresenter();
                        toolbar.add(tbp);
                    } else {
//                        toolbar.add(new Actions.ToolbarButton((Action)action));
                        JButton toolbarButton = new JButton();
                        Actions.connect(toolbarButton, (Action)action);
                        toolbar.add(toolbarButton);
                    }
                }
            }

//            refreshButton =
//                new JButton(new ImageIcon(Utilities.loadImage("org/netbeans/modules/visualweb/designer/refresh.png"))); // NOI18N
//            refreshButton.addActionListener(listener);
//            refreshButton.setToolTipText(NbBundle.getMessage(DesignerTopComp.class, "Refresh"));
//            toolbar.add(refreshButton);

//            showVfButton =
//                new JToggleButton(new ImageIcon(Utilities.loadImage("org/netbeans/modules/visualweb/designer/vf.png")), // NOI18N
//                    webform.getVirtualFormSupport().isEnabled());
//            showVfButton.addActionListener(listener);
//            showVfButton.setToolTipText(NbBundle.getMessage(DesignerTopComp.class, "ShowVF"));
//            toolbar.add(showVfButton);

//            toolbar.addSeparator();

//            toolbar.add(new TargetSizeCombo(webform));
            
            // Perhaps we could add a snap to grid toggle button here...
            // And value binding feedback stuff?
            // DEBUGGING SUPPORT
            //import java.awt.image.BufferedImage;
            //import org.openide.awt.HtmlBrowser.URLDisplayer;
            //import javax.imageio.ImageIO;
            //import javax.swing.CellRendererPane;
            //import javax.swing.JFrame;
            //import org.openide.windows.WindowManager;
            //import org.netbeans.modules.visualweb.css2.PageBox;
            //import java.io.File;
            //import java.awt.Graphics2D;
            //import java.net.URL;
            //import java.net.MalformedURLException;
            //import org.openide.ErrorManager;
            //import java.io.IOException;
            //            if (System.getProperty("designer.debug") != null) {
            //                toolbar.add(new AbstractAction("ShowLayout", null) {
            //                    public void actionPerformed(ActionEvent actionEvent) {
            //                        PageBox pageBox = webform.getSelection().getPageBox();
            //                        StringBuffer sb = new StringBuffer();
            //                        pageBox.printLayout(sb);
            //                        org.openide.DialogDisplayer.getDefault().notify(new org.openide.NotifyDescriptor.Message(new javax.swing.JScrollPane(new javax.swing.JTextArea(sb.toString()))));
            //
            //                    }
            //                });
            //
            //                toolbar.add(new AbstractAction("ShowHtml", null) {
            //                    public void actionPerformed(ActionEvent actionEvent) {
            //                        org.w3c.dom.Node node = webform.getDom().getRoot();
            //                        String s = FacesSupport.getHtmlStream(node);
            //                        org.openide.DialogDisplayer.getDefault().notify(new org.openide.NotifyDescriptor.Message(new javax.swing.JScrollPane(new javax.swing.JTextArea(s))));
            //                    }
            //                });
            //
            //                toolbar.add(new AbstractAction("SaveImage", null) {
            //                    public void actionPerformed(ActionEvent actionEvent) {
            //                        PageBox pageBox = webform.getSelection().getPageBox();
            //                        BufferedImage img2 = paintImage(pageBox);
            //                        showScreenshot(img2);
            //                    }
            //                    protected BufferedImage paintImage(PageBox pageBox) {
            //                        int w = pageBox.getWidth();
            //                        int h = pageBox.getHeight();
            //                        BufferedImage image = new BufferedImage(w, h,
            //                                                                BufferedImage.TYPE_INT_RGB);
            //                        Graphics2D g2 = image.createGraphics();
            //                        DesignerPane.clip.setBounds(0, 0, w, h);
            //                        DesignerPane.clipBr.x = w;
            //                        DesignerPane.clipBr.y = h;
            //                        pageBox.paint(g2);
            //                        g2.dispose();
            //                        return image;
            //                    }
            //                    protected void showScreenshot(BufferedImage bi) {
            //                        try {
            //                            File tmp = File.createTempFile("designer", ".png");
            //                            tmp.deleteOnExit();
            //                            saveImage(bi, tmp);
            //                            showScreenshot(tmp);
            //                        } catch (java.io.IOException ioe) {
            //                            ErrorManager.getDefault().notify(ioe);
            //                        }
            //                    }
            //
            //                    /** Save the given image to disk */
            //                    protected void saveImage(BufferedImage image, File file) {
            //                        try {
            //                            if (file.exists()) {
            //                                file.delete();
            //                            }
            //                            ImageIO.write(image, "png", file);
            //                        } catch (IOException e) {
            //                            System.err.println(e);
            //                        }
            //                    }
            //
            //                    protected void showScreenshot(File file) {
            //                        URL url;
            //                        try {
            //                            url = new URL("file:" + file.getPath()); // NOI18N
            //                        } catch (MalformedURLException e) {
            //                            // Can't show URL
            //                            ErrorManager.getDefault().notify(e);
            //                            return;
            //                        }
            //                        URLDisplayer.getDefault().showURL(url);
            //                    }
            //
            //                });
            //            }
        }

        return toolbar;
    }

    public JComponent getVisualRepresentation() {
        return this;
    }

    // </multiview>
    // Extends SelectionTopComp
    @Override
    protected void installActions() {
        super.installActions();

        ActionMap map = getActionMap();
        
        InputMap keys = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        
//        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape-multiplex"); // NOI18N
////        map.put("escape-multiplex", // NOI18N
////            new AbstractAction() {
////                public void actionPerformed(ActionEvent evt) {
////                    escape(evt.getWhen());
////                }
////            }
////        );
//        map.put("escape-multiplex", new EscapeAction(this)); // NOI18N

        keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);    
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "edit-value"); // NOI18N
//        map.put("edit-value", // NOI18N
//            new AbstractAction() {
//                public void actionPerformed(ActionEvent evt) {
////                    if (webform.getManager().isInlineEditing()) {
//                    if (designer.isInlineEditing()) {
//                        // Already inline editing - no point doing anything
//                        return;
//                    }
//
////                    boolean success = webform.getActions().editValue();
//                    boolean success = editValue();
//
//                    if (!success) {
////                        UIManager.getLookAndFeel().provideErrorFeedback(DesignerTopComp.this); // beep
//                        UIManager.getLookAndFeel().provideErrorFeedback(JsfTopComponent.this); // beep
//                    }
//                }
//            });
        map.put("edit-value", new EditValueAction(this)); // NOI18N
            
        // >>>Debugging helpers
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "rave-designer-dump-html"); // NOI18N
//        map.put("rave-designer-dump-html", // NOI18N
//            new AbstractAction() {
//                public void actionPerformed(ActionEvent evt) {
//                    dumpActivatedMarkupDesignBeansHtml();
//                }
//            }
//        );
        map.put("rave-designer-dump-html", new DumpHtmlAction(this)); // NOI18N
        
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "rave-designer-dump-boxes"); // NOI18N
//        map.put("rave-designer-dump-boxes", // NOI18N
//            new AbstractAction() {
//                public void actionPerformed(ActionEvent evt) {
//                    dumpActivatedComponentCssBoxes();
//                }
//            }
//        );
        map.put("rave-designer-dump-boxes", new DumpBoxesAction(this)); // NOI18N
        // <<< Debugging helpers
    }

    /** XXX Moved from DesignerActions.
     * Edit the selected component's default inline edited property */
    private boolean editValue() {
//        SelectionManager sm = webform.getSelection();
//        ModelViewMapper mapper = webform.getMapper();
        Element[] componentRootElements = designer.getSelectedComponents();
        
//        if (sm.getNumSelected() == 1) {
        if (componentRootElements.length == 1) {
//            Iterator it = sm.iterator();
//
//            while (it.hasNext()) {
//                MarkupDesignBean bean = (MarkupDesignBean)it.next();
//            for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
            for (Element componentRootElement : componentRootElements) {
//                MarkupDesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
//                if (bean == null) {
//                    continue;
//                }
                if (componentRootElement == null) {
                    continue;
                }
                
//                CssBox box = mapper.findBox(bean);
//                CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);

//                webform.getTopComponent().requestActive();
                requestActive();

//                return webform.getManager().startInlineEditing(bean, null, box, true, true, null, false);
//                return webform.getManager().startInlineEditing(componentRootElement, null, box, true, true, null, false);
                designer.startInlineEditing(componentRootElement, null);
            }
        }

        return false;
    }
    
//    /** Horrible hack necessary because I get notified of the escape key
//     * twice: sometimes by my key handler above, sometimes by the default key
//     * handler, and sometimes by both!
//     * @todo This may no longer be an issue now that I'm using
//     *   a better input map (ANCESTOR_OF.... instead of FOCUSED_)
//     */
//    public boolean seenEscape(long when) {
//        if (lastEscape == when) {
//            return true;
//        }
//
//        lastEscape = when;
//
//        return false;
//    }
//
//    private void escape(long when) {
//        // Do escape-like things: cancel drag & drop, select parent, etc.
//        if (!seenEscape(when)) {
////            webform.getManager().getMouseHandler().escape();
//            designer.performEscape();
//        }
//    }
    
    private void dumpActivatedMarkupDesignBeansHtml() {
        Node[] nodes = getActivatedNodes();
        if (nodes == null) {
            return;
        }
        
        for (Node node : nodes) {
            if (node == null) {
                continue;
            }
//            DesignBean designBean = (DesignBean)node.getLookup().lookup(DesignBean.class);
//            if (designBean instanceof MarkupDesignBean) {
//                MarkupDesignBean markupDesignBean = (MarkupDesignBean)designBean;
//                Element sourceElement = markupDesignBean.getElement();
//                Element renderedElement = MarkupService.getRenderedElementForElement(sourceElement);
//                if (renderedElement == null || sourceElement == renderedElement) {
//                    System.err.println("\nMarkup design bean not renderable, markup design bean=" + markupDesignBean); // NOI18N
//                    dumpHtmlMarkupDesignBeanHtml();
//                    continue;
//                }
//                System.err.println("\nRendered markup design bean=" + markupDesignBean); // NOI18N
////                System.err.println(Util.getHtmlStream(renderedElement));
//                System.err.println(WebForm.getDomProviderService().getHtmlStream(renderedElement));
//            } else {
//                System.err.println("\nDesign bean not renderable, design bean=" + designBean); // NOI18N
//                dumpHtmlMarkupDesignBeanHtml();
//            }
//            webform.dumpHtmlMarkupForNode(node);
            jsfForm.dumpHtmlMarkupForNode(node);
        }
    }
    
//    private void dumpHtmlMarkupDesignBeanHtml() {
//        DocumentFragment df = webform.getHtmlDomFragment();
////        Element html = Util.findDescendant(HtmlTag.HTML.name, df);
//        Element html = WebForm.getDomProviderService().findHtmlElementDescendant(df);
//        if (html == null) {
//            return;
//        }
////        System.err.println("\nRendered html element markup design bean=" + MarkupUnit.getMarkupDesignBeanForElement(html)); // NOI18N
//        System.err.println("\nRendered html element markup design bean=" + WebForm.getDomProviderService().getMarkupDesignBeanForElement(html)); // NOI18N
////        System.err.println(Util.getHtmlStream(html)); // NOI18N
//        System.err.println(WebForm.getDomProviderService().getHtmlStream(html)); // NOI18N
//    }
    
    private void dumpActivatedComponentCssBoxes() {
        Node[] nodes = getActivatedNodes();
        if (nodes == null) {
            return;
        }
        
        for (Node node : nodes) {
            if (node == null) {
                continue;
            }
//            DesignBean designBean = (DesignBean)node.getLookup().lookup(DesignBean.class);
//            Element componentRootElement = WebForm.getDomProviderService().getComponentRootElementFromNode(node);
            Element componentRootElement = JsfSupportUtilities.getComponentRootElementFromNode(node);
                    
//            if (designBean == null) {
            if (componentRootElement == null) {
                log("Node doesn't have design bean, node=" + node); // NOI18N
                dumpRootComponentCssBoxes();
            } else {
//                PageBox pageBox = webform.getPane().getPageBox();
//                if (pageBox == null) {
//                    return;
//                }
////                CssBox cssBox = ModelViewMapper.findBox(pageBox, designBean);
//                CssBox cssBox = ModelViewMapper.findBoxForComponentRootElement(pageBox, componentRootElement);
                Box box = designer.findBoxForComponentRootElement(componentRootElement);
                
//                if (cssBox == null) {
                if (box == null) {
                    log("Component doesn't have a corresponding css box, componentRootElement=" + componentRootElement); // NOI18N
                    dumpRootComponentCssBoxes();
                } else {
                    log("Css boxes for componentRootElement=" + componentRootElement); // NOI18N
//                    cssBox.list(System.err, 0);
                    box.list(System.err, 0);
                }
            }
        }
    }
    
    private void dumpRootComponentCssBoxes() {
//        DocumentFragment df = webform.getHtmlDomFragment();
        DocumentFragment df = jsfForm.getHtmlDomFragment();
        
//        Element html = Util.findDescendant(HtmlTag.HTML.name, df);
//        Element html = WebForm.getDomProviderService().findHtmlElementDescendant(df);
        Element html = JsfSupportUtilities.findHtmlElementDescendant(df);
        if (html == null) {
            html = jsfForm.getHtmlBody(false);
        }
//        PageBox pageBox = webform.getPane().getPageBox();
        Box pageBox = designer.getPageBox();
        if (pageBox == null) {
            return;
        }
//        System.err.println("\nCss boxes for html element markup design bean=" + MarkupUnit.getMarkupDesignBeanForElement(html));
//        System.err.println("\nCss boxes for html element markup design bean=" + WebForm.getDomProviderService().getMarkupDesignBeanForElement(html));
        log("CssBoxes for element=" + html);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(new BufferedWriter(stringWriter));
        pageBox.list(writer, 0);
        writer.flush();
        log(stringWriter.toString());
        writer.close();
    }

    protected void showKeyboardPopup() {
// <actions from layers>
//        if (isTrayShowing() && !tray.isSelectionEmpty()) {
//            tray.showPopup(null);
//        } else {
//            webform.getActions().createPopup();
//        }
// ====
        Point point = getSelectionPoint();
        showPopup(null, null, point.x, point.y);
// </actions from layers>
    }

// <actions from layers>
    private static final String PATH_DESIGNER_ACTIONS = "Designer/application/x-designer/Popup"; // NOI18N
    
//    public void showPopupMenu(int x, int y) {
//        JPopupMenu popupMenu = Utilities.actionsToPopup(
//                SystemFileSystemSupport.getActions(PATH_DESIGNER_ACTIONS),
//                getLookup());
//        if (isShowing()) {
//            // #6473708 x, y values are computed to the DesignerPane (html).
////            popupMenu.show(this, x, y);
//            popupMenu.show(html, x, y);
//        }
//    }
    
    public void showPopup(Action[] actions, Lookup context, int x, int y) {
        if (actions == null) {
            actions = SystemFileSystemSupport.getActions(PATH_DESIGNER_ACTIONS);
        }
        if (context == null) {
            context = getLookup();
        }
        JPopupMenu popupMenu = Utilities.actionsToPopup(actions, context);
        
        if (isShowing()) {
            popupMenu.show(this, x, y);
        }
    }
    
    private Point getSelectionPoint() {
//        SelectionManager sm = webform.getSelection();
//
//        if (!sm.isSelectionEmpty()) {
        if (designer.getSelectedCount() > 0) {
            Element primarySelection = designer.getPrimarySelection();
//            if (sm.getPrimary() != null) {
            if (primarySelection != null) {
//                CssBox box = webform.getMapper().findBox(sm.getPrimary());
//                CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), sm.getPrimary());
                Box box = designer.findBoxForComponentRootElement(primarySelection);

                while ((box != null) &&
                        ((box.getX() == CssValue.AUTO) || (box.getY() == CssValue.AUTO) ||
                        (box.getX() == Box.UNINITIALIZED) ||
                        (box.getY() == Box.UNINITIALIZED))) {
                    box = box.getParent();
                }

                if (box != null) {
                    return new Point(box.getAbsoluteX(), box.getAbsoluteY());
                }
            }

//            Iterator it = sm.iterator();
//
//            while (it.hasNext()) {
//                DesignBean bean = (DesignBean)it.next();
//            for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
            for (Element componentRootElement : designer.getSelectedComponents()) {
//                DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
//
//                if (bean != null) {
                if (componentRootElement != null) {
//                    CssBox box = webform.getMapper().findBox(bean);
//                    CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);
                    Box box = designer.findBoxForComponentRootElement(componentRootElement);
                    while ((box != null) &&
                            ((box.getX() == CssValue.AUTO) || (box.getY() == CssValue.AUTO) ||
                            (box.getX() == Box.UNINITIALIZED) ||
                            (box.getY() == Box.UNINITIALIZED))) {
                        box = box.getParent();
                    }

                    if (box != null) {
//                        Element e;
//                        if (bean instanceof MarkupDesignBean) {
//                            e = ((MarkupDesignBean)bean).getElement();
//                        } else {
//                            e = null;
//                        }

                        return new Point(box.getAbsoluteX(), box.getAbsoluteY());
                    }
                }
            }
        }

        // Just return near the top left corner
        return new Point(10, 10);
    }
// </actions from layer>

//    protected boolean isSelectionEmpty() {
////        return webform.getSelection().isSelectionEmpty();
//        return designer.getSelectedCount() == 0;
//    }

//    protected DesignBean getPasteParent() {
    public /*protected*/ Element getPasteParentComponent() {
//        DesignBean lb = webform.getSelection().getSelectedContainer();
//        Element componentRootElement = webform.getSelection().getSelectedContainer();
        Element componentRootElement = designer.getSelectedContainer();
//        DesignBean lb = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);

//        if (lb == null) {
        if (componentRootElement == null) {
//            FacesPageUnit facesUnit = webform.getModel().getFacesUnit();
//            MarkupBean formBean = facesUnit.getDefaultParent();
//            lb = webform.getModel().getLiveUnit().getDesignBean(formBean);
//            lb = webform.getDefaultParentBean();
            
//            Element sourceElement = webform.getDefaultParentMarkupBeanElement();
//            componentRootElement = MarkupService.getRenderedElementForElement(sourceElement);
//            componentRootElement = webform.getDefaultParentComponent();
            componentRootElement = jsfForm.getDefaultParentComponent();
        }

//        return lb;
        return componentRootElement;
    }

//    protected MarkupPosition getPasteMarkupPosition() {
//        return null;
//    }

//    protected void selectBeans(DesignBean[] beans) {
    public /*protected*/ void selectComponents(Element[] componentRootElements) {
//        SelectionManager selectionManager = webform.getSelection();
////        MarkupDesignBean[] markupDesignBeans = selectionManager.getSelectedMarkupDesignBeans();
//        Element[] selectedComponentRootElements = selectionManager.getSelectedComponentRootElements();
        Element[] selectedComponentRootElements = designer.getSelectedComponents();
                
//        List<DesignBean> beansToSelect = Arrays.asList(beans);
        List<Element> componentsToSelect = Arrays.asList(componentRootElements);
//        List<MarkupDesignBean> selectedBeans = Arrays.asList(markupDesignBeans);
//        List<MarkupDesignBean> selectedBeans = new ArrayList<MarkupDesignBean>();
//        for (Element componentRootElement : selectedComponentRootElements) {
//            selectedBeans.add(WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement));
//        }
        List<Element> selectedComponents = Arrays.asList(selectedComponentRootElements);
        
        // XXX Compare the content, the order is irrelevant.
//        if (beansToSelect.containsAll(selectedBeans)
//        && selectedBeans.containsAll(beansToSelect)) {
//            return;
//        }
        if (componentsToSelect.containsAll(selectedComponents)
        && selectedComponents.containsAll(componentsToSelect)) {
            return;
        }

//        selectionManager.selectComponents(beans, true);
//        selectionManager.selectComponents(componentRootElements, true);
        designer.setSelectedComponents(componentRootElements, true);
    }

    protected Transferable copy() {
        Node[] nodes = getActivatedNodes();

        if ((nodes == null) || (nodes.length == 0)) {
            return null;
        }

//        ArrayList list = new ArrayList(nodes.length);
        List<Element> componentRootElements = new ArrayList<Element>();

//        for (int i = 0; i < nodes.length; i++) {
//            if (nodes[i] instanceof DesignBeanNode) {
//                // XXX todo: make sure you don't copy the
//                // document bean!!!!!
//                DesignBeanNode node = (DesignBeanNode)nodes[i];
//                DesignBean bean = node.getDesignBean();
//
////                if (!FacesSupport.isSpecialBean(/*webform, */bean)) {
//                if (!Util.isSpecialBean(bean)) {
//                    list.add(bean);
//                }
//            }
//        }
        for (Node node : nodes) {
            if (node == null) {
                continue;
            }
//            DesignBean designBean = (DesignBean)node.getLookup().lookup(DesignBean.class);
//            if (designBean == null) {
//                continue;
//            }
//            Element componentRootElement = WebForm.getDomProviderService().getComponentRootElementFromNode(node);
            Element componentRootElement = JsfSupportUtilities.getComponentRootElementFromNode(node);
            if (componentRootElement == null) {
                continue;
            }
//            if (!Util.isSpecialBean(designBean)) {
//            if (designBean instanceof MarkupDesignBean && !WebForm.getDomProviderService().isSpecialComponent(
//                    WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean))) {
//            if (!WebForm.getDomProviderService().isSpecialComponent(componentRootElement)) {
            if (!JsfSupportUtilities.isSpecialComponent(componentRootElement)) {
//                list.add(designBean);
                componentRootElements.add(componentRootElement);
            }
        }

        // Ensure that no components in the list are a child of any
        // other
//        removeChildrenBeans(list);
        removeChildrenComponents(componentRootElements);

//        DesignBean[] beans = (DesignBean[])list.toArray(new DesignBean[list.size()]);
//        LiveUnit unit = webform.getModel().getLiveUnit();
//        Transferable t = unit.copyBeans(beans);
//        Transferable t = webform.copyBeans(beans);
//        Transferable t = webform.copyComponents(componentRootElements.toArray(new Element[componentRootElements.size()]));
        Transferable t = jsfForm.copyComponents(componentRootElements.toArray(new Element[componentRootElements.size()]));

        return t;
    }

    /** Remove the currently selected components */
    public void deleteSelection() {
//        Node[] nodes = getActivatedNodes();
//
//        if ((nodes == null) || (nodes.length == 0)) {
//            return;
//        }
//
//        // Do this AFTER we obtain the activated nodes - clearing
//        // the selection changes the activated nodes!
////        webform.getSelection().clearSelection(true);
//        designer.clearSelection(true);
//
////        FacesModel model = webform.getModel();
////        Document doc = webform.getDocument();
//
//////        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(SelectionTopComp.class, "DeleteSelection")); // NOI18N
////        DomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(SelectionTopComp.class, "DeleteSelection")); // NOI18N
////        try {
////            doc.writeLock(NbBundle.getMessage(SelectionTopComp.class, "DeleteSelection")); // NOI18N
//
////            for (int i = 0; i < nodes.length; i++) {
////                if (nodes[i] instanceof DesignBeanNode) {
////                    // XXX todo: make sure you don't delete the
////                    // document bean!!!!!
////                    DesignBeanNode node = (DesignBeanNode)nodes[i];
////                    DesignBean bean = node.getDesignBean();
////
//////                    if (!FacesSupport.isSpecialBean(/*webform, */bean)) {
////                    if (!Util.isSpecialBean(bean)) {
////                        model.getLiveUnit().deleteBean(bean);
////                    }
////                }
////            }
//            List<Element> componentRootElements = new ArrayList<Element>();
//            for (Node node : nodes) {
//                if (node == null) {
//                    continue;
//                }
////                DesignBean designBean = (DesignBean)node.getLookup().lookup(DesignBean.class);
////                if (designBean == null) {
////                    continue;
////                }
////                Element componentRootElement = WebForm.getDomProviderService().getComponentRootElementFromNode(node);
//                Element componentRootElement = JsfSupportUtilities.getComponentRootElementFromNode(node);
//                if (componentRootElement == null) {
//                    continue;
//                }
////                if (!Util.isSpecialBean(designBean)) {
////                if (designBean instanceof MarkupDesignBean && !WebForm.getDomProviderService().isSpecialComponent(
////                        WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean))) {
////                if (!WebForm.getDomProviderService().isSpecialComponent(componentRootElement)) {
//////                    webform.getModel().getLiveUnit().deleteBean(designBean);
//////                    webform.deleteBean(designBean);
////                    webform.deleteComponent(componentRootElement);
////                }
//                componentRootElements.add(componentRootElement);
//            }
////            webform.getDomDocument().deleteComponents(componentRootElements.toArray(new Element[componentRootElements.size()]));
//            jsfForm.deleteComponents(componentRootElements.toArray(new Element[componentRootElements.size()]));
////        } finally {
//////            doc.writeUnlock();
//////            webform.getModel().writeUnlock(undoEvent);
////            webform.writeUnlock(writeLock);
////        }
        Element[] componentRootElements = designer.getSelectedComponents();
        if (componentRootElements.length > 0) {
            designer.clearSelection(true);
            jsfForm.deleteComponents(componentRootElements);
        }
    }

    // XXX Public because implementing multiview.

    /* Deactivates copy/cut/paste actions.
    */
    @Override
    protected void componentDeactivated() {
//        super.componentDeactivated();
//
////        if (webform.hasSelection()) {
////            webform.getManager().finishInlineEditing(false);
////        }
//        if (designer.getSelectedCount() > 0) {
//            designer.finishInlineEditing(false);
//        }
        if (jsfForm.isValid()) {
            designerDeactivated();
        }
    }
    
    // Helper methods

//    /** Opens palette(toolbox), properties, outline and navigator windows. */
//    private static void openAdditionalWindows() {
//        // XXX #6314795 Until fixed NB #62975, schedule it later,
//        // it allows winsys to load all the modes. When NB issue fixed,
//        // remove the scheduling.
//        SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    doOpenAdditionalWindows();
//                }
//            });
//    }

//    private static void doOpenAdditionalWindows() {
//        // XXX TODO Define TC group, and use that, not this way.
//        
//        WindowManager wm = WindowManager.getDefault();
////        TopComponent palette = wm.findTopComponent("toolbox"); // NOI18N
////
////        if (palette != null) {
////            palette.open();
////        }
//
////        TopComponent properties = wm.findTopComponent("properties"); // NOI18N
////        if (properties != null && !properties.isOpened()) {
////            properties.open();
////        }
//
////        TopComponent dynamicHelp = wm.findTopComponent("dynamicHelp"); // NOI18N
////        if (dynamicHelp != null && !dynamicHelp.isOpened()) {
////            dynamicHelp.open();
////        }
//        
////        TopComponent outline = wm.findTopComponent("outline"); // NOI18N
////        if (outline != null && !outline.isOpened()) {
////            outline.open();
////        }
//
////        TopComponent navigator = wm.findTopComponent("navigatorTC"); // NOI18N
////        if (navigator != null && !navigator.isOpened()) {
////            navigator.open();
////        }
//    }

//    private void selectAndUpdateOutlineView() {
////        OutlineTopComp.getInstance().setCurrent(webform);
////        OutlineTopComp.getInstance().requestVisible();
////        selectNewOutline();
//        // XXX Replace with TopComponentGroup.
//        selectNavigator();
//
//        // #6318513 It doesn't keep selection expanded, when switching.
////        WebForm wf = webform;
////
////        if (wf != null) {
////            // XXX Updates outline selection!
////            wf.getSelection().updateSelection();
////        }
////        designer.updateSelection();
//        designer.updateSelectedNodes();
//    }

//    /** Selects the navigator window. */
//    private static void selectNavigatorWindow() {
//        TopComponent navigator = WindowManager.getDefault().findTopComponent("navigatorTC"); // NOI18N
//
//        if (navigator != null) {
//            // XXX #6392131 -> NB #62329.
//            if (!Boolean.TRUE.equals(navigator.getClientProperty("isSliding"))) { // NOI18N
//                navigator.requestVisible();
//            }
//        }
//    }

    /** Weak reference to the lookup. */
    private WeakReference<Lookup> lookupWRef = new WeakReference<Lookup>(null);

    /** Adds <code>NavigatorLookupHint</code> into the original lookup,
     * for the navigator. */
    @Override
    public Lookup getLookup() {
        Lookup lookup = lookupWRef.get();
                
        if (lookup == null) {
            Lookup superLookup = super.getLookup();

//            // XXX Needed in order to close the component automatically by project close.
////            DataObject jspDataObject = webform.getJspDataObject();
//            DataObject jspDataObject = jsfForm.getJspDataObject();
//
////            PaletteController jsfPaletteController = webform.getPaletteController();
//            PaletteController jsfPaletteController = jsfForm.getPaletteController();
//            
//            if (jsfPaletteController == null) {
//                lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.fixed(new Object[] {jspDataObject, NAVIGATOR_HINT})});
//            } else {
//                lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.fixed(new Object[] {jspDataObject, NAVIGATOR_HINT, jsfPaletteController})});
//            }
            lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.proxy(jsfLookupProvider)});
            
            lookupWRef = new WeakReference<Lookup>(lookup);
        }
        
        return lookup;
    }
    
    
//    private class ToolbarListener implements ActionListener {
//        /** Action to switch to selection, connection or add mode. */
//        public void actionPerformed(ActionEvent ev) {
//            Object source = ev.getSource();
//
//            if (source == showVfButton) {
//                webform.getVirtualFormSupport().setEnabled(showVfButton.isSelected());
//                webform.getPane().repaint();
//            } else if (source == refreshButton) {
//                webform.getActions().refresh(true);
//            } else if (source == previewButton) {
//                new BrowserPreview(webform).preview();
//            }
//        }
//    }
    
    
    /** Listens on <code>DesignerSettings</code> changes. */
//    private static class SettingsListener implements PropertyChangeListener {
    private static class SettingsListener implements PreferenceChangeListener {
        
//        private final DesignerTopComp designerTC;
        private final JsfTopComponent designerTC;
        
//        public SettingsListener(DesignerTopComp designerTC) {
        public SettingsListener(JsfTopComponent designerTC) {
            this.designerTC = designerTC;
        }
        
//        public void propertyChange(PropertyChangeEvent evt) {
        public void preferenceChange(final PreferenceChangeEvent evt) {
            // XXX #112708 Prefernce change seems to be fired incorrectly
            // from other than event dispatch thread.
            if (EventQueue.isDispatchThread()) {
                doPreferenceChange(evt);
            } else {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        doPreferenceChange(evt);
                    }
                });
            }
        }
        
        private void doPreferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            // XXX #6488437 Workaround for the cases the component is still held in the memory.
//            if (!designerTC.isOpened()) {
            if (!designerTC.isShowing()) {
                return;
            }
//            if(DesignerSettings.PROP_PAGE_SIZE.equals(key)) {
//            if(Designer.PROP_PAGE_SIZE.equals(key)) {
            if(JsfDesignerPreferences.PROP_PAGE_SIZE.equals(key)) {
                // There should be a cleaner way to request the revalidation,
                // why the standard API doesn't work?
                // XXX #6486462 Possible NPE.
//                DesignerPane designerPane = designerTC.getDesignerPane();
//                PageBox pageBox = designerPane == null ? null : designerPane.getPageBox();
//                if (pageBox != null) {
//                    pageBox.redoLayout(true);
//                }
                designerTC.designer.redoPaneLayout(true);
                
                designerTC.repaint();
//            } else if (DesignerSettings.PROP_GRID_SHOW.equals(key)) {
//            } else if (Designer.PROP_GRID_SHOW.equals(key)) {
            } else if (JsfDesignerPreferences.PROP_GRID_SHOW.equals(key)) {
                GridHandler.getDefault().setGrid(JsfDesignerPreferences.getInstance().getGridShow());
                designerTC.repaint();
//            } else if (DesignerSettings.PROP_GRID_SNAP.equals(key)) {
//            } else if (Designer.PROP_GRID_SNAP.equals(key)) {
            } else if (JsfDesignerPreferences.PROP_GRID_SNAP.equals(key)) {
                GridHandler.getDefault().setSnap(JsfDesignerPreferences.getInstance().getGridSnap());
                designerTC.repaint();
//            } else if (DesignerSettings.PROP_GRID_WIDTH.equals(key)) {
//            } else if (Designer.PROP_GRID_WIDTH.equals(key)) {
            } else if (JsfDesignerPreferences.PROP_GRID_WIDTH.equals(key)) {
                GridHandler.getDefault().setGridWidth(JsfDesignerPreferences.getInstance().getGridWidth());
                designerTC.repaint();
//            } else if (DesignerSettings.PROP_GRID_HEIGHT.equals(key)) {
//            } else if (Designer.PROP_GRID_HEIGHT.equals(key)) {
            } else if (JsfDesignerPreferences.PROP_GRID_HEIGHT.equals(key)) {
                GridHandler.getDefault().setGridHeight(JsfDesignerPreferences.getInstance().getGridHeight());
                designerTC.repaint();
//            } else if (DesignerSettings.PROP_DEFAULT_FONT_SIZE.equals(key)) {
//            } else if (Designer.PROP_DEFAULT_FONT_SIZE.equals(key)) {
            } else if (JsfDesignerPreferences.PROP_DEFAULT_FONT_SIZE.equals(key)) {
//                designerTC.getWebForm().getPane().getPaneUI().resetPageBox();
                designerTC.designer.resetPanePageBox();
                designerTC.repaint();
            } else if (JsfDesignerPreferences.PROP_SHOW_DECORATIONS.equals(key)) {
                designerTC.repaint();
            }
        }
    } // End of SettingsListener.
    
    
    /** Listener on activatedNodes property, to get the notifications from outside world,
     * concretely when set the nodes according to the outline component. */
    private static class ActivatedNodesListener implements PropertyChangeListener {
        
//        private final DesignerTopComp designerTC;
        private final JsfTopComponent designerTC;
        
//        public ActivatedNodesListener(DesignerTopComp designerTC) {
        public ActivatedNodesListener(JsfTopComponent designerTC) {
            this.designerTC = designerTC;
        }
        
        
        public void propertyChange(PropertyChangeEvent evt) {
            if("activatedNodes".equals(evt.getPropertyName())) { // NOI18N
                Node[] activatedNodes = (Node[])evt.getNewValue();
//                DesignBean[] beans = getBeansForNodes(activatedNodes);
                Element[] componentRootElements = getComponentsForNodes(activatedNodes);
                
                // TODO There are still some callbacks fromo and to the old outline which 
                // disrupt this to work nicelly, but for now it should be OK,
                // it needs to be finally fixed when old outline removed.
//                designerTC.selectBeans(beans);
                designerTC.selectComponents(componentRootElements);
            }
        }
    } // End of ActivatedNodesListener.
    
//    private static DesignBean[] getBeansForNodes(Node[] nodes) {
    private static Element[] getComponentsForNodes(Node[] nodes) {
        if (nodes == null) {
//            return new DesignBean[0];
            return new Element[0];
        }

//        List beans = new ArrayList();
        List<Element> components = new ArrayList<Element>();
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
//            DesignBean bean = (DesignBean)node.getLookup().lookup(DesignBean.class);
//            if (bean == null) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new NullPointerException("No DesignBean for node=" + node)); // NOI18N
//                continue;
//            }
//            Element componentRootElement = WebForm.getDomProviderService().getComponentRootElementFromNode(node);
            Element componentRootElement = JsfSupportUtilities.getComponentRootElementFromNode(node);
//            beans.add(bean);
            if (componentRootElement != null) {
                components.add(componentRootElement);
            }
        }
        
//        return (DesignBean[])beans.toArray(new DesignBean[beans.size()]);
        return components.toArray(new Element[components.size()]);
    }
    
  
    // XXX Moved to designer/jsf/PaletteControllerFactory
//    private static class DesignerComplibListener implements ComplibListener {
//        private static DesignerComplibListener INSTANCE = new DesignerComplibListener();
//
//        private WeakReference<PaletteController> paletteControllerWRef = new WeakReference<PaletteController>(null);
//        
//        private boolean installed;
//        
//        public static DesignerComplibListener getDefault() {
//            return INSTANCE;
//        }
//        
//        public void install() {
//            if (installed) {
//                return;
//            }
//            ComplibService complibService = getComplibService();
//            if (complibService == null) {
//                return;
//            }
//            complibService.addComplibListener(this);
//            installed = true;
//        }
//        
//        public void uninstall() {
//            ComplibService complibService = getComplibService();
//            if (complibService == null) {
//                return;
//            }
//            complibService.removeComplibListener(this);
//            installed = false;
//        }
//        
//        public void setPaletteController(PaletteController paletteController) {
//            paletteControllerWRef = new WeakReference<PaletteController>(paletteController);
//        }
//        
//        public void paletteChanged(ComplibEvent evt) {
//            PaletteController paletteController = paletteControllerWRef.get();
//            if (paletteController == null) {
//                return;
//            }
//            paletteController.refresh();
//        }
//        
//        private static ComplibService getComplibService() {
//            return (ComplibService)Lookup.getDefault().lookup(ComplibService.class);
//        }
//    } // End of DesignerComplibListener
    
    private static class ErrorPanelCallbackImpl implements JsfForm.ErrorPanelCallback {
//        private final WebForm webForm;
//        private final JsfForm jsfForm;
//        private final Designer designer;
        private final JsfTopComponent jsfTC;
        
        public ErrorPanelCallbackImpl(/*WebForm webForm*/JsfTopComponent jsfTC) {
//            this.webForm = webForm;
            this.jsfTC = jsfTC;
        }
        
        public void updateTopComponentForErrors() {
//            webForm.getTopComponent().updateErrors();
            jsfTC.updateErrors();
        }

        public void setRenderFailureShown(boolean shown) {
//            webForm.setRenderFailureShown(shown);
            jsfTC.jsfForm.setRenderFailureShown(shown);
        }

//        public Exception getRenderFailure() {
//            return webForm.getRenderFailure();
//        }
//
//        public MarkupDesignBean getRenderFailureComponent() {
//            return webForm.getRenderFailureComponent();
//        }

        public void handleRefresh(boolean showErrors) {
            // Continue from the error panel to the designview
//            webForm.getTopComponent().showErrors(showErrors);
            jsfTC.showErrors(showErrors);
            // 6274302: See if the user has cleared the error
//            webform.refresh(true);
//            webForm.refreshModel(true);
            jsfTC.jsfForm.refreshModel(true);
        }
    } // End of ErrorPanelCallbackImpl.

    
//    public DataObject getJspDataObject() {
//        return jsfForm.getJspDataObject();
//    }
    
    
    // JSF notifications >>>
    public void modelChanged() {
        designer.resetPanePageBox();
    }
    
    public void modelRefreshed() {
        designer.resetAll();
    }
    
    public void nodeChanged(org.w3c.dom.Node node, org.w3c.dom.Node parent, Element[] changedElements) {
        designer.changeNode(node, parent, changedElements);
    }
    
    public void nodeRemoved(org.w3c.dom.Node node, org.w3c.dom.Node parent) {
        designer.removeNode(node, parent);
    }
    
    public void nodeInserted(org.w3c.dom.Node node, org.w3c.dom.Node parent) {
        designer.insertNode(node, parent);
    }
    
    public void gridModeUpdated(boolean gridMode) {
        designer.setPaneGrid(gridMode);
    }
    
    public void documentReplaced() {
        designer.detachDomDocument();
    }
    
    public void showDropMatch(Element componentRootElement, Element regionElement, int dropType) {
        designer.showDropMatch(componentRootElement, regionElement, dropType);
    }
    
    public void clearDropMatch() {
        designer.clearDropMatch();
    }
    
    public void selectComponent(Element componentRootElement) {
        // XXX Get rid of delayed.
        designer.selectComponentDelayed(componentRootElement);
    }
    
    public void inlineEditComponents(Element[] componentRootElements) {
        requestActive();
        designer.inlineEditComponents(componentRootElements);
    }
    // JSF notifications <<<
    
    
//    private static class JsfDesignerListener implements DesignerListener {
//        private final JsfTopComponent jsfTopComponent;
//        
//        public JsfDesignerListener(JsfTopComponent jsfTopComponent) {
//            this.jsfTopComponent = jsfTopComponent;
//        }
//    } // End of JsfDesignerListener.
    
    TopComponent getMultiViewTopComponent() {
        MultiViewElementCallback callback = multiViewElementCallback;
        if (callback == null) {
            return null;
        }
        return callback.getTopComponent();
    }
    
    void closeMultiView() {
        TopComponent multiView = getMultiViewTopComponent();
        if (multiView == null) {
            return;
        }
        multiView.close();
    }
    
    boolean isSelectedInMultiView() {
        MultiViewElementCallback callback = multiViewElementCallback;
        return callback == null ? false : callback.isSelectedElement();
    }
    

    private abstract static class JsfTopComponentAction extends AbstractAction {
        private final JsfTopComponent jsfTopComponent;
        
        public JsfTopComponentAction(JsfTopComponent jsfTopComponent) {
            this.jsfTopComponent = jsfTopComponent;
        }
        
        protected JsfTopComponent getJsfTopComponent() {
            return jsfTopComponent;
        }
    } // End of JsfTopComponentAction.
    
//    private static class EscapeAction extends JsfTopComponentAction {
//        public EscapeAction(JsfTopComponent jsfTopComponent) {
//            super(jsfTopComponent);
//        }
//        public void actionPerformed(ActionEvent evt) {
//            getJsfTopComponent().escape(evt.getWhen());
//        }
//    } // End of EscapeAction.
    
    private static class EditValueAction extends JsfTopComponentAction {
        public EditValueAction(JsfTopComponent jsfTopComponent) {
            super(jsfTopComponent);
        }

        public void actionPerformed(ActionEvent evt) {
            JsfTopComponent jsfTopComponent = getJsfTopComponent();
            if (jsfTopComponent.designer.isInlineEditing()) {
                // Already inline editing - no point doing anything
                return;
            }

            boolean success = jsfTopComponent.editValue();

            if (!success) {
                UIManager.getLookAndFeel().provideErrorFeedback(jsfTopComponent); // beep
            }
        }
        
    } // End of EditValueAction.
    
    private static class DumpHtmlAction extends JsfTopComponentAction {
        public DumpHtmlAction(JsfTopComponent jsfTopComponent) {
            super(jsfTopComponent);
        }

        public void actionPerformed(ActionEvent e) {
            getJsfTopComponent().dumpActivatedMarkupDesignBeansHtml();
        }
    } // End of DumpHtmlAction.
    
    private static class DumpBoxesAction extends JsfTopComponentAction {
        public DumpBoxesAction(JsfTopComponent jsfTopComponent) {
            super(jsfTopComponent);
        }

        public void actionPerformed(ActionEvent e) {
            getJsfTopComponent().dumpActivatedComponentCssBoxes();
        }
    } // End of DumpBoxesAction.
    
    
    private static class DesignerNavigatorLookupHint implements NavigatorLookupHint {
        public String getContentType() {
            // TODO Find out nice MIME type.
            return "application/x-designer"; // NOI18N
        }
    } // End of DesignerNavigatorLookupHint.
    
    
    @Override
    protected void designerActivated() {
//        // XXX Replace with TopComponentGroup.
//        selectNavigator();
        
        designer.updateSelectedNodes();
        
        super.designerActivated();
        
        designer.paneRequestFocus();
    }

    @Override
    protected void designerDeactivated() {
        super.designerDeactivated();

        if (designer.getSelectedCount() > 0) {
            designer.finishInlineEditing(false);
        }
    }

    private void designerOpened() {
//        // The following will also initialize the context listeners,
//        // provided the context is available
//        updateErrors();
//        
////        // Set listening.
////        // XXX
////        designer.registerListeners();
        
//        // XXX Implement TopComponentGroup.
//        openAdditionalWindows();
        // No op.
    }

    private void designerClosed() {
        if (!needListeners) {
            needListeners = true;
        }
        
//        // Stop listening.
//        // XXX
//        designer.unregisterListeners();

        jsfForm.clearHtml();
    }

    private void designerShowing() {
        if (jsfForm.isFragment()) {
            jsfForm.refreshModel(false);
        }

//        // XXX Replace with TopComponentGroup.
//        openNavigator();
//        selectAndUpdateOutlineView();

        // XXX #6314795 See below, also this one should be scheduled later.
        // Remove scheduling when the NB issue fixed.

        // Insync: parse the backing file such that we incorporate
        // user changes in the backing file (and don't blow them away
        // in componentHidden when we later flush the model to the doc)
        //final DataObject dobj = webform.getDataObject();
        if (jsfForm.isSourceDirty() && designer.hasPaneCaret()) {
            // Remove the caret if we do a sync since the caret could be
            // pointing into an old DOM which causes various ugliness
            designer.setPaneCaret(DomPosition.NONE);
        }

        jsfForm.syncModel();

        // XXX #6474723 If sync was alredy synced we need to assure rebuild the boxes.
        // FIXME If sync() also rebuilds the model then this is redundant. The boxes
        // will be rebuild twice. Consider suspending listening during sync() call above.
        designer.resetPanePageBox();

        // It's not enough to do showErrors and showTray in contextChanged
        // because contextChanged is not run when the form is first opened.
        // XXX perhaps I can do this in componentOpened instead?
        updateErrors(); // XXX shouldn't the contextChanged ensure this?

        // Refresh layout for fragments and for pages that contain fragments whenever
        // they are exposed
        if (jsfForm.isFragment() || jsfForm.hasCachedExternalFrames()) {
            // Always refresh fragments on expose since whenever they are
            // rendered as part of other (including) documents those documents
            // may style our elements and stash box references on the elements
            // that point to their own box hierarchies
            designer.redoPaneLayout(true);
        }

        designer.setPaneGrid(jsfForm.isGridMode());

        // XXX This was wrong, showing doesn't necessarily mean activated.
//        // We cannot set the caret to the document position yet; we need
//        // to do layout before the mapper functions work... This is done
//        // after page layout instead.
//        designer.paneRequestFocus();
    }   

    private void designerHidden() {
        // No op.
    }

    private boolean isActivated() {
        MultiViewElementCallback callback = multiViewElementCallback;
        return callback == null 
                ? false
                : callback.isSelectedElement() && callback.getTopComponent() == TopComponent.getRegistry().getActivated();
    }
    
    void modelLoaded() {
        initDesigner();
        designerOpened();
        designerShowing();
        if (isActivated()) {
            designerActivated();
        }
        
        revalidate();
        repaint();
    }
    
    
    private static class JsfLookupProvider implements Lookup.Provider {
        private final Lookup lookup;
        
        public JsfLookupProvider(JsfTopComponent jsfTopComponent, DataObject jspDataObject) {
            this.lookup = createLookup(jsfTopComponent, jspDataObject);
        }
        
        public Lookup getLookup() {
            return lookup;
        }
        
        private Lookup createLookup(JsfTopComponent jsfTopComponent, DataObject jspDataObject) {
            List<Object> objects = new ArrayList<Object>();
            
            objects.add(jspDataObject);
            
            objects.add(jsfTopComponent.NAVIGATOR_HINT);
            
            // Also add the policy to show only panels according to the provided hints.
            objects.add(jsfTopComponent.NAVIGATOR_LOOKUP_PANELS_POLICY);
  
            PaletteController paletteController = jsfTopComponent.getJsfForm().getPaletteController();
            if (paletteController == null) {
                warn("Loaded FacesModel doesn't Project needed to create PaletteController!" +
                        "\nThe Designer lookup won't be fully inited. Palete won't be loaded." +
                        "\nVariable jsfForm=" + jsfTopComponent.getJsfForm()); // NOI18N
            } else {
                objects.add(paletteController);
            }
            
            return Lookups.fixed(objects.toArray());
        }
    } // End of JsfLookupProvider.

    
    private static void warn(String message) {
        Logger logger = getLogger();
        logger.log(Level.WARNING, message);
    }
    
    private static void log(String message) {
        Logger logger = getLogger();
        logger.log(Level.INFO, message);
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(JsfTopComponent.class.getName());
    }
    
    
    private static class DesignerNavigatorLookupPanelsPolicy implements NavigatorLookupPanelsPolicy {

        public int getPanelsPolicy() {
            return NavigatorLookupPanelsPolicy.LOOKUP_HINTS_ONLY;
        }
        
    } // DesignerNavigatorLooupPanelPolicy
}