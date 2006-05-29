/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbbnd;

import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSEjbBnd;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.SectionNodes.*;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSEjbBindingsPanel;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSEjbBndAttributesPanel;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSReferenceBindingsPanel;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.nodes.*;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.multiview.Error;
/**
 *
 * @author dlipin
 */
public class WSEjbBndToolBarMVElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener{
    private ToolBarDesignEditor comp;
    private SectionView view;
    private WSEjbBndDataObject dObj;
    private PanelFactory factory;
    private RequestProcessor.Task repaintingTask;
    private boolean needInit=true;
    private javax.swing.Action addBindingAction, removeBindingAction;
    private static final long serialVersionUID = 76737428339792L;
    
    public WSEjbBndToolBarMVElement(WSEjbBndDataObject dObj) {
        super(dObj);
        this.dObj=dObj;
        comp = new ToolBarDesignEditor();
        factory=new PanelFactory(comp,dObj);
        
        addBindingAction = new AddBindingAction(NbBundle.getMessage(WSEjbBndToolBarMVElement.class,"LBL_addEjbBinding"));
        removeBindingAction = new RemoveBindingAction(NbBundle.getMessage(WSEjbBndToolBarMVElement.class,"LBL_removeEjbBinding"));
        
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
    
    private void repaintView() {
        view =new WSEjbBndView(dObj);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
            //((SectionView)view).openPanel("welcome_files"); //NOI18N
        }
        view.checkValidity();
        //dObj.checkParseable();
    }
    
    
    
    public SectionView getSectionView() {
        return view;
    }
    public WSEjbBndView getEjbBndView() {
        return (WSEjbBndView)view;
    }
    
    public void componentShowing() {
        super.componentShowing();
        view=new WSEjbBndView(dObj);
        comp.setContentView(view);
        try {
            ((SectionView)view).openPanel(dObj.getEjbBnd());
        } catch(java.io.IOException e) {
        }
        view.checkValidity();
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        
    }
    
    private class WSEjbBndView extends SectionView {
        private SectionContainer bindingsCont;
        private WSEjbBnd ejbbnd;
        WSEjbBndView(WSEjbBndDataObject dObj) {
            super(factory);
            
            Children rootChildren = new Children.Array();
            Node root = new AbstractNode(rootChildren);
            try {
                this.ejbbnd=dObj.getEjbBnd();
                rootChildren.add(new Node[]{
                    createEjbBndAttrNode(),
                    createEjbBindingsNode()
                });
            } catch (java.io.IOException ex) {
                System.out.println("ex="+ex);
                root.setDisplayName("Invalid EjbBnd");
            } finally {
                setRoot(root);
            }
            
        }
        
        
        public SectionContainer getEjbBindingsContainer() {
            return bindingsCont;
        }
        
        private Node createEjbBndAttrNode() {
            Node ejbbndNode = new WSEjbBndNode(dObj);
            // add panels
            addSection(new SectionPanel(this,ejbbndNode,ejbbnd));
            return ejbbndNode;
        }
        private Node createEjbBindingsNode() {
            
            EjbBindingsType[] bindings = ejbbnd.getEjbBindings();
            Children extch = new Children.Array();
            Node[] bindingNodes=null;
            
            if(bindings!=null && bindings.length>0) {
                bindingNodes = new Node[bindings.length];
                for (int i=0;i<bindings.length;i++) {
                    bindingNodes[i] = new BindingNode();
                    bindingNodes[i].setDisplayName("Ejb Binding #"+(i+1));
                }
                extch.add(bindingNodes);
            }
            
            Node bindingsNode = new SectionContainerNode(extch);
            bindingsNode.setDisplayName("Ejb Bindings");
            
            
            bindingsCont = new SectionContainer(this,bindingsNode,"Ejb Bindings");
            bindingsCont.setHeaderActions(new javax.swing.Action[]{addBindingAction});
            
            
            // creatings section panels for ResRefs
            if(bindings!=null) {
                SectionPanel[] pan = new SectionPanel[bindings.length];
                
                for (int i=0;i<bindings.length;i++) {
                    pan[i] = new SectionPanel(this, bindingNodes[i], bindings[i]);
                    pan[i].setHeaderActions(new javax.swing.Action[]{removeBindingAction});
                    bindingsCont.addSection(pan[i]);
                }
            }
            addSection(bindingsCont);
            
            return bindingsNode;
        }
        
    }
    
    private class AddBindingAction extends javax.swing.AbstractAction {
        
        AddBindingAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_addJspPG_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            try{
                
                long time_id=java.lang.System.currentTimeMillis();
                WSEjbBnd ejbbnd=dObj.getEjbBnd();
                int number=ejbbnd.sizeEjbBindings()+1;
                
                
                EjbBindingsType ejbBinding=new EjbBindingsType();
                ejbBinding.setEnterpriseBean("");
                
                ejbBinding.setJndiName("Binding_"+time_id);
                ejbBinding.setXmiId("Binding_"+time_id);
                ejbBinding.setHref("EnterpriseBean_"+time_id);
                
                
                //rr.setTitle("Binding Item #"+number);
                ejbbnd.addEjbBindings(ejbBinding);
                //////////////////////////////////
                Node bindingNode = new BindingNode();
                view.getRoot().getChildren().add(new Node[]{bindingNode});
                bindingNode.setDisplayName(bindingNode.getDisplayName()+number);
                dObj.setChangedFromUI(true);
                dObj.modelUpdatedFromUI();
                //EjbBindingsType[] bindings = ejbbnd.getEjbBindings();
                SectionPanel sectionPanel=new SectionPanel(view,bindingNode,ejbBinding);
                sectionPanel.setHeaderActions(new javax.swing.Action[]{removeBindingAction});
                
                ((WSEjbBndView)view).getEjbBindingsContainer().addSection(sectionPanel,true);
                dObj.setChangedFromUI(false);
                
            } catch (java.io.IOException ex) {
            } catch (java.lang.IllegalArgumentException ex) {
            }
        }
    }
    private class RemoveBindingAction extends javax.swing.AbstractAction {
        
        RemoveBindingAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
            EjbBindingsType ejbBinding = (EjbBindingsType)sectionPanel.getKey();
            // removing from data model
            
            try {
                dObj.setChangedFromUI(true);
                dObj.getEjbBnd().removeEjbBindings(ejbBinding);
                sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
                sectionPanel.getSectionView().updateUI();
                sectionPanel.updateUI();
                
                dObj.modelUpdatedFromUI();
                
            } catch (java.io.IOException ec) {
                
            } finally {
                dObj.setChangedFromUI(false);
            }
            
        }
    }
    
    
    public Error validateView() {
        /*try {
         
            WSEjbBnd ejbbnd = dObj.getEjbExt();
         
            ResRefBindingsType[] resrefs = ejbbnd.getResRefBindings();
            if(resrefs==null) {
                return null;
            }
            for (int i=0;i<resrefs.length;i++) {
                String title = resrefs[i].getBindingResourceRef();
                if (title==null || title.length()==0) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(resrefs[i],"title");
                    return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                }
                for (int j=0;j<resrefs.length;j++) {
                    String tit = resrefs[j].getBindingResourceRef();
                    if (i!=j && title.equals(tit)) {
                        Error.ErrorLocation loc = new Error.ErrorLocation(resrefs[i],"title");
                        return new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc);
                    }
                }
            }
         
            EjbRefBindingsType[] ejbrefs = ejbbnd.getEjbRefBindings();
            if(ejbrefs==null) {
                return null;
            }
            for (int i=0;i<ejbrefs.length;i++) {
                String title = ejbrefs[i].getBindingEjbRef();
                if (title==null || title.length()==0) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(ejbrefs[i],"title");
                    return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                }
                for (int j=0;j<ejbrefs.length;j++) {
                    String tit = ejbrefs[j].getBindingEjbRef();
                    if (i!=j && title.equals(tit)) {
                        Error.ErrorLocation loc = new Error.ErrorLocation(ejbrefs[i],"title");
                        return new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc);
                    }
                }
            }
         
            ResEnvRefBindingsType[] resenvrefs = ejbbnd.getResEnvRefBindings();
            if(resenvrefs==null) {
                return null;
            }
            for (int i=0;i<resenvrefs.length;i++) {
                String title = resenvrefs[i].getBindingResourceEnvRef();
                if (title==null || title.length()==0) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(resenvrefs[i],"title");
                    return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                }
                for (int j=0;j<resenvrefs.length;j++) {
                    String tit = resenvrefs[j].getBindingResourceEnvRef();
                    if (i!=j && title.equals(tit)) {
                        Error.ErrorLocation loc = new Error.ErrorLocation(resenvrefs[i],"title");
                        return new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc);
                    }
                }
            }
        } //catch (java.io.IOException ex){}
         **/
        return null;
    }
    
    
    public static class WSEjbBndNode extends org.openide.nodes.AbstractNode {
        WSEjbBndNode(WSEjbBndDataObject ejbbnd) {
            super(org.openide.nodes.Children.LEAF);
            //setDisplayName(ejbbnd.getXmiId());
            setDisplayName("Binding Deployment Information");
            //setIconBaseWithExtension("org/netbeans/modules/ejbbndmultiview/ws6.gif"); //NOI18N
        }
    }
    
    public static class BindingNode extends org.openide.nodes.AbstractNode {
        
        public BindingNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Ejb Binding #");
            //setIconBaseWithExtension("org/netbeans/modules/ejbbndmultiview/ws.gif"); //NOI18N
            
        }
    }
    
    /*
     *
    public static class ServletsCacheConfigNode extends org.openide.nodes.AbstractNode {
        ServletsCacheConfigNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Servlet Cache Configs");
            //setIconBaseWithExtension("org/netbeans/modules/ejbbndmultiview/ws.gif"); //NOI18N
        }
    }
     
    public static class EjbRefNode extends org.openide.nodes.AbstractNode {
        EjbRefNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Binding Item #");
            //setIconBaseWithExtension("org/netbeans/modules/ejbbndmultiview/ws.gif"); //NOI18N
        }
     
    }
     
    public static class ResEnvRefNode extends org.openide.nodes.AbstractNode {
        ResEnvRefNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Binding Item #");
            //setIconBaseWithExtension("org/netbeans/modules/ejbbndmultiview/ws.gif"); //NOI18N
        }
    }*/
}
