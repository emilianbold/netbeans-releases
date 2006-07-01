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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.openide.explorer;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.util.Lookup;

/**
 * An architectural hack - until PropertySheet is separated and openide
 * split up, openide cannot depend on module code due to classloader
 * restrictions.  So we have an interface which will supply a bridge to
 * the tabcontrol code; an implementation of this interface is provided
 * over org.netbeans.swing.tabcontrol.TabbedContainer (in core/swing/tabcontrol)
 * by the window system which depends on it.
 *
 * @see org.netbeans.core.windows.view.ui.tabcontrol.TabbedContainerBridgeImpl
 * @author  Tim Boudreau
 */
public abstract class TabbedContainerBridge {
    
    protected TabbedContainerBridge(){};
    
    public static TabbedContainerBridge getDefault() {
        TabbedContainerBridge result = Lookup.getDefault().lookup (TabbedContainerBridge.class);
        if (result == null) {
            //unit test or standalone library operation
            return new TrivialTabbedContainerBridgeImpl();
        }
        return result;
    }

    public abstract JComponent createTabbedContainer();

    public abstract void setInnerComponent (JComponent container, JComponent inner);

    public abstract JComponent getInnerComponent(JComponent jc);

    public abstract Object[] getItems(JComponent jc);

    public abstract void setItems (JComponent jc, Object[] objects, String[] titles);

    public abstract void attachSelectionListener (JComponent jc, ChangeListener listener);

    public abstract void detachSelectionListener (JComponent jc, ChangeListener listener);

    public abstract Object getSelectedItem(JComponent jc);

    public abstract void setSelectedItem (JComponent jc, Object selection);

    public abstract boolean setSelectionByName(JComponent jc, String tabname);

    public abstract String getCurrentSelectedTabName(JComponent jc);

}
