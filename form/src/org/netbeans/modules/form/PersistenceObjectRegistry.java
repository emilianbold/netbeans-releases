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

import java.util.Map;
import java.util.HashMap;
import org.openide.TopManager;
import org.openide.util.Utilities;

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

  public static Object createInstance(String name)
    throws InstantiationException, IllegalAccessException, ClassNotFoundException
  {
    return loadClass(name).newInstance();
  }

  public static Class loadClass(String name) throws ClassNotFoundException {
    name = Utilities.translate(name);
    String classname = (String) _nameToClassname.get(name);
    if (classname == null)
      classname = name;
    return TopManager.getDefault().currentClassLoader().loadClass(classname);
  }

  public static String getPrimaryName(Object instance) {
    return getPrimaryName(instance.getClass());
  }

  public static String getPrimaryName(Class clazz) {
    String classname = clazz.getName();

    String name = (String) _classToPrimaryName.get(classname);
    return name != null ? name : classname;
  }
  
  static {
//      registerPrimaryName("com.netbeans.developerx.loaders.form.formeditor.layouts.DesignBorderLayout$ConstraintsDesc",
//                          "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignBorderLayout$BorderConstraintsDescription");
  }
}
