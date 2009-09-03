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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

public class JsFormatterTest extends JsTestBase {
    
    public JsFormatterTest(String testName) {
        super(testName);
    }            

    // Used to test arbitrary source trees
    //public void testReformatSourceTree() {
    //    List<FileObject> files = new ArrayList<FileObject>();
    //
    //    // Used to test random source trees
    //    File f = new File("/Users/tor/Desktop/facets-1.8.54"); // NOI18N
    //    FileObject root = FileUtil.toFileObject(f);
    //    addAllRubyFiles(root, files);
    //    reformatAll(files);
    //}
    //
    //private void addAllRubyFiles(FileObject file, List<FileObject> files) {
    //    if (file.isFolder()) {
    //        for (FileObject c : file.getChildren()) {
    //            addAllRubyFiles(c, files);
    //        }
    //    } else if (file.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
    //        files.add(file);
    //    }
    //}

    public void testSemi01() throws Exception {
        format(
                "var p; p = 'hello';",
                "var p;\n" +
                "p = 'hello';", null
                );
    }

    public void testSemi02() throws Exception {
        format(
                "var p;                           p = 'hello';",
                "var p;\n" +
                "p = 'hello';", null
                );
    }

    public void testSemi03() throws Exception {
        format(
                "var p;p = 'hello';",
                "var p;\n" +
                "p = 'hello';", null
                );
    }

    public void testSemi04() throws Exception {
        format(
                "var p; p = getName(); p = stripName(p);",
                "var p;\n" +
                "p = getName();\n" +
                "p = stripName(p);", null
                );
    }
    
    public void testSemi05() throws Exception {
        format(
                "var p; for(var i = 0, l = o.length; i < l; i++) {             createDom(o[i], el);   p = true;} p = stripName(p);",
                "var p;\n" +
                "for(var i = 0, l = o.length; i < l; i++) {\n" +
                "    createDom(o[i], el);\n" +
                "    p = true;\n" +
                "}\n" +
                "p = stripName(p);", null
                );
    }

    public void testSemi06() throws Exception {
        format(
                "if (a == b) { a=c;\n" +
                "    } else if (c == b) { v=d;}",

                "if (a == b) {\n" +
                "    a=c;\n" +
                "} else if (c == b) {\n" +
                "    v=d;\n" +
                "}", null);
    }

    public void testSemi07() throws Exception {
        format(
                "var test = function() { a = b; };",

                "var test = function() {\n" +
                "    a = b;\n" +
                "};", null);
    }

    public void testSemi08() throws Exception {
        format(
                "Spry.forwards = 1; // const\n" +
                "Spry.backwards = 2; // const\n",

                "Spry.forwards = 1; // const\n" +
                "Spry.backwards = 2; // const\n", null);
    }


    public void testCommentAtTheEdnOfLine() throws Exception {
        format (
                "for(var i = 0, l = o.length; i < l; i++) { // some comment \ncreateDom(o[i], el);  p = true;       } //comment2\n p = stripName(p);",
                "for(var i = 0, l = o.length; i < l; i++) { // some comment \n" +
                "    createDom(o[i], el);\n" +
                "    p = true;\n" +
                "} //comment2\n" +
                "p = stripName(p);", null);
    }

    public void testFormat1() throws Exception {
        // Check that the given source files reformat EXACTLY as specified
        reformatFileContents("testfiles/prototype.js",new IndentPrefs(2,2));
    }

    public void testFormat2() throws Exception {
        reformatFileContents("testfiles/SpryEffects.js",new IndentPrefs(2,2));
    }

    public void testFormat3() throws Exception {
        reformatFileContents("testfiles/dragdrop.js",new IndentPrefs(2,2));
    }

    public void testFormat4() throws Exception {
        reformatFileContents("testfiles/orig-dojo.js.uncompressed.js",new IndentPrefs(2,2));
    }

