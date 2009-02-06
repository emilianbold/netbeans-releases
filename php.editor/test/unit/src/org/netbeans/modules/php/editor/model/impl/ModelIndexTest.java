/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import org.netbeans.modules.php.editor.index.*;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport;
import org.netbeans.modules.gsfret.source.usages.RepositoryUpdater;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.impl.ModelTestBase;
import org.netbeans.modules.php.editor.nav.TestUtilities;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Radek Matous
 */
public class ModelIndexTest extends ModelTestBase {

    public ModelIndexTest(String testName) {
        super(testName);
    }
    private static final String FOLDER = "GsfPlugins";

    @Override
    public void setUp() throws Exception {
        FileObject f = FileUtil.getConfigFile(FOLDER + "/text/html");

        if (f != null) {
            f.delete();
        }

        FileUtil.setMIMEType("php", PHPLanguage.PHP_MIME_TYPE);
        Logger.global.setFilter(new Filter() {

            public boolean isLoggable(LogRecord record) {
                Throwable t = record.getThrown();

                if (t == null) {
                    return true;
                }

                for (StackTraceElement e : t.getStackTrace()) {
                    if ("org.netbeans.modules.php.editor.index.GsfUtilities".equals(e.getClassName()) && "getBaseDocument".equals(e.getMethodName()) && t instanceof ClassNotFoundException) {
                        return false;
                    }
                }
                return false;
            }
        });
    }

    public void testModelFile1() throws Exception {
        TestModelTask task = new TestModelTask();
        task.addDeclaredMethodNamesForClass("clsModelTest1", "methodClsModelTest1");
        task.addDeclaredMethodNamesForClass("clsModelTest2", "methodClsModelTest2");
        task.addDeclaredMethodNamesForIface("ifaceModelTest1", "methodIfaceModelTest1");
        task.addDeclaredMethodNamesForIface("ifaceModelTest2", "methodIfaceModelTest2");

        task.addInheritedMethodNamesForClass("clsModelTest1", "methodIfaceModelTest1");
        task.addInheritedMethodNamesForClass("clsModelTest2", "methodClsModelTest1", "methodIfaceModelTest1", "methodIfaceModelTest2");
        task.addInheritedMethodNamesForIface("ifaceModelTest1");
        task.addInheritedMethodNamesForIface("ifaceModelTest2", "methodIfaceModelTest1");

        performModelTest(task, "testfiles/model/modelfile1.php");
    }

    static <T extends ModelElement> T getFirst(Collection<T> allElements,
            final String... elementName) {
        return ModelUtils.getFirst(allElements, elementName);
    }

    void performModelTest(AbstractTestModelTask task, String testFilePath) throws Exception {
        performTest(task, prepareTestFile(testFilePath));
    }

    private static <T extends ModelElement> void assertAnyElement(Collection<? extends ModelElement> allElements, String... elemNames) {
        assertNotNull(allElements);
        ModelElement anyElement = null;
        for (int i = 0; i < elemNames.length; i++) {
            ModelElement elem = getFirst(allElements, elemNames[i]);
            if (elem != null) {
                anyElement = elem;
                break;
            }
        }
        assertNotNull(anyElement);
    }

    private static <T extends ModelElement> void assertAllElements(Collection<? extends ModelElement> allElements, String... elemNames) {
        assertNotNull(allElements);
        for (int i = 0; i < elemNames.length; i++) {
            ModelElement elem = getFirst(allElements, elemNames[i]);
            assertNotNull(elemNames[i], elem);
        }
    }

    private static <T extends ModelElement> void assertAllMethods(TypeScope typeScope, String... elemNames) {
        assertAllElements(typeScope.getMethods(), elemNames);
    }

    private static <T extends ModelElement> void assertExactMethods(TypeScope typeScope, String... elemNames) {
        assertExactElements(typeScope.getMethods(), elemNames);
    }

    private static <T extends ModelElement> void assertExactDeclaredMethods(TypeScope typeScope, String... elemNames) {
        assertExactElements(typeScope.getDeclaredMethods(), elemNames);
    }

    private static <T extends ModelElement> void assertExactInheritedMethods(TypeScope typeScope, String... elemNames) {
        assertExactElements(typeScope.getInheritedMethods(), elemNames);
    }

    private static <T extends ModelElement> void assertExactElements(Collection<? extends ModelElement> allElements, String... elemNames) {
        assertNotNull(allElements);
        List<ModelElement> testedElems = new ArrayList<ModelElement>();
        for (int i = 0; i < elemNames.length; i++) {
            ModelElement elem = getFirst(allElements, elemNames[i]);
            assertNotNull(elemNames[i], elem);
            testedElems.add(elem);
        }
        List<ModelElement> notTestedElems = new ArrayList<ModelElement>(allElements);
        notTestedElems.removeAll(testedElems);
        assertTrue(notTestedElems.toString(), notTestedElems.isEmpty());
        assertEquals(allElements.size(), elemNames.length);
    }

