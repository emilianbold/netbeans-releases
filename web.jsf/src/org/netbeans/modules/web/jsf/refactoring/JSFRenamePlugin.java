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

package org.netbeans.modules.web.jsf.refactoring;

import java.util.List;
import javax.swing.text.Position.Bias;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.javacore.internalapi.ExternalChange;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.netbeans.modules.web.jsf.editor.JSFEditorUtilities;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;

/**
 *
 * @author Petr Pisl
 */

public class JSFRenamePlugin implements RefactoringPlugin {

    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    private static final ErrorManager err =
            ErrorManager.getDefault().getInstance("org.netbeans.modules.web.jsf.refactoring");   // NOI18N
    
    private final RenameRefactoring refactoring;
    
    /** Creates a new instance of WicketRenameRefactoringPlugin */
    public JSFRenamePlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    public Problem preCheck() {
        err.log("preCheck() called.");
        return null;
    }
    
    public Problem checkParameters() {
        err.log("checkParameters() called.");
        return null;
    }
    
    public Problem fastCheckParameters() {
        err.log("fastCheckParameters() called.");
        return null;
    }
    
    public void cancelRequest() {
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() == null) {
            semafor.set(new Object());
            Object element = refactoring.getRefactoredObject();
            err.log("Prepare refactoring: " + element);                 // NOI18N

            if (element instanceof JavaClass){
                JavaClass jclass = (JavaClass) element;
                String newType = renameClass(jclass.getName(), refactoring.getNewName());
                List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(jclass, newType);
                for (Occurrences.OccurrenceItem item : items) {
                    refactoringElements.add(refactoring, new JSFConfigRenameClassElement(item));
                }
            }
            semafor.set(null);
        }
        return null;
    }
    
    /**
     * @return true if given str is null or empty.
     */
    private static boolean isEmpty(String str){
        return str == null || "".equals(str.trim());
    }
    
    /**
     * Constructs new name for given class.
     * @param originalFullyQualifiedName old fully qualified name of the class.
     * @param newName new unqualified name of the class.
     * @return new fully qualified name of the class.
     */
    private static String renameClass(String originalFullyQualifiedName, String newName){
        if (isEmpty(originalFullyQualifiedName) || isEmpty(newName)){
            throw new IllegalArgumentException("Old and new name of the class must be given.");
        }
        int lastDot = originalFullyQualifiedName.lastIndexOf('.');
        if (lastDot <= 0){
            // no package
            return newName;
        }
        return originalFullyQualifiedName.substring(0, lastDot + 1) + newName;
    }
    
    public static class JSFConfigRenameClassElement extends SimpleRefactoringElementImpl implements ExternalChange {
        private Occurrences.OccurrenceItem item;
        
        JSFConfigRenameClassElement(Occurrences.OccurrenceItem item){
            this.item = item;
        }
        
        public String getText() {
            return getDisplayText();
        }
            
        public String getDisplayText() {
            return item.getRenameMessage();
        }
        
        public void performChange() {
            JavaMetamodel.getManager().registerExtChange(this);
        }
        
        public void performExternalChange() {
            item.performRename();
        }

        public void undoExternalChange() {
            item.undoRename();
        }
        
        public Element getJavaElement() {
            return null;
        }
        
        public FileObject getParentFile() {
            return item.getConfigDO().getPrimaryFile();
        }
        
        public PositionBounds getPosition() {
            return item.getClassDefinitionPosition();
        }
    }
    
}
