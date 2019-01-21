/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mobility.svgcore.composer;

import java.util.logging.Level;
import javax.swing.Action;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.SystemAction;

/**
 *
 * 
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

    @SuppressWarnings("unchecked")
    public ActionWrapper createWrapper( SceneManager smgr) {
        if (m_actionId != null) {
            Action action = null;

            if (m_actionId.endsWith(CLASS_EXT)) {
                String className = "";
                try {
                    className = m_actionId.substring(0, m_actionId.length() - CLASS_EXT.length());
                    Class clazz = Class.forName(className);
                    action = SystemAction.get(clazz);
                } catch( ClassNotFoundException e) {
                    SceneManager.log(Level.SEVERE, "Class " + className + " not found for actionID " + m_actionId, e); //NOI18N
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
