/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.java.j2seproject.ui.customizer.MainClassChooser;
import org.netbeans.modules.javacore.ClassIndex;
import org.netbeans.modules.javacore.JMManager;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.openide.filesystems.FileObject;

/**
 * Miscellaneous utilities for the j2seproject module.
 * @author  Jiri Rechtacek
 */
public class J2SEProjectUtil {
    private J2SEProjectUtil () {}
    
    /**
     * Returns the property value evaluated by J2SEProject's PropertyEvaluator.
     *
     * @param p project
     * @param value of property
     * @return evaluated value of given property or null if the property not set or
     * if the project doesn't provide AntProjectHelper
     */    
    public static Object getEvaluatedProperty(Project p, String value) {
        if (value == null) {
            return null;
        }
        J2SEProject j2seprj = (J2SEProject) p.getLookup().lookup(J2SEProject.class);
        if (j2seprj != null) {
            return j2seprj.evaluator().evaluate(value);
        } else {
            return null;
        }
    }
    
    /** Check if the given file object represents a source with the main method.
     * 
     * @param fo source
     * @return true if the source contains the main method
     */
    public static boolean hasMainMethod(FileObject fo) {
        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue ();
        }
        if (fo == null) {
            // ??? maybe better should be thrown IAE
            return false;
        }
        
        boolean has = false;
        JavaMetamodel.getDefaultRepository ().beginTrans (false);
        
        try {
            Resource res = JavaMetamodel.getManager ().getResource (fo);
            assert res != null : "Resource found for FileObject " + fo;
            has = hasMainMethod (res);
        } finally {
            JavaMetamodel.getDefaultRepository ().endTrans ();
        }
        return has;
    }

    /** Returns list of FQN of classes contains the main method.
     * 
     * @param roots the classpath roots of source to start find
     * @return list of names of classes, e.g, [sample.project1.Hello, sample.project.app.MainApp]
     */
    public static List/*String*/ getMainClasses (FileObject[] roots) {
        List result = new ArrayList ();
        for (int i=0; i<roots.length; i++) {
            getMainClasses(roots[i], result);
        }
        return result;
    }
    
    /** Returns list of FQN of classes contains the main method.
     * 
     * @param root the root of source to start find
     * @param addInto list of names of classes, e.g, [sample.project1.Hello, sample.project.app.MainApp]
     */
    private static void getMainClasses (FileObject root, List/*<String>*/ addInto) {
        JavaMetamodel.getDefaultRepository ().beginTrans (false);
        try {
            JavaModelPackage mofPackage = JavaMetamodel.getManager().getJavaExtent(root);
            ClassIndex index = ClassIndex.getIndex (mofPackage);
            //Resource[] res = index.findResourcesForIdentifier ("main"); // NOI18N
            Collection col = index.findResourcesForIdent ("main"); // NOI18N
            Object[] arr = col.toArray ();

            if (arr == null) {
                // no main classes
                return;
            }

            for (int i = 0; i < arr.length; i++) {
                Resource res = (Resource)arr[i];
                if (hasMainMethod (res)) {
                    // has main class -> add to list its name.
                    FileObject fo = ((JMManager)JMManager.getManager ()).getFileObject (res);
                    assert fo != null : "FileObject found for the resource " + res;
                    if (res.getPackageName ().length () > 0) {
                        addInto.add (res.getPackageName () + '.' + fo.getName ());
                    } else {
                        addInto.add (fo.getName ());
                    }
                }
            }
        } finally {
            JavaMetamodel.getDefaultRepository ().endTrans (false);
        }        
    }
    
    /** Returns if the given class name exists under the sources root and
     * it's a main class.
     * 
     * @param className FQN of class
     * @param roots roots of sources
     * @return true if the class name exists and it's a main class
     */
    public static boolean isMainClass(String className, FileObject[] roots) {
        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue ();
        }
        //XXX, should use the classpath created from roots
        
        JavaMetamodel.getDefaultRepository ().beginTrans (false);
        boolean isMain = false;
        
        try {
            JavaClass clazz = ClassIndex.getClassByFqn (className, JavaMetamodel.getManager ().getClassPath ());
            if (clazz != null) {
                isMain = hasMainMethod (clazz.getResource ());
            }
        } finally {
            JavaMetamodel.getDefaultRepository ().endTrans ();
        }
        return isMain;
    }

    // copied from JavaNode.hasMain
    private static boolean hasMainMethod (Resource res) {
        if (res != null && res.containsIdentifier ("main")) { //NOI18N
            for (Iterator i = res.getClassifiers ().iterator (); i.hasNext (); ) {
                JavaClass clazz = (JavaClass) i.next ();
                // now it is only important top-level class with the same 
                // name as file. Continue if the file name differs
                // from top level class name.
                assert JMManager.getManager () != null : "JavaMetamodelManager manager is provided."; // NOI18N
                assert JMManager.getManager () instanceof JMManager : "JavaMetamodelManager <" +JMManager.getManager ().getClass ()+ "> is instanceof JMManager"; // NOI18N
                JMManager mmmanager = (JMManager)JMManager.getManager ();
                assert mmmanager.getFileObject (res) != null : "FileObject found for the resource " + res;
                if (!clazz.getSimpleName ().equals (mmmanager.getFileObject (res).getName ()))
                    continue;

                for (Iterator j = clazz.getFeatures ().iterator(); j.hasNext ();) {
                    Object o = j.next ();
                    // if it is not a method, continue with next feature
                    if (!(o instanceof Method))
                        continue;

                    Method m = (Method) o;
                    int correctMods = (Modifier.PUBLIC | Modifier.STATIC);
                    // check that method is named 'main' and has set public 
                    // and static modifiers! Method has to also return
                    // void type.
                    if (!"main".equals (m.getName()) || // NOI18N
                       ((m.getModifiers () & correctMods) != correctMods) ||
                       (!"void".equals (m.getType().getName ())))
                       continue;

                    // check parameters - it has to be one of type String[]
                    // or String...
                    if (m.getParameters ().size ()==1) {
                        Parameter par = ((Parameter) m.getParameters ().get (0));
                        String typeName = par.getType ().getName ();
                        if (par.isVarArg () && ("java.lang.String".equals (typeName) || "String".equals (typeName))) { // NOI18N
                            // Main methods written with variable arguments parameter:
                            // public static main(String... args) {
                            // }
                            return true; 
                        } else if (typeName.equals ("String[]") || typeName.equals ("java.lang.String[]")) { // NOI18N
                            // Main method written with array parameter:
                            // public static main(String[] args) {
                            // }
                            return true;
                        }

                    } // end if parameters
                } // end features cycle
            }
        }
        return false;
    }
}
