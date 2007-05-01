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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.graph.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;

import com.sun.sql.framework.utils.Logger;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public abstract class GraphAction extends AbstractAction {

    /* log4j logger category */
    private static final String LOG_CATEGORY = GraphAction.class.getName();

    private static HashMap actionMap = new HashMap();

    /**
     * called when this action is performed in the ui
     * 
     * @param ev event
     */
    public abstract void actionPerformed(ActionEvent ev);

    public static GraphAction getAction(Class actionClass) {
        GraphAction action = findAction(actionClass);
        if (action != null) {
            return action;
        }

        action = createAction(actionClass);

        return action;
    }

    private static GraphAction findAction(Class actionClass) {
        return (GraphAction) actionMap.get(actionClass);
    }

    private static GraphAction createAction(Class actionClass) {
        GraphAction action = null;
        try {
            action = (GraphAction) actionClass.newInstance();
            actionMap.put(actionClass, action);
        } catch (InstantiationException e1) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "createAction", "Error creating instance of action" + actionClass.getName(), e1);
        } catch (IllegalAccessException e2) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "createAction", "Error creating instance of action" + actionClass.getName(), e2);
        }

        return action;
    }

    /**
     * return a component that can be used instead of action itself
     * 
     * @return component
     */
    public Component getComponent() {
        return null;
    }
}
