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

package org.netbeans.api.debugger.jpda;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
 * Tests field breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class DummyTests extends NbTestCase {

    public DummyTests (String s) {
        super (s);
    }

    public void testFinalInActionsProviderSupport () throws Exception {
        final int[] test = {0};
        ActionsProviderSupport aps = new ActionsProviderSupport () {
            public Set getActions () {
                return new HashSet ();
            }
            public void doAction (Object a) {
            }
            public boolean isEnabled (Object a) {
                test[0]++;
                return true;
            }
        };
        aps.isEnabled (null);
        assertEquals ("ActionsProviderSupport test ", 1, test[0]);
    }
}
