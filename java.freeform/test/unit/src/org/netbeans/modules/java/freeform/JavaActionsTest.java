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

package org.netbeans.modules.java.freeform;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test how well JavaActions is working to generate targets.
 * @author Jesse Glick
 */
public class JavaActionsTest extends TestBase {
    
    static {
        // Simplify testing of stuff that can include localized messages.
        Locale.setDefault(Locale.US);
    }
    
    public JavaActionsTest(String name) {
        super(name);
    }
    
    private FreeformProject prj;
    private JavaActions ja;
    private FileObject src, myAppJava, someFileJava, someResourceTxt, antsrc, specialTaskJava, buildProperties;

    protected void setUp() throws Exception {
        super.setUp();
        prj = copyProject(simple);
        // Remove existing context-sensitive bindings to make a clean slate.
        Element data = prj.helper().getPrimaryConfigurationData(true);
        Element ideActions = Util.findElement(data, "ide-actions", FreeformProjectType.NS_GENERAL);
        assertNotNull(ideActions);
        Iterator/*<Element>*/ actionsIt = Util.findSubElements(ideActions).iterator();
        while (actionsIt.hasNext()) {
            Element action = (Element) actionsIt.next();
            assertEquals("action", action.getLocalName());
            if (Util.findElement(action, "context", FreeformProjectType.NS_GENERAL) != null) {
                ideActions.removeChild(action);
            }
        }
        prj.helper().putPrimaryConfigurationData(data, true);
        ProjectManager.getDefault().saveProject(prj);
        AuxiliaryConfiguration origAux = (AuxiliaryConfiguration) prj.getLookup().lookup(AuxiliaryConfiguration.class);
        AuxiliaryConfiguration aux = new JavaProjectNature.UpgradingAuxiliaryConfiguration(origAux);
        ja = new JavaActions(prj, prj.helper(), prj.evaluator(), aux);
        src = prj.getProjectDirectory().getFileObject("src");
        assertNotNull(src);
        myAppJava = src.getFileObject("org/foo/myapp/MyApp.java");
        assertNotNull(myAppJava);
        someFileJava = src.getFileObject("org/foo/myapp/SomeFile.java");
        assertNotNull(someFileJava);
        someResourceTxt = src.getFileObject("org/foo/myapp/some-resource.txt");
        assertNotNull(someResourceTxt);
        antsrc = prj.getProjectDirectory().getFileObject("antsrc");
        assertNotNull(antsrc);
        specialTaskJava = antsrc.getFileObject("org/foo/ant/SpecialTask.java");
        assertNotNull(specialTaskJava);
        buildProperties = prj.getProjectDirectory().getFileObject("build.properties");
        assertNotNull(buildProperties);
    }
    
    public void testContainsSelectedJavaSources() throws Exception {
        assertTrue(ja.containsSelectedJavaSources(src, context(new FileObject[] {myAppJava})));
        assertFalse(ja.containsSelectedJavaSources(src, context(new FileObject[] {myAppJava, someResourceTxt})));
    }
    
    public void testFindPackageRoot() throws Exception {
        Lookup context = context(new FileObject[] {myAppJava});
        JavaActions.AntLocation loc = ja.findPackageRoot(context);
        assertNotNull("found a package root for " + context, loc);
        assertEquals("right name", "${src.dir}", loc.virtual);
        assertEquals("right physical", src, loc.physical);
        context = context(new FileObject[] {myAppJava, someFileJava});
        loc = ja.findPackageRoot(context);
        assertNotNull("found a package root for " + context, loc);
        assertEquals("right name", "${src.dir}", loc.virtual);
        assertEquals("right physical", src, loc.physical);
        context = context(new FileObject[] {src});
        loc = ja.findPackageRoot(context);
        assertNotNull("found a package root for " + context, loc);
        assertEquals("right name", "${src.dir}", loc.virtual);
        assertEquals("right physical", src, loc.physical);
        context = context(new FileObject[] {myAppJava, someResourceTxt});
        loc = ja.findPackageRoot(context);
        assertNull("found no package root for " + context + ": " + loc, loc);
        context = context(new FileObject[] {myAppJava, specialTaskJava});
        loc = ja.findPackageRoot(context);
        assertNull("found no package root for " + context, loc);
        context = context(new FileObject[] {});
        loc = ja.findPackageRoot(context);
        assertNull("found no package root for " + context, loc);
        context = context(new FileObject[] {specialTaskJava});
        loc = ja.findPackageRoot(context);
        assertNotNull("found a package root for " + context, loc);
        assertEquals("right name", "${ant.src.dir}", loc.virtual);
        assertEquals("right physical", antsrc, loc.physical);
        context = context(new FileObject[] {buildProperties});
        loc = ja.findPackageRoot(context);
        assertNull("found no package root for " + context, loc);
    }
    
