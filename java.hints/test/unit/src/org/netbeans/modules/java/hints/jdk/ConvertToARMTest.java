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
                       "         in.read();"+
                       "         in.close();"+
                       "         System.out.println(\"Done\");"+                                              
                       "     }" +
                       "}",
                       "0:210-0:212:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\");try (InputStream in = new FileInputStream(new File(\"a\"))) { in.read(); } System.out.println(\"Done\"); }}");
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
    
    public void testNestedInFinally() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         try {"+
                       "             try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "                 in.read();"+
                       "             }"+
                       "         } finally {"+
                       "             in.close();"+
                       "         }"+
                       "     }" +
                       "}",
                       "0:173-0:175:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in = new FileInputStream(new File(\"a\")); InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } }}"
        );
    }
    
    public void testNestedInFinal() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         final InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         try {"+
                       "             try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "                 in.read();"+
                       "             }"+
                       "         } finally {"+
                       "             in.close();"+
                       "         }"+
                       "     }" +
                       "}",
                       "0:179-0:181:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in = new FileInputStream(new File(\"a\")); InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } }}"
        );
    }
    
    public void testNestedInCatchFinally() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         try {"+
                       "             try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "                 in.read();"+
                       "             }"+
                       "         } catch (Exception e) {"+
                       "             throw e;"+
                       "         }finally {"+
                       "             in.close();"+
                       "             System.gc();"+
                       "         }"+
                       "     }" +
                       "}",
                       "0:173-0:175:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in = new FileInputStream(new File(\"a\")); InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } catch (Exception e) { throw e; }finally { System.gc(); } }}"
        );
    }
    
    public void testNestedInLazyCatchFinally() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         InputStream in = null;"+
                       "         try {"+
                       "             in = new FileInputStream(new File(\"a\"));"+
                       "             try (InputStream in2 = new FileInputStream(new File(\"a\"))){" +
                       "                 in.read();"+
                       "             }"+
                       "         } catch (Exception e) {"+
                       "             throw e;"+
                       "         }finally {"+
                       "             if (in != null)"+
                       "             in.close();"+
                       "             System.gc();"+
                       "         }"+
                       "     }" +
                       "}",
                       "0:173-0:175:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in = new FileInputStream(new File(\"a\")); InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } catch (Exception e) { throw e; }finally { System.gc(); } }}"
        );
    }
    
    public void testNestedInStms() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         InputStream in = new FileInputStream(new File(\"a\"));"+
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){"+
                       "             in.read();"+
                       "         }"+
                       "         in.close();"+
                       "     }" +
                       "}",
                       "0:173-0:175:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception {try (InputStream in = new FileInputStream(new File(\"a\"));InputStream in2 = new FileInputStream(new File(\"a\"))) { in.read(); } }}"
        );
    }
    
    public void testEnclosedFinally() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){"+
                       "             InputStream in = new FileInputStream(new File(\"a\"));" +
                       "             try {"+
                       "                 in.read();"+
                       "             } finally {"+
                       "                 in.close();"+
                       "             }"+
                       "        }"+
                       "     }" +
                       "}",
                       "0:245-0:247:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){in.read(); } }}"
        );
    }
    
    public void testEnclosedFinallyInCatchFinally() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){"+
                       "             InputStream in = new FileInputStream(new File(\"a\"));" +
                       "             try {"+
                       "                 in.read();"+
                       "             } finally {"+
                       "                 in.close();"+
                       "             }"+
                       "        } catch (Exception e) {"+
                       "            throw e;"+
                       "        } finally {"+
                       "            System.gc();"+
                       "        }"+
                       "     }" +
                       "}",
                       "0:245-0:247:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){in.read(); } catch (Exception e) { throw e; } finally { System.gc(); } }}"
        );
    }
    
    public void testEnclosedFinallyFinal() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){"+
                       "             final InputStream in = new FileInputStream(new File(\"a\"));" +
                       "             try {"+
                       "                 in.read();"+
                       "             } finally {"+
                       "                 in.close();"+
                       "             }"+
                       "        }"+
                       "     }" +
                       "}",
                       "0:251-0:253:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){in.read(); } }}"
        );
    }
    
    public void testEnclosedStms() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){"+
                       "             InputStream in = new FileInputStream(new File(\"a\"));"+
                       "             in.read();"+
                       "             in.close();"+
                       "         }" +
                       "    }"+
                       "}",
                       "0:245-0:247:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){ in.read(); } }}"
        );
    }
    
    public void testEnclosedStmsInCatchFinally() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){"+
                       "             InputStream in = new FileInputStream(new File(\"a\"));"+
                       "             in.read();"+
                       "             in.close();"+
                       "         } catch (Exception e) { throw e;} finally { System.gc(); }" +
                       "    }"+
                       "}",
                       "0:245-0:247:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){ in.read(); } catch (Exception e) { throw e;} finally { System.gc(); } }}"
        );
    }
    
    public void testEnclosedStmsFinal() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))){"+
                       "             final InputStream in = new FileInputStream(new File(\"a\"));"+
                       "             in.read();"+
                       "             in.close();"+
                       "         }" +
                       "    }"+
                       "}",
                       "0:251-0:253:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"a\"))){ in.read(); } }}"
        );
    }
            
    public void testEnclosedLazy() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))) {"+
                       "             InputStream in = null;"+
                       "             try {"+
                       "                in = new FileInputStream(new File(\"b\"));"+
                       "                in.read();"+
                       ""+
                       "             } finally { if (in != null) in.close(); }"+
                       "         }"+
                       "     }"+
                       "}",
                       "0:246-0:248:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"b\"))) {in.read(); } }}"
        );
    }
    
    public void testEnclosedLazyInCatchFinally() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "         try (InputStream in2 = new FileInputStream(new File(\"a\"))) {"+
                       "             InputStream in = null;"+
                       "             try {"+
                       "                in = new FileInputStream(new File(\"b\"));"+
                       "                in.read();"+
                       ""+
                       "             } finally { if (in != null) in.close(); }"+
                       "         }catch(Exception e) { throw e;} finally {System.exit(1);}"+
                       "     }"+
                       "}",
                       "0:246-0:248:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { try (InputStream in2 = new FileInputStream(new File(\"a\")); InputStream in = new FileInputStream(new File(\"b\"))) {in.read(); }catch(Exception e) { throw e;} finally {System.exit(1);} }}"
        );
    }
    
    public void testTryTry() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6

        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.OutputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.FileOutputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test(File from, File to1) throws Exception {" +                       
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);"+
                       "         try {"+
                       "            final OutputStream out1 = new FileOutputStream(to1);"+
                       "            try {"+
                       "                int len;"+
                       "                while ((len = in.read(data)) > 0) {"+
                       "                    out1.write(data, 0, len);"+
                       "                }"+
                       "            } finally {"+
                       "                out1.close();"+
                       "            }"+
                       "        } finally {" +
                       "            in.close();"+
                       "        }"+                                                                     
                       "     }"+
                       "}",
                       "0:301-0:303:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from)) { final OutputStream out1 = new FileOutputStream(to1); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); } } finally { out1.close(); } } }}"
                       );
        
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.OutputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.FileOutputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test(File from, File to1) throws Exception {" +                       
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);"+
                       "         try {"+
                       "            final OutputStream out1 = new FileOutputStream(to1);"+
                       "            try {"+
                       "                int len;"+
                       "                while ((len = in.read(data)) > 0) {"+
                       "                    out1.write(data, 0, len);"+
                       "                }"+
                       "            } finally {"+
                       "                out1.close();"+
                       "            }"+
                       "        } finally {" +
                       "            in.close();"+
                       "        }"+                                                                     
                       "     }"+
                       "}",
                       "0:377-0:381:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { try (OutputStream out1 = new FileOutputStream(to1)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); } } } finally { in.close(); } }}"
        );
    }
    
    public void testTryTryTry() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.OutputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.FileOutputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test(File from, File to1, File to2) throws Exception {" +                       
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);"+
                       "         try {"+
                       "            final OutputStream out1 = new FileOutputStream(to1);"+
                       "            try {"+
                       "                final OutputStream out2 = new FileOutputStream(to2);"+
                       "                try {"+
                       "                    int len;"+
                       "                    while ((len = in.read(data)) > 0) {"+
                       "                        out1.write(data, 0, len);"+
                       "                        out2.write(data, 0, len);"+
                       "                    }"+
                       "                } finally {"+
                       "                    out2.close();"+
                       "                }"+
                       "            } finally {"+
                       "                out1.close();"+
                       "            }"+
                       "        } finally {"+
                       "            in.close();"+
                       "        }"+
                       "    }"+
                       "}",
                       "0:311-0:313:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from)) { final OutputStream out1 = new FileOutputStream(to1); try { final OutputStream out2 = new FileOutputStream(to2); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } finally { out2.close(); } } finally { out1.close(); } } }}"
                       );
        
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.OutputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.FileOutputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test(File from, File to1, File to2) throws Exception {" +                       
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);"+
                       "         try {"+
                       "            final OutputStream out1 = new FileOutputStream(to1);"+
                       "            try {"+
                       "                final OutputStream out2 = new FileOutputStream(to2);"+
                       "                try {"+
                       "                    int len;"+
                       "                    while ((len = in.read(data)) > 0) {"+
                       "                        out1.write(data, 0, len);"+
                       "                        out2.write(data, 0, len);"+
                       "                    }"+
                       "                } finally {"+
                       "                    out2.close();"+
                       "                }"+
                       "            } finally {"+
                       "                out1.close();"+
                       "            }"+
                       "        } finally {"+
                       "            in.close();"+
                       "        }"+
                       "    }"+
                       "}",
                       "0:387-0:391:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { try (OutputStream out1 = new FileOutputStream(to1)) { final OutputStream out2 = new FileOutputStream(to2); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } finally { out2.close(); } } } finally { in.close(); } }}"
                       );
        
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.OutputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.FileOutputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test(File from, File to1, File to2) throws Exception {" +                       
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);"+
                       "         try {"+
                       "            final OutputStream out1 = new FileOutputStream(to1);"+
                       "            try {"+
                       "                final OutputStream out2 = new FileOutputStream(to2);"+
                       "                try {"+
                       "                    int len;"+
                       "                    while ((len = in.read(data)) > 0) {"+
                       "                        out1.write(data, 0, len);"+
                       "                        out2.write(data, 0, len);"+
                       "                    }"+
                       "                } finally {"+
                       "                    out2.close();"+
                       "                }"+
                       "            } finally {"+
                       "                out1.close();"+
                       "            }"+
                       "        } finally {"+
                       "            in.close();"+
                       "        }"+
                       "    }"+
                       "}",
                       "0:472-0:476:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { final OutputStream out1 = new FileOutputStream(to1); try { try (OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } } finally { out1.close(); } } finally { in.close(); } }}"
                       );
    }
    
    public void testTryTryTryPathUp() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.OutputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.FileOutputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test(File from, File to1, File to2) throws Exception {" +                       
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);"+
                       "         try {"+
                       "            final OutputStream out1 = new FileOutputStream(to1);"+
                       "            try {"+
                       "                final OutputStream out2 = new FileOutputStream(to2);"+
                       "                try {"+
                       "                    int len;"+
                       "                    while ((len = in.read(data)) > 0) {"+
                       "                        out1.write(data, 0, len);"+
                       "                        out2.write(data, 0, len);"+
                       "                    }"+
                       "                } finally {"+
                       "                    out2.close();"+
                       "                }"+
                       "            } finally {"+
                       "                out1.close();"+
                       "            }"+
                       "        } finally {"+
                       "            in.close();"+
                       "        }"+
                       "    }"+
                       "}",
                       "0:472-0:476:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { final OutputStream out1 = new FileOutputStream(to1); try { try (OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } } finally { out1.close(); } } finally { in.close(); } }}"
                       );
        performFixTest("test/Test.java",
                "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { final OutputStream out1 = new FileOutputStream(to1); try { try (OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } } finally { out1.close(); } } finally { in.close(); } }}",
                "0:348-0:352:verifier:Convert to Automatic Resource Management",
                "FixImpl",
                "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { try ( OutputStream out1 = new FileOutputStream(to1); OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } } finally { in.close(); } }}"
                );
        performFixTest("test/Test.java",
                "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; final InputStream in = new FileInputStream(from); try { try ( OutputStream out1 = new FileOutputStream(to1); OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } } finally { in.close(); } }}",
                "0:291-0:293:verifier:Convert to Automatic Resource Management",
                "FixImpl",
                "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try ( InputStream in = new FileInputStream(from); OutputStream out1 = new FileOutputStream(to1); OutputStream out2 = new FileOutputStream(to2)) { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } }}"
                );
    }
    
    public void testTryTryTryPathDown() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.OutputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.FileOutputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test(File from, File to1, File to2) throws Exception {" +                       
                       "         final byte[] data = new byte[512];" +
                       "         final InputStream in = new FileInputStream(from);"+
                       "         try {"+
                       "            final OutputStream out1 = new FileOutputStream(to1);"+
                       "            try {"+
                       "                final OutputStream out2 = new FileOutputStream(to2);"+
                       "                try {"+
                       "                    int len;"+
                       "                    while ((len = in.read(data)) > 0) {"+
                       "                        out1.write(data, 0, len);"+
                       "                        out2.write(data, 0, len);"+
                       "                    }"+
                       "                } finally {"+
                       "                    out2.close();"+
                       "                }"+
                       "            } finally {"+
                       "                out1.close();"+
                       "            }"+
                       "        } finally {"+
                       "            in.close();"+
                       "        }"+
                       "    }"+
                       "}",
                       "0:311-0:313:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from)) { final OutputStream out1 = new FileOutputStream(to1); try { final OutputStream out2 = new FileOutputStream(to2); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } finally { out2.close(); } } finally { out1.close(); } } }}"
                       );
        performFixTest("test/Test.java",
                "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from)) { final OutputStream out1 = new FileOutputStream(to1); try { final OutputStream out2 = new FileOutputStream(to2); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } finally { out2.close(); } } finally { out1.close(); } } }}",
                "0:343-0:347:verifier:Convert to Automatic Resource Management",
                "FixImpl",
                "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test { public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try (InputStream in = new FileInputStream(from); OutputStream out1 = new FileOutputStream(to1)) {final OutputStream out2 = new FileOutputStream(to2); try { int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } } finally { out2.close(); } } }}"
                );
        performFixTest("test/Test.java",
                "package test;"+
                "import java.io.InputStream;"+
                "import java.io.OutputStream;"+
                "import java.io.FileInputStream;"+
                "import java.io.FileOutputStream;"+
                "import java.io.File;"+
                "public class Test {"+ 
                "public void test(File from, File to1, File to2) throws Exception {"+
                "   final byte[] data = new byte[512];"+
                "   try ( InputStream in = new FileInputStream(from);  OutputStream out1 = new FileOutputStream(to1)) {"+
                "       final OutputStream out2 = new FileOutputStream(to2);"+
                "       try {"+
                "           int len;"+
                "           while ((len = in.read(data)) > 0) {"+
                "               out1.write(data, 0, len);"+
                "               out2.write(data, 0, len);"+
                "           }"+
                "       } finally {"+
                "           out2.close();"+
                "       }"+
                "   }"+
                "}"+
                "}",
                "0:401-0:405:verifier:Convert to Automatic Resource Management",
                "FixImpl",
                "package test;import java.io.InputStream;import java.io.OutputStream;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.File;public class Test {public void test(File from, File to1, File to2) throws Exception { final byte[] data = new byte[512]; try ( InputStream in = new FileInputStream(from); OutputStream out1 = new FileOutputStream(to1); OutputStream out2 = new FileOutputStream(to2)) {int len; while ((len = in.read(data)) > 0) { out1.write(data, 0, len); out2.write(data, 0, len); } }}}"
                );
    }

    public void testSimpleVarDeclUsedAfterClose() throws Exception {
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
                       "         InputStream ins = new FileInputStream(\"\");"+
                       "         int r = ins.read();"+
                       "         ins.close();"+
                       "         System.out.println(r);"+
                       "     }" +
                       "}",
                       "0:210-0:213:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\");int r; try (InputStream ins = new FileInputStream(\"\")) { r = ins.read(); } System.out.println(r); }}");
    }

    public void testSimpleVarDeclUsedAfterClose2() throws Exception {
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
                       "         InputStream ins = new FileInputStream(\"\");"+
                       "         int r;" +
                       "         r = ins.read();"+
                       "         ins.close();"+
                       "         System.out.println(r);"+
                       "     }" +
                       "}",
                       "0:210-0:213:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\");int r; try (InputStream ins = new FileInputStream(\"\")) { r = ins.read(); } System.out.println(r); }}");
    }

    public void testComplexVarDeclUsedAfterClose() throws Exception {
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
                       "         InputStream ins = new FileInputStream(\"\");"+
                       "         int r1 = ins.read();"+
                       "         int r2 = ins.read();"+
                       "         int sum = r1 + r2;"+
                       "         ins.close();"+
                       "         System.out.println(r1);"+
                       "         System.out.println(sum);"+
                       "     }" +
                       "}",
                       "0:210-0:213:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\");int r1; int sum; try (InputStream ins = new FileInputStream(\"\")) { r1 = ins.read(); int r2 = ins.read(); sum = r1 + r2; } System.out.println(r1); System.out.println(sum); }}");
    }

    public void testResourceUsedAfterClose1() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "	InputStream ins = new FileInputStream(\"\");" +
                       "        ins.read();"+
                       "        ins.close();"+
                       "        ins = null;"+
                       "        System.out.println(r);"+
                       "     }" +
                       "}",
                       "0:201-0:204:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\");try (InputStream ins = new FileInputStream(\"\")) { ins.read(); } System.out.println(r); }}");
    }

    public void testResourceUsedAfterClose2() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performAnalysisTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "	InputStream ins = new FileInputStream(\"\");" +
                       "        ins.read();"+
                       "        ins.close();"+
                       "        ins.available();"+
                       "        System.out.println(r);"+
                       "     }" +
                       "}");
    }

    public void testResourceUsedAfterClose3() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "	InputStream ins = new FileInputStream(\"\");" +
                       "        ins.read();"+
                       "        ins.close();"+
                       "        if (true) {" +
                       "           ins = null;"+
                       "           System.out.println(r);"+
                       "        }"+
                       "     }" +
                       "}",
                       "0:201-0:204:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\");try (InputStream ins = new FileInputStream(\"\")) { ins.read(); } if (true) { System.out.println(r); } }}");
    }

    public void testNullResourceNoIfCheck() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performFixTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "        InputStream ins = null;" +
                       "        try {"+
                       "            ins = new FileInputStream(\"\");" +
                       "            ins.read();"+
                       "        } finally {"+
                       "             ins.close();"+
                       "        }"+
                       "        System.out.println(\"Done\");" +
                       "     }" +
                       "}",
                       "0:208-0:211:verifier:Convert to Automatic Resource Management",
                       "FixImpl",
                       "package test;import java.io.InputStream;import java.io.FileInputStream;import java.io.File;public class Test { public void test() throws Exception { System.out.println(\"Start\"); try (InputStream ins = new FileInputStream(\"\")) { ins.read(); } System.out.println(\"Done\"); }}");
    }

    public void testNoStatements() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performAnalysisTest("test/Test.java",
                       "package test;" +
                       "import java.io.InputStream;"+
                       "import java.io.FileInputStream;"+
                       "import java.io.File;"+
                       "public class Test {" +
                       "     public void test() throws Exception {" +
                       "        System.out.println(\"Start\");" +
                       "        InputStream ins = new FileInputStream(\"\");" +
                       "        ins.close();"+
                       "        System.out.println(\"Done\");" +
                       "     }" +
                       "}");
    }

    public void testNoARMHintForSourceLevelLessThen17() throws Exception {
        setSourceLevel("1.6");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performAnalysisTest("test/Test.java",
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
                       "}");
    }

    public void testAssignmentToResource() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "import java.io.InputStream;"+
                            "import java.io.FileInputStream;"+
                            "import java.io.File;"+
                            "public class Test {" +
                            "     public void test(boolean b) throws Exception {" +
                            "        System.out.println(\"Start\");" +
                            "        InputStream ins = null;" +
                            "        if (b) {" +
                            "            ins = new FileInputStream(\"\");" +
                            "        }" +
                            "        if (ins == null) {" +
                            "            ins = new FileInputStream(\"\");" +
                            "        }" +
                            "        ins.read();"+
                            "        ins.close();"+
                            "     }" +
                            "}");
    }
    
    public void testCannotSplitVariable() throws Exception {
        setSourceLevel("1.7");
        ConvertToARM.checkAutoCloseable = false;    //To allow run on JDK 1.6
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "import java.io.InputStream;"+
                            "import java.io.FileInputStream;"+
                            "import java.io.File;"+
                            "public class Test {" +
                            "     public int test(boolean b) throws Exception {" +
                            "        System.out.println(\"Start\");" +
                            "        InputStream ins = new FileInputStream(\"\");" +
                            "        if (b) {" +
                            "            int r = 0;" +
                            "            System.err.println(r);" +
                            "        }" +
                            "        int r = ins.read();"+
                            "        ins.close();"+
                            "        return r;"+
                            "     }" +
                            "}");
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return "FixImpl";
    }
}
