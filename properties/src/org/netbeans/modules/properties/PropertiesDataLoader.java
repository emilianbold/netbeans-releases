/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.properties;

import com.netbeans.ide.actions.*;
import com.netbeans.ide.filesystems.FileObject;
import com.netbeans.ide.loaders.UniFileLoader;
import com.netbeans.ide.loaders.ExtensionList;
import com.netbeans.ide.loaders.DataObject;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.util.NbBundle;

/** Data loader which recognizes properties files.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Ian Formanek
*/
public final class PropertiesDataLoader extends UniFileLoader {

  /** Creates new PropertiesDataLoader */
  public PropertiesDataLoader() {
    super(PropertiesDataObject.class);
    initialize();
  }

  /** Does initialization. Initializes display name,
  * extension list and the actions. */
  private void initialize () {
    setDisplayName(NbBundle.getBundle(PropertiesDataLoader.class).
                   getString("PROP_PropertiesLoader_Name"));
    ExtensionList ext = new ExtensionList();
    ext.addExtension("properties");
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

  /** Creates new PropertiesDataObject for this FileObject.
  * @param fo FileObject
  * @return new PropertiesDataObject
  */
  protected MultiDataObject createMultiObject(final FileObject fo)
                            throws java.io.IOException {
    return new PropertiesDataObject(fo, this);
  }

}

/*
* <<Log>>
*  3    Gandalf   1.2         3/26/99  Ian Formanek    Fixed use of obsoleted 
*       NbBundle.getBundle (this)
*  2    Gandalf   1.1         3/9/99   Ian Formanek    Moved images to this 
*       package
*  1    Gandalf   1.0         1/22/99  Ian Formanek    
* $
*/
