/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.visualweb.project.jsfloader;



import java.lang.reflect.InvocationTargetException;
import java.util.Dictionary;
import java.util.Hashtable;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.visualweb.project.jsf.api.JsfPortletSupport;
import org.netbeans.modules.visualweb.project.jsf.api.JsfPortletSupportException;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;

import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.api.portlet.dd.PortletModeType;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Node that represents JSF JSP data object.
 *
 * @author  Peter Zavadsky
 */
public class JsfJspDataNode extends org.openide.loaders.DataNode implements PropertyChangeListener {

    private static final String SHEETNAME_TEXT_PROPERTIES = "textProperties"; // NOI18N
    private static final String PROP_FILE_ENCODING = "encoding"; // NOI18N
    private static final String HELP_ID = "org.netbeans.modules.web.core.syntax.JSPKit";
    private WeakReference<ChangeListener> projectPropListener;
    
    public JsfJspDataNode(DataObject dobj, Children ch) {
        super(dobj, ch);
        setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "LBL_JsfJspNodeShortDesc"));
        FileObject thisFileObject = dobj.getPrimaryFile();
        Project project = FileOwnerQuery.getOwner(thisFileObject);
        
        // Use custom weakly referenced listener since addProjectPropertyListener()
        // is a non-standard interface.
        ChangeListener listener = new ChangeListener(this);
        JsfProjectUtils.addProjectPropertyListener(project, listener);
        
        dobj.addPropertyChangeListener(WeakListeners.propertyChange(this, dobj));
        
        projectPropListener = new WeakReference<ChangeListener>(listener);
    }

    @Override
    public void destroy() throws IOException {
        FileObject thisFileObject = getDataObject().getPrimaryFile();
        Project project = FileOwnerQuery.getOwner(thisFileObject);
        
        ChangeListener listener = projectPropListener.get();
        if (listener != null) {
            JsfProjectUtils.removeProjectPropertyListener(project, listener);
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
    
    @Override
    public Image getIcon(int type) {
        Image returnImage = null;
        
        FileObject fo = getDataObject().getPrimaryFile();
        if(null == fo) return null;
        /**
         * If this node is in a Portlet Project, we need to set the icons
         * appropriately if the JSP is an INITIAL VIEW, EDIT, or HELP page.
         * -David Botterill 9/20/2005
         */
        JsfPortletSupport portletSupport = JsfJspDataNode.getPortletSupport(fo);
        if(null != portletSupport) {
            /**
             * Determine if the current file is an initial page for any of the modes.
             */
            try {
                if(portletSupport.isInitialPage(PortletModeType.VIEW, fo)) {
                    returnImage = ImageUtilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/initialviewpage.png"); // NOI18N
                    setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "LBL_InitialViewShortDesc"));
                } else if(portletSupport.isInitialPage(PortletModeType.EDIT, fo)) {
                    returnImage = ImageUtilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/initialeditpage.png"); // NOI18N
                    setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "LBL_InitialEditShortDesc"));                    
                } else if(portletSupport.isInitialPage(PortletModeType.HELP, fo)) {
                    returnImage = ImageUtilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/initialhelppage.png"); // NOI18N
                    setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "LBL_InitialHelpShortDesc"));                    
                } else {
                    returnImage = ImageUtilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject.png"); // NOI18N
                }
            } catch (JsfPortletSupportException jpse) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, jpse);
            }
            
        } else {
            if (fo.getExt().compareToIgnoreCase("jspf") == 0) { // NOI18N
                returnImage = ImageUtilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspfObject.png"); // NOI18N
                setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "LBL_JsfJspFragmentShortDesc"));                    
            } else {
                if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                    returnImage = ImageUtilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject.png"); // NOI18N
                } else {
                    returnImage = ImageUtilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/jsfJspObject32.gif"); // NOI18N
                }
	   
                if (JsfProjectUtils.isStartPage(getDataObject().getPrimaryFile())) {
                    Image startPageBadge = ImageUtilities.loadImage("org/netbeans/modules/visualweb/project/jsfloader/resources/startpagebadge.png"); // NOI18N
                    returnImage = ImageUtilities.mergeImages(returnImage, startPageBadge, returnImage.getWidth(null),
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
    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        
        // Text sheet, with the encoding property.
        Sheet.Set ps = new Sheet.Set();
        ps.setName(SHEETNAME_TEXT_PROPERTIES);
        ps.setDisplayName(NbBundle.getMessage(JsfJspDataNode.class, "PROP_textfileSetName")); // NOI18N
        ps.setShortDescription(NbBundle.getMessage(JsfJspDataNode.class, "HINT_textfileSetName")); // NOI18N
        sheet.put(ps);
        
        ps.put(new PropertySupport.ReadOnly<String> (
                PROP_FILE_ENCODING,
                String.class,
                NbBundle.getMessage(JsfJspDataNode.class, "PROP_fileEncoding"), //NOI18N
                NbBundle.getMessage(JsfJspDataNode.class, "HINT_fileEncoding") //NOI18N
                ) {
            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return ((JsfJspDataObject)getDataObject()).getFileEncoding();
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
    
    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
    @Override
    public void setName(String name) {
        String currentName = getName();
        if (name.equals(currentName)) {
            return;
        }

        InstanceContent ic = new InstanceContent();
        ic.add(this);
        Dictionary<String,String> d = new Hashtable<String,String>();
        d.put("name", name);
        ic.add(d);
        Lookup l = new AbstractLookup(ic);
        DataObject dob = getCookie(DataObject.class);
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(l);
        if (a.isEnabled()) {
            a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
        } else {
            super.setName(name);
        }
    }

    private static final class ChangeListener implements PropertyChangeListener {
        private WeakReference<JsfJspDataNode> ref;
        
        public ChangeListener(JsfJspDataNode node) {
            ref = new WeakReference<JsfJspDataNode>(node);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            JsfJspDataNode node = ref.get();
            if (node != null) {
                node.propertyChange(evt);
            }
        }
        
    }
        
}

