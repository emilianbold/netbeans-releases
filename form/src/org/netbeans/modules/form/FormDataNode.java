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
import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.nodes.*;
import com.netbeans.developer.modules.loaders.form.formeditor.*;
import com.netbeans.developer.modules.loaders.java.*;

/** The DataNode for Forms.
*
* @author Ian Formanek
* @version 1.00, Jul 21, 1998
*/
public class FormDataNode extends JavaNode {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 1795549004166402392L;
  /** Icons for java data objects. */
  static protected java.awt.Image icon;
  static protected java.awt.Image icon32;
  static private java.awt.Image iconMain;
  static private java.awt.Image iconMain32;
  static private java.awt.Image iconError;
  static private java.awt.Image iconError32;

  static {
    icon = java.awt.Toolkit.getDefaultToolkit ().getImage (
        Object.class.getResource ("/com.netbeans.developer.modules/resources/java/form.gif"));
    icon32 = java.awt.Toolkit.getDefaultToolkit ().getImage (
        Object.class.getResource ("/com.netbeans.developer.modules/resources/java/form32.gif"));
    iconMain = java.awt.Toolkit.getDefaultToolkit ().getImage (
        Object.class.getResource ("/com.netbeans.developer.modules/resources/java/formMain.gif"));
    iconMain32 = java.awt.Toolkit.getDefaultToolkit ().getImage (
        Object.class.getResource ("/com.netbeans.developer.modules/resources/java/formMain32.gif"));
    iconError = java.awt.Toolkit.getDefaultToolkit ().getImage (
        Object.class.getResource ("/com.netbeans.developer.modules/resources/java/formError.gif"));
    iconError32 = java.awt.Toolkit.getDefaultToolkit ().getImage (
        Object.class.getResource ("/com.netbeans.developer.modules/resources/java/formError32.gif"));
  }

  /** Constructs a new FormDataObject for specified primary file */
  public FormDataNode (FormDataObject fdo) {
    super (fdo);
  }

  void updateFormNode () {
    parseChanged ();
  }

  protected Node[] createSubNodes(Node parNode) {
    Node[] inherited = super.createSubNodes(parNode);
    FormDataObject fdo = getFormDataObject();
    if (fdo.isLoaded ()) {
      Node[] newNodes = new Node [inherited.length + 1];
      System.arraycopy (inherited, 0, newNodes, 0, inherited.length);
      DesignForm form = fdo.getDesignForm();
      FormManager manager = form.getFormManager();
      newNodes [inherited.length] = new FilterNode (manager.getComponentsRoot(), this);
      return newNodes;
    }
    else
      return inherited;
  }


  /** @return The FormDataObject represented by this FormDataNode */
  public FormDataObject getFormDataObject() {
    return (FormDataObject) getDataObject();
  }

  protected java.awt.Image getDefaultIcon (int type) {
    if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
      return icon;
    else
      return icon32;
  }
    
  protected void resolveIcons () {
    FormDataObject fdo = (FormDataObject) getDataObject ();
    if (errorWhileParsing || 
        !fdo.hasValidPackage ()
    ) {
      if (currentIcon == iconError) return;
      currentIcon = iconError;
      currentIcon32 = iconError32;
    } else {
      if (fdo.getHasMainMethod ()) {
        if (currentIcon == iconMain) return;
        currentIcon = iconMain;
        currentIcon32 = iconMain32;
      } else {
        if (currentIcon == icon) return;
        currentIcon = icon;
        currentIcon32 = icon32;
      }
    }
    fireIconChange (null, currentIcon);
  }

  /** True, if the subnode for the AWT hierarchy has been added to the children list */
  transient private boolean initializedWithForm = false;
}

/*
 * Log
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    reflecting changes in JavaNode
 */
