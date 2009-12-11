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
import org.netbeans.modules.web.beans.impl.model.results.DefinitionErrorResult;
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;


/**
 * @author ads
 *
 */
public class ParametersTest extends CommonTestCase {

    public ParametersTest( String testName ) {
        super(testName);
    }
    
    public void testSimpleParameter() throws IOException, InterruptedException{
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
                "public @interface Binding2  {} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class SuperClass  { " +
                " @Produces String productionField = \"\"; "+
                " @Produces @foo.Binding2 int[] productionMethod() { return null; } "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@foo.Binding1(value=\"a\") @foo.Binding2 " +
                "public class One extends SuperClass {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "import javax.inject.*; "+
                "public class TestClass  {" +
                " @Inject void method1( @Binding2 SuperClass arg1 , " +
                "   @Binding1(\"a\") SuperClass arg2 ){} " +
                " @Produces boolean  method2( @Binding2 Two arg ){ return false;} "+
                " @Inject void method3( @Default SuperClass arg ){} "+
                " @Produces int method4( @Default String arg ){ return 0;} "+
                " @Inject void method5( @Binding2 int[] arg ){} " +
                " void method6( @Binding2 int[] arg ){} "+
                " @Inject void method7( SuperClass arg ){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@foo.Binding2 " +
                "public class Two extends SuperClass {}" );
        
        inform( "start simple parameters test");
        
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
                    if ( element instanceof ExecutableElement ){
                        List<? extends VariableElement> parameters = 
                            ((ExecutableElement)element).getParameters();
                        for (VariableElement variableElement : parameters) {
                            injectionPoints.add( variableElement );
                        }
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    Element enclosingElement = element.getEnclosingElement();
                    assert enclosingElement instanceof ExecutableElement;
                    ExecutableElement method = (ExecutableElement)enclosingElement;
                    names.add( method.getSimpleName()+ " " +element.getSimpleName());
                    if ( method.getSimpleName().contentEquals("method1")){
                        if ( element.getSimpleName().contentEquals("arg1")){
                            checkMethod1Arg1( element, provider );
                        }
                        else if ( element.getSimpleName().contentEquals("arg2")){
                            checkMethod1Arg2( element, provider );
                        }
                    }
                    else if (method.getSimpleName().contentEquals("method2") ){
                        checkMethod2( element, provider );
                    }
                    else if (method.getSimpleName().contentEquals("method3") ){
                        checkMethod3( element, provider );
                    }
                    else if (method.getSimpleName().contentEquals("method4") ){
                        checkMethod4( element, provider );
                    }
                    else if (method.getSimpleName().contentEquals("method5") ){
                        checkMethod5( element, provider );
                    }
                    else if (method.getSimpleName().contentEquals("method6") ){
                        checkMethod6( element, provider );
                    }
                    else if (method.getSimpleName().contentEquals("method7") ){
                        checkMethod7( element, provider );
                    }
                }
                
                assert names.contains("method1 arg1");
                assert names.contains("method1 arg2");
                assert names.contains("method2 arg");
                assert names.contains("method3 arg");
                assert names.contains("method4 arg");
                assert names.contains("method6 arg");
                return null;
            }
        });
    }
    
    public void testDisposesParameter() throws IOException, InterruptedException{
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
                "public @interface Binding2  {} ");
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/SuperClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class SuperClass  { " +
                " @Produces @Binding2 String getText(){ return null;} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/One.java",
                "package foo; " +
                "@foo.Binding1(value=\"a\") @foo.Binding2 " +
                "public class One extends SuperClass {}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/TestClass.java",
                "package foo; " +
                "import javax.enterprise.inject.*; "+
                "public class TestClass  {" +
                " @Produces @Binding2 int getIndex(){ return 0;} "+
                " @Produces @Binding1(\"a\") boolean isNull(){ return false;} "+
                " @Produces String get(){ return null;} "+
                
                " void clean(@Disposes @Binding2 int index , String text){} "+
                " void stopped(@Disposes @Binding1(\"a\") boolean isNull ){} "+
                " void close(@Disposes @Binding1(\"a\") SuperClass clazz){} "+
                " void inform(@Disposes @Binding2 String text){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@foo.Binding2 " +
                "public class Two extends SuperClass {}" );
        
        inform( "start disposes parameters test");
        
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
                    if ( element instanceof ExecutableElement ){
                        List<? extends VariableElement> parameters = 
                            ((ExecutableElement)element).getParameters();
                        for (VariableElement variableElement : parameters) {
                            injectionPoints.add( variableElement );
                        }
                    }
                }
                Set<String> names = new HashSet<String>(); 
                for( VariableElement element : injectionPoints ){
                    Element enclosingElement = element.getEnclosingElement();
                    assert enclosingElement instanceof ExecutableElement;
                    ExecutableElement method = (ExecutableElement)enclosingElement;
                    names.add( method.getSimpleName()+ " " +element.getSimpleName());
                    if ( method.getSimpleName().contentEquals("clean")){
                        if ( element.getSimpleName().contentEquals("index")){
                            checkCleanIndex( element, provider );
                        }
                        else if ( element.getSimpleName().contentEquals("text")){
                            checkCleanText( element, provider );
                        }
                    }
                    else if (method.getSimpleName().contentEquals("stopped") ){
                        checkStopped( element, provider );
                    }
                    else if (method.getSimpleName().contentEquals("close") ){
                        checkClose( element, provider );
                    }
                    else if (method.getSimpleName().contentEquals("inform") ){
                        checkInform( element, provider );
                    }
                }
                
                assert names.contains("clean index");
                assert names.contains("clean text");
                assert names.contains("stopped isNull");
                assert names.contains("close clazz");
                assert names.contains("inform text");
                return null;
            }
        });
    }
    
    public void testObserves (){
        
         /* 
          * TODO : need to test @Observes
          */
    }
    
    protected void checkMethod1Arg1( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("start test arg1 for method1");

        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(2, typeElements.size());
        assertEquals(0, productions.size());

        boolean oneFound = false;
        boolean twoFound = false;
        for (TypeElement injectable : typeElements) {
            String name = injectable.getQualifiedName().toString();
            if (name.equals("foo.One")) {
                oneFound = true;
            }
            else if (name.equals("foo.Two")) {
                twoFound = true;
            }
        }

        assertTrue("foo.One is eligible for injection , but not found",
                oneFound);
        assertTrue("foo.Two is eligible for injection , but not found",
                twoFound);
    }
    
    protected void checkMethod1Arg2( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("start test arg2 for method1");

        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(1, typeElements.size());
        assertEquals(0, productions.size());

        TypeElement injectable = typeElements.iterator().next();
        assertNotNull(injectable);

        assertEquals("foo.One", injectable.getQualifiedName().toString());
    }
    
    protected void checkMethod2( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("start test arg for method2");

        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);   

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(1, typeElements.size());
        assertEquals(0, productions.size());

        TypeElement injectable = typeElements.iterator().next();
        assertNotNull(injectable);
        assertEquals("foo.Two", injectable.getQualifiedName().toString());

    }
    
    protected void checkMethod3( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("start test arg for method3");
        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(1, typeElements.size());
        assertEquals(0, productions.size());

        TypeElement injectable = typeElements.iterator().next();
        assertNotNull(injectable);
        assertEquals("foo.SuperClass", injectable.getQualifiedName().toString());

    }
    
    protected void checkMethod4( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("start test arg for method4");
        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(1, productions.size());

        Element injectable = productions.iterator().next();
        assertNotNull(injectable);
        assertTrue("injectable should be a production field," + " but found :"
                + injectable.getKind(), injectable instanceof VariableElement);
        assertEquals("productionField", injectable.getSimpleName().toString());

    }
    
    protected void checkMethod5( VariableElement element, 
            TestWebBeansModelProviderImpl provider)
    {
        inform("start test arg for method5");
        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(1, productions.size());

        Element injectable = productions.iterator().next();
        assertNotNull(injectable);
        assertTrue("injectable should be a production method," + " but found :"
                + injectable.getKind(), injectable instanceof ExecutableElement);
        assertEquals("productionMethod", injectable.getSimpleName().toString());

    }
    
    protected void checkMethod6( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("start test arg for method6");
        Result result = provider.findParameterInjectable(element, null);

        /*
         * Method has no any special annotation. It's argument is not injection
         * point.
         */
        assertTrue( result instanceof DefinitionErrorResult );
    }
    
    protected void checkMethod7( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        checkMethod3(element, provider );
    }
    
    protected void checkCleanIndex( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("start test index arg for clean");
        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(1, productions.size());

        Element injectable = productions.iterator().next();
        assertNotNull(injectable);
        assertTrue("injectable should be a production method," + " but found :"
                + injectable.getKind(), injectable instanceof ExecutableElement);
        assertEquals("getIndex", injectable.getSimpleName().toString());

    }
    
    protected void checkCleanText( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("start test text arg for clean");
        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(1, productions.size());

        Element injectable = productions.iterator().next();
        assertNotNull(injectable);
        assertTrue("injectable should be a production method," + " but found :"
                + injectable.getKind(), injectable instanceof ExecutableElement);
        assertEquals("get", injectable.getSimpleName().toString());

    }
    
    protected void checkStopped( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform("start test arg for stopped");
        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(1, productions.size());

        Element injectable = productions.iterator().next();
        assertNotNull(injectable);
        assertTrue("injectable should be a production method," + " but found :"
                + injectable.getKind(), injectable instanceof ExecutableElement);
        assertEquals("isNull", injectable.getSimpleName().toString());

    }
    
    protected void checkClose( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform( "start test arg for close");
        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(0, productions.size());
    }
    
    protected void checkInform( VariableElement element,
            TestWebBeansModelProviderImpl provider )
    {
        inform( "start test arg for inform");
        Result result = provider.findParameterInjectable(element, null);
        assertNotNull(result);

        assertTrue(result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        Set<Element> productions = ((ResultImpl) result).getProductions();

        assertEquals(0, typeElements.size());
        assertEquals(0, productions.size());
    }

}
