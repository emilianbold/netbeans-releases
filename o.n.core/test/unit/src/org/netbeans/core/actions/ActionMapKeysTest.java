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

package org.netbeans.core.actions;

import junit.framework.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;

/** Checks that the keys defined in API are really working.
 *
 * @author Jaroslav Tulach
 */
public class ActionMapKeysTest extends TestCase {

    public ActionMapKeysTest (String testName) {
        super (testName);
    }

    public void testJumpNextAction () {
        JumpNextAction a = JumpNextAction.get(JumpNextAction.class);
        assertEquals ("jumpNext", a.getActionMapKey ());
    }

    public void testJumpPrevAction () {
        JumpPrevAction a = JumpPrevAction.get(JumpPrevAction.class);
        assertEquals ("jumpPrev", a.getActionMapKey ());
    }
    
}
