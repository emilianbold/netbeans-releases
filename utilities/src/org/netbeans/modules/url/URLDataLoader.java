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

package com.netbeans.developer.modules.loaders.url;

import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

/** Data loader which recognizes url files.
* @author Ian Formanek
*/
public class URLDataLoader extends UniFileLoader {

  static final long serialVersionUID =-7407252842873642582L;
  /** Creates new URLDataLoader without the extension. */
  public URLDataLoader() {
    super(URLDataObject.class);
  }

  /** Initialize */
  protected void initialize () {
    setDisplayName(NbBundle.getBundle(URLDataLoader.class).
                   getString("PROP_URLLoader_Name"));
    ExtensionList ext = new ExtensionList();
    ext.addExtension("url"); // NOI18N
    setExtensions(ext);

    setActions(new SystemAction[] {
      SystemAction.get(OpenAction.class),
      SystemAction.get(OpenInNewWindowAction.class),
      SystemAction.get(FileSystemAction.class),
      null,
      SystemAction.get(EditAction.class),
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
      SystemAction.get(PropertiesAction.class),
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
  protected MultiDataObject createMultiObject (FileObject primaryFile)
  throws DataObjectExistsException, java.io.IOException {
    return new URLDataObject(primaryFile, this);
  }

}

/*
 * Log
 *  11   Gandalf   1.10        1/5/00   Ian Formanek    NOI18N
 *  10   Gandalf   1.9         11/27/99 Patrik Knakal   
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/1/99  Jaroslav Tulach Loaders extends 
 *       SharedClassObject
 *  7    Gandalf   1.6         8/31/99  Ian Formanek    Correctly provides 
 *       FileSystemAction on its data objects
 *  6    Gandalf   1.5         7/11/99  Ian Formanek    employed EditAction
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ToolsAction
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  2    Gandalf   1.1         2/25/99  Ian Formanek    
 *  1    Gandalf   1.0         1/22/99  Ian Formanek    
 * $
 */
