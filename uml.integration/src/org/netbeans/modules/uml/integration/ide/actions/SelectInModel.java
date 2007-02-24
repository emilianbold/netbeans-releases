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
 * SelectInModel.java
 *
 * Created on March 31, 2006, 3:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.integration.ide.actions;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.ui.java.UMLJavaAssociationUtil;
import javax.swing.JMenuItem;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class SelectInModel extends CookieAction {
	
	protected void performAction(Node[] activatedNodes) {
		DataObject obj = (DataObject) activatedNodes[0].getCookie(DataObject.class);
		if (obj !=null)
		{
			Project umlProject = getAssociatedUMLProject(obj);
			ProjectUtil.selectInModel(umlProject, obj);
		}
		else
		{
			IElement element = (IElement) activatedNodes[0].getCookie(IElement.class);
			 if (element!=null)
			 {
				 ProjectUtil.findElementInProjectTree(element);
			 }
		}
	}
	
	protected int mode() {
		return CookieAction.MODE_EXACTLY_ONE;
	}
	
	public String getName() {
		return NbBundle.getMessage(SelectInModel.class, "CTL_SelectInModel");
	}
	
	
	public JMenuItem getPopupPresenter() 
	{
		JMenuItem mi = super.getPopupPresenter();
		mi.setText(NbBundle.getMessage(SelectInModel.class, "LBL_Popup_SelectInModel")); // NOI18N
		return mi;
	}

	protected Class[] cookieClasses() {
		return new Class[] {
			DataObject.class,
			IElement.class
		};
	}
	
	protected void initialize() {
		super.initialize();
		putValue("noIconInMenu", Boolean.TRUE);
	}
	
	public HelpCtx getHelpCtx() {
		return HelpCtx.DEFAULT_HELP;
	}
	
	protected boolean asynchronous() {
		return false;
	}
	
	
	protected boolean enable(Node[] activatedNodes) {
		if (!super.enable(activatedNodes))
			return false;
		
		DataObject dObj = (DataObject) activatedNodes[0].getCookie(DataObject.class);
		if (dObj != null) 
		{
			FileObject fObj = dObj.getPrimaryFile();
			Project proj = FileOwnerQuery.getOwner(fObj);

			if (proj == null ) 
				return false;
			
			return UMLJavaAssociationUtil.getAssociatedUMLProject(proj) == null ? false : true;
		}
		
		return (IElement) activatedNodes[0].getCookie(IElement.class) == null ? false : true;
	}
		
	
	private Project getAssociatedUMLProject(DataObject obj)
	{
		if (obj==null)
			return null;
		
		FileObject fObj = obj.getPrimaryFile();
		Project proj = FileOwnerQuery.getOwner(fObj);

		if (proj == null )
			return null;
		
		return UMLJavaAssociationUtil.getAssociatedUMLProject(proj);
	}

}

