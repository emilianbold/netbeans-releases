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

package com.netbeans.developer.modules.loaders.image;

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
  public NBImageIcon(ImageDataObject obj) {
    super(obj.getImageURL());
    this.obj = obj;
  }

  // Get an object to be written to the stream instead of this object.
  public Object writeReplace() {
    return new ResolvableHelper(obj);
  }

  // Helper class for serialization.
  static class ResolvableHelper implements Serializable {
    // generated Serialized Version UID
    static final long serialVersionUID = -1120520132882774882L;
    // serializable data object
    ImageDataObject obj;

    ResolvableHelper(ImageDataObject obj) {
      this.obj = obj;
    }

    // Restore with the same data object.
    public Object readResolve() {
      return new NBImageIcon(obj);
    }
  }
}
