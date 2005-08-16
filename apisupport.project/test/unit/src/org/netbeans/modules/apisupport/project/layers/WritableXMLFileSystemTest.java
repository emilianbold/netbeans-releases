/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.layers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.xml.core.XMLDataLoader;
import org.netbeans.modules.xml.core.XMLDataObjectLook;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Test functionality of {@link WritableXMLFileSystem}.
 * @author Jesse Glick
 */
public class WritableXMLFileSystemTest extends NbTestCase {
    
    // Copied from org.netbeans.api.project.TestUtil:
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        Assert.assertEquals(Lkp.class, Lookup.getDefault().getClass());
    }
    public static final class Lkp extends ProxyLookup {
        private static Lkp DEFAULT;
        public Lkp() {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            setLookup(new Object[0]);
        }
        public static void setLookup(Object[] instances) {
            ClassLoader l = Lkp.class.getClassLoader();
            DEFAULT.setLookups(new Lookup[] {
                Lookups.fixed(instances),
                        Lookups.metaInfServices(l),
                        Lookups.singleton(l),
            });
        }
    }
    
    public WritableXMLFileSystemTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        Lkp.setLookup(new Object[] {
            SharedClassObject.findObject(XMLDataLoader.class, true),
                    new TestCatalog(),
        });
    }
    
    public void testBasicStructureReads() throws Exception {
        FileSystem fs = new Layer("<file name='x'/>").read();
        FileObject[] top = fs.getRoot().getChildren();
        assertEquals(1, top.length);
        FileObject x = top[0];
        assertEquals("x", x.getNameExt());
        assertEquals(0L, x.getSize());
        assertTrue(x.isData());
        fs = new Layer("<folder name='x'><file name='y'/></folder><file name='z'/>").read();
        top = fs.getRoot().getChildren();
        assertEquals(2, top.length);
        FileObject z = fs.findResource("z");
        assertNotNull(z);
        assertTrue(z.isData());
        FileObject y = fs.findResource("x/y");
        assertNotNull(y);
        assertTrue(y.isData());
        x = fs.findResource("x");
        assertEquals(x, y.getParent());
        assertTrue(x.isFolder());
        assertEquals(1, x.getChildren().length);
    }
    
    public void testExternalFileReads() throws Exception {
        FileSystem fs = new Layer("<file name='x' url='x.txt'/>", Collections.singletonMap("x.txt", "stuff")).read();
        FileObject x = fs.findResource("x");
        assertNotNull(x);
        assertTrue(x.isData());
        assertEquals(5L, x.getSize());
        assertEquals("stuff", TestBase.slurp(x));
        fs = new Layer("<file name='x' url='subdir/x.txt'/>", Collections.singletonMap("subdir/x.txt", "more stuff")).read();
        x = fs.findResource("x");
        assertNotNull(x);
        assertEquals("more stuff", TestBase.slurp(x));
    }
    
    public void testSimpleAttributeReads() throws Exception {
        FileSystem fs = new Layer("<file name='x'><attr name='a' stringvalue='v'/> <attr name='b' urlvalue='http://nowhere.net/'/></file> " +
                "<folder name='y'> <file name='ignore'/><attr name='a' boolvalue='true'/><!--ignore--></folder>").read();
        FileObject x = fs.findResource("x");
        assertEquals(new HashSet(Arrays.asList(new String[] {"a", "b"})), new HashSet(Collections.list(x.getAttributes())));
        assertEquals("v", x.getAttribute("a"));
        assertEquals(new URL("http://nowhere.net/"), x.getAttribute("b"));
        assertEquals(null, x.getAttribute("dummy"));
        FileObject y = fs.findResource("y");
        assertEquals(Collections.singletonList("a"), Collections.list(y.getAttributes()));
        assertEquals(Boolean.TRUE, y.getAttribute("a"));
        fs = new Layer("<attr name='a' stringvalue='This \\u0007 is a beep!'/>").read();
        assertEquals("Unicode escapes handled correctly (for non-XML-encodable chars)", "This \u0007 is a beep!", fs.getRoot().getAttribute("a"));
        fs = new Layer("<attr name='a' stringvalue='\\ - \\u - \\u123 - \\uxyzw'/>").read();
        assertEquals("Malformed escapes also handled", "\\ - \\u - \\u123 - \\uxyzw", fs.getRoot().getAttribute("a"));
        fs = new Layer("<attr name='a' urlvalue='yadayada'/>").read();
        assertEquals("Malformed URLs also handled", null, fs.getRoot().getAttribute("a"));
        // XXX test other attr types: byte, short, int, long, float, double, char
    }
    
    public void testCodeAttributeReads() throws Exception {
        // XXX test method, new, serial
    }
    
    public void testCreateNewFolder() throws Exception {
        Layer l = new Layer("");
        FileSystem fs = l.read();
        FileObject x = fs.getRoot().createFolder("x");
        assertEquals("in-memory write worked", 1, fs.getRoot().getChildren().length);
        assertEquals("wrote correct content for top-level folder", "    <folder name=\"x\"/>\n", l.write());
        x.createFolder("y");
        assertEquals("wrote correct content for 2nd-level folder", "    <folder name=\"x\">\n        <folder name=\"y\"/>\n    </folder>\n", l.write());
        l = new Layer("    <folder name=\"original\"/>\n");
        fs = l.read();
        fs.getRoot().createFolder("aardvark");
        fs.getRoot().createFolder("xyzzy");
        assertEquals("alphabetized new folders", "    <folder name=\"aardvark\"/>\n    <folder name=\"original\"/>\n    <folder name=\"xyzzy\"/>\n", l.write());
    }
    
    public void testCreateNewEmptyFile() throws Exception {
        Layer l = new Layer("");
        FileSystem fs = l.read();
        FileUtil.createData(fs.getRoot(), "Menu/Tools/foo");
        FileUtil.createData(fs.getRoot(), "Menu/Tools/bar");
        FileUtil.createFolder(fs.getRoot(), "Menu/Tools/baz");
        assertEquals("correct files written",
                "    <folder name=\"Menu\">\n" +
                "        <folder name=\"Tools\">\n" +
                "            <file name=\"bar\"/>\n" +
                "            <folder name=\"baz\"/>\n" +
                "            <file name=\"foo\"/>\n" +
                "        </folder>\n" +
                "    </folder>\n",
                l.write());
        assertEquals("no external files created", Collections.EMPTY_MAP, l.files());
    }
    
    public void testCreateFileWithContents() throws Exception {
        Layer l = new Layer("");
        FileSystem fs = l.read();
        FileObject f = FileUtil.createData(fs.getRoot(), "Templates/Other/Foo.java");
        f.setAttribute("hello", "there");
        dump(f, "some stuff");
        String xml =
                "    <folder name=\"Templates\">\n" +
                "        <folder name=\"Other\">\n" +
                // We never want to create *.java files since they would be compiled.
                "            <file name=\"Foo.java\" url=\"Foo_java\">\n" +
                "                <attr name=\"hello\" stringvalue=\"there\"/>\n" +
                "            </file>\n" +
                "        </folder>\n" +
                "    </folder>\n";
        assertEquals("correct XML written", xml, l.write());
        Map m = new HashMap();
        m.put("Foo_java", "some stuff");
        assertEquals("one external file created in " + l, m, l.files());
        dump(f, "new stuff");
        assertEquals("same XML as before", xml, l.write());
        m.put("Foo_java", "new stuff");
        assertEquals("different external file", m, l.files());
        f = FileUtil.createData(fs.getRoot(), "Templates/Other2/Foo.java");
        dump(f, "unrelated stuff");
        f = FileUtil.createData(fs.getRoot(), "Templates/Other2/Bar.xml");
        dump(f, "unrelated XML stuff");
        f = FileUtil.createData(fs.getRoot(), "Services/foo.settings");
        dump(f, "scary stuff");
        xml =
                "    <folder name=\"Services\">\n" +
                // *.settings are also potentially dangerous in module sources, so rename them.
                "        <file name=\"foo.settings\" url=\"foo.xml\"/>\n" +
                "    </folder>\n" +
                "    <folder name=\"Templates\">\n" +
                "        <folder name=\"Other\">\n" +
                "            <file name=\"Foo.java\" url=\"Foo_java\">\n" +
                "                <attr name=\"hello\" stringvalue=\"there\"/>\n" +
                "            </file>\n" +
                "        </folder>\n" +
                "        <folder name=\"Other2\">\n" +
                // But *.xml files and other things are generally OK.
                "            <file name=\"Bar.xml\" url=\"Bar.xml\"/>\n" +
                "            <file name=\"Foo.java\" url=\"Foo_1_java\"/>\n" +
                "        </folder>\n" +
                "    </folder>\n";
        assertEquals("right XML written for remaining files", xml, l.write());
        m.put("Foo_1_java", "unrelated stuff");
        m.put("Bar.xml", "unrelated XML stuff");
        m.put("foo.xml", "scary stuff");
        assertEquals("right external files", m, l.files());
        l = new Layer("", Collections.singletonMap("file.txt", "existing stuff"));
        fs = l.read();
        f = FileUtil.createData(fs.getRoot(), "wherever/file.txt");
        dump(f, "unrelated stuff");
        xml =
                "    <folder name=\"wherever\">\n" +
                // Need to pick a different location even though there is no conflict inside the layer.
                // Also should use a suffix before file extension.
                "        <file name=\"file.txt\" url=\"file_1.txt\"/>\n" +
                "    </folder>\n";
        assertEquals("wrote new file contents to non-clobbering location", xml, l.write());
        m.clear();
        m.put("file.txt", "existing stuff");
        m.put("file_1.txt", "unrelated stuff");
        assertEquals("right external files", m, l.files());
        l = new Layer("");
        fs = l.read();
        f = FileUtil.createData(fs.getRoot(), "one/bare");
        dump(f, "bare #1");
        f = FileUtil.createData(fs.getRoot(), "two/bare");
        dump(f, "bare #2");
        f = FileUtil.createData(fs.getRoot(), "three/bare");
        dump(f, "bare #3");
        xml =
                "    <folder name=\"one\">\n" +
                "        <file name=\"bare\" url=\"bare\"/>\n" +
                "    </folder>\n" +
                // Note alpha ordering.
                "    <folder name=\"three\">\n" +
                // Count _1, _2, _3, ...
                "        <file name=\"bare\" url=\"bare_2\"/>\n" +
                "    </folder>\n" +
                "    <folder name=\"two\">\n" +
                // No extension here, fine.
                "        <file name=\"bare\" url=\"bare_1\"/>\n" +
                "    </folder>\n";
        assertEquals("right counter usage", xml, l.write());
        m.clear();
        m.put("bare", "bare #1");
        m.put("bare_1", "bare #2");
        m.put("bare_2", "bare #3");
        assertEquals("right external files", m, l.files());
    }
    // XXX testReplaceExistingInlineContent
    
    public void testAddAttributes() throws Exception {
        Layer l = new Layer("");
        FileSystem fs = l.read();
        FileObject f = fs.getRoot().createFolder("f");
        f.createData("b");
        FileObject a = f.createData("a");
        f.createData("c");
        a.setAttribute("x", "v1");
        a.setAttribute("y", new URL("http://nowhere.net/v2"));
        f.setAttribute("a/b", Boolean.TRUE);
        f.setAttribute("b/c", Boolean.TRUE);
        f.setAttribute("misc", "whatever");
        assertEquals("correct attrs written",
                "    <folder name=\"f\">\n" +
                "        <attr name=\"misc\" stringvalue=\"whatever\"/>\n" +
                "        <file name=\"a\">\n" +
                "            <attr name=\"x\" stringvalue=\"v1\"/>\n" +
                "            <attr name=\"y\" urlvalue=\"http://nowhere.net/v2\"/>\n" +
                "        </file>\n" +
                "        <attr name=\"a/b\" boolvalue=\"true\"/>\n" +
                "        <file name=\"b\"/>\n" +
                "        <attr name=\"b/c\" boolvalue=\"true\"/>\n" +
                "        <file name=\"c\"/>\n" +
                "    </folder>\n",
                l.write());
        // XXX test also string values w/ dangerous chars
    }
    
    public void testDeleteReplaceAttributes() throws Exception {
        Layer l = new Layer(
                "    <file name=\"x\">\n" +
                "        <attr name=\"foo\" stringvalue=\"bar\"/>\n" +
                "    </file>\n");
        FileSystem fs = l.read();
        FileObject x = fs.findResource("x");
        x.setAttribute("foo", Boolean.TRUE);
        assertEquals("replaced attr",
                "    <file name=\"x\">\n" +
                "        <attr name=\"foo\" boolvalue=\"true\"/>\n" +
                "    </file>\n",
                l.write());
        x.setAttribute("foo", null);
        assertEquals("deleted attr",
                "    <file name=\"x\"/>\n",
                l.write());
        x.setAttribute("foo", null);
        assertEquals("cannot delete attr twice",
                "    <file name=\"x\"/>\n",
                l.write());
        fs.getRoot().createData("y");
        fs.getRoot().setAttribute("x/y", Boolean.TRUE);
        fs.getRoot().setAttribute("x/y", null);
        assertEquals("deleted ordering attr",
                "    <file name=\"x\"/>\n" +
                "    <file name=\"y\"/>\n",
                l.write());
    }
    
    public void testCodeAttributeWrites() throws Exception {
        Layer l = new Layer("");
        FileSystem fs = l.read();
        FileObject x = fs.getRoot().createData("x");
        x.setAttribute("nv", "newvalue:org.foo.Clazz");
        x.setAttribute("mv", "methodvalue:org.foo.Clazz.create");
        assertEquals("special attrs written to XML",
                "    <file name=\"x\">\n" +
                "        <attr name=\"mv\" methodvalue=\"org.foo.Clazz.create\"/>\n" +
                "        <attr name=\"nv\" newvalue=\"org.foo.Clazz\"/>\n" +
                "    </file>\n",
                l.write());
    }
    
    public void testOpenideFolderOrder() throws Exception {
        Layer l = new Layer("");
        FileSystem fs = l.read();
        FileObject r = fs.getRoot();
        FileObject a = r.createData("a");
        FileObject b = r.createData("b");
        FileObject f = r.createFolder("f");
        FileObject c = f.createData("c");
        FileObject d = f.createData("d");
        FileObject e = f.createData("e");
        DataFolder.findFolder(r).setOrder(new DataObject[] {DataObject.find(a), DataObject.find(b)});
        DataFolder.findFolder(f).setOrder(new DataObject[] {DataObject.find(c), DataObject.find(d), DataObject.find(e)});
        assertEquals("correct ordering XML",
                "    <file name=\"a\"/>\n" +
                "    <attr name=\"a/b\" boolvalue=\"true\"/>\n" +
                "    <file name=\"b\"/>\n" +
                "    <folder name=\"f\">\n" +
                "        <file name=\"c\"/>\n" +
                "        <attr name=\"c/d\" boolvalue=\"true\"/>\n" +
                "        <file name=\"d\"/>\n" +
                "        <attr name=\"d/e\" boolvalue=\"true\"/>\n" +
                "        <file name=\"e\"/>\n" +
                "    </folder>\n",
                l.write());
        l = new Layer("");
        fs = l.read();
        r = fs.getRoot();
        b = r.createData("b");
        c = r.createFolder("c");
        r.setAttribute("c/b", Boolean.TRUE);
        r.setAttribute("d/c", Boolean.TRUE);
        r.setAttribute("b/a", Boolean.TRUE);
        assertEquals("ordering attrs reorder entries in a folder",
                "    <attr name=\"d/c\" boolvalue=\"true\"/>\n" +
                "    <folder name=\"c\"/>\n" +
                "    <attr name=\"c/b\" boolvalue=\"true\"/>\n" +
                "    <file name=\"b\"/>\n" +
                "    <attr name=\"b/a\" boolvalue=\"true\"/>\n",
                l.write());
        l = new Layer("");
        fs = l.read();
        r = fs.getRoot();
        FileObject main = r.createData("main");
        FileObject before = r.createData("s-before");
        r.setAttribute("pre/s-before", Boolean.TRUE);
        r.setAttribute("s-before/main", Boolean.TRUE);
        assertEquals(
                "    <attr name=\"pre/s-before\" boolvalue=\"true\"/>\n" +
                "    <file name=\"s-before\"/>\n" +
                "    <attr name=\"s-before/main\" boolvalue=\"true\"/>\n" +
                "    <file name=\"main\"/>\n",
                l.write());
        // XXX probably need even more sophisticated tests to really cover WXMLFS.resort!
    }
    
    public void testDeleteFileOrFolder() throws Exception {
        Layer l = new Layer("");
        FileSystem fs = l.read();
        FileObject f = fs.getRoot().createFolder("f");
        FileObject x = f.createData("x");
        x.setAttribute("foo", "bar");
        dump(x, "stuff");
        FileObject y = FileUtil.createData(fs.getRoot(), "y");
        x.delete();
        assertEquals("one file and one folder left",
                "    <folder name=\"f\"/>\n" +
                "    <file name=\"y\"/>\n",
                l.write());
        assertEquals("no external files left", Collections.EMPTY_MAP, l.files());
        f.delete();
        assertEquals("one file left",
                "    <file name=\"y\"/>\n",
                l.write());
        y.delete();
        assertEquals("layer now empty again", "", l.write());
        l = new Layer("");
        fs = l.read();
        f = fs.getRoot().createFolder("f");
        x = f.createData("x");
        dump(x, "stuff");
        f.delete();
        assertEquals("layer empty again", "", l.write());
        assertEquals("no external files left even after only implicitly deleting file", Collections.EMPTY_MAP, l.files());
        // XXX should any associated ordering attrs also be deleted? not acc. to spec, but often handy...
        l = new Layer("");
        fs = l.read();
        FileObject a = fs.getRoot().createData("a");
        FileObject b = fs.getRoot().createData("b");
        FileObject c = fs.getRoot().createData("c");
        FileObject d = fs.getRoot().createData("d");
        a.delete();
        assertEquals("right indentation cleanup for deletion of first child",
                "    <file name=\"b\"/>\n" +
                "    <file name=\"c\"/>\n" +
                "    <file name=\"d\"/>\n",
                l.write());
        c.delete();
        assertEquals("right indentation cleanup for deletion of interior child",
                "    <file name=\"b\"/>\n" +
                "    <file name=\"d\"/>\n",
                l.write());
        d.delete();
        assertEquals("right indentation cleanup for deletion of last child",
                "    <file name=\"b\"/>\n",
                l.write());
    }
    
    public void testSomeFormattingPreserved() throws Exception {
        String orig = "    <file name=\"orig\"><!-- hi --><attr name=\"a\" boolvalue=\"true\"/>" +
                "<attr boolvalue=\"true\" name=\"b\"/></file>\n";
        Layer l = new Layer(orig);
        FileSystem fs = l.read();
        fs.getRoot().createData("aardvark");
        fs.getRoot().createData("zyzzyva");
        assertEquals("kept original XML intact",
                "    <file name=\"aardvark\"/>\n" +
                orig +
                "    <file name=\"zyzzyva\"/>\n",
                l.write());
        // XXX what doesn't work: space before />, unusual whitespace between attrs, ...
        // This is a job for TAX to improve on. Currently uses Xerces XNI parser,
        // which discards some formatting info when constructing its model.
        // Not as much as using DOM + serialization, but some.
    }
    
    public void testStructuralModificationsFired() throws Exception {
        Layer l = new Layer("<folder name='f'/><file name='x'/>");
        FileSystem fs = l.read();
        Listener fcl = new Listener();
        fs.addFileChangeListener(fcl);
        FileUtil.createData(fs.getRoot(), "a");
        FileUtil.createData(fs.getRoot(), "f/b");
        fs.findResource("x").delete();
        assertEquals("expected things fired",
                new HashSet(Arrays.asList(new String[] {"a", "f/b", "x"})),
                        fcl.changes());
    }

    /* Gets random failures; not sure why:
    public void testTextualModificationsFired() throws Exception {
        Layer l = new Layer("<folder name='f'><file name='x'/></folder><file name='y'/>");
        FileSystem fs = l.read();
        Listener fcl = new Listener();
        fs.addFileChangeListener(fcl);
        l.edit("<folder name='f'/><file name='y'/><file name='z'/>");
        / * XXX does not work; fires too much... why?
        assertEquals("expected things fired",
                new HashSet(Arrays.asList(new String[] {"f/x", "z"})),
                        fcl.changes());
         * /
        assertTrue("something fired", !fcl.changes().isEmpty());
        assertNull(fs.findResource("f/x"));
        assertNotNull(fs.findResource("z"));
    }
    */
    
    public void testExternalFileChangesRefired() throws Exception {
        Layer l = new Layer("");
        FileSystem fs = l.read();
        FileObject foo = fs.getRoot().createData("foo");
        dump(foo, "foo text");
        long start = foo.lastModified().getTime();
        assertEquals(start, l.externalLastModified("foo"));
        Thread.sleep(1000L); // make sure the timestamp actually changed
        Listener fcl = new Listener();
        fs.addFileChangeListener(fcl);
        l.editExternal("foo", "new text");
        long end = foo.lastModified().getTime();
        assertEquals(end, l.externalLastModified("foo"));
        assertTrue("foo was touched", end > start);
        assertEquals("expected things fired",
                Collections.singleton("foo"),
                        fcl.changes());
    }
    
    private static String slurp(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(is, baos);
            return baos.toString("UTF-8");
        } finally {
            is.close();
        }
    }
    private static void dump(FileObject f, String contents) throws IOException {
        FileLock lock = f.lock();
        try {
            OutputStream os = f.getOutputStream(lock);
            try {
                Writer w = new OutputStreamWriter(os, "UTF-8");
                w.write(contents);
                w.flush();
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    private static void dump(File f, String contents) throws IOException {
        OutputStream os = new FileOutputStream(f);
        try {
            Writer w = new OutputStreamWriter(os, "UTF-8");
            w.write(contents);
            w.flush();
        } finally {
            os.close();
        }
    }
    
    /**
     * Handle for working with an XML layer.
     */
    private final class Layer {
        private final File folder;
        private final DataObject d;
        private final XMLDataObjectLook look;
        private TreeEditorCookieImpl cookie;
        /**
         * Create a layer from a fixed bit of filesystem-DTD XML.
         * Omit the <filesystem>...</> tag and just give the contents.
         * But if you want to check formatting later using {@link #write()},
         * you should use 4-space indents and a newline after every element.
         */
        public Layer(String xml) throws Exception {
            folder = makeFolder();
            d = makeLayer(xml);
            look = (XMLDataObjectLook) d;
        }
        /**
         * Create a layer from XML plus external file contents.
         * The map's keys are relative disk filenames, and values are contents.
         */
        public Layer(String xml, Map/*<String,String>*/ files) throws Exception {
            this(xml);
            Iterator it = files.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String fname = (String) entry.getKey();
                String contents = (String) entry.getValue();
                File f = new File(folder, fname.replace('/', File.separatorChar));
                f.getParentFile().mkdirs();
                dump(f, contents);
            }
        }
        private File makeFolder() throws Exception {
            File f = File.createTempFile("layerdata", "", getWorkDir());
            f.delete();
            f.mkdir();
            return f;
        }
        private final String HEADER =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n" +
                "<filesystem>\n";
        private final String FOOTER = "</filesystem>\n";
        private DataObject makeLayer(String xml) throws Exception {
            File f = new File(folder, "layer.xml");
            dump(f, HEADER + xml + FOOTER);
            return DataObject.find(FileUtil.toFileObject(f));
        }
        /**
         * Read the filesystem from the layer.
         */
        public WritableXMLFileSystem read() throws Exception {
            // Does not have TEC as a cookie unless we have loaded layers, sigh...
            cookie = new TreeEditorCookieImpl(look);
            return new WritableXMLFileSystem(d.getPrimaryFile().getURL(), cookie, false);
        }
        /**
         * Write the filesystem to the layer and retrieve the new contents.
         * The header and footer are removed for you, but not other whitespace.
         */
        public String write() throws Exception {
            if (d.isModified()) {
                SaveCookie save = (SaveCookie) d.getCookie(SaveCookie.class);
                assertNotNull("have a SaveCookie on modified " + d, save);
                save.save();
            }
            String raw = TestBase.slurp(d.getPrimaryFile());
            assertTrue("unexpected header in '" + raw + "'", raw.startsWith(HEADER));
            assertTrue("unexpected footer in '" + raw + "'", raw.endsWith(FOOTER));
            return raw.substring(HEADER.length(), raw.length() - FOOTER.length());
        }
        /**
         * Edit the text of the layer.
         */
        public void edit(String newText) throws Exception {
            /* Does not work:
            EditorCookie cookie = (EditorCookie) d.getCookie(EditorCookie.class);
            assertNotNull(d.toString(), cookie);
            Document doc = cookie.openDocument();
            doc.remove(0, doc.getLength());
            doc.insertString(0, newText, null);
             */
            /* Also does not work:
            dump(d.getPrimaryFile(), newText);
            cookie.updateTree(new InputSource(d.getPrimaryFile().getURL().toExternalForm()));
             */
            cookie.updateTree(new InputSource(new StringReader(HEADER + newText + FOOTER)));
        }
        /**
         * Edit a referenced external file.
         */
        public void editExternal(String path, String newText) throws Exception {
            assert !path.equals("layer.xml");
            File f = new File(folder, path.replace('/', File.separatorChar));
            assert f.isFile();
            dump(FileUtil.toFileObject(f), newText);
        }
        public long externalLastModified(String path) {
            File f = new File(folder, path.replace('/', File.separatorChar));
            return FileUtil.toFileObject(f).lastModified().getTime();
        }
        /**
         * Get extra files besides layer.xml which were written to.
         * Keys are relative file paths and values are file contents.
         */
        public Map/*<String,String>*/ files() throws IOException {
            Map m = new HashMap();
            traverse(m, folder, "");
            return m;
        }
        private void traverse(Map m, File folder, String prefix) throws IOException {
            String[] kids = folder.list();
            if (kids == null) {
                throw new IOException(folder.toString());
            }
            for (int i = 0; i < kids.length; i++) {
                String path = prefix + kids[i];
                if (path.equals("layer.xml")) {
                    continue;
                }
                File f = new File(folder, kids[i]);
                if (f.isDirectory()) {
                    traverse(m, f, prefix + kids[i] + '/');
                } else {
                    m.put(path, slurp(f));
                }
            }
        }
        public String toString() {
            return "Layer[" + folder + "]";
        }
    }
    
    /**
     * In the actual IDE, the default NetBeans Catalog will already be "mounted", so just for testing:
     */
    private static final class TestCatalog extends UserCatalog implements EntityResolver {
        
        public TestCatalog() {}
        
        public EntityResolver getEntityResolver() {
            return this;
        }
        
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if ("-//NetBeans//DTD Filesystem 1.1//EN".equals(publicId)) {
                return new InputSource("nbres:/org/openide/filesystems/filesystem1_1.dtd");
            } else {
                return null;
            }
        }
        
    }
    
    private static final class Listener implements FileChangeListener {
        
        private Set/*<String>*/ changes = new HashSet();
        
        public Listener() {}
        
        public Set/*<String>*/ changes() {
            Set _changes = changes;
            changes = new HashSet();
            return _changes;
        }
        
        public void fileFolderCreated(FileEvent fe) {
            changed(fe);
        }
        public void fileDataCreated(FileEvent fe) {
            changed(fe);
        }
        public void fileChanged(FileEvent fe) {
            changed(fe);
        }
        public void fileDeleted(FileEvent fe) {
            changed(fe);
        }
        public void fileRenamed(FileRenameEvent fe) {
            changed(fe);
        }
        public void fileAttributeChanged(FileAttributeEvent fe) {
            changed(fe);
        }
        
        private void changed(FileEvent fe) {
            changes.add(fe.getFile().getPath());
        }
        
    }
    
}
