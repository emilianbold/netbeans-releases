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

package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface ITypedElementEventsSink
{
	/**
	 * Fired whenever the Multiplicity object on a particular element is about to be modified.
	*/
	public void onPreMultiplicityModified( ITypedElement element, IMultiplicity proposedValue, IResultCell cell );

	/**
	 * Fired whenever the Multiplicity object on a particular element was just modified.
	*/
	public void onMultiplicityModified( ITypedElement element, IResultCell cell );

	/**
	 * Fired whenever the type on a particular element is about to be modified.
	*/
	public void onPreTypeModified( ITypedElement element, IClassifier proposedValue, IResultCell cell );

	/**
	 * Fired whenever the type flag on a particular element was just modified.
	*/
	public void onTypeModified( ITypedElement element, IResultCell cell );

	/**
	 * Fired when the lower property on the passed in range is about to be modified.
	*/
	public void onPreLowerModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell );

	/**
	 * Fired when the lower property on the passed in range was modified.
	*/
	public void onLowerModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when the upper property on the passed in range is about to be modified.
	*/
	public void onPreUpperModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell );

	/**
	 * Fired when the upper property on the passed in range was modified.
	*/
	public void onUpperModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when a new range is about to be added to the passed in multiplicity.
	*/
	public void onPreRangeAdded( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when a new range is added to the passed in multiplicity.
	*/
	public void onRangeAdded( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when an existing range is about to be removed from the passed in multiplicity.
	*/
	public void onPreRangeRemoved( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when an existing range is removed from the passed in multiplicity.
	*/
	public void onRangeRemoved( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell );

	/**
	 * Fired when the order property is about to be changed on the passed in mulitplicity.
	*/
	public void onPreOrderModified( ITypedElement element, IMultiplicity mult, boolean proposedValue, IResultCell cell );

	/**
	 * Fired when the order property is changed on the passed in mulitplicity.
	*/
	public void onOrderModified( ITypedElement element, IMultiplicity mult, IResultCell cell );
}
