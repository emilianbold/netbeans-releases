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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.util.Collections;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class ServiceLocatorStrategy {
    private static final String CREATE = ".create()"; //NOI18N
    private final ClassPath classPath;
    private final String cName;
    
    private ServiceLocatorStrategy(ClassPath classPath, String name) {
        this.classPath = classPath;
        cName = name;
    }
    
//    public String genLocalEjbStringLookup(String jndiName, String homeName, JavaClass targetClass, boolean create) {
//        String initString = initString("getLocalHome", jndiName, targetClass,""); //NOI18N
//        return "return " + addCast(create, homeName, initString, CREATE) + ";"; // NOI18N
//    }
//    
//    public String genRemoteEjbStringLookup(String jndiName, String homeCls, JavaClass targetClass, boolean create) {
//        String initString = initString("getRemoteHome", jndiName, targetClass, ","+homeCls+".class"); //NOI18N
//        return "return " + addCast(create, homeCls, initString, CREATE) + ";"; //NOI18N
//    }
//    
//    public String genDestinationLookup(String jndiName, JavaClass targetClass) {
//        return initString("getDestination", jndiName, targetClass, ""); //NOI18N
//    }
//    
//    public String genJMSFactory(String jndiName, JavaClass targetClass) {
//        return initString("getConnectionFactory", jndiName, targetClass, ""); //NOI18N
//    }
//    
//    public String genDataSource(String jndiName, JavaClass targetClass) {
//        return initString("getDataSource", jndiName, targetClass, ""); //NOI18N
//    }
//    
//    public String genMailSession(String jndiName, JavaClass targetClass) {
//        return initString("getSession", jndiName, targetClass, ""); //NOI18N
//    }
    
    public static ServiceLocatorStrategy create(Project project, FileObject srcFile, String serviceLocator) {
        ClassPathProvider cpp = (ClassPathProvider)
        project.getLookup().lookup(ClassPathProvider.class);
        assert cpp != null: "project doesn't have class path provider";
        ClassPath classPath = cpp.findClassPath(srcFile, ClassPath.SOURCE);
        assert classPath != null: "project doesn't have a source classpath";
        ClassPath ccp = cpp.findClassPath(srcFile, ClassPath.COMPILE);
        assert cpp != null: "project doesn't have a compile classpath";
        ClassPath aggregate  =
                ClassPathSupport.createProxyClassPath(new ClassPath[] {classPath, ccp});
        return new ServiceLocatorStrategy(aggregate,serviceLocator);
    }
    
    private ClassPath buildClassPathFromImportedProject(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        assert project != null : "cannot find project for file";
        ClassPathProvider cpp = (ClassPathProvider)
            project.getLookup().lookup(ClassPathProvider.class);
        assert cpp != null: "project doesn't have class path provider";
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
        for (int i = 0; i < groups.length; i++) {
            FileObject root = groups[i].getRootFolder();
            if (root.getChildren().length > 0) {
                ClassPath tcp = cpp.findClassPath(root.getChildren()[0], ClassPath.SOURCE);
                classPath = ClassPathSupport.createProxyClassPath(new ClassPath[]{tcp,classPath});
            }
        }
        return classPath;
    }
    
    private String addCast(boolean cast, String clName, String current, String inv) {
        String newValue = current;
        newValue = "("+clName+") " + current; //NOI18N
        if (cast) {
            newValue = "(" + newValue + ")" + inv; //NOI18N
        }
        return newValue;
    }
    
//    private String initString(String methodName, String jndiName, 
//                              JavaClass ce, String otherParams) {
//        String initString = null;
//        JavaClass serviceLocator = findClass(cp);
//        // at this point we are unable to find the actual class 
//        // now lets just assume that we can create a new instance
//        // user can resolve the compiler errors
//        Method staticCreation = null;
//        if (serviceLocator != null) {
//            staticCreation = getStaticLocator(serviceLocator);
//        }
//        if (staticCreation != null) {
//            initString = cName+"."+staticCreation.getName()+"()."+methodName+ //NOI18N
//                         "(\"java:comp/env/"+jndiName+"\""+otherParams+")"; //NOI18N
//        } else {
//            initString = findOrCreateArtifacts(ce)+"()."+methodName+ //NOI18N
//                         "(\"java:comp/env/"+jndiName+"\""+otherParams+")"; //NOI18N
//        }
//        return initString;
//    }
//    
//    private String findOrCreateArtifacts(JavaClass target) {
//        String methodName = null;
//        Method[] methods = JMIUtils.getMethods(target);
//        for (int i = 0; i < methods.length; i++) {
//            String returnValue = methods[i].getType().getName();
//            if (returnValue.equals(cName) &&
//                methods[i].getParameters().size() == 0) {
//                methodName = methods[i].getName();
//                break;
//            }
//        }
//        if (methodName == null) {
//            Field fe = JMIUtils.createField(target, "serviceLocator", cName); //NOI18N
//            fe.setModifiers(Modifier.PRIVATE);
//            target.getContents().add(fe);
//            
//            Method me = JMIUtils.createMethod(target);
//            me.setType(fe.getType());
//            me.setName("getServiceLocator"); //NOI18N
//            me.setModifiers(Modifier.PRIVATE);
//            String body =
//                "if ("+fe.getName()+" == null) {\n" + //NOI18N
//                fe.getName() + " = new "+cName+"();\n" + //NOI18N
//                "}\n" + //NOI18N
//                "return "+fe.getName()+";\n"; //NOI18N
//            me.setBodyText(body);
//            target.getContents().add(me);
//            methodName = me.getName();
//        }
//        return methodName;
//    }
//    
//    private JavaClass findClass(ClassPath cp) {
//        return JMIUtils.findClass(cName, cp);
//        // TODO: what about .class resolving?
////        FileObject clazz = cp.findResource(cName.replace('.', '/')+".java"); //NOI18N
////        if (clazz == null) {
////            clazz = cp.findResource(cName.replace('.', '/')+".class"); //NOI18N
////            if (clazz != null) {
////                return findClass(buildClassPathFromImportedProject(clazz));
////            }
////        }
////        ClassElement ce = null;
////        if (clazz != null) {
////            ce = ClassElement.forName(cName, clazz);
////        }
////        return ce;
//    }
//    
//    private Method getStaticLocator(JavaClass ce) {
//        Method[] methods = JMIUtils.getMethods(ce);
//        String cName = ce.getName();
//        for (int i = 0; i < methods.length; i++) {
//            if (Modifier.isStatic(methods[i].getModifiers()) &&
//                Modifier.isPublic(methods[i].getModifiers()) &&
//                methods[i].getType().getName().equals(cName)) {
//                return methods[i];
//            }
//        }
//        return null;
//    }
}
