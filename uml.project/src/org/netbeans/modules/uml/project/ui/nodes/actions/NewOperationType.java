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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class NewOperationType extends NewType
{
	private Node node;
	
	public NewOperationType(Node node)
	{
		this.node = node;
	}
	
	
	/**
	 *
	 *
	 */
	public String getName()
	{
		return (String)NbBundle.getBundle(this.getClass())
		.getString("NewType_Operation_Name"); // NOI18N
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
		// code based on legacy code from in JProjectTree.NewOperationAction
		
		// this action can be invoked via the Class node or the Operations
		//  container node. If it's the Operations container node, we must get  
		//  it's parent (the Class node) in order to pass the next "if" test
		if (node.getDisplayName().equals("Operations"))
			node = node.getParentNode();
		
		INamespace space = null;
		IElement element = (IElement)node.getCookie(IElement.class);
		
		try
		{
			if (element != null && element instanceof IClassifier)
			{
				IClassifier pClass = (IClassifier)element;
				IOperation pOp = pClass.createOperation3();
				pClass.addOperation(pOp);
			}
		}
		
		catch (Exception ex)
		{
		}
	}
}
