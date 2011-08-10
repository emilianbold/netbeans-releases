/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.api.annotations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.phpmodule.PhpFrameworks;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleIgnoredFilesExtender;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.openide.util.test.MockLookup;

public class PhpFrameworkProviderRegistrationTest extends NbTestCase {

    public PhpFrameworkProviderRegistrationTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testAnnotations() {
        MyFw.factoryCalls = 0;
        MockLookup.init();
        assertSame("No factory method should not be used yet", 0, MyFw.factoryCalls);
        Collection<? extends PhpFrameworkProvider> all = Lookups.forPath(PhpFrameworks.FRAMEWORK_PATH).lookupAll(PhpFrameworkProvider.class);
        assertSame("Two should be found", 2, all.size());
        // ???
        //assertSame("One factory method should be used", 1, MyFw.factoryCalls);

        Iterator<? extends PhpFrameworkProvider> it = all.iterator();
        assertEquals("constructor", it.next().getIdentifier());
        assertEquals("factory", it.next().getIdentifier());
    }

    public void testPublicClass() throws Exception {
        String myFw = ""
                + getImports()
                + "@PhpFrameworkProvider.Registration(position=100)"
                + "class MyFw extends PhpFrameworkProvider {"
                + "  public MyFw() {"
                + "    super(\"MyFw\", null);"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("Class needs to be public"));
    }

    public void testAbstractClass() throws Exception {
        String myFw = ""
                + getImports()
                + "@PhpFrameworkProvider.Registration(position=100)"
                + "public abstract class MyFw extends PhpFrameworkProvider {"
                + "  public MyFw() {"
                + "    super(\"MyFw\", null);"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("Class cannot be abstract"));
    }

    public void testExtendsPhpFrameworkProvider() throws Exception {
        String myFw = ""
                + getImports()
                + "@PhpFrameworkProvider.Registration(position=100)"
                + "public class MyFw extends Object {"
                + "  public MyFw() {"
                + "    super(\"MyFw\", null);"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("Class needs to extend PhpFrameworkProvider"));
    }

    public void testPublicDefaultConstructor1() throws Exception {
        String myFw = ""
                + getImports()
                + "@PhpFrameworkProvider.Registration(position=100)"
                + "public class MyFw extends PhpFrameworkProvider {"
                + "  MyFw() {"
                + "    super(\"MyFw\", null);"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("There needs to be public default constructor"));
    }

    public void testPublicDefaultConstructor2() throws Exception {
        String myFw = ""
                + getImports()
                + "@PhpFrameworkProvider.Registration(position=100)"
                + "public class MyFw extends PhpFrameworkProvider {"
                + "  private MyFw() {"
                + "    super(\"MyFw\", null);"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("There needs to be public default constructor"));
    }

    public void testPublicDefaultConstructor3() throws Exception {
        String myFw = ""
                + getImports()
                + "@PhpFrameworkProvider.Registration(position=100)"
                + "public class MyFw extends PhpFrameworkProvider {"
                + "  public MyFw(String name) {"
                + "    super(name, null);"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("There needs to be public default constructor"));
    }

    public void testFactoryMethodinPublicClass() throws Exception {
        String myFw = ""
                + getImports()
                + "class MyFw extends PhpFrameworkProvider {"
                + "  private MyFw() {"
                + "    super(\"myFw\", null);"
                + "  }"
                + "  @PhpFrameworkProvider.Registration(position=100)"
                + "  public static MyFw create() {"
                + "    return new MyFw();"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("Class needs to be public"));
    }

    public void testFactoryMethodIsPublic() throws Exception {
        String myFw = ""
                + getImports()
                + "public class MyFw extends PhpFrameworkProvider {"
                + "  private MyFw() {"
                + "    super(\"myFw\", null);"
                + "  }"
                + "  @PhpFrameworkProvider.Registration(position=100)"
                + "  static MyFw create() {"
                + "    return new MyFw();"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("The method needs to be public, static and without arguments"));
    }

    public void testFactoryMethodIsStatic() throws Exception {
        String myFw = ""
                + getImports()
                + "public class MyFw extends PhpFrameworkProvider {"
                + "  private MyFw() {"
                + "    super(\"myFw\", null);"
                + "  }"
                + "  @PhpFrameworkProvider.Registration(position=100)"
                + "  public MyFw create() {"
                + "    return new MyFw();"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("The method needs to be public, static and without arguments"));
    }

    public void testFactoryMethodIsWithoutParameters() throws Exception {
        String myFw = ""
                + getImports()
                + "public class MyFw extends PhpFrameworkProvider {"
                + "  private MyFw() {"
                + "    super(\"myFw\", null);"
                + "  }"
                + "  @PhpFrameworkProvider.Registration(position=100)"
                + "  public static MyFw create(String name) {"
                + "    return new MyFw();"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("The method needs to be public, static and without arguments"));
    }

    public void testFactoryMethodReturnType() throws Exception {
        String myFw = ""
                + getImports()
                + "public class MyFw extends PhpFrameworkProvider {"
                + "  private MyFw() {"
                + "    super(\"myFw\", null);"
                + "  }"
                + "  @PhpFrameworkProvider.Registration(position=100)"
                + "  public static Object create() {"
                + "    return new MyFw();"
                + "  }"
                + getEmptyBody()
                + "}"
                + "";
        String output = compile(myFw);
        assertTrue(output, output.contains("Method needs to return PhpFrameworkProvider"));
    }

    private String compile(String myFw) throws IOException {
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.MyFw", myFw);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, out);
        assertFalse("Compilation should fail", res);
        return out.toString();
    }

    public static final class MyFwFactory {
        @PhpFrameworkProvider.Registration(position=200)
        public static MyFw getInstance() {
            MyFw.factoryCalls++;
            return new MyFw("factory");
        }
    }

    @PhpFrameworkProvider.Registration(position=100)
    public static final class MyFw extends PhpFrameworkProvider {
        static int factoryCalls = 0;

        public MyFw() {
            super("constructor", "constructor", null);
        }

        MyFw(String name) {
            super(name, name, null);
        }

        @Override
        public boolean isInPhpModule(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public File[] getConfigurationFiles(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public EditorExtender getEditorExtender(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static String getImports() {
        return new StringBuilder(500)
                .append("import java.io.File;")
                .append("import org.netbeans.modules.php.api.phpmodule.*;")
                .append("import org.netbeans.modules.php.spi.commands.*;")
                .append("import org.netbeans.modules.php.spi.editor.*;")
                .append("import org.netbeans.modules.php.spi.phpmodule.*;")
                .toString();
    }

    private static String getEmptyBody() {
        return new StringBuilder(500)
                .append("public boolean isInPhpModule(PhpModule phpModule) {return false;}")
                .append("public File[] getConfigurationFiles(PhpModule phpModule) {return null;}")
                .append("public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {return null;}")
                .append("public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {return null;}")
                .append("public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {return null;}")
                .append("public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {return null;}")
                .append("public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {return null;}")
                .append("public EditorExtender getEditorExtender(PhpModule phpModule) {return null;}")
                .toString();
    }
}
