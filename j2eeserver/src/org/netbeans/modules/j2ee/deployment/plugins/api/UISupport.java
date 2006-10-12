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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.WeakHashMap;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.ServerRegistryNode;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.*;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * UI support for plugins provided by the j2eeserver.
 *
 * @author sherold
 * @since  1.7
 */
public final class UISupport {
    
    private static final WeakHashMap ioWeakMap = new WeakHashMap();
    
    /**
     * Server icon constants.
     *
     * @since 1.19
     */
    public enum ServerIcon { 
        EJB_ARCHIVE, WAR_ARCHIVE, EAR_ARCHIVE,     
        EJB_FOLDER, EAR_FOLDER, WAR_FOLDER,
        EJB_OPENED_FOLDER, EAR_OPENED_FOLDER, WAR_OPENED_FOLDER
    };
    
    /** Do not allow to create instances of this class */
    private UISupport() {
    }
    
    /**
     * Returns the specified icon.
     *
     * @return The specified icon.
     *
     * @since 1.19
     */
    public static Image getIcon(ServerIcon serverIcon) {
        switch (serverIcon) {
            case EJB_ARCHIVE :
                return Utilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/ejb.png"); // NOI18N
            case WAR_ARCHIVE :
                return Utilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/war.png"); // NOI18N
            case EAR_ARCHIVE :
                return Utilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/ear.png"); // NOI18N
            default :
                return computeIcon(serverIcon);
        }
    }
    
    private static Image computeIcon(ServerIcon serverIcon) {
        // get the default folder icon
        Node folderNode = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
        Image folder;
        if (serverIcon == ServerIcon.EJB_OPENED_FOLDER || serverIcon == ServerIcon.WAR_OPENED_FOLDER 
                || serverIcon == ServerIcon.EAR_OPENED_FOLDER) {
            folder = folderNode.getOpenedIcon(BeanInfo.ICON_COLOR_16x16);
        } else {
            folder = folderNode.getIcon(BeanInfo.ICON_COLOR_16x16);
        }
        Image badge;
        if (serverIcon == ServerIcon.EJB_FOLDER || serverIcon == ServerIcon.EJB_OPENED_FOLDER) {
            badge = Utilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/ejbBadge.png"); // NOI18N
        } else if (serverIcon == ServerIcon.WAR_FOLDER || serverIcon == ServerIcon.WAR_OPENED_FOLDER) {
            badge = Utilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/warBadge.png"); // NOI18N
        } else if (serverIcon == ServerIcon.EAR_FOLDER || serverIcon == ServerIcon.EAR_OPENED_FOLDER) {
            badge = Utilities.loadImage("org/netbeans/modules/j2ee/deployment/impl/ui/resources/earBadge.png" ); // NOI18N
        } else {
            return null;
        }
        return Utilities.mergeImages(folder, badge, 7, 7);
    }
    
    /**
     * Get a named instance of InputOutput, which represents an output tab in
     * the output window. The output tab will expose server state management 
     * actions for the given server: start, debug, restart, stop and refresh. 
     * Streams for reading/writing can be accessed via getters on the returned 
     * instance. If the InputOutput already exists for the given server, the 
     * existing instance will be returned. The display name of the given server
     * will be used as a name for the tab.
     *
     * @param  url server instance id (DeploymentManager url).
     *
     * @return an <code>InputOutput</code> instance for accessing the new tab,
     *         null if there is no registered server instance with the given url.
     *         
     */
    public static InputOutput getServerIO(String url) {

        ServerInstance si = ServerRegistry.getInstance().getServerInstance(url);

        if (si == null) {
            return null;
        }
        
        // look in the cache
        InputOutput io = (InputOutput)ioWeakMap.get(si);
        if (io != null) {
            return io;
        }
        
        // look up the node that belongs to the given server instance
        Node node = ServerRegistryNode.getServerRegistryNode().getChildren().findChild(si.getUrl());
        
        // it looks like that the server instance has been removed 
        if (node == null) {
            return null;
        }
        
        Action[] actions = new Action[] {
            new StartAction.OutputAction(node),
            new DebugAction.OutputAction(node),
            new RestartAction.OutputAction(node),
            new StopAction.OutputAction(node),
            new RefreshAction.OutputAction(node)
        };
        InputOutput newIO = IOProvider.getDefault().getIO(si.getDisplayName(), actions);
        
        // put the newIO in the cache
        ioWeakMap.put(si, newIO);
        return newIO;
    }
}
