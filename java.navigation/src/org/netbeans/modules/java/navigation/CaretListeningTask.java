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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
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
    
    private ElementHandle<Element> lastEh;
    
    CaretListeningTask(CaretListeningFactory whichElementJavaSourceTaskFactory,FileObject fileObject) {
        this.caretListeningFactory = whichElementJavaSourceTaskFactory;
        this.fileObject = fileObject;
    }
    
    public void run(CompilationInfo compilationInfo) {
        
        boolean navigatorShouldUpdate = false;
        boolean javadocShouldUpdate = JavadocTopComponent.shouldUpdate();
        boolean declarationShouldUpdate = DeclarationTopComponent.shouldUpdate();
        
        if ( isCancelled() || ( !navigatorShouldUpdate && !javadocShouldUpdate && !declarationShouldUpdate ) ) {
            return;
        }
        
        // Find the TreePath for the caret position
        TreePath tp =
                compilationInfo.getTreeUtilities().pathFor(caretListeningFactory.getLastPosition(fileObject));        
        // if cancelled, return
        if (isCancelled()) {
            return;
        }
        
        // Get Element
        Element element = compilationInfo.getTrees().getElement(tp);
                       
        // if cancelled or no element, return
        if (isCancelled() || element == null ) {
            return;
        }
        
        // Update the navigator
        if ( navigatorShouldUpdate ) {
            updateNavigatorSelection(); 
        }
                
        if ( isCancelled() ) {            
            return;
        }
        
        // Don't update when element is the same
        if ( lastEh != null && lastEh.signatureEquals(element) ) {
            return;
        }
        else {
            lastEh = ElementHandle.create(element);
            // Diferent element clear data
            setDeclaration(""); // NOI18N
            setJavadoc("", ""); // NOI18N
        }
            
        // Compute and set javadoc
        if ( javadocShouldUpdate ) {
            computeAndSetJavadoc(compilationInfo, element);
        }
        
        if ( isCancelled() ) {
            return;
        }
        
        // Compute and set declaration
        if ( declarationShouldUpdate ) {
            computeAndSetDeclaration(compilationInfo, element);
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
    
    
    private void computeAndSetJavadoc(CompilationInfo compilationInfo, Element element) {
        
        String javadoc;
        
        if ( element.getKind() == ElementKind.PACKAGE ) {
            javadoc = ""; // NOI18N
        }
        else {
            Doc doc = compilationInfo.getElementUtilities().javaDocFor(element);
            javadoc = doc == null ? "" : doc.getRawCommentText(); // NOI18N
        }
        
        if (isCancelled()) {
            return;
        }
                
        setJavadoc(element.toString(), javadoc);
    }
    
    private void computeAndSetDeclaration(CompilationInfo compilationInfo, Element element ) {
            
        if ( element.getKind() == ElementKind.PACKAGE ) { 
            setDeclaration("package " + element.toString() + ";");
            return;
        }
            
        if ( isCancelled() ) {
            return;
        }
        
        Tree tree = compilationInfo.getTrees().getTree(element);

        if ( isCancelled()) {
            return;
        }

        if ( tree != null ) {
            String declaration = tree.toString();
            if (element.getKind() ==  ElementKind.CONSTRUCTOR) {
                String constructorName = element.getEnclosingElement().getSimpleName().toString();
                declaration = declaration.replaceAll(Pattern.quote("<init>"), Matcher.quoteReplacement(constructorName));
            }                    
            setDeclaration(declaration);
            return;
        }
        
        tree = SourceUtils.treeFor(compilationInfo, element);
        
        if ( isCancelled() || tree == null ) {
            return;
        }
        
        String declaration = tree.toString();
        if (element.getKind() ==  ElementKind.CONSTRUCTOR) {
            String constructorName = element.getEnclosingElement().getSimpleName().toString();
            declaration = declaration.replaceAll(Pattern.quote("<init>"), Matcher.quoteReplacement(constructorName));
        }
        setDeclaration(declaration);
                
    }
    
    private void updateNavigatorSelection() {
        // If navigator is visible ...
        
    }
    
}
