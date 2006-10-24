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

package org.netbeans.modules.java.source;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import junit.framework.*;


/** Makes sure the TestUtility class works as expected.
 *
 * @author Petr Hrebejk
 */
public class TestUtilTest extends TestCase {

    public TestUtilTest( String testName ) {
        super( testName );
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite( TestUtilTest.class );        
        return suite;
    }

    public void testCreateAndRemoveWorkDir() throws Exception {
	
	
        File workDir = TestUtil.createWorkFolder();
        
        assertEquals( "WorkDir must exist", true, workDir.exists() );
        assertEquals( "WorkDir must be readable", true, workDir.canRead() );
        assertEquals( "WorkDir must be writeable", true, workDir.canWrite() );
        
        TestUtil.removeWorkFolder( workDir );
        assertEquals( "WorkDir must disapear", false, workDir.exists() );
                
    }
    
    public void testCopyResourceFile() throws Exception {
        
	String SAMPLE_FILE = "samples1/EmptyClass.java";
	
        File workDir = TestUtil.createWorkFolder();
        
        TestUtil.copyFiles( workDir, SAMPLE_FILE );        
        File sf = new File( workDir, SAMPLE_FILE );
        
        assertEquals( "WorkDir must exist", true, sf.exists() );
        assertEquals( "WorkDir must be readable", true, sf.canRead() );
        assertEquals( "WorkDir must be writeable", true, sf.canWrite() );
        
	TestUtil.copyFiles( TestUtil.getJdkDir(), workDir, TestUtil.RT_JAR );        
        File rt = new File( workDir, TestUtil.RT_JAR );
        
        assertEquals( "WorkDir must exist", true, rt.exists() );
        assertEquals( "WorkDir must be readable", true, rt.canRead() );
        assertEquals( "WorkDir must be writeable", true, rt.canWrite() );
	
        TestUtil.removeWorkFolder( workDir );
        assertEquals( "WorkDir must disapear", false, workDir.exists() );
        
    }
    
    public void testCopySampleFile() throws Exception {
        
        File workDir = TestUtil.createWorkFolder();
        
        TestUtil.copyFiles( workDir, "samples1/EmptyClass.java" );        
        File sample = new File( workDir, "samples1/EmptyClass.java" );
        
        assertEquals( "WorkDir must exist", true, sample.exists() );
        assertEquals( "WorkDir must be readable", true, sample.canRead() );
        assertEquals( "WorkDir must be writeable", true, sample.canWrite() );
        
        TestUtil.removeWorkFolder( workDir );
        assertEquals( "WorkDir must disapear", false, workDir.exists() );
        
    }
    
    public void testUnzip() throws Exception {
        
        File workDir = TestUtil.createWorkFolder();
        
        TestUtil.copyFiles( TestUtil.getJdkDir(), workDir, TestUtil.RT_JAR );
        File rt = new File( workDir, TestUtil.RT_JAR );
        JarFile rtJar = new JarFile( rt );
        
        File dest = new File( workDir, "dest" );
        TestUtil.unzip( rtJar, dest ); // Unzip Jar file

        Set<String> entryNames = new TreeSet<String>(); 
        for( Enumeration<? extends ZipEntry> e = rtJar.entries(); e.hasMoreElements(); ) {
            addNames(  e.nextElement(), entryNames );
        }
        
        Set<String> fileNames = new TreeSet<String>();
        addNamesRecursively( dest, fileNames, dest.getPath().length() + 1 );
                
        // TestUtil.collectionDiff( fileNames, entryNames );
        
        assertEquals( "Sets should have the same size", entryNames.size(), fileNames.size() );        
        assertEquals( "Lists should be identical", entryNames, fileNames );
        
        TestUtil.removeWorkFolder( workDir );
        assertEquals( "WorkDir must disapear", false, workDir.exists() );
        
    }
    
    // Private methods ---------------------------------------------------------
    
    private void addNames( ZipEntry entry, Set<String> dest ) {
    
        
        String name = entry.getName();
        if ( entry.isDirectory() ) {
            name = name.substring( name.length() - 1 );
        }
        
        int index = name.indexOf( '/', 0 );        
        while( index != -1 ) {
            if ( index != 0 ) {                
                dest.add( name.substring( 0, index ) );
            }
            index = name.indexOf( '/', index + 1 );                
        }
        
        if ( !"/".equals( name ) ) {
            dest.add( name );
        }
                
    }
    
    private void addNamesRecursively( File folder, Set<String> list, int beginIndex ) {
        
        File[] files = folder.listFiles();
        for( File f : files ) {
            list.add( f.getPath().substring( beginIndex ) );
            if ( f.isDirectory() ) {
                addNamesRecursively( f, list, beginIndex );
            }
        }
                
    }
    
    
}
