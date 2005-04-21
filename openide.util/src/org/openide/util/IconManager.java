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
package org.openide.util;

import org.openide.ErrorManager;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import java.lang.ref.*;

import java.util.*;


/** Registers all loaded images into the AbstractNode, so nothing is loaded twice.
*
* @author Jaroslav Tulach
*/
final class IconManager extends Object {
    /** a value that indicates that the icon does not exists */
    private static final Object NO_ICON = new Object();

    /** map of resource name to loaded icon (String, SoftRefrence (Image)) or (String, NO_ICON) */
    private static final HashMap map = new HashMap(128);
    private static final HashMap localizedMap = new HashMap(128);

    /** Resource paths for which we have had to strip initial slash.
     * @see "#20072"
     */
    private static final Set extraInitialSlashes = new HashSet(); // Set<String>
    private static ClassLoader currentLoader = null;
    private static Lookup.Result loaderQuery = null;
    private static boolean noLoaderWarned = false;
    private static final Component component = new Component() {
        };

    private static final MediaTracker tracker = new MediaTracker(component);
    private static int mediaTrackerID;

    /**
     * Get the class loader from lookup.
     * Since this is done very frequently, it is wasteful to query lookup each time.
     * Instead, remember the last result and just listen for changes.
     */
    private static ClassLoader getLoader() {
        if (currentLoader == null) {
            if (loaderQuery == null) {
                loaderQuery = Lookup.getDefault().lookup(new Lookup.Template(ClassLoader.class));
                loaderQuery.addLookupListener(
                    new LookupListener() {
                        public void resultChanged(LookupEvent ev) {
                            currentLoader = null;
                        }
                    }
                );
            }

            Iterator it = loaderQuery.allInstances().iterator();

            if (it.hasNext()) {
                currentLoader = (ClassLoader) it.next();
            } else if (!noLoaderWarned) {
                noLoaderWarned = true;
                ErrorManager.getDefault().log(
                    ErrorManager.WARNING, "No ClassLoader instance found in " + Lookup.getDefault()
                );
            }
        }

        return currentLoader;
    }

    static Image getIcon(String resource, boolean localized) {
        if (localized) {
            synchronized (localizedMap) {
                Object img = localizedMap.get(resource);

                // no icon for this name (already tested)
                if (img == NO_ICON) {
                    return null;
                }

                if (img != null) {
                    // then it is SoftRefrence
                    img = ((Reference) img).get();
                }

                // icon found
                if (img != null) {
                    return (Image) img;
                }

                // find localized or base image
                ClassLoader loader = getLoader();

                // we'll keep the String probably for long time, optimize it
                resource = new String(resource).intern();

                String base;
                String ext;
                int idx = resource.lastIndexOf('.');

                if ((idx != -1) && (idx > resource.lastIndexOf('/'))) {
                    base = resource.substring(0, idx);
                    ext = resource.substring(idx);
                } else {
                    base = resource;
                    ext = ""; // NOI18N
                }

                // #31008. [PENDING] remove in case package cache is precomputed
                Image baseVariant = getIcon(resource /*base + ext*/, loader, false);
                Iterator it = NbBundle.getLocalizingSuffixes();

                while (it.hasNext()) {
                    String suffix = (String) it.next();
                    Image i;

                    if (suffix.length() == 0) {
                        i = baseVariant;
                    } else {
                        i = getIcon(base + suffix + ext, loader, true);
                    }

                    if (i != null) {
                        localizedMap.put(resource, new ActiveRef(i, localizedMap, resource));

                        return i;
                    }
                }

                localizedMap.put(resource, NO_ICON);

                return null;
            }
        } else {
            return getIcon(resource);
        }
    }

    private static Image getIcon(String name) {
        Object img = map.get(name);

        // no icon for this name (already tested)
        if (img == NO_ICON) {
            return null;
        }

        if (img != null) {
            // then it is SoftRefrence
            img = ((Reference) img).get();
        }

        // icon found
        if (img != null) {
            return (Image) img;
        }

        ClassLoader loader = getLoader();

        // we'll keep the String probably for long time, optimize it
        name = new String(name).intern();

        return getIcon(name, loader, false);
    }

    /** Finds imager for given resource.
    * @param name name of the resource
    * @param loader classloader to use for locating it, or null to use classpath
    * @param localizedQuery whether the name contains some localization suffix
    *  and is not optimized/interned
    */
    private static Image getIcon(String name, ClassLoader loader, boolean localizedQuery) {
        Object img = map.get(name);

        // no icon for this name (already tested)
        if (img == NO_ICON) {
            return null;
        }

        if (img != null) {
            // then it is SoftRefrence
            img = ((Reference) img).get();
        }

        // icon found
        if (img != null) {
            return (Image) img;
        }

        synchronized (map) {
            // again under the lock
            img = map.get(name);

            // no icon for this name (already tested)
            if (img == NO_ICON) {
                return null;
            }

            if (img != null) {
                // then it is SoftRefrence
                img = ((Reference) img).get();
            }

            if (img != null) {
                // cannot be NO_ICON, since it never disappears from the map.
                return (Image) img;
            }

            // path for bug in classloader
            String n;
            boolean warn;

            if (name.startsWith("/")) { // NOI18N
                warn = true;
                n = name.substring(1);
            } else {
                warn = false;
                n = name;
            }

            // we have to load it
            java.net.URL url = (loader != null) ? loader.getResource(n)
                                                : IconManager.class.getClassLoader().getResource(n);

            img = (url == null) ? null : Toolkit.getDefaultToolkit().createImage(url);

            if (img != null) {
                if (warn && extraInitialSlashes.add(name)) {
                    ErrorManager.getDefault().log(
                        ErrorManager.WARNING, "Initial slashes in Utilities.loadImage deprecated (cf. #20072): " +
                        name
                    ); // NOI18N
                }

                Image img2 = toBufferedImage((Image) img);

                //System.err.println("loading icon " + n + " = " + img2);
                // nonlocalized queries already have the String optimized
                if (localizedQuery) {
                    name = new String(name).intern();
                }

                map.put(name, new ActiveRef(img2, map, name));

                return (Image) img2;
            } else { // no icon found

                if (!localizedQuery) {
                    map.put(name, NO_ICON);
                }

                return null;
            }
        }
    }

