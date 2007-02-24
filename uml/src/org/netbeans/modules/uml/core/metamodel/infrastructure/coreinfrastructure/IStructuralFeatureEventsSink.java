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

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IStructuralFeatureEventsSink
{
	/**
	 * Fired whenever the ClientChangeablity flag on a particular feature is about to be modified.
	*/
	public void onPreChangeabilityModified( IStructuralFeature feature, /* ChangeableKind */ int proposedValue, IResultCell cell );

	/**
	 * Fired whenever the ClientChangeablity flag on a particular feature was just modified.
	*/
	public void onChangeabilityModified( IStructuralFeature feature, IResultCell cell );

	/**
	 * Fired whenever the volatile flag on a particular feature is about to be modified.
	*/
	public void onPreVolatileModified( IStructuralFeature feature, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever the volatile flag on particular feature was just modified.
	*/
	public void onVolatileModified( IStructuralFeature feature, IResultCell cell );

	/**
	 * Fired whenever the transient flag on a particular feature is about to be modified.
	*/
	public void onPreTransientModified( IStructuralFeature feature, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever the transient flag on particular feature was just modified.
	*/
	public void onTransientModified( IStructuralFeature feature, IResultCell cell );
}
