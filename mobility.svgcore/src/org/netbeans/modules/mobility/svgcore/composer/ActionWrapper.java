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

import java.lang.Integer;
import javax.swing.Action;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Pavel Benes
 */
public final class ActionWrapper {
    /*
    public static final Comparator<ActionWrapper> MENU_ORDERING = new Comparator<ActionWrapper>() {
        public int compare(ActionWrapper a1, ActionWrapper a2) {
            return (a1.m_menuPosition<a2.m_menuPosition ? -1 :
                (a1.m_menuPosition==a2.m_menuPosition ? 0 : 1));                
        }        
    };*/
    
    private final Action m_action;
    private final int    m_toolBarPosition;
    
    /* Create separator */
    public ActionWrapper() {
        m_action = null;
        m_toolBarPosition = -1;
    }
    
    public ActionWrapper(Action action, FileObject fo) {
        assert action != null;
        m_action = action;
        m_toolBarPosition = getAttribute(fo, "toolBarPosition"); //NOI18N
    }
    
    public ActionWrapper(FileObject fo) {
        m_action = null;
        m_toolBarPosition = getAttribute(fo, "toolBarPosition"); //NOI18N
    }
    
    private static int getAttribute(FileObject fo, String attrName) {
        Integer pos = (Integer) fo.getAttribute(attrName);
        return pos != null ? pos.intValue() : -1;
    }

    public Action getAction() {
        return m_action;
    }
    
    public int getPositionInToolbar() {
        return m_toolBarPosition;
    }
}
