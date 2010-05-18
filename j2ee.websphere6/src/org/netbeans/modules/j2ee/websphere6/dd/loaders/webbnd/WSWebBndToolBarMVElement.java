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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.webbnd;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmiConstants;
import org.netbeans.modules.j2ee.websphere6.dd.beans.EjbRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResEnvRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ResRefBindingsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebBnd;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.SectionNodes.*;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.nodes.*;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.xml.multiview.Error;
/**
 *
 * @author dlipin
 */
public class WSWebBndToolBarMVElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener, DDXmiConstants{
    
    private static final Logger LOGGER = Logger.getLogger(WSWebBndToolBarMVElement.class.getName());
    
    private ToolBarDesignEditor comp;
    private SectionView view;
    private WSWebBndDataObject dObj;
    private PanelFactory factory;
    private RequestProcessor.Task repaintingTask;
    private boolean needInit=true;
    private javax.swing.Action addResRefAction, removeResRefAction;
    private javax.swing.Action addEjbRefAction, removeEjbRefAction;
    private javax.swing.Action addResEnvRefAction, removeResEnvRefAction;
    private static final long serialVersionUID = 76123745399723L;
    private static final String WEBBND_MV_ID = WSMultiViewDataObject.MULTIVIEW_WEBBND + 
            WSMultiViewDataObject.DD_MULTIVIEW_POSTFIX;
    
