/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/* $Id$ */

package org.netbeans.modules.form;

import org.openide.TopManager;
import org.openide.util.SharedClassObject; 

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.HashMap;

/** A class that manages bean property editors for the Form Editor.
 *
 * @author   Ian Formanek
 */
final public class FormPropertyEditorManager extends Object {
    private static FormLoaderSettings formSettings = (FormLoaderSettings)
                   SharedClassObject.findObject(FormLoaderSettings.class, true);
    private static HashMap editorsCache = new HashMap(30);
    private static HashMap expliciteEditors = new HashMap(10);

    public static synchronized PropertyEditor findEditor(Class type) {
        PropertyEditor[] eds = getAllEditors(type, false);
        if (eds.length > 0) {
            return eds[0];
        } else {
            return null;
        }
    }

    public static synchronized void registerEditor(Class type, Class editorClass) {
        Class[] currentEditors =(Class[]) expliciteEditors.get(getTypeName(type));
        Class[] newEditors;
        if (currentEditors == null) {
            newEditors = new Class[1];
            newEditors[0] = editorClass;
        } else {
            // check whether the editor is not already registered
            for (int i = 0; i < currentEditors.length; i++) {
                if (currentEditors[i].equals(editorClass)) {
                    return; // do nothing in such case
                }
            }
            newEditors = new Class[currentEditors.length + 1];
            System.arraycopy(currentEditors, 0, newEditors, 0, currentEditors.length);
            newEditors[newEditors.length - 1] = editorClass;
        }
        expliciteEditors.put(getTypeName(type), newEditors);
    }

    private static String getTypeName(Class type) {
        String typeName = type.getName();
        if (type.isPrimitive()) {
            if (Byte.TYPE.equals(type)) typeName = "byte"; // NOI18N
            else if (Short.TYPE.equals(type)) typeName = "short"; // NOI18N
            else if (Integer.TYPE.equals(type)) typeName = "integer"; // NOI18N
            else if (Long.TYPE.equals(type)) typeName = "long"; // NOI18N
            else if (Boolean.TYPE.equals(type)) typeName = "boolean"; // NOI18N
            else if (Float.TYPE.equals(type)) typeName = "float"; // NOI18N
            else if (Double.TYPE.equals(type)) typeName = "double"; // NOI18N
            else if (Character.TYPE.equals(type)) typeName = "char"; // NOI18N
        }
        return typeName;
    }

    public static synchronized PropertyEditor[] getAllEditors(Class type, boolean allFromSearchPath) {
        Class[] eds =(Class[])editorsCache.get(type);
        if (eds != null) {
            return createEditorInstances(eds, type);
        }

        ArrayList editorsList = new ArrayList(5);

        String typeName = getTypeName(type);

        // 1. try adding "Editor" to the class name. // NOI18N
        String editorName = type.getName() + "Editor"; // NOI18N
        try {
            editorsList.add(Class.forName(editorName, true, TopManager.getDefault().currentClassLoader()));
        } catch (Exception e) {
            // Silently ignore any not found editors.
        }

        // Third try looking for <searchPath>.fooEditor
        String[] searchPath = formSettings.getEditorSearchPath();

        editorName = type.getName();
        while (editorName.indexOf('.') > 0) {
            editorName = editorName.substring(editorName.indexOf('.') + 1);
        }
        for (int i = 0; i < searchPath.length; i++) {
            String name = searchPath[i] + "." + editorName + "Editor"; // NOI18N
            try {
                Class editorClass = Class.forName(name, true, TopManager.getDefault().currentClassLoader());
                editorsList.add(editorClass);
                if (!allFromSearchPath) {
                    break; // stop on first found editor if allFromSearchPath is false
                }
            } catch (Exception e) {
                // Silently ignore any not found editors.
            }
        }

        // 2. use explicitly registered editors
        String [][] registered = formSettings.getRegisteredEditors();
        for (int i = 0; i < registered.length; i++) {
            if (registered[i].length > 0) {
                if (registered[i][0].equals(typeName)) {
                    for (int j = 1; j < registered[i].length; j++) {
                        try {
                            editorsList.add(Class.forName(registered[i][j], true, TopManager.getDefault().currentClassLoader()));
                        } catch (Exception e) {
                            // Silently ignore any errors.
                            if (System.getProperty("netbeans.debug.exceptions") != null) e.printStackTrace();
                        }
                    }
                }
            }
        }

        // 3. use explicitly registered transient editors
        Class[] explicite =(Class[]) expliciteEditors.get(typeName);
        if (explicite != null) {
            for (int i = 0; i < explicite.length; i++) {
                editorsList.add(explicite[i]);
            }
        }

        // 4. Fourth add the RADConnectionPropertyEditor as the default editor for all values
        editorsList.add(RADConnectionPropertyEditor.class);

        Class[] editorsArray = new Class[editorsList.size()];
        editorsList.toArray(editorsArray);
        // Cache the list for future reuse
        editorsCache.put(type, editorsArray);

        return createEditorInstances(editorsArray, type);
    }

    private static PropertyEditor[] createEditorInstances(Class[] editorClasses, Class propertyType) {
        ArrayList instancesList = new ArrayList(editorClasses.length);
        for (int i = 0; i < editorClasses.length; i++) {
            if (RADConnectionPropertyEditor.class.isAssignableFrom(editorClasses[i])) { // ignore classes which do not implement java.beans.PropertyEditor
                instancesList.add(new RADConnectionPropertyEditor(propertyType));
            } else if (java.beans.PropertyEditor.class.isAssignableFrom(editorClasses[i])) { // ignore classes which do not implement java.beans.PropertyEditor
                try {
                    instancesList.add(editorClasses[i].newInstance());
                } catch (Exception e) {
                    // Silently ignore any errors.
                    if (System.getProperty("netbeans.debug.exceptions") != null) e.printStackTrace();
                }
            }
        }
        return(PropertyEditor[])instancesList.toArray(new PropertyEditor [instancesList.size()]);
    }

    synchronized static void clearEditorsCache() {
        editorsCache.clear();
    }

}
