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

package org.netbeans.modules.xml.xam.ui.actions;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.cookies.GetComponentCookie;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.nodes.Node;
import org.openide.util.actions.CookieAction;

/**
 * Base class for all actions which show a component in a particular view.
 *
 * @author Ajit Bhate
 * @author Nathan Fiedler
 */
public abstract class AbstractShowComponentAction extends CookieAction {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of AbstractShowComponentAction.
     */
    public AbstractShowComponentAction() {
        super();
    }

    protected boolean asynchronous() {
        return false;
    }

    protected final boolean enable(Node[] nodes) {
        return nodes != null && super.enable(nodes) &&
                XAMUtils.getViewCookie(XAMUtils.getComponent(
                nodes[0]), getView()) != null;
    }

    protected void performAction(Node[] nodes) {
        Component comp = XAMUtils.getComponent(nodes[0]);
        ViewComponentCookie.View view = getView();
        ViewComponentCookie cookie = XAMUtils.getViewCookie(comp, view);
        if (cookie != null) {
            cookie.view(view, comp);
        }
    }

    /**
     * Return the view in which this action will show the component.
     *
     * @return  component view.
     */
    protected abstract ViewComponentCookie.View getView();

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    protected Class[] cookieClasses() {
        return new Class[]{
            GetComponentCookie.class,
            Component.class
        };
    }
}
