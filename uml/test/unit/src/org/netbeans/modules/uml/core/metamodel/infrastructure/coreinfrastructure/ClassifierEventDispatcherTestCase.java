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

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for ClassifierEventDispatcher.
 */
public class ClassifierEventDispatcherTestCase extends AbstractUMLTestCase
        implements IBehavioralFeatureEventsSink, IClassifierFeatureEventsSink,
                   IFeatureEventsSink, IStructuralFeatureEventsSink,
                   IParameterEventsSink, ITypedElementEventsSink, 
                   IAttributeEventsSink, IOperationEventsSink, 
                   IClassifierTransformEventsSink, 
                   IAssociationEndTransformEventsSink, 
                   IAssociationEndEventsSink, IAffectedElementEventsSink
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ClassifierEventDispatcherTestCase.class);
    }

    private ClassifierEventDispatcher disp;
    private IClass clazz;
        
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        disp  = new ClassifierEventDispatcher();
        disp.registerForBehavioralFeatureEvents(this);
        disp.registerForClassifierFeatureEvents(this);
        disp.registerForFeatureEvents(this);
        disp.registerForStructuralFeatureEvents(this);
        disp.registerForParameterEvents(this);
        disp.registerForTypedElementEvents(this);
        disp.registerForAttributeEvents(this);
        disp.registerForOperationEvents(this);
        disp.registerForTransformEvents(this);
        disp.registerForAssociationEndTransformEvents(this);
        disp.registerForAssociationEndEvents(this);
        disp.registerForAffectedElementEvents(this);
        
        clazz = createClass("Hydrogen");
        
        clearFireStatuses();
    }
    
    /**
     * 
     */
    private void clearFireStatuses()
    {
        onConcurrencyPreModified = false;
        onConcurrencyModified = false;
        onPreHandledSignalAdded = false;
        onHandledSignalAdded = false;
        onPreHandledSignalRemoved = false;
        onHandledSignalRemoved = false;
        onPreParameterAdded = false;
        onParameterAdded = false;
        onPreParameterRemoved = false;
        onParameterRemoved = false;
        onPreAbstractModified = false;
        onAbstractModified = false;
        onPreStrictFPModified = false;
        onStrictFPModified = false;
        onChangeabilityModified = false;
        onConditionAdded = false;
        onConditionPreAdded = false;
        onConditionPreRemoved = false;
        onConditionRemoved = false;
        onDefaultBodyModified = false;
        onDefaultExpBodyModified = false;
        onDefaultExpLanguageModified = false;
        onDefaultExpModified = false;
        onDefaultLanguageModified = false;
        onDefaultModified = false;
        onDefaultPreModified = false;
        onDerivedModified = false;
        onDirectionModified = false;
        onFeatureAdded = false;
        onFeatureDuplicatedToClassifier = false;
        onFeatureMoved = false;
        onFeaturePreAdded = false;
        onFeaturePreDuplicatedToClassifier = false;
        onFeaturePreMoved = false;
        onFeaturePreRemoved = false;
        onFeatureRemoved = false;
        onImpacted = false;
        onLeafModified = false;
        onLowerModified = false;
        onMultiplicityModified = false;
        onNativeModified = false;
        onOrderModified = false;
        onPreChangeabilityModified = false;
        onPreDefaultBodyModified = false;
        onPreDefaultExpBodyModified = false;
        onPreDefaultExpLanguageModified = false;
        onPreDefaultExpModified = false;
        onPreDefaultLanguageModified = false;
        onPreDerivedModified = false;
        onPreDirectionModified = false;
        onPreImpacted = false;
        onPreLeafModified = false;
        onPreLowerModified = false;
        onPreMultiplicityModified = false;
        onPreNativeModified = false;
        onPreOrderModified = false;
        onPrePrimaryKeyModified = false;
        onPreQualifierAttributeAdded = false;
        onPreQualifierAttributeRemoved = false;
        onPreQueryModified = false;
        onPreRangeAdded = false;
        onPreRangeRemoved = false;
        onPreStaticModified = false;
        onPreTemplateParameterAdded = false;
        onPreTemplateParameterRemoved = false;
        onPreTransform = false;
        onPreTransientModified = false;
        onPreTypeModified = false;
        onPreUpperModified = false;
        onPreVolatileModified = false;
        onPrimaryKeyModified = false;
        onQualifierAttributeAdded = false;
        onQualifierAttributeRemoved = false;
        onQueryModified = false;
        onRaisedExceptionAdded = false;
        onRaisedExceptionPreAdded = false;
        onRaisedExceptionPreRemoved = false;
        onRaisedExceptionRemoved = false;
        onRangeAdded = false;
        onRangeRemoved = false;
        onStaticModified = false;
        onTemplateParameterAdded = false;
        onTemplateParameterRemoved = false;
        onTransformed = false;
        onTransientModified = false;
        onTypeModified = false;
        onUpperModified = false;
        onVolatileModified = false;
        onPreOperationPropertyModified = false;
        onOperationPropertyModified = false;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        project.removeOwnedElement(clazz);
        clazz.delete();
    }

    public void testFireAbstractModified()
    {
        IBehavioralFeature feat = clazz.createOperation("int", "proton");
        disp.fireAbstractModified(feat, null);
        assertTrue(onAbstractModified);
    }
    
    public void testRevokeAffectedElementEvents()
    {
        disp.revokeAffectedElementEvents(this);
        disp.fireImpacted(clazz, new ETArrayList<IVersionableElement>(), null);
        assertFalse(onImpacted);
    }
    
    public void testRevokeAssociationEndSink()
    {
        disp.revokeAssociationEndSink(this);
        IAttribute attr = clazz.createAttribute("char", "q");
        IAssociationEnd end = factory.createAssociationEnd(null);
        disp.fireQualifierAttributeAdded(end, attr, null);
        assertFalse(onQualifierAttributeAdded);
    }
    
    public void testRevokeAssociationEndTransformSink()
    {
        disp.revokeAssociationEndTransformSink(this);
        IAssociationEnd end = factory.createAssociationEnd(null);
        disp.fireAssociationEndTransformed(end, null);
        assertFalse(onTransformed);
    }
    
    public void testFireAssociationEndTransformed()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        disp.fireAssociationEndTransformed(end, null);
        assertTrue(onTransformed);
    }
    
    public void testRevokeAttributeSink()
    {
        disp.revokeAttributeSink(this);
        disp.fireDerivedModified(clazz.createAttribute("char", "q"), null);
        assertFalse(onDerivedModified);
    }
    
    public void testRevokeBehavioralFeatureSink()
    {
        disp.revokeBehavioralFeatureSink(this);
        disp.fireConcurrencyModified(clazz.createOperation("int", "canoe"), 
            null);
        assertFalse(onConcurrencyModified);
    }
    
    public void testFireChangeabilityModified()
    {
        disp.fireChangeabilityModified(clazz.createAttribute("int", "ale"), 
            null);
        assertTrue(onChangeabilityModified);
    }
    
    public void testFireClassifierAbstractModified()
    {
        disp.fireClassifierAbstractModified(clazz, null);
        assertTrue(onAbstractModified);
    }
    
    public void testRevokeClassifierFeatureSink()
    {
        disp.revokeClassifierFeatureSink(this);
        disp.fireClassifierAbstractModified(clazz, null);
        assertFalse(onAbstractModified);
    }
    
    public void testFireClassifierPreAbstractModified()
    {
        disp.fireClassifierPreAbstractModified(clazz, true, null);
        assertTrue(onPreAbstractModified);
    }
    
    public void testFireClassifierPreTransientModified()
    {
        disp.fireClassifierPreTransientModified(clazz, false, null);
        assertTrue(onPreTransientModified);
    }
    
    public void testFireClassifierTransientModified()
    {
        disp.fireClassifierTransientModified(clazz, null);
        assertTrue(onTransientModified);
    }
    
    public void testFireConcurrencyModified()
    {
        disp.fireConcurrencyModified(clazz.createOperation("int", "cron"), null);
        assertTrue(onConcurrencyModified);
    }
    
    public void testFireConcurrencyPreModified()
    {
        disp.fireConcurrencyPreModified(clazz.createOperation("int", "cron"), 
            0, null);
        assertTrue(onConcurrencyPreModified);
    }
    
    public void testFireConditionAdded()
    {
        disp.fireConditionAdded(clazz.createOperation3(), 
            factory.createConstraint(null), false, null);
        assertTrue(onConditionAdded);
    }
    
    public void testFireConditionPreAdded()
    {
        disp.fireConditionPreAdded(clazz.createOperation3(), 
            factory.createConstraint(null), false, null);
        assertTrue(onConditionPreAdded);
    }
    
    public void testFireConditionPreRemoved()
    {
        disp.fireConditionPreRemoved(clazz.createOperation3(), 
            factory.createConstraint(null), false, null);
        assertTrue(onConditionPreRemoved);
    }
    
    public void testFireConditionRemoved()
    {
        disp.fireConditionRemoved(clazz.createOperation3(), 
            factory.createConstraint(null), false, null);
        assertTrue(onConditionRemoved);
    }
    
    public void testFireDefaultBodyModified()
    {
        disp.fireDefaultBodyModified(clazz.createAttribute3(), null);
        assertTrue(onDefaultBodyModified);
    }
    
    public void testFireDefaultExpBodyModified()
    {
        IOperation oper = clazz.createOperation3();
        clazz.addOperation(oper);
        IParameter par = oper.createParameter("int", "x");
        disp.fireDefaultExpBodyModified(par, null);
        assertTrue(onDefaultExpBodyModified);
    }
    
    public void testFireDefaultExpLanguageModified()
    {
        IOperation oper = clazz.createOperation3();
        clazz.addOperation(oper);
        IParameter par = oper.createParameter("int", "x");
        disp.fireDefaultExpLanguageModified(par, null);
        assertTrue(onDefaultExpLanguageModified);
    }
    
    public void testFireDefaultExpModified()
    {
        IOperation oper = clazz.createOperation3();
        clazz.addOperation(oper);
        IParameter par = oper.createParameter("int", "x");
        disp.fireDefaultExpModified(par, null);
        assertTrue(onDefaultExpModified);
    }
    
    public void testFireDefaultLanguageModified()
    {
        disp.fireDefaultLanguageModified(clazz.createAttribute3(), null);
        assertTrue(onDefaultLanguageModified);
    }
    
    public void testFireDefaultModified()
    {
        disp.fireDefaultModified(clazz.createAttribute3(), null);
        assertTrue(onDefaultModified);
    }
    
    public void testFireDefaultPreModified()
    {
        disp.fireDefaultPreModified(clazz.createAttribute3(), 
            factory.createExpression(null), null);
        assertTrue(onDefaultPreModified);
    }
    
    public void testFireDerivedModified()
    {
        disp.fireDerivedModified(clazz.createAttribute3(), null);
        assertTrue(onDerivedModified);
    }
    
    public void testFireDirectionModified()
    {
        IOperation oper = clazz.createOperation3();
        clazz.addOperation(oper);
        IParameter par = oper.createParameter("int", "x");
        disp.fireDirectionModified(par, null);
        assertTrue(onDirectionModified);
    }
    
    public void testFireFeatureAdded()
    {
        disp.fireFeatureAdded(clazz, clazz.createOperation3(), null);
        assertTrue(onFeatureAdded);
    }
    
    public void testFireFeatureDuplicatedToClassifier()
    {
        IClass newClazz = createClass("NewC");
        IOperation oldF = clazz.createOperation3(),
                   newF = newClazz.createOperation3();
        
        disp.fireFeatureDuplicatedToClassifier(clazz, oldF, newClazz, newF, 
            null);
        assertTrue(onFeatureDuplicatedToClassifier);
    }
    
    public void testFireFeatureMoved()
    {
        disp.fireFeatureMoved(clazz, clazz.createOperation3(), null);
        assertTrue(onFeatureMoved);
    }
    
    public void testFireFeaturePreMoved()
    {
        disp.fireFeaturePreMoved(clazz, clazz.createOperation3(), null);
        assertTrue(onFeaturePreMoved);
    }
    
    public void testFireFeaturePreAdded()
    {
        disp.fireFeaturePreAdded(clazz, clazz.createOperation3(), null);
        assertTrue(onFeaturePreAdded);
    }
    
    public void testFireFeaturePreDuplicatedToClassifier()
    {
        IOperation oldF = clazz.createOperation3();
        
        disp.fireFeaturePreDuplicatedToClassifier(clazz, oldF, null);
        assertTrue(onFeaturePreDuplicatedToClassifier);
    }
    
    public void testFireFeaturePreRemoved()
    {
        disp.fireFeaturePreRemoved(clazz, clazz.createOperation3(), null);
        assertTrue(onFeaturePreRemoved);
    }
    
    public void testFireFeatureRemoved()
    {
        disp.fireFeatureRemoved(clazz, clazz.createOperation3(), null);
        assertTrue(onFeatureRemoved);
    }
    
    public void testRevokeFeatureSink()
    {
        disp.revokeFeatureSink(this);
        disp.fireStaticModified(clazz.createOperation3(), null);
        assertFalse(onStaticModified);
    }
    
    public void testRegisterForAffectedElementEvents()
    {
        // Tested by various fire tests
    }
    
    public void testRegisterForAssociationEndEvents()
    {
        // Tested by various fire tests
    }
    public void testRegisterForAssociationEndTransformEvents()
    {
        // Tested by various fire tests
    }
    public void testRegisterForAttributeEvents()
    {
        // Tested by various fire tests
    }
    public void testRegisterForBehavioralFeatureEvents()
    {
        // Tested by various fire tests
    }
    public void testRegisterForClassifierFeatureEvents()
    {
        // Tested by various fire tests
    }
    public void testRegisterForFeatureEvents()
    {
        // Tested by various fire tests
    }
    public void testRegisterForOperationEvents()
    {
        // Tested by various fire tests
    }
    public void testRegisterForParameterEvents()
    {
        // Tested by various fire tests
    }
    public void testRegisterForStructuralFeatureEvents()
    {
        // Tested by various fire tests
    }
    public void testRegisterForTransformEvents()
    {
        // Tested by various fire tests
    }
    public void testRegisterForTypedElementEvents()
    {
        // Tested by various fire tests
    }
    public void testFireHandledSignalAdded()
    {
        disp.fireHandledSignalAdded(clazz.createOperation3(), null);
        assertTrue(onHandledSignalAdded);
    }
    public void testFireHandledSignalRemoved()
    {
        disp.fireHandledSignalRemoved(clazz.createOperation3(), null);
        assertTrue(onHandledSignalRemoved);
    }
    
    public void testFireImpacted()
    {
        disp.fireImpacted(clazz, new ETArrayList<IVersionableElement>(), null);
        assertTrue(onImpacted);
    }
    
    public void testFireLeafModified()
    {
        disp.fireLeafModified(clazz, null);
        assertTrue(onLeafModified);
    }
    
    public void testFireLowerModified()
    {
        IAttribute attr = clazz.createAttribute3();
        IMultiplicity mul = factory.createMultiplicity(null);
        IMultiplicityRange mulr = factory.createMultiplicityRange(null);
        disp.fireLowerModified(attr, mul, mulr, null);
        assertTrue(onLowerModified);
    }
    
    public void testFireMultiplicityModified()
    {
        disp.fireMultiplicityModified(clazz.createAttribute3(), null);
        assertTrue(onMultiplicityModified);
    }
    
    public void testFireNativeModified()
    {
        disp.fireNativeModified(clazz.createOperation3(), null);
        assertTrue(onNativeModified);
    }
    
    public void testFireOperationPropertyModified()
    {
        disp.fireOperationPropertyModified(clazz.createOperation3(), 10, null);
        assertTrue(onOperationPropertyModified);
    }
    
    public void testRevokeOperationSink()
    {
        disp.revokeOperationSink(this);
        disp.fireOperationPropertyModified(clazz.createOperation3(), 10, null);
        assertFalse(onOperationPropertyModified);
    }
    
    public void testFireOrderModified()
    {
        disp.fireOrderModified(clazz.createAttribute3(), factory.createMultiplicity(null), null);
        assertTrue(onOrderModified);
    }
    public void testFireParameterAdded()
    {
        disp.fireParameterAdded(clazz.createOperation3(), factory.createParameter(null), null);
        assertTrue(onParameterAdded);
    }
    
    public void testFireParameterRemoved()
    {
        disp.fireParameterRemoved(clazz.createOperation3(), factory.createParameter(null), null);
        assertTrue(onParameterRemoved);
    }
    
    public void testRevokeParameterSink()
    {
        disp.revokeParameterSink(this);
        disp.fireDirectionModified(factory.createParameter(null), null);
        assertFalse(onDirectionModified);
    }
    
    public void testFirePreAbstractModified()
    {
        disp.firePreAbstractModified(clazz.createOperation3(), false, null);
        assertTrue(onPreAbstractModified);
    }
    
    public void testFirePreAssociationEndTransform()
    {
        disp.firePreAssociationEndTransform(factory.createAssociationEnd(null), 
            "NavigableEnd", null);
        assertTrue(onPreTransform);
    }
    
    public void testFirePreChangeabilityModified()
    {
        disp.firePreChangeabilityModified(clazz.createAttribute3(), 10, null);
        assertTrue(onPreChangeabilityModified);
    }
    
    public void testFirePreDefaultBodyModified()
    {
        disp.firePreDefaultBodyModified(clazz.createAttribute3(), "z", null);
        assertTrue(onPreDefaultBodyModified);
    }
    
    public void testFirePreDefaultExpBodyModified()
    {
        disp.firePreDefaultExpBodyModified(factory.createParameter(null), "z", null);
        assertTrue(onPreDefaultExpBodyModified);
    }
    
    public void testFirePreDefaultExpLanguageModified()
    {
        disp.firePreDefaultExpLanguageModified(factory.createParameter(null), "j", null);
        assertTrue(onPreDefaultExpLanguageModified);
    }
    
    public void testFirePreDefaultExpModified()
    {
        disp.firePreDefaultExpModified(factory.createParameter(null), factory.createExpression(null), null);
        assertTrue(onPreDefaultExpModified);
    }
    
    public void testFirePreDefaultLanguageModified()
    {
        disp.firePreDefaultLanguageModified(clazz.createAttribute3(), "java", null);
        assertTrue(onPreDefaultLanguageModified);
    }
    
    public void testFirePreDerivedModified()
    {
        disp.firePreDerivedModified(clazz.createAttribute3(), true, null);
        assertTrue(onPreDerivedModified);
    }
    
    public void testFirePreDirectionModified()
    {
        disp.firePreDirectionModified(factory.createParameter(null), 3, null);
        assertTrue(onPreDirectionModified);
    }
    
    public void testFirePreHandledSignalAdded()
    {
        disp.firePreHandledSignalAdded(clazz.createOperation3(), factory.createSignal(null), null);
        assertTrue(onPreHandledSignalAdded);
    }
    
    public void testFirePreHandledSignalRemoved()
    {
        disp.firePreHandledSignalRemoved(clazz.createOperation3(), factory.createSignal(null), null);
        assertTrue(onPreHandledSignalRemoved);
    }
    
    public void testFirePreImpacted()
    {
        disp.firePreImpacted(clazz, new ETArrayList<IVersionableElement>(), null);
        assertTrue(onPreImpacted);
    }
    
    public void testFirePreLeafModified()
    {
        disp.firePreLeafModified(clazz, false, null);
        assertTrue(onPreLeafModified);
    }
    
    public void testFirePreLowerModified()
    {
        disp.firePreLowerModified(clazz.createAttribute3(), 
            factory.createMultiplicity(null), 
            factory.createMultiplicityRange(null), "0..10", null);
        assertTrue(onPreLowerModified);
    }
    
    public void testFirePreMultiplicityModified()
    {
        disp.firePreMultiplicityModified(clazz.createAttribute3(), factory.createMultiplicity(null), null);
        assertTrue(onPreMultiplicityModified);
    }
    
    public void testFirePreNativeModified()
    {
        disp.firePreNativeModified(clazz.createOperation3(), true, null);
        assertTrue(onPreNativeModified);
    }
    
    public void testFirePreOperationPropertyModified()
    {
        disp.firePreOperationPropertyModified(clazz.createOperation3(), 1, false, null);
        assertTrue(onPreOperationPropertyModified);
    }
    
    public void testFirePreOrderModified()
    {
        disp.firePreOrderModified(clazz.createAttribute3(), factory.createMultiplicity(null), false, null);
        assertTrue(onPreOrderModified);
    }
    
    public void testFirePreParameterAdded()
    {
        disp.firePreParameterAdded(clazz.createOperation3(), factory.createParameter(null), null);
        assertTrue(onPreParameterAdded);
    }
    
    public void testFirePreParameterRemoved()
    {
        disp.firePreParameterRemoved(clazz.createOperation3(), factory.createParameter(null), null);
        assertTrue(onPreParameterRemoved);
    }
    
    public void testFirePrePrimaryKeyModified()
    {
        disp.firePrePrimaryKeyModified(clazz.createAttribute3(), false, null);
        assertTrue(onPrePrimaryKeyModified);
    }
    
    public void testFirePreQualifierAttributeAdded()
    {
        disp.firePreQualifierAttributeAdded(factory.createAssociationEnd(null), clazz.createAttribute3(), null);
        assertTrue(onPreQualifierAttributeAdded);
    }
    
    public void testFirePreQualifierAttributeRemoved()
    {
        disp.firePreQualifierAttributeRemoved(factory.createAssociationEnd(null), clazz.createAttribute3(), null);
        assertTrue(onPreQualifierAttributeRemoved);
    }
    
    public void testFirePreQueryModified()
    {
        disp.firePreQueryModified(clazz.createOperation3(), false, null);
        assertTrue(onPreQueryModified);
    }
    
    public void testFirePreRangeAdded()
    {
        disp.firePreRangeAdded(clazz.createAttribute3(), factory.createMultiplicity(null), 
            factory.createMultiplicityRange(null), null);
        assertTrue(onPreRangeAdded);
    }
    
    public void testFirePreRangeRemoved()
    {
        disp.firePreRangeRemoved(clazz.createAttribute3(), factory.createMultiplicity(null), 
            factory.createMultiplicityRange(null), null);
        assertTrue(onPreRangeRemoved);
    }
    
    public void testFirePreStaticModified()
    {
        disp.firePreStaticModified(clazz.createOperation3(), false, null);
        assertTrue(onPreStaticModified);
    }
    
    public void testFirePreStrictFPModified()
    {
        disp.firePreStrictFPModified(clazz.createOperation3(), false, null);
        assertTrue(onPreStrictFPModified);
    }
    
    public void testFirePreTemplateParameterAdded()
    {
        disp.firePreTemplateParameterAdded(clazz, createClass("T"), null);
        assertTrue(onPreTemplateParameterAdded);
    }
    
    public void testFirePreTemplateParameterRemoved()
    {
        disp.firePreTemplateParameterRemoved(clazz, createClass("T"), null);
        assertTrue(onPreTemplateParameterRemoved);
    }
    
    public void testFirePreTransform()
    {
        disp.firePreTransform(clazz, "interface", null);
        assertTrue(onPreTransform);
    }
    
    public void testFirePreTransientModified()
    {
        disp.firePreTransientModified(clazz.createAttribute3(), false, null);
        assertTrue(onPreTransientModified);
    }
    
    public void testFirePreTypeModified()
    {
        disp.firePreTypeModified(clazz.createAttribute3(), clazz, null);
        assertTrue(onPreTypeModified);
    }
    
    public void testFirePreUpperModified()
    {
        disp.firePreUpperModified(clazz.createAttribute3(), factory.createMultiplicity(null), 
            factory.createMultiplicityRange(null), "yang", null);
        assertTrue(onPreUpperModified);
    }
    
    public void testFirePreVolatileModified()
    {
        disp.firePreVolatileModified(clazz.createAttribute3(), false, null);
        assertTrue(onPreVolatileModified);
    }
    
    public void testFirePrimaryKeyModified()
    {
        disp.firePrimaryKeyModified(clazz.createAttribute3(), null);
        assertTrue(onPrimaryKeyModified);
    }
    
    public void testFireQualifierAttributeAdded()
    {
        disp.fireQualifierAttributeAdded(factory.createAssociationEnd(null), 
            clazz.createAttribute3(), null);
        assertTrue(onQualifierAttributeAdded);
    }
    
    public void testFireQualifierAttributeRemoved()
    {
        disp.fireQualifierAttributeRemoved(factory.createAssociationEnd(null), 
            clazz.createAttribute3(), null);
        assertTrue(onQualifierAttributeRemoved);
    }
    
    public void testFireQueryModified()
    {
        disp.fireQueryModified(clazz.createOperation3(), null);
        assertTrue(onQueryModified);
    }
    
    public void testFireRaisedExceptionAdded()
    {
        disp.fireRaisedExceptionAdded(clazz.createOperation3(), clazz, null);
        assertTrue(onRaisedExceptionAdded);
    }
    
    public void testFireRaisedExceptionPreAdded()
    {
        disp.fireRaisedExceptionPreAdded(clazz.createOperation3(), clazz, null);
        assertTrue(onRaisedExceptionPreAdded);
    }
    
    public void testFireRaisedExceptionPreRemoved()
    {
        disp.fireRaisedExceptionPreRemoved(clazz.createOperation3(), clazz, null);
        assertTrue(onRaisedExceptionPreRemoved);
    }
    
    public void testFireRaisedExceptionRemoved()
    {
        disp.fireRaisedExceptionRemoved(clazz.createOperation3(), clazz, null);
        assertTrue(onRaisedExceptionRemoved);
    }
    
    public void testFireRangeAdded()
    {
        disp.fireRangeAdded(clazz.createAttribute3(), 
            factory.createMultiplicity(null), 
            factory.createMultiplicityRange(null), null);
        assertTrue(onRangeAdded);
    }
    
    public void testFireRangeRemoved()
    {
        disp.fireRangeRemoved(clazz.createAttribute3(), 
            factory.createMultiplicity(null), 
            factory.createMultiplicityRange(null), null);
        assertTrue(onRangeRemoved);
    }
    
    public void testFireStaticModified()
    {
        disp.fireStaticModified(clazz.createOperation3(), null);
        assertTrue(onStaticModified);
    }
    
    public void testFireStrictFPModified()
    {
        disp.fireStrictFPModified(clazz.createOperation3(), null);
        assertTrue(onStrictFPModified);
    }
    
    public void testRevokeStructuralFeatureSink()
    {
        disp.revokeStructuralFeatureSink(this);
        disp.fireVolatileModified(clazz.createAttribute3(), null);
        assertFalse(onVolatileModified);
    }
    
    public void testFireTemplateParameterAdded()
    {
        disp.fireTemplateParameterAdded(clazz, createClass("T"), null);
        assertTrue(onTemplateParameterAdded);
    }
    
    public void testFireTemplateParameterRemoved()
    {
        disp.fireTemplateParameterRemoved(clazz, createClass("T"), null);
        assertTrue(onTemplateParameterRemoved);
    }
    
    public void testRevokeTransformSink()
    {
        disp.revokeTransformSink(this);
        disp.fireTransformed(clazz, null);
        assertFalse(onTransformed);
    }
    
    public void testFireTransformed()
    {
        disp.fireTransformed(clazz, null);
        assertTrue(onTransformed);
    }
    
    public void testFireTransientModified()
    {
        disp.fireTransientModified(clazz.createAttribute3(), null);
        assertTrue(onTransientModified);
    }
    
    public void testFireTypeModified()
    {
        disp.fireTypeModified(clazz.createAttribute3(), null);
        assertTrue(onTypeModified);
    }
    
    public void testRevokeTypedElementSink()
    {
        disp.revokeTypedElementSink(this);
        disp.fireTypeModified(clazz.createAttribute3(), null);
        assertFalse(onTypeModified);
    }
    
    public void testFireUpperModified()
    {
        disp.fireUpperModified(clazz.createAttribute3(), 
            factory.createMultiplicity(null), 
            factory.createMultiplicityRange(null), null);
        assertTrue(onUpperModified);
    }
    
    public void testFireVolatileModified()
    {
        disp.fireVolatileModified(clazz.createAttribute3(), null);
        assertTrue(onVolatileModified);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onConcurrencyPreModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConcurrencyPreModified(IBehavioralFeature feature, int proposedValue, IResultCell cell)
    {
        onConcurrencyPreModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onConcurrencyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConcurrencyModified(IBehavioralFeature feature, IResultCell cell)
    {
        onConcurrencyModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreHandledSignalAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreHandledSignalAdded(IBehavioralFeature feature, ISignal proposedValue, IResultCell cell)
    {
        onPreHandledSignalAdded = true;
        assertNotNull(feature);
        assertNotNull(proposedValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onHandledSignalAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onHandledSignalAdded(IBehavioralFeature feature, IResultCell cell)
    {
        onHandledSignalAdded = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreHandledSignalRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreHandledSignalRemoved(IBehavioralFeature feature, ISignal proposedValue, IResultCell cell)
    {
        onPreHandledSignalRemoved = true;
        assertNotNull(feature);
        assertNotNull(proposedValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onHandledSignalRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onHandledSignalRemoved(IBehavioralFeature feature, IResultCell cell)
    {
        onHandledSignalRemoved = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreParameterAdded(IBehavioralFeature feature, IParameter parm, IResultCell cell)
    {
        onPreParameterAdded = true;
        assertNotNull(feature);
        assertNotNull(parm);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onParameterAdded(IBehavioralFeature feature, IParameter parm, IResultCell cell)
    {
        onParameterAdded = true;
        assertNotNull(feature);
        assertNotNull(parm);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreParameterRemoved(IBehavioralFeature feature, IParameter parm, IResultCell cell)
    {
        onPreParameterRemoved = true;
        assertNotNull(feature);
        assertNotNull(parm);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onParameterRemoved(IBehavioralFeature feature, IParameter parm, IResultCell cell)
    {
        onParameterRemoved = true;
        assertNotNull(feature);
        assertNotNull(parm);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreAbstractModified(IBehavioralFeature feature, boolean proposedValue, IResultCell cell)
    {
        onPreAbstractModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onAbstractModified(IBehavioralFeature feature, IResultCell cell)
    {
        onAbstractModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreStrictFPModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreStrictFPModified(IBehavioralFeature feature, boolean proposedValue, IResultCell cell)
    {
        onPreStrictFPModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onStrictFPModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onStrictFPModified(IBehavioralFeature feature, IResultCell cell)
    {
        onStrictFPModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeaturePreAdded(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        onFeaturePreAdded = true;
        assertNotNull(classifier);
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeatureAdded(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        onFeatureAdded = true;
        assertNotNull(classifier);
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEnumerationLiteralPreAdded(IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
        onEnumerationLiteralPreAdded = true;
        assertNotNull(classifier);
        assertNotNull(enumLit);
        assertNotNull(cell);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEnumerationLiteralAdded(IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
        onEnumerationLiteralAdded = true;
        assertNotNull(classifier);
        assertNotNull(enumLit);
        assertNotNull(cell);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeaturePreRemoved(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        onFeaturePreRemoved = true;
        assertNotNull(classifier);
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeatureRemoved(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        onFeatureRemoved = true;
        assertNotNull(classifier);
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeaturePreMoved(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        onFeaturePreMoved = true;
        assertNotNull(classifier);
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeatureMoved(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        onFeatureMoved = true;
        assertNotNull(classifier);
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreDuplicatedToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeaturePreDuplicatedToClassifier(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        onFeaturePreDuplicatedToClassifier = true;
        assertNotNull(classifier);
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureDuplicatedToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeatureDuplicatedToClassifier(IClassifier pOldClassifier, IFeature pOldFeature, IClassifier pNewClassifier, IFeature pNewFeature, IResultCell cell)
    {
        onFeatureDuplicatedToClassifier = true;
        assertNotNull(pOldClassifier);
        assertNotNull(pOldFeature);
        assertNotNull(pNewClassifier);
        assertNotNull(pNewFeature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreAbstractModified(IClassifier feature, boolean proposedValue, IResultCell cell)
    {
        onPreAbstractModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onAbstractModified(IClassifier feature, IResultCell cell)
    {
        onAbstractModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreLeafModified(IClassifier feature, boolean proposedValue, IResultCell cell)
    {
        onPreLeafModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onLeafModified(IClassifier feature, IResultCell cell)
    {
        onLeafModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTransientModified(IClassifier feature, boolean proposedValue, IResultCell cell)
    {
        onPreTransientModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTransientModified(IClassifier feature, IResultCell cell)
    {
        onTransientModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTemplateParameterAdded(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
    {
        onPreTemplateParameterAdded = true;
        assertNotNull(pClassifier);
        assertNotNull(pParam);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTemplateParameterAdded(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
    {
        onTemplateParameterAdded = true;
        assertNotNull(pClassifier);
        assertNotNull(pParam);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTemplateParameterRemoved(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
    {
        onPreTemplateParameterRemoved = true;
        assertNotNull(pClassifier);
        assertNotNull(pParam);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTemplateParameterRemoved(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
    {
        onTemplateParameterRemoved = true;
        assertNotNull(pClassifier);
        assertNotNull(pParam);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onPreStaticModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreStaticModified(IFeature feature, boolean proposedValue, IResultCell cell)
    {
        onPreStaticModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onStaticModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onStaticModified(IFeature feature, IResultCell cell)
    {
        onStaticModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onPreNativeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreNativeModified(IFeature feature, boolean proposedValue, IResultCell cell)
    {
        onPreNativeModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onNativeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onNativeModified(IFeature feature, IResultCell cell)
    {
        onNativeModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onPreChangeabilityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreChangeabilityModified(IStructuralFeature feature, int proposedValue, IResultCell cell)
    {
        onPreChangeabilityModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onChangeabilityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onChangeabilityModified(IStructuralFeature feature, IResultCell cell)
    {
        onChangeabilityModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onPreVolatileModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreVolatileModified(IStructuralFeature feature, boolean proposedValue, IResultCell cell)
    {
        onPreVolatileModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onVolatileModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onVolatileModified(IStructuralFeature feature, IResultCell cell)
    {
        onVolatileModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onPreTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTransientModified(IStructuralFeature feature, boolean proposedValue, IResultCell cell)
    {
        onPreTransientModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTransientModified(IStructuralFeature feature, IResultCell cell)
    {
        onTransientModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDefaultExpModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultExpModified(IParameter feature, IExpression proposedValue, IResultCell cell)
    {
        onPreDefaultExpModified = true;
        assertNotNull(feature);
        assertNotNull(proposedValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDefaultExpModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultExpModified(IParameter feature, IResultCell cell)
    {
        onDefaultExpModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDefaultExpBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultExpBodyModified(IParameter feature, String bodyValue, IResultCell cell)
    {
        onPreDefaultExpBodyModified = true;
        assertNotNull(feature);
        assertNotNull(bodyValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDefaultExpBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultExpBodyModified(IParameter feature, IResultCell cell)
    {
        onDefaultExpBodyModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDefaultExpLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultExpLanguageModified(IParameter feature, String language, IResultCell cell)
    {
        onPreDefaultExpLanguageModified = true;
        assertNotNull(feature);
        assertNotNull(language);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDefaultExpLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultExpLanguageModified(IParameter feature, IResultCell cell)
    {
        onDefaultExpLanguageModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDirectionModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDirectionModified(IParameter feature, int proposedValue, IResultCell cell)
    {
        onPreDirectionModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDirectionModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDirectionModified(IParameter feature, IResultCell cell)
    {
        onDirectionModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreMultiplicityModified(ITypedElement element, IMultiplicity proposedValue, IResultCell cell)
    {
        onPreMultiplicityModified = true;
        assertNotNull(element);
        assertNotNull(proposedValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onMultiplicityModified(ITypedElement element, IResultCell cell)
    {
        onMultiplicityModified = true;
        assertNotNull(element);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreTypeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTypeModified(ITypedElement element, IClassifier proposedValue, IResultCell cell)
    {
        onPreTypeModified = true;
        assertNotNull(element);
        assertNotNull(proposedValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onTypeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTypeModified(ITypedElement element, IResultCell cell)
    {
        onTypeModified = true;
        assertNotNull(element);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreLowerModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreLowerModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell)
    {
        onPreLowerModified = true;
        assertNotNull(element);
        assertNotNull(mult);
        assertNotNull(range);
        assertNotNull(proposedValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onLowerModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onLowerModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        onLowerModified = true;
        assertNotNull(element);
        assertNotNull(mult);
        assertNotNull(range);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreUpperModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreUpperModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell)
    {
        onPreUpperModified = true;
        assertNotNull(element);
        assertNotNull(mult);
        assertNotNull(range);
        assertNotNull(proposedValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onUpperModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onUpperModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        onUpperModified = true;
        assertNotNull(element);
        assertNotNull(mult);
        assertNotNull(range);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreRangeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRangeAdded(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        onPreRangeAdded = true;
        assertNotNull(element);
        assertNotNull(mult);
        assertNotNull(range);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onRangeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRangeAdded(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        onRangeAdded = true;
        assertNotNull(element);
        assertNotNull(mult);
        assertNotNull(range);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreRangeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRangeRemoved(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        onPreRangeRemoved = true;
        assertNotNull(element);
        assertNotNull(mult);
        assertNotNull(range);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onRangeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRangeRemoved(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        onRangeRemoved = true;
        assertNotNull(element);
        assertNotNull(mult);
        assertNotNull(range);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreOrderModified(ITypedElement element, IMultiplicity mult, boolean proposedValue, IResultCell cell)
    {
        onPreOrderModified = true;
        assertNotNull(element);
        assertNotNull(mult);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onOrderModified(ITypedElement element, IMultiplicity mult, IResultCell cell)
    {
        onOrderModified = true;
        assertNotNull(element);
        assertNotNull(mult);
        assertNotNull(cell);
    }

    /**
     * Fired when the collection type property is changed on the passed in
     * range.
     * @param element The type that owned the multilicity element
     * @param mult The multiplicity
     * @param range The multiplicity range that changed
     * @param cell The event result.
     */
    public void onCollectionTypeModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell )
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultPreModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultPreModified(IAttribute attr, IExpression proposedValue, IResultCell cell)
    {
        onDefaultPreModified = true;
        assertNotNull(attr);
        assertNotNull(proposedValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultModified(IAttribute attr, IResultCell cell)
    {
        onDefaultModified = true;
        assertNotNull(attr);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultBodyModified(IAttribute feature, String bodyValue, IResultCell cell)
    {
        onPreDefaultBodyModified = true;
        assertNotNull(feature);
        assertNotNull(bodyValue);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultBodyModified(IAttribute feature, IResultCell cell)
    {
        onDefaultBodyModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultLanguageModified(IAttribute feature, String language, IResultCell cell)
    {
        onPreDefaultLanguageModified = true;
        assertNotNull(feature);
        assertNotNull(language);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultLanguageModified(IAttribute feature, IResultCell cell)
    {
        onDefaultLanguageModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDerivedModified(IAttribute feature, boolean proposedValue, IResultCell cell)
    {
        onPreDerivedModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDerivedModified(IAttribute feature, IResultCell cell)
    {
        onDerivedModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPrePrimaryKeyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPrePrimaryKeyModified(IAttribute feature, boolean proposedValue, IResultCell cell)
    {
        onPrePrimaryKeyModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPrimaryKeyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPrimaryKeyModified(IAttribute feature, IResultCell cell)
    {
        onPrimaryKeyModified = true;
        assertNotNull(feature);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionPreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConditionPreAdded(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
    {
        onConditionPreAdded = true;
        assertNotNull(oper);
        assertNotNull(condition);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConditionAdded(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
    {
        onConditionAdded = true;
        assertNotNull(oper);
        assertNotNull(condition);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionPreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConditionPreRemoved(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
    {
        onConditionPreRemoved = true;
        assertNotNull(oper);
        assertNotNull(condition);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConditionRemoved(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
    {
        onConditionRemoved = true;
        assertNotNull(oper);
        assertNotNull(condition);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onPreQueryModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreQueryModified(IOperation oper, boolean proposedValue, IResultCell cell)
    {
        onPreQueryModified = true;
        assertNotNull(oper);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onQueryModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onQueryModified(IOperation oper, IResultCell cell)
    {
        onQueryModified = true;
        assertNotNull(oper);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRaisedExceptionPreAdded(IOperation oper, IClassifier pException, IResultCell cell)
    {
        onRaisedExceptionPreAdded = true;
        assertNotNull(oper);
        assertNotNull(pException);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRaisedExceptionAdded(IOperation oper, IClassifier pException, IResultCell cell)
    {
        onRaisedExceptionAdded = true;
        assertNotNull(oper);
        assertNotNull(pException);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRaisedExceptionPreRemoved(IOperation oper, IClassifier pException, IResultCell cell)
    {
        onRaisedExceptionPreRemoved = true;
        assertNotNull(oper);
        assertNotNull(pException);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRaisedExceptionRemoved(IOperation oper, IClassifier pException, IResultCell cell)
    {
        onRaisedExceptionRemoved = true;
        assertNotNull(oper);
        assertNotNull(pException);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink#onPreTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTransform(IClassifier classifier, String newForm, IResultCell cell)
    {
        onPreTransform = true;
        assertNotNull(classifier);
        assertNotNull(newForm);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink#onTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTransformed(IClassifier classifier, IResultCell cell)
    {
        onTransformed = true;
        assertNotNull(classifier);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndTransformEventsSink#onPreTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTransform(IAssociationEnd pEnd, String newForm, IResultCell cell)
    {
        onPreTransform = true;
        assertNotNull(pEnd);
        assertNotNull(newForm);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndTransformEventsSink#onTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTransformed(IAssociationEnd pEnd, IResultCell cell)
    {
        onTransformed = true;
        assertNotNull(pEnd);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink#onPreQualifierAttributeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreQualifierAttributeAdded(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
    {
        onPreQualifierAttributeAdded = true;
        assertNotNull(pEnd);
        assertNotNull(pAttr);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink#onQualifierAttributeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onQualifierAttributeAdded(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
    {
        onQualifierAttributeAdded = true;
        assertNotNull(pEnd);
        assertNotNull(pAttr);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink#onPreQualifierAttributeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreQualifierAttributeRemoved(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
    {
        onPreQualifierAttributeRemoved = true;
        assertNotNull(pEnd);
        assertNotNull(pAttr);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink#onQualifierAttributeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onQualifierAttributeRemoved(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
    {
        onQualifierAttributeRemoved = true;
        assertNotNull(pEnd);
        assertNotNull(pAttr);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink#onPreImpacted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreImpacted(IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell)
    {
        onPreImpacted = true;
        assertNotNull(classifier);
        assertNotNull(impacted);
        assertNotNull(cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink#onImpacted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onImpacted(IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell)
    {
        onImpacted = true;
        assertNotNull(classifier);
        assertNotNull(impacted);
        assertNotNull(cell);
    }

    public void onPreOperationPropertyModified( IOperation oper, int nKind, boolean proposedValue, IResultCell cell )
    {
        assertNotNull(oper);
        onPreOperationPropertyModified = true;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onOperationPropertyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onOperationPropertyModified(IOperation oper, int nKind, IResultCell cell)
    {
        assertNotNull(oper);
        onOperationPropertyModified = true;
    }
    
    private static boolean onConcurrencyPreModified = false;
    private static boolean onConcurrencyModified = false;
    private static boolean onPreHandledSignalAdded = false;
    private static boolean onHandledSignalAdded = false;
    private static boolean onPreHandledSignalRemoved = false;
    private static boolean onHandledSignalRemoved = false;
    private static boolean onPreParameterAdded = false;
    private static boolean onParameterAdded = false;
    private static boolean onPreParameterRemoved = false;
    private static boolean onParameterRemoved = false;
    private static boolean onPreAbstractModified = false;
    private static boolean onAbstractModified = false;
    private static boolean onPreStrictFPModified = false;
    private static boolean onStrictFPModified = false;
    private static boolean onChangeabilityModified = false;
    private static boolean onConditionAdded = false;
    private static boolean onConditionPreAdded = false;
    private static boolean onConditionPreRemoved = false;
    private static boolean onConditionRemoved = false;
    private static boolean onDefaultBodyModified = false;
    private static boolean onDefaultExpBodyModified = false;
    private static boolean onDefaultExpLanguageModified = false;
    private static boolean onDefaultExpModified = false;
    private static boolean onDefaultLanguageModified = false;
    private static boolean onDefaultModified = false;
    private static boolean onDefaultPreModified = false;
    private static boolean onDerivedModified = false;
    private static boolean onDirectionModified = false;
    private static boolean onFeatureAdded = false;
    private static boolean onEnumerationLiteralAdded = false;
    private static boolean onEnumerationLiteralPreAdded = false;
    private static boolean onFeatureDuplicatedToClassifier = false;
    private static boolean onFeatureMoved = false;
    private static boolean onFeaturePreAdded = false;
    private static boolean onFeaturePreDuplicatedToClassifier = false;
    private static boolean onFeaturePreMoved = false;
    private static boolean onFeaturePreRemoved = false;
    private static boolean onFeatureRemoved = false;
    private static boolean onImpacted = false;
    private static boolean onLeafModified = false;
    private static boolean onLowerModified = false;
    private static boolean onMultiplicityModified = false;
    private static boolean onNativeModified = false;
    private static boolean onOrderModified = false;
    private static boolean onPreChangeabilityModified = false;
    private static boolean onPreDefaultBodyModified = false;
    private static boolean onPreDefaultExpBodyModified = false;
    private static boolean onPreDefaultExpLanguageModified = false;
    private static boolean onPreDefaultExpModified = false;
    private static boolean onPreDefaultLanguageModified = false;
    private static boolean onPreDerivedModified = false;
    private static boolean onPreDirectionModified = false;
    private static boolean onPreImpacted = false;
    private static boolean onPreLeafModified = false;
    private static boolean onPreLowerModified = false;
    private static boolean onPreMultiplicityModified = false;
    private static boolean onPreNativeModified = false;
    private static boolean onPreOrderModified = false;
    private static boolean onPrePrimaryKeyModified = false;
    private static boolean onPreQualifierAttributeAdded = false;
    private static boolean onPreQualifierAttributeRemoved = false;
    private static boolean onPreQueryModified = false;
    private static boolean onPreRangeAdded = false;
    private static boolean onPreRangeRemoved = false;
    private static boolean onPreStaticModified = false;
    private static boolean onPreTemplateParameterAdded = false;
    private static boolean onPreTemplateParameterRemoved = false;
    private static boolean onPreTransform = false;
    private static boolean onPreTransientModified = false;
    private static boolean onPreTypeModified = false;
    private static boolean onPreUpperModified = false;
    private static boolean onPreVolatileModified = false;
    private static boolean onPrimaryKeyModified = false;
    private static boolean onQualifierAttributeAdded = false;
    private static boolean onQualifierAttributeRemoved = false;
    private static boolean onQueryModified = false;
    private static boolean onRaisedExceptionAdded = false;
    private static boolean onRaisedExceptionPreAdded = false;
    private static boolean onRaisedExceptionPreRemoved = false;
    private static boolean onRaisedExceptionRemoved = false;
    private static boolean onRangeAdded = false;
    private static boolean onRangeRemoved = false;
    private static boolean onStaticModified = false;
    private static boolean onTemplateParameterAdded = false;
    private static boolean onTemplateParameterRemoved = false;
    private static boolean onTransformed = false;
    private static boolean onTransientModified = false;
    private static boolean onTypeModified = false;
    private static boolean onUpperModified = false;
    private static boolean onVolatileModified = false;
    private static boolean onPreOperationPropertyModified = false;
    private static boolean onOperationPropertyModified = false;
}
