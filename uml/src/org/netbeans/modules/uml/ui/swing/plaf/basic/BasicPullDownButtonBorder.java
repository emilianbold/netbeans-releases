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


package org.netbeans.modules.uml.ui.swing.plaf.basic;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.netbeans.modules.uml.ui.swing.pulldownbutton.JPullDownButton;

/**
 *
 * @author Trey Spiva
 */
public class BasicPullDownButtonBorder extends AbstractBorder
{
   protected Insets borderInsets = null;

   private boolean m_DrawBorder = true;

   public BasicPullDownButtonBorder(boolean drawBorder)
   {
      setDrawBorderAround(drawBorder);
   }
   
   public void setDrawBorderAround(boolean value)
   {
      m_DrawBorder = value;
      
      if(m_DrawBorder == true)
      {
         borderInsets = new Insets(4, 4, 4, 4);
      }
      else
      {
         borderInsets = new Insets(4, 4, 4, 4);
      }
   }
   
   public boolean getDrawBorderAround()
   {
      return m_DrawBorder;
   }
   
   public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
   {
   	if (c instanceof AbstractButton)
   	{
		AbstractButton button = (AbstractButton)c;
		ButtonModel model = button.getModel();

		if (model.isEnabled())
		{
		   boolean isPressed = model.isPressed() && model.isArmed();
		   if (isPressed)
		   {
			  drawPressed3DBorder(g, x, y, w, h);
		   }         
		   else if (c instanceof JPullDownButton)
		   {
			  JPullDownButton btn = (JPullDownButton)c;
			  if(btn.isArrowButtonPressed() == true)
			  { 
				 drawPressed3DBorder(g, x, y, w, h); 
			  }
			  else if(getDrawBorderAround() == true)
			  {
				 drawButtonBorder(g, x, y, w, h, false);
			  }
		   }
		   else if(getDrawBorderAround() == true)
		   {
			  drawButtonBorder(g, x, y, w, h, false);
		   }
		}
		else if(getDrawBorderAround() == true)
		{ // disabled state
		   drawDisabledBorder(g, x, y, w - 1, h - 1);
		}
   	}
   }

   public Insets getBorderInsets(Component c)
   {
      return borderInsets;
   }

   public Insets getBorderInsets(Component c, Insets newInsets)
   {
      newInsets.top = borderInsets.top;
      newInsets.left = borderInsets.left;
      newInsets.bottom = borderInsets.bottom;
      newInsets.right = borderInsets.right;
      return newInsets;
   }

   /**
    * This draws a variant "Flush 3D Border"
    * It is used for things like pressed buttons.
    */
   static void drawPressed3DBorder(Graphics g, int x, int y, int w, int h)
   {
      g.translate(x, y);

      drawFlush3DBorder(g, 0, 0, w, h);

      g.setColor(MetalLookAndFeel.getControlShadow());
      g.drawLine(1, 1, 1, h - 2);
      g.drawLine(1, 1, w - 2, 1);
      g.translate(-x, -y);
   }

   static void drawButtonBorder(Graphics g, int x, int y, int w, int h, boolean active)
   {
      if (active)
      {
         drawActiveButtonBorder(g, x, y, w, h);
      }
      else
      {
         drawFlush3DBorder(g, x, y, w, h);
      }
   }

   /**
         * This draws the "Flush 3D Border" which is used throughout the Metal L&F
         */
   static void drawFlush3DBorder(Graphics g, int x, int y, int w, int h)
   {
      g.translate(x, y);
      g.setColor(MetalLookAndFeel.getControlDarkShadow());
      g.drawRect(0, 0, w - 2, h - 2);
      g.setColor(MetalLookAndFeel.getControlHighlight());
      g.drawRect(1, 1, w - 2, h - 2);
      g.setColor(MetalLookAndFeel.getControl());
      g.drawLine(0, h - 1, 1, h - 2);
      g.drawLine(w - 1, 0, w - 2, 1);
      g.translate(-x, -y);
   }

   static void drawActiveButtonBorder(Graphics g, int x, int y, int w, int h)
   {
      drawFlush3DBorder(g, x, y, w, h);
      g.setColor(MetalLookAndFeel.getPrimaryControl());
      g.drawLine(x + 1, y + 1, x + 1, h - 3);
      g.drawLine(x + 1, y + 1, w - 3, x + 1);
      g.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
      g.drawLine(x + 2, h - 2, w - 2, h - 2);
      g.drawLine(w - 2, y + 2, w - 2, h - 2);
   }

   static void drawDisabledBorder(Graphics g, int x, int y, int w, int h)
   {
      g.translate(x, y);
      g.setColor(MetalLookAndFeel.getControlShadow());
      g.drawRect(0, 0, w - 1, h - 1);
      g.translate(-x, -y);
   }
}
