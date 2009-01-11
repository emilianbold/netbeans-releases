/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.editor;

import java.net.URL;
import java.util.List;

/**
 *
 * @author Tor Norbye
 */
public class PythonCodeCompleterTest extends PythonTestBase {
    public PythonCodeCompleterTest(String testName) {
        super(testName);
    }

    private boolean skipJython = false;

    @Override
    protected boolean skipRhs() {
        return true;
    }

    @Override
    protected List<URL> getExtraCpUrls() {
        // I'm overriding various Jython classes here for tests which causes
        // confusion when it's trying to locate classes and finds it in multiple places
        if (!skipJython) {
            return super.getExtraCpUrls();
        }

        return null;
    }

    public void testLocals1() throws Exception {
        checkCompletion("testfiles/ConfigParser.py", "s^elf.section = section", true);
    }

    public void testClasses2() throws Exception {
        checkCompletion("testfiles/ConfigParser.py", "raise D^uplicateSectionError(section)", true);
    }

    public void testStringCompletion() throws Exception {
        checkCompletion("testfiles/ConfigParser.py", "in \"^rR\"", true);
    }

    public void testNoStringCompletion() throws Exception {
        checkCompletion("testfiles/test_scope.py", "r\"i^mport", true);
    }

    public void testImports2() throws Exception {
        checkCompletion("testfiles/ConfigParser.py", "import r^e", true);
    }

    public void testImports7() throws Exception {
        checkCompletion("testfiles/imports7.py", "import ^", true);
    }

    public void testImports8() throws Exception {
        checkCompletion("testfiles/imports8.py", "import distutils.com^mand", true);
    }

    public void testImports9() throws Exception {
        checkCompletion("testfiles/imports9.py", "from difflib import ^c", true);
    }

    public void testDoc1() throws Exception {
        try {
            skipJython = true;
            checkCompletionDocumentation("testfiles/SocketServer.py", "class TCPServer(BaseSer^ver):", true, "BaseServer");
        } finally {
            skipJython = false;
        }
    }

    public void testDoc2() throws Exception {
        try {
            skipJython = true;
            checkCompletionDocumentation("testfiles/SocketServer.py", "class BaseRequestHandler^:", true, "BaseRequestHandler");
        } finally {
            skipJython = false;
        }
    }

    public void testDoc3() throws Exception {
        try {
            skipJython = true;
            checkCompletionDocumentation("testfiles/pickle.py", "import ^re", true, "pickle");
        } finally {
            skipJython = false;
        }
    }

    public void testDoc5() throws Exception {
        try {
            skipJython = true;
            // Treat r"""raw strings""" specially - as preformatted content perhaps?
            checkCompletionDocumentation("testfiles/pickle.py", "decode_lo^ng", true, "decode_long");
        } finally {
            skipJython = false;
        }
    }

    public void testFutureImport1() throws Exception {
        try {
            skipJython = true;
            checkCompletion("testfiles/futureimport.py", "import ^", true);
        } finally {
            skipJython = false;
        }
    }

    public void testDoc6() throws Exception {
        // Find documentation for a method that doesn't actually have a def - it's an alias
        checkCompletionDocumentation("testfiles/compl5.py", "self.assertA^lmostEquals(1,2)", true, "assertAlmostEquals");
    }

    public void testObjMethodCompletion2() throws Exception {
        checkCompletion("testfiles/compl.py", "self.^my", true);
    }

    public void testObjMethodCompletion3() throws Exception {
        // Test that imports of symbols in a
        checkCompletion("testfiles/compl2.py", "print w^hatever.r", true);
    }

    public void testObjMethodCompletion5() throws Exception {
        checkCompletion("testfiles/compl2.py", "print whatever.r^", true);
    }

    public void testObjMethodCompletion9() throws Exception {
        checkCompletion("testfiles/compl4.py", "faen.e^", true);
    }

    public void testObjMethodCompletion10() throws Exception {
        checkCompletion("testfiles/compl5.py", "unittest.T^", true);
    }

    public void testObjMethodCompletion11() throws Exception {
        checkCompletion("testfiles/compl5.py", "x.^r", true);
    }

    public void testObjMethodConstructors1() throws Exception {
        checkCompletion("testfiles/compl2.py", "myvar.^close()", true);
    }

    public void testObjMethodConstructors2() throws Exception {
        checkCompletion("testfiles/compl2.py", "myothervar.^", true);
    }

    public void testObjMethodConstructors3() throws Exception {
        // Not a calling a recognized type - all-completion
        checkCompletion("testfiles/compl2.py", "unknown.fai^", true);
    }

    public void testOverride1() throws Exception {
        // Not a calling a recognized type - all-completion
        checkCompletion("testfiles/compl5.py", "MyTest(unittest.TestCase):\n  ^", true);
    }

