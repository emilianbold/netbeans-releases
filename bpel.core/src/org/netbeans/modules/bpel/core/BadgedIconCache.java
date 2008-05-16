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
package org.netbeans.modules.bpel.core;

import java.awt.Image;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;

/**
 * copied from Jato and LiteJ2eePlugin module
 *
 * @author  Mike Frisino
 * @author  Matt Stevens (revisons on initial theft)
 * @version
 */
abstract class BadgedIconCache {

    private static Map<String,Image> theCache = Collections.synchronizedMap(new WeakHashMap<String,Image>(101));

    public static final int NW_BADGE_X = 8;
    public static final int NW_BADGE_Y = 0;
    public static final int NE_BADGE_X = 16;
    public static final int NE_BADGE_Y = 0;
    public static final int SE_BADGE_X = 16;
    public static final int SE_BADGE_Y = 8;
    public static final int SW_BADGE_X = 8;
    public static final int SW_BADGE_Y = 8;
    
  
    public static final String DEFAULT_ICON = "org/openide/resources/defaultNode.gif"; // NOI18N
    public static final String DEFAULT_ERROR_BADGE = "org/netbeans/modules/bpel/core/resources/errorbadge.gif"; // NOI18N
    public static final String DEFAULT_WARNING_BADGE = "org/netbeans/modules/bpel/core/resources/warningbadge.gif"; // NOI18N
    
    private static final String ERROR_KEY_PREFIX = "ERR";                 // NOI18N
    private static final String WARNING_KEY_PREFIX = "WRN";               // NOI18N
    
    public static Image getErrorIcon(String base) {
        return getBadgedIcon(base,null,null,null,DEFAULT_ERROR_BADGE);
    }
    
    public static Image getWarningIcon(String base) {
        return getBadgedIcon(base,null,null,null,DEFAULT_WARNING_BADGE);
    }

    public static Image getErrorIcon(Image baseImage) {
        if (null == baseImage) {
            return null;
        }
        String key = ERROR_KEY_PREFIX + baseImage.toString();
        Image result = (Image)theCache.get(key);

        if (result == null) {
            result = createBadgedIcon(
                baseImage, null, null, null, DEFAULT_ERROR_BADGE);
            theCache.put(key, result);
        }
        return result;      
    }
    
    public static Image getWarningIcon(Image baseImage) {
        if (null == baseImage) {
            return null;
        }
        String key = WARNING_KEY_PREFIX + baseImage.toString();
        Image result = (Image)theCache.get(key);

        if (result == null) {
            result = createBadgedIcon(
                baseImage, null, null, null, DEFAULT_WARNING_BADGE);
            theCache.put(key, result);
        }
        return result;      
    }
    
    /**
     * Get a badged icon constructed from the icons given as method arguments.
     * Creates a key for the icon from the String arguments, and checks if we already
     * have a cached icon for that key; if so, return it; if not, create the icon
     * and add to cache.
     * <p>
     * Any argument, except "base", can be null; a null argument means "no badge
     * desired in that quadrant".  Note that there is a rule in the "Icon Badging"
     * specification which says that if the error badge is used, then
     * no other NW or SW badges should be used; that rule is *not* enforced by this
     * method, so it is up to the caller to obey it.
     * <p>
     * If you encounter problems with this method, then turn on logger messages for
     * Type: dbg, Group: 7, Level: 200, Module Name: com.sun.forte4j.j2ee.lib,
     * run it again and look for output trace messages.
     * <p>
     * All method parameters give the class loader path of a .gif (or equivalent)
     * resource; parameters are in order starting with NW quadrant and going clockwise.
     * Notes: these names are cAsE sensitive, even on non-case-sensitive OS's.;
     *        if no file extension is given, then ".gif" is assumed.
     * <p>
     * @param base classloader path for the base icon.
     * @param nwBadge classloader path of the icon file for the NW quadrant badge.
     * @param neBadge classloader path of the icon file for the NE quadrant badge.
     * @param seBadge classloader path of the icon file for the SE quadrant badge.
     * @param swBadge classloader path of the icon file for the SW quadrant badge.
     */
    public static Image getBadgedIcon(String base, String nwBadge, String neBadge, String seBadge, String swBadge) {
        if (base == null) {
            return null;
        }
        String key = buildIconKey(base, nwBadge, neBadge, seBadge, swBadge);
        Image result = (Image)theCache.get(key);

        if (result == null) {
            result = createBadgedIcon(base, nwBadge, neBadge, seBadge, swBadge);
            theCache.put(key, result);
        }
        return result;
    }