    public void testFormatE4x() throws Exception {
        reformatFileContents("testfiles/e4x.js", new IndentPrefs(4,4));
    }

    public void testFormatE4x2() throws Exception {
        reformatFileContents("testfiles/e4x2.js", new IndentPrefs(4,4));
    }

    public void testFormatTryCatch() throws Exception {
        reformatFileContents("testfiles/tryblocks.js", new IndentPrefs(4,4));
    }

    public void testFormatPrototypeNew() throws Exception {
        reformatFileContents("testfiles/prototype-new.js", new IndentPrefs(2,2));
    }

    public void testFormatSwitches() throws Exception {
        reformatFileContents("testfiles/switches.js", new IndentPrefs(4,4));
    }

    public void testFormatWebui() throws Exception {
        reformatFileContents("testfiles/bubble.js", new IndentPrefs(4,4));
    }

    public void testFormatWebui2() throws Exception {
        reformatFileContents("testfiles/woodstock-body.js", new IndentPrefs(4,4));
    }

    public void testIssue144248() throws Exception {
        reformatFileContents("testfiles/issue144248.js", new IndentPrefs(4,4));
    }

    public void testIssue144248b() throws Exception {
        reformatFileContents("testfiles/issue144248-minimal.js", new IndentPrefs(4,4));
    }

    public void testSimpleBlock() throws Exception {
        format("if (true) {\nfoo();\n  }\n",
               "if (true) {\n    foo();\n}\n", null);
        format("if(true) {\nfoo();\n  }\n",
               "if(true) {\n    foo();\n}\n", null);
        format("if (true){\nfoo();\n  }\n",
               "if (true){\n    foo();\n}\n", null);
        format("if(true){\nfoo();\n  }\n",
               "if(true){\n    foo();\n}\n", null);
        format("if (true){\nfoo();\n  }\n",
               "if (true){\n    foo();\n}\n", null);
        format(
                " if (true)\n" +
                " {\n" +
                " foo();\n" +
                " }",
                "if (true)\n" +
                "{\n" +
                "    foo();\n" +
                "}", null
                );
         format(
                "if (true) x = {};\n" +
                "foo()\n" +
                "{\n" +
                "bar();\n" +
                "}",
                "if (true) x = {};\n" +
                "foo()\n" +
                "{\n" +
                "    bar();\n" +
                "}", null
                );
    }

    public void testCombinedBlocks() throws Exception {
        format(
                " if (true)\n" +
                " if (false) {\n" +
                " foo();\n" +
                " }\n" +
                " bar();",
                "if (true)\n" +
                "    if (false) {\n" +
                "        foo();\n" +
                "    }\n" +
                "bar();", null
                );
        format(
                " if (true) {\n" +
                " if (map[0])\n" +
                " foo();\n" +
                " }",
                "if (true) {\n" +
                "    if (map[0])\n" +
                "        foo();\n" +
                "}", null
                );
    }

    public void testBraceFreeBlock() throws Exception {
        // it's good to start lines with space, it discovers some potential problems
        format(
                " if (true) foo()\n" +
                " bar();\n",
               "if (true) foo()\n" +
               "bar();\n", null);
        format(
                " if (true)\n" +
                " foo();\n" +
                " bar();\n",
               "if (true)\n" +
               "    foo();\n" +
               "bar();\n", null);
        format(
                " if (true)\n" +
                " if (true)\n" +
                " foo();\n",
               "if (true)\n" +
               "    if (true)\n" +
               "        foo();\n", null);
        format(
                " if (true)\n" +
                " foo();\n" +
                " else\n" +
                " bar();\n",
               "if (true)\n" +
               "    foo();\n" +
               "else\n" +
               "    bar();\n", null);
        format(
                " while (true)\n" +
                " foo();\n" +
                " bar();\n",
               "while (true)\n" +
               "    foo();\n" +
               "bar();\n", null);
        format(
                " for (i = 0; i < 5; i++)\n" +
                " foo();\n" +
                " bar();",
               "for (i = 0; i < 5; i++)\n" +
               "    foo();\n" +
               "bar();", null);
        format(
                " if (true &&\n" +
                " true)\n" +
                " foo();",
                "if (true &&\n" +
                "    true)\n" +
                "    foo();", null
                );
        format(
                " if (true)\n" +
                " for (var a in b)\n" +
                " foo();\n" +
                " else\n" +
                " bar();",
                "if (true)\n" +
                "    for (var a in b)\n" +
                "        foo();\n" +
                "else\n" +
                "    bar();", null
                );
        format(
                " if (true) // comment\n" +
                " foo();",
                "if (true) // comment\n" +
                "    foo();", null
                );
        // What about thesed: do? with?
    }

