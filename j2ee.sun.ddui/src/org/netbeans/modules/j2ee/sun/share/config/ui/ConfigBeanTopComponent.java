/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ConfigBeanTopComponent.java
 *
 * Created on March 6, 2003, 2:09 PM
 */

package org.netbeans.modules.j2ee.sun.share.config.ui;


import java.awt.BorderLayout;
import java.io.IOException;
import java.util.*;

import javax.enterprise.deploy.shared.ModuleType;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

import org.openide.*;
import org.openide.NotifyDescriptor.Message;
import org.openide.cookies.SaveCookie;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.openide.nodes.Children.Array;
import org.openide.windows.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.HelpCtx;

import org.netbeans.api.project.FileOwnerQuery;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.netbeans.modules.j2ee.sun.share.config.ConfigDataObject;
import org.netbeans.modules.j2ee.sun.share.config.ConfigurationStorage;
import org.netbeans.modules.j2ee.sun.share.config.SecondaryConfigDataObject;

import com.sun.tools.j2ee.editor.ComponentPanel;
import com.sun.tools.j2ee.editor.PanelView;

/**
 *
 * @author  Jeri Lockhart
 */
public class ConfigBeanTopComponent extends CloneableTopComponent {
    
    private ConfigDataObject configDO = null;
    private Node rootNode = null;   // Root node for the editor's explorer
    private TwoPanelComponentPanel componentPanel;
    
    public static final String APPLICATION_ICON_NORMAL =
        "org/netbeans/modules/j2ee/sun/share/config/ui/resources/application.gif";	// NOI18N
    public static final String EJBMODULE_ICON_NORMAL =
        "org/netbeans/modules/j2ee/sun/share/config/ui/resources/ejbmodule.gif";	// NOI18N
    public static final String WEBMODULE_ICON_NORMAL =
        "org/netbeans/modules/j2ee/sun/share/config/ui/resources/webmodule.gif";	// NOI18N
    // Also these types:
    //     ModuleType.RAR;
    //     ModuleType.CAR;
    
    private boolean appConfig = false;
    
    /** default constructor for deserialization */
    public ConfigBeanTopComponent() {
        // hint to windows system
        putClientProperty("PersistenceType", "Never"); //NOI18N
        //        putClientProperty("PersistenceType", "OnlyOpened"); // NOI18N
        
    }
    
    /** Creates a new instance of ConfigBeanTopComponent */
    public ConfigBeanTopComponent(ConfigDataObject dobj) {
        this();
        this.configDO = dobj;
        initialize();
    }
    
    public boolean isFor(FileObject document) {
        return (this.configDO.getPrimaryFile().equals(document));
    }
    
    public boolean isFor(ConfigDataObject configDO) {
        return (this.configDO == configDO);
    }
    
    public boolean isFor(SecondaryConfigDataObject configDO) {
        return configDO.isSecondaryOf(this.configDO);
    }

    private ConfigurationStorage getConfigStorage() {
        if (configDO != null) {
            return (ConfigurationStorage) configDO.getCookie(ConfigurationStorage.class);
        }
        return null;
    }
    
    public static ConfigBeanTopComponent findByConfigStorage(ConfigurationStorage configStorage) {
        Iterator it  = TopComponent.getRegistry().getOpened().iterator();
        while (it.hasNext()) {
            TopComponent tc = (TopComponent) it.next();
            if (tc instanceof ConfigBeanTopComponent) {
                ConfigBeanTopComponent beanTC = (ConfigBeanTopComponent) tc;
                if (configStorage == beanTC.getConfigStorage()) {
                    return beanTC;
                }
            }
        }
        return null;
    }
    
