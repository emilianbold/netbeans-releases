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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 * Test property evaluation.
 * @author Jesse Glick
 */
public class FreeformEvaluatorTest extends TestBase {
    
    public FreeformEvaluatorTest(String name) {
        super(name);
    }
    
    public void testPropertyEvaluation() throws Exception {
        PropertyEvaluator eval = simple.evaluator();
        assertEquals("right src.dir", "src", eval.getProperty("src.dir"));
    }
    
    public void testPropertyEvaluationChanges() throws Exception {
        FreeformProject simple2 = copyProject(simple);
        PropertyEvaluator eval = simple2.evaluator();
        assertEquals("right src.dir", "src", eval.getProperty("src.dir"));
        EditableProperties p = new EditableProperties();
        FileObject buildProperties = simple2.getProjectDirectory().getFileObject("build.properties");
        assertNotNull("have build.properties", buildProperties);
        InputStream is = buildProperties.getInputStream();
        try {
            p.load(is);
        } finally {
            is.close();
        }
        assertEquals("right original value", "src", p.getProperty("src.dir"));
        p.setProperty("src.dir", "somethingnew");
        TestPCL l = new TestPCL();
        eval.addPropertyChangeListener(l);
        FileLock lock = buildProperties.lock();
        try {
            OutputStream os = buildProperties.getOutputStream(lock);
            try {
                p.store(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
        assertEquals("got a change from properties file in src.dir", Collections.singleton("src.dir"), l.changed);
        l.reset();
        assertEquals("new value of src.dir", "somethingnew", eval.getProperty("src.dir"));
    }
    
}
