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


package org.netbeans.modules.form;

import org.openide.TopManager;
import org.openide.util.SharedClassObject; 

import java.beans.*;
import java.util.ArrayList;
import java.util.HashMap;

/** A class that manages bean property editors for the Form Editor.
 *
 * @author   Ian Formanek
 */
final public class FormPropertyEditorManager extends Object
{
    private static FormLoaderSettings formSettings = (FormLoaderSettings)
                   SharedClassObject.findObject(FormLoaderSettings.class, true);

    private static HashMap editorsCache = new HashMap(30);

    private static HashMap expliciteEditors = new HashMap(10);


    public static synchronized PropertyEditor findEditor(Class type) {
        Class[] edClasses = findEditorClasses(type);
        if (edClasses.length > 0) {
            PropertyEditor[] editors =
                createEditorInstances(new Class[] { edClasses[0] }, type);
            if (editors.length > 0)
                return editors[0];
        }
        return null;
    }

    public static synchronized void registerEditor(Class type, Class editorClass) {
        Class[] currentEditors = (Class[])expliciteEditors.get(getTypeName(type));
        Class[] newEditors;
        if (currentEditors == null) {
            newEditors = new Class[1];
            newEditors[0] = editorClass;
        }
        else {
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

    public static synchronized PropertyEditor[] getAllEditors(Class type) {
        return createEditorInstances(findEditorClasses(type), type);
    }

    synchronized static void clearEditorsCache() {
        editorsCache.clear();
    }

    // -------

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

    private static Class[] findEditorClasses(Class type) {
        // try the editors cache
        Class[] edClasses = (Class[]) editorsCache.get(type);
        if (edClasses != null)
            return edClasses;

        ArrayList editorsList = new ArrayList(5);

        // 1st - try standard way through PropertyEditorManager
        PropertyEditor stdPropEd = PropertyEditorManager.findEditor(type);
        if (stdPropEd != null) {
            editorsList.add(stdPropEd.getClass());
        }
        else {
            // 2nd - try the form editor's specific search path
            String editorName = type.getName();
            if (!editorName.startsWith("[")) { // not an array type
                int dot = editorName.lastIndexOf('.');
                if (dot > 0)
                    editorName = editorName.substring(dot+1);

                String[] searchPath = formSettings.getEditorSearchPath();
                for (int i = 0; i < searchPath.length; i++) {
                    String name = searchPath[i] + "." + editorName + "Editor"; // NOI18N
                    try {
                        Class edClass = Class.forName(name, true,
                                TopManager.getDefault().currentClassLoader());
                        editorsList.add(edClass);
                        break; // stop on first found editor
                    }
                    catch (Exception e) {} // silently ignore not found editors
                }
            }
        }

        // 3rd - search in explicitly registered editors (in Options)
        String typeName = getTypeName(type);
        String[][] registered = formSettings.getRegisteredEditors();
        for (int i = 0; i < registered.length; i++) {
            String[] typereg = registered[i];
            if (typereg.length > 0) {
                if (typereg[0].equals(typeName)) {
                    for (int j = 1; j < typereg.length; j++) {
                        try {
                            Class edClass = Class.forName(typereg[j], true,
                                TopManager.getDefault().currentClassLoader());
                            if (!editorsList.contains(edClass))
                                editorsList.add(edClass);
                        }
                        catch (Exception e) { // ignore
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                                e.printStackTrace();
                        }
                    }
                }
            }
        }

        // 4th - search in editors registered using registerEditor(...)
        Class[] explicite = (Class[]) expliciteEditors.get(typeName);
        if (explicite != null) {
            for (int i = 0; i < explicite.length; i++) {
                Class edClass = explicite[i];
                if (!editorsList.contains(edClass))
                    editorsList.add(edClass);
            }
        }

        // 5th - add the RADConnectionPropertyEditor for all values
        editorsList.add(RADConnectionPropertyEditor.class);

        edClasses = new Class[editorsList.size()];
        editorsList.toArray(edClasses);
        return edClasses;
    }

    private static PropertyEditor[] createEditorInstances(Class[] edClasses,
                                                          Class propertyType) {
        ArrayList instancesList = new ArrayList(edClasses.length);

        for (int i = 0; i < edClasses.length; i++) {
            if (RADConnectionPropertyEditor.class.isAssignableFrom(edClasses[i])) {
                instancesList.add(new RADConnectionPropertyEditor(propertyType));
            }
            else if (PropertyEditor.class.isAssignableFrom(edClasses[i])) {
                try {
                    instancesList.add(edClasses[i].newInstance());
                }
                catch (Exception e) { // ignore
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        e.printStackTrace();
                }
            }
            // ignore classes which do not implement java.beans.PropertyEditor
        }

        PropertyEditor[] editors = new PropertyEditor[instancesList.size()];
        instancesList.toArray(editors);
        return editors;
    }
}
