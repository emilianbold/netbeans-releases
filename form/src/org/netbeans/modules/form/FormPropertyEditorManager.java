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
  private static HashMap expliciteEditors = new HashMap (10);

  public static synchronized PropertyEditor findEditor (Class type) {
    PropertyEditor[] eds = getAllEditors (type, false);
    if (eds.length > 0) {
      return eds[0];
    } else {
      return null;
    }
  }

  public static synchronized void registerEditor (Class type, Class editorClass) {
    Class[] currentEditors = (Class[]) expliciteEditors.get (getTypeName (type));
    Class[] newEditors;
    if (currentEditors == null) {
      newEditors = new Class[1];
      newEditors[0] = editorClass;
    } else {
      // check whether the editor is not already registered
      for (int i = 0; i < currentEditors.length; i++) {
        if (currentEditors[i].equals (editorClass)) {
          return; // do nothing in such case
        }
      }
      newEditors = new Class[currentEditors.length + 1];
      System.arraycopy (currentEditors, 0, newEditors, 0, currentEditors.length);
      newEditors[newEditors.length - 1] = editorClass;
    }
    expliciteEditors.put (getTypeName (type), newEditors);
  }

  private static String getTypeName (Class type) {
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
    return typeName;
  }
  
  public static synchronized PropertyEditor[] getAllEditors (Class type, boolean allFromSearchPath) {
/*    PropertyEditor[] eds = (PropertyEditor[])editorsCache.get (type);
    if (eds != null) {
      return eds;
    }
*/
    ArrayList editorsList = new ArrayList (5);

    String typeName = getTypeName (type);

    // 1. use explicitly registered editors
    String [][] registered = formSettings.getRegisteredEditors ();
    for (int i = 0; i < registered.length; i++) {
      if (registered[i].length > 0) {
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
              // Silently ignore any errors.
            }
          }
        }
      }
    }

    // 2. use explicitly registered transient editors
    Class[] explicite = (Class[]) expliciteEditors.get (typeName);
    if (explicite != null) {
      for (int i = 0; i < explicite.length; i++) {
        try {
          editorsList.add (explicite[i].newInstance ());
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

    // 3. try adding "Editor" to the class name.
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

    // Fourth add the RADConnectionPropertyEditor as the default editor for all values
    editorsList.add (new RADConnectionPropertyEditor (type));

    PropertyEditor[] eds = new PropertyEditor[editorsList.size ()];
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
 *  5    Gandalf   1.4         6/27/99  Ian Formanek    Employed 
 *       RADConnectionPropertyEditor
 *  4    Gandalf   1.3         6/22/99  Ian Formanek    registering transient 
 *       property editors
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/30/99  Ian Formanek    minor changes, editors 
 *       cache disabled
 *  1    Gandalf   1.0         5/24/99  Ian Formanek    
 * $
 */
