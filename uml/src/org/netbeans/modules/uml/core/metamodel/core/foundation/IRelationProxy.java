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

public interface IRelationProxy
{
	/**
	 * Sets / Gets the from element in this proxy.
	*/
	public IElement getFrom();

	/**
	 * Sets / Gets the from element in this proxy.
	*/
	public void setFrom( IElement value );

	/**
	 * Sets / Gets the to element in this proxy.
	*/
	public IElement getTo();

	/**
	 * Sets / Gets the to element in this proxy.
	*/
	public void setTo( IElement value );

	/**
	 * Sets gets the element that performs the connection between the two elements.
	*/
	public IElement getConnection();

	/**
	 * If the connection is 0 then this is the type of connection that should be verified.
	*/
	public String getConnectionElementType();

	/**
	 * If the connection is 0 then this is the type of connection that should be verified.
	*/
	public void setConnectionElementType( String value );

	/**
	 * If used for validation this returns true if the relation has been validated.
	*/
	public boolean getRelationValidated();

	/**
	 * If used for validation this returns true if the relation has been validated.
	*/
	public void setRelationValidated( boolean value );

	/**
	 * Sets gets the element that performs the connection between the two elements.
	*/
	public void setConnection( IElement value );

	/**
	 * Determines whether or not this proxy contains the elements passed in.
	*/
	public boolean matches( IElement From, IElement To, IElement Connection );

	/**
	 * Retrieves the from element dictated by the Connection type. If Connection returns 0, so will this method.
	*/
	public IElement getRelationFrom();

	/**
	 * Retrieves the from element dictated by the Connection type. If Connection returns 0, so will this method.
	*/
	public IElement getRelationTo();

	/**
	 * Retrieves the element that physically owns the Connection type. If Connection returns 0, so will this method.
	*/
	public IElement getRelationOwner();

}
