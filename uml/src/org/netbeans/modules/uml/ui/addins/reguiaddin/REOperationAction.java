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
 * DiagramCreatorAction.java
 *
 * Created on January 7, 2005, 10:38 AM
 */

package org.netbeans.modules.uml.ui.addins.reguiaddin;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
//import org.netbeans.modules.uml.regui.*;
import org.netbeans.modules.uml.ui.addins.reguiaddin.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author  Trey Spiva
 */
public class REOperationAction extends CookieAction
{
	
	/**
	 * Creates a new instance of GenerateCodeAction
	 */
	public REOperationAction()
	{
	}
	
    protected Class[] cookieClasses()
    {
       return new Class[] {IElement.class};
    }
	
    protected int mode()
    {
       return MODE_EXACTLY_ONE;
    }
	
	
	protected boolean enable(Node[] nodes)
	{
		boolean retVal = false;
		if (nodes.length == 1)
		{
			IElement element = (IElement)nodes[0].getCookie(IElement.class);
			if (element != null)
			{
				 if (element.getElementType().equals("Operation")) // NOI18N
					retVal = true;
			}
		}
		
		return retVal;
	}
	
	public HelpCtx getHelpCtx()
	{
		return null;
	}
	
	public String getName()
	{
		return NbBundle.getMessage(
				REOperationAction.class, "IDS_RE_OPERATION"); // NOI18N
	}
	
	protected void performAction(Node[] nodes)
	{
		final ETArrayList<IElement> elements = new ETArrayList<IElement>();

		for (Node curNode : nodes)
		{
			IElement curElement = (IElement)curNode.getCookie(IElement.class);
		
			if (curElement != null)
				elements.add(curElement);
		}
		
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				REGUIAddin regui = new REGUIAddin();
				regui.run(getName(), elements);
			}
		});

		thread.run();
	}
	
    protected boolean asynchronous()
    {
        return false;
    }
}
