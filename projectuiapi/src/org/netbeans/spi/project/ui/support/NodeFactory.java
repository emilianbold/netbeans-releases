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

package org.netbeans.spi.project.ui.support;

import org.netbeans.api.project.Project;

/**
 * Factory interface for distributed creation of project node's children. Implementation
 * instances are assumed to be registered in layers at a location specific for the particular
 * project type. Project types wanting to make use of NodeFactory can use the 
 * {@link org.netbeans.spi.project.ui.support.NodeFactorySupport#createCompositeChildren}
 * method to create the Project nodes's children.
 * 
<pre>
public class FooBarLogicalViewProvider implements LogicalViewProvider {
    public Node createLogicalView() {
        return new FooBarRootNode(NodeFactorySupport.createCompositeChildren("Projects/org-foo-bar-project/Nodes");
    }
  
}
</pre>
 * @author RichUnger
 * @since org.netbeans.modules.projectuiapi/1 1.18
 */
public interface NodeFactory {
    
    /** 
     * Create a list of children nodes for the given project. If the list is to be static,
     * use the {@link org.netbeans.spi.project.ui.support.NodeFactorySupport#fixedNodeList}
     * @return never return null, if the project is not relevant to the NodeFactory,
     * use {@link org.netbeans.spi.project.ui.support.NodeFactorySupport#fixedNodeList} empty value.
     */ 
    NodeList<?> createNodes(Project p);
    
//    Node findPath(Project p, Node root, Object target);
    
}
