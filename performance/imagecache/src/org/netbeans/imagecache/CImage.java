/*
 * CImage.java
 *
 * Created on February 17, 2004, 3:47 AM
 */

package org.netbeans.imagecache;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageProducer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.Vector;

/**  XXX this class is only for diagnostics - delete it when stabilized.
 *
 * @author  tim
 */
public class CImage extends BufferedImage {
    private Raster raster;
    /** Creates a new instance of CImage */
    public CImage (ColorModel cm,
                          WritableRaster raster,
                          boolean isRasterPremultiplied,
                          Hashtable properties) {
       super (cm, raster, isRasterPremultiplied, properties);
       this.raster = raster;
    }
    
    public Raster getData() {
        return raster;
    }
    
    public Vector getSources() {
        System.err.println("GetSources...");
        Thread.dumpStack();
        return null;
    }    
    
    public ImageProducer getSource() {
        System.err.println("GET SOURCE");
        Thread.dumpStack();
        return null;
    }
}
