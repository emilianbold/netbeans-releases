/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.domdiff;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.lib.api.HtmlParseResult;
import org.netbeans.modules.html.editor.lib.api.HtmlParser;
import org.netbeans.modules.html.editor.lib.api.HtmlParserFactory;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.ParseException;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.AttributeFilter;
import org.netbeans.modules.html.editor.lib.api.elements.CloseTag;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementFilter;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class DiffTest extends NbTestCase {
    
    public DiffTest() {
        super(DiffTest.class.getName());
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testCase003() throws ParseException {
        HtmlSource s1 = getHtmlSource("diff/test003-v1.html");
        HtmlSource s2 = getHtmlSource("diff/test003-v2.html");
        OpenTag n1 = parse(s1);
        OpenTag n2 = parse(s2);
        Diff d = new Diff(s1, s2, n1, n2);
        List<Change> l = d.compare();
        assertEquals(6, l.size());
        assertRemoveChange(34, "<table>", s1, l.get(0));
        assertRemoveChange(146, "<div>", s1, l.get(5));
//        Revision r = new Revision(new StringBuilder(s2.getSourceCode()), new StringBuilder(""), l, new StringBuilder(""), String.valueOf(System.currentTimeMillis()), 0);
//        String res =
//            "<html>\n" +
//            "        <head></head><body><table>\n" +
//            "        <div></div>\n" +
//            "        <div></div>\n" +
//            "        <p></p>\n" +
//            "        <div></div>\n" +
//            "        <div></div>\n" +
//            "        <a></a><div>\n" +
//            "</body></html>\n";
//        assertEquals(res, r.getContent());
    }
    
    @Test
    public void testCompareAttributes() throws ParseException {
        HtmlSource s1 = getHtmlSource("diff/test004-v1.html");
        HtmlSource s2 = getHtmlSource("diff/test004-v2.html");
        OpenTag n1 = parse(s1);
        OpenTag n2 = parse(s2);
        Diff d = new Diff(s1, s2, n1, n2);
        List<Change> l = d.compare();
        assertEquals(4, l.size());
        assertRemoveChange(6, "c=\"3\"", s1, l.get(0));
        assertRemoveChange(9, "1", s1, l.get(1));
        assertAddChange(9, 1, "5", s2, l.get(2));
        assertAddChange(18, 5, "d=\"4\"", s2, l.get(3));
//        Revision r = new Revision(new StringBuilder(s2.getSourceCode()), new StringBuilder(""), l, new StringBuilder(""), String.valueOf(System.currentTimeMillis()), 0);
//        String res =
//            "<html c=\"3\"a=\"15\" b=\"2\" d=\"4\">\n" +
//            "</html>\n";
//        assertEquals(res, r.getContent());
    }
    
    @Test
    public void testRealFiles1() throws ParseException {
        HtmlSource s1 = getHtmlSource("diff/test001-v1.html");
        HtmlSource s2 = getHtmlSource("diff/test001-v2.html");
        OpenTag n1 = parse(s1);
        OpenTag n2 = parse(s2);
        Diff d = new Diff(s1, s2, n1, n2);
        List<Change> l = d.compare();
        assertEquals(5, l.size());
        assertAddChange(192, 8, "id=\"new\"", s2, l.get(0));
        assertAddChange(201, 14, "data-url=\"new\"", s2, l.get(1));
        assertAddChange(274, 33, "<span>Add New Manufacturer</span>", s2, l.get(2));
        assertAddChange(369, 25, "id=\"new-form-placeholder\"", s2, l.get(3));
        assertAddChange(415, 3, "<p>", s2, l.get(4));
    }
    
    @Test
    public void testRealFiles1b() throws ParseException {
        HtmlSource s1 = getHtmlSource("diff/test001-v2.html");
        HtmlSource s2 = getHtmlSource("diff/test001-v1.html");
        OpenTag n1 = parse(s1);
        OpenTag n2 = parse(s2);
        Diff d = new Diff(s1, s2, n1, n2);
        List<Change> l = d.compare();
        assertEquals(5, l.size());
        assertRemoveChange(175, "id=\"new\" ", s1, l.get(0));
        assertRemoveChange(175, "data-url=\"new\"", s1, l.get(1));
        assertRemoveChange(250, "<span>", s1, l.get(2));
        assertRemoveChange(312, "id=\"new-form-placeholder\"", s1, l.get(3));
        assertRemoveChange(332, "<p>", s1, l.get(4));
//        Revision r = new Revision(new StringBuilder(s2.getSourceCode()), new StringBuilder(""), l, new StringBuilder(""), String.valueOf(System.currentTimeMillis()), 0);
//        String res = 
//                "<!DOCTYPE html>\n" +
//                "<html>\n" +
//                "    <head>\n" +
//                "        <title>Hello</title>\n" +
//                "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
//                "    </head>\n" +
//                "    <body>\n" +
//                "        <div id=\"new\" data-url=\"new\"data-role=\"page\">\n" +
//                "            <div data-role=\"header\">\n" +
//                "                <h1><span>Add New Manufacturer</h1>\n" +
//                "            </div>\n" +
//                "            <div id=\"new-form-placeholder\"data-role=\"content\"><p>	\n" +
//                "            \n" +
//                "                    <table id=\"aa\">\n" +
//                "                    </table>\n" +
//                "                    \n" +
//                "            </div>\n" +
//                "        </div>\n" +
//                "    </body>\n" +
//                "</html>\n";
//        assertEquals(res, r.getContent());
    }
    
    @Test
    public void testRealFiles2() throws ParseException {
        HtmlSource s1 = getHtmlSource("diff/test002-v1.html");
        HtmlSource s2 = getHtmlSource("diff/test002-v2.html");
        OpenTag n1 = parse(s1);
        OpenTag n2 = parse(s2);
        Diff d = new Diff(s1, s2, n1, n2);
        List<Change> l = d.compare();
        assertEquals(1, l.size());
        assertRemoveChangeWithoutText(17, "<div data-role=\"page\"><div data-role=\"header\">", s1, l.get(0));
//        assertEquals(17, l.get(0).getOffset());
//        assertEquals("<div data-role=\"page\"><div data-role=\"header\">", l.get(0).getRemovedText());
        // test that removed text was added properly:
//        Revision r = new Revision(new StringBuilder(s2.getSourceCode()), new StringBuilder(""), l, new StringBuilder(""), String.valueOf(System.currentTimeMillis()), 0);
//        String val = 
//                "<html>\n" +
//                "    <body><div data-role=\"page\"><div data-role=\"header\">\n" +
//                "                <h1 id=\"aaa\">Add New Manufacturer</h1>\n" +
//                "    </body>\n" +
//                "</html>\n";
//        assertEquals(val, r.getContent());
    }
    
    @Test
    public void testRealFiles2b() throws ParseException {
        HtmlSource s1 = getHtmlSource("diff/test002-v2.html");
        HtmlSource s2 = getHtmlSource("diff/test002-v1.html");
        OpenTag n1 = parse(s1);
        OpenTag n2 = parse(s2);
        Diff d = new Diff(s1, s2, n1, n2);
        List<Change> l = d.compare();
        assertEquals(2, l.size());
        assertAddChange(26, 76, "<div data-role=\"page\">\n" +
"            <div data-role=\"header\">\n" +
"                ", s2, l.get(0));
        assertAddChange(140, 34, "\n"+ 
"            </div>\n" +
"        </div>", s2, l.get(1));
    }
    
    @Test
    public void testCase005() throws ParseException {
        HtmlSource s1 = getHtmlSource("diff/test005-v1.html");
        HtmlSource s2 = getHtmlSource("diff/test005-v2.html");
        OpenTag n1 = parse(s1);
        OpenTag n2 = parse(s2);
        Diff d = new Diff(s1, s2, n1, n2);
        List<Change> l = d.compare();
        assertEquals(3, l.size());
        assertRemoveChange(255, "<p>", s1, l.get(0));
        assertRemoveChange(335, "<span>", s1, l.get(1));
        assertRemoveChange(394, "<table id=\"aa\">", s1, l.get(2));
//        Revision r = new Revision(new StringBuilder(s2.getSourceCode()), new StringBuilder(""), l, new StringBuilder(""), String.valueOf(System.currentTimeMillis()), 0);
    }
    
    public static void assertAddChange(int offset, int len, String text, HtmlSource s, Change ch) {
        assertTrue(ch.isAdd());
        assertEquals(offset, ch.getOffset());
        assertEquals(len, ch.getLengthOfNewText());
        assertEquals(text, s.getSourceCode().subSequence(ch.getOffset(), ch.getEndOffsetOfNewText()));
    }
    
    public static void assertRemoveChange(int offset, String text, HtmlSource s, Change ch) {
        assertFalse(ch.isAdd());
        assertEquals(offset, ch.getOffset());
        assertEquals(text, ch.getRemovedText());
        assertEquals(text, s.getSourceCode().subSequence(ch.getOriginalOffset(), ch.getOriginalOffset()+ch.getRemovedText().length()));
    }
    
    public static void assertRemoveChangeWithoutText(int offset, String text, HtmlSource s, Change ch) {
        assertFalse(ch.isAdd());
        assertEquals(offset, ch.getOffset());
        assertEquals(text, ch.getRemovedText());
        //assertEquals(text, s.getSourceCode().subSequence(ch.getOriginalOffset(), ch.getOriginalOffset()+ch.getRemovedText().length()));
    }
    
    private static FileObject getTestFile(File dataDir, String relFilePath) {
        File wholeInputFile = new File(dataDir, relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }
    
    private HtmlSource getHtmlSource(String relFilePath) {
        FileObject file = getTestFile(getDataDir(), relFilePath);
        return new HtmlSource(file);
    }
    
    public static HtmlSource getHtmlSource(File dataDir, String relFilePath) {
        FileObject file = getTestFile(dataDir, relFilePath);
        return new HtmlSource(file);
    }
    
    public static Lookup getParserLookup() {
        Properties p = new Properties();
        p.setProperty("add_text_nodes", "true");
        return Lookups.fixed(p);
    }
    
    private OpenTag parse(HtmlSource source) throws ParseException {
        HtmlParser parser = HtmlParserFactory.findParser(HtmlVersion.getDefaultVersion());
        HtmlParseResult result = parser.parse(source, HtmlVersion.getDefaultVersion(), getParserLookup());
//        HtmlParseResult result = SyntaxAnalyzer.create(source).analyze().parseHtml();
//        ElementUtils.dumpTree(result.root());
        return (OpenTag)result.root().children().iterator().next();
//        Iterator<Element> iterator = new ElementsIterator(source);
//        Node n = XmlSyntaxTreeBuilder.makeUncheckedTree(source, null, iterator);
//        //ElementUtils.dumpTree(n);
//        return (OpenTag)n.children().iterator().next();
    }
    
    private static class OpenTagImpl implements OpenTag {

        private String name;
        private String image;
        private List<Attribute> attrs;

        public OpenTagImpl(String name, String image) {
            this(name, image, new Attribute[0]);
        }
        
        public OpenTagImpl(String name, String image, Attribute[] attrs) {
            this.name = name;
            this.image = image;
            this.attrs = Arrays.asList(attrs);
        }
        
        @Override
        public Collection<Attribute> attributes() {
            return attrs;
        }

        @Override
        public Collection<Attribute> attributes(AttributeFilter filter) {
            return attrs;
        }

        @Override
        public Attribute getAttribute(String name) {
            for (Attribute a : attrs) {
                if (name.equals(a.name())) {
                    return a;
                }
            }
            return null;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public CloseTag matchingCloseTag() {
            return null;
        }

        @Override
        public int semanticEnd() {
            return 0;
        }

        @Override
        public CharSequence name() {
            return name;
        }

        @Override
        public CharSequence namespacePrefix() {
            return null;
        }

        @Override
        public CharSequence unqualifiedName() {
            return null;
        }

        @Override
        public int from() {
            return 0;
        }

        @Override
        public int to() {
            return 0;
        }

        @Override
        public ElementType type() {
            return null;
        }

        @Override
        public CharSequence image() {
            return image;
        }

        @Override
        public CharSequence id() {
            return null;
        }

        @Override
        public Collection<ProblemDescription> problems() {
            return null;
        }

        @Override
        public Node parent() {
            return null;
        }

        @Override
        public Collection<Element> children() {
            return Collections.emptyList();
        }

        @Override
        public Collection<Element> children(ElementType type) {
            return null;
        }

        @Override
        public Collection<Element> children(ElementFilter filter) {
            return null;
        }

        @Override
        public <T extends Element> Collection<T> children(Class<T> type) {
            return null;
        }

        @Override
        public String toString() {
            return "<"+name+">";
        }
        
        private static class AttributeImpl implements Attribute {
            
            private String name;
            private String value;

            public AttributeImpl(String name, String value) {
                this.name = name;
                this.value = value;
            }
            
            @Override
            public int nameOffset() {
                return 0;
            }

            @Override
            public int valueOffset() {
                return 0;
            }

            @Override
            public CharSequence value() {
                return value;
            }

            @Override
            public boolean isValueQuoted() {
                return false;
            }

            @Override
            public CharSequence unquotedValue() {
                return value;
            }

            @Override
            public CharSequence name() {
                return name;
            }

            @Override
            public CharSequence namespacePrefix() {
                return "";
            }

            @Override
            public CharSequence unqualifiedName() {
                return name;
            }

            @Override
            public int from() {
                return 0;
            }

            @Override
            public int to() {
                return 0;
            }

            @Override
            public ElementType type() {
                return ElementType.ATTRIBUTE;
            }

            @Override
            public CharSequence image() {
                return name+"="+value;
            }

            @Override
            public CharSequence id() {
                return null;
            }

            @Override
            public Collection<ProblemDescription> problems() {
                return null;
            }

            @Override
            public Node parent() {
                return null;
            }
            
        }
        
    }
}
