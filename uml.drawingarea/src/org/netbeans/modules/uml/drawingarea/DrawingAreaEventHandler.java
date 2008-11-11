/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.drawingarea;

import java.util.ArrayList;

import java.util.List;
import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IActor;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndTransformEventsSink;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.diagramsupport.DrawingAreaEventsAdapter;

/**
 * @author sumitabhk
 *
 */
public class DrawingAreaEventHandler
        extends DrawingAreaEventsAdapter
        implements
        IElementLifeTimeEventsSink,
        IClassifierFeatureEventsSink,
        IClassifierTransformEventsSink,
        IElementModifiedEventsSink,
        ITypedElementEventsSink,
        IRelationEventsSink,
        INamedElementEventsSink,
        IWSElementEventsSink,
        IAffectedElementEventsSink,
        IStereotypeEventsSink,
        INamespaceModifiedEventsSink,
        IElementDisposalEventsSink,
        IAttributeEventsSink,
        IOperationEventsSink,
        IParameterEventsSink,
        IBehavioralFeatureEventsSink,
        IRedefinableElementModifiedEventsSink,
        ILifelineModifiedEventsSink,
        IFeatureEventsSink,
        ICoreProductInitEventsSink,
        IActivityEdgeEventsSink,
        IProjectEventsSink,
        IAssociationEndEventsSink,
        IAssociationEndTransformEventsSink
{
    private final ArrayList < DrawingAreaChangeListener > listeners =
            new ArrayList < DrawingAreaChangeListener >();
    
    private ArrayList < IElement> m_AffectedModelElements = new ArrayList < IElement> ();
    private ArrayList < String> m_AffectdXMIID = new ArrayList <String>();
    
    private static final DrawingAreaEventHandler instance =
            new DrawingAreaEventHandler();
    
    static
    {
        DispatchHelper helper = new DispatchHelper();
        helper.registerForWSElementEvents(instance);
        helper.registerForElementModifiedEvents(instance);
        helper.registerForLifeTimeEvents(instance);
        helper.registerForClassifierFeatureEvents(instance);
        helper.registerForTransformEvents(instance);
        helper.registerForTypedElementEvents(instance);
        helper.registerForRelationEvents(instance);
        helper.registerForNamedElementEvents(instance);
        helper.registerForAffectedElementEvents(instance);
        helper.registerForAssociationEndEvents(instance);
        helper.registerForStereotypeEventsSink(instance);
        helper.registerElementDisposalEvents(instance);
        helper.registerForNamespaceModifiedEvents(instance);
        helper.registerForAttributeEvents(instance);
        helper.registerForOperationEvents(instance);
        helper.registerForParameterEvents(instance);
        helper.registerForBehavioralFeatureEvents(instance);
        helper.registerForRedefinableElementModifiedEvents(instance);
        helper.registerForDynamicsEvents(instance);
        helper.registerForFeatureEvents(instance);
        helper.registerForInitEvents(instance);
        helper.registerForProjectEvents(instance);
        helper.registerForActivityEdgeEvents(instance);
        helper.registerForAssociationEndTransformEvents(instance);
    }
    
    /**
     *
     */
    private DrawingAreaEventHandler()
    {
        super();
    }
    
    static void addChangeListener(DrawingAreaChangeListener listener)
    {
        instance.listeners.add(listener);
    }
    
    static void removeChangeListener(DrawingAreaChangeListener listener)
    {
        instance.listeners.remove(listener);
    }
    
    protected void notifyChangeListeners(final IElement changedElement,
                                         final IElement secondaryElement,
                                         final ModelElementChangedKind changeType,
                                         boolean delayAction)
    {
        class ElementModified implements Runnable
        {
            public void run()
            {
                for(DrawingAreaChangeListener listener : listeners)
                {
                    listener.elementChanged(changedElement, secondaryElement, changeType);
                }
            }
        }
        
        
        //we just want to run this event instead of doing it later, because we were not updating
        //drawing area if events are ElementModified - in our case the first event that comes is
        //element modified and then other events are not run and hence drawing area is not updated.
        ElementModified runnable = new ElementModified();
        if(delayAction == true)
        {
            SwingUtilities.invokeLater(runnable); 
        }
        else
        {
            runnable.run();
        }
        
        m_AffectdXMIID.clear();
        m_AffectedModelElements.clear();
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreCreate(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onElementPreCreate(String ElementType, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onElementCreated(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onElementPreDelete(IVersionableElement element, IResultCell cell)
    {
        if (element instanceof IElement )
        {
            IElement pEle = (IElement)element;
            IElement secondary = null;
            
            
            if(element instanceof IElementImport)
            {
                // The call to getImportElement acutally retrieves the
                // cloned element that lives in the importing project, not the
                // element that is in the original project.
                
               // Fixed issue 149444
//                IElement clone = ((IElementImport)element).getImportedElement();
//                IElement origOwner = clone.getOwner();
//                pEle = FactoryRetriever.instance().findElementByID(origOwner, clone.getXMIID());
                
                pEle = ((IElementImport)element).getImportedElement();
                IElement origOwner = pEle.getOwner();
                secondary = FactoryRetriever.instance().findElementByID(origOwner,
                        pEle.getXMIID());
            }
            else if(element instanceof IPackageImport)
            {
                // The call to getImportElement acutally retrieves the
                // cloned element that lives in the importing project, not the
                // element that is in the original project.
                IPackage clone = ((IPackageImport)element).getImportedPackage();
                IElement origOwner = clone.getOwner();
                pEle = FactoryRetriever.instance().findElementByID(origOwner,
                        clone.getXMIID());
            }
            else if(element instanceof IActor)
            {
                //keep pEle and secondary as is.
            }
            else if(element instanceof IFeature)
            {
                pEle = ((IFeature)element).getFeaturingClassifier();
                secondary = (IElement)element;
            }
            
            notifyChangeListeners(pEle, 
                                  secondary, 
                                  ModelElementChangedKind.PRE_DELETE,
                                  false);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onElementDeleted(IVersionableElement element, IResultCell cell)
    {
        if (element instanceof IElement)
        {
            IElement pModEle = (IElement)element;
            IElement secondary = null;
             
            if(element instanceof IElementImport)
            {
                IProject ownerProject = pModEle.getProject();
                
                // The call to getImportElement acutally retrieves the
                // cloned element that lives in the importing project, not the
                // element that is in the original project.
                
                // Fixed issue 149444
//                IElement clone = ((IElementImport)element).getImportedElement();
//                IElement origOwner = clone.getOwner();
//                pModEle = FactoryRetriever.instance().findElementByID(origOwner, clone.getXMIID());
                
                pModEle = ((IElementImport)element).getImportedElement();
                IElement origOwner = pModEle.getOwner();
                secondary = FactoryRetriever.instance().findElementByID(origOwner,
                        pModEle.getXMIID());
            }
            
            // I have had to make this not be a delayed event because, if it is
            // delayed the owner of the deleted element will be removed.  Since
            // our logic needs to know who owns the element, it is kind of
            // important.
            notifyChangeListeners(pModEle, secondary, 
                                  ModelElementChangedKind.DELETE,
                                  false);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onElementPreDuplicated(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onElementDuplicated(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onFeaturePreAdded(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onFeatureAdded(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        postElementModifiedEvent(classifier, feature, ModelElementChangedKind.FEATUREADDED);
    }
    
    public void onEnumerationLiteralPreAdded(IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onEnumerationLiteralAdded(IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
        postElementModifiedEvent(classifier, enumLit, ModelElementChangedKind.FEATUREADDED);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onFeaturePreRemoved(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onFeatureRemoved(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        if (classifier != null && feature != null)
        {
            // classifier on entry is the source
            if (classifier != null)
            {
                postElementDeletedEvent(classifier, feature);
            }
            
            // notify the target
            IClassifier pClass = feature.getFeaturingClassifier();
            if (pClass != null && pClass instanceof IElement)
            {
                IElement pEle = (IElement)pClass;
                postElementModifiedEvent(pEle, feature, ModelElementChangedKind.DELETE);
            }
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onFeaturePreMoved(IClassifier classifier, IFeature feature, IResultCell cell)
    {

    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onFeatureMoved(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        if (classifier != null && feature != null)
        {
            // classifier on entry is the source
            if (classifier != null)
            {
                postElementDeletedEvent(classifier, feature);
                postElementModifiedEvent(classifier, feature, ModelElementChangedKind.DELETE);
            }
            
            // notify the target
            IClassifier pClass = feature.getFeaturingClassifier();
            if (pClass != null && pClass instanceof IElement)
            {
                IElement pEle = (IElement)pClass;
                postElementModifiedEvent(pEle, feature, ModelElementChangedKind.FEATUREMOVED);//fired to target classifier
            }
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreDuplicatedToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onFeaturePreDuplicatedToClassifier(IClassifier classifier, IFeature feature, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureDuplicatedToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onFeatureDuplicatedToClassifier(IClassifier pOldClassifier, IFeature pOldFeature, IClassifier pNewClassifier, IFeature pNewFeature, IResultCell cell)
    {
        postElementModifiedEvent(pNewClassifier, pNewFeature, ModelElementChangedKind.FEATUREDUPLICATEDTOCLASSIFIER);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreAbstractModified(IClassifier feature, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onAbstractModified(IClassifier feature, IResultCell cell)
    {
        if (feature != null)
        {
            postElementModifiedEvent(feature, null, ModelElementChangedKind.ABSTRACTMODIFIED);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreLeafModified(IClassifier feature, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onLeafModified(IClassifier feature, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreTransientModified(IClassifier feature, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onTransientModified(IClassifier feature, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreTemplateParameterAdded(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onTemplateParameterAdded(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
    {
        notifyChangeListeners(pClassifier, null, 
                              ModelElementChangedKind.TEMPLATE_PARAMETER,
                              true);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreTemplateParameterRemoved(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onTemplateParameterRemoved(IClassifier pClassifier, IParameterableElement pParam, IResultCell cell)
    {
        notifyChangeListeners(pClassifier, null, 
                              ModelElementChangedKind.TEMPLATE_PARAMETER,
                              true);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink#onPreTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreTransform(IClassifier classifier, String newForm, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink#onTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onTransformed(IClassifier classifier, IResultCell cell)
    {
        if (classifier != null)
        {
            postElementTransformedEvent(classifier);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink#onElementPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onElementPreModified(IVersionableElement element, IResultCell cell)
    {
        // TODO Auto-generated method stub
        
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink#onElementModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onElementModified(IVersionableElement element, IResultCell cell)
    {
        EventContextManager conMan = new EventContextManager();
        
        // Here we make sure that we don't respond to event such as a presentation element
        // being added to a model element.  Who cares!
        if (conMan.isNoEffectModification() == false)
        {
            if (element instanceof IElement)
            {
                IElement elem = (IElement)element;
                postElementModifiedEvent(elem, null, ModelElementChangedKind.ELEMENTMODIFIED);
            }
        }
    }
    
    /**
     * Post an element modified event
     *
     * @param pElement [in] The element that changed
     * @param pSecondaryElement [in] The secondary element, depends on the change kind
     * @param nKind [in] The kind of event that happened
     * @param bVerifyDiagramAndElementInSameProject [in] true to check the elements project to make sure
     * its the same.
     */
    private void postElementModifiedEvent(IElement pElement,
            IElement pSecondaryElement,
            ModelElementChangedKind nKind)
    {
        
        if ((pElement == null && pSecondaryElement == null) || m_AffectdXMIID.contains(pElement.getXMIID()) == true)
        {
            return;
        }
        
        m_AffectdXMIID.add(pElement.getXMIID());
        
        boolean proceed = true;
        
        // I am hoping that we will not have to worry about this case.
        //      if (bVerifyDiagramAndElementInSameProject)
        //      {
        //         // Get the project off the element
        //         IProject pElemProj = pElement.getProject();
        //         if (pElemProj != null)
        //         {
        //            // Get the project off the diagram
        //            IProject pDiaProj = ctrl.getProject();
        //            if (pDiaProj != null)
        //            {
        //               if (!pDiaProj.isSame(pElemProj))
        //               {
        //                  proceed = false;
        //               }
        //            }
        //         }
        //      }
        
        notifyChangeListeners(pElement, pSecondaryElement, nKind, true);
        m_AffectdXMIID.clear();
    }
    
    /**
     * Post an element deleted event
     */
    private void postElementDeletedEvent(IElement pElement, IElement pSecondaryElement)
    {
        notifyChangeListeners(pElement, pSecondaryElement, 
                              ModelElementChangedKind.ELEMENT_DELETED,
                              true);
    }
    
    //	/ Post an element transformed event
    private void postElementTransformedEvent(IElement pElement)
    {
        notifyChangeListeners(pElement, null, 
                              ModelElementChangedKind.ELEMENT_TRANSFORMED,
                              true);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreMultiplicityModified(ITypedElement element, IMultiplicity proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onMultiplicityModified(ITypedElement element, IResultCell cell)
    {
        if (element != null)
        {
            postElementModifiedEvent(element, null, ModelElementChangedKind.MULTIPLICITYMODIFIED);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreTypeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreTypeModified(ITypedElement element, IClassifier proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onTypeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onTypeModified(ITypedElement element, IResultCell cell)
    {
        if (element != null)
        {
            postElementModifiedEvent(element, null, ModelElementChangedKind.TYPEMODIFIED);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreLowerModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreLowerModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onLowerModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onLowerModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        if (mult != null)
        {
            IElement primary = element;
            IElement secondary = null;
            if(element instanceof IAssociationEnd)
            {
                secondary = element;
                primary = ((IAssociationEnd)element).getAssociation();
            }
            
            postElementModifiedEvent(primary, secondary, ModelElementChangedKind.MULTIPLICITYMODIFIED);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreUpperModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreUpperModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onUpperModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onUpperModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        if (mult != null)
        {
            IElement primary = element;
            IElement secondary = null;
            if(element instanceof IAssociationEnd)
            {
                secondary = element;
                primary = ((IAssociationEnd)element).getAssociation();
            }
            postElementModifiedEvent(primary, secondary, ModelElementChangedKind.MULTIPLICITYMODIFIED);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreRangeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreRangeAdded(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onRangeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onRangeAdded(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        IElement primary = element;
        IElement secondary = null;
        if(element instanceof IAssociationEnd)
        {
            secondary = element;
            primary = ((IAssociationEnd)element).getAssociation();
        }
        postElementModifiedEvent(primary, secondary, ModelElementChangedKind.MULTIPLICITYMODIFIED);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreRangeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreRangeRemoved(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onRangeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onRangeRemoved(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        IElement primary = mult;
        IElement secondary = null;
        if(element instanceof IAssociationEnd)
        {
            secondary = element;
            primary = ((IAssociationEnd)element).getAssociation();
        }
        postElementModifiedEvent(primary, secondary, ModelElementChangedKind.MULTIPLICITYMODIFIED);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreOrderModified(ITypedElement element, IMultiplicity mult, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onOrderModified(ITypedElement element, IMultiplicity mult, IResultCell cell)
    {
        if (mult != null)
        {
            postElementModifiedEvent(mult, null, ModelElementChangedKind.MULTIPLICITYMODIFIED);
        }
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
        // This information is not represented on the diagram.
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationEndModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreRelationEndModified(IRelationProxy proxy, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationEndModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onRelationEndModified(IRelationProxy proxy, IResultCell cell)
    {
        if (proxy != null)
        {
            IElement pEle = proxy.getConnection();
            if (pEle != null)
            {
                postElementModifiedEvent(pEle, null, ModelElementChangedKind.RELATION_END_MODIFIED);
            }
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationEndAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreRelationEndAdded(IRelationProxy proxy, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationEndAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onRelationEndAdded(IRelationProxy proxy, IResultCell cell)
    {
        if (proxy != null)
        {
            IElement pEle = proxy.getConnection();
            if (pEle != null)
            {
                postElementModifiedEvent(pEle, null, ModelElementChangedKind.RELATION_END_ADDED);
            }
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationEndRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreRelationEndRemoved(IRelationProxy proxy, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationEndRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onRelationEndRemoved(IRelationProxy proxy, IResultCell cell)
    {
        if (proxy != null)
        {
            IElement pEle = proxy.getConnection();
            if (pEle != null)
            {
                postElementModifiedEvent(pEle, null, ModelElementChangedKind.RELATION_END_REMOVED);
            }
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreRelationCreated(IRelationProxy proxy, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onRelationCreated(IRelationProxy proxy, IResultCell cell)
    {
        if (proxy != null)
        {
            IElement pEle = proxy.getConnection();
            if (pEle != null)
            {
                postElementModifiedEvent(pEle, null, ModelElementChangedKind.RELATION_CREATED);
            }
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreRelationDeleted(IRelationProxy proxy, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onRelationDeleted(IRelationProxy proxy, IResultCell cell)
    {
        if (proxy != null)
        {
            IElement pEle = proxy.getConnection();
            if (pEle != null)
            {
                postElementModifiedEvent(pEle, null, ModelElementChangedKind.RELATION_DELETED);
            }
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreNameModified(INamedElement element, String proposedName, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onNameModified(INamedElement element, IResultCell cell)
    {
        if (element == null)
            return;
        
        IElement secondary = null;
        if(element instanceof IAssociationEnd)
        {
            IAssociationEnd end = (IAssociationEnd) element;
            element = end.getAssociation();
            secondary = end;
        }
        else if (element instanceof IFeature)
        {
            IFeature feature = (IFeature)element;
            if(feature.getFeaturingClassifier() != null)
            {
                // When a feature is an AssociationEnd's Qualifier then there
                // will be no featureing classifier.
                element = feature.getFeaturingClassifier();
                secondary = feature;
            }
            
        }
        else if(element instanceof IParameterableElement)
        {
            IParameterableElement parameter = (IParameterableElement)element;
            if(parameter.getTemplate() != null)
            {
                element = parameter.getTemplate();
                secondary = parameter;
            }
        }
        
        postElementModifiedEvent(element, secondary, ModelElementChangedKind.NAME_MODIFIED);
        
        if (element instanceof IClassifier)
        {
            IClassifier classifier = (IClassifier) element;
            List < IFeature > features = classifier.getFeatures();
            if (features != null)
            {
                for (IFeature feature : features)
                {
                    if (feature.getIsRedefining() == true)
                    {
                        List<IRedefinableElement> redefined = feature.getRedefiningElements();
                        for (IRedefinableElement redefinedElement : redefined)
                        {
                            if (redefinedElement instanceof IFeature)
                            {
                                IFeature redefinedFeature = (IFeature) redefinedElement;
                                IClassifier owner = redefinedFeature.getFeaturingClassifier();
                                postElementModifiedEvent(owner, element, ModelElementChangedKind.REDEFINED_OWNER_NAME_CHANGED);
                            }

                        }
                    }
                }
            }
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreVisibilityModified(INamedElement element, int proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onVisibilityModified(INamedElement element, IResultCell cell)
    {
        if (element != null && element instanceof IFeature)
        {
            IFeature pFeat = (IFeature)element;
            postElementModifiedEvent(element, pFeat, ModelElementChangedKind.VISIBILIT_YMODIFIED);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onAliasNameModified(INamedElement element, IResultCell cell)
    {
        IFeature pFeat = null;
        if (element != null)
        {
            if (element instanceof IFeature)
            {
                pFeat = (IFeature)element;
            }
            postElementModifiedEvent(element, pFeat, ModelElementChangedKind.ALIAS_MODIFIED);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String)
    */
    public void onPreNameCollision(INamedElement element, String proposedName, ETList < INamedElement > collidingElements, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement)
    */
    public void onNameCollision(INamedElement element, ETList < INamedElement > collidingElements, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, java.lang.String, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementPreCreate(IWSProject wsProject, String location, String Name, String data, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementCreated(IWSElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementPreSave(IWSElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementSaved(IWSElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementPreRemove(IWSElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementRemoved(IWSElement element, IResultCell cell)
    {
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreNameChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementPreNameChanged(IWSElement element, String proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementNameChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementNameChanged(IWSElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreOwnerChange(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementPreOwnerChange(IWSElement element, IWSProject newOwner, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementOwnerChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementOwnerChanged(IWSElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreLocationChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementPreLocationChanged(IWSElement element, String proposedLocation, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementLocationChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementLocationChanged(IWSElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreDataChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementPreDataChanged(IWSElement element, String newData, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementDataChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementDataChanged(IWSElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreDocChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementPreDocChanged(IWSElement element, String doc, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementDocChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementDocChanged(IWSElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementPreAliasChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementPreAliasChanged(IWSElement element, String proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink#onWSElementAliasChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWSElementAliasChanged(IWSElement element, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink#onPreImpacted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
    */
    public void onPreImpacted(IClassifier classifier, ETList < IVersionableElement > impacted, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink#onImpacted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
    */
    public void onImpacted(IClassifier classifier, ETList < IVersionableElement > impacted, IResultCell cell)
    {
        // TODO: Find out under what conditions does this event get fired.
//        if (impacted != null)
//        {
//            Vector < IFeature > featureMap = new Vector < IFeature > ();
//            // The ones we're interested in are attribute and parameter changes.  In those cases we need to
//            // alert the correct classifier.
//            int count = impacted.size();
//            for (int i = 0; i < count; i++)
//            {
//                IVersionableElement pEle = impacted.get(i);
//                IFeature pFeatureToNotify = null;
//                if (pEle instanceof IAttribute)
//                {
//                    pFeatureToNotify = (IAttribute)pEle;
//                }
//                else if (pEle instanceof IParameter)
//                {
//                    IElement elem = ((IParameter)pEle).getOwner();
//                    if (elem != null && elem instanceof IOperation)
//                    {
//                        pFeatureToNotify = (IOperation)elem;
//                    }
//                }
//                
//                if (pFeatureToNotify != null)
//                {
//                    featureMap.add(pFeatureToNotify);
//                }
//            }
//            
//            // Now issue an element modified for all the classifiers
//            if (featureMap.size() > 0)
//            {
//                int num = featureMap.size();
//                for (int j = 0; j < num; j++)
//                {
//                    IFeature pFeat = featureMap.get(j);
//                    IClassifier pClass = pFeat.getFeaturingClassifier();
//                    if (pClass != null)
//                    {
//                        postElementModifiedEvent(pClass, pFeat, ModelElementChangedKind.MECK_IMPACTED);
//                    }
//                }
//            }
//        }
    }
    
    public void onPreStereotypeApplied(Object pStereotype, IElement element, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onStereotypeApplied(Object pStereotype, IElement element, IResultCell cell)
    {
        if (element != null)
        {
            IFeature pFeat = null;
            if (element instanceof IFeature)
            {
                pFeat = (IFeature)element;
            }
            postElementModifiedEvent(element, pFeat, ModelElementChangedKind.STEREOTYPE);
        }
    }
    
    public void onPreStereotypeDeleted(Object pStereotype, IElement element, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onStereotypeDeleted(Object pStereotype, IElement element, IResultCell cell)
    {
        if (element != null)
        {
            IFeature pFeat = null;
            if (element instanceof IFeature)
            {
                pFeat = (IFeature)element;
            }
            postElementModifiedEvent(element, pFeat, ModelElementChangedKind.STEREOTYPE);
        }
    }
    
    public void onPreElementAddedToNamespace(INamespace space, INamedElement elementToAdd, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onElementAddedToNamespace(INamespace space, INamedElement elementAdded, IResultCell cell)
    {
        if (space != null && elementAdded != null)
        {
            postElementModifiedEvent(space, elementAdded, ModelElementChangedKind.ELEMENTADDEDTONAMESPACE);
        }
    }
    public void onPreDisposeElements(ETList < IVersionableElement > pElements, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onDisposedElements(ETList < IVersionableElement > pElements, IResultCell cell)
    {
        int count = pElements.size();
        for (int i = 0; i < count; i++)
        {
            IVersionableElement pEle = pElements.get(i);
            if (pEle instanceof IElement)
            {
                postElementDeletedEvent((IElement)pEle, null);
            }
        }
    }
    
    public void onDefaultPreModified(IAttribute attr, IExpression proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onDefaultModified(IAttribute attr, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreDefaultBodyModified(IAttribute feature, String bodyValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onDefaultBodyModified(IAttribute feature, IResultCell cell)
    {
        postElementModifiedEvent(feature, null, ModelElementChangedKind.DEFAULTMODIFIED);
    }
    
    public void onPreDefaultLanguageModified(IAttribute feature, String language, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onDefaultLanguageModified(IAttribute feature, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreDerivedModified(IAttribute feature, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onDerivedModified(IAttribute feature, IResultCell cell)
    {
        postElementModifiedEvent(feature, null, ModelElementChangedKind.DERIVEDMODIFIED);
    }
    
    public void onPrePrimaryKeyModified(IAttribute feature, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPrimaryKeyModified(IAttribute feature, IResultCell cell)
    {
        postElementModifiedEvent(feature, null, ModelElementChangedKind.PRIMARYKEYMODIFIED);
    }
    
    public void onConditionPreAdded(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onConditionAdded(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onConditionPreRemoved(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onConditionRemoved(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreQueryModified(IOperation oper, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onQueryModified(IOperation oper, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onRaisedExceptionPreAdded(IOperation oper, IClassifier pException, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onRaisedExceptionAdded(IOperation oper, IClassifier pException, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onRaisedExceptionPreRemoved(IOperation oper, IClassifier pException, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onRaisedExceptionRemoved(IOperation oper, IClassifier pException, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreOperationPropertyModified(IOperation oper, int nKind, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onOperationPropertyModified(IOperation oper, int nKind, IResultCell cell)
    {
        postElementModifiedEvent(oper, null, ModelElementChangedKind.OPERATION_PROPERTY_CHANGE);
    }
    
    public void onPreDefaultExpModified(IParameter feature, IExpression proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onDefaultExpModified(IParameter feature, IResultCell cell)
    {
        postElementModifiedEvent(feature, null, ModelElementChangedKind.PARAMETER_CHANGED);
    }
    
    public void onPreDefaultExpBodyModified(IParameter feature, String bodyValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onDefaultExpBodyModified(IParameter feature, IResultCell cell)
    {
        postElementModifiedEvent(feature, null, ModelElementChangedKind.PARAMETER_CHANGED);
    }
    
    public void onPreDefaultExpLanguageModified(IParameter feature, String language, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onDefaultExpLanguageModified(IParameter feature, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreDirectionModified(IParameter feature, int proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onDirectionModified(IParameter feature, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onConcurrencyPreModified(IBehavioralFeature feature, int proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onConcurrencyModified(IBehavioralFeature feature, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreHandledSignalAdded(IBehavioralFeature feature, ISignal proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onHandledSignalAdded(IBehavioralFeature feature, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreHandledSignalRemoved(IBehavioralFeature feature, ISignal proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onHandledSignalRemoved(IBehavioralFeature feature, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreParameterAdded(IBehavioralFeature feature, IParameter parm, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onParameterAdded(IBehavioralFeature feature, IParameter parm, IResultCell cell)
    {
        postElementModifiedEvent(feature, null, ModelElementChangedKind.PARAMETER_CHANGED);
    }
    
    public void onPreParameterRemoved(IBehavioralFeature feature, IParameter parm, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onParameterRemoved(IBehavioralFeature feature, IParameter parm, IResultCell cell)
    {
        postElementModifiedEvent(feature, null, ModelElementChangedKind.PARAMETER_CHANGED);
    }
    
    public void onPreAbstractModified(IBehavioralFeature feature, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onAbstractModified(IBehavioralFeature feature, IResultCell cell)
    {
        postElementModifiedEvent(feature, null, ModelElementChangedKind.ABSTRACTMODIFIED);
    }
    
    public void onPreStrictFPModified(IBehavioralFeature feature, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onStrictFPModified(IBehavioralFeature feature, IResultCell cell)
    {
        //nothing to do
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreFinalModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onPreFinalModified(IRedefinableElement element, boolean proposedValue, IResultCell cell)
    {
        // TODO Auto-generated method stub
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onFinalModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onFinalModified(IRedefinableElement element, IResultCell cell)
    {
        // TODO Auto-generated method stub
    }
    
    public void onPreRedefinedElementAdded(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onRedefinedElementAdded(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreRedefinedElementRemoved(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onRedefinedElementRemoved(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IResultCell cell)
    {
        postElementModifiedEvent(redefiningElement, null,
                ModelElementChangedKind.REDEFINING_ELEMENT_REMOVED);
    }
    
    public void onPreRedefiningElementAdded(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onRedefiningElementAdded(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreRedefiningElementRemoved(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onRedefiningElementRemoved(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreChangeRepresentingClassifier(ILifeline pLifeline, ITypedElement pRepresents, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onChangeRepresentingClassifier(ILifeline pLifeline, ITypedElement pRepresents, IResultCell cell)
    {
        postElementModifiedEvent(pLifeline, null, ModelElementChangedKind.REPRESENTING_CLASSIFIER_CHANGED);
    }
    
    public void onPreStaticModified(IFeature feature, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onStaticModified(IFeature feature, IResultCell cell)
    {
        postElementModifiedEvent(feature, null, ModelElementChangedKind.STATIC_MODIFIED);
    }
    
    public void onPreNativeModified(IFeature feature, boolean proposedValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onNativeModified(IFeature feature, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onCoreProductPreInit(ICoreProduct pVal, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onCoreProductPreQuit(ICoreProduct pVal, IResultCell cell)
    {
        // TODO: Handle the product quitting.  This will most likely be handed
        //       by the diagram closing.
//        m_DrawingAreaControl.preQuit();
    }
    
    public void onCoreProductPreSaved(ICoreProduct pVal, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onCoreProductSaved(ICoreProduct newVal, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreWeightModified(IActivityEdge pEdge, String newValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onWeightModified(IActivityEdge pEdge, IResultCell cell)
    {
        postElementModifiedEvent(pEdge, null,
                ModelElementChangedKind.ACTIVITYEDGE_WEIGHTMODIFIED);
    }
    
    public void onPreGuardModified(IActivityEdge pEdge, String newValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onGuardModified(IActivityEdge pEdge, IResultCell cell)
    {
        postElementModifiedEvent(pEdge, null, ModelElementChangedKind.ACTIVITYEDGE_GUARDMODIFIED);
    }
    
    public void onPreModeModified(IProject pProject, String newValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onModeModified(IProject pProject, IResultCell cell)
    {
        // We no longer allow a user to change the projects mode.
        
        //      if (m_DrawingAreaControl != null && pProject != null)
        //      {
        //         if(m_DrawingAreaControl.get() != null)
        //         {
        //            // These are expensive operations so make sure we only post it to diagrams in the project
        //            // that changed by passing true as the last parameter.
        //            postElementModifiedEvent(pProject, null, ModelElementChangedKind.MECK_PROJECT_MODEMODIFIED, true);
        //         }
        //      }
    }
    
    public void onPreDefaultLanguageModified(IProject pProject, String newValue, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onDefaultLanguageModified(IProject pProject, IResultCell cell)
    {
        // We need to think about this one and see if there should be anything
        // we need to do.
        //      if (m_DrawingAreaControl != null && pProject != null)
        //      {
        //         if(m_DrawingAreaControl.get() != null)
        //         {
        //            // These are expensive operations so make sure we only post it to diagrams in the project
        //            // that changed by passing true as the last parameter.
        //            postElementModifiedEvent(pProject, null, ModelElementChangedKind.MECK_PROJECT_LANGUAGEMODIFIED, true);
        //         }
        //      }
    }
    
    public void onProjectPreCreate(IWorkspace space, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onProjectCreated(IProject Project, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onProjectOpened(IProject Project, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onProjectPreRename(IProject Project, String newName, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onProjectRenamed(IProject Project, String oldName, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onProjectPreClose(IProject Project, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onProjectClosed(IProject Project, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onProjectPreSave(IProject project, IResultCell cell)
    {
        // TODO: Save the diagram when the project is saved.
//        if (m_DrawingAreaControl != null)
//        {
//            ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
//            if(ctrl != null)
//            {
//                IProject proj = ctrl.getProject();
//                if (proj != null && proj.isSame(project))
//                {
//                    if (ctrl instanceof ITwoPhaseCommit)
//                    {
//                        ITwoPhaseCommit pTwoPhase = (ITwoPhaseCommit)ctrl;
//                        boolean isDirty = pTwoPhase.isDirty();
//                        if (isDirty)
//                        {
//                            pTwoPhase.preCommit();
//                        }
//                    }
//                }
//            }
//        }
    }
    
    public void onProjectSaved(IProject project, IResultCell cell)
    {
        // TODO: Save the diagram when the project is saved.
        
//        if (m_DrawingAreaControl != null)
//        {
//            ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
//            if(ctrl != null)
//            {
//                IProject proj = ctrl.getProject();
//                if (proj != null && proj.isSame(project))
//                {
//                    if (ctrl instanceof ITwoPhaseCommit)
//                    {
//                        ITwoPhaseCommit pTwoPhase = (ITwoPhaseCommit)ctrl;
//                        // This won't do anything if precommit was not called
//                        pTwoPhase.commit();
//                    }
//                }
//            }
//        }
    }
    
    public void onPreReferencedLibraryAdded(IProject Project, String refLibLoc, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onReferencedLibraryAdded(IProject Project, String refLibLoc, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreReferencedLibraryRemoved(IProject Project, String refLibLoc, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onReferencedLibraryRemoved(IProject Project, String refLibLoc, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onPreQualifierAttributeAdded(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onQualifierAttributeAdded(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
    {
        postElementModifiedEvent(pEnd.getAssociation(), pEnd, ModelElementChangedKind.QUALIFIER_ADDED);
    }
    
    public void onPreQualifierAttributeRemoved(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
    {
        //nothing to do
    }
    
    public void onQualifierAttributeRemoved(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
    {
        postElementModifiedEvent(pEnd, pAttr, ModelElementChangedKind.QUALIFIER_REMOVED);
    }

    public void onPreTransform(IAssociationEnd pEnd, String newForm, IResultCell cell)
    {
        //nothing to do
    }

    public void onTransformed(IAssociationEnd pEnd, IResultCell cell)
    {
        postElementModifiedEvent(pEnd.getAssociation(), null, ModelElementChangedKind.ASSOCIATION_END_TRANDFORMED);
    }
}


