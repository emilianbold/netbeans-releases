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

import org.openide.*;
import org.openide.actions.OpenAction;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
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

  private static java.util.ResourceBundle formBundle = org.openide.util.NbBundle.getBundle (FormDataObject.class);

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


  transient private FormEditorSupport formEditor;

  /** The entry for the .form file */
  FileEntry formEntry;
  
//--------------------------------------------------------------------
// Constructors

static final long serialVersionUID =-975322003627854168L;
  public FormDataObject (FileObject ffo, FileObject jfo, FormDataLoader loader) throws DataObjectExistsException {
    super(jfo, loader);
    formEntry = (FileEntry)registerEntry (ffo);
    init ();
  }

  /** Initalizes the FormDataObject after deserialization */
  private void init() {
    templateInit = false;
    modifiedInit = false;
    componentRefRegistered = false;
  }

//--------------------------------------------------------------------
// Important interface

//--------------------------------------------------------------------
// Other methods

  public FileObject getFormFile () {
    return getFormEntry ().getFile ();
  }
  
  protected JavaEditor createJavaEditor () {
    if (formEditor == null) {
      formEditor = new FormEditorSupport (getPrimaryEntry (), this);
    }
    return formEditor;
  }

  public FormEditorSupport getFormEditor () {
    return (FormEditorSupport)createJavaEditor ();
  }
  
  FileEntry getFormEntry () {
    return formEntry;
  }
  
  /** Help context for this object.
  * @return help context
  */
  public org.openide.util.HelpCtx getHelpCtx () {
    return new org.openide.util.HelpCtx (FormDataObject.class);
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
 *  23   Gandalf   1.22        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  22   Gandalf   1.21        9/7/99   Ian Formanek    Method getFormEditor 
 *       made public
 *  21   Gandalf   1.20        8/10/99  Ian Formanek    Generated Serial Version
 *       UID
 *  20   Gandalf   1.19        7/23/99  Ian Formanek    Fixed Bug 2145 - 
 *       Openning form throws exception and form is not opened. Occurs after 
 *       some time  Bug 2673 - NullPointerException : choose from File menu New 
 *       From Template | AWT ... | Form set name and click OK, class not opened 
 *       but created
 *  19   Gandalf   1.18        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  18   Gandalf   1.17        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  17   Gandalf   1.16        5/17/99  Ian Formanek    Fixed bug 1803 - Newly 
 *       created forms are not automatically opened in editor.
 *  16   Gandalf   1.15        5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  15   Gandalf   1.14        4/27/99  Ian Formanek    Fixed bug #1457 - Form 
 *       DataObject does not have the "Execution" properties
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
