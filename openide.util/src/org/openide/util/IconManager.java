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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.util;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** Registers all loaded images into the AbstractNode, so nothing is loaded twice.
*
* @author Jaroslav Tulach
*/
final class IconManager extends Object {
    /** a value that indicates that the icon does not exists */
    private static final ActiveRef<String> NO_ICON = new ActiveRef<String>(null, null, null);

    private static final Map<String,ActiveRef<String>> cache = new HashMap<String,ActiveRef<String>>(128);
    private static final Map<String,ActiveRef<String>> localizedCache = new HashMap<String,ActiveRef<String>>(128);
    private static final Map<CompositeImageKey,ActiveRef<CompositeImageKey>> compositeCache = new HashMap<CompositeImageKey,ActiveRef<CompositeImageKey>>(128);

    /** Resource paths for which we have had to strip initial slash.
     * @see "#20072"
     */
    private static final Set<String> extraInitialSlashes = new HashSet<String>();
    private static volatile Object currentLoader;
    private static Lookup.Result<ClassLoader> loaderQuery = null;
    private static boolean noLoaderWarned = false;
    private static final Component component = new Component() {
        };

    private static final MediaTracker tracker = new MediaTracker(component);
    private static int mediaTrackerID;
    
    private static ImageReader PNG_READER;
//    private static ImageReader GIF_READER;
    
    private static final Logger ERR = Logger.getLogger(IconManager.class.getName());

    static {
        ImageIO.setUseCache(false);
        PNG_READER = ImageIO.getImageReadersByMIMEType("image/png").next();
//        GIF_READER = ImageIO.getImageReadersByMIMEType("image/gif").next();
    }
    
