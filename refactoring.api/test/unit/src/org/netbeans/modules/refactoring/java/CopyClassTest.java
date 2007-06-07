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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java;

import java.net.URL;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jiri Prox
 */
public class CopyClassTest extends RefactoringTestCase {
    
    
    public CopyClassTest(String name) {
        super(name);
    }
    
    public void testCopyClass() throws Exception{
        FileObject test = getFileInProject("default","src/copypkg/CopyClass.java" );
        FileObject target = getFileInProject("default","src/copypkgdst");
        final URL targetURL = target.getURL();
        final SingleCopyRefactoring copyRefactoring = new SingleCopyRefactoring(Lookups.singleton(test));
        perform(copyRefactoring,new ParameterSetter() {
            public void setParameters() {
                copyRefactoring.setTarget(Lookups.singleton(targetURL));
                copyRefactoring.setNewName("CopyClass");
            }
        });
        
    }
    
    public void testCopyClassToSamePackage() throws Exception{
        FileObject test = getFileInProject("default","src/copypkg/CopyClassToSamePkg.java" );
        FileObject target = getFileInProject("default","src/copypkg");
        final URL targetURL = target.getURL();
        final SingleCopyRefactoring copyRefactoring = new SingleCopyRefactoring(Lookups.singleton(test));
        perform(copyRefactoring,new ParameterSetter() {
            public void setParameters() {
                copyRefactoring.setTarget(Lookups.singleton(targetURL));
                copyRefactoring.setNewName("CopyClassToSamePkg1");
            }
        });
        
    }
    
    public void testCopyClassWithRename() throws Exception{
        FileObject test = getFileInProject("default","src/copypkg/CopyRename.java" );
        FileObject target = getFileInProject("default","src/copypkgdst");
        final URL targetURL = target.getURL();
        final SingleCopyRefactoring copyRefactoring = new SingleCopyRefactoring(Lookups.singleton(test));
        perform(copyRefactoring,new ParameterSetter() {
            public void setParameters() {
                copyRefactoring.setTarget(Lookups.singleton(targetURL));
                copyRefactoring.setNewName("CopiedAndRenamed");
            }
        });
    }
    
    public void testCopyToDefault() throws Exception{
        FileObject test = getFileInProject("default","src/copypkg/CopyDefault.java" );
        FileObject target = getFileInProject("default","src");
        final URL targetURL = target.getURL();
        final SingleCopyRefactoring copyRefactoring = new SingleCopyRefactoring(Lookups.singleton(test));
        perform(copyRefactoring,new ParameterSetter() {
            public void setParameters() {
                copyRefactoring.setTarget(Lookups.singleton(targetURL));
                copyRefactoring.setNewName("CopyDefault");
            }
        });
    }
    
    public void testCopyInvalid1() throws Exception{
        FileObject test = getFileInProject("default","src/copypkg/CopyClassInvalid1.java" );
        FileObject target = getFileInProject("default","src/copypkgdst");
        final URL targetURL = target.getURL();
        final SingleCopyRefactoring copyRefactoring = new SingleCopyRefactoring(Lookups.singleton(test));
        perform(copyRefactoring,new ParameterSetter() {
            public void setParameters() {
                copyRefactoring.setTarget(Lookups.singleton(targetURL));
                copyRefactoring.setNewName("CopyClassInvalid2");
            }
        });
    }
    
    public void testCopyInvalid2() throws Exception{
        FileObject test = getFileInProject("default","src/copypkg/CopyClassInvalid2.java" );
        FileObject target = getFileInProject("default","src/copypkgdst");
        final URL targetURL = target.getURL();
        final SingleCopyRefactoring copyRefactoring = new SingleCopyRefactoring(Lookups.singleton(test));
        perform(copyRefactoring,new ParameterSetter() {
            public void setParameters() {
                copyRefactoring.setTarget(Lookups.singleton(targetURL));
                copyRefactoring.setNewName("CopyClassInvalid2");
            }
        });
    }
    
    
}
