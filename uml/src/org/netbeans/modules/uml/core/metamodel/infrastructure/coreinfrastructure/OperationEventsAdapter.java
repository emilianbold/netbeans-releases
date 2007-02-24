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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 * @author Trey Spiva
 */
public class OperationEventsAdapter implements IOperationEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onConditionPreAdded(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.foundation.IConstraint, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onConditionPreAdded(
      IOperation oper,
      IConstraint condition,
      boolean isPreCondition,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onConditionAdded(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.foundation.IConstraint, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onConditionAdded(
      IOperation oper,
      IConstraint condition,
      boolean isPreCondition,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onConditionPreRemoved(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.foundation.IConstraint, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onConditionPreRemoved(
      IOperation oper,
      IConstraint condition,
      boolean isPreCondition,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onConditionRemoved(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.foundation.IConstraint, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onConditionRemoved(
      IOperation oper,
      IConstraint condition,
      boolean isPreCondition,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onPreQueryModified(com.embarcadero.describe.coreinfrastructure.IOperation, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreQueryModified(
      IOperation oper,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onQueryModified(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onQueryModified(IOperation oper, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreAdded(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRaisedExceptionPreAdded(
      IOperation oper,
      IClassifier pException,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onRaisedExceptionAdded(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRaisedExceptionAdded(
      IOperation oper,
      IClassifier pException,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreRemoved(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRaisedExceptionPreRemoved(
      IOperation oper,
      IClassifier pException,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onRaisedExceptionRemoved(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRaisedExceptionRemoved(
      IOperation oper,
      IClassifier pException,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onPreOperationPropertyModified(com.embarcadero.describe.coreinfrastructure.IOperation, int, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreOperationPropertyModified(
      IOperation oper,
      int nKind,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onOperationPropertyModified(com.embarcadero.describe.coreinfrastructure.IOperation, int, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onOperationPropertyModified(
      IOperation oper,
      int nKind,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

}
