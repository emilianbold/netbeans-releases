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
package org.netbeans.modules.identity.server.manager.ui;

import org.netbeans.modules.identity.profile.api.configurator.ConfiguratorException;
import org.netbeans.modules.identity.server.manager.api.ServerInstance;
import org.netbeans.modules.identity.server.manager.api.ServerManager;
import org.netbeans.modules.j2ee.sun.bridge.apis.NodeExtension;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtController;
import org.openide.nodes.Node;


/**
 *
 * @author Peter Liu
 */
public class NodeExtensionImpl extends NodeExtension {
    
    public Node getAppserverExtensionNode(AppserverMgmtController controller) {    
        try {
            ServerInstance instance = ServerManager.getDefault().getServerInstance("[C:\\Sun\\SDK]deployer:Sun:AppServer::localhost:4848");
            return new ServerInstanceNode(instance);
        } catch (ConfiguratorException ex) {
            return null;
        }
    }

}
