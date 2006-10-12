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

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

import org.netbeans.modules.j2ee.sun.share.config.ConfigDataObject;
import org.netbeans.modules.j2ee.sun.share.config.ConfigurationStorage;
import org.netbeans.modules.j2ee.sun.share.config.SecondaryConfigDataObject;


/**
 *
 * @author  Jeri Lockhart
 */
public class ConfigBeanTopComponent extends CloneableTopComponent
{
    
//    private static int identitySource;
//    private final int identity;
//
//    public int getIdentity() {
//        return identity;
//    }

    private ConfigurationStorage storage = null;
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
//        identity = ++identitySource;

        // hint to windows system
        putClientProperty("PersistenceType", "Never"); //NOI18N
        //        putClientProperty("PersistenceType", "OnlyOpened"); // NOI18N
//        System.out.println("ConfigBeanTopComponent::ctor id #" + getIdentity());
    }
    
    /** Creates a new instance of ConfigBeanTopComponent */
    public ConfigBeanTopComponent(ConfigurationStorage storage) {
        this();
        this.storage = storage;
        initialize();
    }
    
    public ConfigDataObject getConfigDataObject() {
        return storage.getPrimaryDataObject();
    }
    
    public void setName(String name) {
        if(name != null && name.startsWith("sun-ejb-jar")) {
            name = "sun-ejb-jar.xml / sun-cmp-mappings.xml";
        }
        
//        new Exception("Who called ConfigBeanTopComponent.setName( '" + name + "' ) -- ").printStackTrace();
        super.setName(name);
    }

//    public String getName() {
//        String result;
//        result = super.getName();
//        System.out.println("CBTC.getName() returned '" + result + "'");
//        return result;
//    }
//    
//    public String getDisplayName() {
//        String result;
//        result = super.getDisplayName();
//        System.out.println("CBTC.getDisplayName() returned '" + result + "'");
//        return result;
//    }
//
//    public void setDisplayName(String displayName) {
//        System.out.println("CBTC.setDisplayName() called with '" + displayName + "'");
//        new Exception("Who called ConfigBeanTopComponent.setDisplayName( '" + displayName + "' ) -- ").printStackTrace();
//        super.setDisplayName(displayName);
//    }

    public boolean isFor(FileObject document) {
        boolean result = false;
        ConfigDataObject configDO = getConfigDataObject();
        if(configDO != null) {
            result = configDO.getPrimaryFile().equals(document);
        }
        return result;
    }
    
    public boolean isFor(ConfigurationStorage otherStorage) {
        return getConfigStorage() == otherStorage;
    }
    
    private ConfigurationStorage getConfigStorage() {
        return storage;
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
            Node [] topNodes = rootNode.getChildren().getNodes();
            if(topNodes.length > 0) {
                Node mainNode = topNodes[0];
                componentPanel.refresh(rootNode, mainNode); //todo: get currently selected node.
                //open();
            } else {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                    "ConfigBeanTopComponent: empty top level node list.  Root: " + rootNode + ", topNodes: " + topNodes);
            }
        } catch (java.lang.Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public int getPersistenceType () {
        return PERSISTENCE_NEVER;
    }
    
    protected String preferredID() {
        String preferredID = ""; // NOI18N
        ConfigDataObject configDO = getConfigDataObject();
        if(configDO != null) {
            preferredID = configDO.getPrimaryFile().getPath();
        }
        return preferredID;
    }

    /** Initializes this instance. Used by construction and deserialization. */
    public void initialize() {
//        System.out.println("ConfigBeanTopComponent::initialize() called on #" + getIdentity());
        ConfigDataObject configDO = getConfigDataObject();
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
        Node [] topNodes = rootNode.getChildren().getNodes();
        
        // Only one activated node at a time, otherwise <Save> enabling doesn't work.  See IZ 65225
        Node [] activatedNode = new Node[1];
        activatedNode[0] = (topNodes.length > 0) ? topNodes[0] : selNode;
        setActivatedNodes(activatedNode);
        
        setIcon (Utilities.loadImage ("org/netbeans/modules/j2ee/sun/share/config/ui/resources/ConfigFile.gif")); //NOI18N
    }
    
    public synchronized void reset() {
//        System.out.println("ConfigBeanTopComponent::reset() called on #" + getIdentity());
        rootNode = null;
        componentPanel = null;
        appConfig = false;
    }
    
    /** Inits the subcomponents. Sets layout for this top component .
     * @see BundleEditPanel */
    private void initComponents() {
        try {
            rootNode = buildTree();
            Node [] topNodes = rootNode.getChildren().getNodes();
            Node mainNode = null;
            if(topNodes.length > 0) {
                mainNode = topNodes[0];
            }
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
    
    private Node buildTree() {
        ConfigDataObject configDO = getConfigDataObject();
        Node filterRoot = configDO.getNodeDelegate();
        AbstractNode root = null;
        Array  children = new Array();
        /*J2eeModule prov = getProvider();
        if (prov instanceof J2eeModuleContainer) {
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
//        System.out.println("ConfigBeanTopComponent::componentClosed() called on #" + getIdentity());
        super.componentClosed();
        ConfigDataObject configDO = getConfigDataObject();
        if (configDO != null) {
            configDO.editorClosed(this);
        } else {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
//                    "ConfigBeanTopComponent.componentClosed(): Top component has null dataobject!")); // NOI18N
            // No data object means file was deleted from underneath the editor.
//            System.out.println("No data object for config editor being closed.");
//            System.out.println("  Storage is " + storage);
        }
    }
    
    public void open() {
//        System.out.println("ConfigBeanTopComponent::open() called on #" + getIdentity());
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
//        System.out.println("ConfigBeanTopComponent::closeLast() called on #" + getIdentity());
        super.closeLast();
        ConfigDataObject configDO = getConfigDataObject();
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
// This message is displayed in SaveCookie.save(), called above.                    
//                    DialogDisplayer.getDefault().notify(new Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                    // Error performing save, don't close.
                    return false;
                }
             } else if (discardOption.equals(ret)){
                 try {	 
                     configDO.setModified (false);
                     configDO.resetAllChanged();
                     getConfigStorage().load();
                 } catch (java.lang.Exception ex) {	 
                     // Added log message here because we were getting a ConcurrentModificationException
                     // (caused by coding bug) but the message box was empty.
                     ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                     DialogDisplayer.getDefault().notify(new Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
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
//        System.out.println("ConfigBeanTopComponent::createClonedObject() called on #" + getIdentity());
        return new ConfigBeanTopComponent(storage);
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
            } else {
                ConfigDataObject configDO = getConfigDataObject();
                if (configDO != null) {
                    return configDO.getHelpCtx();
                } else {
                    return HelpCtx.DEFAULT_HELP;
                }
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
