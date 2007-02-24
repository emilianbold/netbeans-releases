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
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import java.awt.geom.GeneralPath;


public class ETFilledArrow extends ETUnFilledArrow
{
	public ETFilledArrow()
	{
		super(DrawEngineArrowheadKindEnum.DEAK_FILLED);
	}
	
	protected ETFilledArrow(int kind)
	{
		super(kind);
	}
	
	protected boolean filled() { return true; }
	protected boolean isOpenRegion() { return false; };

	public GeneralPath getShape(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt)
	{
		GeneralPath path = super.getShape(pInfo, fromPt, toPt);
		// Just close the arrow.
		if (path != null)
			path.closePath();
		return path;
	}
}
