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

import java.io.IOException;
import java.text.MessageFormat;

import com.netbeans.ide.*;
import com.netbeans.ide.actions.OpenAction;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.nodes.Node;
import com.netbeans.ide.nodes.CookieSet;
import com.netbeans.developer.modules.loaders.java.JavaDataObject;
import com.netbeans.developer.modules.loaders.java.JavaEditor;
import com.netbeans.developer.modules.loaders.form.*;

/** The DataObject for forms.
*
* @author Ian Formanek, Petr Hamernik
*/
public class FormDataObject extends JavaDataObject {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = 7952143476761137063L;

//--------------------------------------------------------------------
// Static variables

  private static java.util.ResourceBundle formBundle = com.netbeans.ide.util.NbBundle.getBundle (FormDataObject.class);

  /** lock for closing window */
  private static final Object OPEN_FORM_LOCK = new Object ();

//--------------------------------------------------------------------
// Private variables

  /** If true, a postInit method is called after reparsing - used after createFromTemplate */
  transient private boolean templateInit;
  /** If true, the form is marked as modified after regeneration - used if created from template */
  transient private boolean modifiedInit;
  /** A flag to prevent multiple registration of ComponentRefListener */
  transient private boolean componentRefRegistered;


  /** The entry for the .form file */
  FileEntry formEntry;
  
//--------------------------------------------------------------------
// Constructors

  public FormDataObject (FileObject ffo, FileObject jfo, FormDataLoader loader) throws DataObjectExistsException {
    super(jfo, loader);
    init ();
  }

//--------------------------------------------------------------------
// Other methods

  /** Initalizes the FormDataObject after deserialization */
  private void init() {
    templateInit = false;
    modifiedInit = false;
    componentRefRegistered = false;
  }

  protected JavaEditor createJavaEditor () {
    return new FormEditorSupport (getPrimaryEntry (), this);
  }

  FileEntry getFormEntry () {
    return formEntry;
  }
  
  /** Help context for this object.
  * @return help context
  */
  public com.netbeans.ide.util.HelpCtx getHelpCtx () {
    return new com.netbeans.ide.util.HelpCtx (FormDataObject.class);
  }

  /** Handles copy of the data object.
  * @param f target folder
  * @return the new data object
  * @exception IOException if an error occures
  */
  public DataObject handleCopy (DataFolder df) throws IOException {
    String suffix = existInFolder(formEntry.getFile(), df.getPrimaryFile ());
    FileObject ffo = formEntry.copy (df.getPrimaryFile (), suffix);
    FileObject jfo = getPrimaryEntry ().copy (df.getPrimaryFile (), suffix);
    FormDataObject fdo = new FormDataObject (ffo, jfo, (FormDataLoader)getMultiFileLoader ());
//    fdo.instantiated = true;
    return fdo;
  }

  /** Check if in specific folder exists fileobject with the same name.
  * If it exists user is asked for confirmation to rewrite, rename or cancel operation.
  * @param folder destination folder
  * @return the suffix which should be added to the name or null if operation is cancelled
  */
  private static String existInFolder(FileObject fo, FileObject folder) {
    String orig = fo.getName ();
    String name = FileUtil.findFreeFileName(
      folder, orig, fo.getExt ()
    );
    if (name.length () <= orig.length ()) {
      return "";
    } else {
      return name.substring (orig.length ());
    }
  }
  
  /** Handles creation of new data object from template. This method should
  * copy content of the template to destination folder and assign new name
  * to the new object.
  *
  * @param df data folder to create object in
  * @param name name to give to the new object (or <CODE>null</CODE>
  *    if the name is up to the template
  * @return new data object
  * @exception IOException if an error occured
  */
  public DataObject handleCreateFromTemplate (
    DataFolder df, String name
  ) throws IOException {
    return super.handleCreateFromTemplate (df, name); // [PENDING temporary]
/*    if ((name != null) && (!com.netbeans.ide.util.Utilities.isJavaIdentifier (name)))
      throw new IOException ();
/*          java.text.MessageFormat.format (
              javaBundle.getString ("FMT_Not_Valid_Class_Name"),
              new Object[] { name }
              )
          ); * /
    FileObject ffo = formEntry.createFromTemplate (df, name);
    FileObject jfo = null;
    try {
      jfo = getPrimaryEntry ().createFromTemplate (df, name);
    } catch (IOException e) { // if the creation of *.java fails, we must remove the created .form
      FileLock lock = null;
      try {
        lock = ffo.lock ();
        ffo.delete(lock);
      } catch (IOException e2) {
        // ignore, what else can we do
      } finally {
        if (lock != null)
          lock.releaseLock();
      }
      throw e;
    }
    FormDataObject fdo = new FormDataObject (ffo, jfo);
    fdo.setTemplate(false);
    fdo.templateInit = true;
    fdo.instantiated = true;
    fdo.modifiedInit = true;
    return fdo; */
  }

  /** Provides node that should represent this data object. When a node for representation
  * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
  * with only parent changed. This implementation creates instance
  * <CODE>DataNode</CODE>.
  * <P>
  * This method is called only once.
  *
  * @return the node representation for this data object
  * @see DataNode
  */
  protected Node createNodeDelegate () {
    FormDataNode node = new FormDataNode (this);
    node.setDefaultAction (SystemAction.get (OpenAction.class));
    return node;
  }

//--------------------------------------------------------------------
// Serialization

  private void readObject(java.io.ObjectInputStream is)
  throws java.io.IOException, ClassNotFoundException {
    is.defaultReadObject();
    init();
  }

}

/*
 * Log
 *  14   Gandalf   1.13        4/26/99  Ian Formanek    
 *  13   Gandalf   1.12        4/4/99   Ian Formanek    Fixed creation from 
 *       template
 *  12   Gandalf   1.11        3/27/99  Ian Formanek    Removed obsoleted import
 *  11   Gandalf   1.10        3/24/99  Ian Formanek    
 *  10   Gandalf   1.9         3/24/99  Ian Formanek    
 *  9    Gandalf   1.8         3/22/99  Ian Formanek    
 *  8    Gandalf   1.7         3/17/99  Ian Formanek    
 *  7    Gandalf   1.6         3/17/99  Ian Formanek    
 *  6    Gandalf   1.5         3/16/99  Ian Formanek    
 *  5    Gandalf   1.4         3/14/99  Jaroslav Tulach Change of 
 *       MultiDataObject.Entry.
 *  4    Gandalf   1.3         3/10/99  Ian Formanek    Gandalf updated
 *  3    Gandalf   1.2         2/11/99  Ian Formanek    getXXXPresenter -> 
 *       createXXXPresenter (XXX={Menu, Toolbar})
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
