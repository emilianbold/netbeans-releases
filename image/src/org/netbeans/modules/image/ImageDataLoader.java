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

import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

/** Data loader which recognizes image files.
* @author Petr Hamernik, Jaroslav Tulach
*/
public class ImageDataLoader extends UniFileLoader {

  static final long serialVersionUID =-8188309025795898449L;
  /** Create a new loader. */
  public ImageDataLoader() {
    // Set the representation class.
    super(ImageDataObject.class);
    // Get a localized display name.
    setDisplayName(NbBundle.getBundle(ImageDataLoader.class).
                   getString("PROP_ImageLoader_Name"));
    // List of recognized extensions.
    ExtensionList ext = new ExtensionList();
    ext.addExtension("jpg"); // NOI18N
    ext.addExtension("jpeg"); // NOI18N
    ext.addExtension("jpe"); // NOI18N
    ext.addExtension("gif"); // NOI18N
    setExtensions(ext);

    // Common actions for all images.
    setActions(new SystemAction[] {
      SystemAction.get(OpenAction.class),
      SystemAction.get(FileSystemAction.class),
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
      SystemAction.get(ToolsAction.class),
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

/*
 * Log
 *  12   Gandalf   1.11        1/5/00   Ian Formanek    NOI18N
 *  11   Gandalf   1.10        11/27/99 Patrik Knakal   
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         8/8/99   Ian Formanek    Added filesystem action 
 *       to popup menu
 *  8    Gandalf   1.7         6/9/99   Ian Formanek    ToolsAction
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         4/13/99  Jesse Glick     Clean-ups of comments 
 *       and such for public perusal.
 *  5    Gandalf   1.4         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    
 *  3    Gandalf   1.2         1/6/99   Jan Jancura     
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Updated to new 
 *       DataSystem
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
