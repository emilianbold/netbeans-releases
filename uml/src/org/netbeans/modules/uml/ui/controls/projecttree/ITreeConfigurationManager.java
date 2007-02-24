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


package org.netbeans.modules.uml.ui.controls.projecttree;

import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSorter;

public interface ITreeConfigurationManager
{
	/**
	 * Tells the configuration manager that it should control the argument tree
	*/
	public long attach( IProjectTreeControl pParentControl );

	/**
	 * Tells the diagram engine that it should release all references and prepare to be deleted
	*/
	public long detach();

	/**
	 * Tells the configuration manager to refresh the tree.  Set bForceDeep to TRUE to force a complete delete and restore - otherwise stuff like requirements and vba macros will be preserved.
	*/
	public long refresh( boolean bForceDeep );

	/**
	 * This is the guy that should sort the project tree context menus
	*/
	public IProductContextMenuSorter getContextMenuSorter();

}
