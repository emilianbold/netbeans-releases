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

package org.netbeans.modules.web.freeform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

// XXX testContextActions
// XXX testLogicalViewActions

/**
 * Test functionality of actions in FreeformProject.
 * This class just tests the basic functionality found in the "jakarta" project.
 * @author Pavel Buzek
 */
public class ActionsWebTest extends TestBaseWeb {
    
    public ActionsWebTest (String name) {
        super(name);
    }
    
    public void testBasicActions() throws Exception {
        ActionProvider ap = (ActionProvider)jakarta.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        List/*<String>*/ actionNames = new ArrayList(Arrays.asList(ap.getSupportedActions()));
        Collections.sort(actionNames);
        assertEquals("right action names", Arrays.asList(new String[] {"build", "clean", "compile.single", "debug", "javadoc", "rebuild", "redeploy", "run", "test"}), actionNames);       
        assertTrue("clean is enabled", ap.isActionEnabled("clean", Lookup.EMPTY));
        try {
            ap.isActionEnabled("frobnitz", Lookup.EMPTY);
            fail("Should throw IAE for unrecognized commands");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        try {
            ap.invokeAction("goetterdaemmerung", Lookup.EMPTY);
            fail("Should throw IAE for unrecognized commands");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        // XXX actually test running the action? how to know when it is done though? there is no API for that...
        // when Ant logger API is available, could provide a null InputOutput impl, and test that the right messages are logged
    }
    
}
