/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.uml.project.ui.nodes;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;

import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;

/**
 *   The node type created for use in DesignCenterComponent 
 *   for Netbeans cookies and actions machinery to work
 * 
 */
public class UMLRequirementNode extends AbstractNode 
{

    public UMLRequirementNode(IProjectTreeItem item) {
	super(new Children.Array());
	getCookieSet().add(item);
    }
	
    public void setRequirement(IRequirement requirement)
    {
        getCookieSet().add(requirement);
    }

}
