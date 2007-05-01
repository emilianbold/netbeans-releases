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
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import org.netbeans.modules.sql.framework.ui.graph.IGraphView;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class GraphActionDelegator extends GraphAction {

    private IGraphView source;

    private Action delegate;

    public GraphActionDelegator(IGraphView src, Action dgt) {
        this.source = src;
        this.delegate = dgt;

    }

    //override method to delegate it to delegate obj
    public Object getValue(String key) {
        return delegate.getValue(key);
    }

    //override method to delegate it to delegate obj
    public void putValue(String key, Object value) {
        delegate.putValue(key, value);
    }

    //override method to delegate it to delegate obj
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    //override method to delegate it to delegate obj
    public void setEnabled(boolean b) {
        delegate.setEnabled(b);
    }

    /**
     * called when this action is performed in the ui
     * 
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        //set the new source as graph view
        ev.setSource(source);
        delegate.actionPerformed(ev);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
    }
}