    public void testFor() throws Exception {
        format(
                "for (var property in source) {\n" +
                "        destination[property] = source[property];\n" +
                "    }\n" +
                " foo();",
                "for (var property in source) {\n" +
                "    destination[property] = source[property];\n" +
                "}\n" +
                "foo();", null
                );
    }

    public void testFor2() throws Exception {
        format(
                "for (var i = 0; i < length; i++)\n" +
                "foo();\n" +
                "bar();\n",
                "for (var i = 0; i < length; i++)\n" +
                "    foo();\n" +
                "bar();\n", null
                );
    }

    public void testLineContinuationAsgn() throws Exception {
        format("x =\n1",
               "x =\n    1", null);
    }

    public void testLineContinuation2() throws Exception {
        format("x =\n1", 
                // No separate setting for this yet - can't test
               //"x =\n    1", new IndentPrefs(2,4));
               "x =\n  1", new IndentPrefs(2,2));
    }

    public void testLineContinuation3() throws Exception {
        format("x =\n1\ny = 5", 
               //"x =\n    1\ny = 5", new IndentPrefs(2,4));
               "x =\n  1\ny = 5", new IndentPrefs(2,2));
    }

    public void testQuestionmarkIndent1() throws Exception {
        format("j = t ?\n1 : 0\nx = 1",
                "j = t ?\n    1 : 0\nx = 1", null);
    }

    public void testQuestionmarkIndent2() throws Exception {
        format("j = t ?\n1 :\n0\nx = 1",
                "j = t ?\n    1 :\n    0\nx = 1", null);
    }

    public void testSwitch1() throws Exception {
        format(
                " switch (n) {\n" +
                " case 0:\n" +
                " case 1:\n" +
                " // comment\n" +
                " foo();\n" +
                " break;\n" +
                " default: break;\n",
                "switch (n) {\n" +
                "    case 0:\n" +
                "    case 1:\n" +
                "        // comment\n" +
                "        foo();\n" +
                "        break;\n" +
                "    default:\n" +
                "        break;\n", null
                );
    }
    
    public void testSwitch2() throws Exception {
        format(
                " switch (n) {\n" +
                " case 1:\n" +
                " foo();\n" +
                " foo2();\n" +
                " case 2:\n" +
                " bar();\n" +
                " break;\n" +
                " default:\n" +
                " bar2();\n" +
                " bar3();\n" +
                " break;\n" +
                " }",
                "switch (n) {\n" +
                "    case 1:\n" +
                "        foo();\n" +
                "        foo2();\n" +
                "    case 2:\n" +
                "        bar();\n" +
                "        break;\n" +
                "    default:\n" +
                "        bar2();\n" +
                "        bar3();\n" +
                "        break;\n" +
                "}", null
                );
    }
    
    public void testSwitch3() throws Exception {
        format(
                " switch (n) {\n" +
                " case '1':\n" +
                " case '2':\n" +
                " case '3':\n" +
                " case '4':\n" +
                " }\n",
                "switch (n) {\n" +
                "    case '1':\n" +
                "    case '2':\n" +
                "    case '3':\n" +
                "    case '4':\n" +
                "}\n", null
                );
    }
    
