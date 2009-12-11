/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.beans.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;


/**
 * @author ads
 * 
 * Current = Default ( Current is name of Default from old specification ).
 *
 */
public class CurrentTest extends CommonTestCase {

    public CurrentTest( String testName ) {
        super(testName);
    }
    
    public void testA(){
        
    }
    
    public void testDefault() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class SuperClass  { " +
                " @Produces String productionField = \"\"; "+
                " @Produces @Default int[] productionMethod() { return null; } "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Default " +
                "public class One extends SuperClass {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Default SuperClass myField1; "+
                " @Inject @Default String myField2; "+
                " @Inject @Default int[] myField3; "+
                " @Inject @Default One myField4; "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "public class Two extends SuperClass {}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( (VariableElement)element);
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    if ( element.getSimpleName().contentEquals("myField1")){
                        check1( element , provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        check2( element , provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField3")){
                        check3( element , provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField4")){
                        check4( element , provider);
                    }
                }
                assert names.contains("myField1");
                assert names.contains("myField2");
                assert names.contains("myField3");
                assert names.contains("myField4");
                return null;
            }
        });
    }

    public void testMixedDefault() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {" +
                "    String value(); "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1(\"a\") "+
                "public class SuperClass  { " +
                " @Produces @Default @Binding1(\"b\") int[] productionMethod() { return null; } "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding2 " +
                "public class One extends SuperClass {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Default One myField1; "+
                " @Inject @Default  int[] myField2; "+
                " @Inject @Default  SuperClass myField3; "+
                " @Inject @Default  @Binding2 SuperClass myField4; "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "public class Two extends SuperClass {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1(\"a\") @Default "+
                "public class Three extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Four.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding2 @Default "+
                "public class Four extends Two {}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( (VariableElement)element);
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    if ( element.getSimpleName().contentEquals("myField1")){
                        checkMixed1( element , provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        checkMixed2( element , provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField3")){
                        checkMixed3( element , provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField4")){
                        checkMixed4( element , provider);
                    }
                }
                assert names.contains("myField1");
                assert names.contains("myField2");
                assert names.contains("myField3");
                assert names.contains("myField4");
                return null;
            }
        });
    }
    
    public void testSpecializeInheritanceDefault() throws IOException, InterruptedException{
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding2.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "import javax.inject.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "@Inherited "+
                "public @interface Binding2  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Default Two myField1; "+
                " @Inject @Default One1 myField2; "+
                " @Inject @Default Two2 myField3; "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "@Specializes "+
                "public class Two extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "public class One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Specializes "+
                "public class Two1 extends One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One2.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding2 "+
                "public class One2 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two2.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class Two2 extends One2 {}" );
        
        inform("start @Default inheritance and specializtion tests" );
        
        
        TestWebBeansModelImpl modelImpl = createModelImpl();
        final TestWebBeansModelProviderImpl provider = modelImpl.getProvider();
        MetadataModel<WebBeansModel> testModel = modelImpl.createTestModel();
        
        testModel.runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.TestClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                List<VariableElement> injectionPoints = 
                    new ArrayList<VariableElement>( children.size());
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        injectionPoints.add( (VariableElement)element);
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    names.add( element.getSimpleName().toString() );
                    if ( element.getSimpleName().contentEquals("myField1")){
                        checkDefault1( element , provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        checkDefault2( element , provider);
                    }
                    else if ( element.getSimpleName().contentEquals("myField3")){
                        checkDefault3( element , provider);
                    }
                }
                assert names.contains("myField1");
                assert names.contains("myField2");
                assert names.contains("myField3");
                return null;
            }
        });
    }
    
    protected void check1( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("test myField1");

        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(3, typeElements.size());
        assertEquals(0, productions.size());

        boolean superFound = false;
        boolean oneFound = false;
        boolean twoFound = false;
        for (TypeElement injectable : typeElements) {
            if (injectable.getQualifiedName().contentEquals("foo.SuperClass")) {
                superFound = true;
            }
            else if (injectable.getQualifiedName().contentEquals("foo.One")) {
                oneFound = true;
            }
            if (injectable.getQualifiedName().contentEquals("foo.Two")) {
                twoFound = true;
            }
        }
        assertTrue(superFound);
        assertTrue(oneFound);
        assertTrue(twoFound);
    }
    
    protected void check2( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("test myField2");

        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(1, productions.size());

        Element injactable = productions.iterator().next();
        assertNotNull(injactable);

        assertTrue("Expect production field , but found : "
                + injactable.getKind(), injactable instanceof VariableElement);

        assertEquals("productionField", injactable.getSimpleName().toString());
    }
    
    protected void check3( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("test myField3");

        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(1, productions.size());

        Element injactable = productions.iterator().next();
        assertNotNull(injactable);

        assertTrue("Expect production method , but found : "
                + injactable.getKind(), injactable instanceof ExecutableElement);

        assertEquals("productionMethod", injactable.getSimpleName().toString());
    }
    
    protected void check4( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("test myField4");

        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(1, typeElements.size());
        assertEquals(0, productions.size());

        TypeElement injactable = typeElements.iterator().next();

        assertNotNull(injactable);
        
        assertEquals("foo.One", injactable.getQualifiedName().toString());
    }
    
    protected void checkMixed1( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("mixed test myField1");

        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(1, typeElements.size());
        assertEquals(0, productions.size());

        TypeElement injactable = typeElements.iterator().next();

        assertNotNull(injactable);
        assertEquals("foo.Three", ((TypeElement) injactable).getQualifiedName()
                .toString());
    }
    
    protected void checkMixed2( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("mixed test myField2");

        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(1, productions.size());

        Element injactable = productions.iterator().next();

        assertNotNull(injactable);
        assertTrue("Expect production method , but found : "
                + injactable.getKind(), injactable instanceof ExecutableElement);

        assertEquals("productionMethod", injactable.getSimpleName().toString());
    }
    
    protected void checkMixed3( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("mixed test myField3");

        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(3, typeElements.size());
        assertEquals(0, productions.size());

        boolean twoFound = false;
        boolean threeFound = false;
        boolean fourFound = false;
        for (TypeElement injectable : typeElements) {
            if (injectable.getQualifiedName().contentEquals("foo.Two")) {
                twoFound = true;
            }
            else if (injectable.getQualifiedName().contentEquals("foo.Three"))
            {
                threeFound = true;
            }
            if (injectable.getQualifiedName().contentEquals("foo.Four")) {
                fourFound = true;
            }
        }
        assertTrue(twoFound);
        assertTrue(threeFound);
        assertTrue(fourFound);
    }
    
    protected void checkMixed4( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("mixed test myField4");
        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(1, typeElements.size());
        assertEquals(0, productions.size());

        TypeElement injactable = typeElements.iterator().next();

        assertNotNull(injactable);

        assertEquals("foo.Four", ((TypeElement) injactable).getQualifiedName()
                .toString());
    }
    
    protected void checkDefault1( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("test current absence for myField1");
        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(0, productions.size());
    }
    
    protected void checkDefault2( VariableElement element, 
            TestWebBeansModelProviderImpl provider)
    {
        inform("test current presence for myField2");

        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(1, typeElements.size());
        assertEquals(0, productions.size());

        TypeElement injactable = typeElements.iterator().next();
        assertNotNull(injactable);

        assertEquals("foo.Two1", injactable.getQualifiedName().toString());
    }
    
    protected void checkDefault3( VariableElement element,
            TestWebBeansModelProviderImpl provider) 
    {
        inform("test current absence for myField3");

        Result result = provider.findVariableInjectable(element, null);

        assertNotNull(result);
        assertTrue(result instanceof ResultImpl);
        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(0, productions.size());
    }

}
