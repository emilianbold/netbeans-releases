/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.jmx.managerwizard;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.List;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.jmx.JavaModelHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.WizardHelpers;

/**
 *
 *  Wizard Agent code generator class
 */
public class ManagerGenerator {
    /**
     * Entry point to generate manager code.
     * @param wiz <CODE>WizardDescriptor</CODE> a wizard
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>CreationResults</CODE> results of manager creation
     */
    public Set generateManager(final WizardDescriptor wiz)
            throws java.io.IOException, Exception {
        FileObject createdFile = null;
        final String managerName = Templates.getTargetName(wiz);
        FileObject managerFolder = Templates.getTargetFolder(wiz);
        DataFolder managerFolderDataObj = DataFolder.findFolder(managerFolder);
        
        //==============================================
        // manager generation
        //==============================================
        DataObject managerDObj = null;
        
        FileObject template = Templates.getTemplate( wiz );
        DataObject dTemplate = DataObject.find( template );
        managerDObj = dTemplate.createFromTemplate(
                managerFolderDataObj, managerName );
        //Obtain an JavaSource - represents a java file
        JavaSource js = JavaSource.forFileObject(managerDObj.getPrimaryFile());
        Boolean mainMethodSelected = (Boolean)wiz.getProperty(
                WizardConstants.PROP_MANAGER_MAIN_METHOD_SELECTED);
        //Perform an action which changes the content of the java file
        js.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy w) throws Exception {
                //get information of the wizard descriptor
                Boolean sampleSelected = (Boolean)wiz.getProperty(
                        WizardConstants.PROP_MANAGER_SAMPLE_CODE_SELECTED);
                Boolean isSecurityChecked = (Boolean)wiz.getProperty(
                        WizardConstants.PROP_MANAGER_SECURITY_SELECTED);
                Boolean isUserCredential = (Boolean)wiz.getProperty(
                        WizardConstants.PROP_MANAGER_USER_CREDENTIAL_SELECTED);
                Boolean isSampleCredential = (Boolean)wiz.getProperty(
                        WizardConstants.PROP_MANAGER_CREDENTIAL_SAMPLE_SELECTED);
                String userName = (String)wiz.getProperty(
                        WizardConstants.PROP_MANAGER_USER_NAME);
                String userPassword = (String)wiz.getProperty(
                        WizardConstants.PROP_MANAGER_USER_PASSWORD);
                String url = (String)wiz.getProperty(
                        WizardConstants.PROP_MANAGER_AGENT_URL);
                w.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                //Visitor for scanning javac's trees
                TemplateTransformer transformer = new TemplateTransformer(w,
                        managerName,
                        sampleSelected == null ? false : sampleSelected,
                        isSecurityChecked == null ? false : isSecurityChecked,
                        isSampleCredential == null ? false : isSampleCredential,
                        userName, userPassword, url,
                        isUserCredential == null ? false : isUserCredential);
                //execute the visitor on the root (CompilationUnitTree) with no parameter (null)
                transformer.scan(new TreePath(w.getCompilationUnit()), null);
            }
            
