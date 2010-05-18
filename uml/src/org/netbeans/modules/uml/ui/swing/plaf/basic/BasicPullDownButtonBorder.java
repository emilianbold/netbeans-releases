/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
