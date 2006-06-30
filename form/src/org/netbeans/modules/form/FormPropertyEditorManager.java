/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

import java.beans.*;
import java.util.ArrayList;
import java.util.HashMap;

/** A class that manages bean property editors for the Form Editor.
 *
 * @author   Ian Formanek
 */
final public class FormPropertyEditorManager extends Object
{
    private static HashMap editorsCache = new HashMap(30);

    private static HashMap expliciteEditors = new HashMap(10);

    // -------

    public static synchronized PropertyEditor findEditor(FormProperty property) {
        Class propType = property.getValueType();
        FormModel form = property.getPropertyContext().getFormModel();
        Class[] edClasses = findEditorClasses(propType, form);
        if (edClasses.length > 0) {
            PropertyEditor[] editors =
                createEditorInstances(new Class[] { edClasses[0] }, propType);
            if (editors.length > 0)
                return editors[0];
        }
        return null;
    }

    public static synchronized PropertyEditor[] getAllEditors(FormProperty property) {
        Class propType = property.getValueType();
        FormModel form = property.getPropertyContext().getFormModel();
        return createEditorInstances(findEditorClasses(propType, form), propType);
    }

    public static synchronized void registerEditor(Class propertyType,
                                                   Class editorClass)
    {
        Class[] currentEditors = (Class[])expliciteEditors.get(getTypeName(propertyType));
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
        expliciteEditors.put(getTypeName(propertyType), newEditors);
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

    private static Class[] findEditorClasses(Class type, FormModel form) {
        // try the editors cache
        Class[] edClasses = (Class[]) editorsCache.get(type);
        if (edClasses != null)
            return edClasses;

        FormLoaderSettings formSettings = FormLoaderSettings.getInstance();

        ArrayList editorsList = new ArrayList(5);

        // 1st - try standard way through PropertyEditorManager
        PropertyEditor stdPropEd = (type == Object.class || type == java.awt.Font.class) ? null :
                                     PropertyEditorManager.findEditor(type);
        if (stdPropEd != null) {
            editorsList.add(stdPropEd.getClass());
        }
        else {
            // 2nd - try the form editor's specific search path
            String editorName = type.getName();
            if (!editorName.startsWith("[")) { // not an array type // NOI18N
                int dot = editorName.lastIndexOf('.');
                if (dot > 0)
                    editorName = editorName.substring(dot+1);

                String[] searchPath = formSettings.getEditorSearchPath();
                for (int i = 0; i < searchPath.length; i++) {
                    String name = searchPath[i] + "." + editorName + "Editor"; // NOI18N
                    try {
                        Class edClass = FormUtils.loadClass(name, form);
                        editorsList.add(edClass);
                        break; // stop on first found editor
                    }
                    catch (Exception e) {} // silently ignore
                    catch (LinkageError e) {} // silently ignore
                }
            }
        }

        // 3rd - search in explicitly registered editors (in Options)
        String typeName = getTypeName(type);
        String[][] registered = formSettings.getRegisteredEditors();
        for (int i = 0; i < registered.length; i++) {
            String[] typereg = registered[i];
            if ((typereg != null) && (typereg.length > 0)) {
                if (typereg[0].equals(typeName)) {
                    for (int j = 1; j < typereg.length; j++) {
                        try {
                            Class edClass = FormUtils.loadClass(typereg[j], form);
                            if (!editorsList.contains(edClass))
                                editorsList.add(edClass);
                        }
                        catch (Exception e) { // ignore
                            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
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

        // 5th - experimental: add ComponentChooserEditor for Components
        if (java.awt.Component.class.isAssignableFrom(type))
            editorsList.add(ComponentChooserEditor.class);

        // 6th - add the RADConnectionPropertyEditor for all values
        editorsList.add(RADConnectionPropertyEditor.class);

        edClasses = new Class[editorsList.size()];
        editorsList.toArray(edClasses);
        editorsCache.put(type, edClasses);

        return edClasses;
    }

    private static PropertyEditor[] createEditorInstances(Class[] edClasses,
                                                          Class propertyType) {
        ArrayList instancesList = new ArrayList(edClasses.length);

        for (int i = 0; i < edClasses.length; i++) {
            Class edType = edClasses[i];
            if (RADConnectionPropertyEditor.class.isAssignableFrom(edType)) {
                instancesList.add(new RADConnectionPropertyEditor(propertyType));
            }
            else if (ComponentChooserEditor.class.isAssignableFrom(edType)) {
                instancesList.add(new ComponentChooserEditor(
                                        new Class[] { propertyType }));
            }
            else if (PropertyEditor.class.isAssignableFrom(edType)) {
                try {
                    instancesList.add(edType.newInstance());
                }
                catch (Exception e) { // ignore
                    org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
                }
            }
            // ignore classes which do not implement java.beans.PropertyEditor
        }

        PropertyEditor[] editors = new PropertyEditor[instancesList.size()];
        instancesList.toArray(editors);
        return editors;
    }
}
