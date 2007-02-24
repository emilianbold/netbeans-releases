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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 * @author Trey Spiva
 */
public class TypedElementEventsAdapter implements ITypedElementEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onPreMultiplicityModified(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreMultiplicityModified(
      ITypedElement element,
      IMultiplicity proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onMultiplicityModified(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onMultiplicityModified(ITypedElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onPreTypeModified(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreTypeModified(
      ITypedElement element,
      IClassifier proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onTypeModified(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onTypeModified(ITypedElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onPreLowerModified(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, com.embarcadero.describe.foundation.IMultiplicityRange, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreLowerModified(
      ITypedElement element,
      IMultiplicity mult,
      IMultiplicityRange range,
      String proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onLowerModified(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, com.embarcadero.describe.foundation.IMultiplicityRange, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onLowerModified(
      ITypedElement element,
      IMultiplicity mult,
      IMultiplicityRange range,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onPreUpperModified(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, com.embarcadero.describe.foundation.IMultiplicityRange, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreUpperModified(
      ITypedElement element,
      IMultiplicity mult,
      IMultiplicityRange range,
      String proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onUpperModified(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, com.embarcadero.describe.foundation.IMultiplicityRange, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onUpperModified(
      ITypedElement element,
      IMultiplicity mult,
      IMultiplicityRange range,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onPreRangeAdded(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, com.embarcadero.describe.foundation.IMultiplicityRange, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreRangeAdded(
      ITypedElement element,
      IMultiplicity mult,
      IMultiplicityRange range,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onRangeAdded(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, com.embarcadero.describe.foundation.IMultiplicityRange, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRangeAdded(
      ITypedElement element,
      IMultiplicity mult,
      IMultiplicityRange range,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onPreRangeRemoved(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, com.embarcadero.describe.foundation.IMultiplicityRange, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreRangeRemoved(
      ITypedElement element,
      IMultiplicity mult,
      IMultiplicityRange range,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onRangeRemoved(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, com.embarcadero.describe.foundation.IMultiplicityRange, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRangeRemoved(
      ITypedElement element,
      IMultiplicity mult,
      IMultiplicityRange range,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onPreOrderModified(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreOrderModified(
      ITypedElement element,
      IMultiplicity mult,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.ITypedElementEventsSink#onOrderModified(com.embarcadero.describe.coreinfrastructure.ITypedElement, com.embarcadero.describe.foundation.IMultiplicity, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onOrderModified(
      ITypedElement element,
      IMultiplicity mult,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

}
