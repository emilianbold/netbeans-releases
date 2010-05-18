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

package org.netbeans.modules.uml.utils;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

/**
 * The <code>LinearGradientPaint</code> class provides a way to fill
 * a {@link java.awt.Shape} with a linear color gradient pattern.  The user may
 * specify 2 or more gradient colors, and this paint will provide an
 * interpolation between each color.  The user also specifies start and end
 * points which define where in user space the color gradient should begin
 * and end.
 * <p>
 * The user must provide an array of floats specifying how to distribute the
 * colors along the gradient.  These values should range from 0.0 to 1.0 and
 * act like keyframes along the gradient (they mark where the gradient should
 * be exactly a particular color).
 * <p>
 * For example:
 * <br>
 * <code>
 * <p>
 * Point2D start = new Point2D.Float(0, 0);<br>
 * Point2D end = new Point2D.Float(100,100);<br>
 * float[] dist = {0.0, 0.2, 1.0};<br>
 * Color[] colors = {Color.red, Color.white, Color.blue};<br>
 * LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colors);
 * </code>
 *<p>
 * This code will create a LinearGradientPaint which interpolates between
 * red and white for the first 20% of the gradient and between white and blue
 * for the remaining 80%.
 *
 * <p> In the event that the user does not set the first keyframe value equal
 * to 0 and the last keyframe value equal to 1, keyframes will be created at
 * these positions and the first and last colors will be replicated there.
 * So, if a user specifies the following arrays to construct a gradient:<br>
 * {Color.blue, Color.red}, {.3, .7}<br>
 * this will be converted to a gradient with the following keyframes:
 * {Color.blue, Color.blue, Color.red, Color.red}, {0, .3, .7, 1}
 *
 * <p>
 * The user may also select what action the LinearGradientPaint should take
 * when filling color outside the start and end points. If no cycle method is
 * specified, NO_CYCLE will be chosen by default, so the endpoint colors
 * will be used to fill the remaining area.
 *
 * <p> The following image demonstrates the options NO_CYCLE and REFLECT.
 *
 * <p>
 * <img src = "cyclic.jpg">
 *
 * <p> The colorSpace parameter allows the user to specify in which colorspace
 *  the interpolation should be performed, default sRGB or linearized RGB.
 *
 *
 * @author Nicholas Talian, Vincent Hardy, Jim Graham, Jerry Evans
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: LinearGradientPaint.java,v 1.1.2.2 2007/11/19 23:54:02 sherylsu Exp $
 * @see java.awt.Paint
 * @see java.awt.Graphics2D#setPaint
 *
 */
public final class LinearGradientPaint extends MultipleGradientPaint
{
    
    /** Gradient start and end points. */
    private Point2D start, end;
    
    /**<p>
     * Constructs an <code>LinearGradientPaint</code> with the default
     * NO_CYCLE repeating method and SRGB colorspace.
     *
     * @param startX the x coordinate of the gradient axis start point
     * in user space
     *
     * @param startY the y coordinate of the gradient axis start point
     * in user space
     *
     * @param endX the x coordinate of the gradient axis end point
     * in user space
     *
     * @param endY the y coordinate of the gradient axis end point
     * in user space
     *
     * @param fractions numbers ranging from 0.0 to 1.0 specifying the
     * distribution of colors along the gradient
     *
     * @param colors array of colors corresponding to each fractional value
     *
     *
     * @throws IllegalArgumentException if start and end points are the
     * same points, or if fractions.length != colors.length, or if colors
     * is less than 2 in size.
     *
     */
    public LinearGradientPaint(float startX, float startY,
            float endX, float endY,
            float[] fractions, Color[] colors)
    {
        
        this(new Point2D.Float(startX, startY),
                new Point2D.Float(endX, endY),
                fractions,
                colors,
                NO_CYCLE,
                SRGB);
    }
    
    /**<p>
     * Constructs an <code>LinearGradientPaint</code> with default SRGB
     * colorspace.
     *
     * @param startX the x coordinate of the gradient axis start point
     * in user space
     *
     * @param startY the y coordinate of the gradient axis start point
     * in user space
     *
     * @param endX the x coordinate of the gradient axis end point
     * in user space
     *
     * @param endY the y coordinate of the gradient axis end point
     * in user space
     *
     * @param fractions numbers ranging from 0.0 to 1.0 specifying the
     * distribution of colors along the gradient
     *
     * @param colors array of colors corresponding to each fractional value
     *
     * @param cycleMethod either NO_CYCLE, REFLECT, or REPEAT
     *
     * @throws IllegalArgumentException if start and end points are the
     * same points, or if fractions.length != colors.length, or if colors
     * is less than 2 in size.
     *
     */
    public LinearGradientPaint(float startX, float startY,
            float endX, float endY,
            float[] fractions, Color[] colors,
            CycleMethodEnum cycleMethod)
    {
        this(new Point2D.Float(startX, startY),
                new Point2D.Float(endX, endY),
                fractions,
                colors,
                cycleMethod,
                SRGB);
    }
    
    /**<p>
     * Constructs a <code>LinearGradientPaint</code> with the default
     * NO_CYCLE repeating method and SRGB colorspace.
     *
     * @param start the gradient axis start <code>Point</code> in user space
     *
     * @param end the gradient axis end <code>Point</code> in user space
     *
     * @param fractions numbers ranging from 0.0 to 1.0 specifying the
     * distribution of colors along the gradient
     *
     * @param colors array of colors corresponding to each fractional value
     *
     * @throws NullPointerException if one of the points is null
     *
     * @throws IllegalArgumentException if start and end points are the
     * same points, or if fractions.length != colors.length, or if colors
     * is less than 2 in size.
     *
     */
    public LinearGradientPaint(Point2D start, Point2D end, float[] fractions,
            Color[] colors)
    {
        
        this(start, end, fractions, colors, NO_CYCLE, SRGB);
    }
    
