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
public class ImageDataLoader extends UniFileDataLoader {

  /** Creates new ImageDataLoader without the extension. */
  public ImageDataLoader() {
    super(ImageDataObject.class);
    setDisplayName(NbBundle.getBundle(this).
                   getString("PROP_ImageLoader_Name"));
    ExtensionList ext = new ExtensionList();
    ext.addExtension("jpg");
    ext.addExtension("jpeg");
    ext.addExtension("jpe");
    ext.addExtension("gif");
    setExtensions(ext);

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

  /** Creates the right data object for given primary file.
  * It is guaranteed that the provided file is realy primary file
  * returned from the method findPrimaryFile.
  *
  * @param primaryFile the primary file
  * @return the data object for this file
  * @exception DataObjectExistsException if the primary file already has data object
  */
  protected abstract MultiDataObject createMultiObject (FileObject primaryFile)
  throws DataObjectExistsException, java.io.IOException;
    return new ImageDataObject(fo);
  }

}

/*
 * Log
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Updated to new 
 *       DataSystem
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach Multi window is here (again)
 *  0    Tuborg    0.12        --/--/98 Petr Hamernik   small changes
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    locale change
 *  0    Tuborg    0.15        --/--/98 Petr Hamernik   Multi window is away (again)
 */
