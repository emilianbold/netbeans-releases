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

package org.netbeans.modules.uml.core.metamodel.diagrams;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

public interface IGraphicMapLocation
{
	/**
	 * The XMIID of the IElement at this location.
	*/
	public String getElementXMIID();

	/**
	 * The XMIID of the IElement at this location.
	*/
	public void setElementXMIID( String value );

	/**
	 * The name of this item.
	*/
	public String getName();

	/**
	 * The name of this item.
	*/
	public void setName( String value );

	/**
	 * The element type of this item.
	*/
	public String getElementType();

	/**
	 * The element type of this item.
	*/
	public void setElementType( String value );
	
	public IElement getElement();
	
	public void setElement(IElement e);

}
