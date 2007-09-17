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
import org.openide.filesystems.FileObject;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Pavel Benes
 */
public final class ActionWrapperFactory {
    private static final String CLASS_EXT = ".class"; //NOI18N

    final String m_actionId;
    final int    m_toolBarPosition;

    public static ActionWrapperFactory create(FileObject fo) {
        return new ActionWrapperFactory(fo);
    }

    private ActionWrapperFactory( FileObject fo) {
        m_actionId        = (String) fo.getAttribute("actionId"); //NOI18N
        m_toolBarPosition = getAttribute(fo, "toolBarPosition"); //NOI18N
    }

    public ActionWrapper createWrapper( SceneManager smgr) {
        if (m_actionId != null) {
            Action action = null;

            if (m_actionId.endsWith(CLASS_EXT)) {
                try {
                    String className = m_actionId.substring(0, m_actionId.length() - CLASS_EXT.length());
                    Class clazz = Class.forName(className);
                    action = SystemAction.get(clazz);
                } catch( ClassNotFoundException e) {
                    System.err.println("Class " + m_actionId + " not found"); //NOI18N
                }
            } else {
                    action = smgr.getAction(m_actionId);
            }

            if (action != null) {
                return new ActionWrapper(action, m_toolBarPosition);
            } 
        } else {
            //create separator
            return new ActionWrapper();
        }

        return null;
    }
    
    private static int getAttribute(FileObject fo, String attrName) {
        Integer pos = (Integer) fo.getAttribute(attrName);
        return pos != null ? pos.intValue() : -1;
    }
}
