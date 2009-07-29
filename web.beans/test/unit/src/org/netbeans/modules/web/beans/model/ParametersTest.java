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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
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
                "import java.lang.annotation.*; "+
                "@BindingType " +
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
                "@BindingType " +
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
                "public class TestClass  {" +
                " @Initializer void method1( @Binding2 SuperClass arg1 , " +
                "   @Binding1(\"a\") SuperClass arg2 ){} " +
                " @Produces boolean  method2( @Binding2 Two arg ){ return false;} "+
                " @Initializer void method3( @Current SuperClass arg ){} "+
                " @Produces int method4( @Current String arg ){ return 0;} "+
                " @Initializer void method5( @Binding2 int[] arg )" +
                " void method6( @Binding2 int[] arg ){} "+
                " @Initializer void method7( SuperClass arg ){} "+
                "}" );
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Two.java",
                "package foo; " +
                "@foo.Binding2 " +
                "public class Two extends SuperClass {}" );
        
        inform( "start simple parameters test");
        
        createBeansModel().runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

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
                            checkMethod1Arg1( element, model );
                        }
                        else if ( element.getSimpleName().contentEquals("arg2")){
                            checkMethod1Arg2( element, model );
                        }
                    }
                    else if (method.getSimpleName().contentEquals("method2") ){
                        checkMethod2( element, model );
                    }
                    else if (method.getSimpleName().contentEquals("method3") ){
                        checkMethod3( element, model );
                    }
                    else if (method.getSimpleName().contentEquals("method4") ){
                        checkMethod4( element, model );
                    }
                    else if (method.getSimpleName().contentEquals("method5") ){
                        checkMethod5( element, model );
                    }
                    else if (method.getSimpleName().contentEquals("method6") ){
                        checkMethod6( element, model );
                    }
                    else if (method.getSimpleName().contentEquals("method7") ){
                        checkMethod7( element, model );
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
                "import java.lang.annotation.*; "+
                "@BindingType " +
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
                "@BindingType " +
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
        
        createBeansModel().runReadAction( new MetadataModelAction<WebBeansModel,Void>(){

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
                            checkCleanIndex( element, model );
                        }
                        else if ( element.getSimpleName().contentEquals("text")){
                            checkCleanText( element, model );
                        }
                    }
                    else if (method.getSimpleName().contentEquals("stopped") ){
                        checkStopped( element, model );
                    }
                    else if (method.getSimpleName().contentEquals("close") ){
                        checkClose( element, model );
                    }
                    else if (method.getSimpleName().contentEquals("inform") ){
                        checkInform( element, model );
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
         * TODO : need to test @Observes:
         * 1) observed event parameter with some binding.
         * 2) observed event parameter without any binding ( this is not
         * implemented yet by model).
         * 3) other parameters in observer method ( they are injection points ). 
         */
    }

    protected void checkMethod1Arg1( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test arg1 for method1");
        boolean exception = false;
        try {
            model.getInjectable(element);
        }
        catch(AmbiguousDependencyException e ){
            exception = true;
            Collection<Element> elements = e.getElements();
            boolean oneFound = false;
            boolean twoFound = false;
            for (Element injectable : elements) {
                assertTrue( "injectable should be " +
                		"a class definition, but found :"+injectable.getKind(), 
                		injectable instanceof TypeElement);
                String name = ((TypeElement)injectable).getQualifiedName().toString();
                if ( name.equals("foo.One")){
                    oneFound = true;
                }
                else if ( name.equals("foo.Two")){
                    twoFound = true;
                }
            }
            
            assertTrue( "foo.One is eligible for injection , but not found", 
                    oneFound);
            assertTrue( "foo.Two is eligible for injection , but not found", 
                    twoFound);
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
        assertTrue( "There should be two eligible elements for injection", 
                exception );
    }
    
    protected void checkMethod1Arg2( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test arg2 for method1");
        try {
            Element injectable = model.getInjectable(element);
            assertNotNull( injectable );
            assertTrue ( "injectable should be a class definition," +
            		" but found :" +injectable.getKind(), 
            		injectable instanceof TypeElement );
            
            assertEquals("foo.One",  
                    ((TypeElement)injectable).getQualifiedName().toString());
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }
    
    protected void checkMethod2( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test arg for method2");
        try {
            Element injectable = model.getInjectable(element);
            assertNotNull( injectable );
            assertTrue ( "injectable should be a class definition," +
                    " but found :" +injectable.getKind(), 
                    injectable instanceof TypeElement );
            assertEquals("foo.Two",  
                    ((TypeElement)injectable).getQualifiedName().toString());
            
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }
    
    protected void checkMethod3( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test arg for method3");
        try {
            Element injectable = model.getInjectable(element);
            assertNotNull( injectable );
            assertTrue ( "injectable should be a class definition," +
                    " but found :" +injectable.getKind(), 
                    injectable instanceof TypeElement );
            assertEquals("foo.SuperClass",  
                    ((TypeElement)injectable).getQualifiedName().toString());
            
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }
    
    protected void checkMethod4( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test arg for method4");
        try {
            Element injectable = model.getInjectable(element);
            assertNotNull( injectable );
            assertTrue ( "injectable should be a production field," +
                    " but found :" +injectable.getKind(), 
                    injectable instanceof VariableElement );
            assertEquals("productionField",  injectable.getSimpleName().toString());
            
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }
    
    protected void checkMethod5( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test arg for method5");
        try {
            Element injectable = model.getInjectable(element);
            assertNotNull( injectable );
            assertTrue ( "injectable should be a production method," +
                    " but found :" +injectable.getKind(), 
                    injectable instanceof ExecutableElement );
            assertEquals("productionMethod",  injectable.getSimpleName().toString());
            
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }
    
    protected void checkMethod6( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test arg for method6");
        try {
            Element injectable = model.getInjectable(element);
            /*
             * Method has no any special annotation. It's argument is not
             * injection point.  
             */
            assertNull( injectable );
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }
    
    protected void checkMethod7( VariableElement element,
            WebBeansModel model )
    {
        checkMethod3(element, model);
    }
    
    protected void checkCleanIndex( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test index arg for clean");
        try {
            Element injectable = model.getInjectable(element);
            assertNotNull( injectable );
            assertTrue ( "injectable should be a production method," +
                    " but found :" +injectable.getKind(), 
                    injectable instanceof ExecutableElement );
            assertEquals("getIndex",  injectable.getSimpleName().toString());
            
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }
    
    protected void checkCleanText( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test text arg for clean");
        try {
            Element injectable = model.getInjectable(element);
            assertNotNull( injectable );
            assertTrue ( "injectable should be a production method," +
                    " but found :" +injectable.getKind(), 
                    injectable instanceof ExecutableElement );
            assertEquals("get",  injectable.getSimpleName().toString());
            
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }
    
    protected void checkStopped( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test arg for stopped");
        try {
            Element injectable = model.getInjectable(element);
            assertNotNull( injectable );
            assertTrue ( "injectable should be a production method," +
                    " but found :" +injectable.getKind(), 
                    injectable instanceof ExecutableElement );
            assertEquals("isNull",  injectable.getSimpleName().toString());
            
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }
    
    protected void checkClose( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test arg for close");
        try {
            Element injectable = model.getInjectable(element);
            // there is no production method for close method
            assertNull( injectable );
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }
    
    protected void checkInform( VariableElement element,
            WebBeansModel model )
    {
        inform( "start test arg for inform");
        try {
            Element injectable = model.getInjectable(element);
            // production method is contained in other class
            assertNull( injectable );
        }
        catch (WebBeansModelException e) {
            assert false;
            e.printStackTrace();
        }
    }

}
