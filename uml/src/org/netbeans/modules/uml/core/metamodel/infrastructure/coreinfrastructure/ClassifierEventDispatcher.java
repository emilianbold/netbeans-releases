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

import java.util.Vector;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class ClassifierEventDispatcher extends EventDispatcher
									   implements IClassifierEventDispatcher
{
    private EventManager< IClassifierFeatureEventsSink >       m_ClassifierSink = null;
    private EventManager< IFeatureEventsSink >                 m_FeatureSink = null;
    private EventManager< IStructuralFeatureEventsSink >       m_StructFeatureSink = null;
    private EventManager< IBehavioralFeatureEventsSink >       m_BehavFeatureSink = null;
    private EventManager< IParameterEventsSink >               m_ParmSink = null;
    private EventManager< ITypedElementEventsSink >            m_TypeSink = null;
    private EventManager< IAttributeEventsSink >               m_AttrSink = null;
    private EventManager< IOperationEventsSink >               m_OperSink = null;
    private EventManager< IClassifierTransformEventsSink >     m_TransformSink = null;
    private EventManager< IAssociationEndTransformEventsSink > m_AssociationEndTransformSink = null;
    private EventManager< IAssociationEndEventsSink >          m_AssociationEndEventsSink = null;
    private EventManager< IAffectedElementEventsSink >         m_AffectedSink = null;

    public ClassifierEventDispatcher()
    {	
        m_ClassifierSink = new EventManager< IClassifierFeatureEventsSink > ();
        m_FeatureSink =     new EventManager< IFeatureEventsSink > ();
        m_StructFeatureSink =   new EventManager< IStructuralFeatureEventsSink > ();   
        m_BehavFeatureSink =    new EventManager< IBehavioralFeatureEventsSink > ();
        m_ParmSink =     new EventManager< IParameterEventsSink > ();         
        m_TypeSink =     new EventManager< ITypedElementEventsSink > ();           
        m_AttrSink =     new EventManager< IAttributeEventsSink > ();             
        m_OperSink =   new EventManager< IOperationEventsSink > ();
        m_TransformSink = new EventManager< IClassifierTransformEventsSink > ();
        m_AssociationEndTransformSink =   new EventManager< IAssociationEndTransformEventsSink > ();
        m_AssociationEndEventsSink =   new EventManager< IAssociationEndEventsSink > ();
        m_AffectedSink = new EventManager< IAffectedElementEventsSink > ();        
    }


    /**
     * Fired whenever an existing parameter is about to be removed from the behavioral
     * feature's list of parameters.
     *
     * @param feature[in] 
     * @param parm[in]
     * @param payload[in]
     */
    public boolean firePreParameterRemoved( IBehavioralFeature feature,
            IParameter parm,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,parm);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreParameterRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preParameterRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onPreParameterRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preParameterRemoved.setParameters(parms);
            m_BehavFeatureSink.notifyListenersWithQualifiedProceed(preParameterRemoved);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed; 
    }

    /**
     * Fired whenever an existing parameter was just removed from the behavioral
     * feature's list of parameters.
     *
     * @param feature[in] 
     * @param parm[in]
     */
    public void fireParameterRemoved(IBehavioralFeature feature,
            IParameter parm,
            IEventPayload payload)
    {
        if (validateEvent("ParameterRemoved", feature))
        {
            IResultCell cell = prepareResultCell( payload );            
            EventFunctor paramRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onParameterRemoved");
            
            Object[] parms = new Object[] { feature, parm, cell };
            paramRemoved.setParameters(parms);
            m_BehavFeatureSink.notifyListeners(paramRemoved);
        }
    }

    /**
     * Fired whenever the abstract flag on the behavioral feature is about to be
     * modified.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreAbstractModified(IBehavioralFeature feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));        
        Object var = prepareVariant(vect);

        if (validateEvent("PreAbstractModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preAbstractModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onPreAbstractModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preAbstractModified.setParameters(parms);
            m_BehavFeatureSink.notifyListenersWithQualifiedProceed(preAbstractModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;      
    }

    /**
     * Fired whenever the abstract flag on the behavioral feature has been modified.
     *
     * @param feature[in] 
     * @param payload[in]
     */
    public void fireAbstractModified(IBehavioralFeature feature,
            IEventPayload payload)
    {
        if (validateEvent("AbstractModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );            
            EventFunctor abstractModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onAbstractModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;            
            abstractModified.setParameters(parms);
            m_BehavFeatureSink.notifyListeners(abstractModified);
        }
    }


    public boolean firePreStrictFPModified(IBehavioralFeature feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));
        
        Object var = prepareVariant(vect);

        if (validateEvent("PreStrictFPModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preStrictFPModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onPreStrictFPModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preStrictFPModified.setParameters(parms);
            m_BehavFeatureSink.notifyListenersWithQualifiedProceed(preStrictFPModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;      
    }


    public void fireStrictFPModified(IBehavioralFeature feature,
            IEventPayload payload)
    {
        if (validateEvent("StrictFPModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );            
            EventFunctor strictFPModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onStrictFPModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;            
            strictFPModified.setParameters(parms);
            m_BehavFeatureSink.notifyListeners(strictFPModified);
        }
    }

    /**
     * Fired whenever the default expression for the parameter
     * is about to change.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreDefaultExpModified(IParameter feature,
            IExpression proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,proposedValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreDefaultExpModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preDefaultExpModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink", 
                        "onPreDefaultExpModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preDefaultExpModified.setParameters(parms);
            m_ParmSink.notifyListenersWithQualifiedProceed(preDefaultExpModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed; 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireDefaultExpModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireDefaultExpModified(IParameter feature,
            IEventPayload payload)
    {
        if (validateEvent("DefaultExpModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor defaultExpModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink", 
                        "onDefaultExpModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;            
            defaultExpModified.setParameters(parms);
            m_ParmSink.notifyListeners(defaultExpModified);
        }
    }

     /**
      * Fired whenever the default expression's body property for the
      * parameter is about to change.
      *
      * @param feature[in] 
      * @param bodyValue[in]
      * @param payload[in]
      */
    public boolean firePreDefaultExpBodyModified(IParameter feature,
            String bodyValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,bodyValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreDefaultExpBodyModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preDefaultExpBodyModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink", 
                        "onPreDefaultExpBodyModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preDefaultExpBodyModified.setParameters(parms);
            m_ParmSink.notifyListenersWithQualifiedProceed(preDefaultExpBodyModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;  
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireDefaultExpBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireDefaultExpBodyModified(IParameter feature,
            IEventPayload payload)
    {
        if (validateEvent("DefaultExpBodyModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor defaultExpBodyModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink", 
                        "onDefaultExpBodyModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            defaultExpBodyModified.setParameters(parms);
            m_ParmSink.notifyListeners(defaultExpBodyModified);
        }
    }

    /**
     * Fired whenever the default expression's language property for the
     * parameter is about to change.
     *
     * @param feature[in] 
     * @param language[in]
     * @param payload[in]
     */
    public boolean firePreDefaultExpLanguageModified(
            IParameter feature,
            String language,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,language);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreDefaultExpLanguageModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preDefaultExpLanguageModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink", 
                        "onPreDefaultExpLanguageModified");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            preDefaultExpLanguageModified.setParameters(params);
            m_ParmSink.notifyListenersWithQualifiedProceed(preDefaultExpLanguageModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired whenever the default expression's language property for the
     * parameter has changed.
     *
     * @param feature[in] 
     * @param payload[in]
     */
    public void fireDefaultExpLanguageModified(
            IParameter feature,
            IEventPayload payload)
    {
        if (validateEvent("DefaultExpLanguageModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor defaultExpLanguageModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink", 
                        "onDefaultExpLanguageModified");
            
            Object[] params = new Object[2];
            params[0] = feature;
            params[1] = cell;
            defaultExpLanguageModified.setParameters(params);
            m_ParmSink.notifyListeners(defaultExpLanguageModified);
        }
    }

    /**
     * Fired whenever the direction value of the parameter is about to change.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreDirectionModified(
            IParameter feature,
            int proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Integer(proposedValue));        
        Object var = prepareVariant(vect);

        if (validateEvent("PreDirectionModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preDirectionModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink", 
                        "onPreDirectionModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preDirectionModified.setParameters(parms);
            m_ParmSink.notifyListenersWithQualifiedProceed(preDirectionModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed; 
    }

    /**
     * Fired whenever the direction value of the parameter has changed
     *
     * @param feature[in] 
     * @param payload[in]
     */
    public void fireDirectionModified(IParameter feature, IEventPayload payload)
    {
        if (validateEvent("DirectionModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor directionModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink", 
                        "onDirectionModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            directionModified.setParameters(parms);
            m_ParmSink.notifyListeners(directionModified);
        }
    }
    /**
     * Fired whenever the Multiplicity object on a particular element is about
     * to be modified.
     *
     * @param element[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreMultiplicityModified(
            ITypedElement element,
            IMultiplicity proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,proposedValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreMultiplicityModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preMultiplicityModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onPreMultiplicityModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preMultiplicityModified.setParameters(parms);
            m_TypeSink.notifyListenersWithQualifiedProceed(preMultiplicityModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;  
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireMultiplicityModified(
            ITypedElement element,
            IEventPayload payload)
    {
        if (validateEvent("MultiplicityModified", element))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor multiplicityModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onMultiplicityModified");
            
            Object[] parms = new Object[2];
            parms[0] = element;
            parms[1] = cell;
            multiplicityModified.setParameters(parms);
            m_TypeSink.notifyListeners(multiplicityModified);
        }
    }
    
    /**
     * Fired whenever the type on a particular element is about to be modifed.
     *
     * @param element[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreTypeModified(
            ITypedElement element,
            IClassifier proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,proposedValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreTypeModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preTypeModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onPreTypeModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preTypeModified.setParameters(parms);
            m_TypeSink.notifyListenersWithQualifiedProceed(preTypeModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;  
    }

    /**
     * Fired whenever the type flag on a particular element was just modified.
     *
     * @param element[in] 
     * @param payload[in]
     */
    public void fireTypeModified(ITypedElement element, IEventPayload payload)
    {
        if (validateEvent("TypeModified", element))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor typeModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onTypeModified");
            
            Object[] parms = new Object[2];
            parms[0] = element;
            parms[1] = cell;            
            typeModified.setParameters(parms);
            m_TypeSink.notifyListeners(typeModified);
        }

    }

     /**
     * Fired whenever the lower property on the passed-in range is about
     * to be modified.
     *
     * @param element[in] 
     * @param mult[in]
     * @param range[in]
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreLowerModified(
            ITypedElement element,
            IMultiplicity mult,
            IMultiplicityRange range,
            String proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
        vect.add(2,range);
        vect.add(3,proposedValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreLowerModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preLowerModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onPreLowerModified");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            preLowerModified.setParameters(params);
            m_TypeSink.notifyListenersWithQualifiedProceed(preLowerModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }
    /**
     * Fired whenever the lower property on the passed-in range was modified.
     *
     * @param element[in] 
     * @param mult[in]
     * @param range[in]
     */
    public void fireLowerModified(
            ITypedElement element,
            IMultiplicity mult,
            IMultiplicityRange range,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
        vect.add(2,range);
       
        Object var = prepareVariant(vect);

        if (validateEvent("LowerModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor lowerModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onLowerModified");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            lowerModified.setParameters(params);
            m_TypeSink.notifyListeners(lowerModified);        
        }
    }
    
    /**
     * Fired whenever the lower property on the passed-in range was modified.
     *
     * @param element 
     * @param mult 
     * @param range 
     * @param payload 
     */
    public void fireCollectionTypeModified(
            ITypedElement element,
            IMultiplicity mult,
            IMultiplicityRange range,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
        vect.add(2,range);
       
        Object var = prepareVariant(vect);

        if (validateEvent("CollectionTypeModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor lowerModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onCollectionTypeModified");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            lowerModified.setParameters(params);
            m_TypeSink.notifyListeners(lowerModified);        
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#firePreUpperModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreUpperModified(
            ITypedElement element,
            IMultiplicity mult,
            IMultiplicityRange range,
            String proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
        vect.add(2,range);
        vect.add(3,proposedValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreUpperModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preUpperModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onPreUpperModified");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            preUpperModified.setParameters(params);
            m_TypeSink.notifyListenersWithQualifiedProceed(preUpperModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired when the upper property on the passed-in range was modified.
     *
     * @param element[in] 
     * @param mult[in]
     * @param range[in]
     */
    public void fireUpperModified(
            ITypedElement element,
            IMultiplicity mult,
            IMultiplicityRange range,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
        vect.add(2,range);
       
        Object var = prepareVariant(vect);

        if (validateEvent("UpperModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor upperModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onUpperModified");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            upperModified.setParameters(params);
            m_TypeSink.notifyListeners(upperModified);        
        }
    }

    /**
     * Fired when a new range is about to be added to the passed-in
     * multiplicity.
     *
     * @param element[in] 
     * @param mult[in]
     * @param range[in]
     * @param payload[in]
     */
    public boolean firePreRangeAdded(
            ITypedElement element,
            IMultiplicity mult,
            IMultiplicityRange range,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
        vect.add(2,range);
        Object var = prepareVariant(vect);

        if (validateEvent("PreRangeAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preRangeAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onPreRangeAdded");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            preRangeAdded.setParameters(params);
            m_TypeSink.notifyListenersWithQualifiedProceed(preRangeAdded);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired when a new range is added to the passed-in multiplicity.
     *
     * @param element[in] 
     * @param mult[in]
     * @param range[in]
     * @param payload[in]
     */
    public void fireRangeAdded(
            ITypedElement element,
            IMultiplicity mult,
            IMultiplicityRange range,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
        vect.add(2,range);
       
        Object var = prepareVariant(vect);

        if (validateEvent("RangeAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor rangeAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onRangeAdded");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            rangeAdded.setParameters(params);
            m_TypeSink.notifyListeners(rangeAdded);        
        }
    }

    /**
     * Fired when an existing range is about to be removed from the passed-in
     * multiplicity.
     *
     * @param element[in] 
     * @param mult[in]
     * @param range[in]
     * @param payload[in]
     */
    public boolean firePreRangeRemoved(
            ITypedElement element,
            IMultiplicity mult,
            IMultiplicityRange range,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
        vect.add(2,range);
        Object var = prepareVariant(vect);

        if (validateEvent("PreRangeRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preRangeRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onPreRangeRemoved");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            preRangeRemoved.setParameters(params);
            m_TypeSink.notifyListenersWithQualifiedProceed(preRangeRemoved);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired when an existing range is removed from the passed-in multiplicity.
     *
     * @param element[in] 
     * @param mult[in]
     * @param range[in]
     * @param payload[in]
     */
    public void fireRangeRemoved(
            ITypedElement element,
            IMultiplicity mult,
            IMultiplicityRange range,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
        vect.add(2,range);
       
        Object var = prepareVariant(vect);

        if (validateEvent("RangeRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor rangeRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onRangeRemoved");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            rangeRemoved.setParameters(params);
            m_TypeSink.notifyListeners(rangeRemoved);        
        }
    }

    /**
     * Fired when the order property is about to be changed on the passed-in
     * multiplicity.
     *
     * @param element[in] 
     * @param mult[in]
     * @param proposedValue
     * @param range[in]
     * @param payload[in]
     */
    public boolean firePreOrderModified(
            ITypedElement element,
            IMultiplicity mult,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
        vect.add(2,new Boolean(proposedValue));
        Object var = prepareVariant(vect);

        if (validateEvent("PreOrderModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preOrderModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onPreOrderModified");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            preOrderModified.setParameters(params);
            m_TypeSink.notifyListenersWithQualifiedProceed(preOrderModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireOrderModified(
            ITypedElement element,
            IMultiplicity mult,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,element);
        vect.add(1,mult);   
       
        Object var = prepareVariant(vect);

        if (validateEvent("OrderModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor m_OrderModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink", 
                        "onOrderModified");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            m_OrderModified.setParameters(params);
            m_TypeSink.notifyListeners(m_OrderModified);        
        }
    }


    public boolean fireDefaultPreModified(
            IAttribute attr,
            IExpression proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,attr);
        vect.add(1,proposedValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("DefaultPreModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor defaultPreModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink", 
                        "onDefaultPreModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            defaultPreModified.setParameters(parms);
            m_AttrSink.notifyListenersWithQualifiedProceed(defaultPreModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;  
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireDefaultModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireDefaultModified(IAttribute attr, IEventPayload payload)
    {
        if (validateEvent("DefaultModified", attr))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor defaultModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink", 
                        "onDefaultModified");
            
            Object[] parms = new Object[2];
            parms[0] = attr;
            parms[1] = cell;            
            defaultModified.setParameters(parms);
            
            m_AttrSink.notifyListeners(defaultModified);
        }
    }

    /**
     * Fired whenever the default expression's body property for the attribute
     * is about to change.
     *
     * @param attr[in] 
     * @param proposedValue
     * @param payload[in]
     */
    public boolean firePreDefaultBodyModified(
            IAttribute feature,
            String bodyValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,bodyValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreDefaultBodyModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preDefaultBodyModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink", 
                        "onPreDefaultBodyModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preDefaultBodyModified.setParameters(parms);
            m_AttrSink.notifyListenersWithQualifiedProceed(preDefaultBodyModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;  
    }

    /**
     * Fired whenever the default expression's body property for the
     * attribue has changed.
     *
     * @param attr[in] 
     * @param payload[in]
     */
    public void fireDefaultBodyModified(
            IAttribute feature,
            IEventPayload payload)
    {
        if (validateEvent("DefaultBodyModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor defaultBodyModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink", 
                        "onDefaultBodyModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            defaultBodyModified.setParameters(parms);
            m_AttrSink.notifyListeners(defaultBodyModified);
        }
    }

    /**
     * Fired whenever the default expression's language property for the
     * attribute is about to change.
     *
     * @param attr[in] 
     * @param proposedValue
     * @param payload[in]
     */
    public boolean firePreDefaultLanguageModified(
            IAttribute feature,
            String language,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,language);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreDefaultLanguageModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preDefaultLanguageModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink", 
                        "onPreDefaultLanguageModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preDefaultLanguageModified.setParameters(parms);
            m_AttrSink.notifyListenersWithQualifiedProceed(preDefaultLanguageModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;  
    }

    /**
     * Fired whenever the default expression's language property for the
     * attribute has changed.
     *
     * @param attr[in] 
     * @param payload[in]
     */
    public void fireDefaultLanguageModified(
            IAttribute feature,
            IEventPayload payload)
    {
        if (validateEvent("DefaultLanguageModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor defaultLanguageModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink", 
                        "onDefaultLanguageModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            defaultLanguageModified.setParameters(parms);
            m_AttrSink.notifyListeners(defaultLanguageModified);
        }
    }

    /**
     * Fired whenever the attributes derived property is about to change.
     *
     * @param attr[in] 
     * @param proposedValue
     * @param payload[in]
     */
    public boolean firePreDerivedModified(
            IAttribute feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));        
        Object var = prepareVariant(vect);

        if (validateEvent("PreDerivedModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor m_PreDerivedModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink", 
                        "onPreDerivedModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            m_PreDerivedModified.setParameters(parms);
            m_AttrSink.notifyListenersWithQualifiedProceed(m_PreDerivedModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;  
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireDerivedModified(IAttribute feature, IEventPayload payload)
    {
        if (validateEvent("DerivedModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor m_DerivedModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink", 
                        "onDerivedModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            m_DerivedModified.setParameters(parms);
            m_AttrSink.notifyListeners(m_DerivedModified);
        }
    }

    /**
     * Fired whenever the attributes primary key is about to change.
     *
     * @param attr[in] 
     * @param proposedValue
     * @param payload[in]
     */
    public boolean firePrePrimaryKeyModified(
            IAttribute feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));        
        Object var = prepareVariant(vect);

        if (validateEvent("PrePrimaryKeyModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor prePrimaryKeyModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink", 
                        "onPrePrimaryKeyModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            prePrimaryKeyModified.setParameters(parms);
            m_AttrSink.notifyListenersWithQualifiedProceed(prePrimaryKeyModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;    
    }

    /**
     * Fired whenever the attributes primary key has changed.
     *
     * @param attr[in] 
     * @param payload[in]
     */
    public void firePrimaryKeyModified(
            IAttribute feature,
            IEventPayload payload)
    {
        if (validateEvent("PrimaryKeyModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor primaryKeyModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink", 
                        "onPrimaryKeyModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            primaryKeyModified.setParameters(parms);
            m_AttrSink.notifyListeners(primaryKeyModified);
        }
    }

    /**
     * Fired whenever a pre- or post-condition is about to be added to an operation.
     *
     * @param oper[in] 
     * @param condition[in]
     * @param isPreCondition
     * @param payload[in]
     */
    public boolean fireConditionPreAdded(
            IOperation oper,
            IConstraint condition,
            boolean isPreCondition,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,condition);
        vect.add(2,new Boolean(isPreCondition));
        Object var = prepareVariant(vect);

        if (validateEvent("ConditionPreAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor conditionPreAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onConditionPreAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            conditionPreAdded.setParameters(parms);
            m_OperSink.notifyListenersWithQualifiedProceed(conditionPreAdded);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired whenever a pre- or post- condition has been added to an operation.
     *
     * @param oper[in] 
     * @param condition[in]
     * @param isPreCondition[in]
     */
    public void fireConditionAdded(
            IOperation oper,
            IConstraint condition,
            boolean isPreCondition,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,condition);
        vect.add(2,new Boolean(isPreCondition));
        Object var = prepareVariant(vect);

        if (validateEvent("ConditionAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor conditionAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onConditionAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            conditionAdded.setParameters(parms);
            m_OperSink.notifyListeners(conditionAdded);
        }
    }

    /**
     * Fired whenever a pre- or post-condition is about to be removed from an
     * operation.
     *
     * @param oper[in] 
     * @param condition[in]
     * @param isPreCondition
     * @param payload[in]
     */
    public boolean fireConditionPreRemoved(
            IOperation oper,
            IConstraint condition,
            boolean isPreCondition,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,condition);
        vect.add(2,new Boolean(isPreCondition));
        Object var = prepareVariant(vect);

        if (validateEvent("ConditionPreRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor conditionPreRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onConditionPreRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            conditionPreRemoved.setParameters(parms);
            m_OperSink.notifyListenersWithQualifiedProceed(conditionPreRemoved);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired whenever a pre- or post-condition is about to be removed from
     * an operation.
     *
     * @param oper[in] 
     * @param condition[in]
     * @param isPreCondition
     */
    public void fireConditionRemoved(
            IOperation oper,
            IConstraint condition,
            boolean isPreCondition,
            IEventPayload payload)
    {
       
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,condition);
        vect.add(2,new Boolean(isPreCondition));
        Object var = prepareVariant(vect);

        if (validateEvent("ConditionRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor conditionRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onConditionRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            conditionRemoved.setParameters(parms);
            m_OperSink.notifyListeners(conditionRemoved);
        }
    }

    /**
     * Fired whenever the query flag on an operation is about to be modified.
     *
     * @param oper[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreQueryModified(
            IOperation oper,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,new Boolean(proposedValue));
        Object var = prepareVariant(vect);

        if (validateEvent("PreQueryModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preQueryModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onPreQueryModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preQueryModified.setParameters(parms);
            m_OperSink.notifyListenersWithQualifiedProceed(preQueryModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired whenever the query flag on an operatoin has been modified.
     *
     * @param oper[in] 
     * @param payload[in]
     */
    public void fireQueryModified(IOperation oper, IEventPayload payload)
    {
        if (validateEvent("QueryModified", oper))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor queryModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onQueryModified");
            
            Object[] parms = new Object[2];
            parms[0] = oper;
            parms[1] = cell;
            queryModified.setParameters(parms);
            m_OperSink.notifyListeners(queryModified);
        }
    }

    /**
     * Description
     *
     * @param oper[in] 
     * @param pExceptoin[in]
     * @param payload[in]
     */
    public boolean fireRaisedExceptionPreAdded(
            IOperation oper,
            IClassifier pException,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,pException);
        Object var = prepareVariant(vect);

        if (validateEvent("RaisedExceptionPreAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor raisedExceptionPreAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onRaisedExceptionPreAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            raisedExceptionPreAdded.setParameters(parms);
            m_OperSink.notifyListenersWithQualifiedProceed(raisedExceptionPreAdded);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;   
    }

    /**
     * Description.
     *
     * @param oper[in] 
     * @param pException[in]
     * @param payload[in]
     */
    public void fireRaisedExceptionAdded(
            IOperation oper,
            IClassifier pException,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,pException);
        Object var = prepareVariant(vect);

        if (validateEvent("RaisedExceptionAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor raisedExceptionAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onRaisedExceptionAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            raisedExceptionAdded.setParameters(parms);
            m_OperSink.notifyListeners(raisedExceptionAdded);
        }
    }

    /**
     * Descriptoin
     *
     * @param oper[in] 
     * @param pException[in]
     * @param payload[in]
     */
    public boolean fireRaisedExceptionPreRemoved(
            IOperation oper,
            IClassifier pException,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,pException);
        Object var = prepareVariant(vect);

        if (validateEvent("RaisedExceptionPreRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor raisedExceptionPreRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onRaisedExceptionPreRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            raisedExceptionPreRemoved.setParameters(parms);
            m_OperSink.notifyListenersWithQualifiedProceed(raisedExceptionPreRemoved);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;  
    }


    /**
     * Description
     *
     * @param oper[in] 
     * @param pException[in]
     */
    public void fireRaisedExceptionRemoved(
            IOperation oper,
            IClassifier pException,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,pException);
        Object var = prepareVariant(vect);

        if (validateEvent("RaisedExceptionRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor raisedExceptionRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onRaisedExceptionRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            raisedExceptionRemoved.setParameters(parms);
            m_OperSink.notifyListeners(raisedExceptionRemoved);
        }
    }

    /**
     *
     * Fired whenever the name of a Classifier is about to change
     *
     * @param classifier[in]   The Classifier in question
     * @param impacted[in]     The collection of elements potentially impacted by the change
     * @param payload[in]      The event payload
     */
    public boolean firePreImpacted(
            IClassifier classifier,
            ETList<IVersionableElement> impacted,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,impacted);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreImpacted", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preImpacted = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink", 
                        "onPreImpacted");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            preImpacted.setParameters(params);
            m_AffectedSink.notifyListenersWithQualifiedProceed(preImpacted);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     *
     * Fired after the Classifier has changed. Most likely due to a name change.
     *
     * @param classifier[in]      The affected Classifier
     * @param impacted[in]        The collection of impacted elements
     * @param payload[in]         The payload
     */
    public void fireImpacted(
            IClassifier classifier,
            ETList<IVersionableElement> impacted,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,impacted);        
        Object var = prepareVariant(vect);

        if (validateEvent("Impacted", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor impactedFunctor = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink", 
                        "onImpacted");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            impactedFunctor.setParameters(params);
            m_AffectedSink.notifyListeners(impactedFunctor);
        }
    }


    public boolean firePreQualifierAttributeAdded(
            IAssociationEnd pEnd,
            IAttribute pAttr,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,pEnd);
        vect.add(1,pAttr);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreQualifierAttributeAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preQualifierAttributeAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink", 
                        "onPreQualifierAttributeAdded");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            preQualifierAttributeAdded.setParameters(params);
            m_AssociationEndEventsSink.notifyListenersWithQualifiedProceed(preQualifierAttributeAdded);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    public void fireQualifierAttributeAdded(
            IAssociationEnd pEnd,
            IAttribute pAttr,
            IEventPayload payload)
    {

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,pEnd);
        vect.add(1,pAttr);        
        Object var = prepareVariant(vect);

        if (validateEvent("QualifierAttributeAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor qualifierAttributeAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink", 
                        "onQualifierAttributeAdded");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            qualifierAttributeAdded.setParameters(params);
            m_AssociationEndEventsSink.notifyListeners(qualifierAttributeAdded);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#firePreQualifierAttributeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreQualifierAttributeRemoved(
            IAssociationEnd pEnd,
            IAttribute pAttr,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,pEnd);
        vect.add(1,pAttr);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreQualifierAttributeRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preQualifierAttributeRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink", 
                        "onPreQualifierAttributeRemoved");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            preQualifierAttributeRemoved.setParameters(params);
            m_AssociationEndEventsSink.notifyListenersWithQualifiedProceed(preQualifierAttributeRemoved);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireQualifierAttributeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireQualifierAttributeRemoved(
            IAssociationEnd pEnd,
            IAttribute pAttr,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,pEnd);
        vect.add(1,pAttr);        
        Object var = prepareVariant(vect);

        if (validateEvent("QualifierAttributeRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor qualifierAttributeRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink", 
                        "onQualifierAttributeRemoved");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            qualifierAttributeRemoved.setParameters(params);
            m_AssociationEndEventsSink.notifyListeners(qualifierAttributeRemoved);
        }
    }

    /**
     *
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param handler[in] The actual sink that will receive notifications
     */
    public void registerForClassifierFeatureEvents(IClassifierFeatureEventsSink handler)
    {
        m_ClassifierSink.addListener(handler,null);		
    }

    /**
     * Removes a listener from the current list.
     *
     * @param handler[in] 
     */
    public void revokeClassifierFeatureSink(IClassifierFeatureEventsSink handler)
    {   
        m_ClassifierSink.removeListener(handler);
    }

    /**
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param sink[in] The actual sink that will recieve notifications
     */ 
    public void registerForFeatureEvents(IFeatureEventsSink handler)
    {
        m_FeatureSink.addListener(handler,null);
    }

    /**
     * Removes a listener from the current list.
     *
     * @param handler[in] IFeatureEventsSink.
     */
    public void revokeFeatureSink(IFeatureEventsSink handler)
    {
        m_FeatureSink.removeListener(handler);
    }

    /**
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param sink[in] The actual sink that will receive notifications
     */
    public void registerForStructuralFeatureEvents(IStructuralFeatureEventsSink handler)
    {      
        m_StructFeatureSink.addListener(handler,null);
    }

    /**
     * Removes a listener from the current list.
     *
     * @param handler[in] IStructuralFeatureEventsSink.
     */
    public void revokeStructuralFeatureSink(IStructuralFeatureEventsSink handler)
    {
        m_StructFeatureSink.removeListener(handler);
    }

    /**
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param sink[in] The actual sink that will recieve notifications
     */
    public void registerForBehavioralFeatureEvents(IBehavioralFeatureEventsSink handler)
    {      
        m_BehavFeatureSink.addListener(handler,null);
    }

    /**
     * Removes a listener from the current list.
     *
     * @param handler[in] IBehavioralFeatureEventsSink.
     */
    public void revokeBehavioralFeatureSink(IBehavioralFeatureEventsSink handler)
    {      
        m_BehavFeatureSink.removeListener(handler);
    }

    /**
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param sink[in] The actual sink that will receive notifications
     */
    public void registerForParameterEvents(IParameterEventsSink handler)
    {
        m_ParmSink.addListener(handler,null);
    }

    /**
     * Removes a listener from the current list.
     *
     * @param handler[in] IParameterEventsSink.
     */
    public void revokeParameterSink(IParameterEventsSink handler)
    {      
        m_ParmSink.removeListener(handler);
    }

    /**
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param sink[in] The actual sink that will recieve notifications
     */
    public void registerForTypedElementEvents(ITypedElementEventsSink handler)
    {      
        m_TypeSink.addListener(handler,null);
    }

    /**
     * Removes a listener from the current list.
     *
     * @param handler[in] ITypedElementEventsSink.
     */
    public void revokeTypedElementSink(ITypedElementEventsSink handler)
    {      
        m_TypeSink.removeListener(handler);
    }

    /**
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param sink[in] The actual sink that will recieve notifications
     */
    public void registerForAttributeEvents(IAttributeEventsSink handler)
    {      
        m_AttrSink.addListener(handler,null);
    }

    /**
     * Removes a listener from the current list.
     *
     * @param handler[in] IAttributeEventsSink.
     */
    public void revokeAttributeSink(IAttributeEventsSink handler)
    {      
        m_AttrSink.removeListener(handler);
    }

    /**
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param sink[in] The actual sink that will receive notifications
     */
    public void registerForOperationEvents(IOperationEventsSink handler)
    {
        m_OperSink.addListener(handler,null);
    }

    /**
     * Removes a listener from the current list.
     *
     * @param handler[in] IOperationEventsSink.
     */
    public void revokeOperationSink(IOperationEventsSink handler)
    {     
        m_OperSink.removeListener(handler);
    }

    /**
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param sink[in] The actual sink that will recieve notifications
     */ 
    public void registerForTransformEvents(IClassifierTransformEventsSink handler)
    {
        m_TransformSink.addListener(handler,null);
    }

    /**
     * Removes a listener from the current list.
     */
    public void revokeTransformSink(IClassifierTransformEventsSink handler)
    {      
        m_TransformSink.removeListener(handler);
    }

    /**
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param sink[in] The actual sink that will recieve notifications
     */ 
    public void registerForAssociationEndTransformEvents(IAssociationEndTransformEventsSink handler)
    {
        m_AssociationEndTransformSink.addListener(handler,null);
    }

    /**
     * Removes a listener from the current list.
     * @param handler IAssociationEndTransformEventsSink.
     */
    public void revokeAssociationEndTransformSink(IAssociationEndTransformEventsSink handler)
    {
        m_AssociationEndTransformSink.removeListener(handler);
    }

    /**
     *
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param handler[in] The actual sink that will recieve notifications
     */
    public void registerForAffectedElementEvents(IAffectedElementEventsSink handler)
    {
       m_AffectedSink.addListener(handler,null);
    }

    /**
     *
     * Removes a listener from the current list.
     * @param handler[in] The actual sink that need to be removed
     */
    public void revokeAffectedElementEvents(IAffectedElementEventsSink handler)
    {
        m_AffectedSink.removeListener(handler);
    }

    /**
     * Registers the passed-in event sink with this dispatcher.
     *
     * @param sink[in] The actual sink that will receive notifications
     */
    public void registerForAssociationEndEvents(IAssociationEndEventsSink handler)
    {
        m_AssociationEndEventsSink.addListener(handler,null);
    }

    /**
     * Removes a listener from the current list.
     * 
     * @param handler IAssociationEndEventsSink.
     */
    public void revokeAssociationEndSink(IAssociationEndEventsSink handler)
    {
        m_AssociationEndEventsSink.removeListener(handler);
    }

    /**
     * Called whenever a feature is about to be added to a classifier.
     *
     * @param classifier[in] 
     * @param feature[in]
     * @param payload[in]
     * 
     * @return boolean proceed
     */
    public boolean fireFeaturePreAdded(
            IClassifier classifier,
            IFeature feature,
            IEventPayload payload )
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,feature);
        Object var = prepareVariant(vect);

        if (validateEvent("FeaturePreAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor featurePreAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onFeaturePreAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            featurePreAdded.setParameters(parms);
            m_ClassifierSink.notifyListenersWithQualifiedProceed(featurePreAdded);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Called whenever a feature was added to a classifier.
     *
     * @param classifier[in] 
     * @param feature[in]
     * @param payload[in]
     */ 
    public void fireFeatureAdded(
            IClassifier classifier,
            IFeature feature,
            IEventPayload payload)
    {	
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,feature);
        Object var = prepareVariant(vect);		
        if (validateEvent("FeatureAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor featureAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onFeatureAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            featureAdded.setParameters(parms);
            m_ClassifierSink.notifyListeners(featureAdded);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireFeaturePreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean fireFeaturePreRemoved(
            IClassifier classifier,
            IFeature feature,
            IEventPayload payload)
    {
        boolean proceed = true;
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,feature);
        Object var = prepareVariant(vect);		
        if (validateEvent("FeaturePreRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor featurePreRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onFeaturePreRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            featurePreRemoved.setParameters(parms);
            m_ClassifierSink.notifyListenersWithQualifiedProceed(featurePreRemoved);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireFeatureRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireFeatureRemoved(
            IClassifier classifier,
            IFeature feature,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,feature);
        Object var = prepareVariant(vect);		
        if (validateEvent("FeatureRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor featureRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onFeatureRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            featureRemoved.setParameters(parms);
            m_ClassifierSink.notifyListeners(featureRemoved);
        }
    }

    /**
     * Called whenever a feature is to be moved to a classifier.
     *
     * @param classifier[in] 
     * @param feature[in]
     * @param payload[in]
     */ 
    public boolean fireFeaturePreMoved(
            IClassifier classifier,
            IFeature feature,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,feature);
        Object var = prepareVariant(vect);

        if (validateEvent("FeaturePreMoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor featurePreMoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onFeaturePreMoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            featurePreMoved.setParameters(parms);
            m_ClassifierSink.notifyListenersWithQualifiedProceed(featurePreMoved);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;

    }

    /**
     * Called whenever a feature is moved to a classifier.
     *
     * @param classifier[in] 
     * @param feature[in]
     * @param payload[in]
     */
    public void fireFeatureMoved(
            IClassifier classifier,
            IFeature feature,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,feature);
        Object var = prepareVariant(vect);		
        if (validateEvent("FeatureMoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor featureMoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onFeatureMoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            featureMoved.setParameters(parms);
            m_ClassifierSink.notifyListeners(featureMoved);
        }
    }

    /**
     * Description.
     *
     * @param classifier[in] 
     * @param feature[in]
     * @param payload[in]
     */

    public boolean fireFeaturePreDuplicatedToClassifier(
            IClassifier classifier,
            IFeature feature,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,feature);
        Object var = prepareVariant(vect);

        if (validateEvent("FeaturePreDuplicatedToClassifier", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor featurePreDuplicatedToClassifier = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onFeaturePreDuplicatedToClassifier");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            featurePreDuplicatedToClassifier.setParameters(parms);
            m_ClassifierSink.notifyListenersWithQualifiedProceed(featurePreDuplicatedToClassifier);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Description.
     *
     * @param pOldClassifier[in] 
     * @param pOldFeature[in]
     * @param pNewClassifier[in]
     * @param pnewFeature[in]
     */

    public boolean fireFeatureDuplicatedToClassifier(
            IClassifier pOldClassifier,
            IFeature pOldFeature,
            IClassifier pNewClassifier,
            IFeature pNewFeature,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,pOldClassifier);
        vect.add(1,pOldFeature);
        vect.add(2,pNewClassifier);
        vect.add(3,pNewFeature);
        Object var = prepareVariant(vect);		
        if (validateEvent("FeatureDuplicatedToClassifier", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor featureDuplicatedToClassifier = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onFeatureDuplicatedToClassifier");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            featureDuplicatedToClassifier.setParameters(parms);
            m_ClassifierSink.notifyListeners(featureDuplicatedToClassifier);
        }
        return true;
    }

    /**
     * Called whenever the abstract flag on the Classifier is about to be modified.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean fireClassifierPreAbstractModified(
            IClassifier feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));
        Object var = prepareVariant(vect);

        if (validateEvent("PreAbstractModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor classifierFeaturePreAbstractModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onPreAbstractModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            classifierFeaturePreAbstractModified.setParameters(parms);
            m_ClassifierSink.notifyListenersWithQualifiedProceed(classifierFeaturePreAbstractModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Called whenever the abstract flag on the Classifier has been modified..
     *
     * @param feature[in] 
     * @param payload[in]
     */
    public void fireClassifierAbstractModified(
            IClassifier feature,
            IEventPayload payload)
    {
        if (validateEvent("AbstractModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor classifierFeatureAbstractModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onAbstractModified");
            
            Object[] params = new Object[2];
            params[0] = feature;
            params[1] = cell;            
            classifierFeatureAbstractModified.setParameters(params);
            m_ClassifierSink.notifyListeners(classifierFeatureAbstractModified);
        }
    }

    /**
     * Called whenever the leaf flag on the Classifier is about to be modified.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */

    public boolean firePreLeafModified(
            IClassifier feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));
        Object var = prepareVariant(vect);

        if (validateEvent("PreLeafModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preLeafModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onPreLeafModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preLeafModified.setParameters(parms);
            m_ClassifierSink.notifyListenersWithQualifiedProceed(preLeafModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireLeafModified(IClassifier feature, IEventPayload payload)
    {
        if (validateEvent("LeafModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor leafModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onLeafModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            leafModified.setParameters(parms);
            m_ClassifierSink.notifyListeners(leafModified);
        }
    }

    /**
     * Called whenever the transient flag on the Classifier is about to be modified.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean fireClassifierPreTransientModified(
            IClassifier feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));
        Object var = prepareVariant(vect);

        if (validateEvent("PreTransientModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor classifierPreTransientModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onPreTransientModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            classifierPreTransientModified.setParameters(parms);
            m_ClassifierSink.notifyListenersWithQualifiedProceed(classifierPreTransientModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireClassifierTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireClassifierTransientModified(
            IClassifier feature,
            IEventPayload payload)
    {
        if (validateEvent("TransientModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor classifierTransientModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onTransientModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            classifierTransientModified.setParameters(parms);
            m_ClassifierSink.notifyListeners(classifierTransientModified);
        }
    }

    public boolean firePreTemplateParameterAdded(
            IClassifier pClassifier,
            IParameterableElement pParam,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,pClassifier);
        vect.add(1,pParam);
        Object var = prepareVariant(vect);

        if (validateEvent("PreTemplateParameterAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preTemplateParameterAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onPreTemplateParameterAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preTemplateParameterAdded.setParameters(parms);
            m_ClassifierSink.notifyListenersWithQualifiedProceed(preTemplateParameterAdded);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    public void fireTemplateParameterAdded(
            IClassifier pClassifier,
            IParameterableElement pParam,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,pClassifier);
        vect.add(1,pParam);
        Object var = prepareVariant(vect);

        if (validateEvent("TemplateParameterAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor templateParameterAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onTemplateParameterAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            templateParameterAdded.setParameters(parms);
            m_ClassifierSink.notifyListeners(templateParameterAdded);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#firePreTemplateParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreTemplateParameterRemoved(
            IClassifier pClassifier,
            IParameterableElement pParam,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,pClassifier);
        vect.add(1,pParam);
        Object var = prepareVariant(vect);

        if (validateEvent("PreTemplateParameterRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preTemplateParameterRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onPreTemplateParameterRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preTemplateParameterRemoved.setParameters(parms);
            m_ClassifierSink.notifyListenersWithQualifiedProceed(preTemplateParameterRemoved);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireTemplateParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireTemplateParameterRemoved(
            IClassifier pClassifier,
            IParameterableElement pParam,
            IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,pClassifier);
        vect.add(1,pParam);
        Object var = prepareVariant(vect);

        if (validateEvent("TemplateParameterRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor templateParameterRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                        "onTemplateParameterRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            templateParameterRemoved.setParameters(parms);
            m_ClassifierSink.notifyListeners(templateParameterRemoved);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#firePreTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreTransform(
            IClassifier classifier,
            String newForm,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,newForm);
        Object var = prepareVariant(vect);

        if (validateEvent("PreTransform", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preTransform = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink", 
                        "onPreTransform");
            
            Object[] parms = new Object[3];
            parms[0] = classifier;
			parms[1] = newForm;
            parms[2] = cell;
            preTransform.setParameters(parms);
			m_TransformSink.notifyListenersWithQualifiedProceed(preTransform);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireTransformed(IClassifier classifier, IEventPayload payload)
    {
        if (validateEvent("Transformed", classifier))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor transformed = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink", 
                        "onTransformed");
            
            Object[] parms = new Object[2];
            parms[0] = classifier;
            parms[1] = cell;
            transformed.setParameters(parms);
            m_TransformSink.notifyListeners(transformed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#firePreAssociationEndTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreAssociationEndTransform(
            IAssociationEnd pEnd,
            String newForm,
            IEventPayload payload)
    {
        boolean proceed = true;		

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,pEnd);
        vect.add(1,newForm);
        Object var = prepareVariant(vect);

        if (validateEvent("PreTransform", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preAssociationEndTransform = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndTransformEventsSink", 
                        "onPreTransform");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preAssociationEndTransform.setParameters(parms);
            m_AssociationEndTransformSink.notifyListenersWithQualifiedProceed(preAssociationEndTransform);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireAssociationEndTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireAssociationEndTransformed(
            IAssociationEnd pEnd,
            IEventPayload payload)
    {
        if (validateEvent("Transformed", pEnd))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor associationEndTransformed = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndTransformEventsSink", 
                        "onTransformed");
            
            Object[] parms = new Object[2];
            parms[0] = pEnd;
            parms[1] = cell;
            associationEndTransformed.setParameters(parms);
            m_AssociationEndTransformSink.notifyListeners(associationEndTransformed);
        }
    }

    /**
     * Fired whenever the static flag on a particular feature is about
     * to be modified.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreStaticModified(
            IFeature feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));
        Object var = prepareVariant(vect);

        if (validateEvent("PreStaticModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preStaticModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink", 
                        "onPreStaticModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preStaticModified.setParameters(parms);
            m_FeatureSink.notifyListenersWithQualifiedProceed(preStaticModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired whenever the static flag on a particular feature 
     * was just modified.
     *
     * @param feature[in] 
     * @param payload[in]
     */
    public void fireStaticModified(IFeature feature, IEventPayload payload)
    {
        if (validateEvent("StaticModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor staticModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink", 
                        "onStaticModified");
            
            Object[] params = new Object[2];
            params[0] = feature;
            params[1] = cell;            
            staticModified.setParameters(params);
            m_FeatureSink.notifyListeners(staticModified);
        }
    }

    /**
     * Fired whenever the native flag on a particular feature is
     * about to be modified.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreNativeModified(
            IFeature feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));
        Object var = prepareVariant(vect);

        if (validateEvent("PreNativeModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preNativeModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink", 
                        "onPreNativeModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preNativeModified.setParameters(parms);
            m_FeatureSink.notifyListenersWithQualifiedProceed(preNativeModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireNativeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireNativeModified(IFeature feature, IEventPayload payload)
    {
        if (validateEvent("NativeModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor nativeModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink", 
                        "onNativeModified");
            
            Object[] params = new Object[2];
            params[0] = feature;
            params[1] = cell;            
            nativeModified.setParameters(params);
            m_FeatureSink.notifyListeners(nativeModified);
        }
    }

    /**
     * Fired whenever the ClientChangeability flag on a particular feature
     * is about to be modified.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreChangeabilityModified(
            IStructuralFeature feature,
            int proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Integer(proposedValue));
        Object var = prepareVariant(vect);

        if (validateEvent("PreChangeabilityModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preChangeabilityModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink", 
                        "onPreChangeabilityModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preChangeabilityModified.setParameters(parms);
            m_StructFeatureSink.notifyListenersWithQualifiedProceed(preChangeabilityModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireChangeabilityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireChangeabilityModified(
            IStructuralFeature feature,
            IEventPayload payload)
    {
        if (validateEvent("ChangeabilityModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor changeabilityModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink", 
                        "onChangeabilityModified");
            
            Object[] params = new Object[2];
            params[0] = feature;
            params[1] = cell;            
            changeabilityModified.setParameters(params);
            m_StructFeatureSink.notifyListeners(changeabilityModified);
        }
    }

    /**
     * Fired whenever the volatile flag on a particular feature is about
     * to be modified.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreVolatileModified(
            IStructuralFeature feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));
        Object var = prepareVariant(vect);

        if (validateEvent("PreVolatileModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preVolatileModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink", 
                        "onPreVolatileModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preVolatileModified.setParameters(parms);
            m_StructFeatureSink.notifyListenersWithQualifiedProceed(preVolatileModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher#fireVolatileModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireVolatileModified(
            IStructuralFeature feature,
            IEventPayload payload)
    {
        if (validateEvent("VolatileModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor volatileModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink", 
                        "onVolatileModified");
            
            Object[] params = new Object[2];
            params[0] = feature;
            params[1] = cell;            
            volatileModified.setParameters(params);
            m_StructFeatureSink.notifyListeners(volatileModified);
        }
    }

    /**
     * Fired whenever the transient flag on a particular feature is about to be modified.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreTransientModified(
            IStructuralFeature feature,
            boolean proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Boolean(proposedValue));        
        Object var = prepareVariant(vect);

        if (validateEvent("PreTransientModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preTransientModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink", 
                        "onPreTransientModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preTransientModified.setParameters(parms);
            m_StructFeatureSink.notifyListenersWithQualifiedProceed(preTransientModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired whenever the transient flag on a particular feature has been modified.
     *
     * @param feature[in] 
     * @param payload[in]
     */
    public void fireTransientModified(
            IStructuralFeature feature,
            IEventPayload payload)
    {
        if (validateEvent("TransientModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor transientModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink", 
                        "onTransientModified");
            
            Object[] params = new Object[2];
            params[0] = feature;
            params[1] = cell;            
            transientModified.setParameters(params);
            m_StructFeatureSink.notifyListeners(transientModified);
        }
    }

    /**
     * Fired whenever the concurrency value of a behavior feature is about to
     * be modified
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean fireConcurrencyPreModified(
            IBehavioralFeature feature,
            int proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,new Integer(proposedValue));        
        Object var = prepareVariant(vect);

        if (validateEvent("ConcurrencyPreModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor concurrencyPreModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onConcurrencyPreModified");
            
            Object[] params = new Object[2];
            params[0] = var;
            params[1] = cell;
            concurrencyPreModified.setParameters(params);
            m_BehavFeatureSink.notifyListenersWithQualifiedProceed(concurrencyPreModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired whenever the concurrency value of a behavioral feature was modified
     *
     * @param feature[in] 
     * @param payload[in]
     */
    public void fireConcurrencyModified(
            IBehavioralFeature feature,
            IEventPayload payload)
    {
        if (validateEvent("ConcurrencyModified", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor concurrencyModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onConcurrencyModified");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            concurrencyModified.setParameters(parms);
            m_BehavFeatureSink.notifyListeners(concurrencyModified);
        }
    }

    /**
     * Fired whenever a signal is about to be added to the behavioral feature,
     * indicating that the feature can 'catch' the specified signal.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreHandledSignalAdded(
            IBehavioralFeature feature,
            ISignal proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,proposedValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreHandledSignalAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preHandledSignalAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onPreHandledSignalAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preHandledSignalAdded.setParameters(parms);
            m_BehavFeatureSink.notifyListenersWithQualifiedProceed(preHandledSignalAdded);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired whenever a signal is added to the behavioral feature, indicating that the
     * feature can 'catch' the specified signal.
     *
     * @param feature[in] 
     * @param payload[in]
     */
    public void fireHandledSignalAdded(
            IBehavioralFeature feature,
            IEventPayload payload)
    {
        if (validateEvent("HandledSignalAdded", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor handledSignalAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onHandledSignalAdded");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            handledSignalAdded.setParameters(parms);
            m_BehavFeatureSink.notifyListeners(handledSignalAdded);
        }
    }

    /**
     * Fired whenever a signal is about to be removed from the behavioral
     * feature, indicating that the feature can no longer 'catch' the
     * specified signal.
     *
     * @param feature[in] 
     * @param proposedValue[in]
     * @param payload[in]
     */
    public boolean firePreHandledSignalRemoved(
            IBehavioralFeature feature,
            ISignal proposedValue,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,proposedValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreHandledSignalRemoved", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preHandledSignalRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onPreHandledSignalRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preHandledSignalRemoved.setParameters(parms);
            m_BehavFeatureSink.notifyListenersWithQualifiedProceed(preHandledSignalRemoved);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;
    }

    /**
     * Fired whenever a signal was removed from the behavioral feature, indicating
     * that the feature can no longer 'catch' the specified signal.
     *
     * @param feature[in] 
     * @param payload[in]
     */
    public void fireHandledSignalRemoved(
            IBehavioralFeature feature,
            IEventPayload payload)
    {
        if (validateEvent("HandledSignalRemoved", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor handledSignalRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onHandledSignalRemoved");
            
            Object[] parms = new Object[2];
            parms[0] = feature;
            parms[1] = cell;
            handledSignalRemoved.setParameters(parms);
            m_BehavFeatureSink.notifyListeners(handledSignalRemoved);
        }

    }

    /**
     * Fired whenever a new parameter is about to be added to the behavioral
     * feature's list of parameters.
     *
     * @param feature[in] 
     * @param parm[in]
     * @param payload[in]
     */
    public boolean firePreParameterAdded(
            IBehavioralFeature feature,
            IParameter parm,
            IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,feature);
        vect.add(1,parm);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreParameterAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preParameterAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onPreParameterAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preParameterAdded.setParameters(parms);
            m_BehavFeatureSink.notifyListenersWithQualifiedProceed(preParameterAdded);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed; 
    }

    /**
     * Fired whenever a new parameter was added to the behavioral feature's
     * list of parameters.
     *
     * @param feature[in] 
     * @param parm[in]
     */
    public void fireParameterAdded( IBehavioralFeature feature, 
            IParameter parm, 
            IEventPayload payload)
    {
        if (validateEvent("ParameterAdded", feature))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor parameterAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink", 
                        "onParameterAdded");
            
            Object[] parms = new Object[] { feature, parm, cell };
            parameterAdded.setParameters(parms);
            m_BehavFeatureSink.notifyListeners(parameterAdded);
        }
    }
    
    public boolean firePreOperationPropertyModified(IOperation oper,
            int kind,
            boolean proposedValue, 
            IEventPayload payload)
	{
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,new Integer(kind));
        vect.add(2,new Boolean(proposedValue));
        Object var = prepareVariant(vect);

        if (validateEvent("PreOperationPropertyModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preOperationPropertyModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onPreOperationPropertyModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preOperationPropertyModified.setParameters(parms);
            m_OperSink.notifyListenersWithQualifiedProceed(preOperationPropertyModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed;		
	}

    public void fireOperationPropertyModified(IOperation oper,int kind,IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,oper);
        vect.add(1,new Integer(kind));
        Object var = prepareVariant(vect);

        if (validateEvent("OperationPropertyModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor operationPropertyModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink", 
                        "onOperationPropertyModified");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            operationPropertyModified.setParameters(parms);
            m_OperSink.notifyListeners(operationPropertyModified);
        }
    }

    public void fireEnumerationLiteralAdded(
        IClassifier classifier, 
        IEnumerationLiteral enumLit,
        IEventPayload payload)
    {
        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,enumLit);
        Object var = prepareVariant(vect);
        
        if (validateEvent("EnumerationLiteralAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor eventFunctor = new EventFunctor(
                "org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                "onEnumerationLiteralAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            
            eventFunctor.setParameters(parms);
            m_ClassifierSink.notifyListeners(eventFunctor);
        }
    }

    public boolean fireEnumerationLiteralPreAdded(
        IClassifier classifier, 
        IEnumerationLiteral enumLit, 
        IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(0,classifier);
        vect.add(1,enumLit);
        Object var = prepareVariant(vect);

        if (validateEvent("EnumerationLiteralPreAdded", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor eventFunctor = new EventFunctor(
                "org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink", 
                "onEnumerationLiteralPreAdded");
            
            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            
            eventFunctor.setParameters(parms);
            m_ClassifierSink.notifyListenersWithQualifiedProceed(eventFunctor);
            
            if (cell != null)
                proceed = cell.canContinue();
        }

        return proceed;
    }

}


