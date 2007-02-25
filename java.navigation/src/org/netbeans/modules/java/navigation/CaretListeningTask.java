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

package org.netbeans.modules.java.navigation;

import com.sun.javadoc.Doc;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.filesystems.FileObject;

/**
 * This task is called every time the caret position changes in a Java editor.
 * <p>
 * The task finds the TreePath of the Tree under the caret, converts it to
 * an Element and then shows the declartion of the element in Declaration window
 * and javadoc in the Javadoc window.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class CaretListeningTask implements CancellableTask<CompilationInfo> {
    
    private CaretListeningFactory caretListeningFactory;
    private FileObject fileObject;
    private boolean canceled;
    
    
    CaretListeningTask(CaretListeningFactory whichElementJavaSourceTaskFactory,FileObject fileObject) {
        this.caretListeningFactory = whichElementJavaSourceTaskFactory;
        this.fileObject = fileObject;
    }
    
    private static final Collection<Modifier> NO_MODIFIERS = Collections.<Modifier>emptySet();
    
    public void run(CompilationInfo compilationInfo) {
        
        String declartion = "";
        String javadoc = "";
        
        setDeclaration(declartion);
        setJavadoc("", javadoc);
        
        // Find the TreePath for the caret position
        TreePath tp =
                compilationInfo.getTreeUtilities().pathFor(caretListeningFactory.getLastPosition(fileObject));
        
        // if cancelled, return
        if (isCancelled()) {
            return;
        }
        
        // Get Element
        Element element = compilationInfo.getTrees().getElement(tp);
        
        // if cancelled, return
        if (isCancelled()) {
            return;
        }
        
        if (element != null) {
            if (element instanceof PackageElement) {
                setDeclaration("package " + element.toString() + ";");
            } else {
                Doc doc = compilationInfo.getElementUtilities().javaDocFor(element);
                if (doc != null) {
                    setJavadoc(element.toString(), doc.getRawCommentText());
                }
                Tree tree = compilationInfo.getTrees().getTree(element);
                if (tree == null) {
                    FileObject fileObject = SourceUtils.getFile(element, compilationInfo.getClasspathInfo());
                    if (fileObject != null) {
                        switch (element.getKind()) {
                        case PACKAGE:
                        case CLASS:
                        case INTERFACE:
                        case ENUM:
                        case METHOD:
                        case CONSTRUCTOR:
                        case INSTANCE_INIT:
                        case STATIC_INIT:
                        case FIELD:
                        case ENUM_CONSTANT:
                            final ElementHandle elementHandle = ElementHandle.create(element);
                            JavaSource javaSource = JavaSource.forFileObject(fileObject);
                            try {
                                javaSource.runUserActionTask(new CancellableTask<CompilationController>() {
                                    public void cancel() {}
                                    public void run(CompilationController compilationController) throws IOException {
                                        // Move to resolved phase
                                        compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                        Element element = elementHandle.resolve(compilationController);
                                        if (element != null) {
                                            Tree tree = compilationController.getTrees().getTree(element);
                                            if (tree != null) {
                                                String declaration = tree.toString();
                                                if (element.getKind() ==  ElementKind.CONSTRUCTOR) {
                                                    String constructorName = element.getEnclosingElement().getSimpleName().toString();
                                                    declaration = declaration.replaceAll(Pattern.quote("<init>"), Matcher.quoteReplacement(constructorName));
                                                }
                                                setDeclaration(declaration);
                                            }
                                        }
                                    }
                                }, true);
                            } catch (IOException ex) {
                                Logger.global.log(Level.WARNING, ex.getMessage(), ex);;
                            }
                            break;
                        }
                    }
                } else {
                    String declaration = tree.toString();
                    if (element.getKind() ==  ElementKind.CONSTRUCTOR) {
                        String constructorName = element.getEnclosingElement().getSimpleName().toString();
                        declaration = declaration.replaceAll(Pattern.quote("<init>"), Matcher.quoteReplacement(constructorName));
                    }                    
                    setDeclaration(declaration);
                }
            }
        }
    }
    
    private void setDeclaration(final String declaration) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DeclarationTopComponent declarationTopComponent = DeclarationTopComponent.findInstance();
                if (declarationTopComponent != null && declarationTopComponent.isOpened()) {
                    declarationTopComponent.setDeclaration(declaration);
                }
            }
        });
    }
    
    private void setJavadoc(final String header, final String javadoc) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JavadocTopComponent javadocTopComponent = JavadocTopComponent.findInstance();
                if (javadocTopComponent != null && javadocTopComponent.isOpened()) {
                    javadocTopComponent.setJavadoc(header, javadoc);
                }
            }
        });
    }
    
    /**
     * After this method is called the task if running should exit the run
     * method immediately.
     */
    public final synchronized void cancel() {
        canceled = true;
    }
    
    protected final synchronized boolean isCancelled() {
        return canceled;
    }
}
