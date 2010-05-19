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

package org.netbeans.modules.soa.jca.jms;

import org.netbeans.modules.soa.jca.base.generator.api.GeneratorUtil;
import org.netbeans.modules.soa.jca.base.generator.api.ModificationTask;
import org.netbeans.modules.soa.jca.jms.ui.RequestReplyPanel;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author echou
 */
public class RequestReplyTask<T extends WorkingCopy> extends ModificationTask<WorkingCopy> {

    public static final String BEGIN_EDITOR_FOLD =
            "<editor-fold defaultstate=\"collapsed\" desc=\"Generated JCA support code. Click on the + sign on the left to edit the code.\">"; // NOI18N
    public static final String END_EDITOR_FOLD = "</editor-fold>"; // NOI18N

    public static final String REQUEST_REPLY_HELPER_METHOD_NAME = "_requestReply_Helper"; // NOI18N

    private Exception myException = null;

    private ElementHandle methodHandle;
    private String requestMsgVariableName;
    private String connectionFactoryJndiName;
    private String connectionFactoryVariableName;
    private String requestDestinationVariableName;
    private long timeout;


    public RequestReplyTask(final RequestReplyPanel panel) {
        this.methodHandle = panel.getMethodHandle();
        this.requestMsgVariableName = panel.getRequestMessageVariableName();
        this.connectionFactoryJndiName = panel.getConnectionFactoryVariable().jndiName;
        if (panel.getConnectionFactoryVariable().variable != null) {
            this.connectionFactoryVariableName = panel.getConnectionFactoryVariable().variable.getSimpleName().toString();
        }
        this.requestDestinationVariableName = panel.getRequestDestinationVariableName();
        this.timeout = panel.getReplyTimeout();
    }

    @Override
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

            TreeMaker make = workingCopy.getTreeMaker();
            Trees trees = workingCopy.getTrees();
            TreeUtilities treeUtilities = workingCopy.getTreeUtilities();

            ClassTree newClassTree = classTree;
            /*
            if (!hasJMSSendNewTxInterface(treeUtilities, classTree)) {
                // generate new ejb interface
                newClassTree = addInterface(make, workingCopy, newClassTree);
            }

            if (!hasJMSSendNewTxImplClass(classTree)) {
                // generate new ejb class
                newClassTree = addClass(make, workingCopy, newClassTree, fullyQualifiedClassName);
            }

            if (!hasJMSSendNewTxVariable(classTree)) {
                // generate new ejb variable
                newClassTree = addVariable(make, workingCopy, newClassTree, fullyQualifiedClassName);
            }
             */

            if (connectionFactoryVariableName == null) {
                newClassTree = addNewVariableForConnectionFactory(make, workingCopy, newClassTree);
            }

            if (!hasRequestReplyHelperMethod(classTree)) {
                // generate new helper method
                newClassTree = addRequestReplyHelperMethod(make, workingCopy, newClassTree);
            }

            MethodTree methodTree = (MethodTree) trees.getTree(methodHandle.resolve(workingCopy));
            appendStatementsToMethod(make, workingCopy, treeUtilities, methodTree);

