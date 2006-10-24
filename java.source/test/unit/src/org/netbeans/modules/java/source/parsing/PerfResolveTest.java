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
import java.io.File;
import java.net.URL;
import junit.framework.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.StopWatch;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
/** Tests for basic JDK operations
 *
 * @author Petr Hrebejk
 */
public class PerfResolveTest extends TestCase {

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
                
    public PerfResolveTest(String testName) {
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

    /*
    public void testExtendsJTable() throws Exception {
        resolve( "MemoryFile.java", SOURCE );
    }
    */
    
    /*
    public void testJTable() throws Exception {        
        String source = TestUtil.fileToString( new File( workDir, "jdk/JTable.java" ) );
        resolve( "JTable.java", source );
    }
    */
    
    
    public void resolve( String fileName, String source ) throws Exception {
//        JavacInterface ji;
//        
//        StopWatch swatch = new StopWatch();
//                
//        
//        for( int i = 0; i < 10; i++ ) {
//            
//            System.out.println("---------- (" + i + ")" );
//            
//            swatch.start();
//            ji = JavacInterface.create( bootPath, classPath, null);
//            swatch.stop( "JI create done" );
//
//            swatch.start();        
//            CompilationUnitTree cu = ji.parse( FileObjects.memoryFileObject( SOURCE, "MemoryFile.java"), null ); 
//            swatch.stop( "Parsing done" );
//
//            swatch.start();
//            ji.resolveElements( cu );        
//            swatch.stop( "Resolution done " );                
//        }
        
    }

}
