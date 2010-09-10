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

package org.netbeans.modules.java.hints.jdk;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Tomas Zezula
 */
public class ConvertToARMTest extends TestBase {
    
    public ConvertToARMTest(final String name) {
        super(name, ConvertToARM.class);
    }
    
    
    public void testSimpleTryFinally() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         final InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         try {"+
                       "            in.read();"+
                       "         } finally {"+
                       "            in.close();"+
                       "         }"+
                       "         System.out.println(\"Done\");"+                                              
                       "     }" +
                       "}",
                       "0:216-0:218:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } System.out.println(\"Done\"); }}");
    }
    
    public void testTryFinally() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         try {"+
                       "            in.read();"+
                       "         } catch (Exception e) {" +
                       "            System.out.println(\"Ex\");"+
                       "         } finally {"+
                       "            in.close();"+
                       "            System.out.println(\"Fin\");"+
                       "         }"+
                       "         System.out.println(\"Done\");"+                                              
                       "     }" +
                       "}",
                       "0:210-0:212:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } catch (Exception e) { System.out.println(\"Ex\"); } finally { System.out.println(\"Fin\"); } System.out.println(\"Done\"); }}");
    }
    
    public void testTryFinallyWithFinal() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         final InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         try {"+
                       "            in.read();"+
                       "         } catch (Exception e) {" +
                       "            System.out.println(\"Ex\");"+
                       "         } finally {"+
                       "            in.close();"+
                       "            System.out.println(\"Fin\");"+
                       "         }"+
                       "         System.out.println(\"Done\");"+                                              
                       "     }" +
                       "}",
                       "0:216-0:218:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } catch (Exception e) { System.out.println(\"Ex\"); } finally { System.out.println(\"Fin\"); } System.out.println(\"Done\"); }}");
    }
    
    public void testLazyTryFinally() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream in = null;"+
                       "         try {"+
                       "            in = new FileInputStream(new File(\"a\"));"+
                       "            in.read();"+
                       "         } finally {"+
                       "            if (in != null)"+
                       "                in.close();"+
                       "         }"+
                       "         System.out.println(\"Done\");"+                                              
                       "     }" +
                       "}",
                       "0:210-0:212:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } System.out.println(\"Done\"); }}");
    }
    
    public void testNoTry0Stms() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         in.close();"+
                       "         System.out.println(\"Done\");"+                                              
                       "     }" +
                       "}",
                       "0:210-0:212:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\");try (InputStream in = new FileInputStream(new File(\"a\"))) { } System.out.println(\"Done\"); }}");
    }
    
    public void testNoTry1Stm() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         in.read();"+
                       "         in.close();"+
                       "         System.out.println(\"Done\");"+                                              
                       "     }" +
                       "}",
                       "0:210-0:212:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\");try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } System.out.println(\"Done\"); }}");
    }
    
    public void testNoTryMoreStms() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         in.read();"+
                       "         in.read();"+
                       "         in.close();"+
                       "         System.out.println(\"Done\");"+                                              
                       "     }" +
                       "}",
                       "0:210-0:212:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\");try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); in.read(); } System.out.println(\"Done\"); }}");
    }
    
    public void testNoTry1StmFinal() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         System.out.println(\"Start\");" +
                       "         final InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         in.read();"+
                       "         in.close();"+
                       "         System.out.println(\"Done\");"+                                              
                       "     }" +
                       "}",
                       "0:216-0:218:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\");try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } System.out.println(\"Done\"); }}");
    }
    
    
    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return "FixImpl";
    }
}