    public void testSwitch4() throws Exception {
        format(
                " switch (n) {\n" +
                " case 1: foo(); break;\n" +
                " default:            bar();\n" +
                " }\n" +
                " bar();\n",
                "switch (n) {\n" +
                "    case 1:\n" +
                "        foo();\n" +
                "        break;\n" +
                "    default:\n" +
                "        bar();\n" +
                "}\n" +
                "bar();\n", null
                );
    }
    
    public void testSwitch5() throws Exception {
        format(
                " switch(n) {\n" +
                " case 1: foo(); break;\n" +
                " default: bar();\n" +
                " }\n" +
                " bar();\n",
                "switch(n) {\n" +
                "    case 1:\n" +
                "        foo();\n" +
                "        break;\n" +
                "    default:\n" +
                "        bar();\n" +
                "}\n" +
                "bar();\n", null
                );
    }

    public void testSwitch6() throws Exception {
        format(
                " switch (n) {   //comment1\n" +
                " case 0:\n" +
                " case 1:    // comment2\n" +
                " foo();\n" +
                " break;\n" +
                " default:   // comment3\nbreak;\n",

                "switch (n) {   //comment1\n" +
                "    case 0:\n" +
                "    case 1:    // comment2\n" +
                "        foo();\n" +
                "        break;\n" +
                "    default:   // comment3\n" +
                "        break;\n", null
                );
    }

    public void testCommaIndent1() throws Exception {
        insertNewline("hobbies: [ \"chess\",^", "hobbies: [ \"chess\",\n    ^", null);
    }
    
    public void testIfIndent() throws Exception {
        insertNewline("if (true)^\n    foo();", "if (true)\n    ^\n    foo();", null);
    }
    
    public void testParensIndent1() throws Exception {
        insertNewline(
                "if (true)\n" +
                "    for (var a in b)\n" +
                "        foo();^",
                "if (true)\n" +
                "    for (var a in b)\n" +
                "        foo();\n" +
                "^", null
                );
    }
    
    public void testParensIndent2() throws Exception {
        insertNewline(
                "if (true)\n" +
                "    for (var a in b) {\n" +
                "        foo();^",
                "if (true)\n" +
                "    for (var a in b) {\n" +
                "        foo();\n" +
                "        ^", null
                );
    }
    
    public void testParensIndent3() throws Exception {
        insertNewline(
                "if (true) {\n" +
                "    for (var a in b)\n" +
                "        foo();^",
                "if (true) {\n" +
                "    for (var a in b)\n" +
                "        foo();\n" +
                "    ^", null
                );
    }
    
    public void testQuestionmarkIndent3() throws Exception {
        insertNewline("j = t ?^",
                "j = t ?\n    ^", null);
    }

    public void testDotIndent() throws Exception {
        insertNewline("puts foo.^", "puts foo.\n    ^", null);
    }

    public void testIndent1() throws Exception {
        insertNewline("x = [^[5]\n]\ny", "x = [\n    ^[5]\n]\ny", null);
    }

    public void testIndent2() throws Exception {
        insertNewline("x = ^", "x = \n    ^", null);
        insertNewline("x = ^ ", "x = \n    ^", null);

        // No separate setting for hanging indent yet - using same as indent
        //insertNewline("x = ^", "x = \n    ^", new IndentPrefs(2,4));
        insertNewline("x = ^", "x = \n  ^", new IndentPrefs(2,2));
    }

    public void testIndent3() throws Exception {
        insertNewline("      var foo^", "      var foo\n      ^", null);
    }
    
    public void testBraceNewline1() throws Exception {
        format(
                "if (true) { foo(); } else { bar(); }",
                "if (true) {\n" +
                "    foo();\n" +
                "} else {\n" +
                "    bar();\n" +
                "}", null
                );
    }
    
