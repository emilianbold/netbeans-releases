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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.Iterator;

import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETStrokeCache;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETArrowHeadFactory;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEEdge;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/**
 * @author KevinM
 *
 */
public class GDISupport
{
   private static final int SMALLEST_TEXT = 3;

   public static void fillRectangle(Graphics2D dc, final Rectangle boundingRect)
   {
      dc.fillRect(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height);
   }

   public static void fillRectangle(Graphics2D dc, final IETRect boundingRect, final Color fillColor )
   {
      Color prevColor = dc.getColor();
      
      dc.setColor( fillColor );
      fillRectangle( dc, boundingRect );
      
      dc.setColor( prevColor );

   }

   public static void fillRectangle(Graphics2D dc, final IETRect boundingRect)
   {
      dc.fillRect((int) boundingRect.getLeft(), (int) boundingRect.getTop(), (int) boundingRect.getWidth(), (int) boundingRect.getHeight());
   }

   public static void frameRectangle(Graphics2D dc, final Rectangle boundingRect)
   {
      dc.drawRect(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height);
   }

   public static void frameRectangle(Graphics2D dc, final IETRect boundingRect)
   {
      dc.draw(boundingRect.getRectangle());
   }

   /**
    * Draws a frame with the specified parameters
    * 
    * @param dc            Graphical device context
    * @param boundingRect  Rectangle for the frame's location
    * @param lineKind      DrawEngineLineKindEnum which specifies the type of line to use for the frame
    * @param width         Pixel width of the frame
    * @param frameColor    Color of the frame
    */
   public static void frameRectangle(Graphics2D dc, final IETRect boundingRect, int lineKind, int width, final Color frameColor)
   {
      // Select the pen, but save off the current one first.
      Color prevColor = dc.getColor();
      Stroke prevPen = dc.getStroke();

      dc.setColor(frameColor);
      dc.setStroke(getLineStroke(lineKind, width));
      frameRectangle(dc, boundingRect);
      
      dc.setColor(prevColor);
      dc.setStroke(prevPen);
   }

   /*
    *  Draws a rectangle using the dc's current color settings
    */
   public static void drawRectangle(Graphics2D dc, final Rectangle boundingRect)
   {
      fillRectangle(dc, boundingRect);
      frameRectangle(dc, boundingRect);
   }