            workingCopy.rewrite(classTree, newClassTree);
        } catch (Exception e) {
            myException = e;
            throw e;
        }
    }

    /*
    private ClassTree addInterface(TreeMaker make, WorkingCopy workingCopy, ClassTree classTree) throws Exception {
        Set<Modifier> modifierSet = EnumSet.of(Modifier.PUBLIC);
        AnnotationTree annotationTree = GeneratorUtil.createAnnotation(make, workingCopy,
                    "javax.ejb.Local", Collections.EMPTY_LIST);
        ModifiersTree modifiers = make.Modifiers(modifierSet, Collections.singletonList(annotationTree));

        List<VariableTree> params = new ArrayList<VariableTree> ();
        params.add(make.Variable(
                make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                "message",
                make.Identifier("javax.jms.Message"),
                null)
        );
        params.add(make.Variable(
                make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                "dest",
                make.Identifier("javax.jms.Destination"),
                null)
        );
        params.add(make.Variable(
                make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                "connFact",
                make.Identifier("javax.jms.ConnectionFactory"),
                null)
        );
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree> ();
        throwsList.add(make.Identifier("javax.jms.JMSException"));
        MethodTree methodTree = make.Method(
            make.Modifiers(modifierSet, Collections.EMPTY_LIST), // modifiers and annotations
            "send", // name
            GeneratorUtil.createType(make, workingCopy, "void"), // return type
            Collections.EMPTY_LIST, // type parameters for parameters
            params, // parameters
            throwsList, // throws
            (BlockTree) null, // body
            null // default value - not applicable here, used by annotations
        );

        ClassTree interfaceTree = make.Interface(
                modifiers,
                "JMSSendNewTx",
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.<Tree>singletonList(methodTree)
        );

        make.addComment(interfaceTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, BEGIN_EDITOR_FOLD), true);
        make.addComment(interfaceTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, END_EDITOR_FOLD), false);


        return make.addClassMember(classTree, interfaceTree);
    }
     */

    /*
    private ClassTree addClass(TreeMaker make, WorkingCopy workingCopy, ClassTree classTree, String fullyQualifiedClassName) throws Exception {
        Set<Modifier> modifierSet = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC);
        AnnotationTree statelessAnnotation = GeneratorUtil.createAnnotation(make, workingCopy,
                    "javax.ejb.Stateless",
                    Collections.<ExpressionTree>singletonList(make.Assignment(
                        make.Identifier("name"),
                        make.Literal(fullyQualifiedClassName + "$JMSSendNewTx"))
                    )
        );
        AnnotationTree txManagementAnnotation = GeneratorUtil.createAnnotation(make, workingCopy,
                    "javax.ejb.TransactionManagement",
                    Collections.<ExpressionTree>singletonList(make.Assignment(
                        make.Identifier("value"),
                        make.Identifier("javax.ejb.TransactionManagementType.CONTAINER"))
                    )
        );
        AnnotationTree txAttributeAnnotation = GeneratorUtil.createAnnotation(make, workingCopy,
                    "javax.ejb.TransactionAttribute",
                    Collections.<ExpressionTree>singletonList(make.Assignment(
                        make.Identifier("value"),
                        make.Identifier("javax.ejb.TransactionAttributeType.REQUIRES_NEW"))
                    )
        );
        List<AnnotationTree> annotationList = new ArrayList<AnnotationTree> ();
        annotationList.add(statelessAnnotation);
        annotationList.add(txManagementAnnotation);
        annotationList.add(txAttributeAnnotation);
        ModifiersTree modifiers = make.Modifiers(modifierSet, annotationList);

        List<VariableTree> params = new ArrayList<VariableTree> ();
        params.add(make.Variable(
                make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                "message",
                make.Identifier("javax.jms.Message"),
                null)
        );
        params.add(make.Variable(
                make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                "dest",
                make.Identifier("javax.jms.Destination"),
                null)
        );
        params.add(make.Variable(
                make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                "connFact",
                make.Identifier("javax.jms.ConnectionFactory"),
                null)
        );
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree> ();
        throwsList.add(make.Identifier("javax.jms.JMSException"));

        StringBuilder body = new StringBuilder();
        body.append("{\n");
        body.append("javax.jms.Connection conn = null;\n");
        body.append("try {\n");
        body.append("    conn = connFact.createConnection();\n");
        body.append("    javax.jms.Session session = conn.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);\n");
        body.append("    session.createProducer(dest).send(message);\n");
        body.append("} finally {\n");
        body.append("    try {\n");
        body.append("        if (conn != null) {\n");
        body.append("            conn.close();\n");
        body.append("        }\n");
        body.append("    } catch (Exception ignore) {\n");
        body.append("    }\n");
        body.append("}\n");
        body.append("}");

        MethodTree methodTree = make.Method(
            make.Modifiers(EnumSet.of(Modifier.PUBLIC), Collections.EMPTY_LIST), // modifiers and annotations
            "send", // name
            GeneratorUtil.createType(make, workingCopy, "void"), // return type
            Collections.EMPTY_LIST, // type parameters for parameters
            params, // parameters
            throwsList, // throws
            body.toString(), // body
            null // default value - not applicable here, used by annotations
        );

        ClassTree implClassTree = make.Class(
                modifiers, // modifiers
                "JMSSendNewTxImpl", // simpleName
                Collections.EMPTY_LIST, // typeParameters
                null, // extendsClause
                Collections.<Tree>singletonList(make.Identifier("JMSSendNewTx")), // implementsClauses
                Collections.<Tree>singletonList(methodTree) // memberDecls
        );

        make.addComment(implClassTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, BEGIN_EDITOR_FOLD), true);
        make.addComment(implClassTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, END_EDITOR_FOLD), false);

        return make.addClassMember(classTree, implClassTree);
    }
     */

    /*
    private ClassTree addVariable(TreeMaker make, WorkingCopy workingCopy, ClassTree classTree, String fullyQualifiedClassName) throws Exception {
        Set<Modifier> modifierSet = EnumSet.of(Modifier.PRIVATE);
        AnnotationTree annotationTree = GeneratorUtil.createAnnotation(make, workingCopy,
                    "javax.ejb.EJB",
                    Collections.<ExpressionTree>singletonList(make.Assignment(
                        make.Identifier("name"),
                        make.Literal(fullyQualifiedClassName + "$JMSSendNewTx"))
                    )
        );
        ModifiersTree modifiers = make.Modifiers(modifierSet, Collections.singletonList(annotationTree));
        VariableTree variableTree = GeneratorUtil.createField(make, workingCopy,
            modifiers,
            "requestReplyEJB",
            "JMSSendNewTx",
            null
        );

        make.addComment(variableTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, BEGIN_EDITOR_FOLD), true);
        make.addComment(variableTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, END_EDITOR_FOLD), false);

        return make.addClassMember(classTree, variableTree);
    }
     */

    private ClassTree addNewVariableForConnectionFactory(TreeMaker make, WorkingCopy workingCopy, ClassTree classTree) throws Exception {
        Set<Modifier> modifierSet = EnumSet.of(Modifier.PRIVATE);
        AnnotationTree annotationTree = GeneratorUtil.createAnnotation(make, workingCopy,
                    "javax.annotation.Resource",
                    Collections.<ExpressionTree>singletonList(make.Assignment(
                        make.Identifier("name"),
                        make.Literal(this.connectionFactoryJndiName))
                    )
        );
        ModifiersTree modifiers = make.Modifiers(modifierSet, Collections.singletonList(annotationTree));

        String variableName = "_notx_jms_connfact";
        int i = 2;
        while (hasVariable(classTree, variableName)) {
            variableName = variableName + i;
            i++;
        }
        this.connectionFactoryVariableName = variableName;
        VariableTree variableTree = GeneratorUtil.createField(make, workingCopy,
            modifiers,
            variableName,
            "javax.jms.ConnectionFactory",
            null
        );

        make.addComment(variableTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, BEGIN_EDITOR_FOLD), true);
        make.addComment(variableTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, END_EDITOR_FOLD), false);

        return make.addClassMember(classTree, variableTree);
    }

    private ClassTree addRequestReplyHelperMethod(TreeMaker make, WorkingCopy workingCopy, ClassTree classTree) throws Exception {
        List<VariableTree> params = new ArrayList<VariableTree> ();
        params.add(make.Variable(
                make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                "requestMsg",
                make.Identifier("javax.jms.Message"),
                null)
        );
        params.add(make.Variable(
                make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                "requestDestination",
                make.Identifier("javax.jms.Destination"),
                null)
        );
        params.add(make.Variable(
                make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                "notxFactory",
                make.Identifier("javax.jms.ConnectionFactory"),
                null)
        );
        params.add(make.Variable(
                make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                "timeout",
                make.Identifier("long"),
                null)
        );
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree> ();
        throwsList.add(make.Identifier("javax.jms.JMSException"));

        StringBuilder body = new StringBuilder();
        body.append("{\n");
        body.append("Message replyMsg = null;\n");
        body.append("javax.jms.Connection conn = null;\n");
        body.append("javax.jms.Session session = null;\n");
        body.append("javax.jms.MessageConsumer consumer = null;\n");
        body.append("try {\n");
        body.append("    conn = notxFactory.createConnection();\n");
        body.append("    conn.start();\n");
        body.append("    session = conn.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);\n");
        body.append("    javax.jms.Destination replyDestination = (requestDestination instanceof javax.jms.Queue) ? session.createTemporaryQueue() : session.createTemporaryTopic();\n");
        body.append("    requestMsg.setJMSReplyTo(replyDestination);\n");
        //body.append("    String msgSelector = (requestMsg.getJMSCorrelationID() == null) ? null : \"JMSCorrelationID = '\" + requestMsg.getJMSCorrelationID() + \"'\";\n");
        body.append("    consumer = session.createConsumer(replyDestination);\n");
        body.append("    session.createProducer(requestDestination).send(requestMsg);\n");
        body.append("    replyMsg = consumer.receive(timeout);\n");
        body.append("    consumer.close();\n");
        body.append("    session.close();\n");
        body.append("} finally {\n");
        body.append("    if (consumer != null) {\n");
        body.append("        try {\n");
        body.append("            consumer.close();\n");
        body.append("        } catch (Exception e) {\n");
        body.append("            // ignore\n");
        body.append("        }\n");
        body.append("    }\n");
        body.append("    if (session != null) {\n");
        body.append("        try {\n");
        body.append("            session.close();\n");
        body.append("        } catch (Exception e) {\n");
        body.append("            // ignore\n");
        body.append("        }\n");
        body.append("    }\n");
        body.append("    if (conn != null) {\n");
        body.append("        try {\n");
        body.append("            conn.close();\n");
        body.append("        } catch (Exception e) {\n");
        body.append("            // ignore\n");
        body.append("        }\n");
        body.append("    }\n");
        body.append("}\n");
        body.append("return replyMsg;\n");
        body.append("}");

        MethodTree methodTree = make.Method(
            make.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.STATIC), Collections.EMPTY_LIST), // modifiers and annotations
            REQUEST_REPLY_HELPER_METHOD_NAME, // name
            GeneratorUtil.createType(make, workingCopy, "javax.jms.Message"), // return type
            Collections.EMPTY_LIST, // type parameters for parameters
            params, // parameters
            throwsList, // throws
            body.toString(), // body
            null // default value - not applicable here, used by annotations
        );

        make.addComment(methodTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, BEGIN_EDITOR_FOLD), true);
        make.addComment(methodTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, END_EDITOR_FOLD), false);

        return make.addClassMember(classTree, methodTree);
    }

    private void appendStatementsToMethod(TreeMaker make, WorkingCopy workingCopy, TreeUtilities treeUtilities, MethodTree methodTree) throws Exception {
        String statementToAdd =
                "{ " +
                "javax.jms.Message replyMessage = _requestReply_Helper(" +
                this.requestMsgVariableName + ", " +
                this.requestDestinationVariableName + ", " +
                this.connectionFactoryVariableName + ", " +
                this.timeout + ");" +
                " }";
        BlockTree newBlockTree = treeUtilities.parseStaticBlock(statementToAdd, null);
        StatementTree statementTree = newBlockTree.getStatements().get(0);

        /*
        String comment1 = "For JMS Request-Reply pattern, user can optionally set";
        String comment2 = "CorrelationID on the Request Message, and then use";
        String comment3 = "Message Selector to selectively receive Reply Message with";
        String comment4 = "corresponding CorrelationID.  Don't forget on the Replier";
        String comment5 = "side to set CorrelationID on the Reply Message.";
        String comment6 = "For example:";
        String comment7 = this.requestMsgVariableName + ".setJMSCorrelationID(\"some CorrelationID\");";

        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment1), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment2), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment3), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment4), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment5), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment6), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment7), true);
        */
        BlockTree oldBodyTree = methodTree.getBody();
        List<StatementTree> statementList = new ArrayList<StatementTree> ();
        statementList.addAll(oldBodyTree.getStatements());
        statementList.add(statementTree);
        BlockTree newBodyTree = make.Block(statementList, false);

        workingCopy.rewrite(oldBodyTree, newBodyTree);
    }

    /*
    private void appendStatementsToMethod(TreeMaker make, WorkingCopy workingCopy, TreeUtilities treeUtilities, MethodTree methodTree) throws Exception {
        StringBuilder blockStatements = new StringBuilder();
        blockStatements.append("{\n");
        if (requestReplyType.equals("Queue")) {
            blockStatements.append("javax.jms.TemporaryQueue " + replyMessageVariableName + "TempDestination = " + sessionVariableName + ".createTemporaryQueue();\n");
        } else {
            blockStatements.append("javax.jms.TemporaryTopic " + replyMessageVariableName + "TempDestination = " + sessionVariableName + ".createTemporaryTopic();\n");
        }

        if (requestMessageType.equals("javax.jms.TextMessage")) {
            blockStatements.append("javax.jms.TextMessage " + replyMessageVariableName + "RequestMsg = " + sessionVariableName + ".createTextMessage();\n");
        } else if (requestMessageType.equals("javax.jms.BytesMessage")) {
            blockStatements.append("javax.jms.BytesMessage " + replyMessageVariableName + "RequestMsg = " + sessionVariableName + ".createBytesMessage();\n");
        } else if (requestMessageType.equals("javax.jms.MapMessage")) {
            blockStatements.append("javax.jms.MapMessage " + replyMessageVariableName + "RequestMsg = " + sessionVariableName + ".createMapMessage();\n");
        } else if (requestMessageType.equals("javax.jms.ObjectMessage")) {
            blockStatements.append("javax.jms.ObjectMessage " + replyMessageVariableName + "RequestMsg = " + sessionVariableName + ".createObjectMessage();\n");
        } else {
            blockStatements.append("javax.jms.StreamMessage " + replyMessageVariableName + "RequestMsg = " + sessionVariableName + ".createStreamMessage();\n");
        }

        blockStatements.append(replyMessageVariableName + "RequestMsg.setJMSReplyTo(" + replyMessageVariableName  + "TempDestination);\n");
        blockStatements.append("javax.jms.MessageConsumer " + replyMessageVariableName + "Consumer = " + sessionVariableName + ".createConsumer(" + replyMessageVariableName + "TempDestination);\n");
        blockStatements.append("jmsOTDHelperEJB.sendNewTx(" + replyMessageVariableName + "RequestMsg, " + requestDestinationVariableName +  ", " + connectionFactoryVariableName + ");\n");
        blockStatements.append("javax.jms.Message " + replyMessageVariableName + " = " + replyMessageVariableName + "Consumer.receive(" + timeout + ");\n");
        blockStatements.append("}");

        BlockTree newBlockTree = treeUtilities.parseStaticBlock(blockStatements.toString(), null);
        make.addComment(newBlockTree.getStatements().get(2),
                Comment.create(Comment.Style.LINE, -2, -2, -2, "TODO: set JMS request message payload here"), true);

        BlockTree oldBodyTree = methodTree.getBody();
        List<StatementTree> statementList = new ArrayList<StatementTree> ();
        statementList.addAll(oldBodyTree.getStatements());
        statementList.addAll(newBlockTree.getStatements());
        BlockTree newBodyTree = make.Block(statementList, false);

        workingCopy.rewrite(oldBodyTree, newBodyTree);
    }
     */

    /*
    private boolean hasJMSSendNewTxInterface(TreeUtilities treeUtilities, ClassTree classTree) {
        for (Tree t : classTree.getMembers()) {
            if (t.getKind() == Tree.Kind.CLASS) {
                ClassTree ct = (ClassTree) t;
                if (treeUtilities.isInterface(ct) && ct.getSimpleName().contentEquals("JMSSendNewTx")) {
                    return true;
                }
            }
        }
        return false;
    }
     */

    /*
    private boolean hasJMSSendNewTxImplClass(ClassTree classTree) {
        for (Tree t : classTree.getMembers()) {
            if (t.getKind() == Tree.Kind.CLASS) {
                ClassTree ct = (ClassTree) t;
                if (ct.getSimpleName().contentEquals("JMSSendNewTxImpl")) {
                    return true;
                }
            }
        }
        return false;
    }
     */

    /*
    private boolean hasJMSSendNewTxVariable(ClassTree classTree) {
        for (Tree t : classTree.getMembers()) {
            if (t.getKind() == Tree.Kind.VARIABLE) {
                VariableTree vt = (VariableTree) t;
                if (vt.getName().contentEquals("requestReplyEJB")) {
                    return true;
                }
            }
        }
        return false;
    }
     */

    private boolean hasRequestReplyHelperMethod(ClassTree classTree) {
        for (Tree t : classTree.getMembers()) {
            if (t.getKind() == Tree.Kind.METHOD) {
                MethodTree mt = (MethodTree) t;
                if (mt.getName().contentEquals(REQUEST_REPLY_HELPER_METHOD_NAME)) {
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
