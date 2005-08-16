/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.util.WeakHashMap;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.ServerRegistryNode;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.*;
import org.openide.nodes.Node;
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
    
    /** Do not allow to create instances of this class */
    private UISupport() {
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
