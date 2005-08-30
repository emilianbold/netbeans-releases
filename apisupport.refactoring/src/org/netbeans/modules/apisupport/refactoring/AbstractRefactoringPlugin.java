/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author mkleint
 */
public abstract class AbstractRefactoringPlugin implements RefactoringPlugin {
    protected static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.apisupport.refactoring");   // NOI18N
    
    protected AbstractRefactoring refactoring;
    // a regexp pattern for ordering attributes
    protected Pattern orderingLayerAttrPattern = Pattern.compile("([\\S]+)/([\\S]+)"); //NOI18N
    /** Creates a new instance of AbstractRefactoringPlugin */
    public AbstractRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    /** Checks pre-conditions of the refactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem preCheck() {
        return null;
    }
    
    /** Checks parameters of the refactoring.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem checkParameters() {
        return null;
    }
    
    /**
     * returns the line number in the file if found, otherwise -1
     */
    protected final int checkContentOfFile(FileObject fo, String classToLookFor) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(fo.getInputStream(), "UTF-8")); // NOI18N
            String line = reader.readLine();
            int counter = 0;
            while (line != null) {
                if (line.indexOf(classToLookFor) != -1) {
                    return counter;
                }
                counter = counter + 1;
                line = reader.readLine();
            }
        } catch (IOException exc) {
            //TODO
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException x) {
                    // ignore
                }
            }
        }
        return -1;
    }
    
    protected final void checkManifest(NbModuleProject project, JavaClass clzz, RefactoringElementsBag refactoringElements) {
        String name = clzz.getName();
        String pathName = name.replace('.', '/') + ".class"; //NOI18N
        Manifest mf = project.getManifest();
        Attributes attrs = mf.getMainAttributes();
        Iterator it = attrs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String val = (String)entry.getValue();
            if (val.indexOf(name) != -1 || val.indexOf(pathName) != -1) {
                RefactoringElementImplementation elem =
                        createManifestRefactoring(clzz, project.getManifestFile(), ((Attributes.Name)entry.getKey()).toString(), val, null);
                if (elem != null) {
                    refactoringElements.add(refactoring, elem);
                }
            }
        }
        Map entries = mf.getEntries();
        if (entries != null) {
            it = entries.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry secEnt = (Map.Entry)it.next();
                attrs = (Attributes)secEnt.getValue();
                String val = (String)secEnt.getKey();
                if (val.indexOf(name) != -1 || val.indexOf(pathName) != -1) {
                    String section = attrs.getValue("OpenIDE-Module-Class"); //NOI18N
                    RefactoringElementImplementation elem =
                            createManifestRefactoring(clzz, project.getManifestFile(), null, val, section);
                    if (elem != null) {
                        refactoringElements.add(refactoring, elem);
                    }
                }
            }
        }
    }
    
    protected final void checkMetaInfServices(Project project, JavaClass clzz, RefactoringElementsBag refactoringElements) {
        FileObject services = Utility.findMetaInfServices(project);
        if (services == null) {
            return;
        }
        String name = clzz.getName();
        // Easiest to check them all; otherwise would need to find all interfaces and superclasses:
        FileObject[] files = services.getChildren();
        for (int i = 0; i < files.length; i++) {
            int line = checkContentOfFile(files[i], name);
            if (line != -1) {
                RefactoringElementImplementation elem =
                        createMetaInfServicesRefactoring(clzz, files[i]);
                if (elem != null) {
                    refactoringElements.add(refactoring, elem);
                }
            }
        }
    }
    
    protected final void checkLayer(NbModuleProject project, JavaClass clzz, RefactoringElementsBag refactoringElements) {
        LayerUtils.LayerHandle handle = LayerUtils.layerForProject(project);
        FileSystem fs = handle.layer(false);
        if (fs != null) {
            checkFileObject(fs.getRoot(), clzz, refactoringElements, handle);
        }
    }
    
    
    private void checkFileObject(FileObject fo, JavaClass clzz, RefactoringElementsBag refactoringElements, LayerUtils.LayerHandle handle) {
        if (fo.isFolder()) {
            FileObject[] childs = fo.getChildren();
            for (int i =0; i < childs.length; i++) {
                checkFileObject(childs[i], clzz, refactoringElements, handle);
            }
            Enumeration en = fo.getAttributes();
            // check ordering attributes?
            while (en.hasMoreElements()) {
                String attrKey = (String)en.nextElement();
                Matcher match = orderingLayerAttrPattern.matcher(attrKey);
                if (match.matches()) {
                    String first = match.group(1);
                    if (first.endsWith(".instance")) { //NOI18N
                        String name = first.substring(0, first.length() - ".instance".length()).replace('-', '.'); //NOI18N
                        if (name.equals(clzz.getName())) {
                            RefactoringElementImplementation elem = createLayerRefactoring(clzz, handle, fo, attrKey);
                            if (elem != null) {
                                refactoringElements.add(refactoring, elem);
                            }
                        }
                    }
                    String second = match.group(2);
                    if (second.endsWith(".instance")) { //NOI18N
                        String name = second.substring(0, second.length() - ".instance".length()).replace('-', '.'); //NOI18N
                        if (name.equals(clzz.getName())) {
                            RefactoringElementImplementation elem = createLayerRefactoring(clzz, handle, fo, attrKey);
                            if (elem != null) {
                                refactoringElements.add(refactoring, elem);
                            }
                        }
                    }
                }
            }
        } else if (fo.isData()) {
            if ("instance".equals(fo.getExt())) { // NOI18N
                String name = fo.getName().replace('-', '.');
                if (name.equals(clzz.getName())) {
                    RefactoringElementImplementation elem = createLayerRefactoring(clzz, handle, fo, null);
                    if (elem != null) {
                        refactoringElements.add(refactoring, elem);
                    }
                }
            }
            if ("settings".equals(fo.getExt())) { // NOI18N
                //TODO check also content of settings files for matches?
            }
            
            Enumeration en = fo.getAttributes();
            // check just a few specific attributes or iterate all?
            while (en.hasMoreElements()) {
                String attrKey = (String)en.nextElement();
                Object val = fo.getAttribute("literal:" + attrKey); //NOI18N
                if (val instanceof String) {
                    String attrValue = (String)val;
                    boolean check = false;
                    String value = attrValue;
                    if (attrValue.startsWith("new:")) { //NOI18N
                        value = attrValue.substring("new:".length()); //NOI18N
                    }
                    if (attrValue.startsWith("method:")) { //NOI18N
                        value = attrValue.substring("method:".length()); //NOI18N
                        int index = value.lastIndexOf('.');
                        if (index > 0) {
                            value = value.substring(0, index);
                        }
                    }
                    String pattern1 = clzz.getName().replaceAll("\\.", "\\."); //NOI18N
                    String pattern2 = "[a-zA-Z0-9/-]*" + clzz.getName().replaceAll("\\.", "-") + "\\.instance"; //NOI18N

                    if (value.matches(pattern1) || value.matches(pattern2)) {
                        RefactoringElementImplementation elem = createLayerRefactoring(clzz, handle, fo, attrKey);
                        if (elem != null) {
                            refactoringElements.add(refactoring, elem);
                        }
                    }
                }
            }
        }
        
    }
    
    protected final Problem checkLayer(Method method, RefactoringElementsBag refactoringElements) {
        Problem problem = null;
        // do our check just on public static methods..
        if (! Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
            return problem;
        }
        // with no parameters or with parameter of type FileObject
        List params = method.getParameters();
        if (params.size() > 1) {
            return problem;
        }
        Iterator it = params.iterator();
        while (it.hasNext()) {
            Parameter param =(Parameter)it.next();
            if (! "org.openide.filesystems.FileObject".equals(param.getType().getName())) { // NOI18N
                return problem;
            }
        }
        Resource res = method.getResource();
        FileObject fo = JavaModel.getFileObject(res);
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null && project instanceof NbModuleProject) {
            LayerUtils.LayerHandle handle = LayerUtils.layerForProject((NbModuleProject)project);
            FileSystem fs = handle.layer(false);
            if (fs != null) {
                checkFileObject(fs.getRoot(), method, null, refactoringElements, handle);
            }
        }
        return problem;
    }
    
    protected final Problem checkLayer(Constructor constructor, RefactoringElementsBag refactoringElements) {
        Problem problem = null;
        // just consider public constructors with no params..
        if (!Modifier.isPublic(constructor.getModifiers())) {
            return problem;
        }
        List params = constructor.getParameters();
        if (params.size() > 0) {
            return problem;
        }
        Resource res = constructor.getResource();
        FileObject fo = JavaModel.getFileObject(res);
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null && project instanceof NbModuleProject) {
            LayerUtils.LayerHandle handle = LayerUtils.layerForProject((NbModuleProject)project);
            FileSystem fs = handle.layer(false);
            if (fs != null) {
                checkFileObject(fs.getRoot(), null, constructor, refactoringElements, handle);
            }
        }
        return problem;
    }
    
    private void checkFileObject(FileObject fo, Method method, Constructor constructor,
            RefactoringElementsBag refactoringElements, LayerUtils.LayerHandle handle) {
        if (fo.isFolder()) {
            FileObject[] childs = fo.getChildren();
            for (int i =0; i < childs.length; i++) {
                checkFileObject(childs[i], method, constructor, refactoringElements, handle);
            }
        } else if (fo.isData()) {
            if ("settings".equals(fo.getExt())) { // NOI18N
                //TODO check also content of settings files for matches?
            }
            Enumeration en = fo.getAttributes();
            // check just a few specific attributes or iterate all?
            while (en.hasMoreElements()) {
                String attrKey = (String)en.nextElement();
                Object val = fo.getAttribute("literal:" + attrKey); //NOI18N
                if (val instanceof String) {
                    String attrValue = (String)val;
                    if (method != null && attrValue.startsWith("method:") && attrValue.endsWith(method.getName())) { //NOI18N
                        String clazz = attrValue.substring("method:".length()); //NOI18N
                        String methodString = null;
                        int index = clazz.lastIndexOf('.');
                        if (index > 0) {
                            methodString = clazz.substring(index + 1);
                            clazz = clazz.substring(0, index);
                        }
                        if (methodString != null && methodString.equals(method.getName()) &&
                                clazz.equals(method.getDeclaringClass().getName())) {
                            RefactoringElementImplementation elem = createLayerRefactoring(method, handle, fo, attrKey);
                            if (elem != null) {
                                refactoringElements.add(refactoring, elem);
                            }
                        }
                    }
                    if (constructor != null && attrValue.startsWith("new:")) { //NOI18N
                        String clazz = attrValue.substring("new:".length()); //NOI18N
                        if (clazz.equals(constructor.getDeclaringClass().getName())) {
                            RefactoringElementImplementation elem = createLayerRefactoring(constructor, handle, fo, attrKey);
                            if (elem != null) {
                                refactoringElements.add(refactoring, elem);
                            }
                        }
                    }
                }
            }
        }
        
    }
    
    protected abstract RefactoringElementImplementation createMetaInfServicesRefactoring(JavaClass clazz,
            FileObject serviceFile);
    
    protected abstract RefactoringElementImplementation createManifestRefactoring(JavaClass clazz,
            FileObject manifestFile,
            String attributeKey,
            String attributeValue,
            String section);
    
    protected RefactoringElementImplementation createLayerRefactoring(JavaClass clazz,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        throw new AssertionError("if you call checkLayer(), you need to implement this method");
    }
    
    protected RefactoringElementImplementation createLayerRefactoring(Method method,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        throw new AssertionError("if you call checkLayer(), you need to implement this method");
    }
    
    protected RefactoringElementImplementation createLayerRefactoring(Constructor constructor,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        throw new AssertionError("if you call checkLayer(), you need to implement this method");
    }
    
}
