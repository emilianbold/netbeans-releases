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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.appbnd;

import java.io.IOException;
import org.netbeans.modules.j2ee.websphere6.dd.beans.AuthorizationsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppBnd;
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
public class WSAppBndToolBarMVElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener{
    private ToolBarDesignEditor comp;
    private SectionView view;
    private WSAppBndDataObject dObj;
    private PanelFactory factory;
    private RequestProcessor.Task repaintingTask;
    private boolean needInit=true;
    private static final long serialVersionUID = 76117428339792L;
    
    private javax.swing.Action addAuthorizationAction, removeAuthorizationAction;
    private static final String APPBND_MV_ID = WSMultiViewDataObject.MULTIVIEW_APPBND + 
            WSMultiViewDataObject.DD_MULTIVIEW_POSTFIX;
    public WSAppBndToolBarMVElement(WSAppBndDataObject dObj) {
        super(dObj);
        this.dObj=dObj;
        comp = new ToolBarDesignEditor();
        factory=new PanelFactory(comp,dObj);
        
        addAuthorizationAction = new AddAuthorizationAction(NbBundle.getMessage(WSAppBndToolBarMVElement.class,"LBL_addAuthorization"));
        removeAuthorizationAction = new RemoveAuthorizationAction(NbBundle.getMessage(WSAppBndToolBarMVElement.class,"LBL_removeAuthorization"));
        
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
        view =new WSAppBndView(dObj);
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
    public WSAppBndView getAppBndView() {
        return (WSAppBndView)view;
    }
    
    public void componentShowing() {
        super.componentShowing();
        if (needInit) {
            repaintView();
            needInit=false;
        }
        //view=new WSAppBndView(dObj);
        comp.setContentView(view);
        try {
            ((SectionView)view).openPanel(dObj.getAppBnd());
        } catch(java.io.IOException e) {
        }
        view.checkValidity();
    }
    
     public void componentOpened() {
        super.componentOpened();
        try {
            dObj.getAppBnd().addPropertyChangeListener(this);
        } catch(IOException ex) {
            ex=null;
        }
    }
    
    public void componentClosed() {
        super.componentClosed();
        try {
            dObj.getAppBnd().removePropertyChangeListener(this);
        } catch(IOException ex) {
            ex=null;
        }
    }
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!dObj.isChangedFromUI()) {
            String name = evt.getPropertyName();
            if ( name.indexOf("ApplicationBnd")>0 ) { //NOI18
                // repaint view if the wiew is active and something is changed with filters
                if (APPBND_MV_ID.equals(dObj.getSelectedPerspective().preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }
    
    private class WSAppBndView extends SectionView {
        private SectionContainer bindingsCont;
        private WSAppBnd appbnd;
        private SectionContainer authorizationsCont;
        
        WSAppBndView(WSAppBndDataObject dObj) {
            super(factory);
            
            Children rootChildren = new Children.Array();
            Node root = new AbstractNode(rootChildren);
            try {
                this.appbnd=dObj.getAppBnd();
                rootChildren.add(new Node[]{
                    createAppBndAttrNode(),
                    createAppAuthorizationsNode()
                });
            } catch (java.io.IOException ex) {
                System.out.println("ex="+ex);
                root.setDisplayName("Invalid AppBnd");
            } finally {
                setRoot(root);
            }
            
        }
        
        
        public SectionContainer getAuthorizationsContainer() {
            return authorizationsCont;
        }
        
        private Node createAppBndAttrNode() {
            Node appbndNode = new WSAppBndNode(dObj);
            // add panels
            addSection(new SectionPanel(this,appbndNode,appbnd));
            return appbndNode;
        }
        
        private Node createAppAuthorizationsNode() {
            
            AuthorizationsType[] authorizations = appbnd.getAuthorizationTable().getAuthorizations();
            Children extch = new Children.Array();
            Node[] authorizationNodes=null;
            
            if(authorizations!=null && authorizations.length>0) {
                authorizationNodes = new Node[authorizations.length];
                for (int i=0;i<authorizations.length;i++) {
                    authorizationNodes[i] = new AuthorizationNode();
                    authorizationNodes[i].setDisplayName("Authorization #"+(i+1));
                }
                extch.add(authorizationNodes);
            }
            
            Node authorizationsNode = new SectionContainerNode(extch);
            authorizationsNode.setDisplayName("Authorizations");
            
            
            authorizationsCont = new SectionContainer(this,authorizationsNode,"Authorizations");
            authorizationsCont.setHeaderActions(new javax.swing.Action[]{addAuthorizationAction});
            
            
            // creatings section panels for ResRefs
            if(authorizations!=null) {
                SectionPanel[] pan = new SectionPanel[authorizations.length];
                
                for (int i=0;i<authorizations.length;i++) {
                    pan[i] = new SectionPanel(this, authorizationNodes[i], authorizations[i]);
                    pan[i].setHeaderActions(new javax.swing.Action[]{removeAuthorizationAction});
                    authorizationsCont.addSection(pan[i]);
                }
            }
            addSection(authorizationsCont);
            
            return authorizationsNode;
        }
    }
    
    private class AddAuthorizationAction extends javax.swing.AbstractAction {
        
        AddAuthorizationAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_addJspPG_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            try{
                dObj.setChangedFromUI(true);
                long time_id=java.lang.System.currentTimeMillis();
                WSAppBnd appbnd=dObj.getAppBnd();
                int number=appbnd.getAuthorizationTable().sizeAuthorizations()+1;
                AuthorizationsType appAuthorization=new AuthorizationsType();
                appAuthorization.setDefaults();
                appbnd.getAuthorizationTable().addAuthorization(appAuthorization);

                
                Node authorizationNode = new AuthorizationNode();
                view.getRoot().getChildren().add(new Node[]{authorizationNode});
                authorizationNode.setDisplayName(authorizationNode.getDisplayName()+number);
                dObj.setChangedFromUI(true);
                dObj.modelUpdatedFromUI();
                //AppAuthorizationsType[] authorizations = appbnd.getAppAuthorizations();
                SectionPanel sectionPanel=new SectionPanel(view,authorizationNode,appAuthorization);
                sectionPanel.setHeaderActions(new javax.swing.Action[]{removeAuthorizationAction});
                
                ((WSAppBndView)view).getAuthorizationsContainer().addSection(sectionPanel,true);
                dObj.setChangedFromUI(false);
                
            } catch (java.io.IOException ex) {
            } catch (java.lang.IllegalArgumentException ex) {
            }
        }
    }
    private class RemoveAuthorizationAction extends javax.swing.AbstractAction {
        
        RemoveAuthorizationAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
            AuthorizationsType appAuthorization = (AuthorizationsType) sectionPanel.getKey();
            // removing from data model
            
            try {
                dObj.setChangedFromUI(true);
                dObj.getAppBnd().getAuthorizationTable().removeAuthorization(appAuthorization);
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
         
            WSAppBnd appbnd = dObj.getAppExt();
         
            ResRefBindingsType[] resrefs = appbnd.getResRefBindings();
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
         
            AppRefBindingsType[] apprefs = appbnd.getAppRefBindings();
            if(apprefs==null) {
                return null;
            }
            for (int i=0;i<apprefs.length;i++) {
                String title = apprefs[i].getBindingAppRef();
                if (title==null || title.length()==0) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(apprefs[i],"title");
                    return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                }
                for (int j=0;j<apprefs.length;j++) {
                    String tit = apprefs[j].getBindingAppRef();
                    if (i!=j && title.equals(tit)) {
                        Error.ErrorLocation loc = new Error.ErrorLocation(apprefs[i],"title");
                        return new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc);
                    }
                }
            }
         
            ResEnvRefBindingsType[] resenvrefs = appbnd.getResEnvRefBindings();
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
    
    
    public static class WSAppBndNode extends org.openide.nodes.AbstractNode {
        WSAppBndNode(WSAppBndDataObject appbnd) {
            super(org.openide.nodes.Children.LEAF);
            //setDisplayName(appbnd.getXmiId());
            setDisplayName("Binding Deployment Information");
            //setIconBaseWithExtension("org/netbeans/modules/appbndmultiview/ws6.gif"); //NOI18N
        }
    }
    
    public static class AuthorizationNode extends org.openide.nodes.AbstractNode {
        
        public AuthorizationNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Authorization #");
            //setIconBaseWithExtension("org/netbeans/modules/ejbbndmultiview/ws.gif"); //NOI18N
            
        }
    }
}
