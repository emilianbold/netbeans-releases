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
package org.netbeans.modules.web.wizards;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;

/**
 * Generator for servlet listener class
 *
 * @author  milan.kuchtiak@sun.com
 * Created on March, 2004
 */
public class ListenerGenerator {
    boolean isContext,isContextAttr,isSession,isSessionAttr,isRequest,isRequestAttr;
    
    private JavaSource clazz;
    private GenerationUtils gu;
    
    /** Creates a new instance of ListenerGenerator */
    public ListenerGenerator(boolean isContext, boolean isContextAttr, boolean isSession, boolean isSessionAttr,
                             boolean isRequest, boolean isRequestAttr) {
        this.isContext=isContext;
        this.isContextAttr=isContextAttr;
        this.isSession=isSession;
        this.isSessionAttr=isSessionAttr;
        this.isRequest=isRequest;
        this.isRequestAttr=isRequestAttr;
    }
    
    public void generate(JavaSource clazz) throws IOException {
        this.clazz=clazz;
        
        AbstractTask task = new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        gu = GenerationUtils.newInstance(workingCopy);
                        workingCopy.rewrite(gu.getClassTree(), generateInterfaces(gu));

                    }
                }
                
            }
        };
        ModificationResult result = clazz.runModificationTask(task);
        result.commit();

        
//        if (isContext) addContextListenerMethods();
//        if (isContextAttr) addContextAttrListenerMethods();
//        if (isSession) addSessionListenerMethods();
//        if (isSessionAttr) addSessionAttrListenerMethods();
//        if (isRequest) addRequestListenerMethods();
//        if (isRequestAttr) addRequestAttrListenerMethods();
    }
    
    private ClassTree generateInterfaces(GenerationUtils gu) {
        ClassTree newClassTree = gu.getClassTree();
        
        if (isContext)
            newClassTree = gu.addImplementsClause(newClassTree, "javax.servlet.ServletContextListener");
        if (isContextAttr)
            newClassTree = gu.addImplementsClause(newClassTree, "javax.servlet.ServletContextAttributeListener");
        if (isSession)
            newClassTree = gu.addImplementsClause(newClassTree, "javax.servlet.http.HttpSessionListener");
        if (isSessionAttr)
            newClassTree = gu.addImplementsClause(newClassTree, "javax.servlet.http.HttpSessionAttributeListener");
        if (isRequest)
            newClassTree = gu.addImplementsClause(newClassTree, "javax.servlet.ServletRequestListener");
        if (isRequestAttr)
            newClassTree = gu.addImplementsClause(newClassTree, "javax.servlet.ServletRequestAttributeListener");
        
        return newClassTree;
    }
    
//    private void addContextListenerMethods() throws SourceException {
//        StringBuffer docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_contextListener_m1"));
//        StringBuffer bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo_eg")); //NOI18N
//        bodyBuf.append("\n/*\n"); //NOI18N
//        bodyBuf.append("    Connection con = // create connection\n"); //NOI18N
//        bodyBuf.append("    evt.getServletContext().setAttribute(\"con\", con);\n"); //NOI18N
//        bodyBuf.append("*/\n"); //NOI18N
//        addMethod("contextInitialized","ServletContextEvent",docBuf,bodyBuf); //NOI18N
// 
//        docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_contextListener_m2"));
//        bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo_eg")); //NOI18N
//        bodyBuf.append("\n/*\n"); //NOI18N
//        bodyBuf.append("        Connection con = (Connection) e.getServletContext().getAttribute(\"con\");\n"); //NOI18N
//        bodyBuf.append("        try { con.close(); } catch (SQLException ignored) { } // close connection\n"); //NOI18N
//        bodyBuf.append("*/\n"); //NOI18N
//        addMethod("contextDestroyed","ServletContextEvent",docBuf,bodyBuf); //NOI18N
//    }
//    private void addContextAttrListenerMethods() throws SourceException {
//        StringBuffer docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_contextAttrListener_m1"));
//        StringBuffer bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("attributeAdded","ServletContextAttributeEvent",docBuf,bodyBuf); //NOI18N
//        
//        docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_contextAttrListener_m2"));
//        bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("attributeRemoved","ServletContextAttributeEvent",docBuf,bodyBuf); //NOI18N
//        
//        docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_contextAttrListener_m3"));
//        bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("attributeReplaced","ServletContextAttributeEvent",docBuf,bodyBuf); //NOI18N
//    }
//    private void addSessionListenerMethods() throws SourceException {
//        StringBuffer docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_sessionListener_m1"));
//        StringBuffer bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("sessionCreated","HttpSessionEvent",docBuf,bodyBuf); //NOI18N
// 
//        docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_sessionListener_m2"));
//        bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("sessionDestroyed","HttpSessionEvent",docBuf,bodyBuf); //NOI18N
//    }
//    private void addSessionAttrListenerMethods() throws SourceException {
//        StringBuffer docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_sessionAttrListener_m1"));
//        StringBuffer bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("attributeAdded","HttpSessionBindingEvent",docBuf,bodyBuf); //NOI18N
//        
//        docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_sessionAttrListener_m2"));
//        bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("attributeRemoved","HttpSessionBindingEvent",docBuf,bodyBuf); //NOI18N
//        
//        docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_sessionAttrListener_m3"));
//        bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("attributeReplaced","HttpSessionBindingEvent",docBuf,bodyBuf); //NOI18N
//    }
//    private void addRequestListenerMethods() throws SourceException {
//        StringBuffer docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_requestListener_m1"));
//        StringBuffer bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("requestInitialized","ServletRequestEvent",docBuf,bodyBuf); //NOI18N
// 
//        docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_requestListener_m2"));
//        bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("requestDestroyed","ServletRequestEvent",docBuf,bodyBuf); //NOI18N
//    }
//    private void addRequestAttrListenerMethods() throws SourceException {
//        StringBuffer docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_requestAttrListener_m1"));
//        StringBuffer bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("attributeAdded","ServletRequestAttributeEvent",docBuf,bodyBuf); //NOI18N
//        
//        docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_requestAttrListener_m2"));
//        bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("attributeRemoved","ServletRequestAttributeEvent",docBuf,bodyBuf); //NOI18N
//        
//        docBuf = new StringBuffer();
//        docBuf.append(NbBundle.getMessage(ListenerGenerator.class,"TXT_DOC_requestAttrListener_m3"));
//        bodyBuf = new StringBuffer();
//        bodyBuf.append("// "+NbBundle.getMessage(ListenerGenerator.class,"TXT_todo")+"\n"); //NOI18N
//        addMethod("attributeReplaced","ServletRequestAttributeEvent",docBuf,bodyBuf); //NOI18N
//    }
//    private void addMethod(String methodName, 
//                            String eventName,
//                            StringBuffer javadoc,
//                            StringBuffer body) throws SourceException {
//        MethodElement method = new MethodElement();
//        method.setName(Identifier.create(methodName));
//        method.setReturn(Type.VOID);
//        method.setModifiers(java.lang.reflect.Modifier.PUBLIC);
//        MethodParameter mp = MethodParameter.parse(eventName+" evt"); //NOI18N
//        method.setParameters(new MethodParameter[]{mp});       
//        JavaDoc doc = method.getJavaDoc();
//        doc.setRawText(javadoc.toString());
//        method.setBody(body.toString());
//        clazz.addMethod(method);
//    }
}