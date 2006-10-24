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
import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.insane.scanner.ScannerUtils;
import org.netbeans.insane.scanner.SimpleXmlVisitor;
import org.netbeans.insane.scanner.Visitor;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
/** Tests whether the JavacInterface gets GCed after some operations
 *
 * @author Petr Hrebejk
 */
public class PerfJavacIntefaceGCTest extends NbTestCase {
    
    private File workDir;
    private File rtJar;
    private ClassPath bootPath;
    private ClassPath classPath;
    private final String SOURCE =
                "package some;" +
                "import javax.swing.JTable;" +
                "import javax.swing.JLabel;" +
                "public class MemoryFile<K,V> extends JTable {" +
                "    public java.util.Map.Entry<K,V> entry;" +
                "    public JLabel label;" +
                "    public JTable table = new JTable();" +                       
                "    public MemoryFile() {}" +                       
                "}";
                
    public PerfJavacIntefaceGCTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        workDir = TestUtil.createWorkFolder();
        TestUtil.copyFiles( workDir, TestUtil.RT_JAR, "jdk/JTable.java" );
        rtJar = new File( workDir, TestUtil.RT_JAR );
        URL url = FileUtil.getArchiveRoot (rtJar.toURI().toURL());
        this.bootPath = ClassPathSupport.createClassPath (new URL[] {url});
        this.classPath = ClassPathSupport.createClassPath(new URL[0]);
    }

    protected void tearDown() throws Exception {
        TestUtil.removeWorkFolder( workDir );
    }

//    public void testSimple() throws Exception {
//        
//        JavacInterface ji = (JavacInterface) JavacInterface.create( bootPath, classPath, null);
//        WeakReference<JavacInterface> wr = new WeakReference<JavacInterface>( ji );
//        ji = null;
//        assertGC( "JavacInterface should be GCed", wr );
//        
//    }
//    
//    public void testAfterParse() throws Exception {
//        
//        JavacInterface ji = (JavacInterface) JavacInterface.create( bootPath, classPath, null);
//        ji.parse( FileObjects.memoryFileObject( SOURCE, "MemoryFile.java"), null );
//        WeakReference<JavacInterface> wr = new WeakReference<JavacInterface>( ji );
//        ji = null;
//        assertGC( "JavacInterface should be GCed", wr );
//        
//    }
//    
//    public void testAfterParseAndResolve() throws Exception {
//        
//        JavacInterface ji = (JavacInterface) JavacInterface.create( bootPath, classPath, null);
//        CompilationUnitTree cu = ji.parse( FileObjects.memoryFileObject( SOURCE, "MemoryFile.java"), null );
//        ji.resolveElements( cu );
//        WeakReference<JavacInterface> wr = new WeakReference<JavacInterface>( ji );
//        WeakReference<Context> ctx = new WeakReference<Context>( ji.getContext() );        
//        cu = null;
//        ji = null;
//        assertGC( "JavacInterface should be GCed", wr );
//        
//        // Visitor v = new SimpleXmlVisitor( new File( "/tmp/insane.xml" ) );
//        // ScannerUtils.scan( null, v, Collections.singleton( Context.class.getClassLoader() ), true );
//        
//        assertGC( "Context should be GCed", ctx );
//    }
//    
//    public void testAfterGetDeclaration() throws Exception {
//        
//        JavacInterface ji = (JavacInterface) JavacInterface.create( bootPath, classPath, null);
//        TypeDeclaration td  = ji.getTypeDeclaration( "java.lang.Object" );
//        WeakReference<TypeDeclaration> wr = new WeakReference<TypeDeclaration>( td );
//        WeakReference<Context> ctx = new WeakReference<Context>( ji.getContext() );
//        td = null;
//        ji = null;
//        assertGC( "Type Declaration be GCed", wr );
//        assertGC( "Context should be GCed", ctx );
//                
//    }
//    
//    
//    public void testCompilationUnitSize() throws Exception {
//        
//        JavacInterface ji = (JavacInterface) JavacInterface.create( bootPath, classPath, null);
//        CompilationUnitTree cu = ji.parse( FileObjects.memoryFileObject( SOURCE, "MemoryFile.java"), null );
//        ji.resolveElements( cu );
//        
//        assertSize( "Compilation unit should not be too big", Collections.singleton( ji ), 1600000, new Object[] { CachingArchiveProvider.getDefault() }  );
//        
//    }
    
}
