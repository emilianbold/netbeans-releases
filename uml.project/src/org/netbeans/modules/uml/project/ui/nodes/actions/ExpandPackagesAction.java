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

package org.netbeans.modules.uml.project.ui.nodes.actions;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.project.ui.nodes.AbstractModelElementNode;
import org.netbeans.modules.uml.project.ui.nodes.ModelRootNodeCookie;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;


/**
 *
 * @author  Administrator
 */
public class ExpandPackagesAction extends CookieAction
{
	public ExpandPackagesAction()
	{
	}
	
	
	public String getName()
	{
		return (String)NbBundle.getBundle(ExpandPackagesAction.class)
			.getString("ExpandPackagesAction_Name"); // NOI18N
	}
	
	public int mode()
	{
		return CookieAction.MODE_ANY;
	}
	
	public boolean enable(Node[] nodes)
	{
		return true;
	}
	
	public Class[] cookieClasses()
	{
		return new Class[]{null};
	}
	
	public HelpCtx getHelpCtx()
	{
		return null;
	}
	

	public void performAction(Node[] nodes)
	{
		inspectChildNodes(nodes[0].getChildren());
	}
	
	
	private void inspectChildNodes(Children children)
	{
		Node[] childNodes = children.getNodes();
		
		for (Node curChildNode : childNodes)
		{
			if (!curChildNode.isLeaf())
				inspectChildNodes(curChildNode.getChildren());
			
			
			IElement element =
					(IElement)curChildNode.getCookie(IElement.class);
			
			if (element != null)
			{
				String elType = element.getElementType();
				
				if (elType.equals(AbstractModelElementNode.ELEMENT_TYPE_PACKAGE))
				{
					expandPackageNode(curChildNode);
				}
			}
		} // for-each curChildNode
	}
	
	private void expandPackageNode(Node packageNode)
	{
		// TODO: expand tree to the passed-in packageNode
		// if the TreeView instance can be obtained, then
		// <TreeView>.expandNode(Node) should do the trick,
		// or if the ExplorerManager/ExplorerPanel can be obtained,
		// then the setExploredContext(Node) method can be used.
		// Neither seem possible right now.
	}
}
