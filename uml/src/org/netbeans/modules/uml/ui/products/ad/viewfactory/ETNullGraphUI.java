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

/**
 * @author KevinM
 *
 * Used when the grpah window is shutting down by a thread..
 * It stops any acess to the graph.
 */
public class ETNullGraphUI extends ETGenericGraphUI
{

	/**
	 *
	 */
	public ETNullGraphUI()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#draw(com.tomsawyer.editor.graphics.TSEGraphics, boolean, boolean, com.tomsawyer.util.TSConstRect, boolean)
	 */
	public synchronized void draw(TSEGraphics arg0, boolean arg1, boolean arg2, TSConstRect arg3, boolean arg4)
	{
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#draw(com.tomsawyer.editor.graphics.TSEGraphics, boolean, com.tomsawyer.util.TSConstRect)
	 */
	public synchronized void draw(TSEGraphics arg0, boolean arg1, TSConstRect arg2)
	{

	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#draw(com.tomsawyer.editor.graphics.TSEGraphics, boolean)
	 */
	public void draw(TSEGraphics arg0, boolean arg1)
	{
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#drawAll(com.tomsawyer.editor.graphics.TSEGraphics, com.tomsawyer.util.TSConstRect, boolean)
	 */
	public void drawAll(TSEGraphics arg0, TSConstRect arg1, boolean arg2)
	{
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#drawAll(com.tomsawyer.editor.graphics.TSEGraphics, com.tomsawyer.util.TSConstRect)
	 */
	public void drawAll(TSEGraphics arg0, TSConstRect arg1)
	{
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEObjectUI#drawOutline(com.tomsawyer.editor.graphics.TSEGraphics)
	 */
	public void drawOutline(TSEGraphics arg0)
	{
	}

}
