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
package org.netbeans.junit.ide;

import junit.framework.TestCase;
import junit.framework.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.openide.ErrorManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.javacore.JMManager;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;

/**
 *
 * @author Jaroslav Tulach
 */
public class ProjectSupportTest extends TestCase {
    
    public ProjectSupportTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ProjectSupportTest.class);
        
        return suite;
    }

    public void testOpenProject() {
        // just an empty test placeholder for now
    }
    
}
