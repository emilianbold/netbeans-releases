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

import java.util.Map;
import java.util.HashMap;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tran Duc Trung
 */

public class PersistenceObjectRegistry
{

    private static Map _nameToClassname = new HashMap();
    private static Map _classToPrimaryName = new HashMap();

    private PersistenceObjectRegistry() {
    }

    public static void registerPrimaryName(String classname, String name) {
        _classToPrimaryName.put(classname, name);
        _nameToClassname.put(name, classname);
    }

    public static void registerPrimaryName(Class clazz, String name) {
        _classToPrimaryName.put(clazz.getName(), name);
        _nameToClassname.put(name, clazz.getName());
    }

    public static void registerAlias(String classname, String alias) {
        _nameToClassname.put(alias, classname);
    }

    public static void registerAlias(Class clazz, String alias) {
        _nameToClassname.put(alias, clazz.getName());
    }

    public static Object createInstance(String classname, FileObject form)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        return loadClass(classname, form).newInstance();
    }

    public static Class loadClass(String name, FileObject form)
        throws ClassNotFoundException
    {
        name = Utilities.translate(name);
        String classname =(String) _nameToClassname.get(name);
        if (classname == null)
            classname = name;
        return FormUtils.loadClass(classname, form);
    }

    public static String getPrimaryName(Object instance) {
        return getPrimaryName(instance.getClass());
    }

    public static String getPrimaryName(Class clazz) {
        return getPrimaryName(clazz.getName());
    }

    static String getPrimaryName(String className) {
        String name = (String) _classToPrimaryName.get(className);
        return name != null ? name : className;
    }

    static String getClassName(String primaryName) {
        primaryName = Utilities.translate(primaryName);
        String classname = (String) _nameToClassname.get(primaryName);
        return classname != null ? classname : primaryName;
    }
}
