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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 * @author Trey Spiva
 */
public class AttributeEventsAdapter implements IAttributeEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onDefaultPreModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.foundation.IExpression, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDefaultPreModified(
      IAttribute attr,
      IExpression proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onDefaultModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDefaultModified(IAttribute attr, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onPreDefaultBodyModified(com.embarcadero.describe.coreinfrastructure.IAttribute, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreDefaultBodyModified(
      IAttribute feature,
      String bodyValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onDefaultBodyModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDefaultBodyModified(IAttribute feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onPreDefaultLanguageModified(com.embarcadero.describe.coreinfrastructure.IAttribute, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreDefaultLanguageModified(
      IAttribute feature,
      String language,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onDefaultLanguageModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDefaultLanguageModified(IAttribute feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onPreDerivedModified(com.embarcadero.describe.coreinfrastructure.IAttribute, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreDerivedModified(
      IAttribute feature,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onDerivedModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDerivedModified(IAttribute feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onPrePrimaryKeyModified(com.embarcadero.describe.coreinfrastructure.IAttribute, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPrePrimaryKeyModified(
      IAttribute feature,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onPrimaryKeyModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPrimaryKeyModified(IAttribute feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

}