    public void testGetSupportedActions() throws Exception {
        assertEquals("initially all context-sensitive actions supported",
            Collections.singletonList(ActionProvider.COMMAND_COMPILE_SINGLE),
            Arrays.asList(ja.getSupportedActions()));
        /* Not really necessary; once there is a binding, the main ant/freeform Actions will mask this anyway:
        ja.addBinding(ActionProvider.COMMAND_COMPILE_SINGLE, "target", "prop", "${dir}", null, "relative-path", null);
        assertEquals("binding a context-sensitive action makes it not be supported any longer",
            Collections.EMPTY_LIST,
            Arrays.asList(ja.getSupportedActions()));
         */
    }
    
    public void testIsActionEnabled() throws Exception {
        assertTrue("enabled on some source files", ja.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context(new FileObject[] {myAppJava, someFileJava})));
        assertFalse("disabled on other stuff", ja.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context(new FileObject[] {buildProperties})));
    }
    
    private static boolean useDO = false; // exercise lookup of both FO and DO
    private Lookup context(FileObject[] files) throws Exception {
        Object[] objs = new Object[files.length];
        for (int i = 0; i < files.length; i++) {
            objs[i] = useDO ? (Object) DataObject.find(files[i]) : files[i];
            useDO = !useDO;
        }
        return Lookups.fixed(objs);
    }
    
    public void testFindClassesOutputDir() throws Exception {
        assertEquals("Output for src", "${classes.dir}", ja.findClassesOutputDir("${src.dir}"));
        assertEquals("Output for antsrc", "${ant.classes.dir}", ja.findClassesOutputDir("${ant.src.dir}"));
        assertEquals("No output for bogussrc", null, ja.findClassesOutputDir("${bogus.src.dir}"));
    }
    
    public void testAddBinding() throws Exception {
        ja.addBinding("some.action", "special-target", "selection", "${some.src.dir}", "\\.java$", "relative-path", ",");
        Element data = prj.helper().getPrimaryConfigurationData(true);
        assertNotNull(data);
        Element ideActions = Util.findElement(data, "ide-actions", FreeformProjectType.NS_GENERAL);
        assertNotNull(ideActions);
        List/*<Element>*/ actions = Util.findSubElements(ideActions);
        Element lastAction = (Element) actions.get(actions.size() - 1);
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<action xmlns=\"http://www.netbeans.org/ns/freeform-project/1\" name=\"some.action\">\n" +
            "    <script>nbproject/ide-targets.xml</script>\n" +
            "    <target>special-target</target>\n" +
            "    <context>\n" +
            "        <property>selection</property>\n" +
            "        <folder>${some.src.dir}</folder>\n" +
            "        <pattern>\\.java$</pattern>\n" +
            "        <format>relative-path</format>\n" +
            "        <arity>\n" +
            "            <separated-files>,</separated-files>\n" +
            "        </arity>\n" +
            "    </context>\n" +
            "</action>\n";
        assertEquals(expectedXml, xmlToString(lastAction));
        ja.addBinding("some.other.action", "special-target", "selection", "${some.src.dir}", null, "relative-path", null);
        data = prj.helper().getPrimaryConfigurationData(true);
        ideActions = Util.findElement(data, "ide-actions", FreeformProjectType.NS_GENERAL);
        actions = Util.findSubElements(ideActions);
        lastAction = (Element) actions.get(actions.size() - 1);
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<action xmlns=\"http://www.netbeans.org/ns/freeform-project/1\" name=\"some.other.action\">\n" +
            "    <script>nbproject/ide-targets.xml</script>\n" +
            "    <target>special-target</target>\n" +
            "    <context>\n" +
            "        <property>selection</property>\n" +
            "        <folder>${some.src.dir}</folder>\n" +
            "        <format>relative-path</format>\n" +
            "        <arity>\n" +
            "            <one-file-only/>\n" +
            "        </arity>\n" +
            "    </context>\n" +
            "</action>\n";
        assertEquals(expectedXml, xmlToString(lastAction));
    }
    
    public void testCreateCompileSingleTarget() throws Exception {
        Document doc = XMLUtil.createDocument("fake", null, null, null);
        Lookup context = context(new FileObject[] {someFileJava});
        Element target = ja.createCompileSingleTarget(doc, context, "files", new JavaActions.AntLocation("${src.dir}", src));
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<target name=\"compile-selected-files-in-src\">\n" +
            "    <fail unless=\"files\">Must set property 'files'</fail>\n" +
            "    <mkdir dir=\"${classes.dir}\"/>\n" +
            "    <javac destdir=\"${classes.dir}\" includes=\"${files}\" source=\"1.4\" srcdir=\"${src.dir}\">\n" +
            "        <classpath path=\"${src.cp}\"/>\n" +
            "    </javac>\n" +
            "</target>\n";
        assertEquals(expectedXml, xmlToString(target));
    }
    
    public void testReadWriteCustomScript() throws Exception {
        Document script = ja.readCustomScript();
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project basedir=\"..\" name=\"Simple Freeform Project-IDE\"/>\n";
        assertEquals(expectedXml, xmlToString(script.getDocumentElement()));
        script.getDocumentElement().appendChild(script.createElement("foo"));
        ja.writeCustomScript(script);
        script = ja.readCustomScript();
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project basedir=\"..\" name=\"Simple Freeform Project-IDE\">\n" +
            "    <foo/>\n" +
            "</project>\n";
        assertEquals(expectedXml, xmlToString(script.getDocumentElement()));
    }
    
    public void testFindSourceLevel() throws Exception {
        assertEquals("1.4", ja.findSourceLevel("${src.dir}"));
        assertEquals("1.4", ja.findSourceLevel("${ant.src.dir}"));
        assertEquals(null, ja.findSourceLevel("${bogus.src.dir}"));
    }
    
    public void testFindCompileClasspath() throws Exception {
        assertEquals("${src.cp}", ja.findCompileClasspath("${src.dir}"));
        assertEquals("${ant.src.cp}", ja.findCompileClasspath("${ant.src.dir}"));
        assertEquals(null, ja.findCompileClasspath("${bogus.src.dir}"));
    }
    
    public void testFindLine() throws Exception {
        Document script = ja.readCustomScript();
        Element target = script.createElement("target");
        target.setAttribute("name", "targ1");
        target.appendChild(script.createElement("task1"));
        target.appendChild(script.createElement("task2"));
        script.getDocumentElement().appendChild(target);
        target = script.createElement("target");
        target.setAttribute("name", "targ2");
        target.appendChild(script.createElement("task3"));
        script.getDocumentElement().appendChild(target);
        ja.writeCustomScript(script);
        FileObject scriptFile = prj.getProjectDirectory().getFileObject(JavaActions.SCRIPT_PATH);
        assertNotNull(scriptFile);
        //0 <?xml?>
        //1 <project>
        //2     <target name="targ1">
        //3         <task1/>
        //4         <task2/>
        //5     </>
        //6     <target name="targ2">
        //7         <task3/>
        //8     </>
        //9 </>
        assertEquals(2, JavaActions.findLine(scriptFile, "targ1"));
        assertEquals(6, JavaActions.findLine(scriptFile, "targ2"));
        assertEquals(-1, JavaActions.findLine(scriptFile, "no-such-targ"));
    }
    
    /**
     * Format XML as a string. Assumes Xerces serializer in current impl.
     * Collapse all comments to no body.
     */
    private static String xmlToString(Element el) throws Exception {
        Document doc = XMLUtil.createDocument("fake", null, null, null);
        doc.removeChild(doc.getDocumentElement());
        doc.appendChild(doc.importNode(el, true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        return baos.toString("UTF-8").replaceAll("<!--([^-]|-[^-])*-->", "<!---->");
    }
    
}
