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

import com.netbeans.ide.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.loaders.DataNode;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.src.nodes.SourceChildren;
import com.netbeans.ide.util.HelpCtx;
import com.netbeans.developer.modules.loaders.form.formeditor.*;
import com.netbeans.developer.modules.loaders.java.*;

/** The DataNode for Forms.
*
* @author Ian Formanek
* @version 1.00, Jul 21, 1998
*/
public class FormDataNode extends DataNode {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = 1795549004166402392L;
  
  /** Icons for java data objects. */
  private static final String ICON_BASE = "com/netbeans/developer/modules/loaders/form/resources/";
  private static final String ICON_CLASS = "form";
  private static final String ICON_CLASS_MAIN = "formMain";
  private static final String ICON_CLASS_ERROR = "formError";

  transient private String currentIcon;

  /** Constructs a new FormDataObject for specified primary file */
  public FormDataNode (FormDataObject fdo) {
    super (fdo, new SourceChildren(JavaElementNodeFactory.DEFAULT, fdo.getSource()));
    initialize ();
  }

  private void initialize () {
    setIconBase(ICON_BASE + ICON_CLASS);
    currentIcon = ICON_CLASS;
  }

  protected void resolveIcons () {
    FormDataObject fdo = (FormDataObject) getDataObject ();
    if (false /*errorWhileParsing || 
        !fdo.hasValidPackage () */
    ) {
      if (currentIcon == ICON_CLASS_ERROR) return;
      currentIcon = ICON_CLASS_ERROR;
    } else {
      if (false /*fdo.getHasMainMethod ()*/) {
        if (currentIcon == ICON_CLASS_MAIN) return;
        currentIcon = ICON_CLASS_MAIN;
      } else {
        if (currentIcon == ICON_CLASS) return;
        currentIcon = ICON_CLASS;
      }
    }
    setIconBase (ICON_BASE + currentIcon);
  }

}

/*
 * Log
 *  5    Gandalf   1.4         4/13/99  Ian Formanek    Fixed problems with form
 *       node children
 *  4    Gandalf   1.3         3/14/99  Ian Formanek    
 *  3    Gandalf   1.2         3/10/99  Ian Formanek    Gandalf updated
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    reflecting changes in JavaNode
 */
