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

import org.openide.TopManager;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.HashMap;

/** A class that manages bean property editors for the Form Editor.
*
* @author   Ian Formanek
*/
final public class FormPropertyEditorManager extends Object {
  private static FormLoaderSettings formSettings = new FormLoaderSettings ();
  private static HashMap editorsCache = new HashMap (30);

  public static synchronized PropertyEditor findEditor (Class type) {
    PropertyEditor[] eds = getAllEditors (type, false);
    if (eds.length > 0) {
      return eds[0];
    } else {
      return null;
    }
  }
  
  public static synchronized PropertyEditor[] getAllEditors (Class type, boolean allFromSearchPath) {
    PropertyEditor[] eds = (PropertyEditor[])editorsCache.get (type);
    if (eds != null) {
      return eds;
    }

    ArrayList editorsList = new ArrayList (5);

    // First use explicitly registered editors
    String [][] registered = formSettings.getRegisteredEditors ();
    for (int i = 0; i < registered.length; i++) {
      if (registered[i].length > 0) {
        String typeName = type.getName ();
        if (type.isPrimitive ()) {
          if (Byte.TYPE.equals (type)) typeName = "byte";
          else if (Short.TYPE.equals (type)) typeName = "short";
          else if (Integer.TYPE.equals (type)) typeName = "integer";
          else if (Long.TYPE.equals (type)) typeName = "long";
          else if (Boolean.TYPE.equals (type)) typeName = "boolean";
          else if (Float.TYPE.equals (type)) typeName = "float";
          else if (Double.TYPE.equals (type)) typeName = "double";
        }
        if (registered[i][0].equals (typeName)) {
          for (int j = 1; j < registered[i].length; j++) {
            try {
              editorsList.add (Class.forName (registered[i][j], true, TopManager.getDefault ().systemClassLoader ()).newInstance ());
            } catch (Exception e) {
              // Silently ignore any errors.
            } catch (Throwable t) { // [PENDING] should not be necessary
              if (t instanceof ThreadDeath) {
                throw (ThreadDeath)t;
              }
              t.printStackTrace ();
              // Silently ignore any errors.
            }
          }
        }
      }
    }

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
    String[] searchPath = formSettings.getEditorSearchPath ();
    
    editorName = type.getName();
    while (editorName.indexOf('.') > 0) {
      editorName = editorName.substring(editorName.indexOf('.') + 1);
    }
    for (int i = 0; i < searchPath.length; i++) {
      String name = searchPath[i] + "." + editorName + "Editor";
      try {
        editorsList.add (Class.forName (name, true, TopManager.getDefault ().systemClassLoader ()).newInstance ());
        if (!allFromSearchPath) {
          break; // stop on first found editor if allFromSearchPath is false
        }
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
    //editorsCache.put (type, eds);
    
    return eds;
  }
  
  synchronized static void clearEditorsCache () {
    editorsCache.clear ();
  }
  
}

/*
 * Log
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/30/99  Ian Formanek    minor changes, editors 
 *       cache disabled
 *  1    Gandalf   1.0         5/24/99  Ian Formanek    
 * $
 */
