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

import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Test functionality of FreeformProject.
 * @author Jesse Glick
 */
public class FreeformProjectTest extends TestBase {
    
    public FreeformProjectTest(String name) {
        super(name);
    }
    
    public void testPropertyEvaluation() throws Exception {
        PropertyEvaluator eval = simple.evaluator();
        assertEquals("right src.dir", "src", eval.getProperty("src.dir"));
    }
    
}