    public void refresh() {
        try {
            rootNode = buildTree();
            Node mainNode = rootNode.getChildren().getNodes()[0];
            componentPanel.refresh(rootNode, mainNode); //todo: get currently selected node.
            //open();
        } catch (java.lang.Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public int getPersistenceType () {
        return PERSISTENCE_NEVER;
    }
    
    protected String preferredID() {
        if (configDO != null)
            return configDO.getPrimaryFile().getPath();
        else
            return "";
    }

    /** Initializes this instance. Used by construction and deserialization. */
    public void initialize() {
        Node selNode = configDO.getNodeDelegate ();

        try {
            if (getConfigStorage() == null) {
                throw new IllegalArgumentException("ConfigDataObject without ConfigurationStorage cookie!"); //NOI18N
            }
        } catch (RuntimeException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,  ex);
            return;
        }
        
        initComponents();
        
        //setName(NbBundle.getMessage (ConfigBeanTopComponent.class, "LBL_Server_specific_settings", selNode.getDisplayName()));
        setName(selNode.getDisplayName());
        String fsName = "";
        FileObject fo = configDO.getPrimaryFile ();
        try {
            fsName = fo.getFileSystem ().getDisplayName () + "/" + fo.getPath (); //NOI18N
        } catch (org.openide.filesystems.FileStateInvalidException fse) {
            fsName = fo.getPath ();
        }
        char sep = java.io.File.separatorChar;
        char another = sep == '/' ? '\\' : '/';
        fsName = fsName.replace (another, sep);
        setToolTipText (fsName);
        setActivatedNodes(new Node[] { selNode, rootNode.getChildren().getNodes()[0] });
        setIcon (Utilities.loadImage ("org/netbeans/modules/j2ee/sun/share/config/ui/resources/ConfigFile.gif")); //NOI18N
    }
    
    public void reset() {
        configDO = null;
        rootNode = null;
        //panelView = null;
        componentPanel = null;
        appConfig = false;
    }
    
    /** Inits the subcomponents. Sets layout for this top component .
     * @see BundleEditPanel */
    private void initComponents() {
        try {
            rootNode = buildTree();
            Node mainNode = rootNode.getChildren().getNodes()[0];
            setLayout(new BorderLayout());
            PanelView panelView = new ConfigBeanPanelView(rootNode);
            componentPanel = new TwoPanelComponentPanel(panelView, appConfig);
            add(BorderLayout.CENTER, componentPanel);
            componentPanel.getExplorerManager().setSelectedNodes(new Node[] { mainNode });
            panelView.showSelection(new Node[] { mainNode });
        } catch (IllegalArgumentException ie) {
            ErrorManager.getDefault().log(ie.getMessage());
        } catch (java.lang.Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    private Node getMainNode() {
        return getConfigStorage().getMainNode();
    }
    
    private J2eeModule getProvider() {
        J2eeModuleProvider provider = (J2eeModuleProvider) FileOwnerQuery.getOwner (configDO.getPrimaryFile ()).getLookup ().lookup (J2eeModuleProvider.class);
        return provider.getJ2eeModule ();
    }
    
    private Node buildTree() {
        Node filterRoot = configDO.getNodeDelegate();
        J2eeModule prov = getProvider();
        AbstractNode root = null;
        Array  children = new Array();
        /*if (prov instanceof J2eeModuleContainer) {
            appConfig = true;
            J2eeModuleContainer appProv = (J2eeModuleContainer) prov;
            ArrayList childArr = new ArrayList();
            // The first child is the App's ConfigBeanNode
            Node[] nodes = getConfigStorage().getMainNodes();
            for (int i = 0; i < nodes.length; i++) {
                childArr.add(nodes[i]);
            }
            // Add nodes for modules
            J2eeModule[] mods = appProv.getModules(getModuleContainerListener());
            for (int i = 0; i < mods.length; i++) {
                childArr.addAll(Arrays.asList(createModuleNode(mods[i])));
            }
            //            create the app Node
            children.add((Node[]) childArr.toArray(new Node[childArr.size()]));
            root = new AbstractNode(children);
            root.setName(filterRoot.getName());
            root.setIconBaseWithExtension(APPLICATION_ICON_NORMAL);
        } else */ { // not a j2ee app 
            Node[] beanNodes = getConfigStorage().getMainNodes();
            children.add(beanNodes);
            root = new AbstractNode(children);
        }
        return root;
    }
    
    private Node[] createModuleNode(J2eeModule module) {
        Array modChildren = new Array();
        Node[] modConfigBeanNode = getConfigStorage().getNodes(module);
        for (int j = 0; j < modConfigBeanNode.length; j++) {
            if (modConfigBeanNode[j] != null) {
                modChildren.add(new Node[] { modConfigBeanNode[j] });
            }
        }

        AbstractNode modNode = new AbstractNode(modChildren);
        modNode.setName(module.getUrl());
        if (module.getModuleType() == ModuleType.EJB) {
            modNode.setIconBaseWithExtension(EJBMODULE_ICON_NORMAL);
        } else if (module.getModuleType() == ModuleType.WAR) {
            modNode.setIconBaseWithExtension(WEBMODULE_ICON_NORMAL);
        }
        return new Node[] { modNode };
    }
    
    protected void componentClosed () {
        super.componentClosed();
        if (configDO != null) {
            configDO.editorClosed ();
        }
    }
    
    public void open() {
        super.open();
        //refresh();
    }
    
  /*
   *  Find the "root" App, Module or Ear Node for this node or subnode
   *  If the  currNode is the "root", return it.
   */
    //    private Node getRoot(Node currNode) {
    //        Node parent = currNode.getParentNode();
    //        if (parent != null) {
    //            if (parent.getCookie(J2eeComponentProvider.class) == null) {
    //                return currNode;
    //            }
    //            return getRoot(parent);
    //        }
    //        return currNode;
    //    }
    
    
    public boolean closeLast() {
        super.closeLast();
        if (configDO != null && configDO.isModified ()) {
            
            ResourceBundle bundle = NbBundle.getBundle(ConfigBeanTopComponent.class);
            
            String msg = NbBundle.getMessage(ConfigBeanTopComponent.class, "MSG_ConfirmSave", configDO.getName()); // NOI18N
            
            JButton saveOption = new JButton(bundle.getString("CTL_Save")); // NOI18N
            saveOption.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Save")); // NOI18N
            saveOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Save")); // NOI18N
            JButton discardOption = new JButton(bundle.getString("CTL_Discard")); // NOI18N
            discardOption.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Discard")); // NOI18N
            discardOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Discard")); // NOI18N
            discardOption.setMnemonic(bundle.getString("CTL_Discard_Mnemonic").charAt (0)); // NOI18N

            NotifyDescriptor nd = new NotifyDescriptor(
                msg,
                bundle.getString("LBL_SaveFile_Title"), // NOI18N
                NotifyDescriptor.YES_NO_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                new Object[] {saveOption, discardOption, NotifyDescriptor.CANCEL_OPTION},
                saveOption
            );
                
            Object ret = DialogDisplayer.getDefault().notify(nd);
            
             if(saveOption.equals(ret)){
                try {
                    SaveCookie sc =  (SaveCookie)configDO.getCookie (SaveCookie.class);
                    if (sc != null) {
                        sc.save ();
                    }
                }
                catch (IOException e) {
                    DialogDisplayer.getDefault().notify(new Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                }
             } else if (discardOption.equals(ret)){
                 try {	 
                     configDO.setModified (false);
                     configDO.resetAllChanged();
                     getConfigStorage().load ();	 
                 } catch (java.lang.Exception e) {	 
                     DialogDisplayer.getDefault().notify(new Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));	 
                 }	 
             } else {  // user pressed Cancel - don't close	 
                     return false;	 
             }
         }
        // remove module nodes from app node
        if (appConfig = true && rootNode != null) {
            Children modules = rootNode.getChildren();
            Node[] modNodes = modules.getNodes();
            for (int i = 0; i < modNodes.length; i++) {
                if (modNodes[i] instanceof AbstractNode ) {
                    AbstractNode mod = (AbstractNode)modNodes[i];
                    Children ch = mod.getChildren();
                    ch.remove(ch.getNodes());
                }
            }
            modules.remove(modules.getNodes());
            
        }
        
        
        return true;
    }
    
    /**
     * Overrides superclass method.
     * Is called from the superclass <code>clone<code> method to create new component from this one.
     * This implementation only clones the object by calling super.clone method.
     * @return the copy of this object
     */
    protected CloneableTopComponent createClonedObject() {
        return new ConfigBeanTopComponent(this.configDO);
    }
    
    private class TwoPanelComponentPanel extends ComponentPanel {
        
        protected TwoPanelComponentPanel(){
            super();
        }
        /**
         * Creates a new instance of ComponentPanel
         * @param panel The PanelView which will provide the node tree for the structure view
         *              and the set of panels the nodes map to.
         */
        public TwoPanelComponentPanel(PanelView panel){
            super(panel);
        }
        
        public TwoPanelComponentPanel(PanelView panel, boolean rootVisible){
            super(panel);
            JComponent view = getStructureView();
            if (view instanceof TreeView) {
                TreeView tree = (TreeView) view;
                tree.setRootVisible(rootVisible);
            }
        }

        public HelpCtx getHelpCtx () {
            Node[] nodes = this.getExplorerManager().getSelectedNodes();
            if (nodes.length > 0) {
                return nodes[0].getHelpCtx();
            } else if (rootNode != null) {
                return rootNode.getHelpCtx();
            } else if (configDO != null) {
                return configDO.getHelpCtx();
            } else {
                return HelpCtx.DEFAULT_HELP;
            }
        }
        
        protected void createHorizontalSplit() {
            if (panelOrientation == 1)
                split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,getContentView(), getStructureView());
            else
                split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,getStructureView(), getContentView());
            //split1.setDividerLocation(DEFAULT_CONTENT_SPLIT);
            split1.setDividerSize (4);
        }
        
        protected void createVerticalSplit() {
            
        }
        
        protected PanelView getPanelView() {
            return (PanelView) contentView;
        }
        
        public void refresh(Node root, Node selected) throws java.beans.PropertyVetoException {
            getPanelView().setRoot(root);
            setRootContext(root);
            getExplorerManager().setSelectedNodes(new Node[] { selected });
            getPanelView().showSelection(new Node[] { selected });
        }
    }
}
