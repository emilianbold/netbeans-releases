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

import com.netbeans.ide.windows.CloneableTopComponent;
import com.netbeans.ide.util.HelpCtx;

/** Object that provides viewer for images.
*
* @author Petr Hamernik, Ian Formanek
*/
public class ImageViewer extends CloneableTopComponent {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = 6017254068843460960L; // [PENDING SUID]

  /** Creates new image viewer.
  * @exception IOException if the file cannot be loaded.
  */
  public ImageViewer(ImageDataObject obj) throws java.io.IOException {
    super(obj);
    javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(new javax.swing.JLabel(new NBImageIcon(obj)));
    setLayout(new java.awt.BorderLayout());
    add(scroll, "Center");
  }

  /** @return help for image viewer */
  public HelpCtx getHelp() {
    return new HelpCtx("com.netbeans.developer.help.BaseLoadersImageImageViewer");
  }

  /** Is called from the clone method to create new component from this one.
  * This implementation only clones the object by calling super.clone method.
  * @return the copy of this object
  */
/*  protected CloneableTopComponent createClonedObject () {
    try {
      return new ImageViewer((ImageDataObject) getDataObject());
    }
    catch (IOException e) {
      throw new InternalError ();
    }
  } */
  
  /** Returns true */
  public boolean closeLast() {
    return true;
  }

}

/*
 * Log
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
