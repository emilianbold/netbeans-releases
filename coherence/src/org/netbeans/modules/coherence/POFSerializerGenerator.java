/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class POFSerializerGenerator implements CodeGenerator {

    private static final Logger logger = Logger.getLogger(POFSerializerGenerator.class.getCanonicalName());
    public final static String NEW_CLASS = "\treturn new {CLASS}(\n";
    private static final String INSTANCE_VAR_NAME = "instance";
    private static final String REMAINDER_VAR_NAME = "_remainder";
    private static final String SERIALIZER_CLASS_NAME = "Serializer";
    private static final String VARIABLE_COMMENT = "Required to store the Remainder portion for the Serialize / Deserialize functionality in case of Upgrades";
    private static final String CONSTRUCTOR_COMMENT = "Auto generated constructor that will be used by the " + SERIALIZER_CLASS_NAME + " to create new instances of {CLASS}.";
    private static final String INNER_CLASS_COMMENT = "POF Serializer which can be used instead of implementing PortableObject.\n"
            + "The following example XML can be used to reference the Serializer within \n"
            + "your <pof-config> although you will need to generate a TYPEID > 1000.\n\n"
            + "<user-type>\n"
            + "\t<type-id>{TYPEID}</type-id>\n"
            + "\t<class-name>{CLASS}</class-name>\n"
            + "\t<serializer>\n"
            + "\t\t<class-name>{CLASS}$Serializer</class-name>\n"
            + "\t</serializer>\n"
            + "</user-type>\n";
    public final static String SERIALIZE_JAVADOC = "Handles POF Write Serialization\n@param writer POF Writer\n@param obj Object to be Serialized\n@throws IOException if an I/O error occurs\n";
    public final static String DESERIALIZE_JAVADOC = "Handles POF Read De-serialization\n@param reader POF Reader\n@throws IOException if an I/O error occurs\n";
    public final static String CONSTANTS_JAVADOC = "Index Constants used to access the variables in the Serialize / Deserialize methods";
    JTextComponent textComp;

    /**
     * 
     * @param context containing JTextComponent and possibly other items registered by {@link CodeGeneratorContextProvider}
     */
    private POFSerializerGenerator(Lookup context) { // Good practice is not to save Lookup outside ctor
        textComp = context.lookup(JTextComponent.class);
    }

    public static class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            return Collections.singletonList(new POFSerializerGenerator(context));
        }
    }

    /**
     * The name which will be inserted inside Insert Code dialog
     */
    @Override
    public String getDisplayName() {
        return "Coherence POF Serializer Inner Class";
    }

    /**
     * This will be invoked when user chooses this Generator from Insert Code
     * dialog
     */
    @Override
    public void invoke() {
        try {
            Document doc = textComp.getDocument();
            JavaSource javaSource = JavaSource.forDocument(doc);

            // First we need to check if the code already exists within the java
            // and if so is it ok to replace
            if (isOkToModify(javaSource)) {
                modifyJava(javaSource);
            }

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }
    // Build Variables
    boolean insertOverwriteCode = true;

    protected boolean isOkToModify(JavaSource javaSource) throws IOException {
        // Create a working Task to check the code
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            @Override
            public void cancel() {
            }

            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        Element el = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), clazz));
                        if (isConstructorPresent(el) || isInnerClassPresent(el) || isRemainderFieldPresent(el)) {
                            int option = JOptionPane.showConfirmDialog(null, "POF Serializer Code already exists. Overwrite ?");
                            insertOverwriteCode = (option == 0);
                        }
                        break;
                    }
                }
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);

        return insertOverwriteCode;
    }

    protected void modifyJava(JavaSource javaSource) throws IOException {
        /*
         * Create Code Removal Task
         * ========================
         *
         * This task will be executed first to remove any existing generated code
         * we will only be executing this code and deleting the existing code if
         * the user has answer "Yes" to the overwrite question.
         *
         * Because this stage is done first the user will have to execute Undo
         * twice to retrieve the previous code.
         */
        CancellableTask removeTask = new CancellableTask<WorkingCopy>() {

            @Override
            public void cancel() {
            }

            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                /*
                 * Loop through the top level Types although we know that their
                 * will only be one this code is added for safety and once we have
                 * processed the top level class we will exist the loop.
                 */
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        ClassTree modifiedClazz = clazz;
                        Element el = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), clazz));

                        /*
                         * Before executing a remove we will check is the corresponding
                         * generated code exists within the java source.
                         */
                        if (isRemainderFieldPresent(el)) {
                            modifiedClazz = removeRemainderField(workingCopy, modifiedClazz);
                        }
                        if (isConstructorPresent(el)) {
                            modifiedClazz = removeConstructor(workingCopy, modifiedClazz);
                        }
                        if (isInnerClassPresent(el)) {
                            modifiedClazz = removeInnerClass(workingCopy, modifiedClazz);
                        }
                        // Write Working Copy
                        workingCopy.rewrite(clazz, modifiedClazz);
                        break;
                    }
                }
            }
        };
        // Commit the Code Changes
        ModificationResult removeResult = javaSource.runModificationTask(removeTask);
        removeResult.commit();

        /*
         * Create a working Task to edit the code
         * ======================================
         *
         * This Task is executed to generate and insert the generated code into
         * the java source. At this point we will be using the Java Source Compiler
         * to do all the heavy lifting required to correctly format and declare
         * required imports etc.
         */
        CancellableTask insertTask = new CancellableTask<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                /*
                 * Loop through the top level Types although we know that their
                 * will only be one this code is added for safety and once we have
                 * processed the top level class we will exist the loop.
                 */
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        ClassTree modifiedClazz = clazz;
                        VariableTree parameter = null;
                        MethodTree newConstructor = null;
                        ClassTree innerClass = null;

                        // Create _remainder variable
                        parameter = createRemainderField(workingCopy);
                        // Modify the working copy
                        modifiedClazz = make.addClassMember(modifiedClazz, parameter);

                        // Create Constructor
                        newConstructor = createConstructor(workingCopy, clazz);
                        // Modify the working copy
                        modifiedClazz = make.addClassMember(modifiedClazz, newConstructor);

                        // Create Inner Serializer Class
                        innerClass = createInnerClass(workingCopy, clazz);
                        // Write Inner Class to Class
                        modifiedClazz = make.addClassMember(modifiedClazz, innerClass);

                        // Write Working Copy
                        workingCopy.rewrite(clazz, modifiedClazz);
                    }
                }
            }

            @Override
            public void cancel() {
            }
        };
        // Commit the Code Changes
        ModificationResult insertResult = javaSource.runModificationTask(insertTask);
        insertResult.commit();

    }

    /*
     * Define Check Methods
     * ====================
     *
     * Methods used to check if the Generation has been previously executed they
     * simply check for what I will generate. It is assumed that is one of the
     * compnents exists I have run the generation previously.
     */
    protected boolean isRemainderFieldPresent(Element classElement) {
        boolean remainderField = false;

        if (classElement != null) {
            TypeElement te = (TypeElement) classElement;
            List enclosedElements = te.getEnclosedElements();
            for (int i = 0; i < enclosedElements.size(); i++) {
                Element enclosedElement = (Element) enclosedElements.get(i);
                if (enclosedElement.getKind() == ElementKind.FIELD
                        && REMAINDER_VAR_NAME.equals(enclosedElement.getSimpleName().toString())
                        && "com.tangosol.util.Binary".equals(((VariableElement) enclosedElement).asType().toString())) {
                    remainderField = true;
                    break;
                }
            }
        } else {
            StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
        }

        return remainderField;
    }

    protected boolean isInnerClassPresent(Element classElement) {
        boolean innerClass = false;

        if (classElement != null) {
            TypeElement te = (TypeElement) classElement;
            List enclosedElements = te.getEnclosedElements();
            List<TypeMirror> interfaces = null;
            for (int i = 0; i < enclosedElements.size(); i++) {
                Element enclosedElement = (Element) enclosedElements.get(i);
                if (enclosedElement.getKind() == ElementKind.CLASS
                        && SERIALIZER_CLASS_NAME.equals(enclosedElement.getSimpleName().toString())) {
                    interfaces = (List<TypeMirror>) ((TypeElement) enclosedElement).getInterfaces();
                    for (TypeMirror tm : interfaces) {
                        if ("com.tangosol.io.pof.PofSerializer".equals(tm.toString())) {
                            innerClass = true;
                            break;
                        }
                    }
                    if (innerClass) {
                        break;
                    }
                }
            }
        } else {
            StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
        }

        return innerClass;
    }

    protected boolean isConstructorPresent(Element classElement) {
        boolean constructor = false;

        if (classElement != null) {
            TypeElement te = (TypeElement) classElement;
            List enclosedElements = te.getEnclosedElements();
            List<VariableElement> parameters = null;
            for (int i = 0; i < enclosedElements.size(); i++) {
                Element enclosedElement = (Element) enclosedElements.get(i);
                if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                    parameters = (List<VariableElement>) ((ExecutableElement) enclosedElement).getParameters();
                    if (!parameters.isEmpty() && REMAINDER_VAR_NAME.equals(parameters.get(parameters.size() - 1).getSimpleName().toString())) {
                        constructor = true;
                        break;
                    }
                }
            }
        } else {
            StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
        }

        return constructor;
    }

    /*
     * Define Revove Methods
     * =====================
     *
     * These methods will be called to remove any previously generated code if the
     * user chooses to overwrite the previously generated code.
     */
    protected ClassTree removeRemainderField(WorkingCopy workingCopy, ClassTree clazz) {
        TreeMaker make = workingCopy.getTreeMaker();
        List<Tree> membersList = (List<Tree>) clazz.getMembers();
        VariableTree vt = null;
        for (Tree t : membersList) {
            if (t.getKind() == Kind.VARIABLE) {
                vt = (VariableTree) t;
                if (REMAINDER_VAR_NAME.equals(vt.getName().toString())) {
                    clazz = make.removeClassMember(clazz, vt);
                    break;
                }
            }
        }
        return clazz;
    }

    protected ClassTree removeInnerClass(WorkingCopy workingCopy, ClassTree clazz) {
        TreeMaker make = workingCopy.getTreeMaker();
        List<Tree> membersList = (List<Tree>) clazz.getMembers();
        ClassTree tree = null;

        for (Tree t : membersList) {
            if (t.getKind() == Kind.CLASS) {
                tree = (ClassTree) t;
                if (SERIALIZER_CLASS_NAME.equals(tree.getSimpleName().toString())) {
                    clazz = make.removeClassMember(clazz, tree);
                    break;
                }
            }
        }
        return clazz;
    }

    protected ClassTree removeConstructor(WorkingCopy workingCopy, ClassTree clazz) {
        TreeMaker make = workingCopy.getTreeMaker();
        List<Tree> membersList = (List<Tree>) clazz.getMembers();
        MethodTree tree = null;
        List<VariableTree> parameters = null;
        Element el = null;

        /*
         * Loop through the class members looking for Methods because at this level
         * the constructor is a METHOD.
         */
        for (Tree t : membersList) {
            if (t.getKind() == Kind.METHOD) {
                tree = (MethodTree) t;
                /*
                 * To identify if this is a CONSTRUCTOR we will need to access the
                 * Element. We need to do this because the method name associated
                 * with a constructor is <init>. I could test for this string but
                 * a more appropriate way is to retrieve the element for the Tree
                 * and then test if this is a ElementKind.CONSTRUCTOR.
                 */
                el = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), tree));
                if (el.getKind() == ElementKind.CONSTRUCTOR) {
                    parameters = (List<VariableTree>) tree.getParameters();
                    /* Check to see if the last parameter of the construtor is
                     * REMAINDER_VAR_NAME. If so then this is the Constructor that
                     * was generated in a previous invocation.
                     */
                    if (!parameters.isEmpty() && parameters.get(parameters.size() - 1).getName().toString().equals(REMAINDER_VAR_NAME)) {
                        clazz = make.removeClassMember(clazz, tree);
                        break;
                    }
                }
            }
        }
        return clazz;
    }

    /*
     * Define Generate Methods
     * =======================
     *
     * These methods generate the appropriate sections of code that are required
     * to implement the inner Serializer. I assume at this point that they do not
     * already exist.
     */
    protected VariableTree createRemainderField(WorkingCopy workingCopy) {
        TreeMaker make = workingCopy.getTreeMaker();
        TypeElement element = null;
        VariableTree parameter = null;

        // Create _remainder variable
        element = workingCopy.getElements().getTypeElement("com.tangosol.util.Binary");
        parameter = make.Variable(
                make.Modifiers(
                Collections.<Modifier>singleton(Modifier.PRIVATE),
                Collections.<AnnotationTree>emptyList()),
                REMAINDER_VAR_NAME,
                make.QualIdent(element),
                null);
        make.addComment(parameter, Comment.create(Comment.Style.LINE, VARIABLE_COMMENT), true);

        return parameter;
    }

    protected MethodTree createConstructor(WorkingCopy workingCopy, ClassTree clazz) {
        TreeMaker make = workingCopy.getTreeMaker();
        ModifiersTree methodModifiers = null;
        MethodTree newConstructor = null;
        StringBuilder sbComment = new StringBuilder(CONSTRUCTOR_COMMENT);

        /*
         * Get the parameters for the Class
         */
        List<VariableElement> classParams = getClassParams(workingCopy, clazz);
        List<VariableTree> constructorParams = getConstructorParams(workingCopy, clazz, classParams);

        for (VariableTree param : constructorParams) {
            sbComment.append("\n@param ");
            sbComment.append(param.getName().toString());
        }

        methodModifiers =
                make.Modifiers(Collections.<Modifier>singleton(Modifier.PROTECTED),
                Collections.<AnnotationTree>emptyList());
        newConstructor = make.Constructor(
                methodModifiers,
                Collections.<TypeParameterTree>emptyList(),
                constructorParams,
                Collections.<ExpressionTree>emptyList(),
                generateConstructorBodyStatements(workingCopy, clazz));
        make.addComment(newConstructor, Comment.create(Comment.Style.JAVADOC, sbComment.toString().replace("{CLASS}", clazz.getSimpleName())), true);

        return newConstructor;
    }

    protected ClassTree createInnerClass(WorkingCopy workingCopy, ClassTree clazz) {
        TreeMaker make = workingCopy.getTreeMaker();
        TypeElement element = null;
        VariableTree parameter = null;
        ModifiersTree methodModifiers = null;
        ModifiersTree paramModifiers = null;
        ClassTree innerClass = null;
        List<VariableTree> methodParams = null;
        ModifiersTree classModifiers = null;
        ExpressionTree throwsClause = null;
        MethodTree newMethod = null;

        paramModifiers = make.Modifiers(Collections.<Modifier>emptySet());
        /*
         * Get the parameters for the Class
         */
//        List<VariableElement> classParams = getClassParams(workingCopy, clazz);
//        List<VariableTree> innerClassParams = getConstructorParams(workingCopy, clazz, classParams);
//        parameter =
//                make.Variable(
//                paramModifiers,
//                //                                    make.Modifiers(Collections.<Modifier>emptySet()),
//                INSTANCE_VAR_NAME,
//                make.Identifier(clazz.getSimpleName()),
//                null);
//        innerClassParams.add(parameter);
        Set<Modifier> modifiers = new TreeSet<Modifier>();
        modifiers.add(Modifier.PUBLIC);
        modifiers.add(Modifier.STATIC);
        classModifiers = make.Modifiers(modifiers);
        element = workingCopy.getElements().getTypeElement("com.tangosol.io.pof.PofSerializer");
        innerClass = make.Class(
                classModifiers, // Modifiers public static etc
                SERIALIZER_CLASS_NAME, // Name
                Collections.<TypeParameterTree>emptyList(), // Type parameters <TypeParameterTree>
                null, // Extends Clause
                Collections.singletonList(make.QualIdent(element)), // Implements Clause
                //                innerClassParams // List or members / fields
                new ArrayList<VariableTree>());

        // Add Write Serialize Method
        methodModifiers =
                make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC),
                Collections.<AnnotationTree>emptyList());
        methodParams = new ArrayList<VariableTree>();
        // Writer Param
        element = workingCopy.getElements().getTypeElement("com.tangosol.io.pof.PofWriter");
        parameter =
                make.Variable(make.Modifiers(Collections.<Modifier>emptySet()),
                "writer",
                make.QualIdent(element),
                null);
        methodParams.add(parameter);
        // Object Param
        element = workingCopy.getElements().getTypeElement("java.lang.Object");
        parameter =
                make.Variable(make.Modifiers(Collections.<Modifier>emptySet()),
                "obj",
                make.QualIdent(element),
                null);
        methodParams.add(parameter);
        element = workingCopy.getElements().getTypeElement("java.io.IOException");
        throwsClause = make.QualIdent(element);
        newMethod =
                make.Method(
                methodModifiers, // Modifiers and Annotations
                "serialize", // Name of the Method
                make.PrimitiveType(TypeKind.VOID), // Return Type
                Collections.<TypeParameterTree>emptyList(), // Type of Parameters for the Parameters
                methodParams, // Parameters <VariableTree>
                Collections.<ExpressionTree>singletonList(throwsClause), // Throws Clause
                generateSerializerBodyStatements(workingCopy, clazz), // Body of the Methods as a Block of Expressions
                null);
        make.addComment(newMethod, Comment.create(Comment.Style.JAVADOC, SERIALIZE_JAVADOC), true);
        // Modify the working copy
        innerClass = make.addClassMember(innerClass, newMethod);

        // Add the Constants used for index into the Read & Write Methods
        List<VariableTree> constants = generateIdxConstants(workingCopy, clazz);
        for (VariableTree vt : constants) {
            innerClass = make.addClassMember(innerClass, vt);
        }


        // Add deserialize Method
        methodModifiers =
                make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC),
                Collections.<AnnotationTree>emptyList());
        element = workingCopy.getElements().getTypeElement("com.tangosol.io.pof.PofReader");
        parameter =
                make.Variable(make.Modifiers(Collections.<Modifier>emptySet()),
                "reader",
                make.QualIdent(element),
                null);
        element = workingCopy.getElements().getTypeElement("java.io.IOException");
        throwsClause = make.QualIdent(element);
        element = workingCopy.getElements().getTypeElement("java.lang.Object");
        newMethod =
                make.Method(methodModifiers,
                "deserialize",
                make.QualIdent(element), // Return Type
                Collections.<TypeParameterTree>emptyList(),
                Collections.singletonList(parameter), // Parameters <VariableTree>
                Collections.<ExpressionTree>singletonList(throwsClause),
                //                                    make.Block(getReadExternalBody(clazz, make), false),
                generateDeserializerBodyStatements(workingCopy, clazz),
                null);
        make.addComment(newMethod, Comment.create(Comment.Style.JAVADOC, DESERIALIZE_JAVADOC), true);
        // Modify the working copy
        innerClass = make.addClassMember(innerClass, newMethod);

        make.addComment(innerClass, Comment.create(Comment.Style.JAVADOC, INNER_CLASS_COMMENT.replace("{CLASS}", clazz.getSimpleName())), true);

        return innerClass;
    }

    protected BlockTree generateConstructorBodyStatements(WorkingCopy workingCopy, ClassTree clazz) {
        List<StatementTree> statements = new ArrayList<StatementTree>();
        TreeMaker make = workingCopy.getTreeMaker();
        Element el = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), clazz));
        AssignmentTree assignment = null;

        if (el == null) {
            StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
        } else {
            TypeElement te = (TypeElement) el;
            List enclosedElements = te.getEnclosedElements();
            for (int i = 0; i < enclosedElements.size(); i++) {
                Element enclosedElement = (Element) enclosedElements.get(i);
                if (enclosedElement.getKind() == ElementKind.FIELD) {
                    if (isSerializable((VariableElement) enclosedElement)) {
                        assignment = make.Assignment(
                                make.Identifier("this.".concat(enclosedElement.getSimpleName().toString())),
                                make.Identifier(enclosedElement.getSimpleName()));

                        statements.add(make.ExpressionStatement(assignment));
                    }
                }
            }
            assignment = make.Assignment(
                    make.Identifier("this.".concat(REMAINDER_VAR_NAME)),
                    make.Identifier(REMAINDER_VAR_NAME));

            statements.add(make.ExpressionStatement(assignment));
        }

        return make.Block(statements, false);
    }

    protected BlockTree generateDeserializerBodyStatements(WorkingCopy workingCopy, ClassTree clazz) {
        TreeMaker make = workingCopy.getTreeMaker();
        Element classElement = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), clazz));
        List<StatementTree> statements = new ArrayList<StatementTree>();
        AssignmentTree assignment = null;
        String methodName = null;
        Tree typeCast = null;
        Tree typeClass = null;
        List<ExpressionTree> arguments = null;
        List<ExpressionTree> newClassArgs = new ArrayList<ExpressionTree>();
        ExpressionTree expression = null;
        MethodInvocationTree invocation = null;
        boolean isSQLDate = false;
        VariableTree parameter = null;
        ModifiersTree paramModifiers = null;
        TypeElement element = null;

        if (classElement == null) {
            StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
        } else {
            TypeElement te = (TypeElement) classElement;
            List enclosedElements = te.getEnclosedElements();
            int idx = 0;
            VariableElement ve = null;
            Element enclosedElement = null;
            for (Object obj : enclosedElements) {
                enclosedElement = (Element) obj;
                if (enclosedElement.getKind() == ElementKind.FIELD) {
                    ve = (VariableElement) enclosedElement;
                    if (isSerializable(ve)) {
                        String typeName = ((VariableElement) enclosedElement).asType().toString();

                        arguments = new ArrayList<ExpressionTree>();
                        arguments.add(make.Identifier(generateConstantName(enclosedElement.getSimpleName().toString())));
                        newClassArgs.add(make.Identifier(enclosedElement));
                        methodName = "readObject";
                        typeCast = null;
                        typeClass = typeCast = ((VariableTree) workingCopy.getTrees().getTree(enclosedElement)).getType();
                        isSQLDate = false;

                        if (typeName != null) {
                            if (ve.asType().getKind().isPrimitive()) {
                                String methodType = initCaps(typeName);
                                methodType = initCaps(typeName);
                                methodName = "read" + methodType;
                            } else if (ve.asType().getKind().equals(TypeKind.ARRAY)) {
                                String methodType = initCaps(typeName);
                                int pos = typeName.lastIndexOf(".");
                                int openPos = typeName.indexOf("[]");
                                if (pos < 0) {
                                    // Primative Array
                                    methodType = initCaps(typeName.substring(0, openPos));
                                    methodName = "read" + methodType + "Array";
                                } else {
                                    methodType = typeName.substring(pos + 1, openPos);
                                    methodName = "readObjectArray";
//                                    arguments.add(make.Identifier(enclosedElement));
                                    arguments.add(make.Identifier("null"));
                                    typeCast = ((VariableTree) workingCopy.getTrees().getTree(enclosedElement)).getType();
                                }
                            } else {
                                try {

                                    if (isString(ve)) {
                                        methodName = "readString";
                                    } else if (isBigDecimal(ve)) {
                                        methodName = "readBigDecimal";
                                    } else if (isBigInteger(ve)) {
                                        methodName = "readBigInteger";
                                    } else if (isDate(ve)) {
                                        methodName = "readDate";
                                    } else if (isSQLDate(ve)) {
                                        methodName = "readDate";
                                        isSQLDate = true;
                                    } else if (isBinary(ve)) {
                                        methodName = "readBinary";
                                    } else if (isMap(ve)) {
                                        methodName = "readMap";
//                                        arguments.add(make.Identifier(enclosedElement));
                                        arguments.add(make.Identifier("null"));
                                        typeCast = ((VariableTree) workingCopy.getTrees().getTree(enclosedElement)).getType();
                                    } else if (isCollection(ve)) {
                                        methodName = "readCollection";
//                                        arguments.add(make.Identifier(enclosedElement));
                                        arguments.add(make.Identifier("null"));
                                        typeCast = ((VariableTree) workingCopy.getTrees().getTree(enclosedElement)).getType();
                                    } else {
                                        methodName = "readObject";
                                        typeCast = ((VariableTree) workingCopy.getTrees().getTree(enclosedElement)).getType();
                                    }
                                } catch (Exception ex) {
                                    // We can't find the Class in the Classpath so we will treat it as an object
                                    methodName = "readObject";
                                    typeCast = ((VariableTree) workingCopy.getTrees().getTree(enclosedElement)).getType();
                                    logger.log(Level.WARNING, "*** APH-I1 : generateReadExternalBody() " + ex.getMessage());
                                }

                            }
                        }
                        idx++;

                        // Build Assignment and add to Statement Tree
                        invocation = make.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),
                                make.MemberSelect(make.Identifier("reader"), methodName),
                                arguments);
                        if (isSQLDate) {
                            expression = make.NewClass(null, Collections.<ExpressionTree>emptyList(),
                                    make.Identifier("java.sql.Date"),
                                    Collections.<ExpressionTree>singletonList(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(invocation, "getTime"), Collections.<ExpressionTree>emptyList())),
                                    null);
                        } else if (typeCast != null) {
                            expression = make.TypeCast(typeCast, invocation);
                        } else {
                            expression = invocation;
                        }

                        paramModifiers = make.Modifiers(Collections.<Modifier>emptySet());
                        parameter =
                                make.Variable(
                                paramModifiers, // Modifiers public / static etc
                                enclosedElement.getSimpleName(), // Parameter Name
                                typeClass, // Class Name
                                expression); // Assignment (Initial Value)
                        statements.add(parameter);

//                        assignment = make.Assignment(
//                                make.Identifier(enclosedElement.getSimpleName()),
//                                expression);
//
//                        statements.add(make.ExpressionStatement(assignment));
                    }
                }
            }
        }
        // Add Remainder
        expression = make.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                make.MemberSelect(make.Identifier("reader"), "readRemainder"),
                Collections.<ExpressionTree>emptyList());
        
        element = workingCopy.getElements().getTypeElement("com.tangosol.util.Binary");
        parameter = make.Variable(
                paramModifiers,
                REMAINDER_VAR_NAME,
                make.QualIdent(element),
                expression);
        statements.add(parameter);
