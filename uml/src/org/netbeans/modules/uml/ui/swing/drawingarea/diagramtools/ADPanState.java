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


package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import java.awt.event.MouseEvent;

import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import com.tomsawyer.editor.tool.TSEPanTool;


public class ADPanState extends TSEPanTool
{
	public void pan(double dx, double dy)
	{
		super.pan(dx, dy);		
	}
	
	public void onMouseReleased(MouseEvent event)
	{
		super.onMouseReleased(event);
		// 6458848, hold the pan state until right click or press Esc to release
		if (event.isPopupTrigger())
			setDefaultState();
	}
	
	protected void setDefaultState()
	{
		try
		{
			((ADGraphWindow)this.getGraphWindow()).getDrawingArea().switchToDefaultState();
		}
		catch(Exception e)
		{
			e.printStackTrace();			
		}						
	}
}
