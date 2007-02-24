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


package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ISubsystem extends IComponent
{
	/**
	 * method AddSpecificationElement
	*/
	public void addSpecificationElement( IPackageableElement element );

	/**
	 * method RemoveSpecificationElement
	*/
	public void removeSpecificationElement( IPackageableElement element );

	/**
	 * property SpecificationElements
	*/
	public ETList<IPackageableElement> getSpecificationElements();

	/**
	 * method AddRealizationElement
	*/
	public void addRealizationElement( IPackageableElement element );

	/**
	 * method RemoveRealizationElement
	*/
	public void removeRealizationElement( IPackageableElement element );

	/**
	 * property RealizationElements
	*/
	public ETList<IPackageableElement> getRealizationElements();

}
