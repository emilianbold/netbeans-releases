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

import com.netbeans.ide.filesystems.FileObject;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.actions.*;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.util.NbBundle;

/** Data loader which recognizes image files.
* @author Petr Hamernik, Jaroslav Tulach
*/
public class ImageDataLoader extends UniFileLoader {

  /** Create a new loader. */
  public ImageDataLoader() {
    // Set the representation class.
    super(ImageDataObject.class);
    // Get a localized display name.
    setDisplayName(NbBundle.getBundle(ImageDataLoader.class).
                   getString("PROP_ImageLoader_Name"));
    // List of recognized extensions.
    ExtensionList ext = new ExtensionList();
    ext.addExtension("jpg");
    ext.addExtension("jpeg");
    ext.addExtension("jpe");
    ext.addExtension("gif");
    setExtensions(ext);

    // Common actions for all images.
    setActions(new SystemAction[] {
      SystemAction.get(OpenAction.class),
      null,
      SystemAction.get(CutAction.class),
      SystemAction.get(CopyAction.class),
      SystemAction.get(PasteAction.class),
      null,
      SystemAction.get(DeleteAction.class),
      SystemAction.get(RenameAction.class),
      null,
      SystemAction.get(SaveAsTemplateAction.class),
      null,
      SystemAction.get(PropertiesAction.class)
    });

  }

  /** Create the image data object.
  *
  * @param primaryFile the primary file (e.g. <code>*.gif</code>)
  * @return the data object for this file
  * @exception DataObjectExistsException if the primary file already has a data object
  * @exception java.io.IOException should not be thrown
  */
  protected MultiDataObject createMultiObject (FileObject primaryFile)
  throws DataObjectExistsException, java.io.IOException {
    return new ImageDataObject(primaryFile, this);
  }

}
