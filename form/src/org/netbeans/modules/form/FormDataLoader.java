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

package com.netbeans.developer.modules.loaders.form;

import com.netbeans.ide.*;
import com.netbeans.ide.actions.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.developer.modules.loaders.java.JavaDataLoader;

/** Loader for Forms. Recognizes file with extension .form and .java and with extension class if
* there is their source and form file.
*
* @author Ian Formanek
*/
public class FormDataLoader extends MultiFileLoader {
  /* The standard extensions of the recognized files */
  public static final String FORM_EXTENSION = "form";
  public static final String JAVA_EXTENSION = "java";
  public static final String CLASS_EXTENSION = "class";

  /** Constructs a new FormDataLoader */
  public FormDataLoader () {
    super (FormDataObject.class);
    setDisplayName(NbBundle.getBundle(this).getString("PROP_FormLoader_Name"));

    setActions(new SystemAction[] {
      SystemAction.get (OpenAction.class),
      SystemAction.get (CustomizeBeanAction.class),
      null,
      SystemAction.get (CompileAction.class),
      null,
      SystemAction.get (ExecuteAction.class),
      null,
      SystemAction.get (CutAction.class),
      SystemAction.get (CopyAction.class),
      SystemAction.get (PasteAction.class),
      null,
      SystemAction.get (DeleteAction.class),
      SystemAction.get (RenameAction.class),
      null,
      SystemAction.get (SaveAsTemplateAction.class),
      null,
      SystemAction.get (PropertiesAction.class),
    });

  }

  /** finds file with the same name and specified extension in the same folder as param javaFile */
  static public FileObject findFile(FileObject javaFile, String ext) {
    if (javaFile == null) return null;
    String name = javaFile.getName ();
    int indx = name.indexOf ('$');
    if (indx > 0) {
      name = name.substring (0, indx);
    }
    return javaFile.getParent().getFileObject (name, ext);

  }

  /** For a given file finds a primary file.
  * @param fo the file to find primary file for
  *
  * @return the primary file for the file or null if the file is not
  *   recognized by this loader
  */
  protected FileObject findPrimaryFile (FileObject fo) {
    String ext = fo.getExt();
    if (ext.equals(JAVA_EXTENSION))
      return fo;

    if (ext.equals(CLASS_EXTENSION) || ext.equals (FORM_EXTENSION))
      return findFile(fo, JAVA_EXTENSION);

    return null;
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
    return new FormDataObject(findFile (primaryFile, FORM_EXTENSION), primaryFile, this);
  }

  /** Creates the right primary entry for given primary file.
  *
  * @param primaryFile primary file recognized by this loader
  * @return primary entry for that file
  */
  protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
    return new FileEntry(obj, primaryFile);
  }

  /** Creates right secondary entry for given file. The file is said to
  * belong to an object created by this loader.
  *
  * @param secondaryFile secondary file for which we want to create entry
  * @return the entry
  */
  protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
    String ext = secondaryFile.getExt();
    if (ext.equals(FORM_EXTENSION))
      return new FileEntry (obj, secondaryFile);
    else
      return new FileEntry.Numb (obj, secondaryFile);
  }
}

/*
 * Log
 *  8    Gandalf   1.7         3/16/99  Ian Formanek    
 *  7    Gandalf   1.6         3/16/99  Ian Formanek    
 *  6    Gandalf   1.5         3/14/99  Jaroslav Tulach Change of 
 *       MultiDataObject.Entry.
 *  5    Gandalf   1.4         3/11/99  Ian Formanek    fixed order of form and 
 *       java file
 *  4    Gandalf   1.3         3/10/99  Ian Formanek    Gandalf updated
 *  3    Gandalf   1.2         1/25/99  Ian Formanek    First switch to Gandalf
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
