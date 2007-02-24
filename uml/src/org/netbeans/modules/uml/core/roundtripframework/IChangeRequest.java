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

package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;

public interface IChangeRequest
{
	/**
	 * Sets / Gets the element as it was before a particular modification has taken place.
	*/
	public IElement getBefore();

	/**
	 * Sets / Gets the element as it was before a particular modification has taken place.
	*/
	public void setBefore( IElement element );

	/**
	 * Sets / Gets the element after it has been modified.
	*/
	public IElement getAfter();

	/**
	 * Sets / Gets the element after it has been modified.
	*/
	public void setAfter( IElement element );

	/**
	 * Sets / Gets the state of this change request.
	*/
	public int getState();

	/**
	 * Sets / Gets the state of this change request.
	*/
	public void setState( /* ChangeKind */ int changeKind );

	/**
	 * Sets / Gets the detailed change type of this change request.
	*/
	public int getRequestDetailType();

	/**
	 * Sets / Gets the detailed change type of this change request.
	*/
	public void setRequestDetailType( /* RequestDetailKind */ int requestDetailKind );

	/**
	 * Sets / Gets the generic type of element that the element properties represent.
	*/
	public int getElementType();

	/**
	 * Sets / Gets the generic type of element that the element properties represent.
	*/
	public void setElementType( /* RTElementKind */ int rtElementKind );

	/**
	 * Sets / Gets the language that this ChangeRequest pertains to.
	*/
	public String getLanguage();

	/**
	 * Sets / Gets the language that this ChangeRequest pertains to.
	*/
	public void setLanguage( String language );

	/**
	 * Sets / Gets the relationship proxy if the change request is a relation change.
	*/
	public IRelationProxy getRelation();

	/**
	 * Sets / Gets the relationship proxy if the change request is a relation change.
	*/
	public void setRelation( IRelationProxy relation );

	/**
	 * Sets / Gets a payload which can be used to pass any additional information needed by the request processor.
	*/
	public IEventPayload getPayload();

	/**
	 * Sets / Gets a payload which can be used to pass any additional information needed by the request processor.
	*/
	public void setPayload( IEventPayload payload );

}
