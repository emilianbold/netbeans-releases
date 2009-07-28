/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.beans.api.model.AmbiguousDependencyException;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModelException;


/**
 * @author ads
 *
 */
public class SpecializesTest extends CommonTestCase {

    public SpecializesTest( String testName ) {
        super(testName);
    }

    public void testSimpleTypeSpecializes() throws IOException, InterruptedException{
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "@BindingType " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface CustomBinding  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class CustomClass  {" +
                " @CustomBinding One myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "public class One  {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@CustomBinding "+
                "public class Two  extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "@Specializes "+
                "public class Three extends Two {}" );
        
        inform("start simple specializes test");
        
        createBeansModel().runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        assert element.getSimpleName().contentEquals("myField");
                        inform("test injectables for 'myField'");
                        boolean exception = false;
                        try {
                            model.getInjectable((VariableElement)element);
                        }
                        catch( AmbiguousDependencyException e ){
                            exception = true;
                            Collection<Element> elements = e.getElements();
                            boolean twoFound = false;
                            boolean threeFound = false;
                            for (Element injectable : elements) {
                                assertTrue( "injectbale "+element+
                                        " should be class definition ", 
                                        injectable instanceof TypeElement );
                                Name qualifiedName = 
                                    ((TypeElement)injectable).getQualifiedName();
                                if ( qualifiedName.contentEquals("foo.Two")){
                                    twoFound = true;
                                }
                                else if ( qualifiedName.contentEquals("foo.Three")){ 
                                    threeFound = true;
                                }
                            }
                            assertTrue( "foo.Two is eligible for injection , " +
                            		"but not found", twoFound );
                            assertTrue( "foo.Three is eligible for injection , " +
                                    "but not found", threeFound );
                        }
                        assertTrue("There should be two injectables for" +
                        		" injection point " +element.getSimpleName(), exception); 
                    }
                }
                return null;
            }
        });
    }
    
    public void testMergeBindingsSpecializes() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "@BindingType " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface CustomBinding  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "@BindingType " +
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
                "@BindingType " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class CustomClass  {" +
                " @CustomBinding @Binding1 @Binding2 One myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@Binding1 " +
                "public class One  {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "@Specializes "+
                "@Binding2 "+
                "public class Two  extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "@Specializes "+
                "@CustomBinding "+
                "public class Three extends Two {}" );
        
        inform("start merged specializes test");
        
        createBeansModel().runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        assert element.getSimpleName().contentEquals("myField");
                        inform("test injectables for 'myField'");
                        try {
                            Element injectable = 
                                model.getInjectable((VariableElement)element);
                            assertNotNull( injectable );
                            assertTrue ("Injectable element should be " +
                            		"a class definition",
                            		injectable instanceof TypeElement );
                            assertEquals( "foo.Three", 
                                    ((TypeElement)injectable).getQualifiedName().toString());
                        }
                        catch( WebBeansModelException  e){
                            assert false;
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        });
    }
    
    public void testCurrentSpecializes() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "@BindingType " +
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
                "@BindingType " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class CustomClass  {" +
                " @Current Two myField1; "+
                " @Current Three myField2; "+
                " @Current @Binding2 @Binding1 One1 myField3; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "@Current " +
                "public class One  {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "@Specializes "+
                "@Binding2 "+
                "public class Two  extends One {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "public class One1  {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two1.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "@Specializes "+
                "@Binding2 "+
                "public class Two1  extends One1 {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "@Specializes "+
                "@Binding1 "+
                "public class Three  extends Two1 {}" );
        
        inform("start @Current specializes test");
        
        createBeansModel().runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                Set<String> names = new HashSet<String>();
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        names.add( element.getSimpleName().toString());
                        if ( element.getSimpleName().contentEquals("myField1")){
                            check1( element , model );
                        }
                        else if ( element.getSimpleName().contentEquals("myField2")){
                            check2( element , model );
                        }
                        else if ( element.getSimpleName().contentEquals("myField3")){
                            check3( element , model );
                        }
                    }
                }
                assert names.contains("myField1");
                assert names.contains("myField2");
                assert names.contains("myField3");
                return null;
            }
        });
    }

    protected void check1( Element element , WebBeansModel model ) 
        throws WebBeansModelException 
    {
        Element injectable = 
            model.getInjectable((VariableElement)element);
        assertNotNull( injectable );
        assertTrue ("Injectable element should be a class definition",
                injectable instanceof TypeElement );
        assertEquals( "foo.Two", 
                ((TypeElement)injectable).getQualifiedName().toString());        
    }
    
    protected void check2( Element element, WebBeansModel model )
            throws WebBeansModelException
    {
        Element injectable = model.getInjectable((VariableElement) element);
        assertNotNull(injectable);
        assertTrue("Injectable element should be a class definition",
                injectable instanceof TypeElement);
        assertEquals("foo.Three", ((TypeElement) injectable).getQualifiedName()
                .toString());
    }
    
    protected void check3( Element element, WebBeansModel model )
            throws WebBeansModelException
    {
        check2(element, model);
    }
    
    public void testSimpleProductionSpecializes() throws IOException, InterruptedException{
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "@BindingType " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface CustomBinding  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "public class CustomClass  {" +
                " @CustomBinding int myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class One  {" +
                " @CustomBinding @Produces int getIndex(){ return 0;} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "import javax.enterprise.inject.*; "+
                "public class Two  extends One {" +
                " @Produces @Specializes int getIndex(){return 0;} "+
                "}" );
        
        inform("start simple specializes test for production methods");
        
        createBeansModel().runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        assert element.getSimpleName().contentEquals("myField");
                        inform("test injectables for 'myField'");
                        boolean exception = false;
                        try {
                            model.getInjectable((VariableElement)element);
                        }
                        catch( AmbiguousDependencyException e ){
                            exception = true;
                            Collection<Element> elements = e.getElements();
                            for (Element injectable : elements) {
                                assertTrue( "injectbale "+element+
                                        " should be production methods ", 
                                        injectable instanceof ExecutableElement );
                                Name qualifiedName = injectable.getSimpleName();
                                assertEquals( "getIndex" , qualifiedName.toString());
                            }
                        }
                        assertTrue("There should be two injectables for" +
                                " injection point " +element.getSimpleName(), exception); 
                    }
                }
                return null;
            }
        });
    }
    
    public void testMergeProductionSpecializes() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomBinding.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "@BindingType " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface CustomBinding  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "@BindingType " +
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
                "@BindingType " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "public class CustomClass  {" +
                " @CustomBinding @Binding1 @Binding2 int myField; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class One  {" +
                " @Produces @CustomBinding int getIndex(){ return 0; } " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "import javax.enterprise.inject.*; "+
                "public class Two  extends One {" +
                " @Produces @Specializes @Binding1 int getIndex(){ return 0; } " +
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "import javax.enterprise.inject.*; "+
                "public class Three extends Two {" +
                " @Produces @Specializes @Binding2 int getIndex(){ return 0; } " +
                "}" );
        
        inform("start merged specializes test for production method");
        
        createBeansModel().runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        assert element.getSimpleName().contentEquals("myField");
                        inform("test injectables for 'myField'");
                        try {
                            Element injectable = 
                                model.getInjectable((VariableElement)element);
                            assertNotNull( injectable );
                            assertTrue ("Injectable element should be " +
                                    "a production method",
                                    injectable instanceof ExecutableElement );
                            assertEquals( "getIndex", 
                                    injectable.getSimpleName().toString());
                            
                            Element enclosingElement = injectable.getEnclosingElement();
                            assertTrue( enclosingElement instanceof TypeElement);
                            
                            assertEquals("foo.Three", ((TypeElement)enclosingElement).
                                    getQualifiedName().toString());
                        }
                        catch( WebBeansModelException  e){
                            assert false;
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        });
    }
    
    public void testCurrentProductionSpecializes() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "foo/Binding1.java",
                "package foo; " +
                "import static java.lang.annotation.ElementType.METHOD; "+
                "import static java.lang.annotation.ElementType.FIELD; "+
                "import static java.lang.annotation.ElementType.PARAMETER; "+
                "import static java.lang.annotation.ElementType.TYPE; "+
                "import static java.lang.annotation.RetentionPolicy.RUNTIME; "+
                "import javax.enterprise.inject.*; "+
                "import java.lang.annotation.*; "+
                "@BindingType " +
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
                "@BindingType " +
                "@Retention(RUNTIME) "+
                "@Target({METHOD, FIELD, PARAMETER, TYPE}) "+
                "public @interface Binding2  {}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class CustomClass  {" +
                " @Current @Binding1 int myField1; "+
                " @Current @Binding2 @Binding1 boolean myField2; "+
                "}");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class One  {" +
                " @Produces @Current int getIndex(){ return 0;} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "import javax.enterprise.inject.*; "+
                "public class Two  extends One {" +
                " @Produces @Specializes @Binding1 int getIndex(){ return 0;} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One1.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class One1  {" +
                " @Produces boolean isNull(){ return true;} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two1.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "import javax.enterprise.inject.*; "+
                "public class Two1  extends One1 {" +
                " @Produces @Specializes @Binding2 boolean isNull(){ return true;} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Three.java",
                "package foo; " +
                "import javax.enterprise.inject.deployment.*; "+
                "import javax.enterprise.inject.*; "+
                "public class Three  extends Two1 {" +
                " @Produces @Specializes @Binding1 boolean isNull(){ return true;} "+
                "}" );
        
        inform("start @Current specializes test for production method");
        
        createBeansModel().runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

            public Void run( WebBeansModel model ) throws Exception {
                TypeMirror mirror = model.resolveType( "foo.CustomClass" );
                Element clazz = ((DeclaredType)mirror).asElement();
                List<? extends Element> children = clazz.getEnclosedElements();
                Set<String> names = new HashSet<String>();
                for (Element element : children) {
                    if ( element instanceof VariableElement ){
                        names.add( element.getSimpleName().toString());
                        if ( element.getSimpleName().contentEquals("myField1")){
                            checkProduces1( element , model );
                        }
                        else if ( element.getSimpleName().contentEquals("myField2")){
                            checkProduces2( element , model );
                        }
                    }
                }
                assert names.contains("myField1");
                assert names.contains("myField2");
                return null;
            }
        });
    }
    
    protected void checkProduces1( Element element, WebBeansModel model )
            throws WebBeansModelException
    {
        Element injectable = model.getInjectable((VariableElement) element);
        assertNotNull(injectable);
        assertTrue("Injectable element should be a production method",
                injectable instanceof ExecutableElement);
        assertEquals("getIndex", injectable.getSimpleName().toString());
        
        Element enclosingElement = injectable.getEnclosingElement();
        assert enclosingElement instanceof TypeElement;
        
        assertEquals("foo.Two",  
                ((TypeElement)enclosingElement).getQualifiedName().toString());
    }
    
    protected void checkProduces2( Element element, WebBeansModel model )
            throws WebBeansModelException
    {
        Element injectable = model.getInjectable((VariableElement) element);
        assertNotNull(injectable);
        assertTrue("Injectable element should be a production method",
                injectable instanceof ExecutableElement);
        assertEquals("isNull", injectable.getSimpleName().toString());

        Element enclosingElement = injectable.getEnclosingElement();
        assert enclosingElement instanceof TypeElement;

        assertEquals("foo.Three", ((TypeElement) enclosingElement)
                .getQualifiedName().toString());
    }
}
