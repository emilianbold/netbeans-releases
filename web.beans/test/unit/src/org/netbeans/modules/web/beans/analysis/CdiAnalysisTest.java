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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.beans.analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class CdiAnalysisTest extends BaseAnalisysTestCase {

    /**
     * @param testName
     */
    public CdiAnalysisTest( String testName ) {
        super(testName);
    }
    
    private static final ResultProcessor NO_ERRORS_PROCESSOR = new ResultProcessor (){

        @Override
        public void process( TestProblems result ) {
            Set<Element> elements = result.getErrors().keySet();
            String msg = "";
            if ( !elements.isEmpty()) {
                msg = result.getErrors().values().iterator().next();
            }
            assertTrue(  "Expected no errors, but found :" +msg , elements.isEmpty() );
        }
        
    };

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.BaseAnalisysTestCase#createTask()
     */
    @Override
    protected CdiAnalysisTestTask createTask() {
        return new CdiAnalysisTestTask();
    }
    
    public void testTypedClass() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import java.util.List; "+
                "import javax.enterprise.inject.Typed; "+
                "@Typed({List.class}) "+
                " public class Clazz { "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "@Typed({Comparable.class}) "+
                " public class Clazz1 implements Comparable<String> { "+
                " public int comapreTo( String str ) {"+
                "   return 0; "+
                " }"+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }
            
        };
        runAnalysis(errorFile , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testAnnotationsDecoratorInterceptor() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.interceptor.Interceptor; "+
                "@Decorator "+
                "@Interceptor "+
                " public class Clazz { "+
                "}");
        
        FileObject goodFile =TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "@Interceptor "+
                " public class Clazz1 { "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testDecoratorDelegate() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import java.util.List; "+
                "import javax.decorator.Decorator; "+
                "import javax.inject.Inject; "+
                "@Decorator "+
                " public class Clazz { "+
                " @Inject int injectionPoint; "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "@Decorator "+
                " public class Clazz1 { "+
                " public Clazz1( @Delegate Object arg ){ "+
                " }"+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testDecoratorProducerField() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.enterprise.inject.Produces; "+
                "@Decorator "+
                " public class Clazz { "+
                " @Produces int production; "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "import javax.inject.Inject; "+
                "@Decorator "+
                " public class Clazz1 { "+
                "  @Inject @Delegate Object injection; "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testInterceptorMethods() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "import javax.enterprise.inject.Produces; "+
                "@Interceptor "+
                " public class Clazz { "+
                " @Produces int production(){ return 0 } ; "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "import javax.enterprise.inject.Produces; "+
                "@Interceptor "+
                " public class Clazz1 { "+
                "  int method(){ return 0;} "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testInterceptorSessionBeans() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "import javax.ejb.Singleton; "+
                "@Interceptor "+
                "@Singleton "+
                " public class Clazz { "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.interceptor.Interceptor; "+
                "@Interceptor "+
                " public class Clazz1 { "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testInitializerCtors() throws IOException{
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                " public class Clazz { "+
                " @Inject public Clazz( int i){} "+
                " @Inject public Clazz( String str ){} "+
                "}");
        
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.inject.Inject; "+
                " public class Clazz1 { "+
                " @Inject public Clazz( int i){} "+
                " public Clazz( Stirng str ){} "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkTypeElement(result, "foo.Clazz");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testTypedField() throws IOException{
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import java.util.List; "+
                "import javax.enterprise.inject.Typed; "+
                " public class Clazz { "+
                " @Typed({List.class}) Object field; "+
                " int field1; "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "import java.util.List; "+
                "import java.util.Collection; "+
                " public class Clazz1  { "+
                " @Typed({Collection.class}) List<String> field; "+
                " int field1; "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz", "field");
            }
            
        };
        runAnalysis(errorFile , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testDelegateField() throws IOException{
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Iface.java",
                "package foo; " +
                " public interface Iface  { "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "import javax.inject.Inject; "+
                " @Decorator "+
                " public class Clazz  implements Iface { "+
                " @Inject @Delegate Iface delegateInjection; "+
                " int field1; "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                " @Decorator "+
                " public class Clazz1  implements Iface  { "+
                " @Delegate Iface delegateInjection; "+
                " int field1; "+
                "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                " public class Clazz2  implements Iface  { "+
                " @Inject @Delegate Iface delegateInjection; "+
                " int field1; "+
                "}");
        
        FileObject errorFile3 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz3.java",
                "package foo; " +
                "import javax.decorator.Decorator; "+
                "import javax.decorator.Delegate; "+
                "import javax.inject.Inject; "+
                " @Decorator "+
                " public class Clazz3 implements Iface { "+
                " @Inject @Delegate Object delegateInjection; "+
                " int field1; "+
                "}");
        
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz1", "delegateInjection",
                        true);
                Set<Element> elements = result.getErrors().keySet();
                assertEquals( "Exactly two errors should be detected" , 
                        2, elements.size());
                boolean clazzFound = false;
                for (Element element : elements) {
                    if ( element instanceof  TypeElement ){
                        String fqn = ((TypeElement)element).
                                getQualifiedName().toString();
                        if ( fqn.equals("foo.Clazz1")){
                            clazzFound = true;
                        }
                    }
                }
                assertTrue("foo.Clazz1 should be marked with an error ",clazzFound);
            }
            
        };
        runAnalysis(errorFile1 , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz2", "delegateInjection");
            }
            
        };
        runAnalysis(errorFile2 , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz3", "delegateInjection");
            }
            
        };
        runAnalysis(errorFile3 , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }

    private void checkTypeElement( TestProblems result , String expectedName ){
        Set<Element> elements = result.getErrors().keySet();
        if ( elements.size() > 1 ){
            for( Element element : elements ){
                System.out.println( "Found element : "+element.toString());
            }
        }
        assertEquals(  "Expected exactly one error element", 1 , elements.size());
        Element element = elements.iterator().next();
        assertTrue( element instanceof TypeElement );
        String fqn = ((TypeElement)element).getQualifiedName().toString();
        assertEquals(expectedName, fqn);
    }
    
    public void testProductionFieldInSession() throws IOException{
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                "import javax.ejb.Singleton; "+
                "@Singleton "+
                " public class Clazz { "+
                " @Produces int production; "+
                " int field1; "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                "import javax.ejb.Singleton; "+
                "@Singleton "+
                " public class Clazz1  { "+
                " static @Produces int production; "+
                " int field1; "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz", "production");
            }
            
        };
        runAnalysis(errorFile , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testProductionFieldType() throws IOException{
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                " public class Clazz  { "+
                " static @Produces Class<String> production; "+
                " int field1; "+
                "}");
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                " public class Clazz1<T> { "+
                " @Produces T production; "+
                " int field1; "+
                "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz2.java",
                "package foo; " +
                "import javax.enterprise.inject.Produces; "+
                " public class Clazz2 { "+
                " @Produces Class<? extends String> production; "+
                " int field1; "+
                "}");
        
        
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz1", "production");
            }
            
        };
        runAnalysis(errorFile , processor);
        
        processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkFieldElement(result, "foo.Clazz2", "production");
            }
            
        };
        runAnalysis(errorFile1 , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testTypedMethod() throws IOException{
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz.java",
                "package foo; " +
                "import java.util.List; "+
                "import javax.enterprise.inject.Typed; "+
                " public class Clazz { "+
                " @Typed({List.class}) Object method(){ return null; } "+
                " int operation(){ return 0; } "+
                "}");
        
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO, "foo/Clazz1.java",
                "package foo; " +
                "import javax.enterprise.inject.Typed; "+
                "import java.util.List; "+
                "import java.util.Collection; "+
                " public class Clazz1  { "+
                " @Typed({Collection.class}) List<String> method(){ return null; }; "+
                " int operation(){ return 0; }  "+
                "}");
        ResultProcessor processor = new ResultProcessor (){

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz", "method");
            }
            
        };
        runAnalysis(errorFile , processor);
        runAnalysis( goodFile, NO_ERRORS_PROCESSOR );
    }
    
    public void testMethodAnnotations() throws IOException {
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz.java", "package foo; "
                        + "import javax.inject.Inject; "
                        + "import javax.enterprise.event.Observes; "
                        + "import javax.enterprise.inject.Produces; "
                        +" import javax.enterprise.inject.Disposes; "
                        + " public class Clazz { "
                        + " @Inject int initializer( int arg ) { return 0; } " 
                        + " @Produces String production(){return null; } ; "
                        + " void observer( @Observes String event ){} ; "
                        + " void disposer( @Disposes int arg ){} "
                        + "}");
        
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz1.java", 
                "package foo; "
                + "import javax.inject.Inject; "
                + "import javax.enterprise.inject.Produces; "
                + " public class Clazz1 { "
                + " @Inject @Produces int badProduction( int arg ){ return 0; } "+
                " void  method(){}  "
                + "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz2.java", 
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                + "import javax.enterprise.inject.Produces; "
                + " public class Clazz2 { "
                + " @Produces int badProduction( @Observes String event) { return 0; }"+
                " void  method(){}  "
                + "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz3.java", 
                "package foo; "
                + "import javax.enterprise.event.Observes; "
                +" import javax.enterprise.inject.Disposes; "
                + " public class Clazz3 { "
                + " int badObserver( @Disposes @Observes String event) { return 0; }"+
                " void  method(){}  "
                + "}");

        
        ResultProcessor processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz1", "badProduction");
            }

        };
        runAnalysis(errorFile, processor);
        
        processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz2", "badProduction");
            }

        };
        runAnalysis(errorFile1, processor);
        
        processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz3", "badObserver");
            }

        };
        runAnalysis(errorFile2, processor);


        runAnalysis(goodFile, NO_ERRORS_PROCESSOR);
    }
    
  public void testAbstractMethod() throws IOException {
      /*
       * Create a good one class file
       */
      FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO,
          "foo/Clazz.java", "package foo; "
                  + "import javax.inject.Inject; "
                  + "import javax.enterprise.inject.Produces; "
                  +" import javax.enterprise.inject.Disposes; "
                  + " public class Clazz { "
                  + " @Produces String production(){return null; } ; "
                  + " void disposer( @Disposes int arg ){} "
                  + "}");
  
      
      FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO,
              "foo/Clazz1.java", 
              "package foo; "
              + "import javax.inject.Inject; "
              + "import javax.enterprise.inject.Produces; "
              + " public class Clazz1 { "
              + " @Produces  abstract int badProduction( int arg ); "+
              " void  method(){}  "
              + "}");
      
      FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO,
              "foo/Clazz2.java", 
              "package foo; "
              +" import javax.enterprise.inject.Disposes; "
              + " public class Clazz2 { "
              + " abstract int badDisposer( @Disposes String arg);"+
              " void  method(){}  "
              + "}");
    
      
      ResultProcessor processor = new ResultProcessor() {
    
          @Override
          public void process( TestProblems result ) {
              checkMethodElement(result, "foo.Clazz1", "badProduction");
          }
    
      };
      runAnalysis(errorFile, processor);
      
      processor = new ResultProcessor() {

          @Override
          public void process( TestProblems result ) {
              checkMethodElement(result, "foo.Clazz2", "badDisposer");
          }

      };
      runAnalysis(errorFile1, processor);
  
      runAnalysis(goodFile, NO_ERRORS_PROCESSOR);
  }
    
    public void testBusinessAnnotations() throws IOException {
        /*
         * Create a good one class file
         */
        FileObject goodFile = TestUtilities.copyStringToFileObject(srcFO,
            "foo/Clazz.java", "package foo; "
                    + "import javax.inject.Inject; "
                    + "import javax.enterprise.inject.Produces; "
                    +" import javax.enterprise.inject.Disposes; "
                    + "import javax.enterprise.event.Observes; "
                    + "import javax.ejb.Stateful; "
                    + " @Stateful "
                    + " public class Clazz { "
                    + " public @Produces String production(){return null; } ; "
                    + " public void disposer( @Disposes int arg ){} "
                    + " public void observer( @Observes String event  ){} "
                    + "}");
    
        
        FileObject errorFile = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz1.java", 
                "package foo; "
                + "import javax.ejb.Stateful; "
                + "import javax.enterprise.inject.Produces; "
                + " @Stateful "
                + " public class Clazz1 { "
                + " @Produces  int notBusiness( int arg ){ return 0;}  "+
                " void  method(){}  "
                + "}");
        
        FileObject errorFile1 = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz2.java", 
                "package foo; "
                + "import javax.ejb.Stateful; "
                +" import javax.enterprise.inject.Disposes; "
                + " @Stateful "
                + " public class Clazz2 { "
                + " public final void notBusiness( @Disposes String arg){ } "+
                " void  method(){}  "
                + "}");
        
        FileObject errorFile2 = TestUtilities.copyStringToFileObject(srcFO,
                "foo/Clazz3.java", 
                "package foo; "
                + "import javax.ejb.Stateful; "
                + "import javax.enterprise.event.Observes; "
                + "import javax.ejb.PostActivate; "
                + " @Stateful "
                + " public class Clazz3 { "
                + " @PostActivate public void lifecycle( @Observes String event){ } "+
                " void  method(){}  "
                + "}");
      
        
        ResultProcessor processor = new ResultProcessor() {
      
            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz1", "notBusiness");
            }
      
        };
        runAnalysis(errorFile, processor);
        
        processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz2", "notBusiness");
            }

        };
        runAnalysis(errorFile1, processor);
        
        processor = new ResultProcessor() {

            @Override
            public void process( TestProblems result ) {
                checkMethodElement(result, "foo.Clazz3", "lifecycle");
            }

        };
        runAnalysis(errorFile2, processor);
    
        runAnalysis(goodFile, NO_ERRORS_PROCESSOR);
    }
    
    private void checkFieldElement(TestProblems result , String enclosingClass, 
            String expectedName )
    {
        checkFieldElement(result, enclosingClass, expectedName, false );
    }
    
    private void checkFieldElement(TestProblems result , String enclosingClass, 
            String expectedName , boolean checkOnlyFields )
    {
        checkElement(result, enclosingClass, expectedName, VariableElement.class, 
                checkOnlyFields);
    }
    
    private void checkMethodElement(TestProblems result , String enclosingClass, 
            String expectedName , boolean checkOnlyFields)
    {
        checkElement(result, enclosingClass, expectedName, ExecutableElement.class, 
                checkOnlyFields);
    }
    
    private void checkMethodElement(TestProblems result , String enclosingClass, 
            String expectedName )
    {
        checkMethodElement(result, enclosingClass, expectedName, false );
    }
    
    private <T extends Element> void checkElement(TestProblems result , String enclosingClass, 
            String expectedName , Class<T> elementClass, boolean checkOnlyFields )
    {
        Set<Element> elements = result.getErrors().keySet();
        Set<Element> classElements = new HashSet<Element>();
        TypeElement enclosingClazz = null;
        for( Element element : elements ){
            Element enclosingElement = element.getEnclosingElement();
            TypeElement clazz = null;
            boolean forAdd = false ;
            if ( enclosingElement instanceof TypeElement ){
                forAdd = true;
                clazz = (TypeElement) enclosingElement;
            }
            else if ( element instanceof TypeElement ){
                if ( !checkOnlyFields ){
                    forAdd = true;
                }
                clazz = (TypeElement)element;
            }
            if (  forAdd && clazz.getQualifiedName().contentEquals( enclosingClass )){
                enclosingClazz = clazz;
                //System.out.println( "Found element : "+element);
                classElements.add( element );
            }
        }
        assertNotNull("Expected enclosing class doesn't contain errors", enclosingClazz );
        assertEquals(  "Expected exactly one error element", 1 , classElements.size());
        Element element = classElements.iterator().next();
        assertTrue( elementClass.isAssignableFrom( element.getClass() ) );
        assertEquals(expectedName, element.getSimpleName().toString());
    }
    
}
