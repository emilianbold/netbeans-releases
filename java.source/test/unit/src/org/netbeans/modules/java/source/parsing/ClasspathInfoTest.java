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
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hrebejk
 */
public class ClasspathInfoTest extends NbTestCase {
    
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
        this.clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
        TestUtil.copyFiles( TestUtil.getJdkDir(), workDir, TestUtil.RT_JAR );
        rtJar = FileUtil.normalizeFile(new File( workDir, TestUtil.RT_JAR ));
        URL url = FileUtil.getArchiveRoot (rtJar.toURI().toURL());
        this.bootPath = ClassPathSupport.createClassPath (new URL[] {url});
        this.classPath = ClassPathSupport.createClassPath(new URL[0]);
    }

    protected void tearDown() throws Exception {
        //Delete unneeded rt.jar
        rtJar.delete();
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
	JavacElements elements = (JavacElements) JavaSourceAccessor.getINSTANCE().createJavacTask(ci,  (DiagnosticListener) null, (String) null).getElements();
	
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
        JavaFileManager fm = ClasspathInfoAccessor.getINSTANCE().getFileManager(ci);
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
                PackageElement pd = JavaSourceAccessor.getINSTANCE().createJavacTask(ci,  (DiagnosticListener) null, (String) null).getElements().getPackageElement( packageName );
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
