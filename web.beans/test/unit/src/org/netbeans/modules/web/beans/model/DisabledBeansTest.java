/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class DisabledBeansTest extends CommonTestCase {

    public DisabledBeansTest( String testName ) {
        super(testName);
    }
    
    public void testSingeAlternative() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "beans.xml", 
                "<?xml  version='1.0' encoding='UTF-8'?> " +
                "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\">" +
                "<alternatives>" +
                    "<class>foo.One</class> "+
                    "<class>foo.One1</class> "+
                    "<stereotype>foo.Stereotype1</stereotype> "+
                "</alternatives> " +
                "</beans>");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {}");
        
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "@Alternative "+
                "public class One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@Binding1 "+
                "public class Two extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Alternative "+
                "@Binding1 "+
                "public class One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two1.java",
                "package foo; " +
                "@Binding1 "+
                "public class Two1 extends One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                " @Stereotype1 "+
                "public class Three extends One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Stereotype1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import java.lang.annotation.*; "+
                "@Target({METHOD, FIELD, TYPE}) "+  
                "@Retention(RUNTIME) "+
                "@Alternative "+
                "@Stereotype "+
                "public @interface Stereotype1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Binding1 One myField1; "+
                " @Inject @Binding1 One1 myField2; "+
                "}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
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
                        checkAlternative1( element , model );
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        checkAlternative2( element , model);
                    }
                }
                
                assert names.contains("myField1");
                assert names.contains("myField2");
                return null;
            }
        });
    }
    
    public void testSpecializes() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {}");
        
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "public class One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Specializes "+
                "public class Two extends One {}" );
        
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Specializes "+
                "public class Three extends Two {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "public class One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Alternative "+
                "@Specializes "+
                "public class Two1 extends One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One2.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "public class One2 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two2.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Specializes "+
                "@Alternative "+
                "public class Two2 extends One2 {}" );
        
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three2.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Specializes "+
                "public class Three2 extends Two2 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Binding1 One myField1; "+
                " @Inject @Binding1 One1 myField2; "+
                " @Inject @Binding1 One2 myField3; "+
                "}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
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
                        checkSpecializes1( element , model );
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        checkSpecializes2( element , model);
                    }
                    else if ( element.getSimpleName().contentEquals("myField3")){
                        checkSpecializes3( element , model);
                    }
                }
                
                assert names.contains("myField1");
                assert names.contains("myField2");
                assert names.contains("myField3");
                return null;
            }
        });
    }
    
    public void testVariousDisableConditions() throws IOException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "import java.lang.annotation.*; "+
                "@Qualifier " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding1  {}");
        
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Alternative "+
                "public class One {" +
                " @Produces @Binding1 int myField; "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "public final class Two implements Iface {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "public interface Iface {}" );
        
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Binding1 "+
                "public class Three extends Two {" +
                " private Three(){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject @Binding1 int myField1; "+
                " @Inject @Binding1 Iface myField2; "+
                "}" );
        
        TestWebBeansModelImpl modelImpl = createModelImpl(true );
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
                        checkVarious1( element , model );
                    }
                    else if ( element.getSimpleName().contentEquals("myField2")){
                        checkVarious2( element , model);
                    }
                }
                
                assert names.contains("myField1");
                assert names.contains("myField2");
                return null;
            }
        });
    }
    
    /*
     * myField is disabled because it is inside disabled alternative bean. 
     */
    private void checkVarious1( VariableElement element, WebBeansModel model )
    {
        Result result = model.getInjectable(element, null);
        
        assertNotNull( result );
        
        assertEquals( Result.ResultKind.RESOLUTION_ERROR, result.getKind());
        
        assertTrue( result instanceof Result.ApplicableResult );
        assertTrue( result instanceof Result.ResolutionResult );  
        
        Set<Element> productions = ((Result.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((Result.ApplicableResult)result).getTypeElements();
        
        assertEquals( 1 , productions.size());
        assertEquals( 0 , typeElements.size());
        
        Element production = productions.iterator().next();
        
        assertTrue( production instanceof VariableElement );
        
        assertEquals("myField", production.getSimpleName().toString());
        
        assertFalse ( "production field myField is not an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( production ));
        
        assertTrue( "production field myField should be disabled", 
                ((Result.ApplicableResult)result).isDisabled(production));
    }
    
    /*
     * All three types are disabled here.
     * - Iface is not a bean . It is interface . So it can't be available as 
     * result of typesafe resolution
     * - Two is final. So it is unproxyable.
     * - Three has private CTOR . It is also unrpoxyable.
     */
    private void checkVarious2( VariableElement element, WebBeansModel model )
    {
        Result result = model.getInjectable(element, null);
        
        assertNotNull( result );
        
        assertEquals( Result.ResultKind.RESOLUTION_ERROR, result.getKind());
        
        assertTrue( result instanceof Result.ApplicableResult );
        assertTrue( result instanceof Result.ResolutionResult );  
        
        Set<Element> productions = ((Result.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((Result.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 3 , typeElements.size());
        
        boolean twoFound = false;
        boolean ifaceFound = false;
        boolean threeFound = false;
        TypeElement two = null;
        TypeElement iface = null;
        TypeElement three = null;
        for( TypeElement typeElement : typeElements ){
            String typeName = typeElement.getQualifiedName().toString();
            if ( "foo.Two".equals(typeName)){
                twoFound = true;
                two = typeElement;
            }
            if ( "foo.Iface".equals( typeName)){
                ifaceFound = true;
                iface = typeElement;
            }
            if ( "foo.Three".equals( typeName)){
                threeFound = true;
                three = typeElement;
            }
        }
        
        assertTrue( "foo.Two should be available via ApplicableResult interface", 
                twoFound );
        assertTrue( "foo.Iface should be available via ApplicableResult interface", 
                ifaceFound );
        assertTrue( "foo.Three should be available via ApplicableResult interface", 
                threeFound );
        
        assertFalse ( "foo.Two is not an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( two ));
        assertFalse ( "foo.One2 is not an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( iface ));
        assertFalse ( "foo.Three2 is not an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( three ));
        
        assertTrue( "foo.Two2 should be disabled", 
                ((Result.ApplicableResult)result).isDisabled(two));
        assertTrue( "foo.One2 should be disabled", 
                ((Result.ApplicableResult)result).isDisabled(iface));
        assertTrue( "foo.Three2 should be disabled", 
                ((Result.ApplicableResult)result).isDisabled(three));   
    }

    private void checkSpecializes3( VariableElement element,
            WebBeansModel model )
    {
        Result result = model.getInjectable(element, null);
        
        assertNotNull( result );
        
        assertEquals( Result.ResultKind.INJECTABLE_RESOLVED, result.getKind());
        
        assertTrue( result instanceof Result.InjectableResult );
        assertTrue( result instanceof Result.ApplicableResult );
        assertTrue( result instanceof Result.ResolutionResult );
        
        Element injectable = ((Result.InjectableResult)result).getElement();
        
        assertTrue( injectable instanceof TypeElement );
        String name = ((TypeElement) injectable).getQualifiedName().toString();
        
        assertEquals( "foo.Three2", name );
        
        Set<Element> productions = ((Result.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((Result.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 3 , typeElements.size());
        
        boolean twoFound = false;
        boolean oneFound = false;
        boolean threeFound = false;
        TypeElement two = null;
        TypeElement one = null;
        TypeElement three = null;
        for( TypeElement typeElement : typeElements ){
            String typeName = typeElement.getQualifiedName().toString();
            if ( "foo.Two2".equals(typeName)){
                twoFound = true;
                two = typeElement;
            }
            if ( "foo.One2".equals( typeName)){
                oneFound = true;
                one = typeElement;
            }
            if ( "foo.Three2".equals( typeName)){
                threeFound = true;
                three = typeElement;
            }
        }
        
        assertTrue( "foo.Two2 should be available via ApplicableResult interface", 
                twoFound );
        assertTrue( "foo.One2 should be available via ApplicableResult interface", 
                oneFound );
        assertTrue( "foo.Three2 should be available via ApplicableResult interface", 
                threeFound );
        
        assertTrue ( "foo.Two2 is an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( two ));
        assertFalse ( "foo.One2 is not an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( one ));
        assertFalse ( "foo.Three2 is not an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( three ));
        
        assertTrue( "foo.Two2 should be disabled", 
                ((Result.ApplicableResult)result).isDisabled(two));
        assertTrue( "foo.One2 should be enabled", 
                ((Result.ApplicableResult)result).isDisabled(one));
        assertFalse( "foo.Three2 should be disabled", 
                ((Result.ApplicableResult)result).isDisabled(three));   
    }
    
    private void checkSpecializes2( VariableElement element,
            WebBeansModel model )
    {
        Result result = model.getInjectable(element, null);
        
        assertNotNull( result );
        
        assertEquals( Result.ResultKind.INJECTABLE_RESOLVED, result.getKind());
        
        assertTrue( result instanceof Result.InjectableResult );
        assertTrue( result instanceof Result.ApplicableResult );
        assertTrue( result instanceof Result.ResolutionResult );
        
        Element injectable = ((Result.InjectableResult)result).getElement();
        
        assertTrue( injectable instanceof TypeElement );
        String name = ((TypeElement) injectable).getQualifiedName().toString();
        
        assertEquals( "foo.One1", name );
        
        Set<Element> productions = ((Result.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((Result.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 2 , typeElements.size());
        
        boolean twoFound = false;
        boolean oneFound = false;
        TypeElement two = null;
        TypeElement one = null;
        for( TypeElement typeElement : typeElements ){
            String typeName = typeElement.getQualifiedName().toString();
            if ( "foo.Two1".equals(typeName)){
                twoFound = true;
                two = typeElement;
            }
            if ( "foo.One1".equals( typeName)){
                oneFound = true;
                one = typeElement;
            }
        }
        assertTrue( "foo.Two1 should be available via ApplicableResult interface", 
                twoFound );
        assertTrue( "foo.One1 should be available via ApplicableResult interface", 
                oneFound );
        
        assertTrue( "foo.Two1 should be disnabled", 
                ((Result.ApplicableResult)result).isDisabled(two));
        assertFalse( "foo.One1 should be enabled", 
                ((Result.ApplicableResult)result).isDisabled(one));
        
        assertTrue ( "foo.Two1 is an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( two ));
        assertFalse( "foo.One1 is not an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( one ));        
    }

    private void checkSpecializes1( VariableElement element, WebBeansModel model )
    {
        Result result = model.getInjectable(element, null);

        assertNotNull(result);

        assertEquals(Result.ResultKind.INJECTABLE_RESOLVED, result.getKind());

        assertTrue(result instanceof Result.InjectableResult);
        assertTrue(result instanceof Result.ApplicableResult);
        assertTrue(result instanceof Result.ResolutionResult);

        Element injectable = ((Result.InjectableResult)result).getElement();

        assertTrue(injectable instanceof TypeElement);
        String name = ((TypeElement) injectable).getQualifiedName().toString();

        assertEquals("foo.Three", name);

        Set<Element> productions = ((Result.ApplicableResult) result)
                .getProductions();
        Set<TypeElement> typeElements = ((Result.ApplicableResult) result)
                .getTypeElements();

        assertEquals(0, productions.size());
        assertEquals(3, typeElements.size());

        boolean twoFound = false;
        boolean oneFound = false;
        boolean threeFound = false;
        TypeElement two = null;
        TypeElement one = null;
        TypeElement three = null;
        for (TypeElement typeElement : typeElements) {
            String typeName = typeElement.getQualifiedName().toString();
            if ("foo.Two".equals(typeName)) {
                twoFound = true;
                two = typeElement;
            }
            if ("foo.One".equals(typeName)) {
                oneFound = true;
                one = typeElement;
            }
            if ("foo.Three".equals(typeName)) {
                threeFound = true;
                three = typeElement;
            }
        }
        assertTrue("foo.Two should be available via ApplicableResult interface",
                twoFound);
        assertTrue("foo.One should be available via ApplicableResult interface",
                oneFound);
        assertTrue("foo.Three should be available via ApplicableResult interface",
                threeFound);

        assertTrue("foo.One should be disabled",
                ((Result.ApplicableResult) result).isDisabled(one));
        assertTrue("foo.Two should be disabled",
                ((Result.ApplicableResult) result).isDisabled(two));
        assertFalse("foo.Three should be enabled",
                ((Result.ApplicableResult) result).isDisabled(three));
    }

    private void checkAlternative2( VariableElement element,
            WebBeansModel model )
    {
        Result result = model.getInjectable(element, null);
        
        assertNotNull( result );
        
        assertEquals( Result.ResultKind.RESOLUTION_ERROR, result.getKind());
        
        assertTrue( result instanceof Result.ApplicableResult );
        assertTrue( result instanceof Result.ResolutionResult );
        
        Set<Element> productions = ((Result.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((Result.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 3 , typeElements.size());
        
        boolean twoFound = false;
        boolean oneFound = false;
        boolean threeFound = false;
        TypeElement two = null;
        TypeElement one = null;
        TypeElement three = null;
        for( TypeElement typeElement : typeElements ){
            String typeName = typeElement.getQualifiedName().toString();
            if ( "foo.Two1".equals(typeName)){
                twoFound = true;
                two = typeElement;
            }
            if ( "foo.One1".equals( typeName)){
                oneFound = true;
                one = typeElement;
            }
            if ( "foo.Three".equals( typeName)){
                threeFound = true;
                three = typeElement;
            }
        }
        
        assertTrue( "foo.Two1 should be available via ApplicableResult interface", 
                twoFound );
        assertTrue( "foo.One1 should be available via ApplicableResult interface", 
                oneFound );
        assertTrue( "foo.Three should be available via ApplicableResult interface", 
                threeFound );
        
        assertFalse ( "foo.Two1 is not an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( two ));
        assertTrue ( "foo.One1 is an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( one ));
        assertTrue ( "foo.Three is an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( three ));
        
        assertFalse( "foo.Two1 should be enabled", 
                ((Result.ApplicableResult)result).isDisabled(two));
        assertFalse( "foo.One1 should be enabled", 
                ((Result.ApplicableResult)result).isDisabled(one));
        assertFalse( "foo.Three should be enabled", 
                ((Result.ApplicableResult)result).isDisabled(three));        
    }

    private void checkAlternative1( VariableElement element,
            WebBeansModel model )
    {
        Result result = model.getInjectable(element, null);
        
        assertNotNull( result );
        
        assertEquals( Result.ResultKind.INJECTABLE_RESOLVED, result.getKind());
        
        assertTrue( result instanceof Result.InjectableResult );
        assertTrue( result instanceof Result.ApplicableResult );
        assertTrue( result instanceof Result.ResolutionResult );
        
        Element injectable = ((Result.InjectableResult)result).getElement();
        
        assertTrue( injectable instanceof TypeElement );
        String name = ((TypeElement) injectable).getQualifiedName().toString();
        
        assertEquals( "foo.One", name );
        
        Set<Element> productions = ((Result.ApplicableResult)result).getProductions();
        Set<TypeElement> typeElements = ((Result.ApplicableResult)result).getTypeElements();
        
        assertEquals( 0 , productions.size());
        assertEquals( 2 , typeElements.size());
        
        boolean twoFound = false;
        boolean oneFound = false;
        TypeElement two = null;
        TypeElement one = null;
        for( TypeElement typeElement : typeElements ){
            String typeName = typeElement.getQualifiedName().toString();
            if ( "foo.Two".equals(typeName)){
                twoFound = true;
                two = typeElement;
            }
            if ( "foo.One".equals( typeName)){
                oneFound = true;
                one = typeElement;
            }
        }
        assertTrue( "foo.Two should be available via ApplicableResult interface", 
                twoFound );
        assertTrue( "foo.One should be available via ApplicableResult interface", 
                oneFound );
        
        assertFalse( "foo.Two should be enabled", 
                ((Result.ApplicableResult)result).isDisabled(two));
        assertFalse( "foo.One should be disabled", 
                ((Result.ApplicableResult)result).isDisabled(one));
        
        assertFalse ( "foo.Two is not an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( two ));
        assertTrue ( "foo.One is an Alternative", 
                ((Result.ResolutionResult)result).isAlternative( one ));        
    }

}
