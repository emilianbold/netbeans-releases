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

import com.netbeans.ide.TopManager;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.HashMap;

/** A class that manages bean property editors for the Form Editor.
*
* @author   Ian Formanek
*/
final public class FormPropertyEditorManager extends Object {
  private FormLoaderSettings formSettings = new FormLoaderSettings ();
  private static HashMap editorsCache = new HashMap (30);

  public static synchronized PropertyEditor getDefaultEditor (Class type) {
    PropertyEditor[] eds = getAllEditors (type);
    if (eds.length > 0) {
      return eds[0];
    } else {
      return null;
    }
  }
  
  public static synchronized PropertyEditor[] getAllEditors (Class type) {
    PropertyEditor[] eds = (PropertyEditor[])editorsCache.get (type);
    if (eds != null) {
      return eds;
    }

    ArrayList editorsList = new ArrayList (5);

    // First use explicitly registered editors
/*    Map registered = formSettings.getRegisteredEditors ();
    String[] names = registered.get (type.getName ());
    if ((names != null) && (names.length > 0)) {
      for (int i = 0; i < names.length; i++) {
        try {
          editorsList.add (Class.forName (names[i], true, TopManager.getDefault ().systemClassLoader ()).newInstance ());
        } catch (Exception e) {
          // Silently ignore any errors.
        } catch (Throwable t) {
          if (t instanceof ThreadDeath) {
            throw (ThreadDeath)t;
          }
          t.printStackTrace ();
          // Silently ignore any errors.
        }
      }
    }
*/      // [PENDING]

    // Second try adding "Editor" to the class name.
    String editorName = type.getName() + "Editor";
    try {
      editorsList.add (Class.forName (editorName, true, TopManager.getDefault ().systemClassLoader ()).newInstance ());
    } catch (Exception e) {
      // Silently ignore any errors.
    } catch (Throwable t) {
      if (t instanceof ThreadDeath) {
        throw (ThreadDeath)t;
      }
      t.printStackTrace ();
      // Silently ignore any errors.
    }

    // Third try looking for <searchPath>.fooEditor
    String[] searchPath = PropertyEditorManager.getEditorSearchPath (); // formSettings.getEditorSearchPath (); // [PENDING]
    
    editorName = type.getName();
    while (editorName.indexOf('.') > 0) {
      editorName = editorName.substring(editorName.indexOf('.') + 1);
    }
    for (int i = 0; i < searchPath.length; i++) {
      String name = searchPath[i] + "." + editorName + "Editor";
      try {
        editorsList.add (Class.forName (name, true, TopManager.getDefault ().systemClassLoader ()).newInstance ());
      } catch (Exception e) {
        // Silently ignore any errors.
      } catch (Throwable t) {
        if (t instanceof ThreadDeath) {
          throw (ThreadDeath)t;
        }
        t.printStackTrace ();
        // Silently ignore any errors.
      }
    }


    eds = new PropertyEditor[editorsList.size ()];
    editorsList.toArray (eds);
    
    // Cache the list for future reuse
    editorsCache.put (type, eds);
    
    return eds;
  }
  
  synchronized void clearEditorsCache () {
    editorsCache.clear ();
  }
  
}

/*
 * Log
 *  1    Gandalf   1.0         5/24/99  Ian Formanek    
 * $
 */
