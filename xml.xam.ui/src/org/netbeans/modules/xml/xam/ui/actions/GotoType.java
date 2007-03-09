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
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Interface GotoType defines a place where an editor can be opened
 * for a particular component.
 *
 * @author  Nathan Fiedler
 */
public abstract class GotoType {

    /**
     * Finds the component that the given node represents. Subclasses may
     * wish to override this to retrieve the component in different manner.
     *
     * @param  node  the Node from which to get the component.
     * @return  the component, or null if none.
     */
    protected Component getComponent(Node node) {
        return XAMUtils.getComponent(node);
    }

    /**
     * Help context for the goto type.
     *
     * @return  the help context
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Returns the name of this type, to be displayed in a menu. Ideally
     * this should be the name of the view or editor that this type will
     * open the component in (e.g. "Design").
     *
     * @return  name of this type.
     */
    public String getName() {
        return NbBundle.getMessage(GotoType.class, "LBL_GoTo");
    }

    /**
     * Return the view in which this type will show the component.
     *
     * @return  component view.
     */
    protected abstract ViewComponentCookie.View getView();

    /**
     * Show the given node in the view this type represents.
     *
     * @param  node  the Node to be shown.
     */
    public void show(Node node) {
        Component comp = getComponent(node);
        ViewComponentCookie.View view = getView();
        ViewComponentCookie cookie = XAMUtils.getViewCookie(comp, view);
        if (cookie != null) {
            cookie.view(view, comp);
        }
    }
}
