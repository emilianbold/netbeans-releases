/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.cnd.makeproject.api.DefaultProjectActionHandler;

public class ProjectActionSupport {
    private static ActionListener customActionListener = null;

    public static ActionListener getDefaultActionListener() {
        return DefaultProjectActionHandler.getInstance();
    }

    public static void setCustomActionListener(ActionListener listener) {
        customActionListener = listener;
    }
    
    public static ActionListener getCustomActionListener() {
        return customActionListener;
    }
    
    public static ActionListener getActionListener() {
        if (getCustomActionListener() != null)
            return getCustomActionListener();
        else
            return getDefaultActionListener();
    }
        
    public static void fireActionPerformed(ProjectActionEvent[] paes) {
	ActionEvent ae = new ActionEvent(paes, 0, null);
        getActionListener().actionPerformed(ae);
    }
}
