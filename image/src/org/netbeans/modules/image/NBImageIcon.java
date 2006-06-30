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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.image;


import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.ImageIcon;


/**
 * ImageIcon with serialization.
 *
 * @author Petr Hamernik, Michael Wever
 * @author  Marian Petras
 */
class NBImageIcon extends ImageIcon implements Serializable {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -1730253055388017036L;

    /** Appropriate image data object */
    ImageDataObject obj;

    /**
     * Loads an image from an <code>ImageDataObject</code>.
     * If an error occures during reading the image, an exception is thrown.
     * If the image format is not supported, <code>null</code> is returned.
     *
     * @param  obj  <code>ImageDataObject</code> to load the image from
     * @return  loaded image if loaded successfully,
     *          or <code>null</code> if no registered <code>ImageReader</code>
     *          claims to be able to read the image
     * @exception  java.io.IOException
     *             if an error occurs during reading the image
     * @see  javax.imageio.ImageIO#read(java.io.InputStream)
     */
    public static NBImageIcon load(ImageDataObject obj) throws IOException {
        Image image = obj.getImage();
        return (image != null) ? new NBImageIcon(obj, image) : null;
    }
    
    /** Construct a new icon.
     * @param obj the data object to represent the image in
     */
    private NBImageIcon(ImageDataObject obj, Image image) {
        //super(obj.getImageURL()); // PENDING for the time URL is incorrectly cached (in Toolkit)
        super(image);  //mw
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
            Image image;
            try {
                image = obj.getImage();
            } catch (IOException ex) {
                image = null;
            }
            return new NBImageIcon(
                    obj,
                    (image != null) ? image : new ImageIcon().getImage());
        }
    } // End of nested class ResolvableHelper.
}
