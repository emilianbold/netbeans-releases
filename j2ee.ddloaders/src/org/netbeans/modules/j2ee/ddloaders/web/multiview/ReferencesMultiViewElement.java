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
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;

/** ResourcesMultiViewElement.java - Multi View Element for Resources :
 * - env-entries
 * - resource-refs
 * - resource-env-refs
 * - ejb-refs
 * - message-destination-refs
 * 
 * Created on April 11, 2005
 * @author mkuchtiak
 */
public class ReferencesMultiViewElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener {
    private SectionView view;
    private ToolBarDesignEditor comp;
    private DDDataObject dObj;
    private WebApp webApp;
    private ReferencesFactory factory;
    private javax.swing.Action addAction, removeAction;
    private boolean needInit=true;
    private int index;
    private RequestProcessor.Task repaintingTask;
    private static final String REFERENCES_MV_ID=DDDataObject.DD_MULTIVIEW_PREFIX+DDDataObject.MULTIVIEW_REFERENCES;
    private static final String HELP_ID_PREFIX=DDDataObject.HELP_ID_PREFIX_REFERENCES;
    
    /** Creates a new instance of DDMultiViewElement */
    public ReferencesMultiViewElement(final DDDataObject dObj, int index) {
        super(dObj);
        this.dObj=dObj;
        this.index=index;
        comp = new ToolBarDesignEditor();
        factory = new ReferencesFactory(comp, dObj);
        /*
        addAction = new AddAction(dObj, NbBundle.getMessage(FiltersMultiViewElement.class,"LBL_addFilter"));
        removeAction = new RemoveAction(NbBundle.getMessage(FiltersMultiViewElement.class,"LBL_remove"));
         */
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
        view = new ReferencesView(webApp);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
            ReferencesView referencesView = (ReferencesView)view;
            Node initialNode = referencesView.getResRefsNode();
            Children ch = initialNode.getChildren();
            if (ch.getNodesCount()>0) 
                initialNode = ch.getNodes()[0];
            referencesView.selectNode(initialNode);
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
            if ( name.indexOf("Ref")>0 || name.indexOf("EnvEntry")>0) { //NOI18
                // repaint view if the wiew is active and something is changed with references
                if (REFERENCES_MV_ID.equals(dObj.getSelectedPerspective().preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }

    class ReferencesView extends SectionView {
        private SectionContainer serviceRefCont;
        private Node envEntriesNode, resRefsNode, resEnvRefsNode, ejbRefsNode, messageDestRefsNode;
        private SectionPanel filterMappingSectionPanel;
        
        ReferencesView (WebApp webApp) {
            super(factory);
            envEntriesNode = new EnvEntriesNode();
            addSection(new SectionPanel(this,envEntriesNode,"env_entries")); //NOI18N
            
            resRefsNode = new ResRefsNode();
            addSection(new SectionPanel(this,resRefsNode,"res_refs")); //NOI18N

            resEnvRefsNode = new ResEnvRefsNode();
            addSection(new SectionPanel(this,resEnvRefsNode,"res_env_refs")); //NOI18N
            
            ejbRefsNode = new EjbRefsNode();
            addSection(new SectionPanel(this,ejbRefsNode,"ejb_refs")); //NOI18N
            
            messageDestRefsNode = new MessageDestRefsNode();
            addSection(new SectionPanel(this,messageDestRefsNode,"message_dest_refs")); //NOI18N
            
            Children rootChildren = new Children.Array();
            rootChildren.add(new Node[]{envEntriesNode,resRefsNode,resEnvRefsNode,ejbRefsNode,messageDestRefsNode}); 
            AbstractNode root = new AbstractNode(rootChildren);
            setRoot(root);
        }
        
        Node getEnvEntriesNode() {
            return envEntriesNode;
        }
        
        Node getResRefsNode() {
            return resRefsNode;
        }
        
        Node getResEnvRefsNode(){
            return resEnvRefsNode;
        }
        
        Node getEjbRefsNode() {
            return ejbRefsNode;
        }
        
        Node getMessageDestRefsNode() {
            return messageDestRefsNode;
        }
    }
    
    private class EnvEntriesNode extends org.openide.nodes.AbstractNode {
        EnvEntriesNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(ReferencesMultiViewElement.class,"TTL_EnvEntries"));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramNode"); //NOI18N
        }    
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"envEntriesNode"); //NOI18N
        }
    }
    
    private class ResRefsNode extends org.openide.nodes.AbstractNode {
        ResRefsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(ReferencesMultiViewElement.class,"TTL_ResRefs"));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramNode"); //NOI18N
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"resRefsNode"); //NOI18N
        }
    }
    
    private class ResEnvRefsNode extends org.openide.nodes.AbstractNode {
        ResEnvRefsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(ReferencesMultiViewElement.class,"TTL_ResEnvRefs"));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramNode"); //NOI18N
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"resEnvRefsNode"); //NOI18N
        }
    }
    
    private class EjbRefsNode extends org.openide.nodes.AbstractNode {
        EjbRefsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(ReferencesMultiViewElement.class,"TTL_EjbRefs"));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramNode"); //NOI18N
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"ejbRefsNode"); //NOI18N
        }
    }
    
    private class MessageDestRefsNode extends org.openide.nodes.AbstractNode {
        MessageDestRefsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(ReferencesMultiViewElement.class,"TTL_MessageDestRefs"));
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/paramNode"); //NOI18N
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"messageDestRefsNode"); //NOI18N
        }
    }
}
