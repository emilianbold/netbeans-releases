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



package org.netbeans.modules.uml.ui.controls.drawingarea;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
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
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.ITwoPhaseCommit;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManagementException;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.core.scm.ISCMEventsSink;
import org.netbeans.modules.uml.core.scm.ISCMItemGroup;
import org.netbeans.modules.uml.core.scm.ISCMOptions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.NotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.DrawingAreaEventsAdapter;
import org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink;
import org.netbeans.modules.uml.ui.swing.trackbar.JTrackBar;
import com.tomsawyer.drawing.geometry.TSConstRect;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;

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
      ICompartmentEventsSink,
      ITypedElementEventsSink,
      IRelationEventsSink,
      INamedElementEventsSink,
      IExternalElementEventsSink,
      IWSElementEventsSink,
      IAffectedElementEventsSink,
      IStereotypeEventsSink,
      INamespaceModifiedEventsSink,
      ISCMEventsSink,
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
      IAssociationEndEventsSink
{
   //private ADDrawingAreaControl m_DrawingAreaControl = null;
   private WeakReference m_DrawingAreaControl = null;

   /**
    * 
    */
   public DrawingAreaEventHandler()
   {
      super();
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
      if ((element instanceof IElement) && (m_DrawingAreaControl != null))
      {
          IElement pEle = (IElement)element;
          IProject ownerProject = pEle.getProject();
          
          ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
          if((ctrl != null) && (ownerProject.isSame(ctrl.getProject()) == true))
          {
              if(element instanceof IElementImport)
              {
                  // The call to getImportElement acutally retrieves the
                  // cloned element that lives in the importing project, not the
                  // element that is in the original project.
                  IElement clone = ((IElementImport)element).getImportedElement();
                  IElement origOwner = clone.getOwner();
                  pEle = FactoryRetriever.instance().findElementByID(origOwner,
                          clone.getXMIID());
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
              
              ETList < IPresentationElement > elems = ctrl.getAllItems2(pEle);
              if (elems != null)
              {
                  ctrl.postDeletePresentationElements(elems);
              }
          }
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
          
         if(element instanceof IElementImport)
         {
              IProject ownerProject = pModEle.getProject();              
              ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
              if((ctrl != null) && (ownerProject.isSame(ctrl.getProject()) == true))
              {
                 // The call to getImportElement acutally retrieves the
                 // cloned element that lives in the importing project, not the
                 // element that is in the original project.
                 IElement clone = ((IElementImport)element).getImportedElement();
                 IElement origOwner = clone.getOwner();
                 pModEle = FactoryRetriever.instance().findElementByID(origOwner, clone.getXMIID());
              }
         }
         
         postElementDeletedEvent(pModEle, null);
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
      postElementModifiedEvent(classifier, feature, ModelElementChangedKind.MECK_FEATUREADDED, false);
   }

   public void onEnumerationLiteralPreAdded(IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
   {
      //nothing to do
   }

   public void onEnumerationLiteralAdded(IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
   {
      postElementModifiedEvent(classifier, enumLit, ModelElementChangedKind.MECK_FEATUREADDED, false);
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
	   // the logic should be the same as implemented in onFeatureMoved,
      onFeatureMoved(classifier, feature, cell);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onFeaturePreMoved(IClassifier classifier, IFeature feature, IResultCell cell)
   {
      //nothing to do
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
         }

         // notify the target
         IClassifier pClass = feature.getFeaturingClassifier();
         if (pClass != null && pClass instanceof IElement)
         {
            IElement pEle = (IElement)pClass;
            postElementModifiedEvent(pEle, feature, ModelElementChangedKind.MECK_FEATUREMOVED, false);
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
      postElementModifiedEvent(pNewClassifier, pNewFeature, ModelElementChangedKind.MECK_FEATUREDUPLICATEDTOCLASSIFIER, false);
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
         postElementModifiedEvent(feature, null, ModelElementChangedKind.MECK_ABSTRACTMODIFIED, false);
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
      //nothing to do
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
      //nothing to do
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
            postElementModifiedEvent(elem, null, ModelElementChangedKind.MECK_ELEMENTMODIFIED, false);
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
   private void postElementModifiedEvent(IElement pElement, IElement pSecondaryElement, int nKind, boolean bVerifyDiagramAndElementInSameProject)
   {
      
      if ((m_DrawingAreaControl == null) || (m_DrawingAreaControl.get() == null)) 
         return;

      ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
      if ((pElement == null && pSecondaryElement == null) || m_AffectdXMIID.contains(pElement.getXMIID()) == true)
      {
         return;
      }

      m_AffectdXMIID.add(pElement.getXMIID());
      boolean proceed = true;
      if (bVerifyDiagramAndElementInSameProject)
      {
         // Get the project off the element
         IProject pElemProj = pElement.getProject();
         if (pElemProj != null)
         {
            // Get the project off the diagram
            IProject pDiaProj = ctrl.getProject();
            if (pDiaProj != null)
            {
               if (!pDiaProj.isSame(pElemProj))
               {
                  proceed = false;
               }
            }
         }
      }
      NotificationTargets targets = new NotificationTargets();

      targets.setKind(nKind);
      if (pElement != null)
         targets.setChangedModelElement(pElement);
      if (pSecondaryElement != null)
         targets.setSecondaryChangedModelElement(pSecondaryElement);

      class ElementModified implements Runnable
      {
         private NotificationTargets m_targets = null;
         public ElementModified(NotificationTargets targets)
         {
            m_targets = targets;
         }
         public void run()
         {
            if (m_DrawingAreaControl != null)
            {
               ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
               if(ctrl != null)
               {
                  ctrl.elementModified(m_targets);
               }
            }

            m_AffectdXMIID.clear();
         }
      }

      
      //we just want to run this event instead of doing it later, because we were not updating 
      //drawing area if events are ElementModified - in our case the first event that comes is
      //element modified and then other events are not run and hence drawing area is not updated.
      SwingUtilities.invokeLater(new ElementModified(targets)); //.run();
      m_AffectdXMIID.clear();
      //SwingUtilities.invokeLater(new ElementModified(targets));
   }

   private ArrayList m_AffectedModelElements = new ArrayList();
   private ArrayList m_AffectdXMIID = new ArrayList();
   protected synchronized void addChangedModelElement(IElement modelElement)
   {
      if (modelElement != null)
      {
         if (m_AffectdXMIID.contains(modelElement.getXMIID()) == false)
         {
            m_AffectedModelElements.add(modelElement);
            m_AffectdXMIID.add(modelElement.getXMIID());
         }

         if (m_AffectedModelElements.size() == 1)
         {
            SwingUtilities.invokeLater(new Runnable()
            {

               public void run()
               {
                  if (m_AffectedModelElements.size() > 0)
                  {
                     updateDiagram();
                  }
               }
            });
         }
      }
   }

   protected void updateDiagram()
   {
      try
      {
         for (Iterator modelElements = m_AffectedModelElements.iterator(); modelElements.hasNext();)
         {
            IElement curModelElement = (IElement)modelElements.next();

            ETList < IPresentationElement > pElements = curModelElement.getPresentationElements();
            for (Iterator < IPresentationElement > iter = pElements.iterator(); iter.hasNext();)
            {
               IPresentationElement curElement = iter.next();

               IETRect preboundingRect = TypeConversions.getLogicalBoundingRect(curElement);
               IDrawEngine curDrawEngine = TypeConversions.getDrawEngine(curElement);

               if (curDrawEngine != null)
               {
                  curDrawEngine.init();
                  curDrawEngine.sizeToContents();

                  IETRect boundingRect = TypeConversions.getLogicalBoundingRect(curElement);

                  boundingRect.unionWith(preboundingRect);

                  IETNode node = TypeConversions.getETNode(curElement);
                  if (node != null)
                  {
                     boundingRect.unionWith(node.getEdgeBounds());
                     TSConstRect nodeBounds = node.getBounds();
                     //                     ETSystem.out.println("Node Bounds = [x=" + nodeBounds.getLeft() + ", y=" + nodeBounds.getTop() + ", right=" + nodeBounds.getRight() + ", bottom=" + nodeBounds.getBottom());
                  }

                  boundingRect.inflate(10);
                  
                  if(m_DrawingAreaControl != null)
                  {
                     ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
                     if(ctrl != null)
                     {
                        ctrl.refreshRect(boundingRect);
   
                        //                  TSEGraphics g = m_DrawingAreaControl.getGraphWindow().newGraphics(m_DrawingAreaControl.getGraphWindow().getGraphics());
                        //                  
                        //                  TSConstRect rect = new TSConstRect(boundingRect.getLeft(), boundingRect.getTop(), boundingRect.getRight(), boundingRect.getBottom());
                        //                  ETSystem.out.println("Refresh Bounds = [x=" + rect.getLeft() + ", y=" + rect.getTop() + ", right=" + rect.getRight() + ", bottom=" + rect.getBottom());
                        //                  g.setColor(java.awt.Color.BLUE);
                        //                  g.drawRect(rect);
      
                        IETGraphObject curObject = TypeConversions.getETGraphObject(curElement);
                        if (curObject != null)
                        {
                           curObject.modelElementHasChanged(null);
                        }
      
                        JTrackBar bar = ctrl.getTrackBar();
                        if (bar != null)
                        {
                           bar.updateName(curElement);
                        }
                     }
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         m_AffectedModelElements.clear();
         m_AffectdXMIID.clear();
      }
   }

   

   /**
    * Post an element deleted event
    */
   private void postElementDeletedEvent(IElement pElement, IElement pSecondaryElement)
   {
      if (m_DrawingAreaControl != null)
      {
         ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
         if(ctrl != null)
         {
            IEventSinkAction pAction = new EventSinkAction();
            INotificationTargets pTargets = new NotificationTargets();
            if (pElement != null || pSecondaryElement != null)
            {
               if (pElement != null)
               {
                  pTargets.setChangedModelElement(pElement);
               }
               if (pSecondaryElement != null)
               {
                  pTargets.setSecondaryChangedModelElement(pSecondaryElement);
               }
               pTargets.setKind(ModelElementChangedKind.MECK_ELEMENTDELETED);
               pAction.setKind(EventSinkActionKind.ESAK_ELEMENTDELETED);
               pAction.setTargets(pTargets);
               ctrl.postDelayedAction(pAction);
            }
         }
      }
   }

   //	/ Post an element transformed event
   private void postElementTransformedEvent(IElement pElement)
   {
	if (m_DrawingAreaControl != null && pElement != null)
	{
      ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
      if(ctrl != null)
      {
   	   IEventSinkAction pAction = new EventSinkAction();
   	   INotificationTargets pTargets = new NotificationTargets();
   	   pTargets.setChangedModelElement(pElement);
   	   pTargets.setKind(ModelElementChangedKind.MECK_ELEMENTTRANSFORMED);
   	   pAction.setKind(EventSinkActionKind.ESAK_ELEMENTTRANSFORMED);
   	   pAction.setTargets(pTargets);
   	   ctrl.postDelayedAction(pAction);
      }
	}
	/* Need to verify what's going on
   	  if (pElement != null)
   	  {
		IEventSinkAction pAction = new EventSinkAction();
		INotificationTargets pTargets = new NotificationTargets();
		pTargets.setChangedModelElement(pElement);
		pTargets.setKind(ModelElementChangedKind.MECK_ELEMENTTRANSFORMED);
		pAction.setKind(EventSinkActionKind.ESAK_ELEMENTTRANSFORMED);
		pAction.setTargets(pTargets);
   	  	final IEventSinkAction pEle = pAction;
		SwingUtilities.invokeLater(new Runnable() 
		{
		  public void run() {
			  if (m_DrawingAreaControl != null)
			  {
				 m_DrawingAreaControl.postDelayedAction(pEle);
			  }
		  }
		});
   	  }
   	  */
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink#onCompartmentSelected(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onCompartmentSelected(ICompartment pCompartment, boolean bSelected, IResultCell cell)
   {
      //nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink#onCompartmentCollapsed(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onCompartmentCollapsed(ICompartment pCompartment, boolean bCollapsed, IResultCell cell)
   {
      //nothing to do
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
         postElementModifiedEvent(element, null, ModelElementChangedKind.MECK_MULTIPLICITYMODIFIED, false);
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
         postElementModifiedEvent(element, null, ModelElementChangedKind.MECK_TYPEMODIFIED, false);
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
         postElementModifiedEvent(mult, null, ModelElementChangedKind.MECK_LOWERMODIFIED, false);
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
         postElementModifiedEvent(mult, null, ModelElementChangedKind.MECK_UPPERMODIFIED, false);
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
//      if (mult != null && mult instanceof IFeature)
//      {
//         IFeature pFeature = (IFeature)element;
         postElementModifiedEvent(mult, null, ModelElementChangedKind.MECK_RANGEADDED, false);
//      }      
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
//      if (mult != null && mult instanceof IFeature)
//      {
//         IFeature pFeature = (IFeature)mult;
         postElementModifiedEvent(mult, null, ModelElementChangedKind.MECK_RANGEREMOVED, false);
//      }
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
         postElementModifiedEvent(mult, null, ModelElementChangedKind.MECK_ORDERMODIFIED, false);
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
            postElementModifiedEvent(pEle, null, ModelElementChangedKind.MECK_RELATIONENDMODIFIED, false);
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
            postElementModifiedEvent(pEle, null, ModelElementChangedKind.MECK_RELATIONENDADDED, false);
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
            postElementModifiedEvent(pEle, null, ModelElementChangedKind.MECK_RELATIONENDREMOVED, false);
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
            postElementModifiedEvent(pEle, null, ModelElementChangedKind.MECK_RELATIONCREATED, false);
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
            postElementModifiedEvent(pEle, null, ModelElementChangedKind.MECK_RELATIONDELETED, false);
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

      IFeature pFeat = null;
      if (element instanceof IFeature)
      {
         pFeat = (IFeature)element;
      }

      postElementModifiedEvent(element, pFeat, ModelElementChangedKind.MECK_NAMEMODIFIED, false);
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
         postElementModifiedEvent(element, pFeat, ModelElementChangedKind.MECK_VISIBILITYMODIFIED, false);
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
         postElementModifiedEvent(element, pFeat, ModelElementChangedKind.MECK_ALIASNAMEMODIFIED, false);
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
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onExternalElementPreLoaded(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onExternalElementPreLoaded(String uri, IResultCell cell)
   {
      //nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onExternalElementLoaded(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onExternalElementLoaded(IVersionableElement element, IResultCell cell)
   {
      if ((m_DrawingAreaControl != null) && (element != null))
      {
         Object obj = m_DrawingAreaControl.get();         
         if(obj instanceof ADDrawingAreaControl)
         {
            ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
            ctrl.reestablishPresentationElementOwnership(element);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onPreInitialExtraction(java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPreInitialExtraction(String fileName, IVersionableElement element, IResultCell cell)
   {
      //nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onInitialExtraction(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onInitialExtraction(IVersionableElement element, IResultCell cell)
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
      if (m_DrawingAreaControl != null && element != null)
      {
         ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
         if(ctrl != null)
         {
            try
            {
               String sLocation = element.getLocation();
               String sFilename = ctrl.getFilename();
               if (sLocation != null && sFilename != null)
               {
                  if (sLocation.equalsIgnoreCase(sFilename))
                  {
                     // This diagram has been removed.
                     IProductDiagramManager pDiaMan = ProductHelper.getProductDiagramManager();
                     if (pDiaMan != null)
                     {
                        pDiaMan.closeDiagram(sLocation);
                     }
                  }
               }
            }
            catch (WorkspaceManagementException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }
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
      if (impacted != null)
      {
         Vector < IFeature > featureMap = new Vector < IFeature > ();
         // The ones we're interested in are attribute and parameter changes.  In those cases we need to
         // alert the correct classifier.
         int count = impacted.size();
         for (int i = 0; i < count; i++)
         {
            IVersionableElement pEle = impacted.get(i);
            IFeature pFeatureToNotify = null;
            if (pEle instanceof IAttribute)
            {
               pFeatureToNotify = (IAttribute)pEle;
            }
            else if (pEle instanceof IParameter)
            {
               IElement elem = ((IParameter)pEle).getOwner();
               if (elem != null && elem instanceof IOperation)
               {
                  pFeatureToNotify = (IOperation)elem;
               }
            }

            if (pFeatureToNotify != null)
            {
               featureMap.add(pFeatureToNotify);
            }
         }

         // Now issue an element modified for all the classifiers
         if (featureMap.size() > 0)
         {
            int num = featureMap.size();
            for (int j = 0; j < num; j++)
            {
               IFeature pFeat = featureMap.get(j);
               IClassifier pClass = pFeat.getFeaturingClassifier();
               if (pClass != null)
               {
                  postElementModifiedEvent(pClass, pFeat, ModelElementChangedKind.MECK_IMPACTED, false);
               }
            }
         }
      }
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
         postElementModifiedEvent(element, pFeat, ModelElementChangedKind.MECK_STEREOTYPEAPPLIED, false);
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
         postElementModifiedEvent(element, pFeat, ModelElementChangedKind.MECK_STEREOTYPEDELETED, false);
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
         postElementModifiedEvent(space, elementAdded, ModelElementChangedKind.MECK_ELEMENTADDEDTONAMESPACE, false);
      }
   }

   public void onPreFeatureExecuted(int kind, ISCMItemGroup Group, ISCMOptions pOptions, IResultCell cell)
   {
      //nothing to do
   }

   public void onFeatureExecuted(int kind, ISCMItemGroup files, ISCMOptions pOptions, IResultCell cell)
   {
      if (m_DrawingAreaControl != null) 
      {
         ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
         if(ctrl != null)
         {
            ctrl.onFeatureExecuted(kind, files);
         }
      }
   }

   public void onPreDisposeElements(ETList < IVersionableElement > pElements, IResultCell cell)
   {
      //nothing to do
   }

   public void onDisposedElements(ETList < IVersionableElement > pElements, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && pElements != null)
      {
         ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
         if(ctrl != null)
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
      if (m_DrawingAreaControl != null && feature != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(feature, null, ModelElementChangedKind.MECK_DEFAULTMODIFIED, false);
         }
      }
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
      if (m_DrawingAreaControl != null && feature != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(feature, null, ModelElementChangedKind.MECK_DERIVEDMODIFIED, false);
         }
      }
   }

   public void onPrePrimaryKeyModified(IAttribute feature, boolean proposedValue, IResultCell cell)
   {
      //nothing to do
   }

   public void onPrimaryKeyModified(IAttribute feature, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && feature != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {   
            postElementModifiedEvent(feature, null, ModelElementChangedKind.MECK_PRIMARYKEYMODIFIED, false); 
         } 
      }
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
      if (m_DrawingAreaControl != null && oper != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(oper, null, ModelElementChangedKind.MECK_OPERATION_PROPERTY_CHANGE, false);
         }
      }
   }

   public void onPreDefaultExpModified(IParameter feature, IExpression proposedValue, IResultCell cell)
   {
      //nothing to do
   }

   public void onDefaultExpModified(IParameter feature, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && feature != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(feature, null, ModelElementChangedKind.MECK_DEFAULTEXPMODIFIED, false);
         }
      }
   }

   public void onPreDefaultExpBodyModified(IParameter feature, String bodyValue, IResultCell cell)
   {
      //nothing to do
   }

   public void onDefaultExpBodyModified(IParameter feature, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && feature != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(feature, null, ModelElementChangedKind.MECK_DEFAULTEXPBODYMODIFIED, false);
         }
      }
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
      if (m_DrawingAreaControl != null && feature != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(feature, null, ModelElementChangedKind.MECK_PARAMETERADDED, false);
         }
      }
   }

   public void onPreParameterRemoved(IBehavioralFeature feature, IParameter parm, IResultCell cell)
   {
      //nothing to do
   }

   public void onParameterRemoved(IBehavioralFeature feature, IParameter parm, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && feature != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(feature, null, ModelElementChangedKind.MECK_PARAMETERREMOVED, false);
         }
      }
   }

   public void onPreAbstractModified(IBehavioralFeature feature, boolean proposedValue, IResultCell cell)
   {
      //nothing to do
   }

   public void onAbstractModified(IBehavioralFeature feature, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && feature != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(feature, null, ModelElementChangedKind.MECK_ABSTRACTMODIFIED, false);
         }
      }
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
      if (m_DrawingAreaControl != null && redefiningElement != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(redefiningElement, null, ModelElementChangedKind.MECK_REDEFININGELEMENTREMOVED, false);
         }
      }
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
      if (m_DrawingAreaControl != null && pLifeline != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(pLifeline, null, ModelElementChangedKind.MECK_REPRESENTINGCLASSIFIERCHANGED, false);
         }
      }
   }

   public void onPreStaticModified(IFeature feature, boolean proposedValue, IResultCell cell)
   {
      //nothing to do
   }

   public void onStaticModified(IFeature feature, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && feature != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(feature, null, ModelElementChangedKind.MECK_STATICMODIFIED, false);
         }
      }
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
      if (m_DrawingAreaControl != null)
      {
         //m_DrawingAreaControl.preQuit();
      }
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
      if (m_DrawingAreaControl != null && pEdge != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(pEdge, null, ModelElementChangedKind.MECK_ACTIVITYEDGE_WEIGHTMODIFIED, false);
         }
      }
   }

   public void onPreGuardModified(IActivityEdge pEdge, String newValue, IResultCell cell)
   {
      //nothing to do
   }

   public void onGuardModified(IActivityEdge pEdge, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && pEdge != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(pEdge, null, ModelElementChangedKind.MECK_ACTIVITYEDGE_GUARDMODIFIED, false);
         }
      }
   }

   public void onPreModeModified(IProject pProject, String newValue, IResultCell cell)
   {
      //nothing to do
   }

   public void onModeModified(IProject pProject, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && pProject != null)
      {         
         if(m_DrawingAreaControl.get() != null)
         {
            // These are expensive operations so make sure we only post it to diagrams in the project
            // that changed by passing true as the last parameter.
            postElementModifiedEvent(pProject, null, ModelElementChangedKind.MECK_PROJECT_MODEMODIFIED, true);
         }
      }
   }

   public void onPreDefaultLanguageModified(IProject pProject, String newValue, IResultCell cell)
   {
      //nothing to do
   }

   public void onDefaultLanguageModified(IProject pProject, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && pProject != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            // These are expensive operations so make sure we only post it to diagrams in the project
            // that changed by passing true as the last parameter.
            postElementModifiedEvent(pProject, null, ModelElementChangedKind.MECK_PROJECT_LANGUAGEMODIFIED, true);
         }
      }
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
      if (m_DrawingAreaControl != null)
      {
         ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
         if(ctrl != null)
         {
            IProject proj = ctrl.getProject();
            if (proj != null && proj.isSame(project))
            {
               if (ctrl instanceof ITwoPhaseCommit)
               {
                  ITwoPhaseCommit pTwoPhase = (ITwoPhaseCommit)ctrl;
                  boolean isDirty = pTwoPhase.isDirty();
                  if (isDirty)
                  {
                     pTwoPhase.preCommit();
                  }
               }
            }
         }
      }
   }

   public void onProjectSaved(IProject project, IResultCell cell)
   {
      if (m_DrawingAreaControl != null)
      {
         ADDrawingAreaControl ctrl = (ADDrawingAreaControl)m_DrawingAreaControl.get();
         if(ctrl != null)
         {
            IProject proj = ctrl.getProject();
            if (proj != null && proj.isSame(project))
            {
               if (ctrl instanceof ITwoPhaseCommit)
               {
                  ITwoPhaseCommit pTwoPhase = (ITwoPhaseCommit)ctrl;
                  // This won't do anything if precommit was not called
                  pTwoPhase.commit();
               }
            }
         }
      }
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
      if (m_DrawingAreaControl != null && pEnd != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(pEnd, pAttr, ModelElementChangedKind.MECK_QUALIFIER_ADDED, false);
         }
      }
   }

   public void onPreQualifierAttributeRemoved(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
   {
      //nothing to do
   }

   public void onQualifierAttributeRemoved(IAssociationEnd pEnd, IAttribute pAttr, IResultCell cell)
   {
      if (m_DrawingAreaControl != null && pEnd != null)
      {
         if(m_DrawingAreaControl.get() != null)
         {
            postElementModifiedEvent(pEnd, pAttr, ModelElementChangedKind.MECK_QUALIFIER_REMOVED, false);
         }
      }
   }

   public void setDrawingAreaControl(ADDrawingAreaControl control)
   {
      m_DrawingAreaControl = new WeakReference(control);
   }
}


