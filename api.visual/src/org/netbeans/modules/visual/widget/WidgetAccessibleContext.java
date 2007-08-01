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

package org.netbeans.modules.visual.widget;

import org.netbeans.api.visual.widget.Widget;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleStateSet;
import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/**
 * @author David Kaspar
 */
public class WidgetAccessibleContext extends AccessibleContext {

    private Widget widget;

    public WidgetAccessibleContext (Widget widget) {
        this.widget = widget;
    }

    public AccessibleRole getAccessibleRole () {
        return AccessibleRole.UNKNOWN;
    }

    public AccessibleStateSet getAccessibleStateSet () {
        return new AccessibleStateSet ();
    }

    public int getAccessibleIndexInParent () {
        return widget != widget.getScene () ? widget.getParentWidget ().getChildren ().indexOf (widget) : 0;
    }

    public int getAccessibleChildrenCount () {
        return widget.getChildren ().size ();
    }

    public Accessible getAccessibleChild (int i) {
        return widget.getChildren ().get (i);
    }

    public Locale getLocale () throws IllegalComponentStateException {
        JComponent view = widget.getScene ().getView ();
        return view != null ? view.getLocale () : Locale.getDefault ();
    }

    public void notifyChildAdded (Widget parent, Widget child) {
        if (parent == widget)
            firePropertyChange (AccessibleContext.ACCESSIBLE_CHILD_PROPERTY, null, child);
    }

    public void notifyChildRemoved (Widget parent, Widget child) {
        if (parent == widget)
            firePropertyChange (AccessibleContext.ACCESSIBLE_CHILD_PROPERTY, child, null);
    }

}
