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

package org.netbeans.modules.j2ee.deployment.impl.ui;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import javax.swing.Action;


/**
 * Target base node is a base for any target node. The behaviour of this target
 * base node can be customized/extended by the target node provided by the plugin.
 *
 * @author Nam Nguyen
 */
public class TargetBaseNode extends AbstractNode {

    public TargetBaseNode(Children children, ServerTarget target) {
		super(children);
        setDisplayName(target.getName());
        setIconBase(target.getInstance().getServer().getIconBase());
        getCookieSet().add(target);
    }
    
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }
    
    protected ServerTarget getServerTarget() {
        return (ServerTarget) getCookie(ServerTarget.class);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public Sheet createSheet() {
        return Sheet.createDefault();
    }
}