    public void testBraceNewline2() throws Exception {
        format(
                "var Prototype = {\n" +
                "    emptyFunction: function() { },\n" +
                "    K: function(x) { return x },\n" +
                "    L: function(x) { return x }\n" +
                "}",
                "var Prototype = {\n" +
                "    emptyFunction: function() { },\n" +
                "    K: function(x) {\n" +
                "        return x\n" +
                "    },\n" +
                "    L: function(x) {\n" +
                "        return x\n" +
                "    }\n" +
                "}", null
                );
    }
    
    public void testBraceNewline3() throws Exception {
        format(
                "var Prototype = {\n" +
                "    emptyFunction: function() {\n" +
                "    },\n" +
                "    K: function(x) {\n" +
                "        return x\n" +
                "    },\n" +
                "    L: function(x) {\n" +
                "        return x\n" +
                "    }\n" +
                "};",
                "var Prototype = {\n" +
                "    emptyFunction: function() {\n" +
                "    },\n" +
                "    K: function(x) {\n" +
                "        return x\n" +
                "    },\n" +
                "    L: function(x) {\n" +
                "        return x\n" +
                "    }\n" +
                "};", null
                );
    }

    public void testBraceNewline4() throws Exception {
        format (
                "if (this.options.asynchronous)\n" +
                "setTimeout(function() { this.respondToReadyState(1) }.bind(this), 10);",

                "if (this.options.asynchronous)\n" +
                "    setTimeout(function() {\n" +
                "        this.respondToReadyState(1)\n" +
                "    }.bind(this), 10);", null);
    }

    public void testBraceNewline5() throws Exception {
        format (
            "do {        valueT += element.scrollTop  || " +
            "0; valueL += element.scrollLeft || 0; element = element.parentNode; }         while (element);",

            "do {\n" +
            "    valueT += element.scrollTop  || 0;\n" +
            "    valueL += element.scrollLeft || 0;\n" +
            "    element = element.parentNode;\n" +
            "} while (element);", null);
    }

    public void testBraceNewline6() throws Exception {
        format(
                "if(a) {  if(b) {  a=b;  } else {  b=a;   } }",

                "if(a) {\n" +
                "    if(b) {\n" +
                "        a=b;\n" +
                "    } else {\n" +
                "        b=a;\n" +
                "    }\n" +  
                "}", null);
    }

    public void testBraceNewline7() throws Exception {
        format(
                "if(a) {  if(b) {  a=b;  } else {  b=a;   }}",

                "if(a) {\n" +
                "    if(b) {\n" +
                "        a=b;\n" +
                "    } else {\n" +
                "        b=a;\n" +
                "    }\n" +
                "}", null);
    }

    public void testBraceNewline8() throws Exception {
        format ("var miArgs = [{}];", "var miArgs = [{}];", null);
    }

    public void testBraceNewline9() throws Exception {
        format ("var p = <paul><age>{num}</age></paul>",

                "var p = <paul><age>{\n" +
                "num\n" +
                "}</age></paul>", null);
    }

    public void testBraceNewline10() throws Exception {
        format ("if (!document.getElementsByClassName) document.getElementsByClassName = function(instanceMethods){\n" +
                "  function iter(name) {\n" +
                "    return name.blank() ? null : \"[contains(concat(' ', @class, ' '), ' \" + name + \" ')]\";\n" +
                "  }\n" +
                "}(Element.Methods);",

                "if (!document.getElementsByClassName) document.getElementsByClassName = function(instanceMethods){\n" +
                "    function iter(name) {\n" +
                "        return name.blank() ? null : \"[contains(concat(' ', @class, ' '), ' \" + name + \" ')]\";\n" +
                "    }\n" +
                "}(Element.Methods);", null);
    }

    public void testBraceNewLine11() throws Exception {
        format("switch (tagName) {\n" +
                "case 'foo': {   \n" +
                "foo(); }\n" +
                "}",

                "switch (tagName) {\n" +
                "    case 'foo': {\n" +
                "        foo();\n" +
                "    }\n" +
                "}", null);
    }

