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

import com.netbeans.ide.modules.ModuleInstall;
/**
* Module installation class for editor
*
* @author Miloslav Metelka
* @version 1.0
*/
public class EditorModule implements ModuleInstall {

  /** Kit replacements that will be installed into JEditorPane */
  KitInfo[] replacements = new KitInfo[] {
    new KitInfo("text/plain", NbEditorBaseKit.class.getName()),
    new KitInfo("text/x-java", NbEditorJavaKit.class.getName())
  };

  /** Module installed for the first time. */
  public void installed () {
    restored ();
  }

  /** Module installed again. */
  public void restored () {
    for (int i = 0; i < replacements.length; i++) {
      // install new kit
      JEditorPane.registerEditorKitForContentType(
        replacements[i].contentType,
        replacements[i].newKitClassName,
        getClass ().getClassLoader ()
      );
    }
  }

  /** Module was uninstalled. */
  public void uninstalled () {
  }

  /** Module is being closed. */
  public boolean closing () {
    return true; // agree to close
  }

  static class KitInfo {

    /** Content type for which the kits will be switched */
    String contentType;

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
 *  8    Gandalf   1.7         4/8/99   Miloslav Metelka 
 *  7    Gandalf   1.6         3/18/99  Miloslav Metelka 
 *  6    Gandalf   1.5         3/11/99  Jaroslav Tulach Works with plain 
 *       document.
 *  5    Gandalf   1.4         3/10/99  Jaroslav Tulach body of install moved to
 *       restored.
 *  4    Gandalf   1.3         3/9/99   Ian Formanek    Fixed last change
 *  3    Gandalf   1.2         3/9/99   Ian Formanek    Removed obsoleted import
 *  2    Gandalf   1.1         3/8/99   Jesse Glick     For clarity: Module -> 
 *       ModuleInstall; NetBeans-Module-Main -> NetBeans-Module-Install.
 *  1    Gandalf   1.0         2/4/99   Miloslav Metelka 
 * $
 */
