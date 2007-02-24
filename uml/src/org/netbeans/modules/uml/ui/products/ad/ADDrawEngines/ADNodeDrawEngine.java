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


package org.netbeans.modules.uml.ui.products.ad.ADDrawEngines;

import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;

/**
 * @author KevinM
 *
 */
public class ADNodeDrawEngine extends ETNodeDrawEngine implements IADNodeDrawEngine {

	/**
	 * 
	 */
	public ADNodeDrawEngine() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.IADNodeDrawEngine#launchNodeTool(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public void launchNodeTool(IETPoint pStartPos, ICompartment pCompartment, IETRect pBounds) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getLogicalBoundingRect(org.netbeans.modules.uml.core.support.umlsupport.IETRect, boolean)
	 */
	public long getLogicalBoundingRect(IETRect prectBounding, boolean bIncludeLabels) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void handleNameListCompartmentDraw(IDrawInfo pDrawInfo,IETRect boundingRect)
	{
		handleNameListCompartmentDraw(pDrawInfo, boundingRect, 0, 0, false, 0);
	}
	
	/**
	 * Helper for those draw engines that use the INameListCompartment.
	 * Here's where the node drawing actually happens.
	 *
	 * @param pInfo[in] Information about the draw event (ie the DC, are we printing...)
	 * @param overrideBoundingRect [in] If this rect is not zero then us this as the bounding rect, not
	 * the one in pInfo.
	 * @param nMinNameCompartmentX[in] The minimum size of the name list compartment (x)
	 * @param nMinNameCompartmentY[in] The minimum size of the name list compartment (y)
	 * @param bLeftJustifyNameCompartment[in] Tells the draw to left justify the name compartment
	 * @param nLeftJustifyOffset [in] If we're left justifying the name compartment then left justify it
	 * not to the left edge, but the left edge + nLeftJustifyOffset.
	 */
	public void handleNameListCompartmentDraw(IDrawInfo pDrawInfo,IETRect boundingRect, int nMinNameCompartmentX,int nMinNameCompartmentY, boolean bLeftJustifyNameCompartment, int nLeftJustifyOffset)
	{
		try
		{		
			if (pDrawInfo == null)
				return;
				
		   // Get the bounding rectangle of the node.
		  if (boundingRect == null || boundingRect.getWidth() == 0)
		  {
			 boundingRect = pDrawInfo.getBoundingRect();
		  }
		  
		  IETRect deviceRect = boundingRect instanceof ETDeviceRect ? boundingRect : new ETDeviceRect(boundingRect.getRectangle());

		  // Make sure we have a visible compartment
		  int nTempX = 0;
		  int nTempY = 0;
		  int currentBottom = 0;

		  clearVisibleCompartments();
		  int numCompartments = getNumCompartments();
		  for( int i = 0; i < numCompartments; i++ )
		  {
			ICompartment pCompartment = getCompartment(i);

			 if (pCompartment != null)
			 {
				// Get the size at the current zoom level
				IETSize size = pCompartment.calculateOptimumSize(pDrawInfo, false);
				nTempX = size.getWidth();
				nTempY = size.getHeight();
				INameListCompartment pNameListCompartment = pCompartment instanceof INameListCompartment ? (INameListCompartment)pCompartment : null;
				if (pNameListCompartment != null)
				{
				   nTempX = Math.max(nTempX, nMinNameCompartmentX);
				   nTempY = Math.max(nTempY, nMinNameCompartmentY);
				}

				// add it to our list of visible compartments
				addVisibleCompartment(pCompartment);

				currentBottom += nTempY;
				if (currentBottom >= deviceRect.getBottom())
				{
				   break;
				}
			 }
		  }

		  // Draw each compartment, giving the last compartment the rest of the size should the
		  // node exceed the size needed by the compartments.
		  currentBottom = 0;
		  numCompartments = getNumVisibleCompartments();
		  for(int i = 0; i < numCompartments; i++ )
		  {
			 ICompartment pCompartment = getVisibleCompartment(i);

			 if (pCompartment != null)
			 {
				IETSize size = pCompartment.calculateOptimumSize(pDrawInfo, false);
				nTempX = size.getWidth();
				nTempY = size.getHeight();

				INameListCompartment pNameListCompartment = pCompartment instanceof INameListCompartment ? (INameListCompartment)pCompartment : null;
				if (pNameListCompartment != null)
				{
				   nTempX = Math.max(nTempX, nMinNameCompartmentX);
				   nTempY =  Math.max(nTempY, nMinNameCompartmentY);
                                   pNameListCompartment.setResizeToFitCompartments(true);                                   
				}

				// Now offset the rectangle
				ETDeviceRect pThisCompartmentBoundingRect = ETDeviceRect.ensureDeviceRect( (IETRect)deviceRect.clone() );

				pThisCompartmentBoundingRect.setTop(pThisCompartmentBoundingRect.getTop() + currentBottom);

				if (i == numCompartments - 1 )
				{
				   pThisCompartmentBoundingRect.setBottom(deviceRect.getBottom());
				}
				else
				{
				   pThisCompartmentBoundingRect.setBottom(Math.min( (pThisCompartmentBoundingRect.getTop() + nTempY), deviceRect.getBottom()));
				   currentBottom+= nTempY;
				}

				// If we're told to left justify the name compartment then do that
				if (pNameListCompartment != null && bLeftJustifyNameCompartment)
				{
				   if (nLeftJustifyOffset > 0)
				   {
					  if ( (pThisCompartmentBoundingRect.getLeft() + nLeftJustifyOffset) < pThisCompartmentBoundingRect.getRight())
					  {
						 pThisCompartmentBoundingRect.setLeft(pThisCompartmentBoundingRect.getLeft() + nLeftJustifyOffset);
						 nTempX += nLeftJustifyOffset;
						 pThisCompartmentBoundingRect.setRight(Math.min(nTempX, pThisCompartmentBoundingRect.getRight()));
					  }
					  else
					  {
						 pThisCompartmentBoundingRect.setRight(Math.min(nTempX, pThisCompartmentBoundingRect.getRight()));
					  }
				   }
				   else
				   {
					  pThisCompartmentBoundingRect.setRight(Math.min(nTempX, pThisCompartmentBoundingRect.getRight()));
				   }
				}

				pCompartment.draw(pDrawInfo, pThisCompartmentBoundingRect);
			 }
		  }
		}
		catch (Exception e)
		{
		   e.printStackTrace();
		}
  	}
}

