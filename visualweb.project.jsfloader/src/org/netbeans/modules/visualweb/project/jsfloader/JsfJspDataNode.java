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


package org.netbeans.modules.visualweb.project.jsfloader;



import org.netbeans.modules.visualweb.project.jsf.api.JsfPortletSupport;
import org.netbeans.modules.visualweb.project.jsf.api.JsfPortletSupportException;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.actions.DeleteAction;
import org.openide.filesystems.FileObject;

import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.api.portlet.dd.PortletModeType;
import java.util.ArrayList;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;

/**
 * Node that represents JSF JSP data object.
 *
 * @author  Peter Zavadsky
 */
public class JsfJspDataNode extends org.openide.loaders.DataNode implements PropertyChangeListener {

    private static final String SHEETNAME_TEXT_PROPERTIES = "textProperties"; // NOI18N
    private static final String PROP_FILE_ENCODING = "encoding"; // NOI18N
    private static final String HELP_ID = "org.netbeans.modules.web.core.syntax.JSPKit";

    public JsfJspDataNode(DataObject dobj, Children ch) {
        super(dobj, ch);
        setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "LBL_JsfJspNodeShortDesc"));
        FileObject thisFileObject = dobj.getPrimaryFile();
        Project project = FileOwnerQuery.getOwner(thisFileObject);
        dobj.addPropertyChangeListener(this);
        JsfProjectUtils.addProjectPropertyListener(project, this);
    }

    public void destroy() throws IOException {
        FileObject thisFileObject = getDataObject().getPrimaryFile();
        Project project = FileOwnerQuery.getOwner(thisFileObject);
        JsfProjectUtils.removeProjectPropertyListener(project, this);

        /**
         * If this is a portlet project and the page is part of an initial mode,
         * we need to unset the initial page.
         * -David Botterill 12/7/2005
         */
        FileObject fo = getDataObject().getPrimaryFile();
        if(null == fo) return;
        JsfPortletSupport portletSupport = this.getPortletSupport(fo);
        if(null != portletSupport) {
            try {
                if(portletSupport.isInitialPage(PortletModeType.VIEW, fo) ||
                        portletSupport.isInitialPage(PortletModeType.EDIT, fo) ||
                        portletSupport.isInitialPage(PortletModeType.HELP, fo)) {
                    portletSupport.unsetInitialPage(fo);
                }
            } catch(JsfPortletSupportException pse) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, pse);
            }
        }

        super.destroy();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(JsfProjectConstants.PROP_START_PAGE)) {
            FileObject thisFileObject = getDataObject().getPrimaryFile();
            Project project = FileOwnerQuery.getOwner(thisFileObject);
            if (evt.getSource() == project) {
                fireIconChange();
            }
        } else if (evt.getPropertyName().equals(DataObject.PROP_NAME)) {
            // Rename start page
            if (JsfProjectUtils.isStartPage(getDataObject().getPrimaryFile())) {
                fireIconChange();
            }
        }
    }
    
    public Image getIcon(int type) {
        Image returnImage = null;
        
        FileObject fo = getDataObject().getPrimaryFile();
        if(null == fo) return null;
        /**
         * If this node is in a Portlet Project, we need to set the icons
         * appropriately if the JSP is an INITIAL VIEW, EDIT, or HELP page.
         * -David Botterill 9/20/2005
         */
        JsfPortletSupport portletSupport = this.getPortletSupport(fo);
        if(null != portletSupport) {
            /**
             * Determine if the current file is an initial page for any of the modes.
             */
            try {
                if(portletSupport.isInitialPage(PortletModeType.VIEW, fo)) {
                    /**
                     * Get the appropriate icon
                     */
                    returnImage = Utilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/initialviewpage.png"); // NOI18N
                    setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "LBL_InitialViewShortDesc"));
                } else if(portletSupport.isInitialPage(PortletModeType.EDIT, fo)) {
                    /**
                     * Get the appropriate icon
                     */
                    returnImage = Utilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/initialeditpage.png"); // NOI18N
                    setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "LBL_InitialEditShortDesc"));                    
                } else if(portletSupport.isInitialPage(PortletModeType.HELP, fo)) {
                    /**
                     * Get the appropriate icon
                     */
                    returnImage = Utilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/initialhelppage.png"); // NOI18N
                    setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "LBL_InitialHelpShortDesc"));                    
                } 
            } catch (JsfPortletSupportException jpse) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, jpse);
            }
            
        } else {
            if (fo.getExt().compareToIgnoreCase("jspf") == 0) { // NOI18N
                returnImage = Utilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspfObject.png"); // NOI18N
                setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "LBL_JsfJspFragmentShortDesc"));                    
            } else {
                if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                    returnImage = Utilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject.png"); // NOI18N
                } else {
                    returnImage = Utilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject32.gif"); // NOI18N
                }
	   
                if (JsfProjectUtils.isStartPage(getDataObject().getPrimaryFile())) {
                    Image startPageBadge = Utilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/startpagebadge.gif"); // NOI18N
                    returnImage = Utilities.mergeImages(returnImage, startPageBadge, returnImage.getWidth(null),
                            returnImage.getHeight(null) - startPageBadge.getHeight(null) + 1 );
                }
            }
        }
        
        return returnImage;
    }
    
    private static JsfPortletSupport getPortletSupport(FileObject fo){
        //check for jsp extension
        if(fo.getExt().compareToIgnoreCase("jsp") == 0){ // NOI18N
            //check if this is a portlet project
            Project thisProj = FileOwnerQuery.getOwner(fo);
            return JsfProjectUtils.getPortletSupport(thisProj);
        }
        return null;
        
    }
    
    /** Adds the encoding property, and hacking adding of 'helpID's. */
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        
        // Text sheet, with the encoding property.
        Sheet.Set ps = new Sheet.Set();
        ps.setName(SHEETNAME_TEXT_PROPERTIES);
        ps.setDisplayName(NbBundle.getMessage(JsfJspDataNode.class, "PROP_textfileSetName")); // NOI18N
        ps.setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "HINT_textfileSetName")); // NOI18N
        sheet.put(ps);
        
        ps.put(new PropertySupport.ReadOnly(
                PROP_FILE_ENCODING,
                String.class,
                NbBundle.getMessage(JsfJspDataNode.class, "PROP_fileEncoding"), //NOI18N
                NbBundle.getMessage(JsfJspDataNode.class, "HINT_fileEncoding") //NOI18N
                ) {
            public Object getValue() {
                return ((JsfJspDataObject)getDataObject()).getFileEncoding(true);
            }
        });
        
        // XXX Refactored previously not commented hack, probably adding/replacing the helpID's, not clear the intention.
        Node.PropertySet[] propSets = sheet.toArray();
        for(int i = 0; i < propSets.length; i++) {
            Node.PropertySet set = propSets[i];
            set.setValue("helpID", JsfJspDataNode.class.getName() + ".PropertySheet"); // NOI18N
        }
        
        return sheet;
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
    public void setName(String name) {
        String currentName = getName();
        if (name.equals(currentName)) {
            return;
        }
        // Pass on the rename to the Java object, at some point we need to handle it from the JSP itself, but lets do this and get it working
        // for now
        JsfJavaDataObject javaDataObject = Utils.findCorrespondingJsfJavaDataObject(getDataObject().getPrimaryFile(), true);
        if (javaDataObject == null) {
            super.setName(name);
            return;
        }
        javaDataObject.getNodeDelegate().setName(name);
    }

}

