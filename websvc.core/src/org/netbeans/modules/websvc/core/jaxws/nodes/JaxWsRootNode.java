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

package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

public class JaxWsRootNode extends AbstractNode implements PropertyChangeListener{
    private PropertyEvaluator evaluator;
    private Project project;
    private boolean jsr109Supported;
    private static final Image WEB_SERVICES_BADGE = Utilities.loadImage( "org/netbeans/modules/websvc/core/webservices/ui/resources/webservicegroup.png", true ); // NOI18N
    static Icon folderIconCache;
    static Icon openedFolderIconCache;
    
    public JaxWsRootNode(Project project, JaxWsModel jaxWsModel, FileObject[] srcRoots) {
        super(new JaxWsRootChildren(jaxWsModel,srcRoots), Lookups.fixed(project));
        setDisplayName(NbBundle.getBundle(JaxWsRootNode.class).getString("LBL_WebServices"));
        this.project=project;
        if(!Util.isJavaEE5orHigher(project)){
            listenToServerChanges();
            jsr109Supported = isJsr109Supported();
        }
    }
    
    public Image getIcon( int type ) {
        return computeIcon( false, type );
    }
    
    public Image getOpenedIcon( int type ) {
        return computeIcon( true, type );
    }
    
    /**
     * Returns Icon of folder on active platform
     * @param opened should the icon represent opened folder
     * @return the folder icon
     */
    static synchronized Icon getFolderIcon (boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
            openedFolderIconCache = new ImageIcon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
            folderIconCache = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        if (opened) {
            return openedFolderIconCache;
        }
        else {
            return folderIconCache;
        }
    }

    private Image computeIcon( boolean opened, int type ) {        
        Icon icon = getFolderIcon(opened);
        Image image = ((ImageIcon)icon).getImage();
        image = Utilities.mergeImages(image, WEB_SERVICES_BADGE, 7, 7 );
        return image;        
    }

    public Action[] getActions(boolean context) {
        return new Action[]{
            CommonProjectActions.newFileAction(),
            null,
            SystemAction.get(FindAction.class),
            null,
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    private void listenToServerChanges(){
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss != null) {
            evaluator = wss.getAntProjectHelper().getStandardPropertyEvaluator();
            PropertyChangeListener pcl = WeakListeners.propertyChange(this, evaluator);
            evaluator.addPropertyChangeListener(pcl);
        }
    }
    
    private boolean isJsr109Supported() {
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss != null) {
            return isJsr109Supported(wss);
        }
        return false;
    }
    
    private boolean isJsr109Supported(JAXWSSupport wss) {
        Map properties = wss.getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
        String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
        if (serverInstance != null) {
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
            if (j2eePlatform != null) {
                return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
            }
        }
        return false;
    }
    
    
    public void propertyChange(PropertyChangeEvent evt){
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss!=null && wss.getServices().size()>0) {
            if ("j2ee.server.instance".equals(evt.getPropertyName())){
                boolean newJsr109Supported = isJsr109Supported(wss);
                if(jsr109Supported != newJsr109Supported) {
                    JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
                    boolean isJsr109Project = jaxWsModel.getJsr109().booleanValue();
                    if(isJsr109Project != newJsr109Supported){
                        String msg = NbBundle.getMessage(JaxWsRootNode.class, "MSG_IncompatibleWSServer"); //NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE));
                    }
                    jsr109Supported = newJsr109Supported;
                }
            }
        }
    }
    
    // the following templates should be available on this node
    /*
    private static class WSRecommendedTemplates implements RecommendedTemplates, PrivilegedTemplates  {
        private static final String[] TYPES = new String[] { 
        "web-services",         // NOI18N
        };
        private static final String[] WS_TEMPLATES = new String[] {           
            "Templates/WebServices/WebService.java",    // NOI18N
            "Templates/WebServices/WebServiceFromWSDL.java",    // NOI18N
        };
        
        public String[] getRecommendedTypes() {
            return TYPES;
        }
        
        public String[] getPrivilegedTemplates() {
            return WS_TEMPLATES;
        }
    }
    */
}
