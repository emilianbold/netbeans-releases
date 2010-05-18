/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
