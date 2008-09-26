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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.identity.server.manager.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.Collection;
import javax.swing.Action;
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
import org.openide.util.ImageUtilities;
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
    
    private static final Image ICON_BADGE = ImageUtilities.loadImage(PROFILES_NODE_BADGE);
    
    private static final String HELP_ID = "idmtools_am_config_am_sec_mech";     //NOI18N
  
    private static Icon folderIconCache;
    
    private static Icon openedFolderIconCache;
    
    /** Creates a new instance of ProfilesNode */
    public ProfilesNode(ServerInstance instance) {
        super(new ProfilesNodeChildren(instance));
        
        setName("");     //NOI18N
        setDisplayName(NbBundle.getMessage(ProfilesNode.class, "LBL_ProfilesNode"));
        //setIconBaseWithExtension(PROFILES_NODE_ICON);
        setShortDescription(NbBundle.getMessage(ProfilesNode.class, "DESC_ProfilesNode"));
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
        image = ImageUtilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;
    }
    
    public Action[] getActions(boolean context) {
        Action[] actions = new Action[] {
        };
        
        return actions;
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
                    ((new SecurityMechanismHelper(instance.getID()))).getAllWSPSecurityMechanisms();
           
            setKeys(secMechs);
        }
    }
}
