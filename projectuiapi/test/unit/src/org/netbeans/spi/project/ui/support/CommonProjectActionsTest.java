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

package org.netbeans.spi.project.ui.support;
import junit.framework.*;
import javax.swing.Action;
import org.netbeans.modules.project.uiapi.Utilities;

/**
 *
 * @author Petr Kuzel
 */
public class CommonProjectActionsTest extends TestCase {
    
    public CommonProjectActionsTest(String testName) {
        super(testName);
    }


    /**
     * Assure that two clients do not interact.
     */
    public void testNewProjectAction() {
        Action client1 = CommonProjectActions.newProjectAction();
        Object existingSources = new Object();
        client1.putValue(CommonProjectActions.EXISTING_SOURCES_FOLDER, existingSources);
        Action client2 = CommonProjectActions.newProjectAction();
        Object o = client2.getValue(CommonProjectActions.EXISTING_SOURCES_FOLDER);
        assertTrue(o != existingSources);
    }
    
}
