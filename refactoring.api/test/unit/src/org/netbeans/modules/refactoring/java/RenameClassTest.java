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

package org.netbeans.modules.refactoring.java;

import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class RenameClassTest extends RefactoringTestCase {
    
    /** Creates a new instance of RenameClassTest */
    public RenameClassTest(String name) {
        super(name);
        
    }
            
    public void testRenameClass() throws Exception {
        FileObject test = getFileInProject("default","src/defaultpkg/Foo.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("newName");
            }
        });        
    }

//    public void testRenameEnum() throws Exception {
//        FileObject test = getFileInProject("default","src/defaultpkg/RenameEnum.java" );
//        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
//        perform(renameRefactoring,new ParameterSetter() {
//            public void setParameters() {
//                renameRefactoring.setNewName("NewEnumName");
//            }
//        });        
//    }
//    
//    public void testRenameAnnotation() throws Exception {
//        FileObject test = getFileInProject("default","src/defaultpkg/RenameAnnot.java" );
//        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
//        perform(renameRefactoring,new ParameterSetter() {
//            public void setParameters() {
//                renameRefactoring.setNewName("NewAnnotName");
//            }
//        });        
//    }
//    
//    public void testRenamePackage() throws Exception {
//        FileObject test = getFileInProject("default","src/renamepkg" );
//        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
//        perform(renameRefactoring,new ParameterSetter() {
//            public void setParameters() {
//                renameRefactoring.setNewName("newpkgname");
//            }
//        });        
//    }
    
    
}
