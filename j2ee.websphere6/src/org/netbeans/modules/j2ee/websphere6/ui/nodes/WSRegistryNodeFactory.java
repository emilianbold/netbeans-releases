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
package org.netbeans.modules.j2ee.websphere6.ui.nodes;

import org.openide.util.*;
import org.openide.nodes.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

/**
 * Factory that creates nodes that will appear in the server registry.
 *
 * @author Kirill Sorokin
 */
public class WSRegistryNodeFactory implements RegistryNodeFactory {
    /**
     * Creates a node that represents a concrete target in a particular server
     * instance. By default it is filtered and does not get visible
     *
     * @param lookup a lookup with useful objects such as the deployment 
     *      manager for the instance
     * 
     * @return the node for the target
     */
    public Node getTargetNode(Lookup lookup) {
        return new WSTargetNode(lookup);
    }
    
    /**
     * Creates a node that represents a particular server instance.
     * 
     * @param lookup a lookup with useful objects such as the deployment 
     *      manager for the instance
     * 
     * @return the node for the instance
     */
    public Node getManagerNode(Lookup lookup) {
        return new WSManagerNode(new Children.Map(), lookup);
    }
}
