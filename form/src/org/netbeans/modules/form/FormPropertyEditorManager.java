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
      else if (Character.TYPE.equals (type)) typeName = "char";
    }
    return typeName;
  }
  
  public static synchronized PropertyEditor[] getAllEditors (Class type, boolean allFromSearchPath) {
    Class[] eds = (Class[])editorsCache.get (type);
    if (eds != null) {
      return createEditorInstances (eds, type);
    }

    ArrayList editorsList = new ArrayList (5);

    String typeName = getTypeName (type);

    // 1. try adding "Editor" to the class name.
    String editorName = type.getName() + "Editor";
    try {
      editorsList.add (Class.forName (editorName, true, TopManager.getDefault ().currentClassLoader ()));
    } catch (Exception e) {
      // Silently ignore any not found editors.
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
        Class editorClass = Class.forName (name, true, TopManager.getDefault ().currentClassLoader ());
        editorsList.add (editorClass);
        if (!allFromSearchPath) {
          break; // stop on first found editor if allFromSearchPath is false
        }
      } catch (Exception e) {
        // Silently ignore any not found editors.
      }
    }

    // 2. use explicitly registered editors
    String [][] registered = formSettings.getRegisteredEditors ();
    for (int i = 0; i < registered.length; i++) {
      if (registered[i].length > 0) {
        if (registered[i][0].equals (typeName)) {
          for (int j = 1; j < registered[i].length; j++) {
            try {
              editorsList.add (Class.forName (registered[i][j], true, TopManager.getDefault ().currentClassLoader ()));
            } catch (Exception e) {
              // Silently ignore any errors.
              if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace ();
            }
          }
        }
      }
    }

    // 3. use explicitly registered transient editors
    Class[] explicite = (Class[]) expliciteEditors.get (typeName);
    if (explicite != null) {
      for (int i = 0; i < explicite.length; i++) {
        editorsList.add (explicite[i]);
      }
    }

    // 4. Fourth add the RADConnectionPropertyEditor as the default editor for all values
    editorsList.add (RADConnectionPropertyEditor.class);

    Class[] editorsArray = new Class[editorsList.size ()];
    editorsList.toArray (editorsArray);
    // Cache the list for future reuse
    editorsCache.put (type, editorsArray);

    return createEditorInstances (editorsArray, type);
  }

  private static PropertyEditor[] createEditorInstances (Class[] editorClasses, Class propertyType) {
    ArrayList instancesList = new ArrayList (editorClasses.length);
    for (int i = 0; i < editorClasses.length; i++) {
      if (RADConnectionPropertyEditor.class.isAssignableFrom (editorClasses[i])) { // ignore classes which do not implement java.beans.PropertyEditor
        instancesList.add (new RADConnectionPropertyEditor (propertyType));
      } else if (java.beans.PropertyEditor.class.isAssignableFrom (editorClasses[i])) { // ignore classes which do not implement java.beans.PropertyEditor
        try {
          instancesList.add (editorClasses[i].newInstance ());
        } catch (Exception e) {
          // Silently ignore any errors.
          if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace ();
        }
      }
    }
    return (PropertyEditor[])instancesList.toArray (new PropertyEditor [instancesList.size ()]);
  }
  
  synchronized static void clearEditorsCache () {
    editorsCache.clear ();
  }
  
}

/*
 * Log
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         9/24/99  Ian Formanek    Fixed char primitive 
 *       type not being included in list of primitive types.
 *  9    Gandalf   1.8         8/9/99   Ian Formanek    Used currentClassLoader 
 *       to fix problems with loading beans only present in repository
 *  8    Gandalf   1.7         8/1/99   Ian Formanek    PropertyEditors from 
 *       search path are the first ones, as it is more likely that they consume 
 *       the standard value type (i.e. the type equal to the property type as 
 *       opposed to design values)
 *  7    Gandalf   1.6         7/23/99  Ian Formanek    Caching editor classes
 *  6    Gandalf   1.5         7/20/99  Ian Formanek    Fixed bug which 
 *       prevented some forms from opening when there was a bad property editor 
 *       in the search path
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
