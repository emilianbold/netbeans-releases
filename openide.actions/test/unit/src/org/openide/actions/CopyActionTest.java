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

package org.openide.actions;


import java.util.Arrays;
import javax.swing.Action;
import javax.swing.ActionMap;

import junit.textui.TestRunner;

import org.netbeans.junit.*;
import org.openide.actions.*;
import org.openide.util.Lookup;


/** Test behaviour of CopyAction intogether with clonning.
 */
public class CopyActionTest extends AbstractCallbackActionTestHidden {
    public CopyActionTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(CopyActionTest.class));
    }

    
    protected Class actionClass () {
        return CopyAction.class;
    }
    
    protected String actionKey () {
        return javax.swing.text.DefaultEditorKit.copyAction;
    }
}