    /**
     * Method that attempts to find the merged image in the cache first, then
     * creates the image if it was not found.
     */
    static final Image mergeImages(Image im1, Image im2, int x, int y) {
        CompositeImageKey k = new CompositeImageKey(im1, im2, x, y);
        Image cached;

        synchronized (map) {
            Reference r = (Reference) map.get(k);

            if (r != null) {
                cached = (Image) r.get();

                if (cached != null) {
                    return cached;
                }
            }

            cached = doMergeImages(im1, im2, x, y);
            map.put(k, new ActiveRef(cached, map, k));

            return cached;
        }
    }

    /** The method creates a BufferedImage which represents the same Image as the
     * parameter but consumes less memory.
     */
    static final Image toBufferedImage(Image img) {
        // load the image
        new javax.swing.ImageIcon(img);

        java.awt.image.BufferedImage rep = createBufferedImage(img.getWidth(null), img.getHeight(null));
        java.awt.Graphics g = rep.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        img.flush();

        return rep;
    }

    private static void ensureLoaded(Image image) {
        if (
            (Toolkit.getDefaultToolkit().checkImage(image, -1, -1, null) &
                (ImageObserver.ALLBITS | ImageObserver.FRAMEBITS)) != 0
        ) {
            return;
        }

        synchronized (tracker) {
            int id = ++mediaTrackerID;

            tracker.addImage(image, id);

            try {
                tracker.waitForID(id, 0);
            } catch (InterruptedException e) {
                System.out.println("INTERRUPTED while loading Image");
            }

            assert (tracker.statusID(id, false) == MediaTracker.COMPLETE) : "Image loaded";
            tracker.removeImage(image, id);
        }
    }

    private static final Image doMergeImages(Image image1, Image image2, int x, int y) {
        ensureLoaded(image1);
        ensureLoaded(image2);

        int w = Math.max(image1.getWidth(null), x + image2.getWidth(null));
        int h = Math.max(image1.getHeight(null), y + image2.getHeight(null));

        java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                      .getDefaultScreenDevice().getDefaultConfiguration()
                                                                      .getColorModel(java.awt.Transparency.BITMASK);
        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage(
                model, model.createCompatibleWritableRaster(w, h), model.isAlphaPremultiplied(), null
            );

        java.awt.Graphics g = buffImage.createGraphics();
        g.drawImage(image1, 0, 0, null);
        g.drawImage(image2, x, y, null);
        g.dispose();

        return buffImage;
    }

    /** Creates BufferedImage with Transparency.TRANSLUCENT */
    static final java.awt.image.BufferedImage createBufferedImage(int width, int height) {
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        }

        java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                      .getDefaultScreenDevice().getDefaultConfiguration()
                                                                      .getColorModel(java.awt.Transparency.TRANSLUCENT);
        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage(
                model, model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null
            );

        return buffImage;
    }

    /**
     * Key used for composite images -- it holds image identities
     */
    private static class CompositeImageKey {
        Image baseImage;
        Image overlayImage;
        int x;
        int y;

        CompositeImageKey(Image base, Image overlay, int x, int y) {
            this.x = x;
            this.y = y;
            this.baseImage = base;
            this.overlayImage = overlay;
        }

        public boolean equals(Object other) {
            if (!(other instanceof CompositeImageKey)) {
                return false;
            }

            CompositeImageKey k = (CompositeImageKey) other;

            return (x == k.x) && (y == k.y) && (baseImage == k.baseImage) && (overlayImage == k.overlayImage);
        }

        public int hashCode() {
            int hash = ((x << 3) ^ y) << 4;
            hash = hash ^ baseImage.hashCode() ^ overlayImage.hashCode();

            return hash;
        }

        public String toString() {
            return "Composite key for " + baseImage + " + " + overlayImage + " at [" + x + ", " + y + "]"; // NOI18N
        }
    }

    /** Cleaning reference. */
    private static final class ActiveRef extends SoftReference implements Runnable {
        private Map holder;
        private Object key;

        public ActiveRef(Object o, Map holder, Object key) {
            super(o, Utilities.activeReferenceQueue());
            this.holder = holder;
            this.key = key;
        }

        public void run() {
            synchronized (holder) {
                holder.remove(key);
            }
        }
    }
     // end of ActiveRef
}
