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

package com.netbeans.developer.modules.text;

import javax.swing.text.EditorKit;
import javax.swing.JEditorPane;

/**
* Module installation class for editor
*
* @author Miloslav Metelka
* @version 1.0
*/
public class EditorModule implements ModuleInstall {

  /** Kit replacements that will be installed into JEditorPane */
  KitInfo[] replacements = new KitInfo[] {
    new KitInfo("text/plain", com.netbeans.editor.BaseEditorKit.class.getName()),
    new KitInfo("text/x-java", com.netbeans.editor.ext.JavaEditorKit.class.getName())
  };

  /** Module installed for the first time. */
  public void installed () {
    JEditorPane tmpPane = new JEditorPane(); // getEditorKitForContentType is not static
    for (int i = 0; i < replacements.length; i++) {
      // store old kit
      EditorKit kit = tmpPane.getEditorKitForContentType(
          replacements[i].contentType);
      if (kit != null) {
        replacements[i].oldKitClassName = kit.getClass().getName();
      }
      // install new kit
      JEditorPane.registerEditorKitForContentType(replacements[i].contentType,
          replacements[i].newKitClassName);
    }
  }

  /** Module installed again. */
  public void restored () {
    // XXX body of installed() here instead?
  }

  /** Module was uninstalled. */
  public void uninstalled () {
    for (int i = 0; i < replacements.length; i++) {
      // restore old kit
      if (replacements[i].oldKitClassName != null) {
        JEditorPane.registerEditorKitForContentType(replacements[i].contentType,
            replacements[i].oldKitClassName);
      }
    }
  }

  /** Module is being closed. */
  public boolean closing () {
    return true; // agree to close
  }

  static class KitInfo {

    /** Content type for which the kits will be switched */
    String contentType;

    /** Class name of the previously registered editor kit */
    String oldKitClassName;

    /** Class name of the kit that will be registered */
    String newKitClassName;

    KitInfo(String contentType, String newKitClassName) {
      this.contentType = contentType;
      this.newKitClassName = newKitClassName;
    }

  }


}

/*
 * Log
 *  3    Gandalf   1.2         3/9/99   Ian Formanek    Removed obsoleted import
 *  2    Gandalf   1.1         3/8/99   Jesse Glick     For clarity: Module -> 
 *       ModuleInstall; NetBeans-Module-Main -> NetBeans-Module-Install.
 *  1    Gandalf   1.0         2/4/99   Miloslav Metelka 
 * $
 */
