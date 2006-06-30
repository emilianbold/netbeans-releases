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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbext;

import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbExtensionsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSEjbExt;
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
public class WSEjbExtToolBarMVElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener{
    private ToolBarDesignEditor comp;
    private SectionView view;
    private WSEjbExtDataObject dObj;
    private PanelFactory factory;
    private RequestProcessor.Task repaintingTask;
    private boolean needInit=true;
    private javax.swing.Action addExtensionAction, removeExtensionAction;
    private static final long serialVersionUID = 76737428339792L;
    
    public WSEjbExtToolBarMVElement(WSEjbExtDataObject dObj) {
        super(dObj);
        this.dObj=dObj;
        comp = new ToolBarDesignEditor();        
        factory=new PanelFactory(comp,dObj);
        
        addExtensionAction = new AddExtensionAction(NbBundle.getMessage(WSEjbExtToolBarMVElement.class,"LBL_addEjbExtension"));
        removeExtensionAction = new RemoveExtensionAction(NbBundle.getMessage(WSEjbExtToolBarMVElement.class,"LBL_removeEjbExtension"));
        
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
        view =new WSEjbExtView(dObj);
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
    public WSEjbExtView getEjbExtView() {
        return (WSEjbExtView)view;
    }
    
    public void componentShowing() {
        super.componentShowing();
        view=new WSEjbExtView(dObj);
        comp.setContentView(view);
        try {
            view.openPanel(dObj.getEjbExt());
        } catch(java.io.IOException ex){}
        view.checkValidity();
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        
    }
    
    private class WSEjbExtView extends SectionView {
        private SectionContainer extensionsCont;
        private WSEjbExt ejbext;
        
        WSEjbExtView(WSEjbExtDataObject dObj) {
            super(factory);
            
            Children rootChildren = new Children.Array();
            Node root = new AbstractNode(rootChildren);
            try {
                this.ejbext=dObj.getEjbExt();
                rootChildren.add(new Node[]{
                    createEjbExtAttrNode(),
                    createEjbExtensionsNode()
                });
            } catch (java.io.IOException ex) {
                System.out.println("ex="+ex);
                root.setDisplayName("Invalid EjbExt");
            } finally {
                setRoot(root);
            }
        }
        public SectionContainer getEjbExtensionsContainer() {
            return extensionsCont;
        }
        
        private Node createEjbExtAttrNode() {
            Node ejbextNode = new WSEjbExtNode(ejbext);
            // add panels
            addSection(new SectionPanel(this,ejbextNode,ejbext));
            return ejbextNode;
        }
        private Node createEjbExtensionsNode() {
            
            EjbExtensionsType[] extensions = ejbext.getEjbExtensions();
            Children extch = new Children.Array();
            Node[] extensionNode=null;
            
            if(extensions!=null && extensions.length>0) {
                extensionNode = new Node[extensions.length];
                for (int i=0;i<extensions.length;i++) {
                    extensionNode[i] = new ExtensionNode();
                    extensionNode[i].setDisplayName("Ejb Extension #"+(i+1));
                }
                extch.add(extensionNode);
            }
            
            Node extensionsNode = new SectionContainerNode(extch);
            extensionsNode.setDisplayName("Ejb Extensions");
            
            
            extensionsCont = new SectionContainer(this,extensionsNode,"Ejb Extension");
            extensionsCont.setHeaderActions(new javax.swing.Action[]{addExtensionAction});
            
            // creatings section panels for ResRefs
            if(extensions!=null) {
                SectionPanel[] pan = new SectionPanel[extensions.length];
                for (int i=0;i<extensions.length;i++) {
                    pan[i] = new SectionPanel(this, extensionNode[i], extensions[i]);
                    pan[i].setHeaderActions(new javax.swing.Action[]{removeExtensionAction});
                    extensionsCont.addSection(pan[i]);
                }
            }
            addSection(extensionsCont);
            return extensionsNode;
        }
        
    }
    
    private class AddExtensionAction extends javax.swing.AbstractAction {
        
        AddExtensionAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_addJspPG_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            try{
                long time_id=java.lang.System.currentTimeMillis();
                WSEjbExt ejbext=dObj.getEjbExt();
                int number=ejbext.sizeEjbExtensions()+1;
                
                
                EjbExtensionsType ejbExtension=new EjbExtensionsType();
                ejbExtension.setEnterpriseBean("");
                ejbExtension.setXmiName("Extension_"+time_id);
                ejbExtension.setXmiId("Extension_"+time_id);
                ejbExtension.setHref("");
                
                
                //rr.setTitle("Binding Item #"+number);
                ejbext.addEjbExtensions(ejbExtension);
                //////////////////////////////////
                Node extensionNode = new ExtensionNode();
                view.getRoot().getChildren().add(new Node[]{extensionNode});
                extensionNode.setDisplayName(extensionNode.getDisplayName()+number);
                dObj.setChangedFromUI(true);
                dObj.modelUpdatedFromUI();
                EjbExtensionsType[] extensions = ejbext.getEjbExtensions();
                SectionPanel sectionPanel=new SectionPanel(view,extensionNode,ejbExtension);
                sectionPanel.setHeaderActions(new javax.swing.Action[]{removeExtensionAction});
                
                ((WSEjbExtView)view).getEjbExtensionsContainer().addSection(sectionPanel,true);
                dObj.setChangedFromUI(false);
                
            } catch (java.io.IOException ex) {
            } catch (java.lang.IllegalArgumentException ex) {
            }
        }
    }
    private class RemoveExtensionAction extends javax.swing.AbstractAction {
        
        RemoveExtensionAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
            EjbExtensionsType ejbExtension = (EjbExtensionsType)sectionPanel.getKey();
            // removing from data model
            
            try {
                dObj.setChangedFromUI(true);
                dObj.getEjbExt().removeEjbExtensions(ejbExtension);
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
         
            WSEjbExt ejbext = dObj.getEjbExt();
         
            ResRefBindingsType[] resrefs = ejbext.getResRefBindings();
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
         
            EjbRefBindingsType[] ejbrefs = ejbext.getEjbRefBindings();
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
         
            ResEnvRefBindingsType[] resenvrefs = ejbext.getResEnvRefBindings();
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
    
    
    public static class WSEjbExtNode extends org.openide.nodes.AbstractNode {
        WSEjbExtNode(WSEjbExt ejbext) {
            super(org.openide.nodes.Children.LEAF);
            //setDisplayName(ejbext.getXmiId());
            setDisplayName("Extended Deployment Information");
            //setIconBaseWithExtension("org/netbeans/modules/ejbextmultiview/ws6.gif"); //NOI18N
        }
    }
    
    public static class ExtensionNode extends org.openide.nodes.AbstractNode {
        ExtensionNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Ejb Extension #");
            //setIconBaseWithExtension("org/netbeans/modules/ejbextmultiview/ws.gif"); //NOI18N
        }
    }
    /*
    public static class ServletsCacheConfigNode extends org.openide.nodes.AbstractNode {
        ServletsCacheConfigNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Servlet Cache Configs");
            //setIconBaseWithExtension("org/netbeans/modules/ejbextmultiview/ws.gif"); //NOI18N
        }
    }
     
    public static class EjbRefNode extends org.openide.nodes.AbstractNode {
        EjbRefNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Binding Item #");
            //setIconBaseWithExtension("org/netbeans/modules/ejbextmultiview/ws.gif"); //NOI18N
        }
     
    }
     
    public static class ResEnvRefNode extends org.openide.nodes.AbstractNode {
        ResEnvRefNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Binding Item #");
            //setIconBaseWithExtension("org/netbeans/modules/ejbextmultiview/ws.gif"); //NOI18N
        }
    }*/
}
