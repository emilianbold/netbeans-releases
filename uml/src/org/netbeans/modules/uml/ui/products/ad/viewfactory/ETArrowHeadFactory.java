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



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead;

public class ETArrowHeadFactory
{
	public static IETArrowHead create(int kind)
	{
		IETArrowHead pArrowHead = null;
		try
		{
			switch (kind)
			{
				case DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD:
				{
					return null;					
				}

				// <----
				case DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW:				
				{
					pArrowHead = new ETUnFilledArrow();				
					break;
				}
									
				// /----
				case DrawEngineArrowheadKindEnum.DEAK_UNFILLEDHALFARROW:
				{
					pArrowHead = new ETUnFilledHalfArrow();
					break;		
				}
				
				// <|------
				case DrawEngineArrowheadKindEnum.DEAK_FILLED_WHITE:
				{
					pArrowHead = new ETFilledWhiteArrow();
					break;
				}
					
				// <*|------	
				case DrawEngineArrowheadKindEnum.DEAK_FILLED:   				
				{
					pArrowHead = new ETFilledArrow();
					break;
				}
				
				// < >-----
				case DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND: 			
				{				
					pArrowHead = new ETUnFilledDiamond();
					break;
				}
				
				// <*>-----
				case DrawEngineArrowheadKindEnum.DEAK_FILLEDDIAMOND:   			
				{
					pArrowHead = new ETFilledDiamond();
					break;
				}
					
				// < ><-----
				case DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND_NAVIGABLE:
				{
					pArrowHead = new ETUnFilledDiamondNavigable();
					break;
				}
					
				// <*><-----
				case DrawEngineArrowheadKindEnum.DEAK_FILLEDDIAMOND_NAVIGABLE:   
				{
					pArrowHead = new ETFilledDiamondNavigable();
					break;
				}
					
				//	(+)<-----
				case DrawEngineArrowheadKindEnum.DEAK_CIRCLE_WITH_PLUS:
				{
					pArrowHead = new ETCirclePlusArrow();
					break;    		
				}
			}			
		}
		catch(Exception e)
		{
			pArrowHead = null;
		}
		return pArrowHead;
	}
}
