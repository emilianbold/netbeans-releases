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

import org.netbeans.modules.uml.core.eventframework.EventPayload;

/**
 * @author sumitabhk
 *
 */
public class MetaAttributeModifiedEventPayload extends EventPayload
			implements IMetaAttributeModifiedEventPayload{

	/**
	 *
	 */
	public MetaAttributeModifiedEventPayload() {
		super();
	}

	/**
	 * Retrieves the actual element being modified.
	 *
	 * @param [out]
	 *
	 * @return HRESULT
	 */
	public IVersionableElement getElement() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventPayload#setElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement)
	 */
	public void setElement(IVersionableElement value) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Retrieves the name of the property on the element being modified.
	 * 
	 * @param [out] 
	 *
	 * @return S_OK
	 */
	public String getPropertyName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventPayload#setPropertyName(java.lang.String)
	 */
	public void setPropertyName(String value) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Retrieves the original value of the property.
	 * 
	 * @param [out] 
	 *
	 * @return S_OK
	 */
	public String getOriginalValue() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventPayload#setOriginalValue(java.lang.String)
	 */
	public void setOriginalValue(String value) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Retrieves the new value of the property.
	 * 
	 * @param [out] 
	 *
	 * @return S_OK
	 */
	public String getNewValue() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Retrieves the new value of the property.
	 * 
	 * @param [in] 
	 *
	 * @return S_OK
	 */
	public void setNewValue(String value) {
		// TODO Auto-generated method stub
		
	}

}



