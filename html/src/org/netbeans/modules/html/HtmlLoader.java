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

package com.netbeans.developer.modules.loaders.html;

import java.util.*;
import java.io.IOException;

import com.netbeans.ide.actions.*;
import com.netbeans.ide.actions.PropertiesAction;
import com.netbeans.ide.loaders.UniFileLoader;
import com.netbeans.ide.loaders.MultiDataObject;
import com.netbeans.ide.loaders.DataObjectExistsException;
import com.netbeans.ide.loaders.EditorSupport;
import com.netbeans.ide.filesystems.FileObject;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.actions.SystemAction;

/**
* Loader for Html DataObjects.
*
* @author Jan Jancura
*/
public class HtmlLoader extends UniFileLoader {


  {
    setActions (new SystemAction[] {
      SystemAction.get (ViewAction.class),
      SystemAction.get (OpenAction.class),
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
      new PropertiesAction ()
    });
  }

  public HtmlLoader() {
    super (MultiDataObject.class);
    getExtensions ().addExtension ("txt");
    getExtensions ().addExtension ("html");
    getExtensions ().addExtension ("htm");
    getExtensions ().addExtension ("shtml");
  }

  protected MultiDataObject createMultiObject (FileObject primaryFile)
  throws DataObjectExistsException, IOException {
  
    MultiDataObject obj = new MultiDataObject (primaryFile, this);
    obj.getCookieSet ().add (
      new Editor (obj.getPrimaryEntry ())
    );
    return obj;
  }
}

/*
* Log
*  2    Gandalf   1.1         1/11/99  Jan Jancura     
*  1    Gandalf   1.0         1/8/99   Jan Jancura     
* $
*/
