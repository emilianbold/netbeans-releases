/*
 * HTMLImage.java
 *
 * Created on October 17, 2002, 8:01 PM
 */

package org.netbeans.performance.spi.html;
    
/** Wrapper for image tags. */    
public class HTMLImage extends HTMLTextItem {
    public HTMLImage (String filename) {
        super ("<IMG SRC=\"" + filename + "\" BORDER=0>");
    }
}
