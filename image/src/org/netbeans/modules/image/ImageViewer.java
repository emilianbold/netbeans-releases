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

import java.awt.Dimension;

import org.openide.text.EditorSupport;
import org.openide.util.HelpCtx;
import org.openide.windows.*;

/** Top component providing a viewer for images.
*
* @author Petr Hamernik, Ian Formanek
*/
public class ImageViewer extends CloneableTopComponent {
  private static final int MINIMUM_WIDTH = 200;
  private static final int MINIMUM_HEIGHT = 150;

  private static final int DEFAULT_BORDER_WIDTH = 40;
  private static final int DEFAULT_BORDER_HEIGHT = 40;

  private ImageDataObject storedObject;
  private javax.swing.JLabel label;

  /** Create a new image viewer.
  * @param obj the data object holding the image
  */
  public ImageViewer(ImageDataObject obj) {
    super(obj);
    storedObject = obj;
    javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(label = new javax.swing.JLabel(new NBImageIcon(obj)));
    setLayout(new java.awt.BorderLayout());
    add(scroll, "Center");
  }

  /** Show the component on given workspace. If given workspace is
  * not active, component will be shown only after given workspace
  * will become visible.
  * Note that this method only makes it visible, but does not
  * give it focus.
  * @param workspace Workspace on which component should be opened.
  * @see #requestFocus
  */
  public void open (Workspace w) {
    Mode viewerMode = w.findMode(this);
    if (viewerMode == null) {
      Mode editorMode = w.findMode(EditorSupport.EDITOR_MODE);
      if (editorMode != null) editorMode.dockInto(this);
    }
    super.open (w);
  }
  
  public Dimension getPreferredSize () {
    Dimension pref = label.getPreferredSize ();
    return new Dimension (Math.max (DEFAULT_BORDER_WIDTH + pref.width, MINIMUM_WIDTH), Math.max (DEFAULT_BORDER_HEIGHT + pref.height, MINIMUM_HEIGHT));
  }
  
  public HelpCtx getHelpCtx () {
    return new HelpCtx(ImageViewer.class);
  }

  // Cloning the viewer uses the same underlying data object.
  protected CloneableTopComponent createClonedObject () {
    return new ImageViewer(storedObject);
  }

}
