/*
 * CDataBuffer.java
 *
 * Created on February 17, 2004, 2:21 AM
 */

package org.netbeans.imagecache;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;

/** An imaging DataBufferInt implementation backed by an IntBuffer over a
 * memory mapped file.
 *
 * @author  Tim Boudreau
 */
class CDataBuffer extends DataBuffer { 
    IntBuffer buf;

    /** Creates a new instance of CDataBuffer */
    public CDataBuffer(IntBuffer buf, int width, int height) {
        super (TYPE_INT, width * height);
        this.buf = buf;
    }
    
    public int getOffset() {
        return 0;
    }
    
    public int getElem(int bank, int i) {
        return this.buf.get(i);
    }
    
    public void setElem(int bank, int i, int val) {
//        throw new UnsupportedOperationException();
        //do nothing
    }
    
}
