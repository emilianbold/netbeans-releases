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



package org.netbeans.modules.uml.ui.support.visitors;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;

/**
 * @author KevinM
 *
 *	If any graphObject firstSubject model element is the same as the input model element,
 * it's presentation element gets appended to the list.
 */
public class ETFirstSubjectSameVistor implements IETGraphObjectVisitor
{

   protected IElement modelElement;
   protected ETList < IPresentationElement > presentationElements;

   public ETFirstSubjectSameVistor(ETList < IPresentationElement > pPES, IElement modelEle)
   {
      modelElement = modelEle;
      presentationElements = pPES;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.visitors.IETGraphObjectVisitor#visit(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject)
    */
   public boolean visit(IETGraphObject object)
   {
      IPresentationElement presItem = object.getPresentationElement();
      if (presItem != null && modelElement.isSame(presItem.getFirstSubject()))
      {
         presentationElements.add(presItem);
      }
      return true;
   }

}
