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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;

/**
 * @author KevinM
 *
 */
public class ETZoneCompartment extends ETNameListCompartment implements IADZoneCompartment
{
   ///////////////////////////////////////////////////////////////////////////////
   // IADListCompartment operations
   ///////////////////////////////////////////////////////////////////////////////

   /**
    * Returns the package import text (ie { From xxx }
    *
    * @param  element The element to get the package import text from
    * @result The package import text, if there is one.
    */
   public String getPackageImportText(IElement element)
   {
      if (null == element)                throw new IllegalArgumentException();
   
      // Fix W6532:  A zone compartment never needs the  { From xxx } text
      return "";
   }


   ///////////////////////////////////////////////////////////////////////////////
   // ICompartment operations
   ///////////////////////////////////////////////////////////////////////////////
   
   /**
    * This is the name of the drawengine used when storing and reading from the product archive.
    *
    * @return The unique name for this compartment.  Used when reading and writing the product archive (etlp file)
    */
   public String getCompartmentID()
   {
      return "ADZoneCompartment";
   }
   
   /**
    * Draws each of the individual compartments.
    * This does the drawing too, but you don't need to create an IETRect
    * The compartment must not draw outside this rect.  The rect is in
    * device coordinates
    *
    * @param pInfo[in] An IDrawInfo structure containing the data to draw
    * @param pBoundingRect bounding rectangle of the compartment
    */
   public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
   {
      super.draw(pDrawInfo, pBoundingRect);
      
      /* TESTING, use to see the size of the compartment's rectangle
            {
               TSEDrawInfo* pTSEDrawInfo = TypeConversions.getTSEDrawInfo(pInfo);
               final COLORREF color = rGB( 255, 255, 240 );
               CGDISupport.drawRectangle( pTSEDrawInfo.dc(), getTransform().getWinScaledOwnerRect(), color, color );
            }
      */
   }
   
	public void setLogicalOffsetInDrawEngineRect(IETPoint value)
	{
		super.setLogicalOffsetInDrawEngineRect(value);

		this.setAbsoluteOwnerOrigin(value);
	}
   
}
