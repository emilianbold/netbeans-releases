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
