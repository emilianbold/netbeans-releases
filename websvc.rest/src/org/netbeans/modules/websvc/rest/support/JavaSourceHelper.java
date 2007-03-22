/*
 * JavaSourceHelper.java
 *
 * Created on March 19, 2007, 11:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.rest.support;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
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
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author PeterLiu
 */
public class JavaSourceHelper {
    
    static final String CLASS_TEMPLATE = "Templates/Classes/Class.java"; // NOI18N
    static final String INTERFACE_TEMPLATE = "Templates/Classes/Interface.java"; // NOI18N
    
    public static List<JavaSource> getJavaSources(Project project) {
        List<JavaSource> result = new ArrayList<JavaSource>();
        SourceGroup[] groups = SourceGroupSupport.getJavaSourceGroups(project);
        
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
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller)
                        throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    
                    TypeElement classElement = getTopLevelClassElement(controller);
                    
                    if (classElement == null) {
                        System.out.println("Cannot resolve class!");
                    } else {
                        System.out.println("Resolved class: " + classElement.getQualifiedName().toString());
                        List<? extends AnnotationMirror> annotations =
                                controller.getElements().getAllAnnotationMirrors(classElement);
                        
                        System.out.println("annotations: " + annotations);
                        
                        for (AnnotationMirror annotation : annotations) {
                            System.out.println("annotation = " + annotation.toString());
                            if (annotation.toString().equals("@javax.persistence.Entity")) {    //NOI18N
                                isBoolean[0] = true;
                                
                                break;
                            }
                        }
                    }
                }
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
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    ClassTree tree = getTopLevelClassTree(controller);
                    className[0] = tree.getSimpleName().toString();
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return className[0];
    }
    
    public static String getPackageName(JavaSource source) {
        final String[] packageName = new String[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    ExpressionTree packageTree = controller.getCompilationUnit().getPackageName();
                    
                    packageName[0] = packageTree.toString();
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return packageName[0];
    }
    
    public static String getIdFieldName(JavaSource source) {
        final String[] fieldName = new String[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    TypeElement classElement = getTopLevelClassElement(controller);
                    List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());
                    
                    for (VariableElement field : fields) {
                        List<? extends AnnotationMirror> annotations = field.getAnnotationMirrors();
                        
                        for (AnnotationMirror annotation : annotations) {
                            if (annotation.toString().equals("@javax.persistence.Id")) {     //NOI18N
                                fieldName[0] = field.getSimpleName().toString();
                                return;
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return fieldName[0];
    }
    
    public static ClassTree getTopLevelClassTree(CompilationController controller) {
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
    
    public static TypeElement getTopLevelClassElement(CompilationController controller) {
        ClassTree classTree = getTopLevelClassTree(controller);
        Trees trees = controller.getTrees();
        TreePath path = trees.getPath(controller.getCompilationUnit(), classTree);
        
        return (TypeElement) trees.getElement(path);
    }
    
    public static JavaSource createJavaSource(FileObject targetFolder,
            String className) {
        try {
            FileObject fobj = createDataObjectFromTemplate(CLASS_TEMPLATE, targetFolder, className).getPrimaryFile();
            return JavaSource.forFileObject(fobj);
        } catch (IOException ex) {
            
        }
        
        return null;
    }
    
    private static DataObject createDataObjectFromTemplate(String template, FileObject targetFolder, String targetName) throws IOException {
        assert template != null;
        assert targetFolder != null;
        assert targetName != null && targetName.trim().length() >  0;
        
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject templateFO = defaultFS.findResource(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        return templateDO.createFromTemplate(dataFolder, targetName);
    }
    
    public static void addAnnotation(WorkingCopy copy, Tree tree, String annotation,
            List<Object> arguments) {
        TreeMaker maker = copy.getTreeMaker();
        List<ExpressionTree> argumentTrees = new ArrayList<ExpressionTree>();
        
        for (Object argument : arguments) {
            argumentTrees.add(maker.Literal(argument));
        }
        
        AnnotationTree newAnnotation = maker.Annotation(
                maker.Identifier(annotation),
                argumentTrees);
    
        ModifiersTree modifiers = null;
        
        if (tree.getKind() == Tree.Kind.CLASS) {
            modifiers = ((ClassTree) tree).getModifiers();
        }
        
        if (modifiers != null) {
            ModifiersTree newModifiers = maker.addModifiersAnnotation(
                    modifiers, newAnnotation);
            copy.rewrite(modifiers, newModifiers);
        }
    }
    
    public static void addImports(WorkingCopy copy, String[] imports) {
        TreeMaker maker = copy.getTreeMaker();
        
        CompilationUnitTree tree = copy.getCompilationUnit();
        CompilationUnitTree modifiedTree = tree;
        
        for (String imp : imports) {
            modifiedTree = maker.addCompUnitImport(modifiedTree, 
                    maker.Import(maker.Identifier(imp), false));
        }
        
        copy.rewrite(tree, modifiedTree);
    }
}
