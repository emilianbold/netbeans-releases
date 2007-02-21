/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.mapper.common.util;

import java.awt.Color;
import java.awt.Component;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * a swing utility class</p>
 *
 * @author    JoneLin
 * @created   December 3, 2002
 */
public class SbynSwingUtil {
    /**
     * Description of the Field
     */
    protected static SbynSwingUtil util = new SbynSwingUtil();

    /**
     * Constructor for the SbynSwingUtil object
     */
    protected SbynSwingUtil() { }

    /**
     * Get an URL from url string.
     *
     * @param url     url string to be converted to URL.
     * @param aClass - a class
     * @return        a URL.
     */
    public static URL getURL(String url, Class aClass) {
        try {
            return aClass.getResource(url).openConnection().getURL();
        } catch (Exception e) {
            System.err.println("Error getting " + url);
            return null;
        }
    }

    /**
     * Creates URL from a url string.
     *
     * @param url  url string to be converted to URL.
     * @return     a URL.
     */
    public static URL getURL(String url) {
        return getURL(url, util.getClass());
    }

    /**
     * Creates an icon from the given url.
     *
     * @param url  the url representing an image file, either .jpg or
     *      .gif
     * @return     the icon
     */
    public static Icon getIcon(String url) {
        return getIcon(url, util.getClass());
    }

    /**
     * Creates an icon from the given url.
     *
     * @param url     the url representing an image file, either .jpg or
     *      .gif
     * @param aClass - a class
     * @return        Icon the icon
     */
    public static Icon getIcon(String url, Class aClass) {
        try {
            return new ImageIcon(aClass.getResource(url).openConnection().getURL());
        } catch (Exception e) {
            System.err.println("Error getting " + url);
            return null;
        }
    }

    /**
     * Creates an icon from the given url.
     *
     * @param url     the url representing an image file, either .jpg or
     *      .gif
     * @return        Icon the icon
     */
    public static Icon getIcon(URL url) {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(url);
        } catch (Exception io) {
            io.printStackTrace();
        }
        return icon;
    }

    /**
     * Center the given window on the screen.
     *
     * @param w  the window
     */
    public static void centerOnScreen(Window w) {
        Dimension mScreenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Dimension mFrameSize = w.getSize();
        Point mFrameLocation = new Point(
            mScreenSize.width / 2 - mFrameSize.width / 2,
            mScreenSize.height / 2 - mFrameSize.height / 2);
        w.setLocation(mFrameLocation);
    }

    /**
     * Set window size to cover the screen.
     *
     * @param w  the window
     */
    public static void setToScreenSize(Window w) {
        Dimension mScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        w.setSize(mScreenSize);
    }

    /**
     * Creates an image from the given url.
     *
     * @param url  the url representing an image file, either .jpg or
     *      .gif
     * @return     Image the image
     */
    public static Image getImage(String url) {
        return getImage(url, util.getClass());
    }

    /**
     * Creates an image from the given url.
     *
     * @param url     the url representing an image file, either .jpg or
     *      .gif
     * @param aClass  class resource
     * @return        Image
     */
    public static Image getImage(String url, Class aClass) {
        try {
            ImageIcon icon = new ImageIcon(aClass.getResource(url).openConnection().getURL());
            return icon.getImage();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * returns the point with shortest distance to the target point
     *
     * @param p1      point to be tested
     * @param p2      point to be tested
     * @param target  the target point to be tested against
     * @return        the point with shortest distance to the target
     */
    public static Point minDistance(Point p1, Point p2, Point target) {
        double p1Dist = calculateDistance(p1.x, p1.y, target.x, target.y);
        double p2Dist = calculateDistance(p2.x, p2.y, target.x, target.y);
        if (p1Dist < p2Dist) {
            return p1;
        } else {
            return p2;
        }
    }

    /**
     * returns the distance between 2 points
     *
     * @param ax  x coordinates for point 1
     * @param ay  y coordiates for point 1
     * @param bx  x coordinate for point 2
     * @param by  y coordinate for point 3
     * @return    the distance between 2 points
     */
    public static double calculateDistance(int ax, int ay, int bx, int by) {
        double sum = (bx - ax) * (bx - ax) + (by - ay) * (by - ay);
        return Math.sqrt(sum);
    }

    /**
     * create a color based on the hex string (example: "ff9f00") if
     * value is null, return null;
     *
     * @param value  Description of the Parameter
     * @return       Description of the Return Value
     */
    public static Color createColor(String value) {
        Color retColor = null;
        try {
            if ((value != null) && (value.length() > 0)) {
                int pixel = Integer.parseInt(value, 16
                /*
                 * radix
                 */
                    );
                retColor = new Color(pixel);
            }
        } catch (NumberFormatException ex) {
            // may be "white", "black"?
            // try lookup?
            // or even egate standard color name such as "egate.icon.background"
            return null;
        }
        return retColor;
    }

    /**
     * Description of the Method
     *
     * @param component  Description of the Parameter
     * @param x          Description of the Parameter
     * @param y          Description of the Parameter
     * @return           Description of the Return Value
     */
    public static boolean componentContainsPos(Component component, int x, int y) {
        return componentContainsPos(component, x, y, false);
    }

    /**
     * Description of the Method
     *
     * @param component         Description of the Parameter
     * @param x                 Description of the Parameter
     * @param y                 Description of the Parameter
     * @param reduceLowerBound  Description of the Parameter
     * @return                  Description of the Return Value
     */
    public static boolean componentContainsPos(Component component, int x, int y, boolean reduceLowerBound) {

        int xx = component.getX();
        int yy = component.getY();
        if (reduceLowerBound) {
            xx = xx - component.getWidth();
            yy = yy - component.getHeight();
        }

        int xxRight = component.getX();
        int yyBottom = component.getY();
        if (!reduceLowerBound) {
            xxRight = xxRight + component.getWidth();

        }
        yyBottom = yyBottom + component.getHeight();
        if (xx <= x && x <= xxRight && yy <= y && y <= yyBottom) {
            return true;
        }
        return false;
    }

}
