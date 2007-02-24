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
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;


public class NodeMapLocation implements INodeMapLocation, IGraphicMapLocation {
	public NodeMapLocation() {
		m_Location = null;
		m_ElementXMIID = null;
		m_Name = null;
		m_ElementType = null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.ILabelMapLocation#getLocation()
	 */
	public IETRect getLocation() {
		return m_Location;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.ILabelMapLocation#setLocation(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public void setLocation(IETRect value) {
		m_Location = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicMapLocation#getElementXMIID()
	 */
	public String getElementXMIID() {
		return m_ElementXMIID;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicMapLocation#setElementXMIID(java.lang.String)
	 */
	public void setElementXMIID(String value) {
		m_ElementXMIID = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicMapLocation#getName()
	 */
	public String getName() {
		return m_Name;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicMapLocation#setName(java.lang.String)
	 */
	public void setName(String value) {
		m_Name = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicMapLocation#getElementType()
	 */
	public String getElementType() {
		return m_ElementType;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicMapLocation#setElementType(java.lang.String)
	 */
	public void setElementType(String value) {
		m_ElementType = value;
	}

	public IElement getElement()
	{
		return m_Element;
	}
	
	
	public void setElement(IElement value)
	{
		m_Element = value;
	}
	
	private IETRect m_Location;
	private String m_ElementXMIID;
	private String m_Name;
	private String m_ElementType;
	private IElement m_Element;
}
