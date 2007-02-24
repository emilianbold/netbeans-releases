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

public interface IRedefinableElementModifiedEventsSink
{
	/**
	 * Fired whenever an element is about to be modified.
	*/
	public void onPreFinalModified( IRedefinableElement element, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever an element is modified.
	*/
	public void onFinalModified( IRedefinableElement element, IResultCell cell );

	/**
	 * Fired whenever a redefined element is about to be added to a IRedefinableElement.
	*/
	public void onPreRedefinedElementAdded( IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell );

	/**
	 * Fired whenever a redefined element is added to a IRedefinableElement.
	*/
	public void onRedefinedElementAdded( IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell );

	/**
	 * Fired whenever a redefined element is about to be removed to a IRedefinableElement.
	*/
	public void onPreRedefinedElementRemoved( IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell );

	/**
	 * Fired whenever a redefined element is removed to a IRedefinableElement.
	*/
	public void onRedefinedElementRemoved( IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell );

	/**
	 * Fired whenever a redefining element is about to be added to a IRedefinableElement.
	*/
	public void onPreRedefiningElementAdded( IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell );

	/**
	 * Fired whenever a redefining element is added to a IRedefinableElement.
	*/
	public void onRedefiningElementAdded( IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell );

	/**
	 * Fired whenever a redefining element is about to be removed to a IRedefinableElement.
	*/
	public void onPreRedefiningElementRemoved( IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell );

	/**
	 * Fired whenever a redefining element is removed to a IRedefinableElement.
	*/
	public void onRedefiningElementRemoved( IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell );


}