    public void testOverride2() throws Exception {
        // Not a calling a recognized type - all-completion
        checkCompletion("testfiles/compl5.py", "def ^f(self)", true);
    }

    public void testLocals3() throws Exception {
        checkCompletion("testfiles/occurrences2.py", "tople^velvar = 1", true);
    }

    public void testLocals7() throws Exception {
        checkCompletion("testfiles/occurrences2.py", "print t^oplevelvar2", true);
    }

    public void testKeywordFrom() throws Exception {
        // See http://www.netbeans.org/issues/show_bug.cgi?id=154131
        checkCompletion("testfiles/org.py", "from or^", true);
    }

    public void testProperties() throws Exception {
        checkCompletion("testfiles/properties.py", "x.ba^", true);
    }

    public void testParameters1() throws Exception {
        checkCompletion("testfiles/complete-calls.py", "functionfoo(foo^, bar)", true);
    }

    public void testParameters2() throws Exception {
        checkCompletion("testfiles/complete-calls.py", "functionfoo(foo, bar^)", true);
    }

    public void testParameters3() throws Exception {
        checkCompletion("testfiles/complete-calls.py", "y.foo(x^yz)", true);
    }

    public void testParameters4() throws Exception {
        checkCompletion("testfiles/complete-calls.py", "y.foo(xyz, b^az)", true);
    }

    public void testParameters5() throws Exception {
        checkCompletion("testfiles/complete-calls.py", "functionfoo(\"foo\", \"bar\", inval^id)", true);
    }

    public void testDecorators1() throws Exception {
        checkCompletion("testfiles/emptydecorators.py", "@^", true);
    }

    public void testDecorators2() throws Exception {
        checkCompletion("testfiles/decorators.py", "@c^", true);
    }

    public void testTypedVars1() throws Exception {
        checkCompletion("testfiles/compl5.py", "os2.^", true);
    }

    public void testTypedVars2() throws Exception {
        checkCompletion("testfiles/compl5.py", "os3.^", true);
    }

    public void testTypedVars3() throws Exception {
        checkCompletion("testfiles/compl5.py", "os4.^", true);
    }

    public void testTypedVars4() throws Exception {
        // No type specified
        checkCompletion("testfiles/compl5.py", "os5.xhd^", true);
    }

    // -------------------------
    // Unstable tests:
    // -------------------------
    //
    //public void testObjMethodCompletion4() throws Exception {
    //    checkCompletion("testfiles/compl2.py", "print whatever.^r", true);
    //}
    //public void testObjMethodCompletion6() throws Exception {
    //    checkCompletion("testfiles/compl2.py", "print sys.getfile^systeme", true);
    //}
    //public void testObjMethodCompletion7() throws Exception {
    //    checkCompletion("testfiles/compl2.py", "print os.^", true);
    //}
    //public void testObjMethodCompletion8() throws Exception {
    //    checkCompletion("testfiles/compl3.py", "server=SocketServer.^TCPServer", true);
    //}
    //public void testLocals4() throws Exception {
    //    checkCompletion("testfiles/occurrences2.py", "print ^toplevelvar2", true);
    //}
    //public void testLocals5() throws Exception {
    //    checkCompletion("testfiles/occurrences2.py", "x = ^myfunc", true);
    //}
    //public void testDoc4() throws Exception {
    //    try {
    //        skipJython = true;
    //        checkCompletionDocumentation("testfiles/SocketServer.py", "def serve_fore^ver(self):", true, "serve_forever");
    //    } finally {
    //        skipJython = false;
    //    }
    //}
    //public void testObjMethodCompletion1() throws Exception {
    //    checkCompletion("testfiles/compl.py", "^self.my", true);
    //}
    //public void testImports3() throws Exception {
    //    checkCompletion("testfiles/imports3.py", "import ^", true);
    //}
    //public void testImports4() throws Exception {
    //    checkCompletion("testfiles/imports4.py", "import ^", true);
    //}
    //public void testImports5() throws Exception {
    //    checkCompletion("testfiles/imports5.py", "import i^", true);
    //}
    //public void testImports6() throws Exception {
    //    checkCompletion("testfiles/imports6.py", "from ^i", true);
    //}
    //public void testImports6b() throws Exception {
    //    checkCompletion("testfiles/imports6.py", "from i^", true);
    //}
    //public void testKeywords() throws Exception {
    //    checkCompletion("testfiles/empty.py", "^print", true);
    //}
    //public void testLocals2() throws Exception {
    //    checkCompletion("testfiles/datetime.py", "^dnum = _days_before_month(y, m) + d", true);
    //}
    //public void testClasses() throws Exception {
    //    checkCompletion("testfiles/ConfigParser.py", "^self.section = section", true);
    //}
    //public void testImports1() throws Exception {
    //    checkCompletion("testfiles/ConfigParser.py", "import ^re", true);
    //}
}
