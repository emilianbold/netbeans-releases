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

public interface IBehavioralFeatureEventsSink
{
	/**
	 * Fired whenever the concurrency value of a behavioral feature is about to be modified.
	*/
	public void onConcurrencyPreModified( IBehavioralFeature feature, /* CallConcurrencyKind */ int proposedValue, IResultCell cell );

	/**
	 * Fired whenever the concurrency value of a behavioral feature was modified.
	*/
	public void onConcurrencyModified( IBehavioralFeature feature, IResultCell cell );

	/**
	 * Fired whenever a signal is about to be added to the behavioral feature, indicating that the feature can 'catch' the specified signal.
	*/
	public void onPreHandledSignalAdded( IBehavioralFeature feature, ISignal proposedValue, IResultCell cell );

	/**
	 * Fired whenever a signal is added to the behavioral feature, indicating that the feature can 'catch' the specified signal.
	*/
	public void onHandledSignalAdded( IBehavioralFeature feature, IResultCell cell );

	/**
	 * Fired whenever a signal is about to be removed from the behavioral feature, indicating that the feature can no longer 'catch' the specified signal.
	*/
	public void onPreHandledSignalRemoved( IBehavioralFeature feature, ISignal proposedValue, IResultCell cell );

	/**
	 * Fired whenever a signal was removed from the behavioral feature, indicating that the feature can no longer 'catch' the specified signal.
	*/
	public void onHandledSignalRemoved( IBehavioralFeature feature, IResultCell cell );

	/**
	 * Fired whenever a new parameter is about to be added to the behavioral feature's list of parameters.
	*/
	public void onPreParameterAdded( IBehavioralFeature feature, IParameter parm, IResultCell cell );

	/**
	 * Fired whenever a new parameter was added to the behavioral feature's list of parameters.
	*/
	public void onParameterAdded( IBehavioralFeature feature, IParameter parm, IResultCell cell );

	/**
	 * Fired whenever an existing parameter is about to be removed from the behavioral feature's list of parameters.
	*/
	public void onPreParameterRemoved( IBehavioralFeature feature, IParameter parm, IResultCell cell );

	/**
	 * Fired whenever an existing parameter was just removed from the behavioral feature's list of parameters.
	*/
	public void onParameterRemoved( IBehavioralFeature feature, IParameter parm, IResultCell cell );

	/**
	 * Fired whenever the abstract flag on the behavioral feature is about to be modified.
	*/
	public void onPreAbstractModified( IBehavioralFeature feature, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever the abstract flag on the behavioral feature has been modified.
	*/
	public void onAbstractModified( IBehavioralFeature feature, IResultCell cell );

	/**
	 * Fired whenever the strictfp flag on the behavioral feature is about to be modified.
	*/
	public void onPreStrictFPModified( IBehavioralFeature feature, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever the strictfp flag on the behavioral feature has been modified.
	*/
	public void onStrictFPModified( IBehavioralFeature feature, IResultCell cell );
}
