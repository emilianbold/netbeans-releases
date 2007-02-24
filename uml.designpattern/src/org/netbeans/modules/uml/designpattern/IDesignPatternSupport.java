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


package org.netbeans.modules.uml.designpattern;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;

public interface IDesignPatternSupport
{
	// Populate the passed in tree node with its children
	public void populateTreeItem(Object pParent);
	// The project tree the addin deals with
	public IProjectTreeControl getProjectTree();
	// The project tree the addin deals with
	public void setProjectTree(IProjectTreeControl newVal);
	// Build the necessary details for the passed in pattern
	public void buildPatternDetails(Object pCollab, IDesignPatternDetails pDetails);
	// Apply this pattern.  Will build the details needed.
	public void apply(Object pCollab);
	// Apply the pattern in the passed in details using information from these details
	public void apply2(IDesignPatternDetails pDetails );
	// Determines if the addin knows about the passed in element id
	public ICollaboration getPatternByID(String sID);
	// The patterns the addin deals with
	public ETList < IElement > getPatterns();
	// What the addiin should do when it is told to promote
	public void promote(Object pDispatch);

}
