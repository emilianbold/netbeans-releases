/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.nbbuild;

import junit.framework.TestCase;
import junit.framework.*;
import java.io.File;
import java.util.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 *
 * @author Jaroslav Tulach
 */
public class ForEachTest extends TestCase {
    private ForEach fe;
    
    public ForEachTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        fe = new ForEach();
        fe.setProject(new MockProject());
    }

    protected void tearDown() throws Exception {
    }

    public void testSetLocationsMustBeSet() {
        fe.setTarget("anything");
        try {
            fe.execute();
        } catch (BuildException ex) {
            if (ex.getMessage().indexOf("location") == -1) {
                fail("Wrong message: " + ex.getMessage());
            }
            return;
        }
        fail("Should throw an exception");
    }
    public void testSetLocationsCanBeEmpty() {
        fe.setTarget("anything");
        fe.setLocations("");
        fe.execute();
    }

    private static final class MockProject extends Project {

    }
}