    /*
     * @param base the base icon.
     * @param nwBadge classloader path of the icon file for the NW quadrant badge.
     * @param neBadge classloader path of the icon file for the NE quadrant badge.
     * @param seBadge classloader path of the icon file for the SE quadrant badge.
     * @param swBadge classloader path of the icon file for the SW quadrant badge.
     */
    public static Image getBadgedIcon(String base, Image baseImage, String nwBadge, String neBadge, String seBadge, String swBadge) {
        if (base == null) {
            return null;
        }
        String key = buildIconKey(base, nwBadge, neBadge, seBadge, swBadge);
        Image result = (Image)theCache.get(key);

        if (result == null) {
            result = createBadgedIcon(baseImage, nwBadge, neBadge, seBadge, swBadge);
            theCache.put(key, result);
        }
        return result;
    }

    /**
     * Construct hash table key = concatenate all arguments with ";" between
     * each one and null replaced by the string "null".
     * Order (clockwise from nw): base;nw;ne;se;sw
     *
     * Known minor problem: if any of the .gif files can't be opened, would be
     * better to use "null" in the key for that badge.  Currently the bad file name
     * is used to construct the key.
     */
    private static String buildIconKey(String base, String nwBadge, String neBadge, String seBadge, String swBadge) {
        String nullString = new String("null"); // NOI18N
        Object[] params = new Object[] { 
            normalizeGifPath(base),
            ((nwBadge == null) ? nullString : normalizeGifPath(nwBadge)),
            ((neBadge == null) ? nullString : normalizeGifPath(neBadge)),
            ((seBadge == null) ? nullString : normalizeGifPath(seBadge)),
            ((swBadge == null) ? nullString : normalizeGifPath(swBadge)),
        };
        return MessageFormat.format("{0};{1};{2};{3};{4}", params); // NOI18N
    }

    /**
     * Instantiate an Image for each not-null argument.
     * Then call org.openide.util.Utilities.mergeImages() to do the overlaying.
     * return the resulting icon.
     *
     * Note: the special rule that "if swBadge = seriousErrorBadge then other
     * 3 quadrants must be blank" is *not* enforced by this method; therefore,
     * it is up to the caller to respect this and pass appropriate arguments.
     * 
     * Does not do caching.
     */
    public static Image createBadgedIcon(String base, String nwBadge, String neBadge, String seBadge, String swBadge) {
        Image baseImage = getIcon(base);

        if (baseImage == null) {
            baseImage = getIcon(DEFAULT_ICON);
        }
        return createBadgedIcon(baseImage,nwBadge,neBadge,seBadge,swBadge);
    }
    
    public static Image createBadgedIcon(Image baseImage, String nwBadge, String neBadge, String seBadge, String swBadge) {
        if (null == baseImage) {
            return null;
        }
        Image badgedImage = baseImage;
        
        // merge the icon for each quadrant with the base icon:
        if (nwBadge != null)
            badgedImage = mergeSingleImage(
                badgedImage, nwBadge, NW_BADGE_X, NW_BADGE_Y);

        if (neBadge != null)
            badgedImage = mergeSingleImage(
                badgedImage, neBadge, NE_BADGE_X, NE_BADGE_Y);

        if (seBadge != null)
            badgedImage = mergeSingleImage(
                badgedImage, seBadge, SE_BADGE_X, SE_BADGE_Y);

        if (swBadge != null)
            badgedImage = mergeSingleImage(
                badgedImage, swBadge, SW_BADGE_X, SW_BADGE_Y);

        return badgedImage;
    }

    public static Image mergeSingleImage(Image baseImage, String badge, int badge_x, int badge_y) {
        Image badgeImage = getIcon(badge);

        if (badgeImage == null) {
            return baseImage;
        }
        return Utilities.mergeImages(baseImage, badgeImage, badge_x, badge_y);
    }
    
    /**
     * Find icon resource file in cache if available; otherwise get from disk
     * and add to cache.
     */
    public static Image getIcon(String iconFile) {
        String filename = normalizeGifPath(iconFile);
        Image theImage = (Image)theCache.get(filename);

        if (theImage != null) {
            return theImage;    // cache hit
        }
        // got following line of code from openide.util.IconManager.java:
        // ("BadgedIconCache.class.getClassLoader().getResource(iconFile)" doesn't work)
        ClassLoader loader =  (ClassLoader)org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
        URL tmpURL = loader.getResource(filename);

        if (tmpURL == null) {
            return null;
        }
        theImage = new ImageIcon(tmpURL).getImage();
        theCache.put(filename, theImage);

        return theImage;
    }
    
    /**
     * Make the format of the .gif file path name a little more flexible:
     * fix so doesn't start with "/" and does end in ".gif".
     */
    public static String normalizeGifPath(String gifPath) {
        if (gifPath == null) {
            return gifPath;
        }
        String slash = "/"; // NOI18N
        String dot = ".";   // NOI18N
        
        if (gifPath.startsWith(slash)) {
            gifPath = gifPath.substring(slash.length());
        }
        if (gifPath.indexOf(dot) == -1) {
            gifPath = gifPath.concat(".gif");   // NOI18N
        }
        return gifPath;
    }
}
