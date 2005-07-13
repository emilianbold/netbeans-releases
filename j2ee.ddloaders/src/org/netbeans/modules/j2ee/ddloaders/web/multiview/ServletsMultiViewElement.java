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
/**
 *
 * @author mkuchtiak
 */
public class ServletsMultiViewElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener {
    private SectionView view;
    private ToolBarDesignEditor comp;
    private DDDataObject dObj;
    private WebApp webApp;
    private ServletPanelFactory factory;
    private javax.swing.Action addAction, removeAction;
    private boolean needInit=true;
    private int index;
    private RequestProcessor.Task repaintingTask;
    private static final String SERVLET_MV_ID=DDDataObject.DD_MULTIVIEW_PREFIX+DDDataObject.MULTIVIEW_SERVLETS;
    private static final String HELP_ID_PREFIX=DDDataObject.HELP_ID_PREFIX_SERVLETS;
    
    /** Creates a new instance of DDMultiViewElement */
    public ServletsMultiViewElement(final DDDataObject dObj, int index) {
        super(dObj);
        this.dObj=dObj;
        this.index=index;
        comp = new ToolBarDesignEditor();
        factory = new ServletPanelFactory(comp, dObj);
        addAction = new AddAction(dObj, NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_addServlet"));
        removeAction = new RemoveAction(NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_remove"));
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
            if ( name.indexOf("Servlet")>0 ) { //NOI18
                // repaint view if the wiew is active and something is changed with servlets
                if (SERVLET_MV_ID.equals(dObj.getSelectedPerspective().preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }
    
    private void repaintView() {
        webApp = dObj.getWebApp();
        view = new ServletsView(webApp);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
            ServletsView servletsView = (ServletsView)view;
            Node initialNode = servletsView.getServletsNode();
            Children ch = initialNode.getChildren();
            if (ch.getNodesCount()>0) 
                initialNode = ch.getNodes()[0];
            servletsView.selectNode(initialNode);
        }
        view.checkValidity();
        dObj.checkParseable();
    }

    class ServletsView extends SectionView {
        private SectionContainer servletsCont;
        private Node servletsNode;
        
        ServletsView (WebApp webApp) {
            super(factory);
            Servlet[] servlets = webApp.getServlet();
            Node[] nodes = new Node[servlets.length];
            Children ch = new Children.Array();
            for (int i=0;i<nodes.length;i++) {
                nodes[i] = new ServletNode(this,webApp,servlets[i]);
            }
            ch.add(nodes);
            servletsNode = new SectionContainerNode(ch);
            servletsCont = new SectionContainer(this,servletsNode,
                NbBundle.getMessage(ServletsMultiViewElement.class,"TTL_servlets"),false);
            servletsCont.setHeaderActions(new javax.swing.Action[]{addAction});
            Children servletsChildren = new Children.Array();
            servletsChildren.add(new Node[]{servletsNode});
            AbstractNode root = new AbstractNode(servletsChildren);
            SectionPanel[] pan = new SectionPanel[servlets.length];
            for (int i=0;i<nodes.length;i++) {
                pan[i] = new SectionPanel(this, nodes[i], getServletTitle(servlets[i]),servlets[i]);
                pan[i].setHeaderActions(new javax.swing.Action[]{removeAction});
                servletsCont.addSection(pan[i]);
            }
            addSection(servletsCont);
            //root.setDisplayName("<Servlets>");
            servletsNode.setDisplayName(NbBundle.getMessage(ServletsMultiViewElement.class,"TTL_servlets"));
            servletsNode.setName(HELP_ID_PREFIX+"servletsNode"); //NOI18N
            setRoot(root);
        }
        
        Node getServletsNode() {
            return servletsNode;
        }
        
        SectionContainer getServletsContainer(){
            return servletsCont;
        }
        
        String getServletTitle(Servlet servlet) {
            String servletName=servlet.getServletName();
            if (servletName==null) servletName="";
            String mappings = DDUtils.urlPatternList(DDUtils.getUrlPatterns(webApp,servlet));
            return NbBundle.getMessage(ServletsMultiViewElement.class,"TTL_servletPanel",servletName,mappings);
        }
        
        public Error validateView() {
            return SectionValidator.validateServlets(webApp);
        }
    }
    
    private class ServletNode extends org.openide.nodes.AbstractNode {
        private Servlet servlet;
        private WebApp webApp;
        private SectionView view;
        ServletNode(SectionView view, WebApp webApp, Servlet servlet) {
            super(org.openide.nodes.Children.LEAF);
            this.servlet=servlet;
            this.webApp=webApp;
            this.view=view;
            setDisplayName(servlet.getServletName());
            setIconBase("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/class"); //NOI18N
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"servletNode"); //NOI18N
        }
    }

    private class AddAction extends javax.swing.AbstractAction {
        
        AddAction(final DDDataObject dObj, String actionName) {
            super(actionName);
            char mnem = NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_addServlet_mnem").charAt(0);
            putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {

            String[] labels = new String[]{
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_servletName"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_servletClass"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_jspFile"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_description"),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_urlPatterns")
            };
            char[] mnem = new char[] {
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_servletName_mnem").charAt(0),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_servletClass_mnem").charAt(0),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_jspFile_mnem").charAt(0),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_description_mnem").charAt(0),
                NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_urlPatterns_mnem").charAt(0)
            };
            boolean[] buttons = new boolean[]{false,true,true,false,false};
            SimpleDialogPanel.DialogDescriptor descriptor = new SimpleDialogPanel.DialogDescriptor(labels);
            descriptor.setMnemonics(mnem);
            descriptor.setButtons(buttons);
            descriptor.setTextField(new boolean[]{true,true,true,false,true});
            
            final SimpleDialogPanel dialogPanel = new SimpleDialogPanel(descriptor);
            dialogPanel.getCustomizerButtons()[0].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = DDUtils.getJavaSourceGroups(dObj);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String className = DDUtils.getResourcePath(groups,fo);
                            dialogPanel.getTextComponents()[1].setText(className);
                        }
                    } catch (java.io.IOException ex) {}
                }
            });
            dialogPanel.getCustomizerButtons()[1].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String res = "/"+DDUtils.getResourcePath(groups,fo,'/',true);
                            dialogPanel.getTextComponents()[2].setText(res);
                        }
                    } catch (java.io.IOException ex) {}
                }
            });
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(ServletsMultiViewElement.class,"TTL_Servlet"),true) {
                protected String validate() {
                    String[] values = dialogPanel.getValues();
                    String servletName = values[0].trim();
                    String servletClass = values[1].trim();
                    String jspFile = values[2].trim();
                    String urlPatterns = values[4];
                    return SectionValidator.validateNewServlet(dObj.getWebApp(), servletName, servletClass, jspFile, urlPatterns);
                }
            };
            dialog.setValid(false); // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getTextComponents()[0].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[2].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[4].getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.show();
            dialogPanel.getTextComponents()[0].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[2].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[4].getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                String[] values = dialogPanel.getValues();
                dObj.modelUpdatedFromUI();
                try {
                    Servlet servlet = (Servlet)webApp.createBean("Servlet"); //NOI18N
                    servlet.setServletName(values[0].trim());
                    String servletClass = values[1].trim();
                    if (servletClass.length()>0) servlet.setServletClass(servletClass);
                    else servlet.setJspFile(values[2].trim());
                    String desc = values[3].trim();
                    if (desc.length()>0) servlet.setDescription(desc);
                    webApp.addServlet(servlet);
                    
                    String[] patterns = DDUtils.getStringArray(values[4]);
                    DDUtils.addServletMappings(webApp,servlet,patterns);

                    ServletsView view = (ServletsView)comp.getContentView();
                    Node node = new ServletNode(view, webApp, servlet);
                    view.getServletsNode().getChildren().add(new Node[]{node});

                    SectionPanel pan = new SectionPanel(view, node, view.getServletTitle(servlet), servlet);
                    pan.setHeaderActions(new javax.swing.Action[]{removeAction});
                    view.getServletsContainer().addSection(pan, true);
                } catch (ClassNotFoundException ex){}
            }
        }
    }
    private class RemoveAction extends javax.swing.AbstractAction {
        
        RemoveAction(String actionName) {
            super(actionName);
            char mnem = NbBundle.getMessage(ServletsMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            putValue(MNEMONIC_KEY,new Integer((int)mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            org.openide.DialogDescriptor desc = new ConfirmDialog(
                NbBundle.getMessage(ServletsMultiViewElement.class,"TXT_removeServletConfirm"));
            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
            dialog.show();
            if (org.openide.DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
                Servlet servlet = (Servlet)sectionPanel.getKey();
                // updating data model
                dObj.modelUpdatedFromUI();
                DDUtils.removeServletMappings(webApp,servlet.getServletName());
                DDUtils.removeFilterMappingsForServlet(webApp,servlet.getServletName());
                webApp.removeServlet(servlet);
                
                // removing section
                sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
            }
        }
    }
}
