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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.visualweb.project.jsf.actions;

import org.netbeans.modules.visualweb.project.jsf.api.JsfPortletSupport;
import org.netbeans.modules.visualweb.project.jsf.api.JsfPortletSupportException;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;

import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.api.portlet.dd.PortletModeType;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.openide.awt.JMenuPlus;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.actions.Presenter;


/**
 * Action for setting the start (initial) page.
 * Formerly there were two actions put into project/jsfloader module, which was not a proper place.
 *
 * @author Peter Zavadsky (refactored previous actions)
 * @author Mark Dey (originally action for jsp)
 * @author David Botterill (originally action for portlet)
 */
public class SetStartPageAction extends AbstractAction
        implements Presenter.Menu, Presenter.Popup, ContextAwareAction {

    private static final int TYPE_NONE    = 0;
    private static final int TYPE_JSP     = 1;
    private static final int TYPE_PORTLET = 2;

    private final int type;

    private final FileObject fo;
    private final DataObject dataObject;


    /** Creates a new instance of SetStartPageAction */
    public SetStartPageAction() {
        this(TYPE_NONE, null); // Fake action -> The context aware is real one, drawback of the NB design?
    }

    private SetStartPageAction(int type, DataObject inDataObject) {
        this.type = type;
        this.dataObject = inDataObject;

        if(null != inDataObject) {
            this.fo = dataObject.getPrimaryFile();
        } else this.fo = null;

        String name;
        if(type == TYPE_JSP) {
            name = NbBundle.getMessage(SetStartPageAction.class, "LBL_SetAsStartPage");
        } else if(type == TYPE_PORTLET) {
            name = NbBundle.getMessage(SetStartPageAction.class, "LBL_SetInitalPageAction_SETINITIALVIEWPAGE");
        } else {
            name = null;
        }
        putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent evt) {
        if(type == TYPE_JSP) {
            // Copy from previous SetAsStartPageAction (Mark)
            String newStartPage = JsfProjectUtils.setStartPage(fo);
            String msg = newStartPage != null ?
                NbBundle.getMessage(SetStartPageAction.class, "MSG_StartPageChanged") + " " + newStartPage :
                NbBundle.getMessage(SetStartPageAction.class, "MSG_NoStartPage");

            StatusDisplayer.getDefault().setStatusText(msg);
        } else if(type == TYPE_PORTLET) {
            // Copy from previous SetInitialPageAction (David)
            Project project = FileOwnerQuery.getOwner(fo);
            if (project == null)
                return;
            JsfPortletSupport portletSupport = JsfProjectUtils.getPortletSupport(project);
            if (portletSupport == null) return;
            String actionCommand = evt.getActionCommand();
            
            try {
                /**
                 * Fix for CR  6337056.  Need to get the root path to the JSP files to be able 
                 * to set the currently set initial page icon back to the default one.
                 * -David Botterill 10/14/2005
                 */
                /**
                 * We need the path to the JSP root directory so we can use it to set the icons.
                 */

                FileObject jspRootFO = JsfProjectUtils.getDocumentRoot(project);
                File jspRootFile = FileUtil.toFile(jspRootFO);
                String dataNodePath = null;
                try {
                    dataNodePath = jspRootFile.getCanonicalPath();
                } catch (IOException ioe) {
                   ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ioe);
                }
                if(actionCommand.equals(NbBundle.getMessage(SetStartPageAction.class, "MNU_VIEWMODE"))) {
                    /**
                     * Set the icon for the current one for this mode to the default icon.
                     */
                    String currentViewPage = portletSupport.getInitialPage(PortletModeType.VIEW);
                    if(null != currentViewPage) {
                        FileObject currentFO = FileUtil.toFileObject(new File(dataNodePath + File.separator + currentViewPage));
                        /**
                         * Fix for CR 6329425
                         * Make sure the page is found since the user may have deleted the page after they set the initial
                         * mode.
                         * -David Botterill 9/28/2005
                         */
                        if(null != currentFO) {
                            try {
                                DataObject currentDO = DataObject.find(currentFO);
                                ((DataNode)currentDO.getNodeDelegate()).setIconBase("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject.gif"); //NOI18N
                            } catch(DataObjectNotFoundException donfe) {
                                NbBundle.getMessage(SetStartPageAction.class,
                                        "MSG_UnableToSetDefaultIcon", currentViewPage);
                            }
                        }
                        
                    }
                    portletSupport.setInitialPage(PortletModeType.VIEW, fo);                    
                    /**
                     * Now set the right data node with the mode icon.
                     */
                    ((DataNode)dataObject.getNodeDelegate()).setIconBase("org/netbeans/modules/visualweb/project/jsfloader/resources/initialviewpage.png"); //NOI18N
                    ((DataNode)dataObject.getNodeDelegate()).setShortDescription(NbBundle.getMessage(SetStartPageAction.class, "LBL_InitialViewShortDesc"));                    
                } else if(actionCommand.equals(NbBundle.getMessage(SetStartPageAction.class, "MNU_EDITMODE"))) {
                    /**
                     * Set the icon for the current one for this mode to the default icon.
                     */
                    String currentEditPage = portletSupport.getInitialPage(PortletModeType.EDIT);
                    if(null != currentEditPage) {
                        FileObject currentFO = FileUtil.toFileObject(new File(dataNodePath + File.separator + currentEditPage));
                        /**
                         * Fix for CR 6329425
                         * Make sure the page is found since the user may have deleted the page after they set the initial
                         * mode.
                         * -David Botterill 9/28/2005
                         */
                        if(null != currentFO) {
                            try {
                                DataObject currentDO = DataObject.find(currentFO);
                                ((DataNode)currentDO.getNodeDelegate()).setIconBase("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject.gif"); //NOI18N
                            } catch(DataObjectNotFoundException donfe) {
                                NbBundle.getMessage(SetStartPageAction.class,
                                        "MSG_UnableToSetDefaultIcon", currentEditPage);
                            }
                        }
                        
                    }
                    portletSupport.setInitialPage(PortletModeType.EDIT, fo);
                    /**
                     * Now set the right data node with the mode icon.
                     */
                    ((DataNode)dataObject.getNodeDelegate()).setIconBase("org/netbeans/modules/visualweb/project/jsfloader/resources/initialeditpage.png");//NOI18N
                    ((DataNode)dataObject.getNodeDelegate()).setShortDescription(NbBundle.getMessage(SetStartPageAction.class, "LBL_InitialEditShortDesc"));
                } else if(actionCommand.equals(NbBundle.getMessage(SetStartPageAction.class, "MNU_HELPMODE"))) {
                    /**
                     * Set the icon for the current one for this mode to the default icon.
                     */
                    String currentHelpPage = portletSupport.getInitialPage(PortletModeType.HELP);
                    FileObject currentFO = FileUtil.toFileObject(new File(dataNodePath + File.separator + currentHelpPage));
                    /**
                     * Fix for CR 6329425
                     * Make sure the page is found since the user may have deleted the page after they set the initial
                     * mode.
                     * -David Botterill 9/28/2005
                     */
                    if(null != currentFO) {
                        try {
                            DataObject currentDO = DataObject.find(currentFO);
                            ((DataNode)currentDO.getNodeDelegate()).setIconBase("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject.gif"); //NOI18N
                        } catch(DataObjectNotFoundException donfe) {
                            NbBundle.getMessage(SetStartPageAction.class,
                                    "MSG_UnableToSetDefaultIcon", currentHelpPage);
                        }
                    }
                    portletSupport.setInitialPage(PortletModeType.HELP, fo);
                    
                    /**
                     * Now set the right data node with the mode icon.
                     */
                    ((DataNode)dataObject.getNodeDelegate()).setIconBase("org/netbeans/modules/visualweb/project/jsfloader/resources/initialhelppage.png");//NOI18N
                    ((DataNode)dataObject.getNodeDelegate()).setShortDescription(NbBundle.getMessage(SetStartPageAction.class, "LBL_InitialHelpShortDesc"));
                } else if(actionCommand.equals(NbBundle.getMessage(SetStartPageAction.class, "MNU_NONEMODE"))) {
                    /**
                     * Unset the page as an initial page.
                     */
                    portletSupport.unsetInitialPage(fo);
                    
                    /**
                     * Now set the icon to the default icon.
                     */
                       try {
                            DataObject currentDO = DataObject.find(fo);
                    ((DataNode)currentDO.getNodeDelegate()).setIconBase("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject.gif"); //NOI18N
                        } catch(DataObjectNotFoundException donfe) {
                            NbBundle.getMessage(SetStartPageAction.class,
                                    "MSG_UnableToSetDefaultIcon", fo.getNameExt());
                        }
                    

                }
            } catch(JsfPortletSupportException jpse) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, jpse);
            }
        }
    }
    
    public Action createContextAwareInstance(Lookup context) {
        DataObject dob = (DataObject)context.lookup(DataObject.class);
        
        if(dob == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("SetStartPageAction: missing DataObject instance in the context, context=" + context)); // NOI18N
            return null;
        }
        
        
        FileObject fo = dob.getPrimaryFile();
        if(isJspInPortletProject(fo)) {
            return new SetStartPageAction(TYPE_PORTLET, dob);
        } else {
            return new SetStartPageAction(TYPE_JSP, dob);
        }
    }
    // Implementation of Presenter.Menu -----------------------------------
    
    public JMenuItem getMenuPresenter() {
        if(isJspInPortletProject(fo)) {
            JMenu mainItem = new JMenuPlus();
            String name = NbBundle.getMessage(SetStartPageAction.class, "LBL_SetInitalPageAction_SETINITIALVIEWPAGE");
            Mnemonics.setLocalizedText(mainItem,
                    name);
            mainItem.addMenuListener(new InitialItemListener(this));
            
            return mainItem;
        } else {
            JMenuItem mainItem = new JMenuItem();
            String name = NbBundle.getMessage(SetStartPageAction.class, "LBL_SetAsStartPage");
            mainItem.setEnabled(!(JsfProjectUtils.isStartPage(fo) || fo.getExt().equalsIgnoreCase("jspf")));
            Mnemonics.setLocalizedText(mainItem,
                    name);
            mainItem.addActionListener(this);
            return mainItem;
        }
    }
    
    // Implementation of Presenter.Popup ----------------------------------
    
    public JMenuItem getPopupPresenter() {
        if(isJspInPortletProject(fo)) {
            JMenu mainItem = new JMenuPlus();
            String name = NbBundle.getMessage(SetStartPageAction.class, "LBL_SetInitalPageAction_SETINITIALVIEWPAGE");
            Mnemonics.setLocalizedText(mainItem,
                    name);
            mainItem.addMenuListener(new InitialItemListener(this));
            
            return mainItem;
        } else {
            JMenuItem mainItem = new JMenuItem();
            String name = NbBundle.getMessage(SetStartPageAction.class, "LBL_SetAsStartPage");
            mainItem.setEnabled(!(JsfProjectUtils.isStartPage(fo) || fo.getExt().equalsIgnoreCase("jspf")));
            Mnemonics.setLocalizedText(mainItem,
                    name);
            mainItem.addActionListener(this);
            return mainItem;
        }
    }
    
    private static boolean isJspInPortletProject(FileObject fo){
        //check for jsp extension
        if(fo.getExt().compareToIgnoreCase("jsp") == 0){ // NOI18N
            //check if this is a portlet project
            Project thisProj = FileOwnerQuery.getOwner(fo);
            if(JsfProjectUtils.getPortletSupport(thisProj) != null) {
                return true;
            }
        }
        return false;
    }
    
    /** Listens to selection of the INITIAL menu item and expands it
     * into a submenu listing INITIAL modes.
     */
    private class InitialItemListener implements MenuListener {
        ActionListener actionListener;
        
        public InitialItemListener(ActionListener inListener) {
            this.actionListener = inListener;
        }
        
        public void menuCanceled(MenuEvent e) {
        }
        
        public void menuDeselected(MenuEvent e) {
            JMenu menu = (JMenu)e.getSource();
            menu.removeAll();
        }
        
        public void menuSelected(MenuEvent e) {
            JMenu menu = (JMenu)e.getSource();
            ButtonGroup group = new ButtonGroup();
            
            JRadioButtonMenuItem rbViewItem = new JRadioButtonMenuItem(NbBundle.getMessage(SetStartPageAction.class, "MNU_VIEWMODE"));
            rbViewItem.addActionListener(actionListener);
            group.add(rbViewItem);
            menu.add(rbViewItem);            
            rbViewItem.setMnemonic(NbBundle.getMessage(SetStartPageAction.class, "MNE_VIEWMODE").charAt(0));
            
            JRadioButtonMenuItem rbEditItem = new JRadioButtonMenuItem(NbBundle.getMessage(SetStartPageAction.class, "MNU_EDITMODE"));
            rbEditItem.addActionListener(actionListener);
            group.add(rbEditItem);
            menu.add(rbEditItem);            
            rbEditItem.setMnemonic(NbBundle.getMessage(SetStartPageAction.class, "MNE_EDITMODE").charAt(0));
            
            JRadioButtonMenuItem rbHelpItem = new JRadioButtonMenuItem(NbBundle.getMessage(SetStartPageAction.class, "MNU_HELPMODE"));
            rbHelpItem.addActionListener(actionListener);
            group.add(rbHelpItem);
            menu.add(rbHelpItem);            
            rbHelpItem.setMnemonic(NbBundle.getMessage(SetStartPageAction.class, "MNE_HELPMODE").charAt(0));
            
            
            JRadioButtonMenuItem rbNoneItem = new JRadioButtonMenuItem(NbBundle.getMessage(SetStartPageAction.class, "MNU_NONEMODE"));
            rbNoneItem.addActionListener(actionListener);
            group.add(rbNoneItem);
            menu.add(rbNoneItem);            
            rbNoneItem.setMnemonic(NbBundle.getMessage(SetStartPageAction.class, "MNE_NONEMODE").charAt(0));

            /**
             * Now set the one that is currently selected.
             */
            Project project = FileOwnerQuery.getOwner(fo);
            if (project == null)
                return;
            JsfPortletSupport portletSupport = JsfProjectUtils.getPortletSupport(project);
            if(null == portletSupport) return;
            try {
                if(portletSupport.isInitialPage(PortletModeType.VIEW, fo)) {
                    rbViewItem.setSelected(true);
                } else if(portletSupport.isInitialPage(PortletModeType.EDIT, fo)) {
                    rbEditItem.setSelected(true);
                } else if(portletSupport.isInitialPage(PortletModeType.HELP, fo)) {
                    rbHelpItem.setSelected(true);
                } else {
                    rbNoneItem.setSelected(true);
                }
            } catch(JsfPortletSupportException jpse) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, jpse);
            }
            

        }
    }
    
    
}
