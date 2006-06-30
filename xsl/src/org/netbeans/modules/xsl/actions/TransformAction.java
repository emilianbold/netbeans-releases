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
package org.netbeans.modules.xsl.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

import org.netbeans.api.xml.cookies.TransformableCookie;
import org.netbeans.modules.xml.core.actions.CollectXMLAction;

import org.netbeans.modules.xsl.XSLDataObject;
import org.netbeans.modules.xsl.transform.TransformPerformer;

/**
 * Perform Transform action on XML document.
 * <p>
 * It should be cancellable in future.
 *
 * @author  Libor Kramolis
 */
public class TransformAction extends CookieAction implements CollectXMLAction.XMLAction {
    /** Serial Version UID */
    private static final long serialVersionUID = -640535981015250507L;

    private static TransformPerformer recentPerfomer;

    protected boolean enable(Node[] activatedNodes) {
        return super.enable(activatedNodes) && ready();
    }

    /**
     * Avoid spawing next transformatio until recent one is finished.
     * This check should be replaced by cancellable actions in future.
     */
    private boolean ready() {
        if (recentPerfomer == null) {
            return true;
        } else {
            if (recentPerfomer.isActive()) {
                return false;
            } else {
                recentPerfomer = null;
                return true;
            }
        }
    }

    /** */
    protected Class[] cookieClasses () {
        return new Class[] { TransformableCookie.class, XSLDataObject.class };
    }

    /** All selected nodes must be XML one to allow this action. */
    protected int mode () {
        return MODE_ALL;
    }


    /** Human presentable name. */
    public String getName() {
        return Util.THIS.getString ("NAME_transform_action");
    }

    /** Do not slow by any icon. */
    protected String iconResource () {
        return "org/netbeans/modules/xsl/resources/xsl_transformation.png"; // NOI18N
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (TransformAction.class);
    }


    /** Check all selected nodes. */
    protected void performAction (Node[] nodes) {
        recentPerfomer = new TransformPerformer (nodes);
        recentPerfomer.perform();
    }
    
    protected boolean asynchronous() {
        return false;
    }

}
