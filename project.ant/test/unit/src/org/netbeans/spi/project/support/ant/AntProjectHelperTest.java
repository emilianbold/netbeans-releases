/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.project.ant.Util;
import org.netbeans.spi.project.ExtensibleMetadataProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/* XXX tests needed:
 * - testProjectXmlSavedException
 * try throwing an exception from PXSH.pXS and check that next save is OK
 * need to delete: assert !modifiedMetadataPaths.isEmpty();
 * - testMalformedConfigDataProducesLoadException
 * make a project with broken XML files and check that it does not load
 * - testCleanCheckout
 * make sure loading proceeds naturally with a clean checkout, i.e. no nbproject/private/ dir
 * and that appropriate private.xml and private.properties files are created on demand
 * (this is perhaps already tested adequately by ProjectGeneratorTest)
 * - testActionProviderImpl
 * How to run Ant from a unit test?
 * Can perhaps use InstalledFileLocator to define "ant" to be ${ant.home}.
 * Then set up a build script and check that outputs are correct?
 * Also need to install an IOProvider and ExecutionEngine, probably.
 * Tricky!
 * - testVCSFriendliness
 * make various modifications to project metadata files and ensure that the
 * number of lines changed in the diff is kept to a minimum; specifically, need
 * to check that project.xml and private.xml are written in canonical order
 * (probably need schema); separate tests needed for EditableProperties
 * - testDiskChanges
 * check that various modifications on disk (at FS level, i.e. refreshed)
 * do load to firing "unexpected" changes in XML and properties
 * and that the APH loads the new data
 * (also that something reasonable happens if it is locking the data)
 * - testIsProject
 */

/**
 * Test functionality of AntProjectHelper and AntBasedProjectFactorySingleton.
 * @author Jesse Glick
 */
public class AntProjectHelperTest extends NbTestCase {

