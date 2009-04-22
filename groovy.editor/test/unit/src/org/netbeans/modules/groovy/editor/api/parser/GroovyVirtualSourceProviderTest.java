/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.editor.api.parser;

import org.netbeans.modules.groovy.editor.api.parser.GroovyVirtualSourceProvider;
import java.io.IOException;
import java.util.List;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class GroovyVirtualSourceProviderTest extends GroovyTestBase {

    public GroovyVirtualSourceProviderTest(String testName) {
        super(testName);
    }

    public void testGeneratorWithClass() throws IOException {
        copyStringToFileObject(testFO,
                "class Foo {\n" +
                "  def closure1 = {\n" +
                "    println 'closure1'\n" +
                "  }\n" +
                "  def method1() {\n" +
                "    println 'method1'\n" +
                "  }\n" +
                "}");
        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 1);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals(
                "import groovy.util.*;\n" +
                "import java.util.*;\n" +
                "import java.io.*;\n" +
                "import java.lang.*;\n" +
                "import groovy.lang.*;\n" +
                "import java.net.*;\n" +
                "\n" +
                "public class Foo\n" +
                "  extends java.lang.Object  implements\n" +
                "    groovy.lang.GroovyObject {\n" +
                "public java.lang.Object method1() { return null;}\n" +
                "public java.lang.Object getClosure1() { return null;}\n" +
                "public void setClosure1(java.lang.Object value) { }\n" +
                "}\n", charSequence);
    }

    public void testGeneratorWithScript() throws IOException {
        copyStringToFileObject(testFO,
                "def closure1 = {\n" +
                "  println 'closure1'\n" +
                "}\n" +
                "def method1() {\n" +
                "  println 'method1'\n" +
                "}");
        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 1);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals(
                "import groovy.util.*;\n" +
                "import java.util.*;\n" +
                "import java.io.*;\n" +
                "import java.lang.*;\n" +
                "import groovy.lang.*;\n" +
                "import java.net.*;\n" +
                "\n" +
                "public class Test\n" +
                "  extends groovy.lang.Script {\n" +
                "public Test() {}\n" +
                "public Test(groovy.lang.Binding context) {}\n" +
                "public static void main(java.lang.String[] args) { }\n" +
                "public java.lang.Object run() { return null;}\n" +
                "public java.lang.Object method1() { return null;}\n" +
                "}\n", charSequence);
    }

    public void testGenerics() throws IOException {
        copyStringToFileObject(testFO,
                "class Foo {\n" +
                "  static List<String> get() {\n" +
                "    return new ArrayList<String>()" +
                "  }\n" +
                "}");
        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 1);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals(
                "import groovy.util.*;\n" +
                "import java.util.*;\n" +
                "import java.io.*;\n" +
                "import java.lang.*;\n" +
                "import groovy.lang.*;\n" +
                "import java.net.*;\n" +
                "\n" +
                "public class Foo\n" +
                "  extends java.lang.Object  implements\n" +
                "    groovy.lang.GroovyObject {\n" +
                "public static java.util.List<java.lang.String> get() { return (java.util.List<java.lang.String>)null;}\n" +
                "}\n", charSequence);
    }

    public void testImports() throws IOException {
        copyStringToFileObject(testFO,
            "import javax.swing.JPanel\n" +
            "class MyTest extends JPanel {\n"+
            "    JPanel getPanel() {\n" +
            "        return null;\n" +
            "    }\n" +
            "}");

        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 1);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals("import groovy.util.*;\n" +
                "import java.util.*;\n" +
                "import java.io.*;\n" +
                "import java.lang.*;\n" +
                "import javax.swing.*;\n" +
                "import groovy.lang.*;\n" +
                "import java.net.*;\n" +
                "\n" +
                "public class MyTest\n" +
                "  extends javax.swing.JPanel  implements\n" +
                "    groovy.lang.GroovyObject {\n" +
                "public javax.swing.JPanel getPanel() { return (javax.swing.JPanel)null;}\n" +
                "}\n", charSequence);
    }

    public void testMultipleClasses() throws IOException {
        copyStringToFileObject(testFO,
                "class PostService {\n" +
                "    boolean transactional = true\n" +
                "    def serviceMethod() throws PostException {\n" +
                "        throw new PostException();\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class PostException extends Exception {\n" +
                "    public PostException() {\n" +
                "        super();\n" +
                "    }\n" +
                "}");
        List<ClassNode> classNodes = GroovyVirtualSourceProvider.getClassNodes(FileUtil.toFile(testFO));
        assertEquals(classNodes.size(), 2);

        GroovyVirtualSourceProvider.JavaStubGenerator generator = new GroovyVirtualSourceProvider.JavaStubGenerator();
        CharSequence charSequence = generator.generateClass(classNodes.get(0));
        assertEquals("import groovy.util.*;\n"+
                "import java.util.*;\n" +
                "import java.io.*;\n" +
                "import java.lang.*;\n" +
                "import groovy.lang.*;\n" +
                "import java.net.*;\n" +
                "\n" +
                "public class PostService\n" +
                "  extends java.lang.Object  implements\n" +
                "    groovy.lang.GroovyObject {\n" +
                "public java.lang.Object serviceMethod() { return null;}\n" +
                "public boolean getTransactional() { return (boolean)false;}\n" +
                "public void setTransactional(boolean value) { }\n" +
                "}\n", charSequence);

        charSequence = generator.generateClass(classNodes.get(1));
        System.out.println(charSequence);
        assertEquals("import groovy.util.*;\n" +
                "import java.util.*;\n" +
                "import java.io.*;\n" +
                "import java.lang.*;\n" +
                "import groovy.lang.*;\n" +
                "import java.net.*;\n" +
                "\n" +
                "public class PostException\n" +
                "  extends java.lang.Exception  implements\n" +
                "    groovy.lang.GroovyObject {\n" +
                "public PostException() {\n" +
                "super ();\n" +
                "}\n" +
                "}\n", charSequence);
    }
}
