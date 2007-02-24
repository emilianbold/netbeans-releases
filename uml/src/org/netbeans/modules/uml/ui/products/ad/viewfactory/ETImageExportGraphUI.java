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

import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

/**
 * @author KevinM
 *
 * Saves off the transform, so we can figure out where the graph objects
 * are in the web report.
 */
public class ETImageExportGraphUI extends ETGenericGraphUI
{
	protected TSTransform lastDrawTransform = null;

	/*
	 * Returns the transform used to draw the graph on the graphic.
	 */
	public TSTransform getImageTransform()
	{
		return lastDrawTransform;
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#drawAll(com.tomsawyer.editor.graphics.TSEGraphics, com.tomsawyer.util.TSConstRect, boolean)
	 */
	public void drawAll(TSEGraphics arg0, TSConstRect arg1, boolean arg2)
	{
		// Save off the transform, so we can figure out where the graph objects
		// are in the web report.
		lastDrawTransform =(TSTransform) arg0.getTSTransform().clone();
		super.drawAll(arg0, arg1, arg2);
	}
}