    /**<p>
     * Constructs a <code>LinearGradientPaint</code>.
     *
     * @param start the gradient axis start <code>Point</code> in user space
     *
     * @param end the gradient axis end <code>Point</code> in user space
     *
     * @param fractions numbers ranging from 0.0 to 1.0 specifying the
     * distribution of colors along the gradient
     *
     * @param colors array of colors corresponding to each fractional value
     *
     * @param cycleMethod either NO_CYCLE, REFLECT, or REPEAT
     *
     * @param colorSpace which colorspace to use for interpolation,
     * either SRGB or LINEAR_RGB
     *
     * @throws NullPointerException if one of the points is null
     *
     * @throws IllegalArgumentException if start and end points are the
     * same points, or if fractions.length != colors.length, or if colors
     * is less than 2 in size.
     *
     */
    public LinearGradientPaint(Point2D start, Point2D end, float[] fractions,
            Color[] colors,
            CycleMethodEnum cycleMethod,
            ColorSpaceEnum colorSpace)
    {
        
        this(start, end, fractions, colors, cycleMethod, colorSpace,
                new AffineTransform());
        
    }
    
    /**<p>
     * Constructs a <code>LinearGradientPaint</code>.
     *
     * @param start the gradient axis start <code>Point</code> in user space
     *
     * @param end the gradient axis end <code>Point</code> in user space
     *
     * @param fractions numbers ranging from 0.0 to 1.0 specifying the
     * distribution of colors along the gradient
     *
     * @param colors array of colors corresponding to each fractional value
     *
     * @param cycleMethod either NO_CYCLE, REFLECT, or REPEAT
     *
     * @param colorSpace which colorspace to use for interpolation,
     * either SRGB or LINEAR_RGB
     *
     * @param gradientTransform transform to apply to the gradient
     *
     * @throws NullPointerException if one of the points is null,
     * or gradientTransform is null
     *
     * @throws IllegalArgumentException if start and end points are the
     * same points, or if fractions.length != colors.length, or if colors
     * is less than 2 in size.
     *
     */
    public LinearGradientPaint(Point2D start, Point2D end, float[] fractions,
            Color[] colors,
            CycleMethodEnum cycleMethod,
            ColorSpaceEnum colorSpace,
            AffineTransform gradientTransform)
    {
        super(fractions, colors, cycleMethod, colorSpace, gradientTransform);
        
        //
        // Check input parameters
        //
        if (start == null || end == null)
        {
            throw new NullPointerException("Start and end points must be" +
                    "non-null");
        }
        
//        if (start.equals(end))
//        {
//            throw new IllegalArgumentException("Start point cannot equal" +
//                    "endpoint");
//        }
        
        //copy the points...
        this.start = (Point2D)start.clone();
        
        this.end = (Point2D)end.clone();
        
    }
    
    /**
     * Creates and returns a PaintContext used to generate the color pattern,
     * for use by the internal rendering engine.
     *
     * @param cm {@link ColorModel} that receives
     * the <code>Paint</code> data. This is used only as a hint.
     *
     * @param deviceBounds the device space bounding box of the
     * graphics primitive being rendered
     *
     * @param userBounds the user space bounding box of the
     * graphics primitive being rendered
     *
     * @param transform the {@link AffineTransform} from user
     * space into device space
     *
     * @param hints the hints that the context object uses to choose
     * between rendering alternatives
     *
     * @return the {@link PaintContext} that generates color patterns.
     *
     * @see PaintContext
     */
    public PaintContext createContext(ColorModel cm,
            Rectangle deviceBounds,
            Rectangle2D userBounds,
            AffineTransform transform,
            RenderingHints hints)
    {
        
        if(start.equals(end) == false)
        {
            // Can't modify the transform passed in...
            transform = new AffineTransform(transform);
            //incorporate the gradient transform
            transform.concatenate(gradientTransform);

            try
            {
                Point2D pt1 = new Point2D.Double(userBounds.getWidth() * start.getX(),
                                                 userBounds.getHeight() * start.getY());
                Point2D pt2 = new Point2D.Double(userBounds.getWidth() * end.getX(),
                                                 userBounds.getHeight() * end.getY());

                return new LinearGradientPaintContext(cm,
                        deviceBounds,
                        userBounds,
                        transform,
                        hints,
                        pt1,
                        pt2,
                        fractions,
                        this.getColors(),
                        cycleMethod,
                        colorSpace);
            }

            catch(NoninvertibleTransformException e)
            {
                e.printStackTrace();
                throw new IllegalArgumentException("transform should be" +
                        "invertible");
            }
        }
        else
        {
            // Invalid Range
            Color[] colors = getColors();
            if((colors != null) && (colors.length > 0))
            {
                Paint p = colors[0];
                return p.createContext(cm, deviceBounds, userBounds, transform, hints);
            }
        }
        
        return null;
    }
    
    /**
     * Returns a copy of the start point of the gradient axis
     * @return a {@link Point2D} object that is a copy of the point
     * that anchors the first color of this
     * <code>LinearGradientPaint</code>.
     */
    public Point2D getStartPoint()
    {
        return new Point2D.Double(start.getX(), start.getY());
    }
    
    /** Returns a copy of the end point of the gradient axis
     * @return a {@link Point2D} object that is a copy of the point
     * that anchors the last color of this
     * <code>LinearGradientPaint</code>.
     */
    public Point2D getEndPoint()
    {
        return new Point2D.Double(end.getX(), end.getY());
    }
    
}
