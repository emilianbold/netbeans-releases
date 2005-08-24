/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.plaf.util;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** XP color scheme installer.
 *
 * @author  Dafe Simonek
 */
public final class UIUtils {
    private static HashMap hintsMap = null;
    private static final boolean noAntialias = 
        Boolean.getBoolean("nb.no.antialias"); //NOI18N

    /** true when XP style colors are installed into UI manager, false otherwise */ 
    private static boolean colorsReady = false;
            
    /** No need to instantiate this utility class. */
    private UIUtils() {
    }
    
    /** Finds if windows LF is active.
     * @return true if windows LF is active, false otherwise */
    public static boolean isWindowsLF () {
        if (Boolean.getBoolean("netbeans.winsys.forceclassic")) {
            return false;
        }
        String lfID = UIManager.getLookAndFeel().getID();
        return lfID.equals("Windows"); //NOI18N
    }
    
    /** Finds if windows LF with XP theme is active.
     * @return true if windows LF and XP theme is active, false otherwise */
    public static boolean isXPLF () {
        if (!isWindowsLF()) {
            return false;
        }
        Boolean isXP = (Boolean)Toolkit.getDefaultToolkit().
                        getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
        return isXP == null ? false : isXP.booleanValue();
    }

     public static final Map getHints() {
        //XXX should do this in update() in the UI instead
        //Note for this method we do NOT want only text antialiasing - we 
        //want antialiased curves.
        if (hintsMap == null) {
            hintsMap = new HashMap();
            hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            hintsMap.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        return hintsMap;
    }
    
    public static final void configureRenderingHints (Graphics g) {
        if (noAntialias) return;
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.addRenderingHints(getHints());
    }

    public static Image loadImage (String s) {
        if (openideAvailable == null) {
            checkOpenide();
        }
        if (Boolean.TRUE.equals(openideAvailable)) {
            return loadWithUtilities(s);
        } else {
            return loadWithImageIO(s);
        }
    }

    /** Computes "middle" color in terms of rgb color space. Ignores alpha
     * (transparency) channel
     */
    public static Color getMiddle (Color c1, Color c2) {
        return new Color((c1.getRed() + c2.getRed()) / 2,
                        (c1.getGreen() + c2.getGreen()) / 2,
                        (c1.getBlue() + c2.getBlue()) / 2);
    }

    private static void checkOpenide() {
        try {
            utilsClass = Class.forName("org.openide.util.Utilities"); //NOI18N
            utilsMethod = utilsClass.getDeclaredMethod ( "loadImage", new Class[] {String.class}); //NOI18N
            openideAvailable = Boolean.TRUE;
        } catch (Exception e) {
            openideAvailable = Boolean.FALSE;
        }
    }

    private static Image loadWithUtilities (String s) {
        Image result = null;
        try {
            result = (Image) utilsMethod.invoke ( null, new Object[] {s} );
        } catch (Exception e) {
            System.err.println ("Error loading image " + s); //NOI18N
            e.printStackTrace(); //XXX
        }
        return result;
    }

    private static Image loadWithImageIO (String s) {
        Image result = null;
        try {
            URL url = UIUtils.class.getResource ( s );
            result = ImageIO.read ( url );
        } catch (Exception e) {
            System.err.println ("Error loading image using ImageIO " + s); //NOI18N
            e.printStackTrace();
        }
        return result;
    }

    private static Boolean openideAvailable = null;
    private static Class utilsClass = null;
    private static Method utilsMethod = null;

    //XXX move/duplicate org.netbeans.swing.tabcontrol.plaf.ColorUtil gradient paint caching?
    public static GradientPaint getGradientPaint ( float x1, float y1, Color upper, float x2, float y2, Color lower,
                                                   boolean repeats ) {
        return new GradientPaint ( x1, y1, upper, x2, y2, lower, repeats );
    }
    

    public static Color adjustColor (Color c, int rDiff, int gDiff, int bDiff) {
        //XXX deleteme once converted to relative colors
        int red = Math.max(0, Math.min(255, c.getRed() + rDiff));
        int green = Math.max(0, Math.min(255, c.getGreen() + gDiff));
        int blue = Math.max(0, Math.min(255, c.getBlue() + bDiff));
        return new Color(red, green, blue);
    }    
    
    /**
     * Rotates a float value around 0-1
     */
    private static float minMax(float f) {
        return Math.max(0, Math.min(1, f));
    }
    
    public static boolean isBrighter(Color a, Color b) {
        int[] ac = new int[]{a.getRed(), a.getGreen(), a.getBlue()};
        int[] bc = new int[]{b.getRed(), b.getGreen(), b.getBlue()};
        int dif = 0;

        for (int i = 0; i < 3; i++) {
            int currDif = ac[i] - bc[i];
            if (Math.abs(currDif) > Math.abs(dif)) {
                dif = currDif;
            }
        }
        return dif > 0;
    }    
}
