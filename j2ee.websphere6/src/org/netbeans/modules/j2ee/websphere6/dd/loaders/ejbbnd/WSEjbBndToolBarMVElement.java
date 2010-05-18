/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbbnd;

import java.io.IOException;
import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSEjbBnd;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
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
    private static final String EJBBND_MV_ID = WSMultiViewDataObject.MULTIVIEW_EJBBND + 
            WSMultiViewDataObject.DD_MULTIVIEW_POSTFIX;
    
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
        if (needInit) {
            repaintView();
            needInit=false;
        }
        //view=new WSEjbBndView(dObj);
        comp.setContentView(view);
        try {
            ((SectionView)view).openPanel(dObj.getEjbBnd());
        } catch(java.io.IOException e) {
        }
        view.checkValidity();
    }
    
    public void componentOpened() {
        super.componentOpened();
        try {
            dObj.getEjbBnd().addPropertyChangeListener(this);
        } catch(IOException ex) {
            ex=null;
        }
    }
    
    public void componentClosed() {
        super.componentClosed();
        try {
            dObj.getEjbBnd().removePropertyChangeListener(this);
        } catch(IOException ex) {
            ex=null;
        }
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!dObj.isChangedFromUI()) {
            String name = evt.getPropertyName();
            if ( name.indexOf("EjbJarBnd")>0 ) { //NOI18
                // repaint view if the wiew is active and something is changed with filters
                if (EJBBND_MV_ID.equals(dObj.getSelectedPerspective().preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
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
                dObj.setChangedFromUI(true);
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
