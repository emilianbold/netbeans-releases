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

package org.netbeans.modules.identity.server.manager.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanism;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanismHelper;
import org.netbeans.modules.identity.server.manager.api.ServerInstance;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * This class represents the Profiles node in the Runtime tab.
 *
 * Created on July 10, 2006, 6:10 PM
 *
 * @author ptliu
 */
public class ProfilesNode extends AbstractNode {
    
    private static final String PROFILES_NODE_BADGE = "org/netbeans/modules/identity/server/manager/ui/resources/ProfilesNodeBadge.png";//NOI18N
    
    private static final Image ICON_BADGE = Utilities.loadImage(PROFILES_NODE_BADGE);    //NOI18N
    
    private static final String HELP_ID = "idmtools_am_config_am_sec_mech";     //NOI18N
  
    private static Icon folderIconCache;
    
    private static Icon openedFolderIconCache;
    
    /** Creates a new instance of ProfilesNode */
    public ProfilesNode(ServerInstance instance) {
        super(new ProfilesNodeChildren(instance));
        
        setName("");     //NOI18N
        setDisplayName(NbBundle.getMessage(ProfilesNode.class, "LBL_ProfilesNode"));
        //setIconBaseWithExtension(PROFILES_NODE_ICON);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
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
    static synchronized Icon getFolderIcon(boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
            openedFolderIconCache = new ImageIcon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
            folderIconCache = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        if (opened) {
            return openedFolderIconCache;
        } else {
            return folderIconCache;
        }
    }
    
    private Image computeIcon( boolean opened, int type ) {
        Icon icon = getFolderIcon(opened);
        Image image = ((ImageIcon)icon).getImage();
        image = Utilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;
    }
    
    private static class ProfilesNodeChildren extends Children.Keys {
        private ServerInstance instance;
        
        public ProfilesNodeChildren(ServerInstance instance) {
            this.instance = instance;
        }
        
        protected void addNotify() {
            updateKeys();
        }
        
        protected Node[] createNodes(Object key) {
            return new Node[] {new ProfileNode((SecurityMechanism) key, instance)};
        }
        
        private void updateKeys() {
            Collection<SecurityMechanism> secMechs =
                    SecurityMechanismHelper.getDefault().getAllWSPSecurityMechanisms();
           
            setKeys(secMechs);
        }
    }
}
