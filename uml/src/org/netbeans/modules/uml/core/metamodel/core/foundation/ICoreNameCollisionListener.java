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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl;

public interface ICoreNameCollisionListener
{
	/**
	 * true to enable the dispatching of name collision events to the name collision handler.
	*/
	public boolean getEnabled();

	/**
	 * true to enable the dispatching of name collision events to the name collision handler.
	*/
	public void setEnabled( boolean value );

	/**
	 * The actual handler of the name collision event
	*/
	public INameCollisionHandler getHandler();

	/**
	 * The actual handler of the name collision event
	*/
	public void setHandler( INameCollisionHandler value );

	public void onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell );

	public void onPreNameCollision(INamedElement element, 
																			  String proposedName, 
																			  ETList<INamedElement> collidingElements, 
																			  IResultCell cell);

	public void onNameCollision(INamedElement element, 
																		  ETList<INamedElement> collidingElements, 
																		  IResultCell cell);
																		  
	public void onCoreProductPreQuit( );
	
	public void onDeactivate(IEditControl pControl);


																			  

}
