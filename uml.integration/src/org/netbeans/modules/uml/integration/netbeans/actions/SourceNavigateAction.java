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

package org.netbeans.modules.uml.integration.netbeans.actions;

/**
 * Company:
 * @author swadebeshp
 * @version 1.00
 */

import java.util.ResourceBundle;

import org.netbeans.api.project.Project;
import org.openide.nodes.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.integration.netbeans.SourceAction;
import org.netbeans.modules.uml.project.AssociatedSourceProvider;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.ui.nodes.AbstractModelElementNode;
import org.openide.util.actions.CookieAction;

public class SourceNavigateAction extends CookieAction
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String BUNDLE_NAME = "org.netbeans.modules.uml.integration.Bundle";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	public static boolean Round_Trip=false;

    public SourceNavigateAction(){

    }

  	public String getName(){
    	return RESOURCE_BUNDLE.getString("RoundTrip_Toggle");
  	}

  	public org.openide.util.HelpCtx getHelpCtx(){
   		return null;		
		
  	}

	protected int mode() {
		return CookieAction.MODE_EXACTLY_ONE;
	}
	
	protected Class[] cookieClasses() {
		return new Class[] {
			IElement.class
		};
	}
	
	@Override
	protected void performAction(Node[] arg0) {
		for(Node curNode: arg0)
		{
			String str = curNode.getDisplayName();
			IElement curElement=(IElement)curNode.getCookie(IElement.class);
			SourceAction source = new SourceAction();
			source.doFireNavigateEvent(curElement);
		}
	}
   

	@Override
	protected boolean enable(Node[] nodes) {
	
		if (!super.enable(nodes))
			return false;
		
		IElement element = (IElement)nodes[0].getCookie(IElement.class);
		if (element == null)
			return false;
		
		Project proj = ProjectUtil.findElementOwner(element);
		if (proj==null)
			return false;
	
		AssociatedSourceProvider asp = (AssociatedSourceProvider) proj.
				getLookup().lookup(AssociatedSourceProvider.class);
	
		if (asp==null)
			return false;
		
		if (element.getElementType().equals(AbstractModelElementNode.ELEMENT_TYPE_CLASS) ||
			element.getElementType().equals(AbstractModelElementNode.ELEMENT_TYPE_INTERFACE) ||
                        element.getElementType().equals(AbstractModelElementNode.ELEMENT_TYPE_ENUMERATION) ||
			element.getElementType().equals(AbstractModelElementNode.ELEMENT_TYPE_ATTRIBUTE) ||
			element.getElementType().equals(AbstractModelElementNode.ELEMENT_TYPE_OPERATION))	
		{
			ETList<IElement> srcList = element.getSourceFiles();
		
			if (srcList != null && srcList.size() > 0)
				return true;
		}
		
		return false;
	}

    protected boolean asynchronous() 
    {
        return false;
    }

}