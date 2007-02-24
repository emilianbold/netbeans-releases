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



package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;


/**
 *
 * @author Trey Spiva
 */
public class ElementLifeTimeEventsAdapter implements IElementLifeTimeEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.foundation.IElementLifeTimeEventsSink#onElementPreCreate(java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onElementPreCreate(String ElementType, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.foundation.IElementLifeTimeEventsSink#onElementCreated(com.embarcadero.describe.foundation.IVersionableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onElementCreated(IVersionableElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.foundation.IElementLifeTimeEventsSink#onElementPreDelete(com.embarcadero.describe.foundation.IVersionableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onElementPreDelete(
      IVersionableElement element,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.foundation.IElementLifeTimeEventsSink#onElementDeleted(com.embarcadero.describe.foundation.IVersionableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onElementDeleted(IVersionableElement element, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.foundation.IElementLifeTimeEventsSink#onElementPreDuplicated(com.embarcadero.describe.foundation.IVersionableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onElementPreDuplicated(
      IVersionableElement element,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.foundation.IElementLifeTimeEventsSink#onElementDuplicated(com.embarcadero.describe.foundation.IVersionableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onElementDuplicated(
      IVersionableElement element,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

}
