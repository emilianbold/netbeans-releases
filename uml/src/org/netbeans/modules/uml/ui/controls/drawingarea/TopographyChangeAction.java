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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author sumitabhk
 *
 */
public class TopographyChangeAction implements ITopographyChangeAction
{
	private int m_Kind = DiagramAreaEnumerations.TAK_MOVETO;
	private IPresentationElement m_PresentationElement = null;
	private int m_Width = 0;
	private int m_Height = 0;
	private int m_X = 0;
	private int m_Y = 0;
	private int m_LayoutKind = ILayoutKind.LK_NO_LAYOUT;
	private boolean m_DoZoom = false;
	private boolean m_CreateBusyState = false;
	
	/**
	 * 
	 */
	public TopographyChangeAction()
	{
		super();
	}

	/**
	 * The kind of this event
	 *
	 * @param pVal [out,retval] The kind of this event
	 */
	public int getKind()
	{
		return m_Kind;
	}

	/**
	 * The kind of this event
	 *
	 * @param newVal [in] The kind of this event
	 */
	public void setKind(int value)
	{
		m_Kind = value;
	}

	/**
	 * The presentation element to modify
	 *
	 * @param pVal [out,retval] The presentation element
	 */
	public IPresentationElement getPresentationElement()
	{
		return m_PresentationElement;
	}

	/**
	 * The presentation element to modify
	 *
	 * @param newVal [in] The presentation element
	 */
	public void setPresentationElement(IPresentationElement value)
	{
		m_PresentationElement = value;
	}

	/**
	 * The new x location of this graph object
	 *
	 * @param pVal [out,retval] The x location
	 */
	public int getX()
	{
		return m_X;
	}

	/**
	 * The new x location of this graph object
	 *
	 * @param pVal [out,retval] The x location
	 */
	public void setX(int value)
	{
		m_X = value;
	}

	/**
	 * The new y location of this graph object
	 *
	 * @param pVal [out,retval] The y location
	 */
	public int getY()
	{
		return m_Y;
	}

	/**
	 * The new y location of this graph object
	 *
	 * @param pVal [out,retval] The y location
	 */
	public void setY(int value)
	{
		m_Y = value;
	}

	/**
	 * The new width of the graph object
	 *
	 * @param pVal [out,retval] The width
	 */
	public int getWidth()
	{
		return m_Width;
	}

	/**
	 * The new width of the graph object
	 *
	 * @param pVal [out,retval] The width
	 */
	public void setWidth(int value)
	{
		m_Width = value;
	}

	/**
	 * The new height of the graph object
	 *
	 * @param pVal [out,retval] The height
	 */
	public int getHeight()
	{
		return m_Height;
	}

	/**
	 * The new height of the graph object
	 *
	 * @param pVal [out,retval] The height
	 */
	public void setHeight(int value)
	{
		m_Height = value;
	}

	/**
	 * The new layout style for the diagram
	 *
	 * @param nLayoutStyle [in] The layout style
	 */
	public void setLayoutStyle(int value)
	{
		m_LayoutKind = value;
	}

	/**
	 * The new layout style for the diagram
	 *
	 * @param nLayoutStyle [in] The layout style
	 * @param bDoZoom [in] Does a zoom after the layout
	 * @param bCreateBusyState [in] Creates a busy state for the user
	 */
	public void setLayoutStyle(boolean bDoZoom, boolean bCreateBusyState, int value)
	{
		m_DoZoom = bDoZoom;
		m_CreateBusyState = bCreateBusyState;
		setLayoutStyle(value);
	}

	/**
	 * The new layout style for the diagram
	 *
	 * @param pLayoutStyle [out,retval] The layout style
	 */
	public int getLayoutStyle()
	{
		return m_LayoutKind;
	}

	/**
	 * Returns a description of this event
	 *
	 * @param sDesc [out,retval] A description of this event
	 */
	public String getDescription()
	{
		return "TopographyChangeAction : ";
	}

	/**
	 * Executes this action
	 */
	public void execute(IDrawingAreaControl control)
	{
		int kind = getKind();
		IPresentationElement presEle = getPresentationElement();
		
		if (control != null)
		{
			control.setIsDirty(true);
		}
		
      if( kind == DiagramAreaEnumerations.TAK_RESIZETO )
      {
         if (presEle instanceof INodePresentation)
         {
            INodePresentation nodePE = (INodePresentation)presEle;

            nodePE.invalidate();
            nodePE.resize( m_Width, m_Height, false );
            nodePE.moveTo( m_X, m_Y, (int)(MoveToFlags.MTF_MOVEX | MoveToFlags.MTF_MOVEY | MoveToFlags.MTF_LOGICALCOORD));
            nodePE.invalidate();
         }
      }
		else if (kind == DiagramAreaEnumerations.TAK_LAYOUTCHANGE ||
			      kind == DiagramAreaEnumerations.TAK_LAYOUTCHANGE_SILENT ||
			      kind == DiagramAreaEnumerations.TAK_LAYOUTCHANGE_IGNORECONTAINMENT ||
			      kind == DiagramAreaEnumerations.TAK_LAYOUTCHANGE_IGNORECONTAINMENT_SILENT )
		{
			int layoutKind = getLayoutStyle();
			if (control != null)
			{
				boolean silent = false;
				if (DiagramAreaEnumerations.TAK_LAYOUTCHANGE_SILENT == kind || 
					DiagramAreaEnumerations.TAK_LAYOUTCHANGE_IGNORECONTAINMENT_SILENT == kind)
				{
				   silent = true;
				}
				
				control.immediatelySetLayoutStyle(layoutKind, silent);
				if (m_DoZoom)
				{
					// Resize to fit the new contents.
					control.fitInWindow();
					
					// If fit in window returns >100% set to 100
					double zoom = control.getCurrentZoom();
					if (zoom > 1.0f)
					{
						control.zoom(1.0f);
					}
				}
			}
		}
	}

	public void execute()
	{
	}
}


