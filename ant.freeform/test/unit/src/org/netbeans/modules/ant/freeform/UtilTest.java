/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test utility functions.
 * @author Jesse Glick
 */
public class UtilTest extends TestBase {
    
    public UtilTest(String name) {
        super(name);
    }
    
    private FileObject test1Xml;
    private FileObject test2Xml;
    private FileObject test3Xml;
    private FileObject test5Xml;
    
    protected void setUp() throws Exception {
        super.setUp();
        test1Xml = FileUtil.toFileObject(new File(datadir, "test1.xml"));
        assertNotNull("have test1.xml", test1Xml);
        test2Xml = FileUtil.toFileObject(new File(datadir, "test2.xml"));
        assertNotNull("have test2.xml", test2Xml);
        test3Xml = FileUtil.toFileObject(new File(datadir, "test3.xml"));
        assertNotNull("have test3.xml", test3Xml);
        test5Xml = FileUtil.toFileObject(new File(datadir, "test5.xml"));
        assertNotNull("have test5.xml", test5Xml);
    }
    
    public void testGetAntScriptName() throws Exception {
        assertEquals("correct name for test1.xml", "test1", Util.getAntScriptName(test1Xml));
        assertEquals("no name for test2.xml", null, Util.getAntScriptName(test2Xml));
        assertEquals("correct name for test3.xml", "test3", Util.getAntScriptName(test3Xml));
        assertEquals("no name for test5.xml", null, Util.getAntScriptName(test5Xml));
    }
    
    public void testGetAntScriptTargetNames() throws Exception {
        assertEquals("correct targets for test1.xml",
            Arrays.asList(new String[] {"another", "main", "other"}),
            Util.getAntScriptTargetNames(test1Xml));
        assertEquals("correct targets for test2.xml",
            Collections.singletonList("sometarget"),
            Util.getAntScriptTargetNames(test2Xml));
        assertEquals("correct targets for test3.xml",
            Arrays.asList(new String[] {"imported1", "imported2", "main"}),
            Util.getAntScriptTargetNames(test3Xml));
        assertEquals("no targets for test5.xml", null, Util.getAntScriptTargetNames(test5Xml));
    }

}
