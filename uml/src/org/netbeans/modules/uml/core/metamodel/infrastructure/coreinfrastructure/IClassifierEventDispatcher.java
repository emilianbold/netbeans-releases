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

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IClassifierEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers an event sink to handle classifier feature events.
	*/
	public void registerForClassifierFeatureEvents( IClassifierFeatureEventsSink handler );

	/**
	 * Removes a sink listening for classifier feature events.
	*/
	public void revokeClassifierFeatureSink( IClassifierFeatureEventsSink handler );

	/**
	 * Registers an event sink to handle feature events.
	*/
	public void registerForFeatureEvents( IFeatureEventsSink handler );

	/**
	 * Removes a sink listening for feature events.
	*/
	public void revokeFeatureSink( IFeatureEventsSink handler );

	/**
	 * Registers an event sink to handle structural feature events.
	*/
	public void registerForStructuralFeatureEvents( IStructuralFeatureEventsSink handler );

	/**
	 * Removes a sink listening for structural feature events.
	*/
	public void revokeStructuralFeatureSink( IStructuralFeatureEventsSink handler );

	/**
	 * Registers an event sink to handle behavioral feature events.
	*/
	public void registerForBehavioralFeatureEvents( IBehavioralFeatureEventsSink handler );

	/**
	 * Removes a sink listening for behavioral feature events.
	*/
	public void revokeBehavioralFeatureSink( IBehavioralFeatureEventsSink handler );

	/**
	 * Registers an event sink to handle parameter events.
	*/
	public void registerForParameterEvents( IParameterEventsSink handler );

	/**
	 * Removes a sink listening for parameter events.
	*/
	public void revokeParameterSink( IParameterEventsSink handler );

	/**
	 * Registers an event sink to handle typed element events.
	*/
	public void registerForTypedElementEvents( ITypedElementEventsSink handler );

	/**
	 * Removes a sink listening for typed element events.
	*/
	public void revokeTypedElementSink( ITypedElementEventsSink handler );

	/**
	 * Registers an event sink to handle attribute events.
	*/
	public void registerForAttributeEvents( IAttributeEventsSink handler );

	/**
	 * Removes a sink listening for attribute events.
	*/
	public void revokeAttributeSink( IAttributeEventsSink handler );

	/**
	 * Registers an event sink to handle operation events.
	*/
	public void registerForOperationEvents( IOperationEventsSink handler );

	/**
	 * Removes a sink listening for operation events.
	*/
	public void revokeOperationSink( IOperationEventsSink handler );

	/**
	 * Registers an event sink to handle transform events.
	*/
	public void registerForTransformEvents( IClassifierTransformEventsSink handler );

	/**
	 * Removes a sink listening for transform events.
	*/
	public void revokeTransformSink( IClassifierTransformEventsSink handler );

	/**
	 * Registers an event sink to handle transform events.
	*/
	public void registerForAssociationEndTransformEvents( IAssociationEndTransformEventsSink handler );

	/**
	 * Removes a sink listening for transform events.
	*/
	public void revokeAssociationEndTransformSink( IAssociationEndTransformEventsSink handler );

	/**
	 * Registers an event sink to handle transform events.
	*/
	public void registerForAffectedElementEvents( IAffectedElementEventsSink handler );

	/**
	 * Removes a sink listening for transform events.
	*/
	public void revokeAffectedElementEvents( IAffectedElementEventsSink handler );

	/**
	 * Registers an event sink to handle AssociationEnd events.
	*/
	public void registerForAssociationEndEvents( IAssociationEndEventsSink handler );

	/**
	 * Removes a sink listening for AssociationEnd events.
	*/
	public void revokeAssociationEndSink( IAssociationEndEventsSink handler );

	/**
	 * Called whenever a feature is about to be added to a classifier.
	*/
	public boolean fireFeaturePreAdded( IClassifier classifier, IFeature feature, IEventPayload payload );

	/**
	 * Called whenever a feature was added to a classifier.
	*/
	public void fireFeatureAdded( IClassifier classifier, IFeature feature, IEventPayload payload );

    /**
	 * Called whenever a feature is about to be added to a classifier.
	*/
    public boolean fireEnumerationLiteralPreAdded( IClassifier classifier, IEnumerationLiteral enumLit, IEventPayload payload );
    
    /**
	 * Called whenever a feature was added to a classifier.
	*/
	public void fireEnumerationLiteralAdded( IClassifier classifier, IEnumerationLiteral enumLit, IEventPayload payload );

	/**
	 * Fired whenever feature is about to be removed from a classifier.
	*/
	public boolean fireFeaturePreRemoved( IClassifier classifier, IFeature feature, IEventPayload payload );

	/**
	 * Fired whenever feature was just removed from a classifier.
	*/
	public void fireFeatureRemoved( IClassifier classifier, IFeature feature, IEventPayload payload );

	/**
	 * Fired whenever a feature is about to be moved from a classifier to another classifier.
	*/
	public boolean fireFeaturePreMoved( IClassifier classifier, IFeature feature, IEventPayload payload );

	/**
	 * Fired whenever feature was just moved from a classifier to another Classifier.
	*/
	public void fireFeatureMoved( IClassifier classifier, IFeature feature, IEventPayload payload );

	/**
	 * Fired whenever a feature is about to be duplicated then added from one classifier to another classifier.
	*/
	public boolean fireFeaturePreDuplicatedToClassifier( IClassifier classifier, IFeature feature, IEventPayload payload );

	/**
	 * Fired whenever feature was just duplicated and added from one classifier to another Classifier.
	*/
	public boolean fireFeatureDuplicatedToClassifier( IClassifier pOldClassifier, IFeature pOldFeature, IClassifier pNewClassifier, IFeature pNewFeature, IEventPayload payload );

	/**
	 * Called whenever the abstract flag on the Classifier is about to be modified.
	*/
	public boolean fireClassifierPreAbstractModified( IClassifier feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Called whenever the abstract flag on the Classifier has been modified.
	*/
	public void fireClassifierAbstractModified( IClassifier feature, IEventPayload payload );

	/**
	 * Called whenever the leaf flag on the Classifier is about to be modified.
	*/
	public boolean firePreLeafModified( IClassifier feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Called whenever the leaf flag on the Classifier has been modified.
	*/
	public void fireLeafModified( IClassifier feature, IEventPayload payload );

	/**
	 * Called whenever the transient flag on the Classifier is about to be modified.
	*/
	public boolean fireClassifierPreTransientModified( IClassifier feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Called whenever the transient flag on the Classifier has been modified.
	*/
	public void fireClassifierTransientModified( IClassifier feature, IEventPayload payload );

	/**
	 * Fired whenever a new template parameter ( ParameterableElemnet ) is about to be added to the Classifier.
	*/
	public boolean firePreTemplateParameterAdded( IClassifier pClassifier, IParameterableElement pParam, IEventPayload payload );

	/**
	 * Fired whenever a new template parameter ( ParameterableElemnet ) is added to the Classifier.
	*/
	public void fireTemplateParameterAdded( IClassifier pClassifier, IParameterableElement pParam, IEventPayload payload );

	/**
	 * Fired whenever a new template parameter ( ParameterableElemnet ) is about to be added to the Classifier.
	*/
	public boolean firePreTemplateParameterRemoved( IClassifier pClassifier, IParameterableElement pParam, IEventPayload payload );

	/**
	 * Fired whenever a new template parameter ( ParameterableElemnet ) is added to the Classifier.
	*/
	public void fireTemplateParameterRemoved( IClassifier pClassifier, IParameterableElement pParam, IEventPayload payload );

	/**
	 * Called whenever a classifier is about to be transformed.
	*/
	public boolean firePreTransform( IClassifier classifier, String newForm, IEventPayload payload );

	/**
	 * Called whenever a classifier is transformed.
	*/
	public void fireTransformed( IClassifier classifier, IEventPayload payload );

	/**
	 * Called whenever an association end is about to be transformed.
	*/
	public boolean firePreAssociationEndTransform( IAssociationEnd pEnd, String newForm, IEventPayload payload );

	/**
	 * Called whenever an association end is transformed.
	*/
	public void fireAssociationEndTransformed( IAssociationEnd pEnd, IEventPayload payload );

	/**
	 * Fired whenever the static flag on a particular feature is about to be modified.
	*/
	public boolean firePreStaticModified( IFeature feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the static flag on a particular feature was just modified.
	*/
	public void fireStaticModified( IFeature feature, IEventPayload payload );

	/**
	 * Fired whenever the native flag on a particular feature is about to be modified.
	*/
	public boolean firePreNativeModified( IFeature feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the native flag on a particular feature was just modified.
	*/
	public void fireNativeModified( IFeature feature, IEventPayload payload );

	/**
	 * Fired whenever the ClientChangeablity flag on a particular feature is about to be modified.
	*/
	public boolean firePreChangeabilityModified( IStructuralFeature feature, /* ChangeableKind */ int proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the ClientChangeablity flag on a particular feature was just modified.
	*/
	public void fireChangeabilityModified( IStructuralFeature feature, IEventPayload payload );

	/**
	 * Fired whenever the volatile flag on a particular feature is about to be modified.
	*/
	public boolean firePreVolatileModified( IStructuralFeature feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the volatile flag on a particular feature has been modified.
	*/
	public void fireVolatileModified( IStructuralFeature feature, IEventPayload payload );

	/**
	 * Fired whenever the transient flag on a particular feature is about to be modified.
	*/
	public boolean firePreTransientModified( IStructuralFeature feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the transient flag on a particular feature has been modified.
	*/
	public void fireTransientModified( IStructuralFeature feature, IEventPayload payload );

	/**
	 * Fired whenever the concurrency value of a behavioral feature is about to be modified.
	*/
	public boolean fireConcurrencyPreModified( IBehavioralFeature feature, /* CallConcurrencyKind */ int proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the concurrency value of a behavioral feature was modified.
	*/
	public void fireConcurrencyModified( IBehavioralFeature feature, IEventPayload payload );

	/**
	 * Fired whenever a signal is about to be added to the behavioral feature, indicating that the feature can 'catch' the specified signal.
	*/
	public boolean firePreHandledSignalAdded( IBehavioralFeature feature, ISignal proposedValue, IEventPayload payload );

	/**
	 * Fired whenever a signal is added to the behavioral feature, indicating that the feature can 'catch' the specified signal.
	*/
	public void fireHandledSignalAdded( IBehavioralFeature feature, IEventPayload payload );

	/**
	 * Fired whenever a signal is about to be removed from the behavioral feature, indicating that the feature can no longer 'catch' the specified signal.
	*/
	public boolean firePreHandledSignalRemoved( IBehavioralFeature feature, ISignal proposedValue, IEventPayload payload );

	/**
	 * Fired whenever a signal was removed from the behavioral feature, indicating that the feature can no longer 'catch' the specified signal.
	*/
	public void fireHandledSignalRemoved( IBehavioralFeature feature, IEventPayload payload );

	/**
	 * Fired whenever a new parameter is about to be added to the behavioral feature's list of parameters.
	*/
	public boolean firePreParameterAdded( IBehavioralFeature feature, IParameter parm, IEventPayload payload );

	/**
	 * Fired whenever a new parameter was added to the behavioral feature's list of parameters.
	*/
	public void fireParameterAdded( IBehavioralFeature feature, IParameter parm, IEventPayload payload );

	/**
	 * Fired whenever an existing parameter is about to be removed from the behavioral feature's list of parameters.
	*/
	public boolean firePreParameterRemoved( IBehavioralFeature feature, IParameter parm, IEventPayload payload );

	/**
	 * Fired whenever an existing parameter was just removed from the behavioral feature's list of parameters.
	*/
	public void fireParameterRemoved( IBehavioralFeature feature, IParameter parm, IEventPayload payload );

	/**
	 * Fired whenever the abstract flag on the behavioral feature is about to be modified.
	*/
	public boolean firePreAbstractModified( IBehavioralFeature feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the abstract flag on the behavioral feature has been modified.
	*/
	public void fireAbstractModified( IBehavioralFeature feature, IEventPayload payload );

	/**
	 * Fired whenever the strictfp flag on the behavioral feature is about to be modified.
	*/
	public boolean firePreStrictFPModified( IBehavioralFeature feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the strictfp flag on the behavioral feature has been modified.
	*/
	public void fireStrictFPModified( IBehavioralFeature feature, IEventPayload payload );

	/**
	 * Fired whenever the default expression for the parameter is about to change.
	*/
	public boolean firePreDefaultExpModified( IParameter feature, IExpression proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the default expression for the parameter has changed.
	*/
	public void fireDefaultExpModified( IParameter feature, IEventPayload payload );

	/**
	 * Fired whenever the default expression's body property for the parameter is about to change.
	*/
	public boolean firePreDefaultExpBodyModified( IParameter feature, String bodyValue, IEventPayload payload );

	/**
	 * Fired whenever the default expression's body property for the parameter has changed.
	*/
	public void fireDefaultExpBodyModified( IParameter feature, IEventPayload payload );

	/**
	 * Fired whenever the default expression's language property for the parameter is about to change.
	*/
	public boolean firePreDefaultExpLanguageModified( IParameter feature, String language, IEventPayload payload );

	/**
	 * Fired whenever the default expression's language property for the parameter has changed.
	*/
	public void fireDefaultExpLanguageModified( IParameter feature, IEventPayload payload );

	/**
	 * Fired whenever the direction value of the parameter is about to change.
	*/
	public boolean firePreDirectionModified( IParameter feature, /* ParameterDirectionKind */ int proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the direction value of the parameter has changed.
	*/
	public void fireDirectionModified( IParameter feature, IEventPayload payload );

	/**
	 * Fired whenever the Multiplicity object on a particular element is about to be modified.
	*/
	public boolean firePreMultiplicityModified( ITypedElement element, IMultiplicity proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the Multiplicity object on a particular element was just modified.
	*/
	public void fireMultiplicityModified( ITypedElement element, IEventPayload payload );

	/**
	 * Fired whenever the type on a particular element is about to be modified.
	*/
	public boolean firePreTypeModified( ITypedElement element, IClassifier proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the type flag on a particular element was just modified.
	*/
	public void fireTypeModified( ITypedElement element, IEventPayload payload );

	/**
	 * Fired when the lower property on the passed in range is about to be modified.
	*/
	public boolean firePreLowerModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IEventPayload payload );

	/**
	 * Fired when the lower property on the passed in range was modified.
	*/
	public void fireLowerModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IEventPayload payload );

	/**
	 * Fired when the upper property on the passed in range is about to be modified.
	*/
	public boolean firePreUpperModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IEventPayload payload );

	/**
	 * Fired when the upper property on the passed in range was modified.
	*/
	public void fireUpperModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IEventPayload payload );

	/**
	 * Fired when a new range is about to be added to the passed in multiplicity.
	*/
	public boolean firePreRangeAdded( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IEventPayload payload );

	/**
	 * Fired when a new range is added to the passed in multiplicity.
	*/
	public void fireRangeAdded( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IEventPayload payload );

	/**
	 * Fired when an existing range is about to be removed from the passed in multiplicity.
	*/
	public boolean firePreRangeRemoved( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IEventPayload payload );

	/**
	 * Fired when an existing range is removed from the passed in multiplicity.
	*/
	public void fireRangeRemoved( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IEventPayload payload );

	/**
	 * Fired when the order property is about to be changed on the passed in mulitplicity.
	*/
	public boolean firePreOrderModified( ITypedElement element, IMultiplicity mult, boolean proposedValue, IEventPayload payload );

	/**
	 * Fired when the order property is changed on the passed in mulitplicity.
	*/
	public void fireOrderModified( ITypedElement element, IMultiplicity mult, IEventPayload payload );

        /**
	 * Fired when the order property is changed on the passed in mulitplicity.
	*/
	public void fireCollectionTypeModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IEventPayload payload );
        
	/**
	 * Fired whenever the default value of an IAttribute is about to be modified.
	*/
	public boolean fireDefaultPreModified( IAttribute attr, IExpression proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the default value of an IAttribute was modified.
	*/
	public void fireDefaultModified( IAttribute attr, IEventPayload payload );

	/**
	 * Fired whenever the default expression's body property for the attribute is about to change.
	*/
	public boolean firePreDefaultBodyModified( IAttribute feature, String bodyValue, IEventPayload payload );

	/**
	 * Fired whenever the default expression's body property for the attribute has changed.
	*/
	public void fireDefaultBodyModified( IAttribute feature, IEventPayload payload );

	/**
	 * Fired whenever the default expression's language property for the attribute is about to change.
	*/
	public boolean firePreDefaultLanguageModified( IAttribute feature, String language, IEventPayload payload );

	/**
	 * Fired whenever the default expression's language property for the attribute has changed.
	*/
	public void fireDefaultLanguageModified( IAttribute feature, IEventPayload payload );

	/**
	 * Fired whenever the attributes derived property is about to change..
	*/
	public boolean firePreDerivedModified( IAttribute feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the attributes derived property has changed.
	*/
	public void fireDerivedModified( IAttribute feature, IEventPayload payload );

	/**
	 * Fired whenever the attributes primary key property is about to change..
	*/
	public boolean firePrePrimaryKeyModified( IAttribute feature, boolean proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the attributes primary key property has changed.
	*/
	public void firePrimaryKeyModified( IAttribute feature, IEventPayload payload );

	/**
	 * Fired whenever a pre or post condition is about to be added to an operation.
	*/
	public boolean fireConditionPreAdded( IOperation oper, IConstraint condition, boolean isPreCondition, IEventPayload payload );

	/**
	 * Fired whenever  a pre or post condition has been added to an operation.
	*/
	public void fireConditionAdded( IOperation oper, IConstraint condition, boolean isPreCondition, IEventPayload payload );

	/**
	 * Fired whenever a pre or post condition is about to be removed from an operation.
	*/
	public boolean fireConditionPreRemoved( IOperation oper, IConstraint condition, boolean isPreCondition, IEventPayload payload );

	/**
	 * Fired whenever a pre or post condition has been removed from an operation.
	*/
	public void fireConditionRemoved( IOperation oper, IConstraint condition, boolean isPreCondition, IEventPayload payload );

	/**
	 * Fired whenever the query flag on an operation is about to be modified.
	*/
	public boolean firePreQueryModified( IOperation oper, boolean proposedValue, IEventPayload payload );

	/**
	 * Fired whenever the query flag on an operation has been modified.
	*/
	public void fireQueryModified( IOperation oper, IEventPayload payload );

	/**
	 * Fired whenever a RaisedException is about to be added to an operation.
	*/
	public boolean fireRaisedExceptionPreAdded( IOperation oper, IClassifier pException, IEventPayload payload );

	/**
	 * Fired whenever a RaisedException has been added to an operation.
	*/
	public void fireRaisedExceptionAdded( IOperation oper, IClassifier pException, IEventPayload payload );

	/**
	 * Fired whenever a RaisedException is about to be removed from an operation.
	*/
	public boolean fireRaisedExceptionPreRemoved( IOperation oper, IClassifier pException, IEventPayload payload );

	/**
	 * Fired whenever a RaisedException has been removed from an operation.
	*/
	public void fireRaisedExceptionRemoved( IOperation oper, IClassifier pException, IEventPayload payload );

	/**
	 * Fired whenever the passed in Classifier's name is about to change.
	*/
	public boolean firePreImpacted( IClassifier classifier, ETList<IVersionableElement> impacted, IEventPayload payload );

	/**
	 * Fired whenever the passed in Classifier's name has changed.
	*/
	public void fireImpacted( IClassifier classifier, ETList<IVersionableElement> impacted, IEventPayload payload );

	/**
	 * Fired right before a qualifying attribute is added to this end.
	*/
	public boolean firePreQualifierAttributeAdded( IAssociationEnd pEnd, IAttribute pAttr, IEventPayload payload );

	/**
	 * Fired after a qualifying attribute was added to this end.
	*/
	public void fireQualifierAttributeAdded( IAssociationEnd pEnd, IAttribute pAttr, IEventPayload payload );

	/**
	 * Fired right before a qualifying attribute is removed from this end.
	*/
	public boolean firePreQualifierAttributeRemoved( IAssociationEnd pEnd, IAttribute pAttr, IEventPayload payload );

	/**
	 * Fired after a qualifying attribute was removed from this end.
	*/
	public void fireQualifierAttributeRemoved( IAssociationEnd pEnd, IAttribute pAttr, IEventPayload payload );
	
	/**
	 * Fired when a property changes on the operation
	 */
	public void fireOperationPropertyModified(IOperation oper,int kind,IEventPayload payload);
	
	/**
	 * Fired when a property is about to be changed on the operation
	 */
	public boolean firePreOperationPropertyModified(IOperation oper,int kind,boolean proposedValue, IEventPayload payload);
}
