/*
 * FileStatePropertyTest.java
 * NetBeans JUnit based test
 *
 * Created on November 13, 2002, 1:32 PM
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
                new FileStateProperty("Project-Layer").getValue();
            } catch (NullPointerException npe) {}
        } catch (Exception e) {
            throw new AssertionFailedErrorException("JellyTools compatibility conflict, please contact QA or any JellyTools developer.", e);
        }
    }
    
    
}
