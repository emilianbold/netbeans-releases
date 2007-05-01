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

import java.awt.event.ActionEvent;

import javax.swing.Action;

/**
 * A wraper around a given action to show in tool bar this action hides the name of the
 * action since most of the time tool bar action should show icon not the text
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ToolBarAction extends GraphAction {

    private GraphAction delegate;

    public ToolBarAction(GraphAction dgt) {
        this.delegate = dgt;
    }

    //override getValue to return empty string for Action.NAME
    public Object getValue(String key) {
        if (key.equals(Action.NAME)) {
            return "";
        }

        return delegate.getValue(key);
    }

    /**
     * called when this action is performed in the ui
     * 
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        delegate.actionPerformed(ev);
    }
}
