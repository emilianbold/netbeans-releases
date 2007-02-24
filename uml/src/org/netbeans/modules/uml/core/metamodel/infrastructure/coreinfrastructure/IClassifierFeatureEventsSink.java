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

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IClassifierFeatureEventsSink
{
	/**
	 * Fired whenever a feature is about to be added to a classifier.
	*/
	public void onFeaturePreAdded( IClassifier classifier, IFeature feature, IResultCell cell );

	/**
	 * Fired whenever feature was just added to a classifier.
	*/
	public void onFeatureAdded( IClassifier classifier, IFeature feature, IResultCell cell );

	/**
	 * Fired whenever EnumerationLiteral was just added to a classifier.
	*/
	public void onEnumerationLiteralPreAdded( IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell );

    /**
	 * Fired whenever EnumerationLiteral was just added to a classifier.
	*/
	public void onEnumerationLiteralAdded( IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell );

	/**
	 * Fired whenever a feature is about to be removed from a classifier.
	*/
	public void onFeaturePreRemoved( IClassifier classifier, IFeature feature, IResultCell cell );

	/**
	 * Fired whenever feature was just removed from a classifier.
	*/
	public void onFeatureRemoved( IClassifier classifier, IFeature feature, IResultCell cell );

	/**
	 * Fired whenever a feature is about to be moved from a classifier to another classifier.
	*/
	public void onFeaturePreMoved( IClassifier classifier, IFeature feature, IResultCell cell );

	/**
	 * Fired whenever feature was just moved from a classifier to another Classifier.
	*/
	public void onFeatureMoved( IClassifier classifier, IFeature feature, IResultCell cell );

	/**
	 * Fired whenever a feature is about to be duplicated then added from one classifier to another classifier.
	*/
	public void onFeaturePreDuplicatedToClassifier( IClassifier classifier, IFeature feature, IResultCell cell );

	/**
	 * Fired whenever feature was just duplicated and added from one classifier to another Classifier.
	*/
	public void onFeatureDuplicatedToClassifier( IClassifier pOldClassifier, IFeature pOldFeature, IClassifier pNewClassifier, IFeature pNewFeature, IResultCell cell );

	/**
	 * Fired whenever the abstract flag on the Classifier is about to be modified.
	*/
	public void onPreAbstractModified( IClassifier feature, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever the abstract flag on the Classifier has been modified.
	*/
	public void onAbstractModified( IClassifier feature, IResultCell cell );

	/**
	 * Fired whenever the leaf flag on the Classifier is about to be modified.
	*/
	public void onPreLeafModified( IClassifier feature, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever the leaf flag on the Classifier has been modified.
	*/
	public void onLeafModified( IClassifier feature, IResultCell cell );

	/**
	 * Fired whenever the transient flag on the Classifier is about to be modified.
	*/
	public void onPreTransientModified( IClassifier feature, boolean proposedValue, IResultCell cell );

	/**
	 * Fired whenever the transient flag on the Classifier has been modified.
	*/
	public void onTransientModified( IClassifier feature, IResultCell cell );

	/**
	 * Fired whenever a new template parameter ( ParameterableElemnet ) is about to be added to the Classifier.
	*/
	public void onPreTemplateParameterAdded( IClassifier pClassifier, IParameterableElement pParam, IResultCell cell );

	/**
	 * Fired whenever a new template parameter ( ParameterableElemnet ) is added to the Classifier.
	*/
	public void onTemplateParameterAdded( IClassifier pClassifier, IParameterableElement pParam, IResultCell cell );

	/**
	 * Fired whenever a new template parameter ( ParameterableElemnet ) is about to be added to the Classifier.
	*/
	public void onPreTemplateParameterRemoved( IClassifier pClassifier, IParameterableElement pParam, IResultCell cell );

	/**
	 * Fired whenever a new template parameter ( ParameterableElemnet ) is added to the Classifier.
	*/
	public void onTemplateParameterRemoved( IClassifier pClassifier, IParameterableElement pParam, IResultCell cell );
}
