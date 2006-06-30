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

package org.netbeans.modules.projectimport.j2seimport;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Radek Matous
 */
public class DependencyValidatorTest extends NbTestCase {

    static {
        System.setProperty("projectimport.logging.level", "WARNING");
    }

    public DependencyValidatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DependencyValidatorTest.class);
        
        return suite;
    }

    /**
     * Test of isValid method, of class org.netbeans.modules.projectimport.jbuilder.elementary.utilities.DependencyValidator.
     */
    public void testIsValid() throws Exception {
        FileObject dir = FileUtil.toFileObject(getWorkDir());
        AbstractProject d = new AbstractProject("d", dir);
        AbstractProject c = new AbstractProject("c", dir);
        c.addDependency(d);
        AbstractProject b = new AbstractProject("b", dir);
        b.addDependency(c);
        AbstractProject a = new AbstractProject("a", dir);
        a.addDependency(b);
        
        
        
        
        DependencyValidator dv = DependencyValidator.checkProject(a);
        assertTrue(dv.isValid());
        
        dv = DependencyValidator.checkProject(b);
        assertTrue(dv.isValid());

        dv = DependencyValidator.checkProject(c);
        assertTrue(dv.isValid());

        dv = DependencyValidator.checkProject(d);
        assertTrue(dv.isValid());        
        
        
        d.addDependency(a);
        dv = DependencyValidator.checkProject(a);
        assertFalse(dv.isValid());
        
        dv = DependencyValidator.checkProject(b);
        assertFalse(dv.isValid());

        dv = DependencyValidator.checkProject(c);
        assertFalse(dv.isValid());

        dv = DependencyValidator.checkProject(d);
        assertFalse(dv.isValid());        
    }
    
}
