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

package org.netbeans.modules.sun.manager.jbi.nodes;

import javax.management.MBeanServerConnection;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.j2ee.sun.api.SimpleNodeExtensionProvider;
import org.openide.nodes.Node;

/**
 * Implementation of <code>SimpleNodeExtensionProvider</code> to provide the top level
 * JBI meta-container node.
 *
 * @author jqian
 */
public class JBINodeExtensionProvider implements SimpleNodeExtensionProvider {

    /**
     * Gets the JBI node under the application server node.
     * 
     * @param connection 
     * 
     * @return  the top level JBI meta-container node if the "JBIFramework"  
     *          Lifecycle Module is installed in the application server; 
     *          or <code>null</code> otherwise.
     */
    public Node getExtensionNode(MBeanServerConnection connection) {        
        AppserverJBIMgmtController jbiController = 
                new AppserverJBIMgmtController(connection);
                
        if (jbiController.isJBIFrameworkEnabled()) {
            return new JBINode(jbiController);
        } else {
            return null;
        }
    }
    
}
