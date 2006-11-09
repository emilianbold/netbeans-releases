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

package org.netbeans.modules.j2ee.common.source;

import com.sun.source.tree.*;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

/**
 *
 * @author Andrei Badea
 */
public final class GenerationUtils extends SourceUtils {

    /**
     * The templates for regular Java class and interface.
     */
    static final String CLASS_TEMPLATE = "Templates/Classes/Class.java"; // NOI18N
    static final String INTERFACE_TEMPLATE = "Templates/Classes/Interface.java"; // NOI18N

    // <editor-fold desc="Constructors and factory methods">

    private GenerationUtils(WorkingCopy copy, ClassTree classTree) {
        super(copy, classTree);
    }

    public static GenerationUtils newInstance(WorkingCopy copy, ClassTree classTree) {
        Parameters.notNull("copy", copy); // NOI18N
        Parameters.notNull("classTree", classTree); // NOI18N

        return new GenerationUtils(copy, classTree);
    }

    public static GenerationUtils newInstance(WorkingCopy copy) throws IOException {
        Parameters.notNull("copy", copy); // NOI18N

        ClassTree classTree = findPublicTopLevelClass(copy);
        if (classTree != null) {
            return newInstance(copy, classTree);
        }
        return null;
    }

    // </editor-fold>

    // <editor-fold desc="Public static methods">

