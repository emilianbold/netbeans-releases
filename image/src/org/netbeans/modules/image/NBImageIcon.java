/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.*;

import org.openide.util.io.*;

/** ImageIcon with serialization.
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
    public NBImageIcon(ImageDataObject obj) throws IOException {
        //super(obj.getImageURL()); // PENDING for the time URL is incorrectly cached (in Toolkit)
        super(obj.getImageData());
        this.obj = obj;
    }


    /** Get an object to be written to the stream instead of this object.
    */
    public Object writeReplace() {
        return new ResolvableHelper(obj);
    }

    /** Helper class for serialization.
    */
    static class ResolvableHelper implements Serializable {
        /** generated Serialized Version UID
        */
        static final long serialVersionUID = -1120520132882774882L;
        /** serializable data object
        */
        ImageDataObject obj;

        /** Constructs ResolvableHelper object for given ImageDataObject.
        */
        ResolvableHelper(ImageDataObject obj) {
            this.obj = obj;
        }

        /** Restore with the same data object.
        */
        public Object readResolve() {
            try {
                return new NBImageIcon(obj);
            } catch (IOException ioe) {
                return new ImageIcon(new byte[0]); // empty icon
            }
        }
    }
}

/*
 * Log
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         4/13/99  Jesse Glick     Clean-ups of comments 
 *       and such for public perusal.
 *  3    Gandalf   1.2         1/20/99  Petr Hamernik   
 *  2    Gandalf   1.1         1/7/99   Jaroslav Tulach Uses OpenSupport
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
