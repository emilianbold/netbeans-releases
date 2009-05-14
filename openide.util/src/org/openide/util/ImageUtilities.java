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
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/** 
 * Useful static methods for manipulation with images/icons, results are cached.
 * 
 * @author Jaroslav Tulach, Tomas Holy
 * @since 7.15
 */
public final class ImageUtilities {
    /** separator for individual parts of tool tip text */
    static final String TOOLTIP_SEPAR = "<br>"; // NOI18N
    /** a value that indicates that the icon does not exists */
    private static final ActiveRef<String> NO_ICON = new ActiveRef<String>(null, null, null);

    private static final Map<String,ActiveRef<String>> cache = new HashMap<String,ActiveRef<String>>(128);
    private static final Map<String,ActiveRef<String>> localizedCache = new HashMap<String,ActiveRef<String>>(128);
    private static final Map<CompositeImageKey,ActiveRef<CompositeImageKey>> compositeCache = new HashMap<CompositeImageKey,ActiveRef<CompositeImageKey>>(128);
    private static final Map<ToolTipImageKey, ActiveRef<ToolTipImageKey>> imageToolTipCache = new HashMap<ToolTipImageKey, ActiveRef<ToolTipImageKey>>(128);

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
    
    private static final Logger ERR = Logger.getLogger(ImageUtilities.class.getName());
    
    private ImageUtilities() {
    }

    static {
        ImageIO.setUseCache(false);
        PNG_READER = ImageIO.getImageReadersByMIMEType("image/png").next();
//        GIF_READER = ImageIO.getImageReadersByMIMEType("image/gif").next();
    }

    /**
     * Loads an image from the specified resource ID. The image is loaded using the "system" classloader registered in
     * Lookup.
     * @param resourceID resource path of the icon (no initial slash)
     * @return icon's Image, or null, if the icon cannot be loaded.     
     */
    public static final Image loadImage(String resourceID) {
        return getIcon(resourceID, false);
    }
    
    /**
     * Loads an image based on resource path.
     * Exactly like {@link #loadImage(String)} but may do a localized search.
     * For example, requesting <samp>org/netbeans/modules/foo/resources/foo.gif</samp>
     * might actually find <samp>org/netbeans/modules/foo/resources/foo_ja.gif</samp>
     * or <samp>org/netbeans/modules/foo/resources/foo_mybranding.gif</samp>.
     * 
     * <p>Caching of loaded images can be used internally to improve performance.
     * 
     * @param resource resource path of the image (no initial slash)
     * @param localized true for localized search
     * @return icon's Image or null if the icon cannot be loaded
     */
    public static final Image loadImage(String resource, boolean localized) {
        return getIcon(resource, localized);
    }

    /**
     * Loads an icon based on resource path.
     * Similar to {@link #loadImage(String, boolean)}, returns ImageIcon instead of Image.
     * @param resource resource path of the icon (no initial slash)
     * @param localized localized resource should be used
     * @return ImageIcon or null, if the icon cannot be loaded.
     * @since 7.22
     */
    public static final ImageIcon loadImageIcon(String resource, boolean localized) {
        Image image = getIcon(resource, localized);
        return image == null ? null : (ImageIcon) image2Icon(image);
    }

    /** This method merges two images into the new one. The second image is drawn
     * over the first one with its top-left corner at x, y. Images need not be of the same size.
     * New image will have a size of max(second image size + top-left corner, first image size).
     * Method is used mostly when second image contains transparent pixels (e.g. for badging).
     * Method that attempts to find the merged image in the cache first, then
     * creates the image if it was not found.
     * @param image1 underlying image
     * @param image2 second image
     * @param x x position of top-left corner
     * @param y y position of top-left corner
     * @return new merged image
     */    
    public static final Image mergeImages(Image image1, Image image2, int x, int y) {
        if (image1 == null || image2 == null) {
            throw new NullPointerException();
        }
        
        CompositeImageKey k = new CompositeImageKey(image1, image2, x, y);
        Image cached;

        synchronized (compositeCache) {
            ActiveRef<CompositeImageKey> r = compositeCache.get(k);
            if (r != null) {
                cached = r.get();
                if (cached != null) {
                    return cached;
                }
            }
            cached = doMergeImages(image1, image2, x, y);
            compositeCache.put(k, new ActiveRef<CompositeImageKey>(cached, compositeCache, k));
            return cached;
        }
    }    
    
    /**
     * Converts given image to an icon.
     * @param image to be converted
     * @return icon corresponding icon
     */    
    public static final Icon image2Icon(Image image) {
        if (image instanceof ToolTipImage) {
            return ((ToolTipImage) image).getIcon();
        } else {
            return new ImageIcon(image);
        }
    }
    
