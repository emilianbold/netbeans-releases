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

import java.lang.reflect.InvocationTargetException;

import com.netbeans.ide.util.HelpCtx;
import com.netbeans.developer.modules.loaders.form.formeditor.*;
import com.netbeans.developer.modules.loaders.java.JavaNode;

/** The DataNode for Forms.
*
* @author Ian Formanek
* @version 1.00, Jul 21, 1998
*/
public class FormDataNode extends JavaNode {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = 1795549004166402392L;
  
  /** Icons for form data objects. */
  private static final String[] FORM_ICONS = { "form", "formMain", "formError", "form", "formMain" }; // [PENDING bean forms]

  /** Icon base for form data objects. */
  private static final String FORM_ICON_BASE = "com/netbeans/developer/modules/loaders/form/resources/";
    
  transient private String currentIcon;

  /** Constructs a new FormDataObject for specified primary file */
  public FormDataNode (FormDataObject fdo) {
    super (fdo);
  }

  protected String getIconBase() {
    return FORM_ICON_BASE;
  }

  protected String[] getIcons() {
    return FORM_ICONS;
  }
  
}

/*
 * Log
 *  7    Gandalf   1.6         4/29/99  Ian Formanek    
 *  6    Gandalf   1.5         4/27/99  Ian Formanek    Fixed bug #1457 - Form 
 *       DataObject does not have the "Execution" properties
 *  5    Gandalf   1.4         4/13/99  Ian Formanek    Fixed problems with form
 *       node children
 *  4    Gandalf   1.3         3/14/99  Ian Formanek    
 *  3    Gandalf   1.2         3/10/99  Ian Formanek    Gandalf updated
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
