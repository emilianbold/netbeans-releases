/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.image;


import java.awt.Image;
import java.io.Serializable;
import javax.swing.ImageIcon;


/** 
 * ImageIcon with serialization.
 *
 * @author Petr Hamernik
 */
class NBImageIcon extends ImageIcon implements Serializable {
    
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1730253055388017036L;
    
    /** Appropriate image data object */
    ImageDataObject obj;

    
    /** Construct a new icon.
     * @param obj the data object to represent the image in
     */
    public NBImageIcon(ImageDataObject obj) {
        //super(obj.getImageURL()); // PENDING for the time URL is incorrectly cached (in Toolkit)
        super(obj.getImageData());
        this.obj = obj;
    }
    
    
    /** Get an object to be written to the stream instead of this object. */
    public Object writeReplace() {
        return new ResolvableHelper(obj);
    }

    
    /** Helper class for serialization. */
    static class ResolvableHelper implements Serializable {
        
        /** generated Serialized Version UID. */
        static final long serialVersionUID = -1120520132882774882L;
        
        /** serializable data object. */
        ImageDataObject obj;
        
        /** Constructs ResolvableHelper object for given ImageDataObject. */
        ResolvableHelper(ImageDataObject obj) {
            this.obj = obj;
        }

        /** Restore with the same data object. */
        public Object readResolve() {
            return new NBImageIcon(obj);
        }
    } // End of nested class ResolvableHelper.
}
