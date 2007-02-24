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

/**
 * TransitionElementImpl implements the ITransitionElement interface.  It
 * handles the temporary state that an element is in during the creation
 * process of that element.
 */
public class TransitionElement implements ITransitionElement{

	private IElement m_Owner = null;

	/**
	 *
	 */
	public TransitionElement() {
		super();
	}

	/**
	 * Retrieves the future owner of the object implementing this interface.
	 *
	 * @param pVal[out]
	 *
	 * @return HRESULT
	 */
	public IElement getFutureOwner() {
		return m_Owner;
	}

	/**
	 * Sets the owner to be used for type resolution
	 *
	 * @param newVal[in]	The owner
	 *
	 * @return S_OK
	 */
	public void setFutureOwner(IElement value) {
		m_Owner = value;
	}

}

