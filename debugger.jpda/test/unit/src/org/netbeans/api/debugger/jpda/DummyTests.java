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

package org.netbeans.api.debugger.jpda;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.viewmodel.NoInformationException;


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