    public WSWebBndToolBarMVElement(WSWebBndDataObject dObj) {
        super(dObj);
        this.dObj=dObj;
        comp = new ToolBarDesignEditor();
        factory=new PanelFactory(comp,dObj);
        addResRefAction = new AddResRefAction("Add New Resource Reference Binding.."/*NbBundle.getMessage(WSWebBndToolBarMVElement.class,"LBL_addJspPG")*/);
        removeResRefAction = new RemoveResRefAction("Remove This Binding"/*NbBundle.getMessage(WSWebBndToolBarMVElement.class,"LBL_addJspPG")*/);
        
        addEjbRefAction = new AddEjbRefAction("Add New Ejb Reference Binding.."/*NbBundle.getMessage(WSWebBndToolBarMVElement.class,"LBL_addJspPG")*/);
        removeEjbRefAction = new RemoveEjbRefAction("Remove This Binding"/*NbBundle.getMessage(WSWebBndToolBarMVElement.class,"LBL_addJspPG")*/);
        
        addResEnvRefAction = new AddResEnvRefAction("Add New Resource Env Reference Binding.."/*NbBundle.getMessage(WSWebBndToolBarMVElement.class,"LBL_addJspPG")*/);
        removeResEnvRefAction = new RemoveResEnvRefAction("Remove This Binding"/*NbBundle.getMessage(WSWebBndToolBarMVElement.class,"LBL_addJspPG")*/);
        
        
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
        view =new WSWebBndView(dObj);
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
    public WSWebBndView getWebBndView() {
        return (WSWebBndView)view;
    }
    
    public void componentShowing() {
        super.componentShowing();
        if (needInit) {
            repaintView();
            needInit=false;
        }
        //view=new WSWebBndView(dObj);
        comp.setContentView(view);
        try {
            ((SectionView)view).openPanel(dObj.getWebBnd());
        } catch(java.io.IOException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        
        view.checkValidity();
    }
    public void componentOpened() {
        super.componentOpened();
        try {
            dObj.getWebBnd().addPropertyChangeListener(this);
        } catch(IOException ex) {
            ex=null;
        }
    }
    
    public void componentClosed() {
        super.componentClosed();
        try {
            dObj.getWebBnd().removePropertyChangeListener(this);
        } catch(IOException ex) {
            ex=null;
        }
    }
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!dObj.isChangedFromUI()) {
            String name = evt.getPropertyName();
            if ( name.indexOf("WebApplicationBnd")>0 ) { //NOI18
                // repaint view if the wiew is active and something is changed with filters
                if (WEBBND_MV_ID.equals(dObj.getSelectedPerspective().preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }
    
    private class AddResRefAction extends javax.swing.AbstractAction {
        
        AddResRefAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_addJspPG_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            try{
		dObj.setChangedFromUI(true);
                WSWebBnd webbnd=dObj.getWebBnd();
                ResRefBindingsType rr=new ResRefBindingsType(WEB_APPLICATION);
                rr.setDefaults();
                int number=webbnd.getResRefBindings().length+1;
                
                //rr.setTitle("Binding Item #"+number);
                webbnd.addResRefBindings(rr);
                //////////////////////////////////
                Node resrefNode = new  ResRefNode();
                view.getRoot().getChildren().add(new Node[]{resrefNode});
                resrefNode.setDisplayName(resrefNode.getDisplayName()+number);
                dObj.setChangedFromUI(true);
                dObj.modelUpdatedFromUI();
                SectionPanel sectionPanel=new SectionPanel(view,resrefNode,rr);
                sectionPanel.setHeaderActions(new javax.swing.Action[]{removeResRefAction});
                ((WSWebBndView)view).getResRefsContainer().addSection(sectionPanel,true);
                dObj.setChangedFromUI(false);
                
            } catch (java.io.IOException ex) {
            } catch (java.lang.IllegalArgumentException ex) {
            }
        }
    }
    private class RemoveResRefAction extends javax.swing.AbstractAction {
        
        RemoveResRefAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
            ResRefBindingsType resref = (ResRefBindingsType)sectionPanel.getKey();
            // removing from data model
            
            try {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                dObj.getWebBnd().removeResRefBindings(resref);
                sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
            } catch (java.io.IOException ec) {
                
            } finally {
                dObj.setChangedFromUI(false);
            }
            
        }
    }
    
    
    private class AddEjbRefAction extends javax.swing.AbstractAction {
        
        AddEjbRefAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_addJspPG_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            try{
		dObj.setChangedFromUI(true);
                WSWebBnd webbnd=dObj.getWebBnd();
                EjbRefBindingsType er=new EjbRefBindingsType(WEB_APPLICATION);
                er.setDefaults();
                int number=webbnd.getEjbRefBindings().length+1;
                
                //er.setTitle("Binding Item #"+number);
                webbnd.addEjbRefBindings(er);
                //////////////////////////////////
                Node ejbrefNode = new  EjbRefNode();
                Node root=view.getRoot();
                root.getChildren().add(new Node[]{ejbrefNode});
                ejbrefNode.setDisplayName(ejbrefNode.getDisplayName()+number);
                dObj.setChangedFromUI(true);
                dObj.modelUpdatedFromUI();
                SectionPanel sectionPanel=new SectionPanel(view,ejbrefNode,er);
                sectionPanel.setHeaderActions(new javax.swing.Action[]{removeEjbRefAction});
                ((WSWebBndView)view).getEjbRefsContainer().addSection(sectionPanel,true);
                dObj.setChangedFromUI(false);
                
            } catch (java.io.IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (java.lang.IllegalArgumentException ex) {
                // FIXME what is that? really suspicious
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }
    private class RemoveEjbRefAction extends javax.swing.AbstractAction {
        
        RemoveEjbRefAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
            EjbRefBindingsType ejbref = (EjbRefBindingsType)sectionPanel.getKey();
            // removing from data model
            
            try {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                dObj.getWebBnd().removeEjbRefBindings(ejbref);
                sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
            } catch (java.io.IOException ec) {
                
            } finally {
                dObj.setChangedFromUI(false);
            }
            
            
        }
    }
    
    
    
    private class AddResEnvRefAction extends javax.swing.AbstractAction {
        
        AddResEnvRefAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_addJspPG_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            try{
                WSWebBnd webbnd=dObj.getWebBnd();
                ResEnvRefBindingsType rer=new ResEnvRefBindingsType(WEB_APPLICATION);
                rer.setDefaults();
                int number=webbnd.getResEnvRefBindings().length+1;
                
                //rr.setTitle("Binding Item #"+number);
                webbnd.addResEnvRefBindings(rer);
                //////////////////////////////////
                Node resenvrefNode = new  ResEnvRefNode();
                view.getRoot().getChildren().add(new Node[]{resenvrefNode});
                resenvrefNode.setDisplayName(resenvrefNode.getDisplayName()+number);
                dObj.setChangedFromUI(true);
                dObj.modelUpdatedFromUI();
                SectionPanel sectionPanel=new SectionPanel(view,resenvrefNode,rer);
                sectionPanel.setHeaderActions(new javax.swing.Action[]{removeResEnvRefAction});
                ((WSWebBndView)view).getResEnvRefsContainer().addSection(sectionPanel,true);
                dObj.setChangedFromUI(false);
                
            } catch (java.io.IOException ex) {
            } catch (java.lang.IllegalArgumentException ex) {
            }
        }
    }
    private class RemoveResEnvRefAction extends javax.swing.AbstractAction {
        
        RemoveResEnvRefAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
            ResEnvRefBindingsType resenvref = (ResEnvRefBindingsType)sectionPanel.getKey();
            // removing from data model
            
            try {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                dObj.getWebBnd().removeResEnvRefBindings(resenvref);
                sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
            } catch (java.io.IOException ec) {
                
            } finally {
                dObj.setChangedFromUI(false);
            }
            
        }
    }
    
    
    private class WSWebBndView extends SectionView {
        private SectionContainer resrefsCont;
        private SectionContainer ejbrefsCont;
        private SectionContainer resenvrefsCont;
        private WSWebBnd webbnd;
        XmlMultiViewDataSynchronizer sync;
        WSWebBndView(WSWebBndDataObject dObj) {
            super(factory);
            Children rootChildren = new Children.Array();
            Node root = new AbstractNode(rootChildren);
            try {
                this.webbnd=dObj.getWebBnd();
                rootChildren.add(new Node[]{
                    createWebBndAttrNode(),
                    createResRefNode(),
                    createEjbRefNode(),
                    createResEnvRefNode()}
                );
            } catch (java.io.IOException ex) {
                System.out.println("ex="+ex);
                root.setDisplayName("Invalid WebBnd");
            } finally {
                setRoot(root);
            }
        }
        
        
        private Node createWebBndAttrNode() {
            Node webbndNode = new WSWebBndNode();
            // add panels
            addSection(new SectionPanel(this,webbndNode,webbnd));
            return webbndNode;
        }
        
        private Node createResRefNode() {
            ResRefBindingsType[] resrefs = webbnd.getResRefBindings();
            Children resch = new Children.Array();
            Node[] resrefNode=null;
            
            if(resrefs!=null && resrefs.length>0) {
                resrefNode = new Node[resrefs.length];
                for (int i=0;i<resrefs.length;i++) {
                    resrefNode[i] = new ResRefNode();
                    resrefNode[i].setDisplayName("Binding Item #"+(i+1));
                }
                resch.add(resrefNode);
            }
            
            Node resrefsNode = new SectionContainerNode(resch);
            resrefsNode.setDisplayName("Resource Reference Bindings");
            
            //rootChildren.add(new Node[]{webbndNode,resrefsNode});
            resrefsCont = new SectionContainer(this,resrefsNode,"Resource Reference Bindings");
            resrefsCont.setHeaderActions(new javax.swing.Action[]{addResRefAction});
            
            // creatings section panels for ResRefs
            if(resrefs!=null) {
                SectionPanel[] pan = new SectionPanel[resrefs.length];
                for (int i=0;i<resrefs.length;i++) {
                    pan[i] = new SectionPanel(this, resrefNode[i], resrefs[i]);
                    pan[i].setHeaderActions(new javax.swing.Action[]{removeResRefAction});
                    resrefsCont.addSection(pan[i]);
                }
            }
            addSection(resrefsCont);
            return resrefsNode;
        }
        
        private Node createEjbRefNode() {
            //adding ejbRef
            EjbRefBindingsType[] ejbrefs = webbnd.getEjbRefBindings();
            Children ejbch = new Children.Array();
            Node[] ejbrefNode=null;
            
            if(ejbrefs!=null && ejbrefs.length>0) {
                ejbrefNode = new Node[ejbrefs.length];
                for (int i=0;i<ejbrefs.length;i++) {
                    ejbrefNode[i] = new EjbRefNode();
                    ejbrefNode[i].setDisplayName("Binding Item #"+(i+1));
                }
                ejbch.add(ejbrefNode);
            }
            
            Node ejbrefsNode = new SectionContainerNode(ejbch);
            ejbrefsNode.setDisplayName("Ejb Reference Bindings");
            
            ejbrefsCont = new SectionContainer(this,ejbrefsNode,"Ejb Reference Bindings");
            ejbrefsCont.setHeaderActions(new javax.swing.Action[]{addEjbRefAction});
            
            // creatings section panels for EjbRefs
            if(ejbrefs!=null) {
                SectionPanel[] pan = new SectionPanel[ejbrefs.length];
                for (int i=0;i<ejbrefs.length;i++) {
                    pan[i] = new SectionPanel(this, ejbrefNode[i], ejbrefs[i]);
                    pan[i].setHeaderActions(new javax.swing.Action[]{removeEjbRefAction});
                    ejbrefsCont.addSection(pan[i]);
                }
            }
            addSection(ejbrefsCont);
            return ejbrefsNode;
        }
        
        private Node createResEnvRefNode() {
            ResEnvRefBindingsType[] resenvrefs = webbnd.getResEnvRefBindings();
            Children resenvch = new Children.Array();
            Node[] resenvrefNode=null;
            
            if(resenvrefs!=null && resenvrefs.length>0) {
                resenvrefNode = new Node[resenvrefs.length];
                for (int i=0;i<resenvrefs.length;i++) {
                    resenvrefNode[i] = new ResEnvRefNode();
                    resenvrefNode[i].setDisplayName("Binding Item #"+(i+1));
                }
                resenvch.add(resenvrefNode);
            }
            
            Node resenvrefsNode = new SectionContainerNode(resenvch);
            resenvrefsNode.setDisplayName("Resource Env Reference Bindings");
            
            //rootChildren.add(new Node[]{webbndNode,resenvrefsNode});
            resenvrefsCont = new SectionContainer(this,resenvrefsNode,"Resource Env Reference Bindings");
            resenvrefsCont.setHeaderActions(new javax.swing.Action[]{addResEnvRefAction});
            
            // creatings section panels for ResEnvRefs
            if(resenvrefs!=null) {
                SectionPanel[] pan = new SectionPanel[resenvrefs.length];
                for (int i=0;i<resenvrefs.length;i++) {
                    pan[i] = new SectionPanel(this, resenvrefNode[i], resenvrefs[i]);
                    pan[i].setHeaderActions(new javax.swing.Action[]{removeResEnvRefAction});
                    resenvrefsCont.addSection(pan[i]);
                }
            }
            addSection(resenvrefsCont);
            return resenvrefsNode;
            
        }
        
        public SectionContainer getResRefsContainer() {
            return resrefsCont;
        }
        public SectionContainer getEjbRefsContainer() {
            return ejbrefsCont;
        }
        public SectionContainer getResEnvRefsContainer() {
            return resenvrefsCont;
        }
    }
    
    
    
    public Error validateView() {
        try {
            WSWebBnd webbnd = dObj.getWebBnd();
            
            ResRefBindingsType[] resrefs = webbnd.getResRefBindings();
            if(resrefs==null) {
                return null;
            }
            for (int i=0;i<resrefs.length;i++) {
                String title = resrefs[i].getBindingReference();
                if (title==null || title.length()==0) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(resrefs[i],"title");
                    return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                }
                for (int j=0;j<resrefs.length;j++) {
                    String tit = resrefs[j].getBindingReference();
                    if (i!=j && title.equals(tit)) {
                        Error.ErrorLocation loc = new Error.ErrorLocation(resrefs[i],"title");
                        return new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc);
                    }
                }
            }
            
            EjbRefBindingsType[] ejbrefs = webbnd.getEjbRefBindings();
            if(ejbrefs==null) {
                return null;
            }
            for (int i=0;i<ejbrefs.length;i++) {
                String title = ejbrefs[i].getBindingReference();
                if (title==null || title.length()==0) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(ejbrefs[i],"title");
                    return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                }
                for (int j=0;j<ejbrefs.length;j++) {
                    String tit = ejbrefs[j].getBindingReference();
                    if (i!=j && title.equals(tit)) {
                        Error.ErrorLocation loc = new Error.ErrorLocation(ejbrefs[i],"title");
                        return new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc);
                    }
                }
            }
            
            ResEnvRefBindingsType[] resenvrefs = webbnd.getResEnvRefBindings();
            if(resenvrefs==null) {
                return null;
            }
            for (int i=0;i<resenvrefs.length;i++) {
                String title = resenvrefs[i].getBindingReference();
                if (title==null || title.length()==0) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(resenvrefs[i],"title");
                    return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                }
                for (int j=0;j<resenvrefs.length;j++) {
                    String tit = resenvrefs[j].getBindingReference();
                    if (i!=j && title.equals(tit)) {
                        Error.ErrorLocation loc = new Error.ErrorLocation(resenvrefs[i],"title");
                        return new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc);
                    }
                }
            }
        } catch (java.io.IOException ex){}
        return null;
    }
    
    
    public static class WSWebBndNode extends org.openide.nodes.AbstractNode {
        WSWebBndNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("General Deployment Binding Information");
            //setIconBaseWithExtension("org/netbeans/modules/webbndmultiview/ws6.gif"); //NOI18N
        }
    }
    
}
