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
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.developer.impl.actions.*;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.developer.modules.loaders.java.JavaDataLoader;

/** Loader for Forms. Recognizes file with extension .form and .java and with extension class if
* there is their source and form file.
*
* @author Ian Formanek
*/
public class FormDataLoader extends DataLoader {
  /* The standard extensions of the recognized files */
  public static final String FORM_EXTENSION = "form";
  public static final String JAVA_EXTENSION = "java";
  public static final String CLASS_EXTENSION = "class";

  /** Constructs a new FormDataLoader */
  public FormDataLoader () {
    super (FormDataObject.class);

    setActions(new SystemAction[] {
      SystemAction.get(OpenAction.class),
      null,
      SystemAction.get(CompileAction.class),
      null,
      SystemAction.get(ExecuteAction.class),
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
      SystemAction.get(PropertiesAction.class),
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

  /** This method is used when you need to find a form DataObject for file object.
  * The FormDataLoader recognizes this:<BR>
  * required files: *.java *.form      <BR>
  * optional files: *.class            <BR>
  * @param fo file object to recognize
  * @param recognized recognized files buffer.
  * @return suitable data object or <CODE>null</CODE> if the handler cannot
  *   recognize this object
  */
  public DataObject handleFindDataObject (FileObject fo, DataLoaderRecognized recognized)
  throws com.netbeans.ide.loaders.DataObjectExistsException {
    if (fo == null)
      return null;

    String ext = fo.getExt();

    FileObject jfo = null;
    FileObject ffo = null;
    FileObject cfo = null;

    if (ext.equals(FORM_EXTENSION)) {
      ffo = fo;
      jfo = findFile (fo, JAVA_EXTENSION);
      cfo = findFile (fo, CLASS_EXTENSION);
    }

    if (ext.equals(JAVA_EXTENSION)) {
      ffo = findFile (fo, FORM_EXTENSION);
      jfo = fo;
      cfo = findFile (fo, CLASS_EXTENSION);
    }

    if (ext.equals(CLASS_EXTENSION)) {
      ffo = findFile (fo, FORM_EXTENSION);
      jfo = findFile (fo, JAVA_EXTENSION);
      cfo = fo;
    }

    if ((jfo != null) && (ffo != null)) {
      recognized.markRecognized (ffo);
      recognized.markRecognized (jfo);
      if (cfo != null)
        recognized.markRecognized (cfo);
      FormDataObject obj = null;
      try {
        obj = new FormDataObject (ffo, jfo);
      } catch (DataObjectExistsException ex) {
        if (ex.getDataObject () instanceof FormDataObject)
          obj = (FormDataObject) ex.getDataObject();
        else
          return null; // recognized as different type of object
      }
      if (cfo != null) {
        MultiDataObject.FileEntry fe = new MultiDataObject.NumbEntry(cfo);
        obj.addSecondaryEntry(fe);
        obj.fileEntryAdded(fe);
      }
      obj.addSecondaryEntry(new MultiDataObject.MirroringEntry(ffo));
      return obj;
    }
    else {
      return null;
    }
  }
}

/*
 * Log
 *  3    Gandalf   1.2         1/25/99  Ian Formanek    First switch to Gandalf
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
