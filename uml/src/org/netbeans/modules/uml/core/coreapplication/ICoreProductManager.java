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


package org.netbeans.modules.uml.core.coreapplication;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ICoreProductManager
{
	/**
	 * Get/Set the product for the process that this ICoreProductManager is located.
	*/
	public ICoreProduct getCoreProduct();

	/**
	 * Get/Set the product for the process that this ICoreProductManager is located.
	*/
	public void setCoreProduct( ICoreProduct value );

	/**
	 * Get/Set the product.  The nPID is the process ID where the application is located.
	*/
	public ICoreProduct getCoreProduct( int nPID );

	/**
	 * Get/Set the product.  The nPID is the process ID where the application is located.
	*/
	public void setCoreProduct( int nPID, ICoreProduct value );

	/**
	 * The collection of CoreProducts this manager manages.
	*/
	public ETList<IProductDescriptor> getProducts();

	/**
	 * Returns the product manager that is on the ROT.
	*/
	public ICoreProductManager getProductManagerOnROT();

	/**
	 * Removes this item from the ROT.
	*/
	public void removeFromROT();

	/**
	 * Sets the product for which you want to attach (ie Describe).
	*/
	public void setProductAlias( String value );

	/**
	 * Sets the product for which you want to attach (ie Describe).
	*/
	public String getProductAlias();

}
