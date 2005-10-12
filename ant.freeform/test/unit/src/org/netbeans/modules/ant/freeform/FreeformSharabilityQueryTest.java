/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import org.netbeans.api.queries.SharabilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Jan Lahoda
 */
public class FreeformSharabilityQueryTest extends TestBase {
    
    public FreeformSharabilityQueryTest(String testName) {
	super(testName);
    }

    public void testSharability() throws Exception {
	FreeformProject prj = copyProject(simple);
	FileObject nbproject = prj.getProjectDirectory().getFileObject("nbproject");
	FileObject nbprojectProjectXML = nbproject.getFileObject("project.xml");
	FileObject nbprojectPrivate = nbproject.createFolder("private");
	FileObject nbprojectPrivatePrivateXML = nbprojectPrivate.createData("private.xml");
	FileObject src = prj.getProjectDirectory().getFileObject("src");
	FileObject myAppJava = src.getFileObject("org/foo/myapp/MyApp.java");
	FileObject buildXML = prj.getProjectDirectory().getFileObject("build.xml");
	
	assertNotNull(nbproject);
	assertNotNull(nbprojectProjectXML);
	assertNotNull(nbprojectPrivate);
	assertNotNull(nbprojectPrivatePrivateXML);
	assertNotNull(src);
	assertNotNull(myAppJava);
	assertNotNull(buildXML);
	
	assertEquals(SharabilityQuery.MIXED, SharabilityQuery.getSharability(FileUtil.toFile(nbproject)));
	assertEquals(SharabilityQuery.SHARABLE, SharabilityQuery.getSharability(FileUtil.toFile(nbprojectProjectXML)));
	assertEquals(SharabilityQuery.NOT_SHARABLE, SharabilityQuery.getSharability(FileUtil.toFile(nbprojectPrivate)));
	assertEquals(SharabilityQuery.NOT_SHARABLE, SharabilityQuery.getSharability(FileUtil.toFile(nbprojectPrivatePrivateXML)));
	assertEquals(SharabilityQuery.UNKNOWN, SharabilityQuery.getSharability(FileUtil.toFile(src)));
	assertEquals(SharabilityQuery.UNKNOWN, SharabilityQuery.getSharability(FileUtil.toFile(myAppJava)));
	assertEquals(SharabilityQuery.UNKNOWN, SharabilityQuery.getSharability(FileUtil.toFile(buildXML)));
    }
    
}
