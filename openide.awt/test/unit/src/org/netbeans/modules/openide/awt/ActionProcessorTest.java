/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.awt;

import java.net.URL;
import javax.tools.ToolProvider;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import java.io.IOException;
import org.openide.util.test.AnnotationProcessorTestUtils;
import java.util.Collections;
import java.util.List;
import org.openide.awt.ActionID;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.net.URLClassLoader;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JSeparator;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ActionProcessorTest extends NbTestCase {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public ActionProcessorTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testHeadlessCompilationWorks() throws IOException {
        clearWorkDir();
        assertTrue("Headless run", GraphicsEnvironment.isHeadless());
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A",
                  "import org.openide.awt.ActionRegistration;\n"
                + "import org.openide.awt.ActionID;\n"
                + "import org.openide.awt.ActionReference;\n"
                + "import java.awt.event.*;\n"
                + "@ActionID(category=\"Tools\",id=\"my.action\")"
                + "@ActionRegistration(displayName=\"AAA\") "
                + "@ActionReference(path=\"Shortcuts\", name=\"C-F2 D-A\")"
                + "public class A implements ActionListener {\n"
                + "    public void actionPerformed(ActionEvent e) {}"
                + "}\n");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation has to succeed:\n" + os, r);
    }

    @ActionRegistration(
        displayName="#AlwaysOn"
    )
    @ActionID(
        id="my.test.Always", category="Tools"
    )
    @ActionReference(path="My/Folder", position=333, name="D-F6")        
    public static final class Always implements ActionListener {
        static int created;

        public Always() {
            created++;
        }

        static int cnt;
        @Override
        public void actionPerformed(ActionEvent e) {
            cnt += e.getID();
        }
    }

    public void testAlwaysEnabledAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/my-test-Always.instance"
        );
        assertNotNull("File found", fo);
        assertEquals("Not created yet", 0, Always.created);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is an action", obj instanceof Action);
        Action a = (Action)obj;
        assertEquals("Still not created", 0, Always.created);

        assertEquals("I am always on!", a.getValue(Action.NAME));
        assertEquals("Not even now created", 0, Always.created);
        a.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Created now!", 1, Always.created);

        assertEquals("Action called", 300, Always.cnt);
        
        FileObject shad = FileUtil.getConfigFile(
            "My/Folder/D-F6.shadow"
        );
        assertNotNull("Shadow created", shad);
        assertEquals("Right position", 333, shad.getAttribute("position"));
        assertEquals("Proper link", fo.getPath(), shad.getAttribute("originalFile"));
    }
    
    public void testVerifyReferencesInstalledViaPackageInfo() {
        FileObject one = FileUtil.getConfigFile("pkg/one/action-one.shadow");
        assertNotNull("Found", one);
        assertEquals("Actions/Fool/action-one.instance", one.getAttribute("originalFile"));
        
        FileObject two = FileUtil.getConfigFile("pkg/two/action-two.shadow");
        assertNotNull("Found", two);
        assertEquals("Actions/Pool/action-two.instance", two.getAttribute("originalFile"));
    }

    public static final class AlwaysByMethod {
        private AlwaysByMethod() {}
        static int created, cnt;
        @ActionRegistration(displayName="#AlwaysOn")
        @ActionID(id="my.test.AlwaysByMethod", category="Tools")
        @ActionReferences({
            @ActionReference(path="Kuk/buk", position=1, separatorAfter=2),
            @ActionReference(path="Muk/luk", position=11, separatorBefore=10)
        })
        public static ActionListener factory() {
            created++;
            return new ActionListener() {
                public @Override void actionPerformed(ActionEvent e) {
                    cnt += e.getID();
                }
            };
        }
    }

    public void testAlwaysEnabledActionByMethod() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Actions/Tools/my-test-AlwaysByMethod.instance");
        assertNotNull("File found", fo);
        assertEquals("Not created yet", 0, AlwaysByMethod.created);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is an action", obj instanceof Action);
        Action a = (Action)obj;
        assertEquals("Still not created", 0, AlwaysByMethod.created);
        assertEquals("I am always on!", a.getValue(Action.NAME));
        assertEquals("Not even now created", 0, AlwaysByMethod.created);
        a.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Created now!", 1, AlwaysByMethod.created);
        assertEquals("Action called", 300, AlwaysByMethod.cnt);

        {
            FileObject shad = FileUtil.getConfigFile(
                "Kuk/buk/my-test-AlwaysByMethod.shadow"
            );
            assertNotNull("Shadow created", shad);
            assertEquals("Right position", 1, shad.getAttribute("position"));
            assertEquals("Proper link", fo.getPath(), shad.getAttribute("originalFile"));
            FileObject sep = FileUtil.getConfigFile(
                "Kuk/buk/my-test-AlwaysByMethod-separatorAfter.instance"
            );
            assertNotNull("Separator generated", sep);
            assertEquals("Position 2", 2, sep.getAttribute("position"));
            Object instSep = sep.getAttribute("instanceCreate");
            assertTrue("Right instance " + instSep, instSep instanceof JSeparator);
        }
        {
            FileObject shad = FileUtil.getConfigFile(
                "Muk/luk/my-test-AlwaysByMethod.shadow"
            );
            assertNotNull("Shadow created", shad);
            assertEquals("Right position", 11, shad.getAttribute("position"));
            assertEquals("Proper link", fo.getPath(), shad.getAttribute("originalFile"));
            FileObject sep = FileUtil.getConfigFile(
                "Muk/luk/my-test-AlwaysByMethod-separatorBefore.instance"
            );
            assertNotNull("Separator generated", sep);
            assertEquals("Position ten", 10, sep.getAttribute("position"));
            Object instSep = sep.getAttribute("instanceCreate");
            assertTrue("Right instance " + instSep, instSep instanceof JSeparator);
        }
        
    }

    @ActionRegistration(
        displayName="#Key",
        key="klic"
    )
    @ActionID(
        category="Tools",
        id = "my.action"
    )
    public static final class Callback implements ActionListener {
        static int cnt;
        @Override
        public void actionPerformed(ActionEvent e) {
            cnt += e.getID();
        }
    }

    public void testCallbackAction() throws Exception {
        Callback.cnt = 0;
        
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/my-action.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        class MyAction extends AbstractAction {
            int cnt;
            @Override
            public void actionPerformed(ActionEvent e) {
                cnt += e.getID();
            }
        }
        MyAction my = new MyAction();
        ActionMap m = new ActionMap();
        m.put("klic", my);

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(m);

        assertEquals("I am context", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Local Action called", 300, my.cnt);
        assertEquals("Global Action not called", 0, Callback.cnt);

        ic.remove(m);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Local Action stays", 300, my.cnt);
        assertEquals("Global Action ncalled", 200, Callback.cnt);
    }
    
    
    @ActionRegistration(
        displayName = "#Key",
        iconBase="org/openide/awt/TestIcon.png"
    )
    @ActionID(
        category = "Edit",
        id = "my.field.action"
    )
    public static final String ACTION_MAP_KEY = "my.action.map.key";
    
    public void testCallbackOnFieldAction() throws Exception {
        Callback.cnt = 0;
        
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Edit/my-field-action.instance"
        );
        assertNotNull("File found", fo);
        Object icon = fo.getAttribute("iconBase");
        assertTrue("Icon found", icon instanceof String);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        class MyAction extends AbstractAction {
            int cnt;
            @Override
            public void actionPerformed(ActionEvent e) {
                cnt += e.getID();
            }
        }
        MyAction my = new MyAction();
        ActionMap m = new ActionMap();
        m.put(ACTION_MAP_KEY, my);

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(m);

        assertEquals("I am context", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Local Action called", 300, my.cnt);
        assertEquals("Global Action not called", 0, Callback.cnt);

        ic.remove(m);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Local Action stays", 300, my.cnt);
        assertEquals("Global Action not called, there is no fallback", 0, Callback.cnt);
    }

    @ActionID(category = "Tools", id = "on.int")
    @ActionRegistration(displayName = "#OnInt")
    public static final class Context implements ActionListener {
        private final int context;
        
        public Context(Integer context) {
            this.context = context;
        }

        static int cnt;

        @Override
        public void actionPerformed(ActionEvent e) {
            cnt += context;
        }

    }

    public void testContextAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/on-int.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(10);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 10, Context.cnt);

        ic.remove(10);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Global Action stays same", 10, Context.cnt);
    }

    @ActionRegistration(
        displayName="#OnInt"
    )
    @ActionID(
        category="Tools",
        id="on.numbers"
    )
    public static final class MultiContext implements ActionListener {
        private final List<Number> context;

        public MultiContext(List<Number> context) {
            this.context = context;
        }

        static int cnt;

        @Override
        public void actionPerformed(ActionEvent e) {
            for (Number n : context) {
                cnt += n.intValue();
            }
        }

    }

    public void testMultiContextAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/on-numbers.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(10);
        ic.add(3L);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 13, MultiContext.cnt);

        ic.remove(10);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Adds 3", 16, MultiContext.cnt);

        ic.remove(3L);
        assertFalse("It is disabled", clone.isEnabled());
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("No change", 16, MultiContext.cnt);
    }
    
    @ActionRegistration(displayName="somename", surviveFocusChange=true)
    @ActionID(category="Windows", id="my.survival.action")
    public static final String SURVIVE_KEY = "somekey";

    public void testSurviveFocusChangeBehavior() throws Exception {
        class MyAction extends AbstractAction {
            public int cntEnabled;
            public int cntPerformed;
            
            @Override
            public boolean isEnabled() {
                cntEnabled++;
                return true;
            }
            
            @Override
            public void actionPerformed(ActionEvent ev) {
                cntPerformed++;
            }
        }
        MyAction myAction = new MyAction();
        
        ActionMap disable = new ActionMap();
        ActionMap enable = new ActionMap();
        
        InstanceContent ic = new InstanceContent();
        AbstractLookup al = new AbstractLookup(ic);
        
        FileObject fo = FileUtil.getConfigFile(
                "Actions/Windows/my-survival-action.instance");
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction temp = (ContextAwareAction) obj;
        Action a = temp.createContextAwareInstance(al);
        
        enable.put(SURVIVE_KEY, myAction);
        
        ic.add(enable);
        assertTrue("MyAction is enabled", a.isEnabled());
        ic.set(Collections.singletonList(disable), null);
        assertTrue("Remains enabled on other component", a.isEnabled());
        ic.remove(disable);
    }

    public void testSubclass() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionID;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\") " +
            "public class A {\n" +
            "  public A(Integer i) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("ActionListener")) {
            fail(os.toString());
        }
    }
    
    public void testNoConstructorIsFine() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\") " +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertTrue("Compilation has to succeed:\n" + os, r);
    }

    @ActionID(category="eager", id="direct.one")
    @ActionRegistration(displayName="Direct Action")
    public static class Direct extends AbstractAction implements Presenter.Menu {
        static int cnt;
        public Direct() {
            cnt++;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public JMenuItem getMenuPresenter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    @ActionID(category="eager", id="direct.two")
    @ActionRegistration(displayName="Direct Action")
    @ActionReference(path="Shortcuts", name="C-F2 D-A")
    public static class Direct2 extends AbstractAction implements Presenter.Toolbar {
        static int cnt;
        public Direct2() {
            cnt++;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public Component getToolbarPresenter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    @ActionID(category="eager", id="direct.three")
    @ActionRegistration(displayName="Direct Action")
    public static class Direct3 extends AbstractAction implements Presenter.Popup {
        static int cnt;
        public Direct3() {
            cnt++;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public JMenuItem getPopupPresenter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    @ActionID(category="eager", id="direct.four")
    @ActionRegistration(displayName="Direct Action")
    public static class Direct4 extends AbstractAction implements ContextAwareAction {
        static int cnt;
        public Direct4() {
            cnt++;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return this;
        }
    }
    @ActionID(category="eager", id="direct.five")
    @ActionRegistration(displayName="Direct Action")
    public static ContextAwareAction direct5() {return new Direct5();}
    private static class Direct5 extends AbstractAction implements ContextAwareAction {
        static int cnt;
        public Direct5() {
            cnt++;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return this;
        }
    }
    @ActionID(category="eager", id="direct.six")
    @ActionRegistration(displayName="Direct Action")
    public static class Direct6 extends AbstractAction implements DynamicMenuContent {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        @Override
        public JComponent[] getMenuPresenters() {
            return null;
        }
        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return null;
        }
    }
    
    @ActionID(category="menutext", id="namedaction")
    @ActionRegistration(displayName="This is an Action", menuText="This is a Menu Action", popupText="This is a Popup Action")
    public static class NamedAction extends AbstractAction {
        public NamedAction() { }
        @Override
        public void actionPerformed(ActionEvent e) { }
    }

    public void testPopupAndMenuText() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Actions/menutext/namedaction.instance");
        assertNotNull("Instance found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Action created", obj);
        
        Action a = (Action) obj;
        assertEquals("This is an Action", a.getValue(Action.NAME));
        JMenuItem item = new JMenuItem();
        Actions.connect(item, a, false);
        assertEquals("This is a Menu Action", item.getText());
        item = new JMenuItem();
        Actions.connect(item, a, true);
        assertEquals("This is a Popup Action", item.getText());
    }
    
    public void testDirectInstanceIfImplementsMenuPresenter() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Actions/eager/direct-one.instance");
        assertNotNull("Instance found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Action created", obj);
        assertEquals("Direct class is created", Direct.class, obj.getClass());
    }
    public void testDirectInstanceIfImplementsToolbarPresenter() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Actions/eager/direct-two.instance");
        assertNotNull("Instance found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Action created", obj);
        assertEquals("Direct class is created", Direct2.class, obj.getClass());
    }
    public void testDirectInstanceIfImplementsPopupPresenter() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Actions/eager/direct-three.instance");
        assertNotNull("Instance found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Action created", obj);
        assertEquals("Direct class is created", Direct3.class, obj.getClass());
    }
    public void testDirectInstanceIfImplementsContextAwareAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Actions/eager/direct-four.instance");
        assertNotNull("Instance found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Action created", obj);
        assertEquals("Direct class is created", Direct4.class, obj.getClass());
    }
    public void testDirectInstanceIfImplementsContextAwareActionByMethod() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Actions/eager/direct-five.instance");
        assertNotNull("Instance found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Action created", obj);
        assertEquals("Direct class is created", Direct5.class, obj.getClass());
    }
    public void testDirectInstanceIfImplementsDynamicMenuContent() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Actions/eager/direct-six.instance");
        assertNotNull("Instance found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Action created", obj);
        assertEquals("Direct class is created", Direct6.class, obj.getClass());
    }
    
    public void testNoKeyForDirects() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "public class A extends AbstractAction implements Presenter.Menu {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }
    
    public void testListWithNoType() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "public class A extends AbstractAction {\n" +
            "    public A(List wrongCnt) {}\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }
    
    public void testNoActionIDInReferences() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "@ActionReference(path=\"manka\", position=11, id=@ActionID(category=\"Cat\",id=\"x.y.z\"))" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testPackageInfoNeedsActionID() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.package-info", 
            "@ActionReferences({\n" +
            "  @ActionReference(path=\"manka\", position=11)\n" +
            "})\n" +
            "package test;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionReferences;\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testNoReferenceWithoutRegistration() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.*;\n" +
            "import org.openide.awt.ActionReferences;\n" +
            "import java.awt.event.*;\n" +
            "@ActionReference(path=\"manka\", position=11, id=@ActionID(category=\"Cat\",id=\"x.y.z\"))" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testNoReferencesWithoutRegistrationExceptOnPackage() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.*;\n" +
            "import org.openide.awt.ActionReferences;\n" +
            "import java.awt.event.*;\n" +
            "@ActionReferences({\n" +
            "  @ActionReference(path=\"manka\", position=11, id=@ActionID(category=\"Cat\",id=\"x.y.z\"))" +
            "})\n" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }
    
    
    public void testCheckSyntaxInShortcutsNoName() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.*;\n" +
            "import org.openide.awt.*;\n" +
            "import java.awt.event.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "@ActionReference(path=\"Shortcuts\")" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        assertTrue("Contains hint", os.toString().contains("Utilities.stringToKey"));
    }

    public void testCheckSyntaxInShortcuts() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import java.awt.event.*;\n" +
            "import org.openide.awt.*;\n" +
            "import java.awt.event.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "@ActionReference(path=\"Shortcuts\", name=\"silly\")" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        assertTrue("Contains hint", os.toString().contains("Utilities.stringToKey"));
    }
    
    public void testReferenceWithoutPosition() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Shortcuts/C-F2 D-A.shadow");
        assertNotNull(fo);
        assertEquals("Actions/eager/direct-two.instance", fo.getAttribute("originalFile"));
        assertEquals(null, fo.getAttribute("position"));
    }

    public void testSeparatorBeforeIsBefore() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "@ActionReference(path=\"manka\", position=11, separatorBefore=13)" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }
    
    public void testSeparatorAfterIsAfter() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\") " +
            "@ActionReference(path=\"manka\", position=11, separatorAfter=7)" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
    }

    public void testWrongPointerToIcon() throws IOException {
        clearWorkDir();
        // Cannot just check for e.g. SourceVersion.RELEASE_7 because we might be running JDK 6 javac w/ JDK 7 boot CP, and that is in JRE.
        // (Anyway libs.javacapi/external/javac-api-nb-7.0-b07.jar, in the test's normal boot CP, has this!)
        // Filter.class added in 7ae4016c5938, not long after f3323b1c65ee which we rely on for this to work.
        // Also cannot just check Class.forName(...) since tools.jar not in CP but ToolProvider loads it specially.
        if (new URLClassLoader(new URL[] {ToolProvider.getSystemJavaCompiler().getClass().getProtectionDomain().getCodeSource().getLocation()}).findResource("com/sun/tools/javac/util/Filter.class") == null) {
            System.err.println("#196933: testWrongPointerToIcon will only pass when using JDK 7 javac, skipping");
            return;
        }
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.awt.ActionRegistration;\n" +
            "import org.openide.awt.ActionReference;\n" +
            "import org.openide.awt.ActionID;\n" +
            "import org.openide.util.actions.Presenter;\n" +
            "import java.awt.event.*;\n" +
            "import java.util.List;\n" +
            "import javax.swing.*;\n" +
            "@ActionID(category=\"Tools\",id=\"my.action\")" +
            "@ActionRegistration(displayName=\"AAA\", key=\"K\", iconBase=\"does/not/exist.png\") " +
            "@ActionReference(path=\"manka\", position=11)" +
            "public class A implements ActionListener {\n" +
            "    public void actionPerformed(ActionEvent e) {}" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("does/not/exist.png")) {
            fail("Shall contain warning about does/not/exist.png resource:\n" + os);
        }
    }
    
}
