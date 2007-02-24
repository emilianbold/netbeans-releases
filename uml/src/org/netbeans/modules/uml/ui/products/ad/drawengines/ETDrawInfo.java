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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADDrawingAreaSelectState;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.editor.state.TSEMoveSelectedState;
import com.tomsawyer.editor.tool.TSEMoveSelectedTool;
//import com.tomsawyer.editor.state.TSESelectState;
import com.tomsawyer.editor.tool.TSESelectTool;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

public class ETDrawInfo implements IDrawInfo
{
   private TSEGraphics graphics;
   private boolean m_AlwaysSetFont = false;
   private boolean m_IsTransparent = false;
   private IETRect m_BoundingRect = null;
   private IETRect m_DeviceRect = null;
   private IETGraphObject m_graphObject = null;
   private Color m_TextColor = Color.BLACK;
   private boolean m_IsBorder = true;
   private int m_MainDrawingArea = -1;
	private double m_fontScaleFactor = -1.0;

   public ETDrawInfo()
   {
      this.graphics = null;
   }

   public ETDrawInfo(TSEGraphics graphics)
   {
      this.graphics = graphics;
   }

   public TSEGraphics getTSEGraphics()
   {
      return this.graphics;
   }

   public void setTSEGraphics(TSEGraphics newVal)
   {
      this.graphics = newVal;
		// Reset the font scale factor, the graphics have changed.
		m_fontScaleFactor =  -1.0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getAlwaysSetFont()
    */
   public boolean getAlwaysSetFont()
   {
      return m_AlwaysSetFont;
   }

   /** 
    * The current text background color.
    *
    * @return the current background color 
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getBackColor()
    */
   public Color getBackColor()
   {
      return getTSEGraphics() != null ? getTSEGraphics().getBackground() : Color.white;
   }

   /** 
    * The background drawing mode.
    * 
    * @return 2 if opaque, 1 if transparent.  If opaque the current brush is used to paint
    * the background, otherwise the background is not changed.
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getBackgroundMode()
    */
   public boolean isTransparent()
   {
      return m_IsTransparent;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getBoundingRect()
    */
   public IETRect getBoundingRect()
   {
      return m_BoundingRect;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getDeviceBounds()
    */
   public IETRect getDeviceBounds()
   {
      return m_DeviceRect;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getDrawingToMainDrawingArea()
    */
   public boolean getDrawingToMainDrawingArea()
   {
      if (m_MainDrawingArea < 0 && getGraphObject() != null && this.graphics != null)
      {
         try
         {
            // calculate it
            return getGraphObject().getEngine().getDrawingArea().getGraphWindow() == graphics.getGraphWindow();
         }
         catch (Exception e)
         {
            e.printStackTrace();
            return false;
         }
      }
      else
         return m_MainDrawingArea > 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getFont()
    */
   public Font getFont()
   {
      return getTSEGraphics() != null ? getTSEGraphics().getFont() : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getGraphDisplay()
    */
   public TSEGraphWindow getGraphDisplay()
   {
      return getTSEGraphics() != null ? getTSEGraphics().getGraphWindow() : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getGraphObject()
    */
   public IETGraphObject getGraphObject()
   {
      return m_graphObject;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getOnDrawZoom()
    */
   public double getOnDrawZoom()
   {
      return graphics != null ? graphics.getGraphWindow().getZoomLevel() : 1.0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getTextColor()
    */
   public Color getTextColor()
   {
      return m_TextColor;
   }

   /**
    * The viewport origin.
    *
    * @return The coordinate of the viewport origin.
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getViewportOrg(int, int)
    */
   public long getViewportOrg(int cx, int cy)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#printFontToOutputWindow()
    */
   public long printFontToOutputWindow()
   { 
		Font font = this.getFont();
		if (font != null)
		{
			ETSystem.out.println("ETDrawInfo font = " + font.toString());
		}
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#restoreSettings(int)
    */
   public long restoreSettings(int nCookie)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#saveSettings()
    */
   public int saveSettings()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setActualTSEDrawInfo(int)
    */
   public void setActualTSEDrawInfo(int value)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setAlwaysSetFont(boolean)
    */
   public void setAlwaysSetFont(boolean value)
   {
      m_AlwaysSetFont = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setBackColor(long)
    */
   public void setBackColor(Color value)
   {
      if (getTSEGraphics() != null)
      {
         getTSEGraphics().setBackground(value);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setBackgroundMode(int)
    */
   public void isTransparent(boolean value)
   {
      m_IsTransparent = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setBoundingRect(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public void setBoundingRect(IETRect value)
   {
      m_BoundingRect = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setDeviceBounds(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public void setDeviceBounds(IETRect value)
   {
      m_DeviceRect = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setDrawingToMainDrawingArea(boolean)
    */
   public void setDrawingToMainDrawingArea(boolean value)
   {
      m_MainDrawingArea = value ? 1 : 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setFont(java.lang.Object)
    */
   public void setFont(Font value)
   {
      if (getTSEGraphics() != null)
      {
         getTSEGraphics().setFont(value);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setGraphDisplay(com.embarcadero.describe.gui.layout.TSGraphDisplay)
    */
   public void setGraphDisplay(TSEGraphWindow value)
   {
      // no op
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setGraphObject(com.tomsawyer.graph.TSGraphObject)
    */
   public void setGraphObject(IETGraphObject object)
   {
      m_graphObject = object;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setOnDrawZoom(double)
    */
   public void setOnDrawZoom(double value)
   {
   	// we always get the zoom level from the graphics object.
      // m_ZoomLevel = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setTextColor(long)
    */
   public void setTextColor(Color value)
   {
      m_TextColor = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setViewportOrg(int, int)
    */
   public long setViewportOrg(int cx, int cy)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setIsTransparent(boolean)
    */
   public void setIsTransparent(boolean value)
   {
      m_IsTransparent = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#isBorderDrawn()
    */
   public boolean isBorderDrawn()
   {
      return m_IsBorder;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#setIsBorderDrawn(boolean)
    */
   public void setIsBorderDrawn(boolean value)
   {
      m_IsBorder = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#getTSTransform()
    */
   public TSTransform getTSTransform()
   {
      return this.graphics != null ? this.graphics.getTSTransform() : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#clip()
    */
   public Rectangle clip()
   {
      if (this.graphics != null)
      {
         Rectangle prevClip = this.graphics.getClipRect();
         
         // Fix J1570:  During selection & dragging of the node inside a container
         //             we don't want to use the clipping rect.  We are not sure
         //             why this is necessary, but it cleans up some clipping problems.
         
         // This was turned off by kevin.  It this causing major bugs, we need to find the true source of
         // the containment drawing bug.
         
         //if( ! (getGraphDisplay().getCurrentState() instanceof TSESelectState) &&
         //    ! (getGraphDisplay().getCurrentState() instanceof TSEMoveSelectedState) )
         {
            graphics.setClip(calculateClippingRect());
         }
         return prevClip;
      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo#calculateClippingRect()
    */
   public Rectangle calculateClippingRect()
   {
   	IETRect deviceBounds = getDeviceBounds();
   	if (deviceBounds != null)
      {
      	// inflate the device rect by one in each direction.
      	IETRect deviceRect = (IETRect) deviceBounds.clone();
			deviceRect.inflate(1, 1);
			return deviceRect.getRectangle();
      }
 		return null;
   }

	public double getFontScaleFactor()
	{
		if (m_fontScaleFactor == -1.0)
		{
			TSTransform transform = this.getTSTransform();
			
			if (transform != null)
			{
				m_fontScaleFactor =  transform.getScaleX();
			}
			else
			{
				return 1.0;
			}
		}
		return m_fontScaleFactor;

		//return this.getOnDrawZoom();
	}
}
