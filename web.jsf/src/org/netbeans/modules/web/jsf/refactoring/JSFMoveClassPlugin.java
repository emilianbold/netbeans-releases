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

import org.openide.ErrorManager;

//TODO: RETOUCHE refactoring
/**
 *
 * @author Petr Pisl
 */
public class JSFMoveClassPlugin /*implements RefactoringPlugin*/{
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    private static final ErrorManager err =
            ErrorManager.getDefault().getInstance("org.netbeans.modules.web.jsf.refactoring");   // NOI18N
    
//    private final MoveClassRefactoring refactoring;
//    
//    /** Creates a new instance of JSFWhereUsedPlugin */
//    public JSFMoveClassPlugin(MoveClassRefactoring refactoring) {
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
//            Collection <Resource> resources = refactoring.getResources();
//            for (Resource resource : resources) {
//                String newPackageName = refactoring.getTargetPackageName(resource);
//                List <JavaClass> classes = resource.getClassifiers();
//                for (JavaClass jClass : classes) {
//                    String newClassName = newPackageName + "." + jClass.getSimpleName();
//                    List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(jClass, newClassName);
//                    for (Occurrences.OccurrenceItem item : items) {
//                        refactoringElements.add(refactoring, new JSFRenamePlugin.JSFConfigRenameClassElement(item));
//                    }
//                }
//            }
//            semafor.set(null);
//        }        
//        return null;
//    }
    
}
