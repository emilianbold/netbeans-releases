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
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.addins.dependencyanalyzer.DependencyAnalyzerAddIn;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author  Craig Conover
 */
public class DependencyDiagramAction extends CookieAction
{
	
	/**
	 * Creates a new instance of DependencyDiagramAction
	 */
	public DependencyDiagramAction()
	{
	}
	
	public Class[] cookieClasses()
	{
		return new Class[] {IElement.class};
	}
	
	public int mode()
	{
		return MODE_ALL;
	}
	
	
	protected boolean asynchronous()
	{
		return false;
	}
	
	
	protected boolean enable(Node[] nodes)
	{
		if (!super.enable(nodes))
			return false;
		
		for(Node curNode: nodes)
		{
			IElement curElement = (IElement)curNode.getCookie(IElement.class);
			if (curElement == null || !(curElement instanceof IClassifier))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public HelpCtx getHelpCtx()
	{
		return null;
	}
	
	public String getName()
	{
		return NbBundle.getMessage(
				DependencyDiagramAction.class,
				"CTL_DependencyDiagramAction"); // NOI18N
	}
	
	public void performAction(Node[] nodes)
	{
		final ETList<IElement> elements = new ETArrayList<IElement>();
		
		for(Node curNode : nodes)
		{
			IElement curElement = (IElement)curNode.getCookie(IElement.class);
			if (curElement != null)
			{
				elements.add(curElement);
			}
		}
		
		if (elements != null && elements.size() > 0)
		{
			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					DependencyAnalyzerAddIn addin = 
							new DependencyAnalyzerAddIn();
					addin.initialize(null);
					addin.run(elements);
				}
			});
			thread.run();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Helper Methods
}
