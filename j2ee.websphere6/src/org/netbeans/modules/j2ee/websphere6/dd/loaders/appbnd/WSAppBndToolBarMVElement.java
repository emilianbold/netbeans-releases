package org.netbeans.modules.j2ee.websphere6.dd.loaders.appbnd;

import org.netbeans.modules.j2ee.websphere6.dd.beans.AuthorizationsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppBnd;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.SectionNodes.*;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.WSAppBndAttributesPanel;
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
        view=new WSAppBndView(dObj);
        comp.setContentView(view);
        try {
            ((SectionView)view).openPanel(dObj.getAppBnd());
        } catch(java.io.IOException e) {
        }
        view.checkValidity();
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        
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
