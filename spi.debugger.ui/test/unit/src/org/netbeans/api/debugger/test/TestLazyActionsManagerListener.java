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

package org.netbeans.api.debugger.test;

import org.netbeans.api.debugger.LazyActionsManagerListener;

import java.util.*;

/**
 * A test lazy actions manager listener.
 *
 * @author Maros Sandor
 */
public class TestLazyActionsManagerListener extends LazyActionsManagerListener {

    private TestActionsManagerListener amListener;

    public TestLazyActionsManagerListener() {
        this.amListener = new TestActionsManagerListener();
    }

    protected void destroy() {
    }

    public String[] getProperties() {
        return null;
    }

    public List getPerformedActions() {
        return amListener.getPerformedActions();
    }

    public void actionPerformed(Object action) {
        amListener.actionPerformed(action);
    }

    public void actionStateChanged(Object action, boolean enabled) {
        amListener.actionStateChanged(action, enabled);
    }
}
