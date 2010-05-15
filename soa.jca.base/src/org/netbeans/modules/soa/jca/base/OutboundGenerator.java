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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.jca.base;

import org.netbeans.modules.soa.jca.base.generator.api.GeneratorUtil;
import org.netbeans.modules.soa.jca.base.generator.api.JavacTreeModel;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.annotation.Resource;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.soa.jca.base.spi.GlobalRarProvider;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ReturnTree;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.xml.XMLUtil;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * generator Java code fragments for Outbound Global Rar, code is generated
 * as a result drag-n-drop action of an icon from palette.
 *
 * @author echou
 */
public class OutboundGenerator {

    private static final Logger logger = Logger.getLogger(OutboundGenerator.class.getName());

    public static final String BEGIN_EDITOR_FOLD_CONN_SETUP =
            "<editor-fold defaultstate=\"collapsed\" desc=\"Connection setup and takedown. Click on the + sign on the left to edit the code.\">"; // NOI18N
    public static final String BEGIN_EDITOR_FOLD_RESOURCE_DECLARATION =
            "<editor-fold defaultstate=\"collapsed\" desc=\"${varname} resource declaration. Click on the + sign on the left to edit the code.\">"; // NOI18N
    public static final String BEGIN_EDITOR_FOLD_EJB_CTX_DECLARATION =
            "<editor-fold defaultstate=\"collapsed\" desc=\"EJBContext declaration. Click on the + sign on the left to edit the code.\">"; // NOI18N
    public static final String BEGIN_EDITOR_FOLD_CLASS_DECLARATION =
            "<editor-fold defaultstate=\"collapsed\" desc=\"Inner Class declaration. Click on the + sign on the left to edit the code.\">"; // NOI18N
    public static final String BEGIN_EDITOR_FOLD_INTF_DECLARATION =
            "<editor-fold defaultstate=\"collapsed\" desc=\"Inner Interface declaration. Click on the + sign on the left to edit the code.\">"; // NOI18N

    public static final String END_EDITOR_FOLD = "</editor-fold>"; // NOI18N

    private boolean generatedNewMethodFlag = false;

    private JTextComponent target;
    private JavacTreeModel javacTreeModel;
    private String businessRule;
    private String rarName;
    private List<String> libNames;
    private String jndiName;
    private boolean rollbackTx;
    private boolean logException;
    private boolean rethrowException;
    private String description;
    private String authentication;
    private String shareable;
    private String localVarName;
    private String otdType;
    private Properties additionalConfig;
    private String returnType;

    private GlobalRarProvider rarProvider;
    private Document templateDoc;

