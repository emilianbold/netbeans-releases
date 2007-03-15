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

package org.netbeans.modules.apisupport.project.spi;

import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.ui.ImportantFilesNodeFactory;
import org.openide.nodes.Node;

/**
 * Utility class for exposing creation of layer, services or other 
 * special purpose nodes.
 * @author mkleint
 */
public final class NodeFactoryUtils {
    
    /** Creates a new instance of NodeFactoryUtils */
    private NodeFactoryUtils() { }
    
    /**
     * creates a Node for displaying the layer content.
     * @param project 
     * @return node or null.
     */
    public static Node createLayersNode(Project project) {
        NbModuleProvider prv = project.getLookup().lookup(NbModuleProvider.class);
        if (prv != null) {
            return ImportantFilesNodeFactory.createLayerNode(project);
        }
        return null;
    }
    
}
