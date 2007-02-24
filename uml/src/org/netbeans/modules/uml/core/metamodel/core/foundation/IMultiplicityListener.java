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

public interface IMultiplicityListener
{
	/**
	 * Fired when the lower property on the passed in range is about to be modified.
	*/
	public boolean onPreLowerModified( IMultiplicity mult, IMultiplicityRange range, String proposedValue );

	/**
	 * Fired when the lower property on the passed in range was modified.
	*/
	public void onLowerModified( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when the upper property on the passed in range is about to be modified.
	*/
	public boolean onPreUpperModified( IMultiplicity mult, IMultiplicityRange range, String proposedValue );

	/**
	 * Fired when the upper property on the passed in range was modified.
	*/
	public void onUpperModified( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when a new range is about to be added to the passed in multiplicity.
	*/
	public boolean onPreRangeAdded( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when a new range is added to the passed in multiplicity.
	*/
	public void onRangeAdded( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when an existing range is about to be removed from the passed in multiplicity.
	*/
	public boolean onPreRangeRemoved( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when an existing range is removed from the passed in multiplicity.
	*/
	public void onRangeRemoved( IMultiplicity mult, IMultiplicityRange range );

	/**
	 * Fired when the order property is about to be changed on the passed in mulitplicity.
	*/
	public boolean onPreOrderModified( IMultiplicity mult, boolean proposedValue );

	/**
	 * Fired when the order property is changed on the passed in mulitplicity.
	*/
	public void onOrderModified( IMultiplicity mult );

}
