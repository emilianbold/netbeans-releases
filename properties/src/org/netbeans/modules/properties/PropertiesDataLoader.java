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

import java.util.StringTokenizer;
import java.io.IOException;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.FileEntry;
import org.openide.loaders.ExtensionList;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

/** Data loader which recognizes properties files.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Ian Formanek
*/
public final class PropertiesDataLoader extends MultiFileLoader {
                                        
  static final String PROPERTIES_EXTENSION = "properties";
                                        
  /** Table of recognized extensions for this data loader */
  private ExtensionList extensions;

  /** Character used to separate parts of bundle properties file name */                                                                                     
  public static final char PRB_SEPARATOR_CHAR = '_';

  /** Creates new PropertiesDataLoader */
  public PropertiesDataLoader() {
    super(PropertiesDataObject.class);
  }

  /** Does initialization. Initializes display name,
  * extension list and the actions. */
  protected void initialize () {
    setDisplayName(NbBundle.getBundle(PropertiesDataLoader.class).
                   getString("PROP_PropertiesLoader_Name"));
    ExtensionList ext = new ExtensionList();
    ext.addExtension("properties");
    ext.addExtension("impl"); // for CORBA module
    setExtensions(ext);

    setActions(new SystemAction[] {
      SystemAction.get(OpenAction.class),
      SystemAction.get(EditAction.class),
      SystemAction.get(FileSystemAction.class),
      null,
      SystemAction.get(CutAction.class),
      SystemAction.get(CopyAction.class),
      SystemAction.get(PasteAction.class),
      null,
      SystemAction.get(DeleteAction.class),
      SystemAction.get(RenameAction.class),
      null,
      SystemAction.get(NewAction.class),
      SystemAction.get(SaveAsTemplateAction.class),
      null,
      SystemAction.get(ToolsAction.class),
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

  /** For a given file finds a primary file.
  * @param fo the file to find primary file for
  *
  * @return the primary file for the file or null if the file is not
  *   recognized by this loader
  */
  protected FileObject findPrimaryFile (FileObject fo) {
    if (fo.getExt().equalsIgnoreCase(PROPERTIES_EXTENSION)) {
      // returns a file whose name is the shortest valid prefix corresponding to an existing file
      String fName = fo.getName();
      int index = fName.indexOf(PRB_SEPARATOR_CHAR);
      while (index != -1) {
        FileObject candidate = fo.getParent().getFileObject(fName.substring(0, index), fo.getExt());
        if (candidate != null) {
          return candidate;     
        }  
        index = fName.indexOf(PRB_SEPARATOR_CHAR, index + 1);
      }
      return fo;                              
    }

    else 
      return getExtensions().isRegistered(fo) ? fo : null;
  }

  /** Creates the right primary entry for given primary file.
  *
  * @param primaryFile primary file recognized by this loader
  * @return primary entry for that file
  */
  protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
    return new PropertiesFileEntry(obj, primaryFile);
  }

  /** Creates right secondary entry for given file. The file is said to
  * belong to an object created by this loader.
  *
  * @param secondaryFile secondary file for which we want to create entry
  * @return the entry
  */
  protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
    PropertiesFileEntry pfe = new PropertiesFileEntry(obj, secondaryFile);
    //((PropertiesDataObject)obj).registerEntryListener (pfe);
    return pfe;
  }

  /** Set the extension list for this data loader.
  * @param ext new list of extensions
  */
  public void setExtensions(ExtensionList ext) {
    extensions = ext;
  }

  /** Get the extension list for this data loader.
  * @return list of extensions
  */
  public ExtensionList getExtensions() {
    if (extensions == null)
      extensions = new ExtensionList();
    return extensions;
  }
}

/*
* <<Log>>
*  11   Gandalf   1.10        10/1/99  Jaroslav Tulach Loaders extends 
*       SharedClassObject
*  10   Gandalf   1.9         8/31/99  Petr Jiricka    Allowed extension 
*       settings, "impl" added to extensions
*  9    Gandalf   1.8         7/16/99  Petr Jiricka    
*  8    Gandalf   1.7         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  7    Gandalf   1.6         6/6/99   Petr Jiricka    
*  6    Gandalf   1.5         5/12/99  Petr Jiricka    
*  5    Gandalf   1.4         5/11/99  Ian Formanek    Undone last change to 
*       compile
*  4    Gandalf   1.3         5/11/99  Petr Jiricka    
*  3    Gandalf   1.2         3/26/99  Ian Formanek    Fixed use of obsoleted 
*       NbBundle.getBundle (this)
*  2    Gandalf   1.1         3/9/99   Ian Formanek    Moved images to this 
*       package
*  1    Gandalf   1.0         1/22/99  Ian Formanek    
* $
*/
