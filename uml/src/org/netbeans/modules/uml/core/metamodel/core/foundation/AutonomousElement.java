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
 * @author sumitabhk
 *
 */
public class AutonomousElement extends NamedElement implements IAutonomousElement{

	/**
	 *
	 */
	public AutonomousElement() {
		super();
	}

	/**
	 *
	 * Indicates that an AutonomousElement is expanded based on a PackageExtension.
	 * Expanded AutonomousElements need not be interchanged through XMI, but can
	 * always be calculated from the set of packages and package extensions that are
	 * interchanged. Expanded elements are used as ordinary elements, but must not
	 * necessarily have an explicit repository representation. The default value is false.
	 *
	 * @param curVal[out] The current value.
	 *
	 * @return HRESULT
	 *
	 */
	public boolean isExpanded()
	{
		return getBooleanAttributeValue("isExpanded", false);
	}

	/**
	 *
	 * Indicates that an AutonomousElement is expanded based on a PackageExtension.
	 * Expanded AutonomousElements need not be interchanged through XMI, but can
	 * always be calculated from the set of packages and package extensions that are
	 * interchanged. Expanded elements are used as ordinary elements, but must not
	 * necessarily have an explicit repository representation. The default value is false.
	 *
	 * @param newVal[in] The new value.
	 *
	 * @return HRESULT
	 *
	 */
	public void setIsExpanded(boolean newVal )
	{
		setBooleanAttributeValue("isExpanded", newVal);
	}

	/**
	 *
	 * @see NamedElementImpl::PerformDuplication()
	 *
	 */
	public IVersionableElement performDuplication()
	{
		IVersionableElement dup = super.performDuplication();
		IAutonomousElement autoDup = null;
		if (dup instanceof IAutonomousElement)
		{
			autoDup = (IAutonomousElement)dup;
			boolean expanded = isExpanded();
			autoDup.setIsExpanded(expanded);
		}
		return autoDup;
	}

}



