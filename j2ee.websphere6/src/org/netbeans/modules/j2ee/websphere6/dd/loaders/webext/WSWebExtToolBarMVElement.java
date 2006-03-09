package org.netbeans.modules.j2ee.websphere6.dd.loaders.webext;

import org.netbeans.modules.j2ee.websphere6.dd.beans.*;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.*;
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
public class WSWebExtToolBarMVElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener{
    private ToolBarDesignEditor comp;
    private SectionView view;
    private WSWebExtDataObject dObj;
    private PanelFactory factory;
    private RequestProcessor.Task repaintingTask;
    private boolean needInit=true;
    private javax.swing.Action addServletAction, removeServletAction;
    private static final long serialVersionUID = 76757425329721L;
    public WSWebExtToolBarMVElement(WSWebExtDataObject dObj) {
        super(dObj);
        this.dObj=dObj;
        comp = new ToolBarDesignEditor();
        factory=new PanelFactory(comp,dObj);
        addServletAction = new AddServletAction(NbBundle.getMessage(WSWebExtToolBarMVElement.class,"LBL_AddExtendedServlet"));
        removeServletAction = new RemoveServletAction(NbBundle.getMessage(WSWebExtToolBarMVElement.class,"LBL_RemoveExtendedServlet"));
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
        view =new WSWebExtView(dObj);
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
    public WSWebExtView getWebExtView() {
        return (WSWebExtView)view;
    }
    
    public void componentShowing() {
        super.componentShowing();
        view=new WSWebExtView(dObj);
        comp.setContentView(view);
        try {
            view.openPanel(dObj.getWebExt());
        } catch(java.io.IOException ex){}
        view.checkValidity();
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        
    }
    
    private class WSWebExtView extends SectionView {
        private SectionContainer servletsCont;
        private WSWebExt webext;
        
        WSWebExtView(WSWebExtDataObject dObj) {
            super(factory);
            
            Children rootChildren = new Children.Array();
            Node root = new AbstractNode(rootChildren);
            try {
                this.webext=dObj.getWebExt();
                rootChildren.add(new Node[]{
                    createWebExtAttrNode(),
                    createServletsNode()
                });
                /*
                rootChildren.add(new Node[]{
                    createWebExtAttrNode(),
                    createResRefNode(),
                    createEjbRefNode(),
                    createResEnvRefNode()}
                );*/
            } catch (java.io.IOException ex) {
                System.out.println("ex="+ex);
                root.setDisplayName("Invalid WebExt");
            } finally {
                setRoot(root);
            }
        }
        
        
        private Node createWebExtAttrNode() {
            Node webextNode = new WSWebExtNode(webext);
            // add panels
            addSection(new SectionPanel(this,webextNode,webext));
            return webextNode;
        }
        private Node createServletsNode(){
            ExtendedServletsType[] servlets = webext.getExtendedServlets();
            Children servch = new Children.Array();
            Node[] servletNode=null;
            
            if(servlets!=null && servlets.length>0) {
                servletNode = new Node[servlets.length];
                for (int i=0;i<servlets.length;i++) {
                    servletNode[i] = new ServletsNode();
                    servletNode[i].setDisplayName("Extended Servlet #"+(i+1));
                }
                servch.add(servletNode);
            }
            
            Node servletsNode = new SectionContainerNode(servch);
            servletsNode.setDisplayName("Extended Servlets");
            
            //rootChildren.add(new Node[]{webbndNode,servletsNode});
            servletsCont = new SectionContainer(this,servletsNode,"Extended Servlets");
            servletsCont.setHeaderActions(new javax.swing.Action[]{addServletAction});
            
            // creatings section panels for ResRefs
            if(servlets!=null) {
                SectionPanel[] pan = new SectionPanel[servlets.length];
                for (int i=0;i<servlets.length;i++) {
                    pan[i] = new SectionPanel(this, servletNode[i], servlets[i]);
                    pan[i].setHeaderActions(new javax.swing.Action[]{removeServletAction});
                    servletsCont.addSection(pan[i]);
                }
            }
            addSection(servletsCont);
            return servletsNode;
        }
        private Node createServletsCacheConfigNode(){
            Node servletsCCNode=new ServletsCacheConfigNode();
            addSection(new SectionPanel(this,servletsCCNode,webext));
            return servletsCCNode;
        }
         
        public SectionContainer getServletsContainer() {
            return servletsCont;
        }
        
    }
    
     private class AddServletAction extends javax.swing.AbstractAction {
        
        AddServletAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_addJspPG_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            try{
                WSWebExt webext=dObj.getWebExt();
                ExtendedServletsType es=new ExtendedServletsType();
                es.setDefaults();
                int number=webext.getExtendedServlets().length+1;
                long time_id=java.lang.System.currentTimeMillis();
                        
                es.setExtendedServlet("");        
                es.setXmiId("ServletExtension_"+time_id);
                es.setHref("");
                
                //rr.setTitle("Binding Item #"+number);
                webext.addExtendedServlets(es);
                //////////////////////////////////
                Node servletNode = new ServletsNode();
                view.getRoot().getChildren().add(new Node[]{servletNode});
                servletNode.setDisplayName(servletNode.getDisplayName()+number);
                dObj.setChangedFromUI(true);
                dObj.modelUpdatedFromUI();
                ExtendedServletsType[] servlets = webext.getExtendedServlets();
                SectionPanel sectionPanel=new SectionPanel(view,servletNode,es);
                sectionPanel.setHeaderActions(new javax.swing.Action[]{removeServletAction});
                ((WSWebExtView)view).getServletsContainer().addSection(sectionPanel,true);
                dObj.setChangedFromUI(false);
                
            } catch (java.io.IOException ex) {
            } catch (java.lang.IllegalArgumentException ex) {
            }
        }
    }
    private class RemoveServletAction extends javax.swing.AbstractAction {
        
        RemoveServletAction(String actionName) {
            super(actionName);
            //char mnem = NbBundle.getMessage(PagesMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            //putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
            ExtendedServletsType servlet = (ExtendedServletsType)sectionPanel.getKey();
            // removing from data model
            
            try {
                dObj.setChangedFromUI(true);
                dObj.getWebExt().removeExtendedServlets(servlet);
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
         
            WSWebExt webext = dObj.getWebExt();
         
            ResRefBindingsType[] resrefs = webext.getResRefBindings();
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
         
            EjbRefBindingsType[] ejbrefs = webext.getEjbRefBindings();
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
         
            ResEnvRefBindingsType[] resenvrefs = webext.getResEnvRefBindings();
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
    
    
    public static class WSWebExtNode extends org.openide.nodes.AbstractNode {
        WSWebExtNode(WSWebExt webext) {
            super(org.openide.nodes.Children.LEAF);
            //setDisplayName(webext.getXmiId());
            setDisplayName("Extended Deployment Information");
            //setIconBaseWithExtension("org/netbeans/modules/webextmultiview/ws6.gif"); //NOI18N
        }
    }
    
    public static class ServletsNode extends org.openide.nodes.AbstractNode {
        ServletsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Extended Servlet #");
            //setIconBaseWithExtension("org/netbeans/modules/webextmultiview/ws.gif"); //NOI18N
        }
    }
    public static class ServletsCacheConfigNode extends org.openide.nodes.AbstractNode {
        ServletsCacheConfigNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName("Servlet Cache Configs");
            //setIconBaseWithExtension("org/netbeans/modules/webextmultiview/ws.gif"); //NOI18N
        }
    }   
}
