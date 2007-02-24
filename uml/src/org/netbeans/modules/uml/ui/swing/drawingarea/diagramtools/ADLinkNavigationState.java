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
//import com.tomsawyer.editor.state.TSELinkNavigationState;
import com.tomsawyer.editor.tool.TSELinkNavigationTool;
import com.tomsawyer.editor.TSEObject;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/*
 *
 * @author Kevinm
 *
 */
//public class ADLinkNavigationState extends TSELinkNavigationState
public class ADLinkNavigationState extends TSELinkNavigationTool
{
	protected boolean hitGraphObj = false;
	protected boolean didScroll = false;

	public void onMousePressed(MouseEvent event)
	{
		super.onMousePressed(event);

		if (objectFromMouse(event) != null)
			hitGraphObj = true;
	}
	
	protected TSEObject objectFromMouse(MouseEvent event)
	{
		TSConstPoint point = this.getNonalignedWorldPoint(event);
//		return this.getObjectAt(point, null, this.getGraphWindow().getGraph());
                return this.getHitTesting().getGraphObjectAt(point, this.getGraphWindow().getGraph(), true);
	}

	public String getToolTipText(MouseEvent event)
	{
		String toolTip = super.getToolTipText(event);

		if (hitGraphObj && !this.scrollingInProgress())
			setDefaultState();
		return toolTip;
	}

	protected void setDefaultState()
	{
		try
		{
			((ADGraphWindow) this.getGraphWindow()).getDrawingArea().switchToDefaultState();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	
	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMouseMoved(java.awt.event.MouseEvent)
	 */
	public void onMouseMoved(MouseEvent arg0)
	{
		super.onMouseMoved(arg0);
		
		if (didScroll && !scrollingInProgress())
		{
			setDefaultState();		
		}
	}	


	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.state.TSELinkNavigationState#scrollingInProgress()
	 */
	public boolean scrollingInProgress()
	{
		boolean retVal = super.scrollingInProgress();
		if (didScroll == false && retVal)
			didScroll = retVal;
		return retVal;
	}
}
