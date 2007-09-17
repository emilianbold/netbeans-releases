/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.composer;

import javax.swing.Action;

/**
 *
 * @author Pavel Benes
 */
public final class ActionWrapper {
    private final Action m_action;
    private final int    m_toolBarPosition;
    
    /* Create separator */
    public ActionWrapper() {
        this( null, -1);
    }
    
    public ActionWrapper(Action action, int toolBarPosition) {
        m_action = action;
        m_toolBarPosition = toolBarPosition;
    }
    
    public Action getAction() {
        return m_action;
    }
    
    public int getPositionInToolbar() {
        return m_toolBarPosition;
    }
}