//        assignment = make.Assignment(
//                make.Identifier(REMAINDER_VAR_NAME),
//                expression);
//
//        statements.add(make.ExpressionStatement(assignment));

        newClassArgs.add(make.Identifier(REMAINDER_VAR_NAME));
        // Add Return
        expression = make.NewClass(null, Collections.<ExpressionTree>emptyList(),
                make.Identifier(classElement),
                newClassArgs, null);
        ReturnTree returnNew = make.Return(expression);
        statements.add(returnNew);

        return make.Block(statements, false);
    }

    protected BlockTree generateSerializerBodyStatements(WorkingCopy workingCopy, ClassTree clazz) {
        TreeMaker make = workingCopy.getTreeMaker();
        Element classElement = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), clazz));
        AssignmentTree assignment = null;
        List<StatementTree> statements = new ArrayList<StatementTree>();
        String methodName = null;
        Tree typeCast = null;
        List<ExpressionTree> arguments = null;
        ExpressionTree expression = null;
        boolean isSQLDate = false;
        VariableTree parameter = null;
        ModifiersTree paramModifiers = null;

        if (classElement == null) {
            StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
        } else {
            TypeElement te = (TypeElement) classElement;
            List enclosedElements = te.getEnclosedElements();
            int idx = 0;
            VariableElement ve = null;
            Element enclosedElement = null;

            // Add instance assignment
            typeCast = make.Identifier(classElement);
            expression = make.TypeCast(typeCast, make.Identifier("obj"));

            paramModifiers = make.Modifiers(Collections.<Modifier>emptySet());
            parameter =
                    make.Variable(
                    paramModifiers,
                    //                                    make.Modifiers(Collections.<Modifier>emptySet()),
                    INSTANCE_VAR_NAME,
                    make.Identifier(clazz.getSimpleName()),
                    expression);
            statements.add(parameter);

//            assignment = make.Assignment(
//                    make.Identifier("instance"),
//                    expression);
//            statements.add(make.ExpressionStatement(assignment));

            for (Object obj : enclosedElements) {
                enclosedElement = (Element) obj;
                if (enclosedElement.getKind() == ElementKind.FIELD) {
                    ve = (VariableElement) enclosedElement;
                    if (isSerializable(ve)) {
                        String typeName = ve.asType().toString();

                        arguments = new ArrayList<ExpressionTree>();
                        arguments.add(make.Identifier(generateConstantName(enclosedElement.getSimpleName().toString())));
                        methodName = "writeObject";
                        isSQLDate = false;

                        if (typeName != null) {
                            if (ve.asType().getKind().isPrimitive()) {
                                String methodType = initCaps(typeName);
                                methodType = initCaps(typeName);
                                methodName = "write" + methodType;
                            } else if (((VariableElement) enclosedElement).asType().getKind().equals(TypeKind.ARRAY)) {
                                String methodType = initCaps(typeName);
                                int pos = typeName.lastIndexOf(".");
                                int openPos = typeName.indexOf("[]");
                                if (pos < 0) {
                                    // Primative Array
                                    methodType = initCaps(typeName.substring(0, openPos));
                                    methodName = "write" + methodType + "Array";
                                } else {
                                    methodName = "writeObjectArray";
                                }
                            } else {
                                try {
                                    if (isString(ve)) {
                                        methodName = "writeString";
                                    } else if (isBigDecimal(ve)) {
                                        methodName = "writeBigDecimal";
                                    } else if (isBigInteger(ve)) {
                                        methodName = "writeBigInteger";
                                    } else if (isDate(ve)) {
                                        methodName = "writeDate";
                                    } else if (isSQLDate(ve)) {
                                        methodName = "writeDate";
                                        isSQLDate = true;
                                    } else if (isBinary(ve)) {
                                        methodName = "writeBinary";
                                    } else if (isMap(ve)) {
                                        methodName = "writeMap";
                                    } else if (isCollection(ve)) {
                                        methodName = "writeCollection";
                                    } else {
                                        methodName = "writeObject";
                                    }
                                } catch (Exception ex) {
                                    // We can't find the Class in the Classpath so we will treat it as an object
                                    methodName = "writeObject";
                                }
                            }
                        }

                        idx++;
                        // Build Assignment and add to Statement Tree
                        if (isSQLDate) {
                            arguments.add(
                                    make.NewClass(null, Collections.<ExpressionTree>emptyList(),
                                    make.Identifier("java.util.Date"),
                                    Collections.<ExpressionTree>singletonList(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier(INSTANCE_VAR_NAME+"."+enclosedElement.getSimpleName()), "getTime"), Collections.<ExpressionTree>emptyList())),
                                    null));
                        } else {
                            arguments.add(make.Identifier(INSTANCE_VAR_NAME+"."+enclosedElement.getSimpleName()));
                        }

                        expression = make.MethodInvocation(
                                Collections.<ExpressionTree>emptyList(),
                                make.MemberSelect(make.Identifier("writer"), methodName),
                                arguments);

                        statements.add(make.ExpressionStatement(expression));
                    }
                }
            }
        }

        // Add Remainder
        arguments = new ArrayList<ExpressionTree>();
        arguments.add(make.Identifier(INSTANCE_VAR_NAME+"."+REMAINDER_VAR_NAME));
        expression = make.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                make.MemberSelect(make.Identifier("writer"), "writeRemainder"),
                arguments);

        statements.add(make.ExpressionStatement(expression));

        return make.Block(statements, false);
    }

    protected List<VariableTree> generateIdxConstants(WorkingCopy workingCopy, ClassTree clazz) {
        List<VariableTree> idxConstants = new ArrayList<VariableTree>();
        TreeMaker make = workingCopy.getTreeMaker();
        Element classElement = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), clazz));

        if (classElement == null) {
            StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
            logger.log(Level.WARNING, "*** APH-I1 : generateIdxConstants() Cannot resolve class!");
        } else {
            Set<Modifier> modifiers = new TreeSet<Modifier>();
            modifiers.add(Modifier.PUBLIC);
            modifiers.add(Modifier.STATIC);
            modifiers.add(Modifier.FINAL);
            ModifiersTree paramModifiers = make.Modifiers(modifiers);
            VariableTree parameter = null;
            TypeElement te = (TypeElement) classElement;
            List enclosedElements = te.getEnclosedElements();
            int idx = 0;
            String constantName = null;
            for (int i = 0; i < enclosedElements.size(); i++) {
                Element enclosedElement = (Element) enclosedElements.get(i);
                logger.log(Level.FINE, "*** APH-I2 : generateIdxConstants() Element ".concat(
                        enclosedElement.getSimpleName().toString().concat(
                        " Type ".concat(
                        enclosedElement.getKind().toString()))));

                if (enclosedElement.getKind() == ElementKind.FIELD) {
                    if (isSerializable((VariableElement) enclosedElement)) {
                        constantName = generateConstantName(enclosedElement.getSimpleName().toString());
                        parameter =
                                make.Variable(
                                paramModifiers, // Type Modifiers
                                constantName, // Name
                                make.PrimitiveType(TypeKind.INT), // Type
                                make.Literal(idx)); // Value
                        if (idx == 0) {
                            make.addComment(parameter, Comment.create(Comment.Style.LINE, CONSTANTS_JAVADOC), true);
                        }
                        idxConstants.add(parameter);

                        idx++;
                        logger.log(Level.FINE, "*** APH-I3 : generateIdxConstants() Adding Parameter ".concat(parameter.toString()));
                    }
                }
            }
        }

        logger.log(Level.FINE, "*** APH-I1 : generateIdxConstants() Returning ".concat("" + idxConstants.size()).concat(" Constants ").concat(idxConstants.toString()));
        return idxConstants;
    }

    /*
     * The following methods are used to test the class / interface associated with
     * the currently processing object.
     */
    // Check if the Class is a String type
    public final static String STRING_CLASS_NAME = String.class.getCanonicalName();

    public boolean isString(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(STRING_CLASS_NAME);
        }
        return is;
    }
    // Check is the Class is a BigDecimal
    public final static String BIGDECIMAL_CLASS_NAME = BigDecimal.class.getCanonicalName();

    public boolean isBigDecimal(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(BIGDECIMAL_CLASS_NAME);
        }
        return is;
    }
    // Check is the Class is a BigInteger
    public final static String BIGINTEGER_CLASS_NAME = BigInteger.class.getCanonicalName();

    public boolean isBigInteger(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(BIGINTEGER_CLASS_NAME);
        }
        return is;
    }
    // Check is the Class is a Date
    public final static String DATE_CLASS_NAME = Date.class.getCanonicalName();

    public boolean isDate(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(DATE_CLASS_NAME);
        }
        return is;
    }
    // Check is the Class is a SQL Date
    public final static String SQLDATE_CLASS_NAME = java.sql.Date.class.getCanonicalName();

    public boolean isSQLDate(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(SQLDATE_CLASS_NAME);
        }
        return is;
    }
    // Check is the Class is a Binary
    public final static String BINARY_CLASS_NAME = "com.tangosol.util.Binary";

    public boolean isBinary(VariableElement ve) {
        boolean is = false;
        if (ve != null) {
            is = ve.asType().toString().contains(BINARY_CLASS_NAME);
        }
        return is;
    }
    // Check if the class implements the Map interface
    public final static String MAP_CLASS_NAME = Map.class.getCanonicalName();

    public boolean isMap(VariableElement ve) {
        boolean is = false;

        if (ve != null) {
            is = ve.asType().toString().contains(MAP_CLASS_NAME);
            if (!is) {
                String className = ve.asType().toString();
                int genericsPos = className.indexOf("<");
                if (genericsPos > 0) {
                    className = ve.asType().toString().substring(0, genericsPos);
                }
                try {
                    Class theClass = Class.forName(className);
                    is = isMap(theClass);
                } catch (ClassNotFoundException e) {
                }
            }
        }

        return is;
    }

    public boolean isMap(Class theClass) {
        boolean isMap = false;
        if (theClass != null) {
            Class[] classes = theClass.getInterfaces();
            for (Class oneClass : classes) {
                isMap = MAP_CLASS_NAME.equals(oneClass.getCanonicalName());
                if (isMap) {
                    break;
                }
            }
            if (!isMap) {
                isMap = isMap(theClass.getSuperclass());
            }
        }
        return isMap;
    }
    // check if the class implements Collection
    public final static String COLLECTION_CLASS_NAME = Collection.class.getCanonicalName();

    public boolean isCollection(VariableElement ve) {
        boolean is = false;

        if (ve != null) {
            is = ve.asType().toString().contains(COLLECTION_CLASS_NAME);
            if (!is) {
                String className = ve.asType().toString();
                int genericsPos = className.indexOf("<");
                if (genericsPos > 0) {
                    className = ve.asType().toString().substring(0, genericsPos);
                }
                try {
                    Class theClass = Class.forName(className);
                    is = isCollection(theClass);
                } catch (ClassNotFoundException e) {
                }
            }
        }

        return is;
    }

    public boolean isCollection(Class theClass) {
        boolean isCollection = false;
        if (theClass != null) {
            Class[] classes = theClass.getInterfaces();
            for (Class oneClass : classes) {
                isCollection = COLLECTION_CLASS_NAME.equals(oneClass.getCanonicalName());
                if (isCollection) {
                    break;
                }
            }
            if (!isCollection) {
                isCollection = isCollection(theClass.getSuperclass());
            }
        }
        return isCollection;
    }

    /**
     * Check if the enclose VariableElement is Serializeable.
     * That is to say not final, transient, etc.
     * @param enclosedElement
     * @return
     */
    public boolean isSerializable(VariableElement enclosedElement) {
        boolean isSerializable = true;

        Set<Modifier> modifiers = (enclosedElement).getModifiers();
        Iterator<Modifier> itr = modifiers.iterator();
        String name = "";

        while (itr.hasNext()) {
            name = itr.next().name();
            if ("TRANSIENT".equalsIgnoreCase(name)
                    || "FINAL".equalsIgnoreCase(name)) {
                isSerializable = false;
                break;
            }
        }

        return isSerializable;
    }

    private String getInitialisedType(WorkingCopy workingCopy, String fieldName) {
        String initType = null;

        try {
            Document document = workingCopy.getDocument();
            String lines[] = document.getText(0, document.getLength()).split(";");
            int namePos = 0;
            int equalsPos = 0;
            int newPos = 0;
            int parenPos = 0;
            String line = null;

            for (int i = 0; i < lines.length; i++) {
                line = lines[i];
                if ((namePos = line.indexOf(fieldName)) >= 0) {
                    if ((equalsPos = line.indexOf("=", namePos)) >= 0) {
                        if ((newPos = line.indexOf("new", equalsPos)) > 0) {
                            if ((parenPos = line.indexOf("(", newPos)) >= 0) {
                                initType = line.substring(newPos + 4, parenPos);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return initType;
    }

    /**
     * Simply Take the String and Capitalise the first character.
     * @param s
     * @return
     */
    private String initCaps(String s) {
        if (s == null) {
            return s;
        } else {
            return s.toUpperCase().substring(0, 1) + s.toLowerCase().substring(1);
        }
    }

    protected List<VariableElement> getClassParams(WorkingCopy workingCopy, ClassTree clazz) {
        List<VariableElement> classParams = new ArrayList<VariableElement>();
        TreeMaker make = workingCopy.getTreeMaker();
        Element classElement = workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), clazz));

        if (classElement == null) {
            StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
        } else {
            TypeElement te = (TypeElement) classElement;
            List enclosedElements = te.getEnclosedElements();
            for (int i = 0; i < enclosedElements.size(); i++) {
                Element enclosedElement = (Element) enclosedElements.get(i);
                if (enclosedElement.getKind() == ElementKind.FIELD) {
                    if (isSerializable((VariableElement) enclosedElement)) {
                        classParams.add((VariableElement) enclosedElement);
                    }
                }
            }
        }

        return classParams;
    }

    protected List<VariableTree> getConstructorParams(WorkingCopy workingCopy, ClassTree clazz, List<VariableElement> classParams) {
        List<VariableTree> constructorParams = new ArrayList<VariableTree>();
        TreeMaker make = workingCopy.getTreeMaker();
        TypeElement element = null;
        VariableTree parameter = null;
        ModifiersTree paramModifiers = null;

        // Create Constructor
        paramModifiers = make.Modifiers(Collections.<Modifier>emptySet());
        String type = null;
        String generic = null;
        for (VariableElement e : classParams) {
            if (e.asType().getKind().isPrimitive()) {
                parameter = make.Variable(paramModifiers, e.getSimpleName(), make.PrimitiveType(e.asType().getKind()), null);
            } else if (e.asType().getKind().equals(TypeKind.ARRAY)) {
                type = e.asType().toString();
                int pos = type.lastIndexOf(".");
                int openPos = type.indexOf("[]");
                ArrayTypeTree array = null;
                String name = null;
                if (pos < 0) {
                    // Primative Array
                    name = type.substring(0, openPos).toUpperCase();
                    array = make.ArrayType(make.PrimitiveType(TypeKind.valueOf(name)));
                    parameter = make.Variable(paramModifiers, e.getSimpleName(), array, null);
                } else {
                    name = type.substring(0, openPos);
                    element = workingCopy.getElements().getTypeElement(name);
                    array = make.ArrayType(make.QualIdent(element));
                    parameter = make.Variable(paramModifiers, e.getSimpleName(), array, null);
                }
            } else {
                type = e.asType().toString();
                generic = null;
                if (type.indexOf("<") >= 0) {
                    type = type.substring(0, type.indexOf("<"));
                    generic = type.substring(type.indexOf("<") + 1);
                }
                element = workingCopy.getElements().getTypeElement(type);
                //TODO: Add Generics Code
//                                    element = make.ParameterizedType(element, classParams);
                if (element != null) {
                    parameter = make.Variable(paramModifiers, e.getSimpleName(), make.QualIdent(element), null);
                }
            }
            constructorParams.add(parameter);
        }
        element = workingCopy.getElements().getTypeElement("com.tangosol.util.Binary");
        parameter =
                make.Variable(
                paramModifiers,
                REMAINDER_VAR_NAME,
                make.QualIdent(element),
                null);

        constructorParams.add(parameter);

        return constructorParams;
    }

    private String generateConstantName(String s) {
        if (s == null) {
            return s;
        } else {
            return s.trim().toUpperCase() + "_IDX";
        }
    }
}
