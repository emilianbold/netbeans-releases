/*
 * CRaster.java
 *
 * Created on February 17, 2004, 3:20 AM
 */

package org.netbeans.imagecache;

import java.awt.Point;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;
import sun.awt.image.SunWritableRaster;

/**
 *
 * @author  tim
 */
public class CRaster extends SunWritableRaster {
    
    /** Creates a new instance of CRaster */
    public CRaster(IntBuffer buf, int width, int height) {
        super (new CSampleModel(width, height), new CDataBuffer(buf, width, height), new Point(0,0));
    }
    
    public int[] getSamples(int x, int y, int w, int h, int b, int[] iArray) {
        System.err.println("GET SAMPLES: " + x + "," + y);
        return super.getSamples(x,y,w,h,b,iArray);
    }
    
}
