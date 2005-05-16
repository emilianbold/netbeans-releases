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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.core.spi.multiview.*;
import org.openide.nodes.*;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;

/**
 * @author mkuchtiak
 */
public class OverviewMultiViewElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener {
    private SectionView view;
    private ToolBarDesignEditor comp;
    private DDDataObject dObj;
    private WebApp webApp;
    private OverviewFactory factory;
    private boolean needInit=true;
    private int index;
    private RequestProcessor.Task repaintingTask;
    private static final String OVERVIEW_MV_ID=DDDataObject.DD_MULTIVIEW_PREFIX+DDDataObject.MULTIVIEW_OVERVIEW; 
    private static final String HELP_ID_PREFIX=DDDataObject.HELP_ID_PREFIX_OVERVIEW;
    
    /** Creates a new instance of DDMultiViewElement */
    public OverviewMultiViewElement(final DDDataObject dObj, int index) {
        super(dObj);
        this.dObj=dObj;
        this.index=index;
        comp = new ToolBarDesignEditor();
        factory = new OverviewFactory(comp, dObj);
        setVisualEditor(comp);
        repaintingTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        repaintView();
                    }
                });
            }
        });
    }
    
    public SectionView getSectionView() {
        return view;
    }
    
    public void componentShowing() {
        super.componentShowing();
        dObj.setLastOpenView(index);
        if (needInit || !dObj.isDocumentParseable()) {
            repaintView();
            needInit=false;
        }
    }
    
    private void repaintView() {
        webApp = dObj.getWebApp();
        view =new OverView(webApp);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
            ((SectionView)view).openPanel("overview"); //NOI18N
        }
        view.checkValidity();
        dObj.checkParseable();
    }

    public void componentOpened() {
        super.componentOpened();
        dObj.getWebApp().addPropertyChangeListener(this);
    }
    
    public void componentClosed() {
        super.componentClosed();
        dObj.getWebApp().removePropertyChangeListener(this);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!dObj.isChangedFromUI()) {
            String name = evt.getPropertyName();
            if ( name.indexOf("/WebApp/DisplayName")>=0 || //NOI18N
                 name.indexOf("/WebApp/Description")>=0 || //NOI18N
                 name.indexOf("Distributable")>0 || //NOI18N
                 name.indexOf("ContextParam")>0 || //NOI18N
                 name.indexOf("Listener")>0 || //NOI18N
                 name.indexOf("SessionConfig")>0 ) { //NOI18N
                // repaint view if the wiew is active and something is changed with elements listed above
                if (OVERVIEW_MV_ID.equals(dObj.getSelectedPerspective().preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }

    class OverView extends SectionView {
        private Node overviewNode, contextParamsNode, listenersNode;
        OverView(WebApp webApp) {
            super(factory);
            overviewNode = new OverviewNode();
            addSection(new SectionPanel(this,overviewNode,"overview")); //NOI18N
            
            contextParamsNode = new ContextParamsNode();
            addSection(new SectionPanel(this,contextParamsNode,"context_params")); //NOI18N

            listenersNode = new ListenersNode();
            addSection(new SectionPanel(this,listenersNode,"listeners")); //NOI18N

            Children rootChildren = new Children.Array();
            rootChildren.add(new Node[]{overviewNode,contextParamsNode,listenersNode}); 
            AbstractNode root = new AbstractNode(rootChildren);
            setRoot(root);
        }
        
        Node getOverviewNode() {
            return overviewNode;
        }
        
        Node getContextParamsNode() {
            return contextParamsNode;
        }
        
        Node getListenersNode(){
            return listenersNode;
        }
    }
    
    private class OverviewNode extends org.openide.nodes.AbstractNode {
        OverviewNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_Overview"));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/class"); //NOI18N
        }    
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"overviewNode"); //NOI18N
        }
    }
    
    private class ContextParamsNode extends org.openide.nodes.AbstractNode {
        ContextParamsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_ContextParams"));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramsNode"); //NOI18N
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"contextParamsNode"); //NOI18N
        }
    }
    
    private class ListenersNode extends org.openide.nodes.AbstractNode {
        ListenersNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_Listeners"));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/class"); //NOI18N
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"listenersNode"); //NOI18N
        }
    }
}
