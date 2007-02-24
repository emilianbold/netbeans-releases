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

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * PresentationReferenceImpl implements the IPresentationReference meta type.
 *
 * This is a more specific version of the Reference relationship, in that it is
 * designed to be used when referring to presentation elements on the target
 * side of the relationship.
 */
public class PresentationReference extends Reference implements IPresentationReference{

	/**
	 *
	 */
	public PresentationReference() {
		super();
	}

	/**
	 *
	 * Retrieves the presentation element that this reference refers to.
	 *
	 * @param pVal[out] The element
	 *
	 * @return HRESULT
	 *
	 */
	public IPresentationElement getPresentationElement() {
		IPresentationElement retEle = null;
		IElement elem = getReferredElement();
		if (elem instanceof IPresentationElement)
		{
			retEle = (IPresentationElement)elem;
		}
		return retEle;
	}

	/**
	 *
	 * Sets the passed in presentation element on the referred side of this reference
	 *
	 * @param pVal[in] The element
	 *
	 * @return HRESULT
	 * @note A convenience pass through to put_ReferredElement()
	 *
	 */
	public void setPresentationElement(IPresentationElement value) {
		setReferredElement(value);
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 *
	 * @return HRESULT
	 */
	public void establishNodePresence( Document doc, Node parent )
	{
	   buildNodePresence( "UML:PresentationReference", doc, parent );
	}
}

