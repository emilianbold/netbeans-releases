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

/*
 * Created on Nov 10, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * @author aztec
 *
 */
public class TypeChangeRequest extends ChangeRequest implements ITypeChangeRequest
{
	IClassifier m_Classifier;
	String m_OldTypeName;
	String m_NewTypeName;
	/**
	 * @return
	 */
	public IClassifier getModifiedClassifier()
	{
		return m_Classifier;
	}

	/**
	 * @return
	 */
	public String getNewTypeName()
	{
		return m_NewTypeName;
	}

	/**
	 * @return
	 */
	public String getOldTypeName()
	{
		return m_OldTypeName;
	}

	/**
	 * @param classifier
	 */
	public void setModifiedClassifier(IClassifier classifier)
	{
		m_Classifier = classifier;
	}

	/**
	 * @param string
	 */
	public void setNewTypeName(String name)
	{
		m_NewTypeName = name;
	}

	/**
	 * @param string
	 */
	public void setOldTypeName(String name)
	{
		m_OldTypeName = name;
	}

	public IElement getImpactedElement()
	{
		return getBefore();
	}
}



