/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form.actions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;

/** CustomizeLayout action - enabled on RADContainerNodes and RADLayoutNodes.
 *
 * @author   Ian Formanek
 */
public class CustomizeLayoutAction extends CookieAction {
    static final long serialVersionUID =-9123795816864877128L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = -5280204757097896304L;

    /** @return the mode of action. Possible values are disjunctions of MODE_XXX
     * constants. */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    /** Creates new set of classes that are tested by the cookie.
     *
     * @return list of classes the that the cookie tests
     */
    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class, FormLayoutCookie.class };
    }

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return org.openide.util.NbBundle.getBundle(CustomizeLayoutAction.class).getString("ACT_CustomizeLayout");
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizeLayoutAction.class);
    }

    /** Icon resource.
     * @return name of resource for icon
     */
    protected String iconResource() {
        return "/org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected void performAction(Node[] activatedNodes) {
        FormLayoutCookie layoutCookie =(FormLayoutCookie)activatedNodes[0].getCookie(FormLayoutCookie.class);
        if (layoutCookie != null) {
            org.openide.TopManager.getDefault().getNodeOperation().customize(layoutCookie.getLayoutNode());
        } else {
            RADComponentCookie nodeCookie =(RADComponentCookie)activatedNodes[0].getCookie(RADComponentCookie.class);
            if (nodeCookie != null) {
                if (nodeCookie.getRADComponent() instanceof RADVisualContainer) {
                    RADVisualContainer container =(RADVisualContainer)nodeCookie.getRADComponent();
                    org.openide.TopManager.getDefault().getNodeOperation().customize(container.getLayoutNodeReference());
                }
            }
        }
    }

    /*
     * In this method the enable / disable action logic can be defined.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected boolean enable(Node[] activatedNodes) {
        if (super.enable(activatedNodes)) {
            FormLayoutCookie layoutCookie =(FormLayoutCookie)activatedNodes[0].getCookie(FormLayoutCookie.class);
            if (layoutCookie != null) {
                return layoutCookie.getLayoutNode().hasCustomizer();
            } else {
                RADComponentCookie nodeCookie =(RADComponentCookie)activatedNodes[0].getCookie(RADComponentCookie.class);
                if (nodeCookie != null) {
                    if (nodeCookie.getRADComponent() instanceof RADVisualContainer) {
                        RADVisualContainer container =(RADVisualContainer)nodeCookie.getRADComponent();
                        return(container.getLayoutNodeReference() != null) && container.getLayoutNodeReference().hasCustomizer();
                    }
                }
            }
        }
        return false;
    }

}
