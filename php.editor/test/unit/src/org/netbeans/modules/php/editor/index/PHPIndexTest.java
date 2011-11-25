/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.NameKind.Exact;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.nav.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Radek Matous
 */
public class PHPIndexTest extends TestBase {

    private ElementQuery.Index index;

    public PHPIndexTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        QuerySupport querySupport = QuerySupportFactory.get(Arrays.asList(createSourceClassPathsForTest()));
        index = ElementQueryFactory.createIndexQuery(querySupport);
    }

    /**
     * Test of getClasses method, of class PHPIndex.
     */
    @Test
    public void testGetClasses() throws Exception {
        checkIndexer(getTestPath());
    }

    /**
     * Test of getClasses method, of class PHPIndex.
     */
    @Test
    public void testGetClasses_all() throws Exception {
        Collection<String> classNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getClasses(NameKind.empty()));
        assertEquals(classNames.size(), allTypes.size());
        for (TypeElement indexedClass : allTypes) {
            assertTrue(classNames.contains(indexedClass.getName()));
            assertEquals(indexedClass, indexedClass);
            assertEquals(PhpElementKind.CLASS, indexedClass.getPhpElementKind());
        }
    }

    /**
     * Test of getClasses method, of class PHPIndex.
     */
    @Test
    public void testGetClasses_exact() throws Exception {
        Collection<String> classNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getClasses(NameKind.empty()));
        assertEquals(classNames.size(), allTypes.size());
        for (String clsName : classNames) {
            Collection<ClassElement> classes = index.getClasses(NameKind.exact(clsName));
            assertTrue(classes.size() > 0);
            for (ClassElement indexedClass : classes) {
                assertEquals(clsName, indexedClass.getName());
                assertTrue(classNames.contains(indexedClass.getName()));
                assertTrue(allTypes.contains(indexedClass));
            }
        }
    }

    /**
     * Test of getClasses method, of class PHPIndex.
     */
    @Test
    public void testGetClasses_prefix() throws Exception {
        Collection<String> classNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getClasses(NameKind.empty()));
        assertEquals(classNames.size(), allTypes.size());
        for (String clsName : classNames) {
            Collection<ClassElement> classes = index.getClasses(NameKind.prefix(clsName.substring(0, 1)));
            assertTrue(classes.size() > 0);
            for (ClassElement indexedClass : classes) {
                assertEquals(clsName, indexedClass.getName());
                assertTrue(classNames.contains(indexedClass.getName()));
                assertTrue(allTypes.contains(indexedClass));
            }
        }
    }

    /**
     * Test of getClasses method, of class PHPIndex.
     */
    @Test
    public void testGetClasses_preferred() throws Exception {
        Collection<TypeElement> ccClasses = new ArrayList<TypeElement>(index.getClasses(NameKind.exact("CCC")));
        assertEquals(2, ccClasses.size());
        TypeElement[] classesArray = ccClasses.toArray(new TypeElement[ccClasses.size()]);
        final TypeElement firstCC = classesArray[0];
        final TypeElement secondCC = classesArray[1];
        assertNotNull(firstCC);
        assertNotNull(secondCC);
        assertNotSame(secondCC, firstCC);
        assertNotNull(firstCC.getFileObject());
        assertNotNull(secondCC.getFileObject());
        assertNotSame(secondCC.getFileObject(), firstCC.getFileObject());
        final Collection<ClassElement> preferredClasses =
                ElementFilter.forFiles(firstCC.getFileObject()).prefer(index.getClasses(NameKind.exact("CCC")));
        assertEquals(1, preferredClasses.size());
        final ClassElement preffered = getFirst(preferredClasses);
        assertEquals(firstCC, preffered);
        assertEquals(firstCC.getFileObject(), preffered.getFileObject());

        final Collection<ClassElement> aaClasses =
                ElementFilter.forFiles(preffered.getFileObject()).prefer(index.getClasses(NameKind.exact("AAA")));
        assertEquals(1, aaClasses.size());
        assertNotSame(getFirst(aaClasses).getFileObject(), preffered.getFileObject());
    }

    private static <T extends PhpElement> T getFirst(Collection<T> classes) {
        final Iterator<T> iterator = classes.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    private static <T extends PhpElement> T getSecond(Collection<T> classes) {
        final Iterator<T> iterator = classes.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            return iterator.hasNext() ? iterator.next() : null;
        }
        return null;
    }

    /**
     * Test of getInterfaces method, of class PHPIndex.
     */
    @Test
    public void testGetInterfaces() throws Exception {
        checkIndexer(getTestPath());
    }

    /**
     * Test of getInterfaces method, of class PHPIndex.
     */
    @Test
    public void testGetInterfaces_all() throws Exception {
        Collection<String> typeNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getInterfaces(NameKind.empty()));
        assertEquals(typeNames.size(), allTypes.size());
        for (TypeElement indexedIface : allTypes) {
            assertTrue(typeNames.contains(indexedIface.getName()));
            assertEquals(indexedIface, indexedIface);
            assertEquals(PhpElementKind.IFACE, indexedIface.getPhpElementKind());
        }
    }

    /**
     * Test of getInterfaces method, of class PHPIndex.
     */
    @Test
    public void testGetInterfaces_exact() throws Exception {
        Collection<String> typeNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getInterfaces(NameKind.empty()));
        assertEquals(typeNames.size(), allTypes.size());
        for (String clsName : typeNames) {
            Collection<InterfaceElement> ifaces = index.getInterfaces(NameKind.exact(clsName));
            assertTrue(ifaces.size() > 0);
            for (InterfaceElement indexedIface : ifaces) {
                assertEquals(clsName, indexedIface.getName());
                assertTrue(typeNames.contains(indexedIface.getName()));
                assertTrue(allTypes.contains(indexedIface));
            }
        }
    }

    /**
     * Test of getInterfaces method, of class PHPIndex.
     */
    @Test
    public void testGetInterfaces_prefix() throws Exception {
        Collection<String> classNames = Arrays.asList(new String[]{"AAA", "BBB", "CCC", "CCC", "DDD"});
        Collection<TypeElement> allTypes = new ArrayList<TypeElement>(index.getInterfaces(NameKind.empty()));
        assertEquals(classNames.size(), allTypes.size());
        for (String typeName : classNames) {
            Collection<InterfaceElement> classes = index.getInterfaces(NameKind.prefix(typeName.substring(0, 1)));
            assertTrue(classes.size() > 0);
            for (InterfaceElement indexedInterfaces : classes) {
                assertEquals(typeName, indexedInterfaces.getName());
                assertTrue(classNames.contains(indexedInterfaces.getName()));
                assertTrue(allTypes.contains(indexedInterfaces));
            }
        }
    }

    /**
     * Test of getInterfaces method, of class PHPIndex.
     */
    @Test
    public void testGetInterfaces_preferred() throws Exception {
        Collection<TypeElement> ccInterfaces = new ArrayList<TypeElement>(index.getInterfaces(NameKind.exact("CCC")));
        assertEquals(2, ccInterfaces.size());
        TypeElement[] interfacesArray = ccInterfaces.toArray(new TypeElement[ccInterfaces.size()]);
        TypeElement firstCC = interfacesArray[0];
        TypeElement secondCC = interfacesArray[1];
        assertNotNull(firstCC);
        assertNotNull(secondCC);
        assertNotSame(secondCC, firstCC);
        assertNotNull(firstCC.getFileObject());
        assertNotNull(secondCC.getFileObject());
        assertNotSame(secondCC.getFileObject(), firstCC.getFileObject());

        if (firstCC.getFileObject().getName().endsWith("_1")) {
            final TypeElement tmpCC = firstCC;
            firstCC = secondCC;
            secondCC = tmpCC;
        }

        final Collection<InterfaceElement> preferredInterfaces =
                ElementFilter.forFiles(firstCC.getFileObject()).prefer(index.getInterfaces(NameKind.exact("CCC")));
        assertEquals(1, preferredInterfaces.size());
        final InterfaceElement preffered = getFirst(preferredInterfaces);
        assertEquals(firstCC, preffered);
        assertEquals(firstCC.getFileObject(), preffered.getFileObject());

        final Collection<InterfaceElement> aaInterfaces =
                ElementFilter.forFiles(preffered.getFileObject()).prefer(index.getInterfaces(NameKind.exact("AAA")));
        assertEquals(1, aaInterfaces.size());
        assertSame(getFirst(aaInterfaces).getFileObject(), preffered.getFileObject());
    }

    /**
     * Test of getFunctions method, of class PHPIndex.
     */
    @Test
    public void testGetFunctions() throws Exception {
        checkIndexer(getTestPath());
    }

    public void testGetFunctions_parameters() throws Exception {
        final Exact fncName = NameKind.exact("af");
        Set<FunctionElement> functions = index.getFunctions(fncName);
        FunctionElement fncA = getFirst(functions);
        assertNotNull(fncA);
        assertEquals(fncName.getQueryName(), fncA.getName());

        List<ParameterElement> parameters = fncA.getParameters();
        assertEquals(parameters.size(), 4);

        final ParameterElement firstParam = parameters.get(1);
        assertTrue(firstParam.isMandatory());
        assertTrue(firstParam.hasDeclaredType());
        assertEquals(1, firstParam.getTypes().size());
        TypeResolver firstType = firstParam.getTypes().iterator().next();
        assertTrue(firstType.isResolved());
        assertTrue(firstType.canBeResolved());
        assertEquals("ParameterIface", firstType.getRawTypeName());

        final ParameterElement secondParam = parameters.get(0);
        assertTrue(secondParam.isMandatory());
        assertTrue(secondParam.hasDeclaredType());
        assertEquals(1, secondParam.getTypes().size());
        TypeResolver secondType = secondParam.getTypes().iterator().next();
        assertTrue(secondType.isResolved());
        assertTrue(secondType.canBeResolved());
        assertEquals("ParameterClass", secondType.getRawTypeName());

        final ParameterElement thirdParam = parameters.get(2);
        assertFalse(thirdParam.isMandatory());
        assertEquals("\"test\"", thirdParam.getDefaultValue());
        assertFalse(thirdParam.hasDeclaredType());
        assertEquals(0, thirdParam.getTypes().size());

        final ParameterElement fourthParam = parameters.get(3);
        assertFalse(fourthParam.isMandatory());
        assertEquals("MY_CONST", fourthParam.getDefaultValue());
        assertFalse(fourthParam.hasDeclaredType());
        assertEquals(0, thirdParam.getTypes().size());
    }

    /**
     * Test of getFunctions method, of class PHPIndex.
     */
    @Test
    public void testGetFunctions_all() throws Exception {
        Collection<String> fncNames = Arrays.asList(new String[]{"af", "bf", "cf", "cf", "df"});
        Collection<FunctionElement> allFunctions = index.getFunctions(NameKind.empty());
        assertEquals(fncNames.size(), allFunctions.size());
        for (FunctionElement indexedFunction : allFunctions) {
            assertTrue(fncNames.contains(indexedFunction.getName()));
            assertEquals(indexedFunction, indexedFunction);
            assertEquals(PhpElementKind.FUNCTION, indexedFunction.getPhpElementKind());
        }
    }

    /**
     * Test of getFunctions method, of class PHPIndex.
     */
    @Test
    public void testGetFunctions_exact() throws Exception {
        Collection<String> fncNames = Arrays.asList(new String[]{"af", "bf", "cf", "cf", "df"});
        Collection<FunctionElement> allFunctions = index.getFunctions(NameKind.empty());
        assertEquals(fncNames.size(), allFunctions.size());
        for (String fnName : fncNames) {
            Collection<FunctionElement> functions = index.getFunctions(NameKind.exact(fnName));
            assertTrue(functions.size() > 0);
            for (FunctionElement indexedFnc : functions) {
                assertEquals(fnName, indexedFnc.getName());
                assertTrue(fncNames.contains(indexedFnc.getName()));
                assertTrue(allFunctions.contains(indexedFnc));
            }
        }
    }

    /**
     * Test of getFunctionsmethod, of class PHPIndex.
     */
    @Test
    public void testGetFunctions_prefix() throws Exception {
        Collection<String> fncNames = Arrays.asList(new String[]{"af", "bf", "cf", "cf", "df"});
        Collection<FunctionElement> allFunctions = index.getFunctions(NameKind.empty());
        assertEquals(fncNames.size(), allFunctions.size());
        for (String fncName : fncNames) {
            Collection<FunctionElement> functions = index.getFunctions(NameKind.prefix(fncName.substring(0, 1)));
            assertTrue(functions.size() > 0);
            for (FunctionElement indexedFunction : functions) {
                assertEquals(fncName, indexedFunction.getName());
                assertTrue(fncNames.contains(indexedFunction.getName()));
                assertTrue(allFunctions.contains(indexedFunction));
            }
        }
    }

    @Test
    public void testGetMethods() throws Exception {
        checkIndexer(getTestPath());
    }

    @Test
    public void testGetMethods_NameKind_empty() {
        Collection<String> methodNames = Arrays.asList(
                new String[]{"testMethodDeclaration",
                    "testMethodDeclarationIface1",
                    "testMethodDeclarationIface2",
                    "testMethodDeclarationNext1",
                    "testMethodDeclarationNext2"});
        Collection<MethodElement> allMethods = index.getMethods(NameKind.empty());
        assertTrue(allMethods.size() >= methodNames.size());
        for (String methName : methodNames) {
            Set<MethodElement> methods = index.getMethods(NameKind.exact(methName));
            assertTrue(methods.size() > 0);
            for (MethodElement indexedMethod : methods) {
                assertEquals(methName, indexedMethod.getName());
                assertTrue(methodNames.contains(indexedMethod.getName()));
                assertTrue(allMethods.contains(indexedMethod));
            }
        }
    }

    @Test
    public void testGetMethods_TypeElement() {
        Set<ClassElement> classes = index.getClasses(NameKind.exact("testMethodDeclaration"));
        assertEquals(1, classes.size());
        ClassElement clz = getFirst(classes);
        Set<MethodElement> methods = index.getDeclaredMethods(clz);
        assertEquals(1, methods.size());
        methods = index.getInheritedMethods(clz);
        assertEquals(0, methods.size());

        classes = index.getClasses(NameKind.exact("testMethodDeclaration_1"));
        assertEquals(1, classes.size());
        clz = getFirst(classes);
        LinkedHashSet<TypeElement> inheritedTypes = index.getInheritedTypes(clz);
        assertEquals(2, inheritedTypes.size());

        methods = index.getDeclaredMethods(clz);
        assertEquals(3, methods.size());
        methods = index.getInheritedMethods(clz);
        assertEquals(3, methods.size());
        MethodElement firstMethod = getFirst(methods);
        TypeElement firstType = firstMethod.getType();
        final boolean isFirstTypeClass = firstType.getPhpElementKind().equals(PhpElementKind.CLASS);
        if (isFirstTypeClass) {
            assertEquals(firstType.getName(), clz.getSuperClassName().getName());
            assertEquals(getSecond(methods).getType().getName(), clz.getSuperInterfaces().iterator().next().getName());
            assertEquals(firstMethod.getName(), "testMethodDeclaration");
        } else {
            assertEquals(firstType.getName(), clz.getSuperInterfaces().iterator().next().getName());
            assertEquals(getSecond(methods).getType().getName(), clz.getSuperClassName().getName());
            assertTrue(firstMethod.getName().startsWith("testMethodDeclarationIface"));
        }
        Collection<String> methodNames = Arrays.asList(
                new String[]{"testMethodDeclaration",
                    "testMethodDeclarationIface1",
                    "testMethodDeclarationIface2",
                    "testMethodDeclarationNext1",
                    "testMethodDeclarationNext2"});
        Collection<MethodElement> allMethods = index.getAccessibleMethods(clz, clz);
        assertTrue(allMethods.size() >= methodNames.size());
        for (String methName : methodNames) {
            Set<MethodElement> meths = index.getMethods(NameKind.exact(methName));
            assertTrue(meths.size() > 0);
            for (MethodElement indexedMethod : meths) {
                assertEquals(methName, indexedMethod.getName());
                assertTrue(methodNames.contains(indexedMethod.getName()));
                if (indexedMethod.getType().equals(clz)) {
                    assertTrue(allMethods.contains(indexedMethod));
                }
            }
        }
    }

    @Test
    public void testGetNamespaces() throws Exception {
        checkIndexer(getTestPath());
    }

    /*@Test
    public void testGetNamespaces_NameKind() throws Exception {
        Collection<String> nsNames = Arrays.asList(new String[]{
            "\\my\\name",
            "\\your\\name",
            "\\their\\name",
            "\\our\\name",
            "\\my\\surname",
            "\\your\\surname",
            "\\their\\surname",
            "\\our\\surname"
        });
        Set<NamespaceElement> allNamespaces = index.getNamespaces(NameKind.empty());
        assertEquals(nsNames.size(), allNamespaces.size());
        for (NamespaceElement ns : allNamespaces) {
            Set<NamespaceElement> namespaces = index.getNamespaces(NameKind.exact(ns.getFullyQualifiedName()));
            assertEquals(1, namespaces.size());
            NamespaceElement firstNs = getFirst(namespaces);
            assertEquals(firstNs.getFullyQualifiedName(), ns.getFullyQualifiedName());
            assertTrue(nsNames.contains(firstNs.getFullyQualifiedName().toString()));
        }

        assertEquals(1, index.getNamespaces(NameKind.prefix(QualifiedName.create("\\my\\na"))).size());
        assertEquals(2, index.getNamespaces(NameKind.prefix(QualifiedName.create("\\my\\"))).size());
        assertEquals(1, index.getNamespaces(NameKind.prefix(QualifiedName.createFullyQualified("n", "my"))).size());
        assertEquals(2, index.getNamespaces(NameKind.prefix(QualifiedName.createFullyQualified("", "my"))).size());
        assertEquals(4, index.getNamespaces(NameKind.prefix(QualifiedName.create("nam"))).size());
        assertEquals(4, index.getNamespaces(NameKind.prefix(QualifiedName.create("sur"))).size());

    }*/

    /**
     * Test of getConstants method, of class PHPIndex.
     */
    @Test
    public void testGetConstants() {
    }


    /**
     * Test of getTopLevelVariables method, of class PHPIndex.
     */
    @Test
    public void testGetTopLevelVariables() {
    }

    /**
     * Test of getAllTopLevel method, of class PHPIndex.
     */
    @Test
    public void testGetAllTopLevel_ElementQuery() {
    }

    /**
     * Test of getAllTopLevel method, of class PHPIndex.
     */
    @Test
    public void testGetAllTopLevel_ElementQuery_EnumSet() {
    }

    /**
     * Test of getClassAncestors method, of class PHPIndex.
     */
    @Test
    public void testGetClassAncestors() {
    }

    /**
     * Test of getFiles method, of class PHPIndex.
     */
    @Test
    public void testGetFiles() {
    }

    /**
     * Test of getDirectIncludes method, of class PHPIndex.
     */
    @Test
    public void testGetDirectIncludes() {
    }

    @Override
    protected FileObject[] createSourceClassPathsForTest() {
        final File folder = new File(getDataDir(), getTestFolderPath());
        return new FileObject[]{FileUtil.toFileObject(folder)};
    }

    private String getTestFolderPath() {
        return "testfiles/index/" + getTestName();//NOI18N
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getTestName() + ".php";//NOI18N
    }

    private String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }
}
