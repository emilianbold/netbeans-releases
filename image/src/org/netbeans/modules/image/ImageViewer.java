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

import org.openide.windows.CloneableTopComponent;
import org.openide.util.HelpCtx;

/** Top component providing a viewer for images.
*
* @author Petr Hamernik, Ian Formanek
*/
public class ImageViewer extends CloneableTopComponent {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = 6017254068843460960L; // [PENDING SUID]

  private ImageDataObject storedObject;

  /** Create a new image viewer.
  * @param obj the data object holding the image
  */
  public ImageViewer(ImageDataObject obj) {
    super(obj);
    storedObject = obj;
    javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(new javax.swing.JLabel(new NBImageIcon(obj)));
    setLayout(new java.awt.BorderLayout());
    add(scroll, "Center");
  }

  public HelpCtx getHelp() {
    return new HelpCtx(ImageViewer.class);
  }

  // Cloning the viewer uses the same underlying data object.
  protected CloneableTopComponent createClonedObject () {
    return new ImageViewer(storedObject);
  }

}
