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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java;

import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jiri Prox
 */
public class RenameClass2Test extends RefactoringElementTestCase {
    
    public RenameClass2Test(String name) {
        super(name);
    }
    
    public void testRenameEmptyClass() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "public class MyClass {\n" +
                "\n" +
                "}\n" +
                "\n";
        
        createClass("MyClass", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("newName");
            }
        },false);
    }
    
    public void testRenameCtor() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "public class MyClass2 {\n" +
                "    public MyClass2() {\n" +
                "    \n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "\n";
        
        createClass("MyClass2", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass2.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewClass2");
            }
        },false);
    }
    
    public void testRenameClassField() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "public class MyClass3 {\n" +
                "\n" +
                "}\n" +
                "\n";
        createClass("MyClass3", "org", content);
        content = "\n" +
                "package org;\n" +
                "\n" +
                "public class MyClass3Test {\n" +
                "    MyClass3 field;" +
                "}\n" +
                "\n";
        createClass("MyClass3Test", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass3.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewClass3");
            }
        },false);
    }
    
    public void testRenameGenerics() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class MyClass4 {\n" +
                "    List<MyClass4> list;\n" +
                "}\n" +
                "\n";
        createClass("MyClass4", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass4.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewClass4");
            }
        },false);
    }
    
    public void testRenameGenerics2() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class MyClass5 {\n" +
                "    List<? extends MyClass5> list;\n" +
                "}\n" +
                "\n";
        createClass("MyClass5", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass5.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewClass5");
            }
        },false);
    }
    
    public void testRenameReturnType() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class MyClass5a {\n" +
                "    \n" +
                "    public MyClass5a get() { return null;}\n" +
                "    \n" +
                "}\n" +
                "\n";
        createClass("MyClass5a", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass5a.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewClass5a");
            }
        },false);
    }
    
    public void testRenameExtends() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "public class MyClass6 {\n" +
                "    \n" +
                "}\n" +
                "\n";
        createClass("MyClass6", "org", content);
        content = "\n" +
                "package org;\n" +
                "\n" +
                "public class MyClass6Test extends MyClass6 {\n" +
                "    \n" +
                "}\n" +
                "\n";
        createClass("MyClass6Test", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass6.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewClass6");
            }
        },false);
    }
    
    public void testRenameImplements() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "public interface MyClass7 {\n" +
                "    \n" +
                "}\n" +
                "\n";
        createClass("MyClass7", "org", content);
        content = "\n" +
                "package org;\n" +
                "\n" +
                "public class MyClass7Test implements MyClass7 {\n" +
                "    \n" +
                "}\n" +
                "\n";
        createClass("MyClass7Test", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass7.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewClass7");
            }
        },false);
    }
    
    public void testRenameThrows() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "public class MyClass8 extends Exception {\n" +
                "    \n" +
                "}\n" +
                "\n";
        createClass("MyClass8", "org", content);
        content = "\n" +
                "package org;\n" +
                "\n" +
                "public class MyClass8Test {\n" +
                "    \n" +
                "    public void method() throws MyClass8 {}\n" +
                "    \n" +
                "}\n" +
                "\n";
        createClass("MyClass8Test", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass8.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewClass8");
            }
        },false);
    }
    
    public void testRenameAnnotation() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "public @interface MyClass9 {\n" +
                "    \n" +
                "}\n" +
                "\n";
        createClass("MyClass9", "org", content);
        content = "\n" +
                "package org;\n" +
                "\n" +
                "@MyClass9\n" +
                "public class MyClass9Test {\n" +
                "    \n" +                
                "}\n" +
                "\n";
        createClass("MyClass9Test", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass9.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewClass9");
            }
        },false);
    }
    
    public void testRenameEnum() throws Exception {
        String content = "\n" +
                "package org;\n" +
                "\n" +
                "public enum MyClass10 {\n" +
                "    \n" +
                "}\n" +
                "\n";
        createClass("MyClass10", "org", content);
        content = "\n" +
                "package org;\n" +
                "\n" +
                "public class MyClass10Test {\n" +
                "    \n" +
                "    MyClass10 myEnum;\n" +
                "    \n" +
                "}\n" +
                "\n";
        createClass("MyClass10Test", "org", content);
        FileObject test = getFileInProject("default","src/org/MyClass10.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewClass10");
            }
        },false);
    }
    
    
}
