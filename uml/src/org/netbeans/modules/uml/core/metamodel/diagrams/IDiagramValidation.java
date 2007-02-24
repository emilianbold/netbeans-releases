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

public interface IDiagramValidation
{
	/**
	 * Reset the validation and response flags.  Done on create automatically.
	*/
	public void reset();

	/**
	 * Should we validate nodes?
	*/
	public boolean getValidateNodes();

	/**
	 * Should we validate nodes?
	*/
	public void setValidateNodes( boolean value );

	/**
	 * Should we validate links?
	*/
	public boolean getValidateLinks();

	/**
	 * Should we validate links?
	*/
	public void setValidateLinks( boolean value );

	/**
	 * Add something that needs validation
	*/
	public void addValidationKind( /* DiagramValidateKind */ int nKind );

	/**
	 * Remove an item from validation
	*/
	public void removeValidationKind( /* DiagramValidateKind */ int nKind );

	/**
	 * Should we validate this item?
	*/
	public boolean getValidationKind( /* DiagramValidateKind */ int nKind );

	/**
	 * Create a IGraphObjectValidation based on the kinds in this object
	*/
	public IGraphObjectValidation createGraphObjectValidation();

	/**
	 * Add an action we should take after validation has finished.
	*/
	public void addValidationResponse( /* DiagramValidateResponse */ int nKind );

	/**
	 * Remove a response
	*/
	public void removeValidationResponse( /* DiagramValidateResponse */ int nKind );

	/**
	 * Get a validation response.  What should the diagram do after the validation has completed?
	*/
	public boolean getValidationResponse( /* DiagramValidateResponse */ int nKind );

}
