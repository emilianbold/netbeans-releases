/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
