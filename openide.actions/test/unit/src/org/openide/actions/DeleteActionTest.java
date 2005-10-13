/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.actions;


import java.util.Arrays;
import javax.swing.Action;
import javax.swing.ActionMap;

import junit.textui.TestRunner;

import org.netbeans.junit.*;
import org.openide.actions.*;
import org.openide.util.Lookup;


/** Test behaviour of DeleteAction intogether with clonning.
 */
public class DeleteActionTest extends AbstractCallbackActionTestHidden {
    public DeleteActionTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(DeleteActionTest.class));
    }

    
    protected Class actionClass () {
        return DeleteAction.class;
    }
    
    protected String actionKey () {
        return "delete";
    }
}
