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
//TODO: RETOUCHE
//import org.netbeans.jmi.javamodel.Element;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.modules.javacore.internalapi.ExternalChange;
//import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
//import org.netbeans.modules.refactoring.api.Problem;
//import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
//import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
//import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
//import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/**
 *
 * @author Petr Pisl
 */
public class JSFSafeDeletePlugin /*implements RefactoringPlugin*/{
    
//    /** This one is important creature - makes sure that cycles between plugins won't appear */
//    private static ThreadLocal semafor = new ThreadLocal();
//    
//    private SafeDeleteRefactoring refactoring;
//    
//    private static final ErrorManager err =
//            ErrorManager.getDefault().getInstance("org.netbeans.modules.web.jsf.refactoring");   // NOI18N
//    
//
//    
//    /** Creates a new instance of JSFWhereUsedPlugin */
//    public JSFSafeDeletePlugin(SafeDeleteRefactoring refactoring) {
//        this.refactoring = refactoring;
//    }
//    
//    public Problem preCheck() {
//        return null;
//    }
//    
//    public Problem checkParameters() {
//        return null;
//    }
//    
//    public Problem fastCheckParameters() {
//        return null;
//    }
//    
//    public void cancelRequest() {
//    }
//    
//    public Problem prepare(RefactoringElementsBag refactoringElements) {
//        if (semafor.get() == null) {
//            semafor.set(new Object());
//            Element[] elements = refactoring.getElementsToDelete();
//            for (int i = 0; i < elements.length; i++) {
//                if (elements[i] instanceof JavaClass){
//                    JavaClass jClass = (JavaClass) elements[i];
//                    List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(jClass, "");
//                    for (Occurrences.OccurrenceItem item : items) {
//                        refactoringElements.add(refactoring, new JSFSafeDeleteClassElement(item));
//                    }
//                }
//            
//            } 
//            semafor.set(null);
//        }        
//        return null;
//    }
//    
//    
//    public static class JSFSafeDeleteClassElement extends SimpleRefactoringElementImpl implements ExternalChange {
//        private Occurrences.OccurrenceItem item;
//        
//        JSFSafeDeleteClassElement(Occurrences.OccurrenceItem item){
//            this.item = item;
//        }
//        
//        public String getText() {
//            return getDisplayText();
//        }
//            
//        public String getDisplayText() {
//            return item.getRenameMessage();
//        }
//        
//        public void performChange() {
//            JavaMetamodel.getManager().registerExtChange(this);
//        }
//        
//        public void performExternalChange() {
//            item.performSafeDelete();
//        }
//
//        public void undoExternalChange() {
//            item.undoSafeDelete();
//        }
//        
//        public Element getJavaElement() {
//            return null;
//        }
//        
//        public FileObject getParentFile() {
//            return item.getConfigDO().getPrimaryFile();
//        }
//        
//        public PositionBounds getPosition() {
//            return item.getElementDefinitionPosition();
//        }
//    }
}