    /**
     * Get the class loader from lookup.
     * Since this is done very frequently, it is wasteful to query lookup each time.
     * Instead, remember the last result and just listen for changes.
     */
    static ClassLoader getLoader() {
        Object is = currentLoader;
        if (is instanceof ClassLoader) {
            return (ClassLoader)is;
        }
            
        currentLoader = Thread.currentThread();
            
        if (loaderQuery == null) {
            loaderQuery = Lookup.getDefault().lookup(new Lookup.Template<ClassLoader>(ClassLoader.class));
            loaderQuery.addLookupListener(
                new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                        ERR.fine("Loader cleared"); // NOI18N
                        currentLoader = null;
                    }
                }
            );
        }

        Iterator it = loaderQuery.allInstances().iterator();
        if (it.hasNext()) {
            ClassLoader toReturn = (ClassLoader) it.next();
            if (currentLoader == Thread.currentThread()) {
                currentLoader = toReturn;
            }
            ERR.fine("Loader computed: " + currentLoader); // NOI18N
            return toReturn;
        } else { if (!noLoaderWarned) {
                noLoaderWarned = true;
                ERR.warning(
                    "No ClassLoader instance found in " + Lookup.getDefault() // NOI18N
                );
            }
            return null;
        }
    }

    static Image getIcon(String resource, boolean localized) {
        if (localized) {
            synchronized (localizedCache) {
                ActiveRef<String> ref = localizedCache.get(resource);
                Image img = null;

                // no icon for this name (already tested)
                if (ref == NO_ICON) {
                    return null;
                }

                if (ref != null) {
                    // then it is SoftRefrence
                    img = ref.get();
                }

                // icon found
                if (img != null) {
                    return img;
                }

                // find localized or base image
                ClassLoader loader = getLoader();

                // we'll keep the String probably for long time, optimize it
                resource = new String(resource).intern(); // NOPMD

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
                java.net.URL baseurl = (loader != null) ? loader.getResource(resource) // NOPMD
                        : IconManager.class.getClassLoader().getResource(resource);
                Iterator<String> it = NbBundle.getLocalizingSuffixes();
                
                while (it.hasNext()) {
                    String suffix = it.next();
                    Image i;

                    if (suffix.length() == 0) {
                        i = getIcon(resource, loader, false);
                    } else {
                        i = getIcon(base + suffix + ext, loader, true);
                    }

                    if (i != null) {
                        localizedCache.put(resource, new ActiveRef<String>(i, localizedCache, resource));

                        return i;
                    }
                }

                localizedCache.put(resource, NO_ICON);

                return null;
            }
        } else {
            return getIcon(resource, getLoader(), false);
        }
    }

    /** Finds image for given resource.
    * @param name name of the resource
    * @param loader classloader to use for locating it, or null to use classpath
    * @param localizedQuery whether the name contains some localization suffix
    *  and is not optimized/interned
    */
    private static Image getIcon(String name, ClassLoader loader, boolean localizedQuery) {
        ActiveRef<String> ref = cache.get(name);
        Image img = null;

        // no icon for this name (already tested)
        if (ref == NO_ICON) {
            return null;
        }

        if (ref != null) {
            img = ref.get();
        }

        // icon found
        if (img != null) {
            return img;
        }

        synchronized (cache) {
            // again under the lock
            ref = cache.get(name);

            // no icon for this name (already tested)
            if (ref == NO_ICON) {
                return null;
            }

            if (ref != null) {
                // then it is SoftRefrence
                img = ref.get();
            }

            if (img != null) {
                // cannot be NO_ICON, since it never disappears from the map.
                return img;
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

//            img = (url == null) ? null : Toolkit.getDefaultToolkit().createImage(url);
            Image result = null;
            try {
                if (url != null) {
                    if (name.endsWith(".png")) {
                        ImageInputStream stream = ImageIO.createImageInputStream(url.openStream());
                        ImageReadParam param = PNG_READER.getDefaultReadParam();
                        try {
                            PNG_READER.setInput(stream, true, true);
                            result = PNG_READER.read(0, param);
                        }
                        catch (IOException ioe1) {
                            ERR.log(Level.INFO, "Image "+name+" is not PNG", ioe1);
                        }
                        stream.close();
                    } 
                    /*
                    else if (name.endsWith(".gif")) {
                        ImageInputStream stream = ImageIO.createImageInputStream(url.openStream());
                        ImageReadParam param = GIF_READER.getDefaultReadParam();
                        try {
                            GIF_READER.setInput(stream, true, true);
                            result = GIF_READER.read(0, param);
                        }
                        catch (IOException ioe1) {
                            ERR.log(Level.INFO, "Image "+name+" is not GIF", ioe1);
                        }
                        stream.close();
                    }
                     */

                    if (result == null) {
                        result = ImageIO.read(url);
                    }
                }
            } catch (IOException ioe) {
                ERR.log(Level.WARNING, "Cannot load " + name + " image", ioe);
            }

            if (result != null) {
                if (warn && extraInitialSlashes.add(name)) {
                    ERR.warning(
                        "Initial slashes in Utilities.loadImage deprecated (cf. #20072): " +
                        name
                    ); // NOI18N
                }

//                Image img2 = toBufferedImage(result);

                ERR.log(Level.FINE, 
                        "loading icon {0} = {1}", new Object[] {n, result});
                name = new String(name).intern(); // NOPMD

                cache.put(name, new ActiveRef<String>(result, cache, name));

                return result;
            } else { // no icon found

                if (!localizedQuery) {
                    cache.put(name, NO_ICON);
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

        synchronized (compositeCache) {
            ActiveRef<CompositeImageKey> r = compositeCache.get(k);

            if (r != null) {
                cached = r.get();

                if (cached != null) {
                    return cached;
                }
            }

            cached = doMergeImages(im1, im2, x, y);
            compositeCache.put(k, new ActiveRef<CompositeImageKey>(cached, compositeCache, k));

            return cached;
        }
    }

    /** The method creates a BufferedImage which represents the same Image as the
     * parameter but consumes less memory.
     */
    static final Image toBufferedImage(Image img) {
        // load the image
        new javax.swing.ImageIcon(img, "");

        if (img.getHeight(null)*img.getWidth(null) > 24*24) {
            return img;
        }
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
        boolean bitmask = (image1 instanceof Transparency) && ((Transparency)image1).getTransparency() != Transparency.TRANSLUCENT
                && (image2 instanceof Transparency) && ((Transparency)image2).getTransparency() != Transparency.TRANSLUCENT;

        ColorModel model = colorModel(bitmask? Transparency.BITMASK: Transparency.TRANSLUCENT);
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
        if (Utilities.isMac()) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        }

        ColorModel model = colorModel(java.awt.Transparency.TRANSLUCENT);
        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage(
                model, model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null
            );

        return buffImage;
    }
    
    static private ColorModel colorModel(int transparency) {
        ColorModel model;
        try {
            model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .getColorModel(transparency);
        }
        catch(HeadlessException he) {
            model = ColorModel.getRGBdefault();
        }

        return model;
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
    private static final class ActiveRef<T> extends SoftReference<Image> implements Runnable {
        private Map<T,ActiveRef<T>> holder;
        private T key;

        public ActiveRef(Image o, Map<T,ActiveRef<T>> holder, T key) {
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
