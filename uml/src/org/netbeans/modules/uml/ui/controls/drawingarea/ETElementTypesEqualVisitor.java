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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.support.visitors.IETGraphObjectVisitor;

/**
 * @author KevinM
 * Helper visiter for performance, in getAllByType,
 * If the visited object element type equals the type, it gets appended to elements list.
 */
public class ETElementTypesEqualVisitor implements IETGraphObjectVisitor
{
   protected String m_elementType;
   protected ETList < IPresentationElement > presentationElements;

   /*
    * If the visited object element type equals the type, it gets appended to elements list.
    */
   public ETElementTypesEqualVisitor(ETList < IPresentationElement > elements, final String type)
   {
      m_elementType = type;
      presentationElements = elements;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IETGraphObjectVisitor#visit(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject)
    */
   public boolean visit(IETGraphObject object)
   {
      IElement element = TypeConversions.getElement(object);
      if( element != null )
      {
         String elementType = element.getElementType();
         if ( (elementType != null) &&
              elementType.equals(m_elementType) )
         {
            presentationElements.add( object.getPresentationElement() );
         }
      }

      return true;
   }

}
