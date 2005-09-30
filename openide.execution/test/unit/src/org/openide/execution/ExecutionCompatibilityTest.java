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

package org.openide.execution;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.execution.ExecutionEngine;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Entry point to the whole execution compatibility suite.
 *
 * @author Jaroslav Tulach
 */
public class ExecutionCompatibilityTest {
    
    /** Creates a new instance of ExecutionCompatibilityTest */
    private ExecutionCompatibilityTest() {
    }
    
    /** Executes the execution compatibility kit on the default implementation of the
     * ExecutionEngine.
     */
    public static Test suite() {
        return suite(null);
    }
    
    /** Executes the execution compatibility kit tests on the provided instance
     * of execution engine.
     */
    public static Test suite(ExecutionEngine engine) {
        System.setProperty("org.openide.util.Lookup", ExecutionCompatibilityTest.class.getName() + "$Lkp");
        Object o = Lookup.getDefault();
        if (!(o instanceof Lkp)) {
            Assert.fail("Wrong lookup object: " + o);
        }
        
        Lkp l = (Lkp)o;
        l.assignExecutionEngine(engine);
        
        if (engine != null) {
            Assert.assertEquals("Same engine found", engine, ExecutionEngine.getDefault());
        } else {
            o = ExecutionEngine.getDefault();
            Assert.assertNotNull("Engine found", o);
            Assert.assertEquals(ExecutionEngine.Trivial.class, o.getClass());
        }
        
        TestSuite ts = new TestSuite();
        ts.addTestSuite(ExecutionEngineHid.class);
        
        return ts;
    }
    
    /** Default lookup used in the suite.
     */
    public static final class Lkp extends AbstractLookup {
        private InstanceContent ic;
        
        public Lkp() {
            this(new InstanceContent());
        }
        private Lkp(InstanceContent ic) {
            super(ic);
            this.ic = ic;
        }
        
        final void assignExecutionEngine(ExecutionEngine executionEngine) {
//          ic.setPairs(java.util.Collections.EMPTY_LIST);
            if (executionEngine != null) {
                ic.add(executionEngine);
            }
        }
        
        
    }
}