    /**
     * Create test suite.
     * @param name suite name
     */
    public AntProjectHelperTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private ProjectManager pm;
    private Project p;
    private AntProjectHelper h;
    private AntBasedTestUtil.TestListener l;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        Repository.getDefault().addFileSystem(scratch.getFileSystem()); // so FileUtil.fromFile works
        projdir = scratch.createFolder("proj");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/project.xml"), projdir, "nbproject/project.xml");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/private.xml"), projdir, "nbproject/private/private.xml");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/project.properties"), projdir, "nbproject/project.properties");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/private.properties"), projdir, "nbproject/private/private.properties");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/global.properties"), scratch, "userdir/build.properties");
        TestUtil.setLookup(Lookups.fixed(new Object[] {
            new AntBasedProjectFactorySingleton(),
            AntBasedTestUtil.testAntBasedProjectType(),
        }));
        pm = ProjectManager.getDefault();
        p = pm.findProject(projdir);
        h = (AntProjectHelper)p.getLookup().lookup(AntProjectHelper.class);
        l = new AntBasedTestUtil.TestListener();
    }
    
    protected void tearDown() throws Exception {
        Repository.getDefault().removeFileSystem(scratch.getFileSystem());
        scratch = null;
        projdir = null;
        pm = null;
        p = null;
        h = null;
        l = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }

    /**
     * Test that Ant-based projects are at least recognized as such.
     * @throws Exception if anything unexpected happens
     */
    public void testBasicRecognition() throws Exception {
        assertNotNull("recognized the project", p);
        assertEquals("correct project directory", projdir, p.getProjectDirectory());
        assertEquals("found something in project lookup", "hello", p.getLookup().lookup(String.class));
    }
    
    /**
     * Test that it is possible to retrieve the main data from project.xml and private.xml.
     * @throws Exception if anything unexpected happens
     */
    public void testGetPrimaryConfigurationData() throws Exception {
        assertNotNull("Had helper in lookup", h);
        Element data = h.getPrimaryConfigurationData(true);
        assertEquals("correct element name", "data", data.getLocalName());
        assertEquals("correct element namespace", "urn:test:shared", data.getNamespaceURI());
        Element stuff = Util.findElement(data, "shared-stuff", "urn:test:shared");
        assertNotNull("had nested stuff in it", stuff);
        data = h.getPrimaryConfigurationData(false);
        assertEquals("correct element name", "data", data.getLocalName());
        assertEquals("correct element namespace", "urn:test:private", data.getNamespaceURI());
        stuff = Util.findElement(data, "private-stuff", "urn:test:private");
        assertNotNull("had nested stuff in it", stuff);
    }
    
    /**
     * Test that after retrieving XML config data, you can't mess up other internal stuff.
     * @throws Exception if anything unexpected happens
     */
    public void testImmutabilityOfGottenConfigurationData() throws Exception {
        Element data = h.getPrimaryConfigurationData(true);
        assertNull("no parent for data", data.getParentNode());
        // XXX assure that modifications to data have no effect on a subsequent call
        // XXX get the ownerDocument and assure that the tree cannot be modified using it
    }
    
    /**
     * Test that it is possible to load properties from .properties files.
     * @throws Exception if anything unexpected happens
     */
    public void testGetProperties() throws Exception {
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull("getProperties should never return null", ep);
        assertEquals("three properties defined", 3, ep.size());
        assertEquals("shared.prop correct", "value1", ep.get("shared.prop"));
        assertEquals("overridden.prop correct", "value3", ep.get("overridden.prop"));
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        assertNotNull("getProperties should never return null", ep);
        assertEquals("four properties defined", 4, ep.size());
        assertEquals("private.prop correct", "value2", ep.get("private.prop"));
        assertEquals("overridden.prop correct", "value4", ep.get("overridden.prop"));
        ep = h.getProperties("bogus/path.properties");
        assertNotNull("getProperties should never return null", ep);
        assertEquals("no properties defined", 0, ep.size());
    }
    
    /**
     * Test that Ant properties can be evaluated with proper (recursive) substitutions.
     * @throws Exception if anything unexpected happens
     */
    public void testEvaluate() throws Exception {
        assertEquals("shared.prop correct", "value1", h.evaluate("shared.prop"));
        assertEquals("private.prop correct", "value2", h.evaluate("private.prop"));
        assertEquals("overridden.prop correct", "value4", h.evaluate("overridden.prop"));
        assertEquals("derived.prop correct", "value2:value1:${undefined.prop}", h.evaluate("derived.prop"));
        assertEquals("tempdir correct", System.getProperty("java.io.tmpdir") + "/foo", h.evaluate("tempdir"));
        assertEquals("global.prop correct", "value5", h.evaluate("global.prop"));
        assertEquals("does not have other defs", null, h.evaluate("bogus.prop"));
        Map m = h.evaluateAll();
        assertEquals("shared.prop correct", "value1", m.get("shared.prop"));
        assertEquals("private.prop correct", "value2", m.get("private.prop"));
        assertEquals("overridden.prop correct", "value4", m.get("overridden.prop"));
        assertEquals("derived.prop correct", "value2:value1:${undefined.prop}", m.get("derived.prop"));
        assertEquals("tempdir correct", System.getProperty("java.io.tmpdir") + "/foo", m.get("tempdir"));
        assertEquals("global.prop correct", "value5", m.get("global.prop"));
        assertEquals("does not have other defs", null, m.get("bogus.prop"));
        assertEquals("correct evaluateString", "value1:value2",
            h.evaluateString("${shared.prop}:${private.prop}"));
    }
    
    /**
     * Test that resolving file names relative to the project basedir works.
     * @throws Exception if anything unexpected happens
     */
    public void testResolveFile() throws Exception {
        // XXX could also be moved to PropertyUtilsTest
        File scratchF = FileUtil.toFile(scratch);
        assertNotNull("scratch directory exists on disk", scratchF);
        File projdirF = FileUtil.toFile(projdir);
        assertNotNull("project directory exists on disk", projdirF);
        assertEquals(". resolves to project basedir", projdirF, h.resolveFile("."));
        assertEquals(". resolves to project basedir", projdir, h.resolveFileObject("."));
        assertEquals("simple relative path resolves", new File(projdirF, "foo"), h.resolveFile("foo"));
        assertEquals("simple relative path resolves (but there is no such file object)", null, h.resolveFileObject("foo"));
        assertEquals("Unix-style ./ resolves", new File(projdirF, "foo"), h.resolveFile("./foo"));
        assertEquals("DOS-style .\\ resolves", new File(projdirF, "foo"), h.resolveFile(".\\foo"));
        assertEquals("Unix-style ./ resolves (but there is no such file object)", null, h.resolveFileObject("./foo"));
        assertEquals("DOS-style ./ resolves (but there is no such file object)", null, h.resolveFileObject(".\\foo"));
        assertEquals(".. resolves up a dir", scratchF, h.resolveFile(".."));
        assertEquals(".. resolves up a dir", scratch, h.resolveFileObject(".."));
        assertEquals("Unix-style ../ resolves up and down", projdirF, h.resolveFile("../proj"));
        assertEquals("DOS-style ..\\ resolves up and down", projdirF, h.resolveFile("..\\proj"));
        assertEquals("Unix-style ../ resolves up and down", projdir, h.resolveFileObject("../proj"));
        assertEquals("DOS-style ..\\ resolves up and down", projdir, h.resolveFileObject("..\\proj"));
    }
    
    /**
     * Test that resolving file paths (for example, classpaths) relative to the project basedir works.
     * Note that Ant permits any kind of path separator;
     * see {@link PropertyUtils#tokenizePath} for details of the tokenization.
     * @throws Exception if anything unexpected happens
     */
    public void testResolvePath() throws Exception {
        // XXX could also be moved to PropertyUtilsTest
        File scratchF = FileUtil.toFile(scratch);
        assertNotNull("scratch dir exists on disk", scratchF);
        String scratchS = scratchF.getAbsolutePath();
        File projdirF = FileUtil.toFile(projdir);
        assertNotNull("project dir exists on disk", projdirF);
        String projdirS = projdirF.getAbsolutePath();
        assertEquals("empty path doesn't need to resolve", "", h.resolvePath(""));
        assertEquals(". resolves", projdirS, h.resolvePath("."));
        assertEquals(".. resolves", scratchS, h.resolvePath(".."));
        assertEquals("Unix-style ../ resolves", projdirS, h.resolvePath("../proj"));
        assertEquals("DOS-style ..\\ resolves", projdirS, h.resolvePath("..\\proj"));
        String longpath = projdirS + File.pathSeparator + scratchS + File.pathSeparator + projdirS + File.pathSeparator + projdirS;
        assertEquals("mixed Unix-style path resolves", longpath, h.resolvePath(".:..:../proj:..\\proj"));
        assertEquals("mixed DOS-style path resolves", longpath, h.resolvePath(".;..;../proj;..\\proj"));
        assertEquals("absolute path resolves to itself", scratchS, h.resolvePath(scratchS));
        // XXX check use of Unix symlinks - don't want them canonicalized
        // details of tokenization semantics left to PropertyUtilsTest.testTokenizePath
    }
    
    /**
     * Test that storing changes to .properties files works.
     * @throws Exception if anything unexpected happens
     */
    public void testPutProperties() throws Exception {
        h.addAntProjectListener(l);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull("getProperties should never return null", ep);
        assertEquals("three properties defined", 3, ep.size());
        ep.put("testprop", "testval");
        assertTrue("uncommitted changes do not modify project", !pm.isModified(p));
        assertEquals("uncommitted changes not yet in project properties", null, h.evaluate("testprop"));
        assertEquals("uncommitted changes fire no events", 0, l.events().length);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        assertTrue("committed changes do modify project", pm.isModified(p));
        AntProjectEvent[] evs = l.events();
        assertEquals("putProperties fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PROJECT_PROPERTIES_PATH, evs[0].getPath());
        assertTrue("expected change", evs[0].isExpected());
        assertEquals("committed changes are in project properties", "testval", h.evaluate("testprop"));
        Properties props = AntBasedTestUtil.slurpProperties(h, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull("project.properties already exists", props);
        assertEquals("project.properties does not yet contain testprop", null, props.getProperty("testprop"));
        pm.saveProject(p);
        assertTrue("project is now saved", !pm.isModified(p));
        assertEquals("saving changes fires no new events", 0, l.events().length);
        assertEquals("committed & saved changes are in project properties", "testval", h.evaluate("testprop"));
        props = AntBasedTestUtil.slurpProperties(h, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull("project.properties still exists", props);
        assertEquals("project.properties now contains testprop", "testval", props.getProperty("testprop"));
        // XXX try creating and deleting property files
        // XXX try modifying several property files and saving in a batch
        // XXX try storing unmodified properties and see what happens
        // XXX try storing a fresh EditableProperties object not returned from getProperties
    }
    
    /**
     * Test that writing changes to project.xml and private.xml works.
     * @throws Exception if anything unexpected happens
     */
    public void testPutPrimaryConfigurationData() throws Exception {
        h.addAntProjectListener(l);
        Element data = h.getPrimaryConfigurationData(true);
        assertNotNull("<shared-stuff/> is there to start", Util.findElement(data, "shared-stuff", "urn:test:shared"));
        assertTrue("project is not initially modified", !pm.isModified(p));
        assertEquals("gPCD fires no events", 0, l.events().length);
        assertNotNull("config data has an owner document", data.getOwnerDocument());
        Element nue = data.getOwnerDocument().createElementNS("urn:test:shared", "misc");
        data.appendChild(nue);
        assertTrue("project is not modified after uncommitted change", !pm.isModified(p));
        assertEquals("no events fired after uncommitted change", 0, l.events().length);
        assertEquals("after uncommitted change gPCD does not yet have new <misc/>", null, Util.findElement(h.getPrimaryConfigurationData(true), "misc", "urn:test:shared"));
        h.putPrimaryConfigurationData(data, true);
        assertTrue("project is modified after committed change", pm.isModified(p));
        AntProjectEvent[] evs = l.events();
        assertEquals("putPCD fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PROJECT_XML_PATH, evs[0].getPath());
        assertTrue("expected change", evs[0].isExpected());
        nue = Util.findElement(h.getPrimaryConfigurationData(true), "misc", "urn:test:shared");
        assertNotNull("after committed change gPCD has new <misc/>", nue);
        assertEquals("new element name is correct", "misc", nue.getLocalName());
        assertEquals("new element namespace is correct", "urn:test:shared", nue.getNamespaceURI());
        Document doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        Element configuration = Util.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("still has <configuration> on disk", configuration);
        data = Util.findElement(configuration, "data", "urn:test:shared");
        assertNotNull("still has <data> on disk", data);
        nue = Util.findElement(data, "misc", "urn:test:shared");
        assertEquals("<misc/> not yet on disk", null, nue);
        pm.saveProject(p);
        assertTrue("project is not modified after save", !pm.isModified(p));
        assertEquals("saving changes fires no new events", 0, l.events().length);
        nue = Util.findElement(h.getPrimaryConfigurationData(true), "misc", "urn:test:shared");
        assertNotNull("after save gPCD still has new <misc/>", nue);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        configuration = Util.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("still has <configuration> on disk", configuration);
        data = Util.findElement(configuration, "data", "urn:test:shared");
        assertNotNull("still has <data> on disk", data);
        nue = Util.findElement(data, "misc", "urn:test:shared");
        assertNotNull("<misc/> now on disk", nue);
        // XXX try private.xml too
        // XXX try modifying both XML files, or different parts of the same, and saving in a batch
        // XXX try storing unmodified XML fragments and see what happens
        // XXX try storing a fresh Element not returned from getPrimaryConfigurationData
    }
    
    /**
     * Test that reading and writing the project code and display names works.
     * @throws Exception if anything unexpected happens
     */
    public void testNameAndDisplayName() throws Exception {
        h.addAntProjectListener(l);
        assertEquals("correct code name", "somename", h.getName());
        assertEquals("correct display name", "Some Name", h.getDisplayName());
        assertTrue("project not yet modified", !pm.isModified(p));
        h.setDisplayName("New Name");
        assertEquals("correct new display name", "New Name", h.getDisplayName());
        assertTrue("project now modified", pm.isModified(p));
        AntProjectEvent[] evs = l.events();
        assertEquals("sDN fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PROJECT_XML_PATH, evs[0].getPath());
        assertTrue("expected change", evs[0].isExpected());
        Document doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        Element dn = Util.findElement(doc.getDocumentElement(), "display-name", AntProjectHelper.PROJECT_NS);
        assertNotNull("project.xml still has <display-name>", dn);
        assertEquals("project.xml still has old display name", "Some Name", Util.findText(dn));
        pm.saveProject(p);
        assertEquals("still correct new display name", "New Name", h.getDisplayName());
        assertTrue("project now saved", !pm.isModified(p));
        assertEquals("saving changes fires no new events", 0, l.events().length);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        dn = Util.findElement(doc.getDocumentElement(), "display-name", AntProjectHelper.PROJECT_NS);
        assertNotNull("project.xml still has <display-name>", dn);
        assertEquals("project.xml now has new display name", "New Name", Util.findText(dn));
        h.setDisplayName("New Name");
        assertTrue("setting the same name does not modify project", !pm.isModified(p));
        assertEquals("no-op sDN fires no events", 0, l.events().length);
        h.setName("somename2");
        assertEquals("correct new code name", "somename2", h.getName());
        assertTrue("project now modified", pm.isModified(p));
        evs = l.events();
        assertEquals("sDN fires one event", 1, evs.length);
        assertEquals("correct path", AntProjectHelper.PROJECT_XML_PATH, evs[0].getPath());
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        Element nm = Util.findElement(doc.getDocumentElement(), "name", AntProjectHelper.PROJECT_NS);
        assertEquals("project.xml still has old code name", "somename", Util.findText(nm));
        pm.saveProject(p);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        nm = Util.findElement(doc.getDocumentElement(), "name", AntProjectHelper.PROJECT_NS);
        assertEquals("project.xml now has new code name", "somename2", Util.findText(nm));
        h.setName("somename2");
        assertFalse("setting the same name does not modify project", pm.isModified(p));
    }
    
    /**
     * Test that it is possible for external code to store custom data in project.xml and private.xml.
     * @throws Exception if anything unexpected happens
     */
    public void testExtensibleMetadataProviderImpl() throws Exception {
        ExtensibleMetadataProvider emp = (ExtensibleMetadataProvider)p.getLookup().lookup(ExtensibleMetadataProvider.class);
        assertNotNull("ExtensibleMetadataProvider present", emp);
        // Check cache dir.
        FileObject cache = emp.getCacheDirectory();
        assertNotNull("has a cache dir", cache);
        assertTrue("cache dir is a folder", cache.isFolder());
        assertEquals("cache dir is empty", Collections.EMPTY_LIST, Arrays.asList(cache.getChildren()));
        cache.createData("foo");
        cache = emp.getCacheDirectory();
        assertEquals("cache contents still there", 1, cache.getChildren().length);
        // Check read of shared data.
        h.addAntProjectListener(l);
        Element data = emp.getConfigurationFragment("data", "urn:test:shared-aux", true);
        assertNotNull("found shared <data>", data);
        assertEquals("correct name", "data", data.getLocalName());
        assertEquals("correct namespace", "urn:test:shared-aux", data.getNamespaceURI());
        Element stuff = Util.findElement(data, "aux-shared-stuff", "urn:test:shared-aux");
        assertNotNull("found <aux-shared-stuff/>", stuff);
        assertEquals("gCF fires no changes", 0, l.events().length);
        // Check write of shared data.
        stuff.setAttribute("attr", "val");
        assertFalse("project not modified by local change", pm.isModified(p));
        emp.putConfigurationFragment(data, true);
        assertTrue("now project is modified", pm.isModified(p));
        AntProjectEvent[] evs = l.events();
        assertEquals("pCF fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PROJECT_XML_PATH, evs[0].getPath());
        assertTrue("expected change", evs[0].isExpected());
        pm.saveProject(p);
        assertEquals("saving project fires no new changes", 0, l.events().length);
        Document doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        Element config = Util.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("<configuration> still exists", config);
        data = Util.findElement(config, "data", "urn:test:shared-aux");
        assertNotNull("<data> still exists", data);
        stuff = Util.findElement(data, "aux-shared-stuff", "urn:test:shared-aux");
        assertNotNull("still have <aux-shared-stuff/>", stuff);
        assertEquals("attr written correctly", "val", stuff.getAttribute("attr"));
        // Check read of private data.
        data = emp.getConfigurationFragment("data", "urn:test:private-aux", false);
        assertNotNull("found shared <data>", data);
        assertEquals("correct name", "data", data.getLocalName());
        assertEquals("correct namespace", "urn:test:private-aux", data.getNamespaceURI());
        stuff = Util.findElement(data, "aux-private-stuff", "urn:test:private-aux");
        assertNotNull("found <aux-private-stuff/>", stuff);
        assertEquals("gCF fires no changes", 0, l.events().length);
        // Check write of private data.
        stuff.setAttribute("attr", "val");
        assertFalse("project not modified by local change", pm.isModified(p));
        emp.putConfigurationFragment(data, false);
        assertTrue("now project is modified", pm.isModified(p));
        evs = l.events();
        assertEquals("pCF fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PRIVATE_XML_PATH, evs[0].getPath());
        assertTrue("expected change", evs[0].isExpected());
        pm.saveProject(p);
        assertEquals("saving project fires no new changes", 0, l.events().length);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PRIVATE_XML_PATH);
        config = doc.getDocumentElement();
        data = Util.findElement(config, "data", "urn:test:private-aux");
        assertNotNull("<data> still exists", data);
        stuff = Util.findElement(data, "aux-private-stuff", "urn:test:private-aux");
        assertNotNull("still have <aux-private-stuff/>", stuff);
        assertEquals("attr written correctly", "val", stuff.getAttribute("attr"));
        // Check that missing fragments are not returned.
        Element bogus = emp.getConfigurationFragment("doesn't exist", "bogus", true);
        assertNull("no such fragment - wrong name/ns", bogus);
        bogus = emp.getConfigurationFragment("data", "bogus", true);
        assertNull("no such fragment - wrong ns", bogus);
        bogus = emp.getConfigurationFragment("doesn't exist", "urn:test:shared-aux", true);
        assertNull("no such fragment - wrong name", bogus);
        bogus = emp.getConfigurationFragment("data", "urn:test:shared-aux", false);
        assertNull("no such fragment - wrong file", bogus);
        // Try adding a new fragment.
        Document temp = XMLUtil.createDocument("whatever", null, null, null);
        data = temp.createElementNS("urn:test:whatever", "hello");
        data.appendChild(temp.createTextNode("stuff"));
        assertFalse("project currently unmodified", pm.isModified(p));
        emp.putConfigurationFragment(data, true);
        assertTrue("adding frag modified project", pm.isModified(p));
        evs = l.events();
        assertEquals("pCF fires one event", 1, evs.length);
        assertEquals("correct path", AntProjectHelper.PROJECT_XML_PATH, evs[0].getPath());
        pm.saveProject(p);
        assertEquals("saving project fires no new changes", 0, l.events().length);
        data = emp.getConfigurationFragment("hello", "urn:test:whatever", true);
        assertNotNull("can retrieve new frag", data);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        config = Util.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("<configuration> still exists", config);
        data = Util.findElement(config, "hello", "urn:test:whatever");
        assertNotNull("<hello> still exists", data);
        assertEquals("correct nested contents too", "stuff", Util.findText(data));
        // Try removing a fragment.
        assertFalse("project is unmodified", pm.isModified(p));
        assertTrue("can remove new frag", emp.removeConfigurationFragment("hello", "urn:test:whatever", true));
        assertTrue("project is now modified", pm.isModified(p));
        assertNull("now frag is gone", emp.getConfigurationFragment("hello", "urn:test:whatever", true));
        pm.saveProject(p);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        config = Util.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("<configuration> still exists", config);
        data = Util.findElement(config, "hello", "urn:test:whatever");
        assertNull("now <hello> is gone", data);
        assertFalse("cannot remove a frag that is not there", emp.removeConfigurationFragment("hello", "urn:test:whatever", true));
        assertFalse("trying to remove a nonexistent frag does not modify project", pm.isModified(p));
        // XXX check that it cannot be used to load or store primary configuration data
        // or other general fixed metadata
        // XXX try overwriting data
    }

}