    /**
     * Converts given icon to a {@link java.awt.Image}.
     *
     * @param icon {@link javax.swing.Icon} to be converted.
     */
    public static final Image icon2Image(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon) icon).getImage();
        } else {
            ToolTipImage image = new ToolTipImage("", icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            icon.paintIcon(new JLabel(), g, 0, 0);
            g.dispose();
            return image;
        }
    }
    
    /**
     * Assign tool tip text to given image (creates new or returns cached, original remains unmodified)
     * Text can contain HTML tags e.g. "&#60;b&#62;my&#60;/b&#62; text"
     * @param image image to which tool tip should be set
     * @param text tool tip text
     * @return Image with attached tool tip 
     */    
    public static final Image assignToolTipToImage(Image image, String text) {
        Parameters.notNull("image", image);
        Parameters.notNull("text", text);
        ToolTipImageKey key = new ToolTipImageKey(image, text);
        Image cached;
        synchronized (imageToolTipCache) {
            ActiveRef<ToolTipImageKey> r = imageToolTipCache.get(key);
            if (r != null) {
                cached = r.get();
                if (cached != null) {
                    return cached;
                }
            }
            cached = ToolTipImage.createNew(text, image);
            imageToolTipCache.put(key, new ActiveRef<ToolTipImageKey>(cached, imageToolTipCache, key));
            return cached;
        }
    }

    /**
     * Get tool tip text for given image
     * @param image image which is asked for tool tip text
     * @return String containing attached tool tip text
     */
    public static final String getImageToolTip(Image image) {
        if (image instanceof ToolTipImage) {
            return ((ToolTipImage) image).toolTipText;
        } else {
            return "";
        }
    }

    /**
     * Add text to tool tip for given image (creates new or returns cached, original remains unmodified)
     * Text can contain HTML tags e.g. "&#60;b&#62;my&#60;/b&#62; text"
     * @param text text to add to tool tip
     * @return Image with attached tool tip
     */
    public static final Image addToolTipToImage(Image image, String text) {
        if (image instanceof ToolTipImage) {
            ToolTipImage tti = (ToolTipImage) image;
            StringBuilder str = new StringBuilder(tti.toolTipText);
            if (str.length() > 0 && text.length() > 0) {
                str.append(TOOLTIP_SEPAR);
            }
            str.append(text);
            return assignToolTipToImage(image, str.toString());
        } else {
            return assignToolTipToImage(image, text);
        }
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
                        : ImageUtilities.class.getClassLoader().getResource(resource);
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
                                                : ImageUtilities.class.getClassLoader().getResource(n);

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

                ERR.log(Level.FINE, "loading icon {0} = {1}", new Object[] {n, result});
                name = new String(name).intern(); // NOPMD
                result = ToolTipImage.createNew("", result);
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

        StringBuilder str = new StringBuilder(image1 instanceof ToolTipImage ? ((ToolTipImage)image1).toolTipText : "");
        if (image2 instanceof ToolTipImage) {
            String toolTip = ((ToolTipImage)image2).toolTipText;
            if (str.length() > 0 && toolTip.length() > 0) {
                str.append(TOOLTIP_SEPAR);
            }
            str.append(toolTip);
        }
        
        ColorModel model = colorModel(bitmask? Transparency.BITMASK: Transparency.TRANSLUCENT);
        ToolTipImage buffImage = new ToolTipImage(str.toString(), 
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

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof CompositeImageKey)) {
                return false;
            }

            CompositeImageKey k = (CompositeImageKey) other;

            return (x == k.x) && (y == k.y) && (baseImage == k.baseImage) && (overlayImage == k.overlayImage);
        }

        @Override
        public int hashCode() {
            int hash = ((x << 3) ^ y) << 4;
            hash = hash ^ baseImage.hashCode() ^ overlayImage.hashCode();

            return hash;
        }

        @Override
        public String toString() {
            return "Composite key for " + baseImage + " + " + overlayImage + " at [" + x + ", " + y + "]"; // NOI18N
        }
    }
    
    /**
     * Key used for ToolTippedImage
     */
    private static class ToolTipImageKey {
        Image image;
        String str;

        ToolTipImageKey(Image image, String str) {
            this.image = image;
            this.str = str;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ToolTipImageKey)) {
                return false;
            }
            ToolTipImageKey k = (ToolTipImageKey) other;
            return (str.equals(k.str)) && (image == k.image);
        }

        @Override
        public int hashCode() {
            int hash = image.hashCode() ^ str.hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return "ImageStringKey for " + image + " + " + str; // NOI18N
        }
    }

    /** Cleaning reference. */
    private static final class ActiveRef<T> extends SoftReference<Image> implements Runnable {
        private final Map<T,ActiveRef<T>> holder;
        private final T key;

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

    /**
     * Image with tool tip text (for icons with badges)
     */
    private static class ToolTipImage extends BufferedImage implements Icon {
        final String toolTipText;
        ImageIcon imageIcon;

        public static ToolTipImage createNew(String toolTipText, Image image) {
            ImageUtilities.ensureLoaded(image);
            boolean bitmask = (image instanceof Transparency) && ((Transparency) image).getTransparency() != Transparency.TRANSLUCENT;
            ColorModel model = colorModel(bitmask ? Transparency.BITMASK : Transparency.TRANSLUCENT);
            int w = image.getWidth(null);
            int h = image.getHeight(null);
            ToolTipImage newImage = new ToolTipImage(toolTipText, model, model.createCompatibleWritableRaster(w, h), model.isAlphaPremultiplied(), null);

            java.awt.Graphics g = newImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            return newImage;
        }
        
        public ToolTipImage(String toolTipText, ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
            super(cm, raster, isRasterPremultiplied, properties);
            this.toolTipText = toolTipText;
        }

        public ToolTipImage(String toolTipText, int width, int height, int imageType, IndexColorModel cm) {
            super(width, height, imageType, cm);
            this.toolTipText = toolTipText;
        }

        public ToolTipImage(String toolTipText, int width, int height, int imageType) {
            super(width, height, imageType);
            this.toolTipText = toolTipText;
        }
        
        public ToolTipImage(String toolTipText, BufferedImage image) {
            super(image.getWidth(), image.getHeight(), image.getType());
            this.toolTipText = toolTipText;
        }

        synchronized ImageIcon getIcon() {
            if (imageIcon == null) {
                imageIcon = new ImageIcon(this);
            }
            return imageIcon;
        }

        public int getIconHeight() {
            return super.getHeight();
        }

        public int getIconWidth() {
            return super.getWidth();
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawImage(this, x, y, null);
        }
    }
}
