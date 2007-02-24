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
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
public interface IMetaAttributeModifiedEventPayload extends IEventPayload
{
	/**
	 * Retrieves the actual element being modified.
	*/
	public IVersionableElement getElement();

	/**
	 * Retrieves the actual element being modified.
	*/
	public void setElement( IVersionableElement value );

	/**
	 * Retrieves the name of the property on the element being modified.
	*/
	public String getPropertyName();

	/**
	 * Retrieves the name of the property on the element being modified.
	*/
	public void setPropertyName( String value );

	/**
	 * Retrieves the original value of the property.
	*/
	public String getOriginalValue();

	/**
	 * Retrieves the original value of the property.
	*/
	public void setOriginalValue( String value );

	/**
	 * Retrieves the new value of the property.
	*/
	public String getNewValue();

	/**
	 * Retrieves the new value of the property.
	*/
	public void setNewValue( String value );

}
