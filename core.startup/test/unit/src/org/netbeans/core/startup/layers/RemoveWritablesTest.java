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

package org.netbeans.core.startup.layers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.Module;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Stanislav Aubrecht
 */
public class RemoveWritablesTest extends NbTestCase {
    SystemFileSystem sfs;
    Module myModule;
    File configDir;
    
    private static final String manifest = "Manifest-Version: 1.0\n"
                + "OpenIDE-Module: org.netbeans.modules.foo\n"
                + "OpenIDE-Module-Specification-Version: 1.0\n"
                + "OpenIDE-Module-Implementation-Version: today\n"
                + "OpenIDE-Module-Layer: mf-layer.xml\n";


    
    public RemoveWritablesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        File u = new File(getWorkDir(), "userdir");
        File uc = new File(u, "config");
        uc.mkdirs();
        System.setProperty("netbeans.user", u.toString());
        
        File h = new File(getWorkDir(), "nb/installdir");
        new File(h, "config").mkdirs();
        System.setProperty("netbeans.home", h.toString());

        File moduleJar = createModuleJar( manifest );
        myModule = Main.getModuleSystem().getManager().create( moduleJar, null, true, false, false );
        Main.getModuleSystem().getManager().enable( myModule );
        
        sfs = (SystemFileSystem)Repository.getDefault().getDefaultFileSystem();
        
        assertNotNull("Module layer is installed", sfs.findResource( "foo" ) );
        
        configDir = FileUtil.toFile( sfs.getRoot() );//new File( getWorkDir(), "userdir/config" );
        
    }

    protected void tearDown() throws Exception {
        if( null != myModule ) {
            Main.getModuleSystem().getManager().disable( myModule );
            Main.getModuleSystem().getManager().delete( myModule );
        }
    }
    
    public void testAddedFile() throws Exception {
        FileObject folder = sfs.findResource( "foo" );
        FileObject newFile = folder.createData( "newFile", "ext" );
        
        File writableFile = new File( new File( configDir, "foo"), "newFile.ext" );
        assertTrue( writableFile.exists() );
        
        Object writablesRemover = newFile.getAttribute( "removeWritables" );
        
        assertNotNull( writablesRemover );
        assertTrue( writablesRemover instanceof Callable );
        
        ((Callable)writablesRemover).call();
        
        assertFalse( "local file removed", writableFile.exists() );
        assertNull( "FileObject does not exist", sfs.findResource( "foo/newFile.ext" ) );
    }
    
    public void testRemovedFile() throws Exception {
        FileObject folder = sfs.findResource( "foo" );
        FileObject existingFile = sfs.findResource( "foo/test1" );
        
        assertNotNull( existingFile );
        
        existingFile.delete();
        
        File maskFile = new File( new File( configDir, "foo"), "test1_hidden" );
        assertTrue( maskFile.exists() );
        
        Object writablesRemover = sfs.findResource( "foo" ).getAttribute( "removeWritables" );
        
        assertNotNull( writablesRemover );
        assertTrue( writablesRemover instanceof Callable );
        
        ((Callable)writablesRemover).call();
        
        assertFalse( "local file removed", maskFile.exists() );
        assertNotNull( "FileObject exists again", sfs.findResource( "foo/test1" ) );
    }
    
    public void testRenamedFile() throws Exception {
        FileObject folder = sfs.findResource( "foo" );
        FileObject existingFile = sfs.findResource( "foo/test1" );
        
        assertNotNull( existingFile );
        
        FileLock lock = existingFile.lock();
        existingFile.rename( lock, "newName", "newExt" );
        lock.releaseLock();
        
        assertNotNull( sfs.findResource( "foo/newName.newExt" ) );
        
        File maskFile = new File( new File( configDir, "foo"), "test1_hidden" );
        assertTrue( maskFile.exists() );
        
        Object writablesRemover = sfs.findResource( "foo" ).getAttribute( "removeWritables" );
        
        assertNotNull( writablesRemover );
        assertTrue( writablesRemover instanceof Callable );
        
        ((Callable)writablesRemover).call();
        
        assertFalse( "local file removed", maskFile.exists() );
        assertNotNull( "FileObject exists again", sfs.findResource( "foo/test1" ) );
        assertNull( "renamed file is gone", sfs.findResource( "foo/newName.newExt" ) );
    }

    public void testModifiedAttributesFile() throws Exception {
        FileObject folder = sfs.findResource( "foo" );
        FileObject existingFile = sfs.findResource( "foo/test1" );
        
        assertNotNull( existingFile );
        
        existingFile.setAttribute( "myAttribute", "myAttributeValue" );
        
        assertNull( "removeWritables does not work for file attributes", sfs.findResource( "foo" ).getAttribute( "removeWritables" ) );
    }


    private File createModuleJar(String manifest) throws IOException {
        File jarFile = new File( getWorkDir(), "mymodule.jar" );
        
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jarFile), new Manifest(
            new ByteArrayInputStream(manifest.getBytes())
        ));
        JarEntry entry = new JarEntry("mf-layer.xml");
        os.putNextEntry( entry );
        InputStream is = RemoveWritablesTest.class.getResourceAsStream( "data/layer3.xml" );
        FileUtil.copy( is, os );
        is.close();
        os.close();
        
        return jarFile;
    }
}
