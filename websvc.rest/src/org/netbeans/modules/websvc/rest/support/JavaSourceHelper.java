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
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
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
    static final String JAVA_EXT = "java";                  //NOI18N
    
    public static List<JavaSource> getJavaSources(Project project) {
        List<JavaSource> result = new ArrayList<JavaSource>();
        SourceGroup[] groups = SourceGroupSupport.getJavaSourceGroups(project);
        
        for (SourceGroup group : groups) {
            FileObject root = group.getRootFolder();
            Enumeration<? extends FileObject> files = root.getData(true);
            
            while(files.hasMoreElements()) {
                FileObject fobj = files.nextElement();
                
                if (fobj.getExt().equals(JAVA_EXT)) {
                    JavaSource source = JavaSource.forFileObject(fobj);
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
        
        System.out.println("isEntity called on source = " + source);
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller)
                        throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    
                    TypeElement classElement = getTopLevelClassElement(controller);
                    
                    if (classElement == null) {
                        System.out.println("Cannot resolve class!");
                    } else {
                        List<? extends AnnotationMirror> annotations =
                                controller.getElements().getAllAnnotationMirrors(classElement);
                        
                        for (AnnotationMirror annotation : annotations) {
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
    
    public static String getClassType(JavaSource source) {
        final String[] className = new String[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    ClassTree tree = getTopLevelClassTree(controller);
                    className[0] = controller.getCompilationUnit().getPackageName().toString() +
                            "." + tree.getSimpleName().toString();
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
            String packageName, String className) {
        try {
            FileObject fobj = createDataObjectFromTemplate(CLASS_TEMPLATE,
                    targetFolder, packageName, className).getPrimaryFile();
            return JavaSource.forFileObject(fobj);
        } catch (IOException ex) {
            
        }
        
        return null;
    }
    
    private static DataObject createDataObjectFromTemplate(String template,
            FileObject targetFolder, String packageName, String targetName) throws IOException {
        assert template != null;
        assert targetFolder != null;
        assert targetName != null && targetName.trim().length() >  0;
        
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject templateFO = defaultFS.findResource(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("package", packageName);
        
        return templateDO.createFromTemplate(dataFolder, targetName, params);
    }
    
    public static void addClassAnnotation(WorkingCopy copy, String[] annotations,
            Object[] annotationAttrs) {
        TreeMaker maker = copy.getTreeMaker();
        ClassTree tree = getTopLevelClassTree(copy);
        
        ModifiersTree modifiers = tree.getModifiers();
        
        for (int i = 0; i < annotations.length; i++) {
            List<ExpressionTree> attrTrees = null;
            Object attr = annotationAttrs[i];
            
            if (attr != null) {
                attrTrees = new ArrayList<ExpressionTree>();
                
                if (attr instanceof ExpressionTree) {
                    attrTrees.add((ExpressionTree) attr);
                } else {
                    attrTrees.add(maker.Literal(attr));
                }
            } else {
                attrTrees = Collections.<ExpressionTree>emptyList();
            }
    
            AnnotationTree newAnnotation = maker.Annotation(
                    maker.Identifier(annotations[i]),
                    attrTrees);
            
            if (modifiers != null) {
                modifiers = maker.addModifiersAnnotation(
                        modifiers, newAnnotation);
            }
        }
        
        copy.rewrite(tree.getModifiers(), modifiers);
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
    
    public static ClassTree addField(WorkingCopy copy, ClassTree tree,
            Modifier[] modifiers, String[] annotations, Object[] annotationAttrs,
            String name, Object type) {
        
        TreeMaker maker = copy.getTreeMaker();
        ClassTree modifiedTree = tree;
        
        Tree typeTree = createTypeTree(copy, type);
        
        ModifiersTree modifiersTree = createModifiersTree(copy, modifiers,
                annotations, annotationAttrs);
        
        VariableTree variableTree = maker.Variable(modifiersTree, name,
                typeTree, null);
        
        return maker.insertClassMember(modifiedTree, 0, variableTree);
    }
    
    public static ClassTree addConstructor(WorkingCopy copy, ClassTree tree,
            Modifier[] modifiers, String[] parameters,
            Object[] paramTypes, String bodyText) {
        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree modifiersTree = createModifiersTree(copy, modifiers, null, null);
        ModifiersTree paramModTree = maker.Modifiers(Collections.<Modifier>emptySet());
        List<VariableTree> paramTrees = new ArrayList<VariableTree>();
        
        for (int i = 0; i < parameters.length; i++) {
            paramTrees.add(maker.Variable(paramModTree,
                    parameters[i], createTypeTree(copy, paramTypes[i]), null));
        }
        
        MethodTree methodTree = maker.Constructor(modifiersTree,
                Collections.<TypeParameterTree>emptyList(),
                paramTrees,
                Collections.<ExpressionTree>emptyList(),
                bodyText);
        
        return maker.addClassMember(tree, methodTree);
    }
    
    public static ClassTree addMethod(WorkingCopy copy, ClassTree tree,
            Modifier[] modifiers, String[] annotations, Object[] annotationAttrs,
            String name, Object returnType,
            String[] parameters, Object[] paramTypes,
            String bodyText) {
        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree modifiersTree = createModifiersTree(copy, modifiers,
                annotations, annotationAttrs);
        
        Tree returnTypeTree = createTypeTree(copy, returnType);
        
        ModifiersTree paramModTree = maker.Modifiers(
                Collections.<Modifier>emptySet());
        List<VariableTree> paramTrees = new ArrayList<VariableTree>();
        
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                paramTrees.add(maker.Variable(paramModTree,
                        parameters[i], createTypeTree(copy, paramTypes[i]), null));
            }
        }
        
        MethodTree methodTree = maker.Method(modifiersTree,
                name, returnTypeTree,
                Collections.<TypeParameterTree>emptyList(),
                paramTrees,
                Collections.<ExpressionTree>emptyList(),
                bodyText,
                null);
        
        return maker.addClassMember(tree, methodTree);
    }
    
    public static AssignmentTree createAssignmentTree(WorkingCopy copy, String variable,
            Object value) {
        TreeMaker maker = copy.getTreeMaker();
        
        return maker.Assignment(maker.Identifier(variable), maker.Literal(value));
    }
    
    private static Tree createTypeTree(WorkingCopy copy, Object type) {
        if (type instanceof String) {
            TypeElement element = copy.getElements().getTypeElement((String) type);
            if (element != null) {
                return copy.getTreeMaker().QualIdent(element);
            } else {
                return copy.getTreeMaker().Identifier((String) type);
            }
        } else {
            return (Tree) type;
        }
    }
   
    public static Tree createIdentifierTree(WorkingCopy copy, String value) {
        return copy.getTreeMaker().Identifier(value);
    }
    
    public static Tree createParameterizedTypeTree(WorkingCopy copy,
            String type, String[] typeArgs) {
        TreeMaker maker = copy.getTreeMaker();
        Tree typeTree = createTypeTree(copy, type);
        List<ExpressionTree> typeArgTrees = new ArrayList<ExpressionTree>();
        
        for (String arg : typeArgs) {
            typeArgTrees.add((ExpressionTree)  createTypeTree(copy, arg));
        }
        
        return maker.ParameterizedType(typeTree, typeArgTrees);
    }
    
    private static ModifiersTree createModifiersTree(WorkingCopy copy,
            Modifier[] modifiers, String[] annotations,
            Object[] annotationAttrs) {
        TreeMaker maker = copy.getTreeMaker();
        Set<Modifier> modifierSet = new HashSet<Modifier>();
        
        for (Modifier modifier : modifiers) {
            modifierSet.add(modifier);
        }
        
        List<AnnotationTree> annotationTrees = createAnnotationTrees(copy,
                annotations, annotationAttrs);
        
        return maker.Modifiers(modifierSet, annotationTrees);
    }
    
    private static List<AnnotationTree> createAnnotationTrees(WorkingCopy copy,
            String[] annotations, Object[] annotationAttrs) {
        TreeMaker maker = copy.getTreeMaker();
        List<AnnotationTree> annotationTrees = null;
        
        if (annotations != null) {
            annotationTrees = new ArrayList<AnnotationTree>();
            
            for (int i = 0; i < annotations.length; i++) {
                String annotation = annotations[i];
                List<ExpressionTree> expressionTrees = Collections.<ExpressionTree>emptyList();
                
                if (annotationAttrs != null) {
                    Object attr = annotationAttrs[i];
                    
                    if (attr != null) {
                        expressionTrees = new ArrayList<ExpressionTree>();
                        
                        if (attr instanceof ExpressionTree) {
                            expressionTrees.add((ExpressionTree) attr);
                        } else {
                            expressionTrees.add(maker.Literal(attr));
                        }
                    }
                }
                
                annotationTrees.add(maker.Annotation(maker.Identifier(annotation),
                        expressionTrees));
            }
        } else {
            annotationTrees = Collections.<AnnotationTree>emptyList();
        }
        
        return annotationTrees;
    }
}