    public OutboundGenerator(JTextComponent target, JavacTreeModel javacTreeModel, String businessRule, String rarName,
            String jndiName, boolean rollbackTx, boolean logException, boolean rethrowException,
            String description, String authentication,
            String shareable, String localVarName, String otdType,
            Properties additionalConfig, String returnType) throws Exception {
        this.target = target;
        this.javacTreeModel = javacTreeModel;
        this.businessRule = businessRule;
        this.rarName = rarName;
        this.jndiName = jndiName;
        this.rollbackTx = rollbackTx;
        this.logException = logException;
        this.rethrowException = rethrowException;
        this.description = description;
        this.authentication = authentication;
        this.shareable = shareable;
        this.localVarName = localVarName;
        this.otdType = otdType;
        this.additionalConfig = additionalConfig;
        this.returnType = returnType;

        // load template xml
        this.rarProvider = GlobalRarRegistry.getInstance().getRar(rarName);
        if (this.rarProvider == null) {
            throw new Exception(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/Bundle").getString("unknown_global_rar:_") + rarName);
        }
        this.libNames = rarProvider.getLibraryNames();

        InputStream is = this.rarProvider.getTemplate();
        if (is == null) {
            throw new Exception(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/Bundle").getString("unable_to_load_template_resource:_") +
                    this.rarProvider.getName());
        }

        try {
            this.templateDoc = XMLUtil.parse(
                    new InputSource(is),
                    false, false, null, null);
        } finally {
            is.close();
        }
    }

    public void addLibraryDependency() throws IOException {
        JavaSource javaSource = JavaSource.forDocument(target.getDocument());
        FileObject fo = javaSource.getFileObjects().iterator().next();
        Project project = FileOwnerQuery.getOwner(fo);
        for (String libName : libNames) {
            GeneratorUtil.addLibrary(libName, project);
        }
    }

    public void generateFromTemplate() throws IOException {
        JavaSource javaSource = JavaSource.forDocument(target.getDocument());

        // task to generate tree from the template xml
        GenerateTask<WorkingCopy> genTask = new GenerateTask<WorkingCopy> ();

        ModificationResult result = null;
        try {
            result = javaSource.runModificationTask(genTask);
        } catch (IOException ioe) {
            Exception taskException = genTask.getException();
            if (taskException != null) {
                NotifyDescriptor d = new NotifyDescriptor.Exception(taskException);
                DialogDisplayer.getDefault().notifyLater(d);
                return;
            } else {
                throw ioe;
            }
        }
        result.commit();

        // don't fix imports for fully-qualified types here, might unintentionally
        // fix user's code.
        /*
        // task to fix all the import statements
        Task<WorkingCopy> importTask = new Task<WorkingCopy> () {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                GeneratorUtilities genUtil = GeneratorUtilities.get(workingCopy);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                Tree newCut = genUtil.importFQNs(cut);
                workingCopy.rewrite(cut, newCut);
            }
        };
        javaSource.runModificationTask(importTask).commit();
        */
    }

    class GenerateTask<T extends WorkingCopy> implements Task<WorkingCopy> {

        private Exception myException = null;

        public Exception getException() {
            return myException;
        }

        public void run(WorkingCopy workingCopy) throws Exception {
            try {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);

                String fullyQualifiedClassName = null;
                List<? extends TypeElement> elements = workingCopy.getTopLevelElements();
                if (elements.size() > 0) {
                    TypeElement topElement = elements.get(0);
                    fullyQualifiedClassName = topElement.getQualifiedName().toString();
                }
                if (fullyQualifiedClassName == null) {
                    fullyQualifiedClassName = classTree.getSimpleName().toString();
                }

                Element root = templateDoc.getDocumentElement();
                NodeList children = root.getChildNodes();
                ClassTree newClassTree = classTree;
                for (int i = 0; i < children.getLength(); i++) {
                    final Node curChild = children.item(i);
                    if (curChild instanceof Element) {
                        ClassTree result = handleNode((Element) curChild, workingCopy, newClassTree, fullyQualifiedClassName);
                        if (result != null) {
                            newClassTree = result;
                        }
                    }
                }

                if (generatedNewMethodFlag) {
                    String varName = "ectx"; // NOI18N
                    if (javacTreeModel.getVariablesByName(varName).size() == 0) {
                        TreeMaker make = workingCopy.getTreeMaker();
                        ModifiersTree modifiers = handleModifiersAndAnnotations(make, workingCopy,
                                "javax.annotation.Resource", null); // NOI18N

                        VariableTree variableTree = GeneratorUtil.createField(make, workingCopy,
                            modifiers,
                            varName,
                            "javax.ejb.EJBContext", // NOI18N
                            null
                        );
                        make.addComment(variableTree, Comment.create(Comment.Style.LINE, -2, -2, -2, BEGIN_EDITOR_FOLD_EJB_CTX_DECLARATION), true);
                        make.addComment(variableTree, Comment.create(Comment.Style.LINE, -2, -2, -2, END_EDITOR_FOLD), false);

                        newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, variableTree);
                    }
                    generateMethodInvocation(workingCopy, varName);
                }

                workingCopy.rewrite(classTree, newClassTree);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "error during outbound generation: " + e, e);
                myException = e;
                throw e;
            }
        }
    }

    private ClassTree handleNode(Element elem, WorkingCopy workingCopy, ClassTree classTree, String fullyQualifiedClassName) throws Exception {
        String nodeName = elem.getTagName();
        if (nodeName.equals("variable")) { // NOI18N
            return handleVariable(elem, workingCopy, classTree, fullyQualifiedClassName);
        } else if (nodeName.equals("method")) { // NOI18N
            return handleMethod(elem, workingCopy, classTree, fullyQualifiedClassName);
        } else if (nodeName.equals("interface")) { // NOI18N
            return handleInterface(elem, workingCopy, classTree, fullyQualifiedClassName);
        } else if (nodeName.equals("class")) { // NOI18N
            return handleClass(elem, workingCopy, classTree, fullyQualifiedClassName);
        } else {
            //throw new Exception("unable to handle template node: " + nodeName);
        }
        return null;
    }

    private ClassTree handleVariable(Element elem, WorkingCopy workingCopy, ClassTree classTree, String fullyQualifiedClassName) throws Exception {
        String type = filterText(elem.getAttribute("type")); // NOI18N
        String name = filterText(elem.getAttribute("name")); // NOI18N

        if ("false".equalsIgnoreCase(elem.getAttribute("allowDuplicate")) && hasVariable(classTree, name)) { // NOI18N
            return classTree;
        }

        TreeMaker make = workingCopy.getTreeMaker();
        ModifiersTree modifiers = handleModifiersAndAnnotations(
                elem.getElementsByTagName("modifier"), // NOI18N
                elem.getElementsByTagName("annotation"), // NOI18N
                fullyQualifiedClassName,
                workingCopy);


        boolean fold = Boolean.parseBoolean(elem.getAttribute("fold")); // NOI18N

        VariableTree variableTree = GeneratorUtil.createField(make, workingCopy,
                modifiers,
                name,
                type,
                null
        );

        handleComment(elem, variableTree, workingCopy, fold, BEGIN_EDITOR_FOLD_RESOURCE_DECLARATION.replaceAll("\\$\\{varname\\}", name));

        return make.addClassMember(classTree, variableTree);
    }

    private ClassTree handleMethod(Element elem, WorkingCopy workingCopy, ClassTree classTree, String fullyQualifiedClassName) throws Exception {
        String methodName = elem.getAttribute("name"); // NOI18N
        if (methodName.startsWith("_execute_")) { // NOI18N
            methodName = methodName.substring(9);
        }
        methodName = filterText(methodName);
        ExecutableElement method = GeneratorUtil.findMethodByName(workingCopy, methodName);
        if (method == null) {
            generatedNewMethodFlag = true;
            return createMethodTree(elem, workingCopy, classTree, fullyQualifiedClassName);
        }

        // specific handling for special method
        if (methodName.startsWith("_invoke_")) { // NOI18N
            mergeInvokeMethodBody(method, elem, workingCopy);
        } else {
            appendExecuteMethodParam(method, elem, workingCopy);
        }

        return null;
    }

    private ClassTree handleInterface(Element elem, WorkingCopy workingCopy, ClassTree classTree, String fullyQualifiedClassName) throws Exception {
        String type = elem.getAttribute("type");
        if (hasType(classTree, type)) {
            return classTree;
        }

        // generate new ejb interface
        TreeMaker make = workingCopy.getTreeMaker();
        ModifiersTree modifiers = handleModifiersAndAnnotations(
                elem.getElementsByTagName("modifier"), // NOI18N
                elem.getElementsByTagName("annotation"), // NOI18N
                fullyQualifiedClassName,
                workingCopy);

        String extendsAttr = elem.getAttribute("extends"); // NOI18N
        List<Tree> extendsList = new ArrayList<Tree> ();
        if (extendsAttr != null && extendsAttr.length() > 0) {
            extendsList.add(make.Identifier(extendsAttr));
        }

        ClassTree interfaceTree = make.Interface(
                modifiers, // modifiers
                type, // simpleName
                Collections.EMPTY_LIST, // typeParameters
                extendsList, // extendsClauses
                Collections.EMPTY_LIST  // memberDecls
        );

        boolean fold = Boolean.parseBoolean(elem.getAttribute("fold")); // NOI18N
        handleComment(elem, interfaceTree, workingCopy, fold, BEGIN_EDITOR_FOLD_INTF_DECLARATION);

        return make.addClassMember(classTree, interfaceTree);
    }

    private ClassTree handleClass(Element elem, WorkingCopy workingCopy, ClassTree classTree, String fullyQualifiedClassName) throws Exception {
        String type = elem.getAttribute("type");
        if (hasType(classTree, type)) {
            return classTree;
        }

        // generate new ejb interface
        TreeMaker make = workingCopy.getTreeMaker();
        ModifiersTree modifiers = handleModifiersAndAnnotations(
                elem.getElementsByTagName("modifier"), // NOI18N
                elem.getElementsByTagName("annotation"), // NOI18N
                fullyQualifiedClassName,
                workingCopy);

        String extendsAttr = elem.getAttribute("extends"); // NOI18N
        Tree extendsTree = null;
        if (extendsAttr != null && extendsAttr.length() > 0) {
            extendsTree = make.Identifier(extendsAttr);
        }

        String implementsAttr = elem.getAttribute("implements"); // NOI18N
        List<Tree> implementsList = new ArrayList<Tree> ();
        if (implementsAttr != null && implementsAttr.length() > 0) {
            String[] tokens = implementsAttr.split(",");
            for (String token : tokens) {
                String s = token.trim();
                if (s != null && s.length() > 0) {
                    implementsList.add(make.Identifier(s));
                }
            }
        }

        ClassTree newClassTree = make.Class(
                modifiers, // modifiers
                type, // simpleName
                Collections.EMPTY_LIST, // typeParameters
                extendsTree, // extendsClauses
                implementsList, // implementsClauses
                Collections.EMPTY_LIST  // memberDecls
        );

        boolean fold = Boolean.parseBoolean(elem.getAttribute("fold")); // NOI18N
        handleComment(elem, newClassTree, workingCopy, fold, BEGIN_EDITOR_FOLD_CLASS_DECLARATION);

        return make.addClassMember(classTree, newClassTree);
    }

    private ClassTree createMethodTree(Element elem, WorkingCopy workingCopy, ClassTree classTree, String fullyQualifiedClassName) throws Exception {
        TreeMaker make = workingCopy.getTreeMaker();
        ModifiersTree modifiers = handleModifiersAndAnnotations(
                elem.getElementsByTagName("modifier"), // NOI18N
                elem.getElementsByTagName("annotation"), // NOI18N
                fullyQualifiedClassName,
                workingCopy);

        String paramNamesStr = ""; // NOI18N
        List<VariableTree> params = new ArrayList<VariableTree> ();
        MethodTree enclosingMethodTree = getEnclosingMethod(workingCopy);
        if (enclosingMethodTree != null) {
            for (VariableTree var: enclosingMethodTree.getParameters()) {
                params.add(make.Variable(
                        make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                        var.getName(),
                        var.getType(),
                        null)
                );
                paramNamesStr = paramNamesStr + var.getName() + ","; // NOI18N
            }
        }
        params.addAll(handleParams(elem.getElementsByTagName("param"), workingCopy)); // NOI18N

        List<ExpressionTree> throwsList = handleThrows(
                elem.getElementsByTagName("throws"), // NOI18N
                workingCopy);

        String methodName = elem.getAttribute("name"); // NOI18N
        if (methodName.startsWith("_execute_")) { // NOI18N
            methodName = methodName.substring(9);
        }
        methodName = filterText(methodName); // NOI18N
        //String returnType = elem.getAttribute("returnType"); // NOI18N
        boolean fold = Boolean.parseBoolean(elem.getAttribute("fold")); // NOI18N
        String bodyText = filterText(getMethodBodyCDATAText(elem.getElementsByTagName("body"))); // NOI18N
        bodyText = applyAdditionalConfig(bodyText);

        // work around for now
        bodyText = bodyText.replaceAll("\\$\\{EXTRA_PARAMS\\}", paramNamesStr); // NOI18N
        if (returnType.equals("void")) { // NOI18N
            bodyText = bodyText.replaceAll("_execute_", ""); // NOI18N
        } else {
            bodyText = bodyText.replaceAll("_execute_", "return "); // NOI18N
        }
        if (!methodName.startsWith("_invoke_")) { // NOI18n
            if (returnType.equals("void")) {
                // do nothing
            } else if (returnType.equals("byte") || returnType.equals("short") || returnType.equals("int")) {
                bodyText = bodyText + "\nreturn 0;";
            } else if (returnType.equals("long")) {
                bodyText = bodyText + "\nreturn 0L;";
            } else if (returnType.equals("float")) {
                bodyText = bodyText + "\nreturn 0.0f;";
            } else if (returnType.equals("double")) {
                bodyText = bodyText + "\nreturn 0.0d;";
            } else if (returnType.equals("char")) {
                bodyText = bodyText + "\nreturn '\\u0000';";
            } else if (returnType.equals("boolean")) {
                bodyText = bodyText + "\nreturn false;";
            } else {
                bodyText = bodyText + "\nreturn null;";
            }
        }

        // make a new tree
        MethodTree methodTree = make.Method(
            modifiers, // modifiers and annotations
            methodName, // name
            GeneratorUtil.createType(make, workingCopy, returnType), // return type
            Collections.EMPTY_LIST, // type parameters for parameters
            params, // parameters
            throwsList, // throws
            //make.Block(Collections.<StatementTree>emptyList(), false), // body
            "{" + bodyText + "}", // body text
            null // default value - not applicable here, used by annotations
        );

        handleComment(elem, methodTree, workingCopy, fold, BEGIN_EDITOR_FOLD_CONN_SETUP);

        return make.addClassMember(classTree, methodTree);

        // now propogate method argument to method invocation
        /*
        if (enclosingMethodTree != null) {
            TryTree tryTree = null;
            for (StatementTree statement : methodTree.getBody().getStatements()) {
                if (statement.getKind() == Tree.Kind.TRY) {
                    tryTree = (TryTree) statement;
                    break;
                }
            }
            if (tryTree != null) {
                BlockTree tryTreeBlock = tryTree.getBlock();
                MethodInvocationTree invocationTree = (MethodInvocationTree) ((ExpressionStatementTree)
                    tryTreeBlock.getStatements().get(tryTreeBlock.getStatements().size() - 1)).getExpression();
                int index = 0;

                MethodInvocationTree newInvocationTree = invocationTree;
                for (Tree tree: invocationTree.getTypeArguments()) {
                    System.out.println("tree = " + tree);
                }
                for (VariableTree var: enclosingMethodTree.getParameters()) {
                    newInvocationTree = make.insertMethodInvocationArgument(newInvocationTree,
                            index,
                            make.Identifier(var.getName()),
                            null);
                    index++;
                }
                workingCopy.rewrite(invocationTree, newInvocationTree);
            }
        }
        */
    }

    private void mergeInvokeMethodBody(ExecutableElement method, Element elem, WorkingCopy workingCopy) throws Exception {
        TreeMaker make = workingCopy.getTreeMaker();

        MethodTree oldMethodTree = workingCopy.getTrees().getTree(method);
        BlockTree oldBody = oldMethodTree.getBody();
        int tryBlockPosition = 0;
        for (StatementTree statement : oldBody.getStatements()) {
            if (statement.getKind() == Tree.Kind.TRY) {
                break;
            }
            tryBlockPosition++;
        }

        String bodyText = filterText(getMethodBodyCDATAText(elem.getElementsByTagName("body"))); // NOI18N
        bodyText = applyAdditionalConfig(bodyText);

        // work around for now
        bodyText = bodyText.replaceAll("\\$\\{EXTRA_PARAMS\\}", ""); // NOI18N

        MethodTree newMethodTree = make.Method(
            make.Modifiers(EnumSet.noneOf(Modifier.class)), // modifiers and annotations
            "anyname", // // NOI18N
            GeneratorUtil.createType(make, workingCopy, "void"), // return type
            Collections.EMPTY_LIST, // type parameters for parameters
            Collections.EMPTY_LIST, // parameters
            Collections.EMPTY_LIST, // throws
            "{" + bodyText + "}", // body text
            null // default value - not applicable here, used by annotations
        );
        BlockTree newBody = newMethodTree.getBody();

        TryTree newTryTree = null;
        BlockTree mergedBody = oldBody;
        // merge new body into old body
        for (StatementTree statement : newBody.getStatements()) {
            if (statement.getKind() == Tree.Kind.TRY) {
                newTryTree = (TryTree) statement;
                break;
            }
            mergedBody = make.insertBlockStatement(mergedBody, tryBlockPosition++, statement);
        }

        TryTree oldTryTree = null;
        for (StatementTree statement : mergedBody.getStatements()) {
            if (statement.getKind() == Tree.Kind.TRY) {
                oldTryTree = (TryTree) statement;
                break;
            }
        }

        // merge try tree block
        BlockTree newTryTreeBlock = newTryTree.getBlock();
        BlockTree mergedTryTreeBlock = oldTryTree.getBlock();
        int indexToInsert = oldTryTree.getBlock().getStatements().size() - 1;
        for (int i = 0; i < newTryTreeBlock.getStatements().size() - 1; i++) {
            StatementTree statement = newTryTreeBlock.getStatements().get(i);
            mergedTryTreeBlock = make.insertBlockStatement(mergedTryTreeBlock, indexToInsert++, statement);
        }

        MethodInvocationTree newInvocation = (MethodInvocationTree) ((ExpressionStatementTree)
                newTryTreeBlock.getStatements().get(newTryTreeBlock.getStatements().size() - 1)).getExpression();
        MethodInvocationTree oldInvocation = null;
        StatementTree oldStatementTree =
                mergedTryTreeBlock.getStatements().get(mergedTryTreeBlock.getStatements().size() - 1);
        if (oldStatementTree.getKind() == Tree.Kind.RETURN) {
            oldInvocation = (MethodInvocationTree) ((ReturnTree) oldStatementTree).getExpression();
        } else {
            oldInvocation = (MethodInvocationTree) ((ExpressionStatementTree) oldStatementTree).getExpression();
        }

        MethodInvocationTree mergedMethodInvocation = make.addMethodInvocationArgument(oldInvocation, newInvocation.getArguments().get(0));
        workingCopy.rewrite(oldInvocation, mergedMethodInvocation);
        workingCopy.rewrite(oldTryTree.getBlock(), mergedTryTreeBlock);

        // merge try tree finally
        BlockTree newTryTreeFinally = newTryTree.getFinallyBlock();
        BlockTree mergedTryTreeFinally = oldTryTree.getFinallyBlock();
        for (StatementTree statement : newTryTreeFinally.getStatements()) {
            mergedTryTreeFinally = make.addBlockStatement(mergedTryTreeFinally, statement);
        }
        workingCopy.rewrite(oldTryTree.getFinallyBlock(), mergedTryTreeFinally);
        workingCopy.rewrite(oldBody, mergedBody);
    }

    private void appendExecuteMethodParam(ExecutableElement method, Element elem, WorkingCopy workingCopy) throws Exception {
        TreeMaker make = workingCopy.getTreeMaker();

        MethodTree oldMethodTree = workingCopy.getTrees().getTree(method);
        List<VariableTree> params = handleParams(
                elem.getElementsByTagName("param"), // NOI18N
                workingCopy);

        MethodTree newMethodTree = oldMethodTree;
        for (VariableTree param : params) {
            newMethodTree = make.addMethodParameter(newMethodTree, param);
        }
        workingCopy.rewrite(oldMethodTree, newMethodTree);

    }

    private void handleComment(Element elem, Tree tree, WorkingCopy workingCopy, boolean fold, String beginFoldString) throws Exception {
        TreeMaker make = workingCopy.getTreeMaker();
        String comment = elem.getAttribute("comment"); // NOI18N

        if (fold) {
            make.addComment(tree,
                    Comment.create(Comment.Style.LINE, -2, -2, -2, beginFoldString), true);
        }
        if (!comment.equals("")) { // NOI18N
            Comment c = Comment.create(Comment.Style.LINE, -2, -2, -2, comment);
            make.addComment(tree, c, true);
        }

        if (fold) {
            make.addComment(tree,
                    Comment.create(Comment.Style.LINE, -2, -2, -2, END_EDITOR_FOLD), false);
        }
    }

    private ModifiersTree handleModifiersAndAnnotations(NodeList modifiers,
            NodeList annotations, String fullyQualifiedClassName,  WorkingCopy workingCopy) throws Exception {
        TreeMaker make = workingCopy.getTreeMaker();
        Set<Modifier> modifierSet = EnumSet.noneOf(Modifier.class);
        for (int i = 0; i < modifiers.getLength(); i++) {
            Element curModifier = (Element) modifiers.item(i);
            modifierSet.add(Modifier.valueOf(curModifier.getAttribute("type"))); // NOI18N
        }

        List<AnnotationTree> annotationTreeList = new ArrayList<AnnotationTree> ();
        for (int i = 0; i < annotations.getLength(); i++) {
            Element curAnnotation = (Element) annotations.item(i);
            String type = curAnnotation.getAttribute("type"); // NOI18N
            List<ExpressionTree> annoArgList = new ArrayList<ExpressionTree> ();
            if (type.equals("javax.annotation.Resource")) { // NOI18N
                ExpressionTree annoArg1 = GeneratorUtil.createAnnotationArgument(make,
                        "name", jndiName); // NOI18N
                ExpressionTree annoArg2 = GeneratorUtil.createAnnotationArgument(make,
                        "description", description); // NOI18N
                ExpressionTree annoArg3 = GeneratorUtil.createAnnotationArgument(make,
                        "authenticationType", // NOI18N
                        authentication.equals("Container") ? Resource.AuthenticationType.CONTAINER.ordinal() : Resource.AuthenticationType.APPLICATION.ordinal()); // NOI18N
                ExpressionTree annoArg4 = GeneratorUtil.createAnnotationArgument(make,
                        "shareable", Boolean.parseBoolean(shareable)); // NOI18N
                annoArgList.add(annoArg1);
                annoArgList.add(annoArg2);
                //annoArgList.add(annoArg3);
                annoArgList.add(annoArg4);
            } else {
                NodeList annotationArgList = curAnnotation.getElementsByTagName("annotation-arg"); // NOI18N
                for (int j = 0; j < annotationArgList.getLength(); j++) {
                    Element curAnnotationArg = (Element) annotationArgList.item(j);
                    String annotationArgName = curAnnotationArg.getAttribute("name"); // NOI18N
                    String annotationArgValue = curAnnotationArg.getAttribute("value"); // NOI18N
                    annotationArgValue = annotationArgValue.replaceAll("\\$\\{CLASSNAME\\}", fullyQualifiedClassName);
                    AssignmentTree assignmentTree = make.Assignment(
                            make.Identifier(annotationArgName),
                            make.Literal(annotationArgValue));
                    annoArgList.add(assignmentTree);
                }
            }
            AnnotationTree annotationTree = GeneratorUtil.createAnnotation(make, workingCopy,
                    type, annoArgList);
            annotationTreeList.add(annotationTree);
        }

        return make.Modifiers(modifierSet, annotationTreeList);
    }

    private List<VariableTree> handleParams(NodeList params,
            WorkingCopy workingCopy) throws Exception {
        TreeMaker make = workingCopy.getTreeMaker();
        List<VariableTree> paramList = new ArrayList<VariableTree> ();
        for (int i = 0; i < params.getLength(); i++) {
            Element param = (Element) params.item(i);
            String type = filterText(param.getAttribute("type")); // NOI18N
            String name = filterText(param.getAttribute("name")); // NOI18N
            paramList.add(make.Variable(
                    make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                    name,
                    GeneratorUtil.createQualIdent(make, workingCopy, type),
                    null)
            );
        }

        return paramList;
    }

    private List<ExpressionTree> handleThrows(NodeList nodeList,
            WorkingCopy workingCopy) throws Exception {
        TreeMaker make = workingCopy.getTreeMaker();
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree> ();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element throwsElem = (Element) nodeList.item(i);
            String type = throwsElem.getAttribute("type"); // NOI18N
            throwsList.add(GeneratorUtil.createQualIdent(make, workingCopy, type));
        }

        return throwsList;
    }

    /**
     * the logic is: if more than one "body" elements exists, then need to match
     * the "otdType" attribute with user selected otdType, else just return the
     * contents of the first "body" element.
     *
     * @param bodyList NodeList of "body" elements
     * @return
     */
    private String getMethodBodyCDATAText(NodeList bodyList) {
        if (bodyList.getLength() < 1) {
            return ""; // NOI18N
        }
        if (bodyList.getLength() > 1) {
            for (int i = 0; i < bodyList.getLength(); i++) {
                Element bodyElem = (Element) bodyList.item(i);
                if (otdType.equals(bodyElem.getAttribute("otdType"))) { // NOI18N
                    NodeList children = bodyElem.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        Node child = children.item(j);
                        if (child instanceof CDATASection) {
                            return ((CDATASection) child).getData();
                        }
                    }
                }
            }
        } else {
            NodeList children = bodyList.item(0).getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof CDATASection) {
                    return ((CDATASection) child).getData();
                }
            }
        }
        return ""; // NOI18N
    }

    private String filterText(String input) {
        return input.replaceAll("\\$\\{BUSINESS_RULE\\}", businessRule). // NOI18N
                replaceAll("\\$\\{LOCAL_VAR_NAME\\}", localVarName). // NOI18N
                replaceAll("\\$\\{OTD_TYPE\\}", otdType); // NOI18N
    }

    private String applyAdditionalConfig(String input) {
        String s = input;
        if (additionalConfig == null) {
            return s;
        }
        for (Enumeration e = additionalConfig.propertyNames(); e.hasMoreElements(); ) {
            String propName = (String) e.nextElement();
            String propValue = additionalConfig.getProperty(propName);
            s = s.replaceAll("\\$\\{" + propName + "\\}", propValue); // NOI18N
        }
        return s;
    }

    private void generateMethodInvocation(WorkingCopy workingCopy, String ejbContextVarName) throws Exception {
        MethodTree enclosingMethodTree = getEnclosingMethod(workingCopy);
        if (enclosingMethodTree == null) {
            return;
        }

        BlockTree body = enclosingMethodTree.getBody();

        TreeMaker make = workingCopy.getTreeMaker();
        List<ExpressionTree> paramNameList = new ArrayList<ExpressionTree> ();
        for (VariableTree var : enclosingMethodTree.getParameters()) {
            paramNameList.add(make.Identifier(var.getName()));
        }

        String invokeMethodName = "_invoke_" + businessRule; // NOI18N

        BlockTree tryBlockTree = make.Block(
                Collections.singletonList(make.ExpressionStatement(make.MethodInvocation(
                    Collections.EMPTY_LIST,
                    make.Identifier(invokeMethodName),
                    paramNameList))),
                false);

        List<StatementTree> statementTreeList = new ArrayList<StatementTree> ();

        if (rollbackTx) {
            String rollbackStatement = ejbContextVarName + ".setRollbackOnly();"; // NOI18N
            StatementTree rollbackStatementTree = workingCopy.getTreeUtilities().parseStatement(rollbackStatement, null);
            statementTreeList.add(rollbackStatementTree);
        }
        if (logException) {
            String loggingStatement =
                    "java.util.logging.Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.WARNING, \"Failed to invoke " + invokeMethodName + ": \" + t, t);"; // NOI18N
            StatementTree loggingStatementTree = workingCopy.getTreeUtilities().parseStatement(loggingStatement, null);
            statementTreeList.add(loggingStatementTree);
        }
        if (rethrowException) {
            String rethrowStatement = "throw t;"; // NOI18N
            StatementTree rethrowStatementTree = workingCopy.getTreeUtilities().parseStatement(rethrowStatement, null);
            statementTreeList.add(rethrowStatementTree);
        }

        BlockTree catchBlockTree = make.Block(statementTreeList, false);

        CatchTree catchTree = make.Catch(
                make.Variable(
                    make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                    "t", // NOI18N
                    GeneratorUtil.createQualIdent(make, workingCopy, "java.lang.Throwable"), // NOI18N
                    null),
                catchBlockTree);

        TryTree tryTree = make.Try(tryBlockTree, Collections.singletonList(catchTree), null);

        int index = body.getStatements().size();
        if (index > 0) {
            StatementTree lastStatement = body.getStatements().get(index - 1);
            if (lastStatement.getKind() == Tree.Kind.RETURN) {
                index--;
            }
        }
        BlockTree newBody = make.insertBlockStatement(body, index, tryTree);
        workingCopy.rewrite(body, newBody);
    }

    private MethodTree getEnclosingMethod(WorkingCopy workingCopy) {
        Scope scope = workingCopy.getTreeUtilities().scopeFor(target.getCaretPosition());
        if (scope == null) {
            return null;
        }
        ExecutableElement enclosingMethod = scope.getEnclosingMethod();
        if (enclosingMethod == null) {
            return null;
        }
        return workingCopy.getTrees().getTree(enclosingMethod);
    }

    private ModifiersTree handleModifiersAndAnnotations(TreeMaker make, WorkingCopy workingCopy,
            String annotationType, Map<String, Object> annotationArguments) throws Exception {
        Set<Modifier> modifierSet = EnumSet.of(Modifier.PRIVATE);

        List<ExpressionTree> annoArgList = new ArrayList<ExpressionTree> ();
        if (annotationArguments != null) {
            for (String key : annotationArguments.keySet()) {
                Object val = annotationArguments.get(key);
                ExpressionTree annoArg = GeneratorUtil.createAnnotationArgument(make, key, val);
                annoArgList.add(annoArg);
            }
        }
        AnnotationTree annotationTree = GeneratorUtil.createAnnotation(make, workingCopy,
                    annotationType, annoArgList);

        return make.Modifiers(modifierSet, Collections.singletonList(annotationTree));
    }

    private boolean hasType(ClassTree classTree, String type) {
        for (Tree t : classTree.getMembers()) {
            if (t.getKind() == Tree.Kind.CLASS) {
                ClassTree ct = (ClassTree) t;
                if (ct.getSimpleName().contentEquals(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasVariable(ClassTree classTree, String varName) {
        for (Tree t : classTree.getMembers()) {
            if (t.getKind() == Tree.Kind.VARIABLE) {
                VariableTree vt = (VariableTree) t;
                if (vt.getName().contentEquals(varName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
