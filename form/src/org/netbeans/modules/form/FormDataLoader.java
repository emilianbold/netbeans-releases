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
import com.netbeans.ide.loaders.MultiFileDataObject;
import com.netbeans.developer.impl.actions.*;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.developer.modules.loaders.java.JavaDataLoader;

/** Loader for Forms. Recognizes file with extension .form and .java and with extension class if
* there is their source and form file.
*
* @author Ian Formanek, Petr Hamernik
* @version 0.15, June 05, 1998
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
      new OpenAction(),
      null,
      new CompileAction(),
      null,
      new ExecuteAction(),
      null,
      new CutAction(),
      new CopyAction(),
      new PasteAction(),
      null,
      new DeleteAction(),
      new RenameAction(),
      null,
      new SaveAsTemplateAction(),
      null,
      new PropertiesAction()
    });

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
      jfo = JavaDataLoader.findFile (fo, JAVA_EXTENSION);
      cfo = JavaDataLoader.findFile (fo, CLASS_EXTENSION);
    }

    if (ext.equals(JAVA_EXTENSION)) {
      ffo = JavaDataLoader.findFile (fo, FORM_EXTENSION);
      jfo = fo;
      cfo = JavaDataLoader.findFile (fo, CLASS_EXTENSION);
    }

    if (ext.equals(CLASS_EXTENSION)) {
      ffo = JavaDataLoader.findFile (fo, FORM_EXTENSION);
      jfo = JavaDataLoader.findFile (fo, JAVA_EXTENSION);
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
        MultiFileDataObject.FileEntry fe = new MultiFileDataObject.NumbEntry(cfo);
        obj.addSecondaryEntry(fe);
        obj.fileEntryAdded(fe);
      }
      obj.addSecondaryEntry(new MultiFileDataObject.MirroringEntry(ffo));
      return obj;
    }
    else {
      return null;
    }
  }
}

/*
 * Log
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.14        --/--/98 Petr Hamernik   recognizing files bugfix.
 *  0    Tuborg    0.15        --/--/98 Ales Novak      recognizing files bugfix
 */
