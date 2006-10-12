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

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.j2ee.earproject.ui.actions.AddModuleAction;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Represents the <em>J2EE Modules</em> node in the EAR project's logical view.
 *
 * @author vkraemer
 * @author Ludovic Champenois
 */
public final class LogicalViewNode extends AbstractNode {
    
    static final String J2EE_MODULES_NAME = "j2ee.modules"; // NOI18N    
    private static Image J2EE_MODULES_BADGE = Utilities.loadImage( "org/netbeans/modules/j2ee/earproject/ui/resources/application_16.gif", true ); // NOI18N
    private static Icon folderIconCache;
    private static Icon openedFolderIconCache;	
    private final AntProjectHelper model;
    
    public LogicalViewNode(AntProjectHelper model) {
        super(new LogicalViewChildren(model), Lookups.fixed( new Object[] { model }));
        this.model = model;
        // Set FeatureDescriptor stuff:
        setName(J2EE_MODULES_NAME);
        setDisplayName(NbBundle.getMessage(LogicalViewNode.class, "LBL_LogicalViewNode"));
        setShortDescription(NbBundle.getMessage(LogicalViewNode.class, "HINT_LogicalViewNode"));
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
        image = Utilities.mergeImages(image, J2EE_MODULES_BADGE, 7, 7 );
        return image;        
    }

    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(AddModuleAction.class),
        };
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // When you have help, change to:
        // return new HelpCtx(LogicalViewNode.class);
    }
    
    // Handle copying and cutting specially:
    public boolean canCopy() {
        return false;
    }
    
}