    public void testBraceNewLine12 () throws Exception {
        format("if (a == b) { if (b == c) { a = c;}}",

                "if (a == b) {\n" +
                "    if (b == c) {\n" +
                "        a = c;\n" +
                "    }\n" +
                "}", null);
    }

    public void testFunction1() throws Exception {
        format(
                " toQueryString: function(obj) {\n" +
                "\t  this.prototype._each.call(obj, function(pair) {\n" +
                "\t  foo();\n" +
                "\t  });\n" +
                " }\n",
                "toQueryString: function(obj) {\n" +
                "    this.prototype._each.call(obj, function(pair) {\n" +
                "        foo();\n" +
                "    });\n" +
                "}\n", null);
    }
    
    public void testBlocks() throws Exception {
        format(
                " while (true) {\n" +
                "   if (true) {\n" +
                "     foo();\n" +
                "    }\n" +
                "    }",
                "while (true) {\n" +
                "    if (true) {\n" +
                "        foo();\n" +
                "    }\n" +
                "}", null
                );
        format(
                "if (true)\n" +
                "foo();",
                "if (true)\n" +
                "    foo();", null
                );
        format(
                " Object.extend = function() {\n" +
                "    foo();\n" +
                "  }",
                "Object.extend = function() {\n" +
                "    foo();\n" +
                "}", null
                );
    }
    
    public void testRegexp() throws Exception {
        format(
                "dojo.isAlien = function(/*anything*/ it){\n" +
                "        return it && !dojo.isFunction(it) && /\\{\\s*\\[native code\\]\\s*\\}/.test(String(it)); // Boolean\n" +
                "}", 
                "dojo.isAlien = function(/*anything*/ it){\n" +
                "    return it && !dojo.isFunction(it) && /\\{\\s*\\[native code\\]\\s*\\}/.test(String(it)); // Boolean\n" +
                "}", null
                );
    }
    
    public void testComment() throws Exception {
        format(
                "foo = function(/* foo() {}*/ it) { // call foo() { bar(); }\n" +
                "  bar();\n" +
                " }",
                "foo = function(/* foo() {}*/ it) { // call foo() { bar(); }\n" +
                "    bar();\n" +
                "}", null
                );
    }
    
    public void testExpression() throws Exception {
        format(
                "foo();\n" +
                "Foo.Bar.name = bar();",
                "foo();\n" +
                "Foo.Bar.name = bar();", null
                );
    }
    
    public void testTryCatch1() throws Exception {
        format(
                "function myfunc() {\n" +
                "   try {\n" +
                "       in_try_block();\n" +
                "   } catch ( e if e == \"InvalidNameException\"  ) {\n" +
                "       in_first_catch();\n" +
                "   } finally {\n" +
                "       in_finally();\n" +
                "   }\n" +
                "}",
                "function myfunc() {\n" +
                "    try {\n" +
                "        in_try_block();\n" +
                "    } catch ( e if e == \"InvalidNameException\"  ) {\n" +
                "        in_first_catch();\n" +
                "    } finally {\n" +
                "        in_finally();\n" +
                "    }\n" +
                "}", null
                );
    }

    public void testTryCatch2() throws Exception {
        format(
                "if (true) {\n" +
                "    try {\n" +
                "    } finally {\n" +
                "}\n" +
                "}",
                "if (true) {\n" +
                "    try {\n" +
                "    } finally {\n" +
                "    }\n" +
                "}", null
                );
    }
    
    public void testCall() throws Exception {
        format(
                "sortBy: function(iterator) {\n" +
                "return this.map(function(value, index) {\n" +
                "  return {value: value, criteria: iterator(value, index)};\n" +
                "}).sort(function() {\n" +
                "  return foo();\n" +
                "}).pluck('value');\n" +
                "}\n",
                "sortBy: function(iterator) {\n" +
                "    return this.map(function(value, index) {\n" +
                "        return {\n" +
                "            value: value, \n" +
                "            criteria: iterator(value, index)\n" +
                "            };\n" +
                "    }).sort(function() {\n" +
                "        return foo();\n" +
                "    }).pluck('value');\n" +
                "}\n", null
                );
    }

