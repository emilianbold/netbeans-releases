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

package org.netbeans.core.projects;

import java.lang.reflect.Modifier;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.core.projects.SettingChildren;
import org.netbeans.core.projects.SettingChildren.FileStateProperty;

/**
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class FileStatePropertyTest extends NbTestCase {
    
    public FileStatePropertyTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(FileStatePropertyTest.class));
    }
    
    /** This test assures compatibility with Jelly library.
     * Please contact QA or any JellyTools developer in case of failure.
     */    
    public void testJellyCompatibility() {
        try {
            assertTrue("SettingChildren class is public", Modifier.isPublic(SettingChildren.class.getModifiers()));
            assertTrue("FileStateProperty class is public", Modifier.isPublic(FileStateProperty.class.getModifiers()));
            try {
                new FileStateProperty("Modules-Layer").getValue();
            } catch (NullPointerException npe) {}
        } catch (Exception e) {
            throw new AssertionFailedErrorException("JellyTools compatibility conflict, please contact QA or any JellyTools developer.", e);
        }
    }
    
    
}
