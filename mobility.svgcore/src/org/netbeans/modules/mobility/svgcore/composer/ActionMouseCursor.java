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

import java.awt.Cursor;

/**
 *
 * @author Pavel Benes
 */
public class ActionMouseCursor {
    private final Cursor m_cursor;
    private final int    m_priority;
    
    public ActionMouseCursor( int cursorType, int priority) {
        m_cursor   = new Cursor(cursorType);
        m_priority = priority;
    }

    public ActionMouseCursor( Cursor cursor, int priority) {
        m_cursor   = cursor;
        m_priority = priority;
    }
    
    public Cursor getCursor() {
        return m_cursor;
    }
    
    public int getPriority() {
        return m_priority;
    }
}
