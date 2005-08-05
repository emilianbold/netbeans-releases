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

package org.netbeans.modules.ant.freeform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.actions.FindAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Test functionality of actions in FreeformProject.
 * This class just tests the basic functionality found in the "simple" project.
 * @author Jesse Glick
 */
public class ActionsTest extends TestBase {
    
    private static final class AntTargetInvocation {
        public final FileObject scriptFile;
        public final String[] targetNameArray;
        public final Map/*<String,String>*/ props;
        public AntTargetInvocation(FileObject scriptFile, String[] targetNameArray, Map/*<String,String>*/ props) {
            assert scriptFile != null;
            this.scriptFile = scriptFile;
            this.targetNameArray = targetNameArray;
            this.props = props != null ? new HashMap(props) : Collections.EMPTY_MAP;
        }
        public String toString() {
            return "invocation<script=" + scriptFile + ",targets=" + (targetNameArray != null ? Arrays.asList(targetNameArray) : null) + ",props=" + props + ">";
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof AntTargetInvocation)) {
                return false;
            }
            AntTargetInvocation other = (AntTargetInvocation) obj;
            return other.scriptFile == scriptFile &&
                Utilities.compareObjects(other.targetNameArray, targetNameArray) &&
                other.props.equals(props);
        }
        public int hashCode() {
            int x = scriptFile.hashCode() ^ props.hashCode();
            if (targetNameArray != null) {
                x ^= Arrays.asList(targetNameArray).hashCode();
            }
            return x;
        }
    }
    
    private static final List/*<AntTargetInvocation>*/ targetsRun = new ArrayList();
    
    static {
        Actions.TARGET_RUNNER = new Actions.TargetRunner() {
            public void runTarget(FileObject scriptFile, String[] targetNameArray, Properties props) {
                targetsRun.add(new AntTargetInvocation(scriptFile, targetNameArray, props));
            }
        };
    }
    
    public ActionsTest(String name) {
        super(name);
    }
    
    private FileObject buildXml;
    private ActionProvider ap;
    private LogicalViewProvider lvp;
    private DataObject myAppJavaDO, someFileJavaDO, someResourceTxtDO, specialTaskJavaDO;
    
    protected void setUp() throws Exception {
        super.setUp();
        targetsRun.clear();
        buildXml = simple.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("found build.xml", buildXml);
        ap = (ActionProvider) simple.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        FileObject myAppJava = simple.getProjectDirectory().getFileObject("src/org/foo/myapp/MyApp.java");
        assertNotNull("have MyApp.java", myAppJava);
        myAppJavaDO = DataObject.find(myAppJava);
        FileObject someFileJava = simple.getProjectDirectory().getFileObject("src/org/foo/myapp/SomeFile.java");
        assertNotNull("have SomeFile.java", someFileJava);
        someFileJavaDO = DataObject.find(someFileJava);
        FileObject someResourceTxt = simple.getProjectDirectory().getFileObject("src/org/foo/myapp/some-resource.txt");
        assertNotNull("have some-resource.txt", someResourceTxt);
        someResourceTxtDO = DataObject.find(someResourceTxt);
        FileObject specialTaskJava = simple.getProjectDirectory().getFileObject("antsrc/org/foo/ant/SpecialTask.java");
        assertNotNull("have SpecialTask.java", specialTaskJava);
        specialTaskJavaDO = DataObject.find(specialTaskJava);
        lvp = (LogicalViewProvider) simple.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
    }
    
    public boolean runInEQ () {
        return true;
    }
    
    public void testBasicActions() throws Exception {
        List/*<String>*/ actionNames = new ArrayList(Arrays.asList(ap.getSupportedActions()));
        Collections.sort(actionNames);
        assertEquals("right action names", Arrays.asList(new String[] {
            "build",
            "clean",
            "compile.single",
            "javadoc",
            "rebuild",
            // #46886: COMMON_NON_IDE_GLOBAL_ACTIONS are now also enabled
            "redeploy",
            "run",
            "run.single",
            // #46886 again
            "test",
        }), actionNames);
        assertTrue("clean is enabled", ap.isActionEnabled("clean", Lookup.EMPTY));
        try {
            ap.isActionEnabled("frobnitz", Lookup.EMPTY);
            fail("Should throw IAE for unrecognized commands");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        try {
            ap.invokeAction("goetterdaemmerung", Lookup.EMPTY);
            fail("Should throw IAE for unrecognized commands");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        ap.invokeAction("rebuild", Lookup.EMPTY);
        AntTargetInvocation inv = new AntTargetInvocation(buildXml, new String[] {"clean", "jar"}, null);
        assertEquals("ran right target", Collections.singletonList(inv), targetsRun);
    }
    
    public void testLogicalViewActions() throws Exception {
        Action[] actions = lvp.createLogicalView().getActions(false);
        assertNotNull("have some context actions", actions);
        ResourceBundle bundle = NbBundle.getBundle(Actions.class);
        assertEquals("correct labels", Arrays.asList(new String[] {
            (String) CommonProjectActions.newFileAction().getValue(Action.NAME),
            null,
            bundle.getString("CMD_build"),
            bundle.getString("CMD_clean"),
            bundle.getString("CMD_rebuild"),
            null,
            bundle.getString("CMD_run"),
            null,
            bundle.getString("CMD_javadoc"),
            "Generate XDocs",
            null,
            "Create Distribution",
            null,
            (String) CommonProjectActions.setAsMainProjectAction().getValue(Action.NAME),
            (String) CommonProjectActions.openSubprojectsAction().getValue(Action.NAME),
            (String) CommonProjectActions.closeProjectAction().getValue(Action.NAME),
            null,
            (String) SystemAction.get(FindAction.class).getValue(Action.NAME),
            null,
            (String) SystemAction.get(ToolsAction.class).getValue(Action.NAME),
            null,
            (String) CommonProjectActions.customizeProjectAction().getValue(Action.NAME),
        }), findActionLabels(actions));
        Action javadocAction = actions[8];
        assertEquals("this is Run Javadoc", bundle.getString("CMD_javadoc"), javadocAction.getValue(Action.NAME));
        runContextMenuAction(javadocAction, simple);
        AntTargetInvocation inv = new AntTargetInvocation(buildXml, new String[] {"build-javadoc"}, Collections.singletonMap("from-ide", "true"));
        assertEquals("ran right target", Collections.singletonList(inv), targetsRun);
        targetsRun.clear();
        Action xdocsAction = actions[9];
        assertEquals("this is Generate XDocs", "Generate XDocs", xdocsAction.getValue(Action.NAME));
        runContextMenuAction(xdocsAction, simple);
        inv = new AntTargetInvocation(buildXml, new String[] {"generate-xdocs"}, Collections.singletonMap("from-ide", "true"));
        assertEquals("ran right target", Collections.singletonList(inv), targetsRun);
    }
    
    private static List/*<String>*/ findActionLabels(Action[] actions) {
        String[] labels = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] != null) {
                String label = (String) actions[i].getValue(Action.NAME);
                if (label == null) {
                    label = "???";
                }
                labels[i] = label;
            } else {
                labels[i] = null;
            }
        }
        return Arrays.asList(labels);
    }
    
    /**
     * Run an action as if it were in the context menu of a project.
     */
    private void runContextMenuAction(Action a, Project p) {
        if (a instanceof ContextAwareAction) {
            Lookup l = Lookups.singleton(p);
            a = ((ContextAwareAction) a).createContextAwareInstance(l);
        }
        a.actionPerformed(null);
    }
    
    public void testContextSensitiveActions() throws Exception {
        assertFalse("c.s disabled on empty selection", ap.isActionEnabled("compile.single", Lookup.EMPTY));
        assertTrue("c.s enabled on SomeFile.java", ap.isActionEnabled("compile.single", Lookups.singleton(someFileJavaDO)));
        assertTrue("c.s enabled on SomeFile.java (FileObject)", ap.isActionEnabled("compile.single", Lookups.singleton(someFileJavaDO.getPrimaryFile())));
        assertTrue("c.s enabled on SpecialTask.java", ap.isActionEnabled("compile.single", Lookups.singleton(specialTaskJavaDO)));
        assertFalse("c.s disabled on some-resource.txt", ap.isActionEnabled("compile.single", Lookups.singleton(someResourceTxtDO)));
        assertTrue("c.s enabled on similar *.java", ap.isActionEnabled("compile.single", Lookups.fixed(new DataObject[] {someFileJavaDO, myAppJavaDO})));
        assertFalse("c.s disabled on mixed *.java", ap.isActionEnabled("compile.single", Lookups.fixed(new DataObject[] {someFileJavaDO, specialTaskJavaDO})));
        assertFalse("c.s disabled on mixed types", ap.isActionEnabled("compile.single", Lookups.fixed(new DataObject[] {someFileJavaDO, someResourceTxtDO})));
        assertFalse("r.s disabled on empty selection", ap.isActionEnabled("run.single", Lookup.EMPTY));
        assertTrue("r.s enabled on SomeFile.java", ap.isActionEnabled("run.single", Lookups.singleton(someFileJavaDO)));
        assertFalse("r.s disabled on SpecialTask.java", ap.isActionEnabled("run.single", Lookups.singleton(specialTaskJavaDO)));
        assertFalse("r.s disabled on some-resource.txt", ap.isActionEnabled("run.single", Lookups.singleton(someResourceTxtDO)));
        assertFalse("r.s disabled on multiple files", ap.isActionEnabled("run.single", Lookups.fixed(new DataObject[] {someFileJavaDO, myAppJavaDO})));
        ap.invokeAction("compile.single", Lookups.singleton(someFileJavaDO));
        AntTargetInvocation inv = new AntTargetInvocation(buildXml, new String[] {"compile-some-files"}, Collections.singletonMap("files", "org/foo/myapp/SomeFile.java"));
        assertEquals("compiled one file in src", Collections.singletonList(inv), targetsRun);
        targetsRun.clear();
        ap.invokeAction("compile.single", Lookups.singleton(someFileJavaDO.getPrimaryFile()));
        inv = new AntTargetInvocation(buildXml, new String[] {"compile-some-files"}, Collections.singletonMap("files", "org/foo/myapp/SomeFile.java"));
        assertEquals("compiled one file in src (FileObject)", Collections.singletonList(inv), targetsRun);
        targetsRun.clear();
        ap.invokeAction("compile.single", Lookups.singleton(specialTaskJavaDO));
        inv = new AntTargetInvocation(buildXml, new String[] {"ant-compile-some-files"}, Collections.singletonMap("files", "org/foo/ant/SpecialTask.java"));
        assertEquals("compiled one file in antsrc", Collections.singletonList(inv), targetsRun);
        targetsRun.clear();
        ap.invokeAction("compile.single", Lookups.fixed(new DataObject[] {someFileJavaDO, myAppJavaDO}));
        inv = new AntTargetInvocation(buildXml, new String[] {"compile-some-files"}, Collections.singletonMap("files", "org/foo/myapp/SomeFile.java,org/foo/myapp/MyApp.java"));
        assertEquals("compiled two files in src", Collections.singletonList(inv), targetsRun);
        targetsRun.clear();
        ap.invokeAction("run.single", Lookups.singleton(someFileJavaDO));
        inv = new AntTargetInvocation(buildXml, new String[] {"start-with-specified-class"}, Collections.singletonMap("class", "org.foo.myapp.SomeFile"));
        assertEquals("ran one file in src", Collections.singletonList(inv), targetsRun);
    }
    
}
