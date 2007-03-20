/*
 * JavaSourceHelper.java
 *
 * Created on March 19, 2007, 11:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.rest.support;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;

/**
 *
 * @author PeterLiu
 */
public class JavaSourceHelper {
    
    public static List<JavaSource> getJavaSources(Project project) {
        List<JavaSource> result = new ArrayList<JavaSource>();
        
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        for (SourceGroup group : groups) {
            System.out.println("group = " + group);
            FileObject root = group.getRootFolder();
            Enumeration<? extends FileObject> files = root.getData(true);
            
            while(files.hasMoreElements()) {
                FileObject fobj = files.nextElement();
                System.out.println("file = " + fobj);
                
                if (fobj.getExt().equals("java")) {
                    JavaSource source = JavaSource.forFileObject(fobj);
                    System.out.println("source = " + source);
                    result.add(source);
                }
            }
        }
        
        return result;
    }
    
    public static List<JavaSource> getEntityClasses(Project project) {
        List<JavaSource> sources = getJavaSources(project);
        List<JavaSource> entityClasses = new ArrayList<JavaSource>();
        
        for (JavaSource source : sources) {
            if (isEntity(source)) {
                entityClasses.add(source);
            }
        }
        
        return entityClasses;
    }
    
    public static boolean isEntity(JavaSource source) {
        final boolean[] isBoolean = new boolean[1];
        
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                public void run(CompilationController controller)
                        throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    
                    ClassTree classTree = findPublicTopLevelClass(controller);
                    Trees trees = controller.getTrees();
                    TreePath path = trees.getPath(controller.getCompilationUnit(), classTree);
                    TypeElement classElement = (TypeElement) trees.getElement(path);
                    
                    if (classElement == null) {
                        System.out.println("Cannot resolve class!");
                    } else {
                        System.out.println("Resolved class: " + classElement.getQualifiedName().toString());
                        List<? extends AnnotationMirror> annotations =
                                controller.getElements().getAllAnnotationMirrors(classElement);
                        
                        System.out.println("annotations: " + annotations);
                        
                        for (AnnotationMirror annotation : annotations) {
                            System.out.println("annotation = " + annotation.toString());
                            if (annotation.toString().equals("@javax.persistence.Entity")) {
                                isBoolean[0] = true;
                                
                                break;
                            }
                        }
                    }
                }
                
                public void cancel() {}
            }, true);
        } catch (IOException ex) {
            
        }
        
        return isBoolean[0];
    }
    
    /**
     *
     * @param source
     * @return
     */
    public static String getClassName(JavaSource source) {
        final String[] className = new String[1];
        
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    className[0] = controller.getFileObject().getName();
                }
                
                public void cancel() {}
            }, true);
        } catch (IOException ex) {
            
        }
        
        return className[0];
    }
    
    public static String getPackageName(JavaSource source) {
        final String[] packageName = new String[1];
        
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    ExpressionTree packageTree = controller.getCompilationUnit().getPackageName();
                   
                    packageName[0] = packageTree.toString();
                }
                
                public void cancel() {}
            }, true);
        } catch (IOException ex) {
            
        }
        
        return packageName[0];
    }
    
    private static ClassTree findPublicTopLevelClass(CompilationController controller) {
        String className = controller.getFileObject().getName();
        
        List<? extends Tree> decls = controller.getCompilationUnit().getTypeDecls();
        
        for (Tree decl : decls) {
            if (decl.getKind() != Tree.Kind.CLASS) {
                continue;
            }
            
            ClassTree classTree = (ClassTree) decl;
            
            if (classTree.getSimpleName().contentEquals(className) &&
                    classTree.getModifiers().getFlags().contains(Modifier.PUBLIC))
                return classTree;
        }
        
        return null;
    }
}
