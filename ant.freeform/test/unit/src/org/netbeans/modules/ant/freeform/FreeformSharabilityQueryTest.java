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
