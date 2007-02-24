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


/**
 *
 * @author Trey Spiva
 */
public class ClassifierFeatureEventsAdapter
   implements IClassifierFeatureEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreAdded(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeaturePreAdded(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureAdded(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeatureAdded(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreRemoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeaturePreRemoved(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureRemoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeatureRemoved(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreMoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeaturePreMoved(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureMoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeatureMoved(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreDuplicatedToClassifier(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeaturePreDuplicatedToClassifier(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureDuplicatedToClassifier(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeatureDuplicatedToClassifier(
      IClassifier pOldClassifier,
      IFeature pOldFeature,
      IClassifier pNewClassifier,
      IFeature pNewFeature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onPreAbstractModified(com.embarcadero.describe.coreinfrastructure.IClassifier, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreAbstractModified(
      IClassifier feature,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onAbstractModified(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onAbstractModified(IClassifier feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onPreLeafModified(com.embarcadero.describe.coreinfrastructure.IClassifier, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreLeafModified(
      IClassifier feature,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onLeafModified(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onLeafModified(IClassifier feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onPreTransientModified(com.embarcadero.describe.coreinfrastructure.IClassifier, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreTransientModified(
      IClassifier feature,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onTransientModified(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onTransientModified(IClassifier feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterAdded(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IParameterableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreTemplateParameterAdded(
      IClassifier pClassifier,
      IParameterableElement pParam,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterAdded(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IParameterableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onTemplateParameterAdded(
      IClassifier pClassifier,
      IParameterableElement pParam,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterRemoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IParameterableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreTemplateParameterRemoved(
      IClassifier pClassifier,
      IParameterableElement pParam,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterRemoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IParameterableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onTemplateParameterRemoved(
      IClassifier pClassifier,
      IParameterableElement pParam,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

    public void onEnumerationLiteralAdded(
        IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
      // TODO Auto-generated method stub
    }

    public void onEnumerationLiteralPreAdded(
        IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
      // TODO Auto-generated method stub
    }
}