    private static <T> void assertNotEmpty(final Collection<? extends T> collection) {
        assertNotNull(collection);
        assertFalse(collection.isEmpty());
    }

    protected static String computeFileName(int index) {
        return "test" + (index == (-1) ? "" : (char) ('a' + index)) + ".php";
    }

    protected void performTest(final CancellableTask<CompilationInfo> task, String... code) throws Exception {
        clearWorkDir();
        FileUtil.refreshAll();

        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject cache = workDir.createFolder("cache");
        FileObject folder = workDir.createFolder("src");
        FileObject cluster = workDir.createFolder("cluster");
        int index = -1;

        for (String c : code) {
            FileObject f = FileUtil.createData(folder, computeFileName(index));
            TestUtilities.copyStringToFile(f, c);
            index++;
        }

        System.setProperty("netbeans.user", FileUtil.toFile(cache).getAbsolutePath());
        PHPIndex.setClusterUrl(cluster.getURL().toExternalForm());
        CountDownLatch l = RepositoryUpdater.getDefault().scheduleCompilationAndWait(folder, folder);

        l.await();

        final FileObject test = folder.getFileObject("test.php");

        Document doc = openDocument(test);

        ClassPath empty = ClassPathSupport.createClassPath(new FileObject[0]);
        ClassPath source = ClassPathSupport.createClassPath(folder);
        ClasspathInfo info = ClasspathInfo.create(empty, empty, source);
        Source s = Source.create(info, test);

        s.runUserActionTask(new CancellableTask<CompilationController>() {

            public void cancel() {
            }

            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.UP_TO_DATE);

                task.run(parameter);
            }
        }, true);
    }

    private static Document openDocument(FileObject fileObject) throws Exception {
        DataObject dobj = DataObject.find(fileObject);

        EditorCookie ec = dobj.getCookie(EditorCookie.class);

        assertNotNull(ec);

        return ec.openDocument();
    }

    abstract class AbstractTestModelTask implements CancellableTask<CompilationInfo> {

        public void cancel() {
        }

        public void run(CompilationInfo parameter) throws Exception {
            Model model = ModelFactory.getModel(parameter);
            assertNotNull(model);
            FileScope fileScope = model.getFileScope();
            assertNotNull(fileScope);
            IndexScope indexScope = fileScope.getIndexScope();
            assertNotNull(indexScope);
            testIndexScope(model, fileScope, indexScope);
            testFileScope(model, fileScope, indexScope);
            for (ClassScope classScope : fileScope.getDeclaredClasses()) {
                testClassScope(model, fileScope, classScope, indexScope);
            }
            for (InterfaceScope ifaceScope : fileScope.getDeclaredInterfaces()) {
                testIfaceScope(model, fileScope, ifaceScope, indexScope);
            }
        }
        abstract void testIndexScope(Model model, FileScope fileScope, IndexScope indexScope);

        abstract void testFileScope(Model model, FileScope fileScope, IndexScope indexScope);

        abstract void testClassScope(Model model, FileScope fileScope, ClassScope classScope, IndexScope indexScope);

        abstract void testIfaceScope(Model model, FileScope fileScope, InterfaceScope classScope, IndexScope indexScope);
    }

    enum MemberType {

        DECLARED, INHERITED, ALL
    };

    private class TestModelTask extends AbstractTestModelTask {

        private Set<String> clsNames = new LinkedHashSet<String>();
        private Set<String> ifaceNames = new LinkedHashSet<String>();
        private Map<String, List<String>> declaredClsMethods = new LinkedHashMap<String, List<String>>();
        private Map<String, List<String>> declaredIfaceMethods = new LinkedHashMap<String, List<String>>();
        private Map<String, List<String>> inheritedClsMethods = new LinkedHashMap<String, List<String>>();
        private Map<String, List<String>> inheritedIfaceMethods = new LinkedHashMap<String, List<String>>();
        private Map<String, List<String>> allClsMethods = new LinkedHashMap<String, List<String>>();
        private Map<String, List<String>> allIfaceMethods = new LinkedHashMap<String, List<String>>();

        void addDeclaredMethodNamesForClass(String typeName, String... methodNames) {
            addMethodNames(typeName, true, MemberType.DECLARED, methodNames);
        }

        void addDeclaredMethodNamesForIface(String typeName, String... methodNames) {
            addMethodNames(typeName, false, MemberType.DECLARED, methodNames);
        }

        void addInheritedMethodNamesForClass(String typeName, String... methodNames) {
            addMethodNames(typeName, true, MemberType.INHERITED, methodNames);
        }

        void addInheritedMethodNamesForIface(String typeName, String... methodNames) {
            addMethodNames(typeName, false, MemberType.INHERITED, methodNames);
        }

        private void addMethodNames(String typeName, boolean isClass, MemberType memberType, String... methodNames) {
            Set<String> typeNames = isClass ? clsNames : ifaceNames;
            Map<String, List<String>> allMethods = isClass ? allClsMethods : allIfaceMethods;
            Map<String, List<String>> typeMethods = null;
            switch (memberType) {
                case ALL:
                    typeMethods = isClass ? allClsMethods : allIfaceMethods;
                    break;
                case DECLARED:
                    typeMethods = isClass ? declaredClsMethods : declaredIfaceMethods;
                    break;
                case INHERITED:
                    typeMethods = isClass ? inheritedClsMethods : inheritedIfaceMethods;
                    break;
                default:
                    fail();
            }
            List<String> methNameList = typeMethods.get(typeName);
            if (methNameList == null) {
                methNameList = new ArrayList<String>();
                typeMethods.put(typeName, methNameList);
            }

            typeNames.add(typeName);
            methNameList.addAll(Arrays.asList(methodNames));

            List<String> allMethNameList = allMethods.get(typeName);
            if (allMethNameList == null) {
                allMethNameList = new ArrayList<String>();
                allMethods.put(typeName, allMethNameList);
            }

            allMethNameList.addAll(Arrays.asList(methodNames));

        }

        @Override
        void testIndexScope(Model model, FileScope fScope, IndexScope iScope) {
            Collection<? extends ClassScope> declaredClasses = fScope.getDeclaredClasses();
            for (ClassScope classScope : declaredClasses) {
                assertNotNull(classScope.getName(), getFirst(iScope.findClasses(classScope.getName())));
            }
        }

        @Override
        void testFileScope(Model model, FileScope fScope, IndexScope iScope) {
            assertExactElements(fScope.getDeclaredClasses(), getClsNames());
            assertExactElements(fScope.getDeclaredInterfaces(), getIfaceNames());
        }

        @Override
        void testClassScope(Model model, FileScope fileScope, ClassScope clsScope, IndexScope indexScope) {
            assertDeclaredMethodNames(clsScope, declaredClsMethods);
            assertInheritedMethodNames(clsScope, inheritedClsMethods);
            assertMethodNames(clsScope, allClsMethods);
        }

        @Override
        void testIfaceScope(Model model, FileScope fileScope, InterfaceScope ifaceScope, IndexScope indexScope) {
            assertDeclaredMethodNames(ifaceScope, declaredIfaceMethods);
            assertInheritedMethodNames(ifaceScope, inheritedIfaceMethods);
            assertMethodNames(ifaceScope, allIfaceMethods);
        }

        private void assertDeclaredMethodNames(TypeScope typeScope, Map<String, List<String>> methods) {
            String[] typeNames = methods.keySet().toArray(new String[methods.size()]);
            assertAnyElement(Collections.singletonList(typeScope), typeNames);
            String name = typeScope.getName();
            for (int i = 0; i < typeNames.length; i++) {
                String iName = typeNames[i];
                if (name.equals(iName)) {
                    List<String> names = methods.get(iName);

                    assertExactDeclaredMethods(typeScope, names.toArray(new String[names.size()]));
                }
            }
        }

        private void assertInheritedMethodNames(TypeScope typeScope, Map<String, List<String>> methods) {
            String[] typeNames = methods.keySet().toArray(new String[methods.size()]);
            assertAnyElement(Collections.singletonList(typeScope), typeNames);
            String name = typeScope.getName();
            for (int i = 0; i < typeNames.length; i++) {
                String iName = typeNames[i];
                if (name.equals(iName)) {
                    List<String> names = methods.get(iName);
                    assertExactInheritedMethods(typeScope, names.toArray(new String[names.size()]));
                }
            }
        }

        private void assertMethodNames(TypeScope typeScope, Map<String, List<String>> methods) {
            String[] typeNames = methods.keySet().toArray(new String[methods.size()]);
            assertAnyElement(Collections.singletonList(typeScope), typeNames);
            String name = typeScope.getName();
            for (int i = 0; i < typeNames.length; i++) {
                String iName = typeNames[i];
                if (name.equals(iName)) {
                    List<String> names = methods.get(iName);
                    assertExactMethods(typeScope, names.toArray(new String[names.size()]));
                }
            }
        }

        private String[] getClsNames() {
            return clsNames.toArray(new String[clsNames.size()]);
        }

        private String[] getIfaceNames() {
            return ifaceNames.toArray(new String[ifaceNames.size()]);
        }
    }
}
