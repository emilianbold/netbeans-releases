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

import java.net.URL;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jiri Prox
 */
public class MoveClassTest extends RefactoringTestCase {
    
    public MoveClassTest(String name) {
        super(name);
    }
    
    public void testMoveClass() throws Exception {
        FileObject test = getFileInProject("default","src/movepkg/MoveClass.java" );
        FileObject target = getFileInProject("default","src/movepkgdst" );
        final URL targetURL = target.getURL();
        final MoveRefactoring moveRefactoring = new MoveRefactoring(Lookups.singleton(test));
        perform(moveRefactoring,new ParameterSetter() {
            public void setParameters() {                
                moveRefactoring.setTarget(Lookups.singleton(targetURL));
            }
        });
    }
    
    public void testMoveMultiple() throws Exception {
        FileObject test = getFileInProject("default","src/movepkg/MoveMultiple1.java" );
        FileObject test2 = getFileInProject("default","src/movepkg/MoveMultiple2.java" );
        FileObject target = getFileInProject("default","src/movepkgdst" );
        final URL targetURL = target.getURL();
        final MoveRefactoring moveRefactoring = new MoveRefactoring(Lookups.fixed(test,test2));
        perform(moveRefactoring,new ParameterSetter() {
            public void setParameters() {                
                moveRefactoring.setTarget(Lookups.singleton(targetURL));
            }
        });
    }
    
    public void testMovePackageImport() throws Exception {
        FileObject test = getFileInProject("default","src/movepkg/MoveImport.java" );        
        FileObject target = getFileInProject("default","src/movepkgdst" );
        final URL targetURL = target.getURL();
        final MoveRefactoring moveRefactoring = new MoveRefactoring(Lookups.singleton(test));
        perform(moveRefactoring,new ParameterSetter() {
            public void setParameters() {                
                moveRefactoring.setTarget(Lookups.singleton(targetURL));
            }
        });        
    }
    
    public void testMoveToSamePackage() throws Exception {
        FileObject test = getFileInProject("default","src/movepkg/MoveToSame.java" );        
        FileObject target = getFileInProject("default","src/movepkg" );
        final URL targetURL = target.getURL();
        final MoveRefactoring moveRefactoring = new MoveRefactoring(Lookups.singleton(test));
        perform(moveRefactoring,new ParameterSetter() {
            public void setParameters() {                
                moveRefactoring.setTarget(Lookups.singleton(targetURL));
            }
        });
    }            
}