    /**
     * Creates a new Java class based on the default template for classes.
     *
     * @param  targetFolder the folder the new class should be created in;
     *         cannot be null.
     * @param  targetName the name of the new interface (a valid Java identifier);
     *         cannot be null.
     * @return the FileObject for the new Java class; never null.
     */
    public static FileObject createClass(FileObject targetFolder, String className, final String javadoc) throws IOException{
        Parameters.notNull("targetFolder", targetFolder); // NOI18N
        Parameters.javaIdentifier("className", className); // NOI18N

        FileObject classFO = createDataObjectFromTemplate(CLASS_TEMPLATE, targetFolder, className).getPrimaryFile();
        JavaSource javaSource = JavaSource.forFileObject(classFO);

        final boolean[] commit = { false };
        ModificationResult modification = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                commit[0] = genUtils.ensureDefaultConstructor();
                // if (javadoc != null) {
                //     genUtils.setJavadoc(copy, mainType, javadoc);
                // }
            }
        });
        if (commit[0]) {
            modification.commit();
        }

        return classFO;
    }

    /**
     * Creates a new Java class based on the default template for interfaces.
     *
     * @param  targetFolder the folder the new interface should be created in;
     *         cannot be null.
     * @param  targetName the name of the new interface (a valid Java identifier);
     *         cannot be null.
     * @return the FileObject for the new Java interface; never null.
     */
    public static FileObject createInterface(FileObject targetFolder, String interfaceName, final String javadoc) throws IOException{
        Parameters.notNull("targetFolder", targetFolder); // NOI18N
        Parameters.javaIdentifier("interfaceName", interfaceName); // NOI18N

        FileObject classFO = createDataObjectFromTemplate(INTERFACE_TEMPLATE, targetFolder, interfaceName).getPrimaryFile();

        // JavaSource javaSource = JavaSource.forFileObject(classFO);
        // final boolean[] commit = { false };
        // ModificationResult modification = javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
        //     public void run(WorkingCopy copy) throws IOException {
        //         GenerationUtils genUtils = GenerationUtils.newInstance(copy);
        //         if (javadoc != null) {
        //             genUtils.setJavadoc(copy, mainType, javadoc);
        //         }
        //     }
        // });
        // if (commit[0]) {
        //     modification.commit();
        // }

        return classFO;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-public static methods">

    /**
     * Creates a data object from a given template path in the system
     * file system.
     *
     * @return the <code>DataObject</code> of the newly created file.
     * @throws IOException if an error occured while creating the file.
     */
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

    // </editor-fold>

    // <editor-fold desc="Public methods">

    /**
     * Creates a new annotation.
     *
     * @param  annotationType the fully-qualified name of the annotation type;
     *         cannot be null.
     * @return the new annotation; never null.
     */
    public AnnotationTree createAnnotation(String annotationType) {
        Parameters.notNull("annotationType", annotationType); // NOI18N

        return createAnnotation(annotationType, null);
    }

    /**
     * Creates a new annotation.
     *
     * @param  annotationType the fully-qualified name of the annotation type;
     *         cannot be null.
     *         <code>java.lang.SuppressWarnings</code>; cannot be null.
     * @param arguments the arguments of the new annotation; cannot be null.
     * @return the new annotation; never null.
     */
    public AnnotationTree createAnnotation(String annotationType, List<? extends ExpressionTree> arguments) {
        Parameters.notNull("annotationType", annotationType); // NOI18N
        Parameters.notNull("arguments", arguments); // NOI18N

        ExpressionTree annotationTree = createAnnotationQualIdent(annotationType);
        List<? extends ExpressionTree> realArguments = arguments != null ? arguments : Collections.<ExpressionTree>emptyList();
        return getTreeMaker().Annotation(annotationTree, realArguments);
    }

    /**
     * Creates a new annotation argument whose value is a string literal.
     *
     * @param  argumentName the argument name; cannot be null.
     * @param  argumentValue the argument value; cannot be null.
     * @return the new annotation argument; never null.
     */
    public ExpressionTree createAnnotationArgument(String argumentName, String argumentValue) {
        Parameters.javaIdentifier("argumentName", argumentName); // NOI18N
        Parameters.notNull("argumentValue", argumentValue); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Assignment(make.Identifier(argumentName), make.Literal(argumentValue));
    }

    /**
     * Creates a new field.
     *
     * @param  modifier the field modifier.
     * @param  fieldType the fully-qualified name of the field type; cannot be null.
     * @param  fieldName the field name; cannot be null.
     * @return the new field; never null.
     */
    public VariableTree createField(Modifier modifier, String fieldType, String fieldName) {
        Parameters.notNull("fieldType", fieldType); // NOI18N
        Parameters.javaIdentifier("fieldName", fieldName); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Variable(
                make.Modifiers(Collections.singleton(modifier)),
                fieldName,
                createQualIdent(fieldType),
                null);
    }

    /**
     * Creates a new public property getter method.
     *
     * @param  propertyType the fully-qualified name of the property type; cannot be null.
     * @param  propertyName the property name; cannot be null.
     * @return the new method; never null.
     */
    public MethodTree createPropertyGetterMethod(String propertyType, String propertyName) {
        Parameters.notNull("propertyType", propertyType); // NOI18N
        Parameters.javaIdentifier("propertyName", propertyName); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Method(
                make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                createPropertyAccessorName(propertyName, true),
                createQualIdent(propertyType),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                "{ return " + propertyName + "; }", // NOI18N
                null);
    }

    /**
     * Creates a new public property setter method.
     *
     * @param  propertyType the fully-qualified name of the property type; cannot be null.
     * @param  propertyName the property name; cannot be null.
     * @return the new method; never null.
     */
    public MethodTree createPropertySetterMethod(String propertyType, String propertyName) {
        Parameters.notNull("propertyType", propertyType); // NOI18N
        Parameters.javaIdentifier("propertyName", propertyName); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Method(
                make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                createPropertyAccessorName(propertyName, false),
                make.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                Collections.singletonList(createVariable(propertyType, propertyName)),
                Collections.<ExpressionTree>emptyList(),
                "{ this." + propertyName + " = " + propertyName + "; }", // NOI18N
                null);
    }

    @SuppressWarnings("unchecked")
    public ClassTree addAnnotation(AnnotationTree annotationTree, ClassTree classTree) {
        Parameters.notNull("annotationTree", annotationTree); // NOI18N
        Parameters.notNull("classTree", classTree); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Class(
                make.addModifiersAnnotation(classTree.getModifiers(), annotationTree),
                classTree.getSimpleName(),
                classTree.getTypeParameters(),
                classTree.getExtendsClause(),
                (List<ExpressionTree>)classTree.getImplementsClause(),
                classTree.getMembers());
    }

    public MethodTree addAnnotation(AnnotationTree annotationTree, MethodTree methodTree) {
        Parameters.notNull("annotationTree", annotationTree); // NOI18N
        Parameters.notNull("methodTree", methodTree); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Method(
                make.addModifiersAnnotation(methodTree.getModifiers(), annotationTree),
                methodTree.getName(),
                methodTree.getReturnType(),
                methodTree.getTypeParameters(),
                methodTree.getParameters(),
                methodTree.getThrows(),
                methodTree.getBody(),
                (ExpressionTree)methodTree.getDefaultValue());
    }

    public VariableTree addAnnotation(AnnotationTree annotationTree, VariableTree variableTree) {
        Parameters.notNull("annotationTree", annotationTree); // NOI18N
        Parameters.notNull("variableTree", variableTree); // NOI18N

        TreeMaker make = getTreeMaker();
        return make.Variable(
                make.addModifiersAnnotation(variableTree.getModifiers(), annotationTree),
                variableTree.getName(),
                variableTree.getType(),
                variableTree.getInitializer());
    }

    public ClassTree addClassFields(ClassTree classTree, List<? extends VariableTree> fieldTrees) {
        Parameters.notNull("classTree", classTree); // NOI18N
        Parameters.notNull("fieldTrees", fieldTrees); // NOI18N

        int firstNonFieldIndex = 0;
        Iterator<? extends Tree> memberTrees = classTree.getMembers().iterator();
        while (memberTrees.hasNext() && memberTrees.next().getKind() == Tree.Kind.VARIABLE) {
            firstNonFieldIndex++;
        }
        TreeMaker make = getTreeMaker();
        ClassTree newClassTree = getClassTree();
        for (VariableTree fieldTree : fieldTrees) {
            newClassTree = make.insertClassMember(newClassTree, firstNonFieldIndex, fieldTree);
            firstNonFieldIndex++;
        }
        return newClassTree;
    }

    /**
     * Adds the specified interface to the implements clause of
     * {@link #getClassTree()}.
     *
     * @param interfaceType the fully-qualified name of the interface; cannot be null.
     */
    public ClassTree addImplementsClause(ClassTree classTree, String interfaceType) {
        if (getTypeElement().getKind() != ElementKind.CLASS) {
            throw new IllegalStateException("Cannot add an implements clause to the non-class type " + getTypeElement().getQualifiedName()); // NOI18N
        }

        ExpressionTree interfaceTree = createInterfaceQualIdent(interfaceType);
        return getTreeMaker().addClassImplementsClause(classTree, interfaceTree);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Non-public methods">

    /**
     * Ensures the main type element contains a concrete (that is, not synthetic)
     * default constructor.
     *
     * @return true if the working copy was modified, false otherwise
     */
    private boolean ensureDefaultConstructor() throws IOException {
        ExecutableElement constructor = getDefaultConstructor();
        boolean modified = false;
        if (constructor != null) {
            if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                ModifiersTree oldModifiersTree = getWorkingCopy().getTrees().getTree(constructor).getModifiers();
                Set<Modifier> newModifiers = new HashSet<Modifier>();
                newModifiers.add(Modifier.PUBLIC);
                for (Modifier modifier : oldModifiersTree.getFlags()) {
                    if (Modifier.PROTECTED.equals(modifier) || Modifier.PRIVATE.equals(modifier)) {
                        continue;
                    }
                    newModifiers.add(modifier);
                }
                TreeMaker make = getTreeMaker();
                ModifiersTree newModifiersTree = make.Modifiers(newModifiers, oldModifiersTree.getAnnotations());
                getWorkingCopy().rewrite(oldModifiersTree, newModifiersTree);
                modified = true;
            }
        } else {
            TreeMaker make = getTreeMaker();
            ClassTree oldClassTree = getClassTree();
            MethodTree method = make.Method(
                    make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                    "<init>", // NOI18N
                    null,
                    Collections.<TypeParameterTree>emptyList(),
                    Collections.<VariableTree>emptyList(),
                    Collections.<ExpressionTree>emptyList(),
                    "{ }", // NOI18N
                    null
            );
            ClassTree newClassTree = make.addClassMember(oldClassTree, method);
            getWorkingCopy().rewrite(oldClassTree, newClassTree);
            modified = true;
        }
        return modified;
    }

    /**
     * Returns the working copy this instance works with.
     *
     * @return the working copy this instance works with; never null.
     */
    private WorkingCopy getWorkingCopy() {
        return (WorkingCopy)getCompilationController();
    }

    private TreeMaker getTreeMaker() {
        return getWorkingCopy().getTreeMaker();
    }

    private VariableTree createVariable(String fieldType, String fieldName) {
        TreeMaker make = getTreeMaker();
        return make.Variable(
                make.Modifiers(Collections.<Modifier>emptySet()),
                fieldName,
                createQualIdent(fieldType),
                null);
    }

    private ExpressionTree createQualIdent(String typeName) {
        TypeElement typeElement = getWorkingCopy().getElements().getTypeElement(typeName);
        if (typeElement == null) {
            throw new IllegalArgumentException("Type " + typeName + " cannot be found"); // NOI18N
        }
        return getTreeMaker().QualIdent(typeElement);
    }

    private ExpressionTree createAnnotationQualIdent(String annotationName) {
        TypeElement annotationElement = getWorkingCopy().getElements().getTypeElement(annotationName);
        if (annotationElement == null) {
            throw new IllegalArgumentException("Type " + annotationName + " cannot be found"); // NOI18N
        }
        if (annotationElement.getKind() != ElementKind.ANNOTATION_TYPE) {
            throw new IllegalArgumentException("Type " + annotationName + " is not an annotation"); // NOI18N
        }
        return getTreeMaker().QualIdent(annotationElement);
    }

    private ExpressionTree createInterfaceQualIdent(String interfaceName) {
        TypeElement interfaceElement = getWorkingCopy().getElements().getTypeElement(interfaceName);
        if (interfaceElement == null) {
            throw new IllegalArgumentException("Type " + interfaceName + " cannot be found"); // NOI18N
        }
        if (interfaceElement.getKind() != ElementKind.INTERFACE) {
            throw new IllegalArgumentException("Type " + interfaceName + " is not an interface"); // NOI18N
        }
        return getTreeMaker().QualIdent(interfaceElement);
    }

    private String createPropertyAccessorName(String propertyName, boolean getter) {
        assert propertyName.length() > 0;
        StringBuffer pascalCaseName = new StringBuffer(propertyName);
        pascalCaseName.setCharAt(0, Character.toUpperCase(pascalCaseName.charAt(0)));
        return (getter ? "get" : "set") + pascalCaseName; // NOI18N
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Inner classes">

    private static final class Parameters {

        public static void notNull(String name, Object value) {
            if (value == null) {
                throw new NullPointerException("The " + name + " parameter cannot be null"); // NOI18N
            }
        }

        public static void notEmpty(String name, String value) {
            notNull(name, value);
            if (value.length() == 0) {
                throw new IllegalArgumentException("The " + name + " parameter cannot be null"); // NOI18N
            }
        }

        public static void notWhitespace(String name, String value) {
            notNull(name, value);
            notEmpty(name, value.trim());
        }

        public static void javaIdentifier(String name, String value) {
            notNull(name, value);
            if (!Utilities.isJavaIdentifier(value)) {
                throw new IllegalArgumentException("The " + name + " parameter is not a valid Java identifier"); // NOI18N
            }
        }
    }

    // </editor-fold>
}