   /*
    * Draw a rectangle with a pen color and solid back color
    * If the background color is not desired pass bkColor = null to just draw the frame
    */
   public static void drawRectangle(Graphics2D dc, 
                                    final Rectangle boundingRect, 
                                    final Paint penColor, 
                                    final Paint bkColor)
   {
      drawRectangle(dc, boundingRect, penColor, bkColor, 
                    getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, 1));
   }

   /*
    * Draw a dashed rectangle with a pen color and solid back color
    * If the background color is not desired pass bkColor = null to just draw the frame
    */
   public static void drawDashedRectangle(Graphics2D dc, final Rectangle boundingRect, Paint penColor, Paint bkColor)
   {
      drawRectangle(dc, boundingRect, penColor, bkColor, getLineStroke(DrawEngineLineKindEnum.DELK_DASH, 1));
   }

   /*
    * Draw a hatched rectangle with a pen color and solid back color
    * If the background color is not desired pass bkColor = null to just draw the frame
    */
   public static void drawHatchedRectangle(Graphics2D dc, final Rectangle boundingRect, Color penColor, Color bkColor)
   {
      drawRectangle(dc, boundingRect, penColor, bkColor, getLineStroke(DrawEngineLineKindEnum.DELK_HATCHED, 1));
   }

   /// Draws a rectangle with the pen and the brush arguments
   public static void drawRectangle(Graphics2D dc, 
                                    final Rectangle boundingRect, 
                                    final Paint penColor, 
                                    final Paint bkColor, 
                                    final Stroke pPen)
   {
      Paint prevColor = dc.getPaint();

      // Select the pen, but save off the current one first.
      Stroke prevPen = dc.getStroke();
      dc.setStroke(pPen);

      if (bkColor != null)
      {
         dc.setPaint(bkColor);
         dc.fill(boundingRect);
      }

      dc.setPaint(penColor);
      dc.draw(boundingRect);

      dc.setPaint(prevColor);
      dc.setStroke(prevPen);
   }

   public static void frameEllipse(Graphics2D dc, final Rectangle boundingRect)
   {
      dc.drawOval(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height);
   }

   public static void fillEllipse(Graphics2D dc, final Rectangle boundingRect)
   {
      //dc.fillOval(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height);
      java.awt.geom.Ellipse2D.Float shape = new java.awt.geom.Ellipse2D.Float(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height); 
      dc.fill(shape);
   }

   /// Draws an ellipse using the current pen and brush
   public static void drawEllipse(Graphics2D dc, final Rectangle boundingRect)
   {
      fillEllipse(dc, boundingRect);
      Color prevColor = dc.getColor();
      dc.setColor(TSEColor.black.getColor());
      frameEllipse(dc, boundingRect);
      dc.setColor(prevColor);
   }

   public static void drawEllipse(Graphics2D dc, final Rectangle boundingRect, final Paint penColor, final Paint bkColor)
   {
      drawEllipse(dc, boundingRect, penColor, bkColor, getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, 1));
   }

   public static void drawEllipse(Graphics2D dc, 
                                  final Rectangle boundingRect,             
                                  final Paint penColor, 
                                  final Paint bkColor, 
                                  final Stroke pen)
   {
      Paint prevColor = dc.getPaint();

      // Select the pen, but save off the current one first.
      Stroke prevPen = dc.getStroke();
      dc.setPaint(bkColor);
      fillEllipse(dc, boundingRect);
      dc.setStroke(pen);
      dc.setPaint(penColor);
      frameEllipse(dc, boundingRect);
      dc.setPaint(prevColor);
      dc.setStroke(prevPen);
   }
   
   /// Draws an dashed ellipse using the argument pen and brush
   public static void drawDashedEllipse(Graphics2D dc, 
                                        final Rectangle boundingRect, 
                                        final Paint penColor, 
                                        final Paint bkColor)
   {
      drawEllipse(dc, boundingRect, penColor, bkColor, getLineStroke(DrawEngineLineKindEnum.DELK_DASH, 1));
   }
   
   public static void drawRoundRect(Graphics2D dc, Rectangle boundingRect, double nCurrentZoom, Paint penColor, Paint bkColor) {
      drawRoundRect(dc, boundingRect, nCurrentZoom, penColor, bkColor, getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, 1));
   }

   public static void drawRoundRect(Graphics2D dc, Rectangle boundingRect, double nCurrentZoom, Paint penColor, Paint bkColor, Stroke pen) {
      Paint prevColor = dc.getPaint();

      // Select the pen, but save off the current one first.
      Stroke prevPen = dc.getStroke();
      dc.setPaint(bkColor);
      fillRoundRect(dc, boundingRect, nCurrentZoom);
      dc.setStroke(pen);
      dc.setPaint(penColor);
      frameRoundRect(dc, boundingRect, nCurrentZoom);
      dc.setPaint(prevColor);
      dc.setStroke(prevPen);
   }

   public static void frameRoundRect(Graphics2D dc, final Rectangle boundingRect, double nCurrentZoom)
   {
      dc.drawRoundRect(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height, 15, 15);
   }

   public static void fillRoundRect(Graphics2D dc, final Rectangle boundingRect, double nCurrentZoom)
   {
      dc.fillRoundRect(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height, 15, 15);
   }

   /// Draws a rounded rectangle
   public static void drawRoundRect(Graphics2D dc, final Rectangle boundingRect, double nCurrentZoom, final Color penColor, final Color bkColor, final Stroke pen)
   {
      Color prevColor = dc.getColor();

      // Select the pen, but save off the current one first.
      Stroke prevPen = dc.getStroke();
      dc.setColor(bkColor);
      fillRoundRect(dc, boundingRect, nCurrentZoom);
      dc.setStroke(pen);
      dc.setColor(penColor);
      frameRoundRect(dc, boundingRect, nCurrentZoom);
      dc.setColor(prevColor);
      dc.setStroke(prevPen);
   }

   /// Draws a dashed, rounded rectangle using the current pen and brush
   public static void drawDashedRoundRect(Graphics2D dc, final Rectangle boundingRect, double nCurrentZoom, final Color penColor, final Paint bkColor)
   {
      drawRoundRect(dc, boundingRect, nCurrentZoom, penColor, bkColor, getLineStroke(DrawEngineLineKindEnum.DELK_DASH, 1));
   }

   /// Draws a rounded rectangle using the argument pen and brush
   public static void drawRoundRect(Graphics2D dc, final Rectangle boundingRect, double nCurrentZoom, final Color penColor, final Paint bkColor)
   {
      drawRoundRect(dc, boundingRect, nCurrentZoom, penColor, bkColor, getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, 1));
   }

   /// Draws a black rounded rectangle using the current color.
   public static void drawRoundRect(Graphics2D dc, final Rectangle boundingRect, double nCurrentZoom)
   {
      drawRoundRect(dc, boundingRect, nCurrentZoom, TSEColor.black.getColor(), dc.getColor());
   }

   /// Draws a rounded rectangle using the argument pen and brush
   public static void drawRoundOnTopRect(Graphics2D dc, final Rectangle boundingRect, double nCurrentZoom, final Color penColor, final Color bkColor)
   {
   }

   /// Draws a rounded rectangle using the current pen and brush
   public static void drawRoundOnTopRect(Graphics2D dc, final Rectangle boundingRect, double nCurrentZoom)
   {
   }

   public static void drawLine(Graphics2D dc, final Point topLeft, final Point bottomRight, final Stroke pen)
   {
      // Select the pen, but save off the current one first.
      Stroke prevPen = dc.getStroke();
		dc.setStroke(pen);
      dc.drawLine(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
      dc.setStroke(prevPen);
   }

   /*
    * Draws the line onto the Graphics context
    */
   public static void drawLine(Graphics2D dc, final Point topLeft, final Point bottomRight, final Color penColor, final Stroke pen)
   {
      Color prevColor = dc.getColor();

      // Select the pen, but save off the current one first.
      dc.setColor(penColor);
      drawLine(dc, topLeft, bottomRight, pen);
      dc.setColor(prevColor);
   }

   public static void drawLine(Graphics2D dc, final Point topLeft, final Point bottomRight, final Color penColor, int penWidth, int lineStyle)
   {
      drawLine(dc, topLeft, bottomRight, penColor, getLineStroke(lineStyle, penWidth));
   }

   public static void drawLine(Graphics2D dc, final Point topLeft, final Point bottomRight, final Color penColor, int penWidth)
   {
      drawLine(dc, topLeft, bottomRight, penColor, getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, penWidth));
   }

   /// Draws a line using the currently selected colors
   public static void drawLine(Graphics2D dc, final Point topLeft, final Point bottomRight)
   {
      dc.drawLine(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
   }

   public static void drawDashedLine(Graphics2D dc, final Point topLeft, final Point bottomRight, final Color penColor)
   {
      drawLine(dc, topLeft, bottomRight, penColor, getLineStroke(DrawEngineLineKindEnum.DELK_DASH, 1));
   }

   /// Draws a dotted line with the pen color and the brush arguments
   public static void drawDottedLine(Graphics2D dc, final Point topLeft, final Point bottomRight, final Color penColor)
   {
      drawLine(dc, topLeft, bottomRight, penColor, getLineStroke(DrawEngineLineKindEnum.DELK_COUNT, 1));
   }

   /// Draws a poly line with the pen color and the brush arguments
   public static void drawPolyLine(Graphics2D dc, ETList < IETPoint > pPoints, final Color penColor, int penWidth)
   {
      Color prevColor = dc.getColor();
      Stroke prevPen = dc.getStroke();
      dc.setStroke(getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, penWidth));
      dc.setColor(penColor);
      drawPolyLine(dc, pPoints, penColor);
      dc.setStroke(prevPen);
      dc.setColor(prevColor);

   }

   /// Draws a poly line with the pen color and the brush arguments
   public static void drawPolyLine(Graphics2D dc, ETList < IETPoint > pPoints, final Color penColor)
   {
      if (pPoints == null || pPoints.size() < 2)
         return;

      Color prevColor = dc.getColor();

      dc.setColor(penColor);

      GeneralPath polyline = getPolyline(pPoints);
      if (polyline != null)
         dc.draw(polyline);

      dc.setColor(prevColor);
   }

   /**
    * Draws a poly line with the pen color and the brush arguments
    */
   public static void drawPolyLine(Graphics2D dc, final Rectangle boundingRect, final Color penColor)
   {
      Color prevColor = dc.getColor();
      fillRectangle(dc, boundingRect);
      dc.setColor(prevColor);
   }

   public static GeneralPath getPolyline(ETList < IETPoint > pPoints)
   {
      if (pPoints == null || pPoints.size() < 2)
         return null;

      GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
      Iterator < IETPoint > iter = pPoints.iterator();
      IETPoint pt = iter.next();
      polyline.moveTo((int) pt.getX(), (int) pt.getY());

      while (iter.hasNext())
      {
         pt = iter.next();
         polyline.lineTo((int) pt.getX(), (int) pt.getY());
      }
      return polyline;
   }

   public static GeneralPath getPolygon(ETList < IETPoint > pPoints)
   {
      GeneralPath polygon = getPolyline(pPoints);
      if (polygon != null)
         polygon.closePath();
      return polygon;
   }

   public static void framePolygon(Graphics2D dc, ETList < IETPoint > pPoints)
   {
      framePolygon(dc, getPolygon(pPoints));
   }

   public static void framePolygon(Graphics2D dc, GeneralPath polygon)
   {
      if (polygon != null)
         dc.draw(polygon);
   }

   public static void fillPolygon(Graphics2D dc, ETList < IETPoint > pPoints)
   {
      fillPolygon(dc, getPolygon(pPoints));
   }

   public static void fillPolygon(Graphics2D dc, GeneralPath polygon)
   {
      if (polygon != null)
         dc.fill(polygon);
   }

   public static void drawPolygon(Graphics2D dc, ETList < IETPoint > pPoints, final Color penColor, final Color bkColor, final Stroke pen)
   {
      drawPolygon(dc, getPolygon(pPoints),penColor,bkColor,pen);
   }

   /// Draws a polygon with the pen color and the brush arguments
   public static void drawPolygon(Graphics2D dc, ETList < IETPoint > pPoints, final Color penColor, int penWidth, final Color bkColor)
   {
      drawPolygon(dc, pPoints, penColor, bkColor, getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, penWidth));
   }

   /// Draws a polygon with the pen color and the brush arguments
   public static void drawPolygon(Graphics2D dc, ETList < IETPoint > pPoints, final Color penColor, final Color bkColor)
   {
      Stroke currentPen = dc.getStroke() != null ? dc.getStroke() : getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, 1);

      drawPolygon(dc, pPoints, penColor, bkColor, currentPen);
   }

	public static void drawPolygon(Graphics2D dc, GeneralPath polygon, final Color penColor, final Color bkColor)
	{
		Stroke currentPen = dc.getStroke() != null ? dc.getStroke() : getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, 1);

		drawPolygon(dc, polygon, penColor, bkColor, currentPen);		
	}
	
	
   public static void drawPolygon(Graphics2D dc, 
                                  final ETList<IETPoint> pPoints,             
                                  final Paint penColor, 
                                  final int penWidth,
                                  final Paint bkColor)
   {
       drawPolygon(dc, getPolygon(pPoints), penColor, bkColor, getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, penWidth));
   }
   public static void drawPolygon(Graphics2D dc, 
                                  final ETList<IETPoint> pPoints,             
                                  final Paint penColor, 
                                  final Paint bkColor)
   {
       drawPolygon(dc, getPolygon(pPoints), penColor, bkColor, getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, 1));
   }
   public static void drawPolygon(Graphics2D dc, 
                                  final GeneralPath polygon,             
                                  final Paint penColor, 
                                  final Paint bkColor, 
                                  final Stroke pen)
   {
      Paint prevColor = dc.getPaint();

      // Select the pen, but save off the current one first.
      Stroke prevPen = dc.getStroke();
      dc.setPaint(bkColor);
      fillPolygon(dc, polygon);
      dc.setStroke(pen);
      dc.setPaint(penColor);
      framePolygon(dc, polygon);
      dc.setPaint(prevColor);
      dc.setStroke(prevPen);
   }

   public static void drawPolygon(Graphics2D dc, GeneralPath polygon, final Color penColor, final Color bkColor,  final Stroke pen)
	{
		Color prevColor = dc.getColor();

		// Select the pen, but save off the current one first.
		Stroke prevPen = dc.getStroke();
		dc.setColor(bkColor);
		fillPolygon(dc, polygon);
		dc.setStroke(pen);
		dc.setColor(penColor);
		framePolygon(dc, polygon);
		dc.setColor(prevColor);
		dc.setStroke(prevPen);		
	}
   
   /*
    * Get the text extent, a small margin is added to the width to avoid drawing on its bounding rectangle
    */
   public static IETSize getTextExtent(Graphics2D dc, Font font, String text)
   {
      return dc != null && font != null ? getTextExtent( dc.getFontMetrics( font ), text ) : null;
   }
   
   /*
    * Get the text extent, a small margin is added to the width to avoid drawing on its bounding rectangle
    */
   public static IETSize getTextExtent(Graphics2D dc, String text)
   {
		return dc != null ? getTextExtent( dc.getFontMetrics(), text ) : null;
   }
   
   /*
    * Get the text extent, a small margin is added to the width to avoid drawing on its bounding rectangle
    */
   public static IETSize getTextExtent( FontMetrics metrics, String text )
   {
      if ( metrics != null && text != null )
      {
         final int iBuffer = metrics.getMaxAdvance() / 2;
         return new ETSize(metrics.stringWidth(text) + iBuffer, metrics.getHeight());
      }
      
      return null;
   }

   /**
    * Draws text in the rectangle.  If the rect is less than SMALLEST_TEXT height then we don't draw
    */
   public static void drawText(Graphics2D dc, Font font, String text, IETRect rectText)
   {
      if( dc != null )
      {
         Font originalFont = dc.getFont();
         dc.setFont( font );
      
         drawText( dc, text, rectText );

         dc.setFont( originalFont );
      }
   }
   
   /**
    * Draws text in the rectangle.  If the rect is less than SMALLEST_TEXT height then we don't draw
    */
   public static void drawText(Graphics2D dc, String text, IETRect rectText)
   {
      // TODO add all the other capabilities for this function

      FontMetrics metrics = dc.getFontMetrics();
      if ((metrics != null) && (rectText.getHeight() > SMALLEST_TEXT))
      {
         Color prevColor = dc.getColor();

         final int iStartX = rectText.getLeft() + (metrics.getMaxAdvance() / 4);
         final int iBaseline = (int) (rectText.getTop() + rectText.getHeight()) - metrics.getMaxDescent();

         dc.setColor(Color.BLACK);
         dc.drawString(text, iStartX, iBaseline);
         dc.setColor(prevColor);
      }
   }

   /*
    * Return the input rectangle converted into a diamond points list, with a 90 degree rotational transform applied
    */
   public static ETList < IETPoint > getDiamondPolygonPoints(Graphics2D dc, Rectangle rect)
   {
      IETRect boundingRect = new ETRect(rect);
 /*
      Point centerPt = boundingRect.getCenterPoint();
		ETPoint top = new ETPoint(centerPt.x, boundingRect.getTop());
		ETPoint bottom = new ETPoint(centerPt.x, boundingRect.getBottom());
		ETPoint left = new ETPoint(boundingRect.getLeft(), centerPt.y);
		ETPoint right = new ETPoint(boundingRect.getRight(), centerPt.y);
*/
      int maxY = Math.max(boundingRect.getTop(), boundingRect.getBottom());
      int minY = Math.min(boundingRect.getTop(), boundingRect.getBottom());
      int maxX = Math.max(boundingRect.getLeft(), boundingRect.getRight());
      int minX = Math.min(boundingRect.getLeft(), boundingRect.getRight());

      ETPoint top = new ETPoint(minX + (int) (boundingRect.getWidth() / 2), minY + (int) boundingRect.getHeight());
      ETPoint bottom = new ETPoint(minX + (int) (boundingRect.getWidth() / 2), maxY + (int) boundingRect.getHeight());
      ETPoint left = new ETPoint(minX, maxY + (int) (boundingRect.getHeight() / 2));
      ETPoint right = new ETPoint(maxX, left.getY());

      ETList < IETPoint > polylinePoints = new ETArrayList < IETPoint > ();

      polylinePoints.add(top);
      polylinePoints.add(left);
      polylinePoints.add(bottom);
      polylinePoints.add(right);
      polylinePoints.add(top);
      return polylinePoints;
   }

   /*
    * Return the input rectangle converted into Polygon converted into a diamond, with a 90 degree rotational transform applied
    */
   public static GeneralPath getDiamondPolygon(Graphics2D dc, Rectangle rect)
   {
      return getPolygon(getDiamondPolygonPoints(dc, rect));
   }

   /*
    * Returns the Pen or Stroke used to draw the Path Digraph.
    */
   public static Stroke getLineStroke(int nLineKind, float width)
   {
      return m_stokeCache.getStroke(nLineKind, width);
   }

   public static void drawArrowHead(IDrawInfo drawInfo, IETEdge pOwnerEdge, IETEdgeUI pOwnerEdgeView, TSConstPoint fromPoint, TSConstPoint toPoint, int nArrowheadKind, Color nEdgeBackgroundColor, Color nSelectedEdgeBackgroundColor)
   {
      boolean selected = pOwnerEdge != null ? pOwnerEdge.isSelected() : false;

      IETArrowHead arrowHead = ETArrowHeadFactory.create(nArrowheadKind);
      TSEColor color = new TSEColor(selected ? nSelectedEdgeBackgroundColor : nEdgeBackgroundColor);
      if (arrowHead != null)
         arrowHead.draw(drawInfo, fromPoint, toPoint, color);
   }

   protected static ETStrokeCache m_stokeCache = new ETStrokeCache();
}
