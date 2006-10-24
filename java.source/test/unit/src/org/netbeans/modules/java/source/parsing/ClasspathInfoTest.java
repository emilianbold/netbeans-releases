/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.model.JavacElements;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import junit.framework.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hrebejk
 */
public class ClasspathInfoTest extends TestCase {
    
    private File workDir;
    private File rtJar;
    private ClassPath bootPath;
    private ClassPath classPath;
    
    private final String SOURCE =
                "package some;" +
                "public class MemoryFile<K,V> extends javax.swing.JTable {" +
                "    public java.util.Map.Entry<K,V> entry;" +                       
                "}";
    
    public ClasspathInfoTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        workDir = TestUtil.createWorkFolder();
        TestUtil.copyFiles( TestUtil.getJdkDir(), workDir, TestUtil.RT_JAR );
        rtJar = FileUtil.normalizeFile(new File( workDir, TestUtil.RT_JAR ));
        URL url = FileUtil.getArchiveRoot (rtJar.toURI().toURL());
        this.bootPath = ClassPathSupport.createClassPath (new URL[] {url});
        this.classPath = ClassPathSupport.createClassPath(new URL[0]);
    }

    protected void tearDown() throws Exception {
        TestUtil.removeWorkFolder( workDir );
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ClasspathInfoTest.class);        
        return suite;
    }

    public void testCreate() {
        ClasspathInfo ci = ClasspathInfo.create( bootPath, classPath, null);
        assertNotNull( "Classpath Info should be created", ci );
    }
    
//
//    public void testParse() throws Exception {
//        final String TEST_FILE = "samples1/EmptyClass.java";
//                
//        JavacInterface ji = JavacInterface.create( bootPath, classPath, null);
//                
//        TestUtil.copyFiles( workDir, TEST_FILE );
//        CompilationUnitTree cu = ji.parse( FileObjects.fileFileObject( new File( workDir, TEST_FILE ) ), null );         
//        assertNotNull( "Should produce compilation unit.", cu );                
//    }
//    
//    public void testParseString() throws Exception {
//                        
//        JavacInterface ji = JavacInterface.create( bootPath, classPath, null);
//                
//        CompilationUnitTree cu = ji.parse( FileObjects.memoryFileObject( SOURCE, "MemoryFile.java"), null ); 
//        assertNotNull( "Should produce compilation unit.", cu );                
//    }
//
//    public void testResolve() {
//        JavacInterface ji = JavacInterface.create( bootPath, classPath, null);
//                
//        CompilationUnitTree cu = ji.parse( FileObjects.memoryFileObject( SOURCE, "MemoryFile.java"), null ); 
//        assertNotNull( "Should produce compilation unit.", cu );
//                
//        ji.resolveElements( cu );
//                
//    }
////
////    /**
////     * Test of resolveEnvironment method, of class org.netbeans.modules.java.search.parsing.JavacInterface.
////     */
////    public void testResolveEnvironment() {
////        System.out.println("testResolveEnvironment");        
////        // TODO add your test code below by replacing the default call to fail.
////        fail("The test case is empty.");
////    }
//
    
    public void testGetTypeDeclaration() throws Exception {
        ClasspathInfo ci = ClasspathInfo.create( bootPath, classPath, null);
	JavacElements elements = (JavacElements) JavaSourceAccessor.INSTANCE.createJavacTask(ci,  (DiagnosticListener) null, (String) null).getElements();
	
        List<String> notFound = new LinkedList<String>();
        JarFile jf = new JarFile( rtJar );       
        for( Enumeration entries = jf.entries(); entries.hasMoreElements(); ) {
            JarEntry je = (JarEntry)entries.nextElement();
            String jeName = je.getName();
            if ( !je.isDirectory() && jeName.endsWith( ".class" ) ) {
                String typeName = jeName.substring( 0, jeName.length() - ".class".length() );

                typeName = typeName.replace( "/", "." ); //.replace( "$", "." );
                TypeElement te = elements.getTypeElementByBinaryName( typeName );
//                assertNotNull( "Declaration for " + typeName + " should not be null.", td );
                if ( te == null ) {
                    notFound.add( typeName );
                }
            }
        }
        
        assertTrue( "Should be empty " + notFound, notFound.isEmpty() );
        
    }    
    
    public void testGetPackageDeclaration() throws Exception {
        ClasspathInfo ci = ClasspathInfo.create( bootPath, classPath, null);
        JavaFileManager fm = ClasspathInfoAccessor.INSTANCE.getFileManager(ci);
        JarFile jf = new JarFile( rtJar );
        for( Enumeration entries = jf.entries(); entries.hasMoreElements(); ) {
            JarEntry je = (JarEntry)entries.nextElement();
            String jeName = je.getName();
            if ( je.isDirectory() ) {
                String packageName = jeName.replace( "/", "." );
                if ( !fm.list( StandardLocation.PLATFORM_CLASS_PATH,packageName, EnumSet.of( JavaFileObject.Kind.CLASS ), false).iterator().hasNext() ) {
                    // empty package
                    continue;
                }
                PackageElement pd = JavaSourceAccessor.INSTANCE.createJavacTask(ci,  (DiagnosticListener) null, (String) null).getElements().getPackageElement( packageName );
                assertNotNull( "Declaration for " + packageName + " should not be null.", pd );
            }
        }
    }

//
//    /**
//     * Test of getPackageNames method, of class org.netbeans.modules.java.search.parsing.JavacInterface.
//     */
//    public void testGetPackageNames() {
//        System.out.println("testGetPackageNames");
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of getClassNames method, of class org.netbeans.modules.java.search.parsing.JavacInterface.
//     */
//    public void testGetClassNames() {
//        System.out.println("testGetClassNames");
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of getSourcePositions method, of class org.netbeans.modules.java.search.parsing.JavacInterface.
//     */
//    public void testGetSourcePositions() {
//        System.out.println("testGetSourcePositions");
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of getTypeChecker method, of class org.netbeans.modules.java.search.parsing.JavacInterface.
//     */
//    public void testGetTypeChecker() {
//        System.out.println("testGetTypeChecker");
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of getAttribution method, of class org.netbeans.modules.java.search.parsing.JavacInterface.
//     */
//    public void testGetAttribution() {
//        System.out.println("testGetAttribution");
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of cleanCaches method, of class org.netbeans.modules.java.search.parsing.JavacInterface.
//     */
//    public void testCleanCaches() {
//        System.out.println("testCleanCaches");
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of getClasspath method, of class org.netbeans.modules.java.search.parsing.JavacInterface.
//     */
//    public void testGetClasspath() {
//        System.out.println("testGetClasspath");
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of createContext method, of class org.netbeans.modules.java.search.parsing.JavacInterface.
//     */
//    public void testCreateContext() {
//        System.out.println("testCreateContext");
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }
//
//    /**
//     * Test of getErrorsFor method, of class org.netbeans.modules.java.search.parsing.JavacInterface.
//     */
//    public void testGetErrorsFor() {
//        System.out.println("testGetErrorsFor");
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }
    
}
