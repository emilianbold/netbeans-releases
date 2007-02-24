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

package org.netbeans.modules.uml.integration.ide.actions;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.ui.nodes.UMLLogicalViewCookie;
import org.netbeans.modules.uml.project.ui.nodes.UMLPhysicalViewProvider;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewDiagramType;
import java.io.IOException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.ErrorManager;

public final class NewDiagramAction extends CookieAction {
	
	protected void performAction(Node[] activatedNodes) {
		UMLLogicalViewCookie c = (UMLLogicalViewCookie) activatedNodes[0].getCookie(UMLLogicalViewCookie.class);
		IProjectTreeItem item = (IProjectTreeItem) activatedNodes[0].getCookie(IProjectTreeItem.class);
		
		if (c!=null) // UML project node
		{
			Node node = c.getModelRootNode();	
			NewDiagramType newType = new NewDiagramType(node);
			try {
				newType.create();
				return;
			}catch (IOException e)
			{
				ErrorManager.getDefault().notify(e);
			}
		}
		
		if (item != null)
		{
			IElement element = item.getModelElement();
			if (element==null && item.isDiagram()) // could be diagram node
			{
				element = item.getDiagram().getProject();
			}
			NewDiagramType newType = new NewDiagramType(element);
			try {
				newType.create();
			}catch (IOException e)
			{
				ErrorManager.getDefault().notify(e);
			}
		}
		else  // UML model root node or diagrams root node
		{
			Object obj = activatedNodes[0].getLookup().lookup(UMLProject.class);
			if (obj!=null)
			{
				UMLPhysicalViewProvider provider = (UMLPhysicalViewProvider)((UMLProject)obj).getLookup().lookup(UMLPhysicalViewProvider.class);
				if (provider== null)
					return;
				
				Node node = provider.getModelRootNode();	
				NewDiagramType newType = new NewDiagramType(node);
				try {
					newType.create();
				}catch (IOException e)
				{
					ErrorManager.getDefault().notify(e);
				}	
			}
		}	
	}
	
	protected int mode() {
		return CookieAction.MODE_EXACTLY_ONE;
	}
	
	public String getName() {
		return NbBundle.getMessage(NewDiagramAction.class, "CTL_NewDiagramAction");
	}
	
	protected Class[] cookieClasses() {
		return new Class[] {
			UMLProject.class,
			IProjectTreeItem.class
		};
	}
	
	protected String iconResource() {
		return "org/netbeans/modules/uml/resources/toolbar_images/NewDiagram.png";
	}
	
	public HelpCtx getHelpCtx() {
		return HelpCtx.DEFAULT_HELP;
	}
	
	protected boolean asynchronous() {
		return false;
	}
	
}

