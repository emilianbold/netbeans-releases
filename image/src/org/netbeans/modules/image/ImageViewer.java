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

import java.awt.BorderLayout;
import java.io.*;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.netbeans.ide.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.windows.*;
import com.netbeans.developer.impl.*;
import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.util.NotImplementedException;
import com.netbeans.ide.util.io.*;

/** Object that provides viewer for images.
*
* @author Petr Hamernik
* @version 0.19, Jun 2, 1998
*/
public class ImageViewer extends CloneableTopComponent {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 6017254068843460960L;
  /** Creates new image viewer.
  * @exception IOException if the file cannot be loaded.
  */
  public ImageViewer(ImageDataObject obj) throws IOException {
    super(obj);
    JScrollPane scroll = new JScrollPane(new JLabel(new NBImageIcon(obj)));
    setLayout(new BorderLayout());
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
  protected CloneableTopComponent createClonedObject () {
    try {
      return new ImageViewer((ImageDataObject) getDataObject());
    }
    catch (IOException e) {
      throw new InternalError ();
    }
  }

  /** Returns true */
  public boolean closeLast() {
    ImageDataObject obj = (ImageDataObject) getDataObject();
    return true;
  }

  protected void removeFromFrame() {
    super.removeFromFrame();
  }
}

/*
 * Log
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
