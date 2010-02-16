/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author lahvac
 */
public class ThrowableInitCauseTest extends TestBase {

    public ThrowableInitCauseTest(String name) {
        super(name, ThrowableInitCause.class);
    }

    public void testSimple1() throws Exception {
        performFixTest("test/Test.java",
                            "package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             throw (IllegalStateException) new IllegalStateException(e.toString()).initCause(e);\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "6:19-6:95:verifier:ERR_ThrowableInitCause",
                            "FIX_ThrowableInitCause",
                            ("package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             throw new IllegalStateException(e);\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testSimple2() throws Exception {
        performFixTest("test/Test.java",
                            "package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             throw (IllegalStateException) new IllegalStateException(\"a\").initCause(e);\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "6:19-6:86:verifier:ERR_ThrowableInitCause",
                            "FIX_ThrowableInitCause",
                            ("package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             throw new IllegalStateException(\"a\", e);\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testSimple3() throws Exception {
        performFixTest("test/Test.java",
                            "package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             IllegalStateException ex = new IllegalStateException(e.toString());\n" +
                            "             ex.initCause(e);" +
                            "             throw ex;\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "6:13-6:80:verifier:ERR_ThrowableInitCause",
                            "FIX_ThrowableInitCause",
                            ("package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             throw new IllegalStateException(e);\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testSimpleNoStringArgNotStrict() throws Exception {
        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.jdk.ThrowableInitCause", HintsSettings.getCurrentProfileId());

        p.putBoolean(ThrowableInitCause.STRICT_KEY, false);
        performFixTest("test/Test.java",
                            "package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             IllegalStateException ex = new IllegalStateException();\n" +
                            "             ex.initCause(e);" +
                            "             throw ex;\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "6:13-6:68:verifier:ERR_ThrowableInitCause",
                            "FIX_ThrowableInitCause",
                            ("package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             throw new IllegalStateException(e);\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testSimpleNoStringArgNotStrict2() throws Exception {
        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.jdk.ThrowableInitCause", HintsSettings.getCurrentProfileId());

        p.putBoolean(ThrowableInitCause.STRICT_KEY, false);
        performFixTest("test/Test.java",
                            "package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         Exception[] blex = null;\n" +
                            "         IOException ioe = new IOException(blex[0].getMessage());\n" +
                            "         ioe.initCause(blex[0]);\n" +
                            "         throw ioe;\n" +
                            "     }\n" +
                            "}\n",
                            "4:9-4:65:verifier:ERR_ThrowableInitCause",
                            "FIX_ThrowableInitCause",
                            ("package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         Exception[] blex = null;\n" +
                            "         throw new IOException(blex[0]);\n" +
                            "     }\n" +
                            "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testSimpleNoStringArgStrict() throws Exception {
        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.jdk.ThrowableInitCause", HintsSettings.getCurrentProfileId());

        p.putBoolean(ThrowableInitCause.STRICT_KEY, true);
        performFixTest("test/Test.java",
                            "package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             IllegalStateException ex = new IllegalStateException();\n" +
                            "             ex.initCause(e);" +
                            "             throw ex;\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "6:13-6:68:verifier:ERR_ThrowableInitCause",
                            "FIX_ThrowableInitCause",
                            ("package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             throw new IllegalStateException(null, e);\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testFinalVariable() throws Exception {
        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.jdk.ThrowableInitCause", HintsSettings.getCurrentProfileId());

        p.putBoolean(ThrowableInitCause.STRICT_KEY, true);
        performFixTest("test/Test.java",
                            "package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             final IllegalStateException ex = new IllegalStateException();\n" +
                            "             ex.initCause(e);" +
                            "             throw ex;\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "6:13-6:74:verifier:ERR_ThrowableInitCause",
                            "FIX_ThrowableInitCause",
                            ("package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             throw new IllegalStateException(null, e);\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testExpression() throws Exception {
        Preferences p = RulesManager.getPreferences("org.netbeans.modules.java.hints.jdk.ThrowableInitCause", HintsSettings.getCurrentProfileId());

        p.putBoolean(ThrowableInitCause.STRICT_KEY, false);
        performFixTest("test/Test.java",
                            "package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private Expression test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             return (IllegalStateException) new IllegalStateException(e.toString()).initCause(e);\n" +
                            "         }\n" +
                            "         return null;\n" +
                            "     }\n" +
                            "}\n",
                            "6:20-6:96:verifier:ERR_ThrowableInitCause",
                            "FIX_ThrowableInitCause",
                            ("package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private Expression test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             return new IllegalStateException(e);\n" +
                            "         }\n" +
                            "         return null;\n" +
                            "     }\n" +
                            "}\n").replaceAll("[ \t\n]+", " "));
    }

    //TODO:
    public void XtestMoreStatementsNoThrow() throws Exception {
        performFixTest("test/Test.java",
                            "package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             IllegalStateException ex = new IllegalStateException(e.toString());\n" +
                            "             ex.initCause(e);" +
                            "             ex.printStackTrace();\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "TODO",
                            "FIX_ThrowableInitCause",
                            ("package test;" +
                            "import java.io.IOException;" +
                            "import java.util.ArrayList;\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         try {\n" +
                            "             throw new IOException(\"a\");\n" +
                            "         } catch (IOException e) {\n" +
                            "             IllegalStateException ex = new IllegalStateException(e);\n" +
                            "             ex.printStackTrace();\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n").replaceAll("[ \t\n]+", " "));
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

}