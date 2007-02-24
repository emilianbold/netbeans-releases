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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IREClass extends IREClassElement
{
	/**
	 * Retrieves the name of the package that contains the class.  If the class does not belong to a package the name will be an empty string.
	*/
	public String getPackage();

	/**
	 * Specifies whether the element may not have a direct instance. True indicates that an instance of the element must be an instance of a child of the element. False indicates that there may an instance of the element that is not an instance of a child.
	*/
	public boolean getIsAbstract();

	/**
	 * Specifies whether the element can have decedents.  True indicates that it may not have descendents; false indicates that it may have descendents (whether or not it actually has any descendents at the moment).
	*/
	public boolean getIsLeaf();

	/**
	 * Retrieves a collection of operations for the class element.
	*/
	public ETList<IREOperation> getOperations();

	/**
	 * Retrieves a collection of attributes for the class element.
	*/
	public ETList<IREAttribute> getAttributes();

	/**
	 * Retrieves the collection of super classes for the class.
	*/
	public IREGeneralization getGeneralizations();

	/**
	 * Retrieves the collection of implemented interfaces.
	*/
	public IRERealization getRealizations();

	/**
	 * Retrieves all inner classes and interfaces.
	*/
	public ETList<IREClass> getAllInnerClasses();

	/**
	 * Determines if the class is an interface class.
	*/
	public boolean getIsInterface();
}
