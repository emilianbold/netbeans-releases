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
package org.netbeans.modules.websvc.core;

import org.netbeans.api.project.Project;
import org.openide.nodes.Node;

/**
 *
 * @author rico
 
 */
public interface ServiceNodesProvider {
    
    /**
     * Get the web service nodes that are in the project. 
     * @param project Project that contains the web service nodes
     * @return Array of web service nodes in the project. These nodes represent the web service node
     * only without any operations children. Returns null if there are no web service nodes
     */ 
    public Node[] getServiceNodes(Project project);
    
}