    public void testCompressed() throws Exception {
        format(
                "if(true&&(/alpha/i).test()){}",
                "if(true&&(/alpha/i).test()){}", null
                );
    }

    public void test153819() throws Exception {
        insertNewline(
           "if (true) {\n" +
           "    for (var a in b)\n" +
           "            foo();^",
           "if (true) {\n" +
           "    for (var a in b)\n" +
           "            foo();\n" +
           "        ^", null
         );
    }

    
//    public void testLineContinuation4() throws Exception {
//        format("def foo\nfoo\nif true\nx\nend\nend", 
//               "def foo\n  foo\n  if true\n    x\n  end\nend", null);
//    }
//
//    public void testLineContinuation5() throws Exception {
//        format("def foo\nfoo\nif true\nx\nend\nend", 
//               "def foo\n    foo\n    if true\n        x\n    end\nend", new IndentPrefs(4,4));
//    }
//
//    // Trigger lexer bug!
//    //public void testLineContinuationBackslash() throws Exception {
//    //    format("x\\\n= 1", 
//    //           "x\\\n  = 1", new IndentPrefs(2,4));
//    //}
//    
//    public void testLineContinuationComma() throws Exception {
//        format("render foo,\nbar\nbaz",
//               "render foo,\n  bar\nbaz", null);
//    }
//    
//    public void testBackslashIndent() throws Exception {
//        insertNewline("puts foo\\^", "puts foo\\\n  ^", null);
//    }
//
//    public void testLineContinuationParens() throws Exception {
//        format("foo(1,2\n3,4)\nx",
//               "foo(1,2\n  3,4)\nx", null);
//    }
//
//    public void testLiterals() throws Exception {
//        format("def foo\n  x = %q-foo\nbar-",
//               "def foo\n  x = %q-foo\nbar-", null);
//    }
//
//    public void testLiterals2() throws Exception {
//        insertNewline("def foo\n=begin\nfoo^\n=end\nend",
//                "def foo\n=begin\nfoo\n^\n=end\nend", null);
//    }
//    
//    public void testLiterals3() throws Exception {
//        insertNewline("def foo\nx = '\nfoo^\n'\nend",
//                "def foo\nx = '\nfoo\n^\n'\nend", null);
//    }
//    
//    public void testLineContinuationAlias() throws Exception {
//        format("foo ==\ntrue",
//               "foo ==\n  true", null);
//        format("alias foo ==\ntrue",
//               "alias foo ==\ntrue", null);
//        format("def ==\ntrue",
//               "def ==\n  true", new IndentPrefs(2,4));
//    }
//
//    public void testBrackets() throws Exception {
//        format("x = [[5]\n]\ny",
//               "x = [[5]\n]\ny", null);
//    }
//
//    public void testBrackets2() throws Exception {
//        format("x = [\n[5]\n]\ny",
//               "x = [\n  [5]\n]\ny", null);
//        format("x = [\n[5]\n]\ny",
//               "x = [\n  [5]\n]\ny", new IndentPrefs(2,4));
//    }
//
//    public void testHeredoc1() throws Exception {
//        format("def foo\n  s = <<EOS\n  stuff\nEOS\nend",
//               "def foo\n  s = <<EOS\n  stuff\nEOS\nend", null);
//    }
//
//    public void testHeredoc2() throws Exception {
//        format("def foo\n  s = <<-EOS\n  stuff\nEOS\nend",
//               "def foo\n  s = <<-EOS\n  stuff\n  EOS\nend", null);
//    }
//    
//    public void testHeredoc3() throws Exception {
//        format("def foo\n    s = <<EOS\nstuff\n  foo\nbar\nEOS\n  end",
//               "def foo\n  s = <<EOS\nstuff\n  foo\nbar\nEOS\nend", null);
//    }
//
//    public void testHeredoc4() throws Exception {
//        format("def foo\n    s = <<-EOS\nstuff\n  foo\nbar\nEOS\n  end",
//               "def foo\n  s = <<-EOS\nstuff\n  foo\nbar\n  EOS\nend", null);
//    }
//
//    public void testArrayDecl() throws Exception {
//        format("@foo = [\n'bar',\n'bar2',\n'bar3'\n]",
//                "@foo = [\n  'bar',\n  'bar2',\n  'bar3'\n]", null);
//    }
//
//    public void testHashDecl() throws Exception {
//        String unformatted = "@foo = {\n" +
//            "'bar' => :foo,\n" +
//            "'bar2' => :bar,\n" +
//            "'bar3' => :baz\n" +
//            "}";
//        String formatted = "@foo = {\n" +
//            "  'bar' => :foo,\n" +
//            "  'bar2' => :bar,\n" +
//            "  'bar3' => :baz\n" +
//            "}";
//        format(unformatted, formatted, null);
//    }
//
//    public void testParenCommaList() throws Exception {
//        String unformatted = "foo(\nx,\ny,\nz\n)";
//        String formatted = "foo(\n" +
//            "  x,\n" +
//            "  y,\n" +
//            "  z\n" +
//            ")";
//        format(unformatted, formatted, null);
//    }
//    
//    public void testDocumentRange1() throws Exception {
//        format("      def foo\n%<%foo%>%\n      end\n", 
//               "      def foo\n        foo\n      end\n", null);
//        format("def foo\nfoo\nend\n", 
//               "def foo\n  foo\nend\n", null);
//    }
//
//    public void testDocumentRange2() throws Exception {
//        format("def foo\n     if true\n           %<%xxx%>%\n     end\nend\n",
//                "def foo\n     if true\n       xxx\n     end\nend\n", null);
//    }    
//
//    public void testDocumentRange3() throws Exception {
//        format("class Foo\n  def bar\n  end\n\n\n%<%def test\nhello\nend%>%\nend\n",
//               "class Foo\n  def bar\n  end\n\n\n  def test\n    hello\n  end\nend\n", null);
//    }    
//    
//    public void testPercentWIndent110983a() throws Exception {
//        insertNewline(
//            "class Apple\n  def foo\n    snark %w[a b c]^\n    blah",
//            "class Apple\n  def foo\n    snark %w[a b c]\n    ^\n    blah", null);
//    }
//
//    public void testPercentWIndent110983b() throws Exception {
//        insertNewline(
//            "class Apple\n  def foo\n    snark %w,a b c,^\n    blah",
//            "class Apple\n  def foo\n    snark %w,a b c,\n    ^\n    blah", null);
//    }
//
//    public void testPercentWIndent110983c() throws Exception {
//        insertNewline(
//            "class Apple\n  def foo\n    snark %w/a/^\n    blah",
//            "class Apple\n  def foo\n    snark %w/a/\n    ^\n    blah", null);
//    }
//    
//    public void testPercentWIndent110983d() throws Exception {
//        insertNewline(
//            "class Apple\n  def foo\n    snark %W[a b c]^\n    blah",
//            "class Apple\n  def foo\n    snark %W[a b c]\n    ^\n    blah", null);
//    }
//
//    public void testPercentWIndent110983e() throws Exception {
//        insertNewline(
//            "class Apple\n  def foo\n    snark %Q[a b c]^\n    blah",
//            "class Apple\n  def foo\n    snark %Q[a b c]\n    ^\n    blah", null);
//    }
//
//    public void testEof() throws Exception {
//        format("def foo\n     if true\n           %<%xxx%>%\n     end\nend\n",
//                "def foo\n     if true\n       xxx\n     end\nend\n", null);
//        format("x\n",
//               "x\n", null);
//    }
}
