/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.graphics.TSEGraphics;

public interface IDrawInfo
{
   /**
    * Are we drawing to the main drawing area or something else (ie overview window).
   */
   public boolean getDrawingToMainDrawingArea();

   /**
    * Are we drawing to the main drawing area or something else (ie overview window).
   */
   public void setDrawingToMainDrawingArea(boolean value);

   /**
    * The TSGraphDisplay TS object
   */
   public TSEGraphWindow getGraphDisplay();

   /**
    * The TSGraphDisplay TS object
   */
   public void setGraphDisplay(TSEGraphWindow value);

   /**
    * The device bounds as returned by TS
   */
   public IETRect getDeviceBounds();

   /**
    * The device bounds as returned by TS
   */
   public void setDeviceBounds(IETRect value);

   /**
    * The bounding rectangle as returned by TS
   */
   public IETRect getBoundingRect();

   /**
    * The bounding rectangle as returned by TS
   */
   public void setBoundingRect(IETRect value);

   /**
    * The TS TSGraphObject object
   */
   public IETGraphObject getGraphObject();

   /**
    * The TS TSGraphObject object
   */
   public void setGraphObject(IETGraphObject value);

   /**
    * The TSEDrawInfo object received from the drawing application.
   */
   public void setActualTSEDrawInfo(int value);

   /**
    * During on draw we alter the zoom setting, otherwise print preview and overview windows are messed up.
   */
   public double getOnDrawZoom();

   /**
    * Saves the drawing device context settings.
   */
   public int saveSettings();

   /**
    * Saves the drawing device context settings.
   */
   public long restoreSettings(int nCookie);

   /**
    * The current text color.
   */
   public Color getTextColor();

   /**
    * The current text color.
   */
   public void setTextColor(Color value);

   /**
    * The current text background color.
   */
   public Color getBackColor();

   /**
    * The current text background color.
   */
   public void setBackColor(Color value);

   /**
    * The current text font.
   */
   public Font getFont();

   /**
    * The current text font.
   */
   public void setFont(Font value);

   /**
    * The 0,0 ordinate start point.
   */
   public long getViewportOrg(int cx, int cy);

   /**
    * The 0,0 ordinate start point.
   */
   public long setViewportOrg(int cx, int cy);

   /**
    * Specifies if the node is transparent.
    */
   public boolean isTransparent();

   /**
    * Sets if the node is transparent.
    */
   public void setIsTransparent(boolean value);

   /**
    * Prints the current font information to the outputwindow.
   */
   public long printFontToOutputWindow();

   /**
    * Makes sure to set the font if we are exporting raster or printing
   */
   public boolean getAlwaysSetFont();

   /**
    * Makes sure to set the font if we are exporting raster or printing
   */
   public void setAlwaysSetFont(boolean value);

   public TSEGraphics getTSEGraphics();
   public void setTSEGraphics(TSEGraphics newValue);

   /**
    * Specifies if the border should be drawn or not.
    * 
    * @return <code>true</code> if the border is to be rendered.
    */
   public boolean isBorderDrawn();

   /**
    * Sets whether or not to draw the border.
    * @param value
    */
   public void setIsBorderDrawn(boolean value);

	/*
	 * Returns the TSTransform of the graphics object.
	 */
   public TSTransform getTSTransform();
   
   /*
    * Calculates the clipping rectangle, the getDeviceBounds is inside this rect.
    */
	public Rectangle calculateClippingRect();
   
   /*
    * Clips to the logical drawing rect retruns the previous clipping rect.
    */
   public Rectangle clip();
	
	/*
	 * Returns the Font Scale factor for this device
	 */
	public double getFontScaleFactor();
}
