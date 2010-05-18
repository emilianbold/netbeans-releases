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
