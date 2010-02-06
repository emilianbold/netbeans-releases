/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.form;

import com.sun.source.tree.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;

/**
 * Class responsible for handling of fully-qualified names of classes.
 *
 * @author Jan Stola
 */
public class FQNImporter {
    /** Java file of the form. */
    private FileObject javaFile;
    /** Determines whether <code>initComponents()</code> method should be handled. */
    private boolean handleInitComponents;
    /**
     * Name of form listener class, can be <code>null</code> if
     * form listener shouldn't be handled.
     */
    private String formListener;
    /**
     * Names of fields in variables section. Can be <code>null</code>
     * if variables shouldn't be handled.
     */
    private Collection<String> variableNames;
    /**
     * Names of event handlers. Can be <code>null</code>
     * if event handlers shouldn't be handled.
     */
    private Collection<String> eventHandlers;

    /**
     * Creates new <code>FQNImporter</code>.
     * 
     * @param javaFile java file of the form.
     */
    FQNImporter(FileObject javaFile) {
        this.javaFile = javaFile;
    }

    /**
     * Sets whether initComponents() method should be handled.
     * 
     * @param handleInitComponents if true, then FQNs
     * in <code>initComponents()</code> will be handled.
     */
    void setHandleInitComponents(boolean handleInitComponents) {
        this.handleInitComponents = handleInitComponents;
    }

    /**
     * Sets the set of variables that should be handled.
     * 
     * @param variableNames names of variables that should be handled.
     */
    void setHandleVariables(Collection<String> variableNames) {
        this.variableNames = variableNames;
    }

    /**
     * Sets the name of form listener that should be handled.
     * 
     * @param formListener class name (simple name) of the form listener.
     */
    void setHandleFormListener(String formListener) {
        this.formListener = formListener;
    }

    /**
     * Sets the set of event handlers that should be handled.
     * 
     * @param eventHandlers names of event handlers that should be handled.
     */
    void setHandleEventHandlers(Collection<String> eventHandlers) {
        this.eventHandlers = eventHandlers;
    }

    /**
     * Imports fully qualified names e.g. replaces fully qualified names
     * with simple names and creates the corresponding imports.
     */
    void importFQNs() {
        try {
            JavaSource source = JavaSource.forFileObject(javaFile);
            CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
                @Override
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cu = wc.getCompilationUnit();
                    
                    // Find form's class
                    ClassTree clazz = null;
                    String fileName = javaFile.getName();
                    for (Tree typeDecl : cu.getTypeDecls()) {
                        if (Tree.Kind.CLASS == typeDecl.getKind()) {
                            ClassTree candidate = (ClassTree) typeDecl;
                            if (fileName.equals(candidate.getSimpleName().toString())) {
                                clazz = candidate;
                                break;
                            }
                        }
                    }
                    if (clazz == null) {
                        return;
                    }

                    GeneratorUtilities utils = GeneratorUtilities.get(wc);
                    
                    // initComponents() method
                    if (handleInitComponents) {
                        for (Tree tree : clazz.getMembers()) {
                            if (Tree.Kind.METHOD == tree.getKind()) {
                                MethodTree method = (MethodTree)tree;
                                if ("initComponents".equals(method.getName().toString())) { // NOI18N
                                    Tree oldBody = method.getBody();
                                    Tree newBody = utils.importFQNs(oldBody);
                                    wc.rewrite(oldBody, newBody);
                                }
                            }
                        }
                    }

                    // form listener
                    if (formListener != null) {
                        for (Tree tree : clazz.getMembers()) {
                            if (Tree.Kind.CLASS == tree.getKind()) {
                                ClassTree klass = (ClassTree)tree;
                                if (formListener.equals(klass.getSimpleName().toString())) {
                                    Tree newTree = utils.importFQNs(klass);
                                    wc.rewrite(klass, newTree);
                                }
                            }
                        }
                    }

                    // variables section
                    if (variableNames != null) {
                        for (Tree tree : clazz.getMembers()) {
                            if (Tree.Kind.VARIABLE == tree.getKind()) {
                                VariableTree variable = (VariableTree)tree;
                                if (variableNames.contains(variable.getName().toString())) {
                                    // It would be ideal to invoke importFQNs
                                    // directly on 'variable', but this corrupts
                                    // comments around variables section

                                    // Variable type
                                    Tree type = variable.getType();
                                    Tree newTree = utils.importFQNs(type);
                                    wc.rewrite(type, newTree);
                                    
                                    // Variable initializer
                                    Tree initializer = variable.getInitializer();
                                    if (initializer != null) {
                                        newTree = utils.importFQNs(initializer);
                                        wc.rewrite(initializer, newTree);
                                    }
                                }
                            }
                        }
                    }

                    // event handlers
                    if (eventHandlers != null) {
                        for (Tree tree : clazz.getMembers()) {
                            if (Tree.Kind.METHOD == tree.getKind()) {
                                MethodTree method = (MethodTree)tree;
                                if (eventHandlers.contains(method.getName().toString())) {
                                    for (VariableTree variable : method.getParameters()) {
                                        Tree newTree = utils.importFQNs(variable);
                                        wc.rewrite(variable, newTree);
                                    }
                                }
                            }
                        }
                    }
                }
                @Override
                public void cancel() {
                }
            };
            
            // Permit the changes in guarded blocks
            ModificationResult result = source.runModificationTask(task);
            List<? extends ModificationResult.Difference> diffs = result.getDifferences(javaFile);
            if (diffs != null) {
                for (ModificationResult.Difference diff : diffs) {
                    diff.setCommitToGuards(true);
                }
                result.commit();
            }
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, e.getMessage(), e);
        }
    }
    
}
