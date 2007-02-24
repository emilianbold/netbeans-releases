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

/*
 * NewDiagramType.java
 *
 * Created on March 4, 2005, 4:39 PM
 */

package org.netbeans.modules.uml.project.ui.nodes.actions;

import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.project.UMLProjectModule;
import org.netbeans.modules.uml.project.ui.nodes.UMLDiagramNode;
import org.netbeans.modules.uml.project.ui.nodes.UMLPhysicalViewProvider;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class NewPackageType extends NewType
{
	private Node node;
	private IElement element = null;
	
	/** Creates a new instance of NewDiagramType */
	public NewPackageType(Node node)
	{
		this.node = node;
	}
	
	public NewPackageType(IElement element)
	{
		this.element = element;
	}
	
	/**
	 *
	 *
	 */
	public String getName()
	{
		return (String)NbBundle.getBundle(this.getClass())
				.getString("NewType_Package_Name"); // NOI18N
	}
	
	
	/**
	 *
	 *
	 */
	public HelpCtx getHelpCtx()
	{
		return HelpCtx.DEFAULT_HELP;
	}
	
	
	public void create() throws java.io.IOException
	{
		// code based on legacy code from in JProjectTree.NewPackageAction
		if (node==null)
		{
			Project project = ProjectUtil.findReferencingProject(element);
			UMLPhysicalViewProvider provider = (UMLPhysicalViewProvider) 
					project.getLookup().lookup(UMLPhysicalViewProvider.class);
			if (provider!= null)
				node = provider.getModelRootNode();	
		}      
		INamespace space = null;
		if (element == null)
			element = (IElement)node.getCookie(IElement.class);
		
		if (node instanceof UMLDiagramNode)
		{
			// cvc - CR 6287660
			while (element == null && node != null)
			{
				node = node.getParentNode();
				if (node != null)
					element = (IElement)node.getCookie(IElement.class);
			}
		}
		
		if (element != null)
		{
			if (element instanceof INamespace)
			{
				space = (INamespace)element;
			}
			else if (element instanceof INamedElement)
			{
				//get the parent namespace
				space = ((INamedElement)element).getNamespace();
			}
		}
		
		IProxyUserInterface proxyUI = ProductHelper.getProxyUserInterface();
		if (proxyUI != null && space != null)
		{
			IElement pkg = proxyUI.newPackageDialog(space);
//			if (pkg != null && pkg instanceof INamedElement)
//			{
//				UMLProjectModule.getProjectTreeEngine().
//						addNewlyCreatedElement((INamedElement)pkg);
//				ProjectUtil.findElementInProjectTree(pkg);  
//			}

		}
	}
}