            public void cancel() {
                //Not important for userActionTasks
            }
        }).commit();    //Commit the changes into document
        
        if(!mainMethodSelected)
            JavaModelHelper.removeMethod(js, "main");// NOI18N
        
        WizardHelpers.save(managerDObj);
        Set set = new HashSet();
        set.add(managerDObj.getPrimaryFile());
        return set;
    }
    
    private static class TemplateTransformer extends TreePathScanner<Void,Object> {
        
        private final String className;
        private final WorkingCopy w;
        private final boolean sampleSelected;
        private final boolean isSecurityChecked;
        private final boolean isSampleCredential;
        private final String userName;
        private final String userPassword;
        private final String url;
        private final boolean isUserCredential;
        
        public TemplateTransformer(WorkingCopy w, String className,
                boolean sampleSelected, boolean isSecurityChecked,
                boolean isSampleCredential, String userName,
                String userPassword, String url, boolean isUserCredential) {
            assert className != null;
            assert w != null;
            this.className = className;
            this.w = w;
            this.sampleSelected = sampleSelected;
            this.isSecurityChecked = isSecurityChecked;
            this.isSampleCredential = isSampleCredential;
            this.userName = userName;
            this.userPassword = userPassword;
            this.url = url;
            this.isUserCredential = isUserCredential;
        }
        
        //Called for every method in the java source file
        @Override
        public Void visitMethod(MethodTree tree, Object p) {
            //Obtain the owner of this method, getCurrentPath() returns a path from root (CompilationUnitTree) to current node (tree)
            ClassTree owner = (ClassTree) getCurrentPath().getParentPath().getLeaf();
            //Is it the generated class, for the case when the template has more classes
            if (className.contentEquals(owner.getSimpleName())) {
                //is this method a main method
                //Get the Element for this method tree  - Trees.getElement(TreePath)
                ExecutableElement e = (ExecutableElement)w.getTrees().getElement(getCurrentPath());
                TreeMaker treeMaker = w.getTreeMaker();
                if(JavaModelHelper.isMain(e)) {
                    List<? extends StatementTree> statements = tree.getBody().getStatements();
                    List<StatementTree> newStatements = new ArrayList<StatementTree>(statements);
                    IdentifierTree managerField = w.getTreeMaker().Identifier("manager");// NOI18N
                    MemberSelectTree closeSelect = treeMaker.MemberSelect(managerField, "close");// NOI18N
                    MethodInvocationTree closeInvokation =
                            treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                            closeSelect,
                            Collections.<ExpressionTree>emptyList());
                    
                    ExpressionStatementTree t = treeMaker.ExpressionStatement(closeInvokation);
                    if (sampleSelected) {
                        JavaModelHelper.addCommentPrefixedByWhiteLine(treeMaker, t, Comment.Style.LINE, "SAMPLE MBEAN NAME DISCOVERY. Uncomment following code."); // NOI18N
                        JavaModelHelper.addCommentFollowedByWhiteLine(treeMaker, t, Comment.Style.BLOCK, "\n" +// NOI18N
                                " Set resultSet = \n" +// NOI18N
                                "    manager.getMBeanServerConnection().queryNames(null, null);\n" +// NOI18N
                                " for(Iterator i = resultSet.iterator(); i.hasNext();) {\n" +// NOI18N
                                "     System.out.println(\"MBean name: \" + i.next());\n" +// NOI18N
                                " }\n");// NOI18N
                    } else {
                        JavaModelHelper.addCommentSurroundedByWhiteLine(treeMaker, t, Comment.Style.LINE, " TODO add your Management Logic");// NOI18N
                    }
                    JavaModelHelper.addComment(treeMaker, t, Comment.Style.LINE, " Close connection"); // NOI18N
                    newStatements.add((StatementTree)t);
                    // Add System.out.println
                    IdentifierTree systemField = treeMaker.Identifier("System");// NOI18N
                    MemberSelectTree outSelect = treeMaker.MemberSelect(systemField, "out");// NOI18N
                    MemberSelectTree printlnSelect = treeMaker.MemberSelect(outSelect, "println");// NOI18N
                    LiteralTree msgValTree = treeMaker.Literal("Connection closed.");// NOI18N
                    List<ExpressionTree> params = new ArrayList<ExpressionTree>(1);
                    params.add(msgValTree);
                    MethodInvocationTree printlnInvokation =
                            treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                            printlnSelect,
                            params);
                    
                    ExpressionStatementTree t2 = treeMaker.ExpressionStatement(printlnInvokation);
                    newStatements.add((StatementTree)t2);
                    
                    BlockTree newBody = treeMaker.Block(newStatements, false);
                    w.rewrite(tree.getBody(), newBody);
                } else {
                    if(e.getSimpleName().toString().equals("connect")) {// NOI18N
                       // List<? extends StatementTree> statements = tree.getBody().getStatements();
                       // List<StatementTree> newStatements = new ArrayList<StatementTree>(statements);
                        List<StatementTree> newStatements = new ArrayList<StatementTree>();
                        
                        IdentifierTree connectorField = w.getTreeMaker().Identifier("connector");// NOI18N
                        MemberSelectTree getMBSCSelect = treeMaker.MemberSelect(connectorField, "getMBeanServerConnection");// NOI18N
                        MethodInvocationTree getMBSCInvokation =
                                treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                getMBSCSelect,
                                Collections.<ExpressionTree>emptyList());
                        
                        IdentifierTree mbscField = w.getTreeMaker().Identifier("mbsc");// NOI18N
                        AssignmentTree bt = treeMaker.Assignment(mbscField, getMBSCInvokation);
                        
                        ExpressionStatementTree t = treeMaker.ExpressionStatement(bt);
                        JavaModelHelper.addComment(treeMaker, t, Comment.Style.LINE, " Get the MBeanServerConnection"); // NOI18N
                        
                        fillURL(treeMaker, newStatements);
                        
                        fillUserCredentials(treeMaker, newStatements);

                        fillConnector(treeMaker, newStatements);
                        
                        newStatements.add((StatementTree)t);
                        
                        BlockTree newBody = treeMaker.Block(newStatements, false);
                        w.rewrite(tree.getBody(), newBody);
                    }
                }
            }
            super.visitMethod(tree, p);
            return null;
        }
        
        private void fillUserCredentials(TreeMaker treeMaker,
                List<StatementTree> newStatements) {
            if (isSecurityChecked && !isSampleCredential) {
                TypeElement hashMapClass =
                        w.getElements().getTypeElement("java.util.HashMap");// NOI18N
                ExpressionTree hashMapEx = treeMaker.QualIdent(hashMapClass);
                TypeElement mapClass =
                        w.getElements().getTypeElement("java.util.Map");// NOI18N
                ExpressionTree mapEx = treeMaker.QualIdent(mapClass);
                
                NewClassTree mapConstructor =
                        treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(),
                        hashMapEx, Collections.<ExpressionTree>emptyList(), null);
                VariableTree vt = treeMaker.Variable( treeMaker.Modifiers(
                        Collections.<Modifier>emptySet(),
                        Collections.<AnnotationTree>emptyList()
                        ), "env", mapEx, mapConstructor);// NOI18N
                
               newStatements.add(vt);
                
                List<ExpressionTree> putParams = new ArrayList<ExpressionTree>(2);
                
                IdentifierTree envField = treeMaker.Identifier("env");// NOI18N
                MemberSelectTree putSelect = treeMaker.MemberSelect(envField, "put");// NOI18N
                
                TypeElement connectorClass =
                        w.getElements().getTypeElement("javax.management.remote.JMXConnector");// NOI18N
                ExpressionTree connectorTree = treeMaker.QualIdent(connectorClass);
                
                MemberSelectTree credSelect = treeMaker.MemberSelect(connectorTree,
                        "CREDENTIALS");// NOI18N
                
                putParams.add(credSelect);
                
                TypeElement stringClass =
                        w.getElements().getTypeElement("java.lang.String");// NOI18N
                ExpressionTree stringEx = treeMaker.QualIdent(stringClass);
                LiteralTree user = treeMaker.Literal(userName);
                LiteralTree userP = treeMaker.Literal(userPassword);
                List<ExpressionTree> arrayInit = new ArrayList<ExpressionTree>(2);
                arrayInit.add(user);
                arrayInit.add(userP);
                NewArrayTree arrayTree =
                        treeMaker.NewArray(stringEx,
                        Collections.<ExpressionTree>emptyList(), arrayInit);
                putParams.add(arrayTree);
                
                MethodInvocationTree putInvokation =
                        treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                        putSelect,
                        putParams);
                
                ExpressionStatementTree t = treeMaker.ExpressionStatement(putInvokation);
                newStatements.add(t);
            }
        }
        
        private void fillURL(TreeMaker treeMaker,
                List<StatementTree> newStatements) {
            TypeElement urlClass =
                    w.getElements().getTypeElement("javax.management.remote.JMXServiceURL");// NOI18N
            ExpressionTree urlTree = treeMaker.QualIdent(urlClass);
            List<ExpressionTree> ctrParams = new ArrayList<ExpressionTree>(1);
            ctrParams.add(treeMaker.Literal(url));
            
            NewClassTree urlConstructor =
                    treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(),
                    urlTree, ctrParams, null);
            
            VariableTree vt = treeMaker.Variable(treeMaker.Modifiers(
                    Collections.<Modifier>emptySet(),
                    Collections.<AnnotationTree>emptyList()
                    ), "url", urlTree, urlConstructor);// NOI18N
           JavaModelHelper.addComment(treeMaker, vt, Comment.Style.LINE, " Create JMX Agent URL");// NOI18N
            newStatements.add(vt);
        }
        
        private void fillConnector(TreeMaker treeMaker,
                List<StatementTree> newStatements) {
            TypeElement factoryClass =
                    w.getElements().getTypeElement("javax.management.remote.JMXConnectorFactory");// NOI18N
            ExpressionTree factoryTree = treeMaker.QualIdent(factoryClass);
            MemberSelectTree connectSelect = treeMaker.MemberSelect(factoryTree, "connect");// NOI18N
            List<ExpressionTree> connectParams = new ArrayList<ExpressionTree>(1);
            connectParams.add(treeMaker.Identifier("url"));// NOI18N
            
            if (isSecurityChecked && isUserCredential)
                connectParams.add(treeMaker.Identifier("env"));// NOI18N
            else
                connectParams.add(treeMaker.Literal(null));
            
            MethodInvocationTree connectInvokation =
                    treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                    connectSelect,
                    connectParams);
            
            IdentifierTree connectorField = w.getTreeMaker().Identifier("connector");// NOI18N
            AssignmentTree bt = treeMaker.Assignment(connectorField, connectInvokation);
            
            ExpressionStatementTree t = treeMaker.ExpressionStatement(bt);
            newStatements.add((StatementTree)t);
            if (isSecurityChecked && isSampleCredential) {
                JavaModelHelper.addCommentPrefixedByWhiteLine(treeMaker, t, Comment.Style.BLOCK, "\n SAMPLE CREDENTIALS. Uncomment following code. \n" + // NOI18N
                        " Replace userName and userPassword with your parameters. \n" + // NOI18N
                        " Provide env parameter when calling JMXConnectorFactory.connect(url, env)\n\n");// NOI18N
                
                JavaModelHelper.addCommentFollowedByWhiteLine(treeMaker, t, Comment.Style.BLOCK, "\n" +// NOI18N
                        "Map env = new HashMap(); \n" +// NOI18N
                        "env.put(JMXConnector.CREDENTIALS, new String[]{\"" +// NOI18N
                        "userName\", \"" +// NOI18N
                        "userPassword" +// NOI18N
                        "\"});\n");// NOI18N
            }
            JavaModelHelper.addComment(treeMaker, t, Comment.Style.LINE, " Connect the JMXConnector"); // NOI18N
        }
    }
}
