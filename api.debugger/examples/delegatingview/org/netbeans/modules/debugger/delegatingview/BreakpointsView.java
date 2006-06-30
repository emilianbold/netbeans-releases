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

package org.netbeans.modules.debugger.delegatingview;

import org.netbeans.modules.debugger.support.DebuggerModule;
import org.netbeans.modules.debugger.support.nodes.ExplorerViewSupport;
import org.netbeans.modules.debugger.support.nodes.DebuggerNode;

import javax.swing.ImageIcon;
import java.awt.Image;

public class BreakpointsView extends ExplorerViewSupport {

    public BreakpointsView () {
        super (false);
    }

    public String getRootNode () {
        return DebuggerModule.BREAKPOINTS_ROOT_NODE;
    }

    public String getName () {
        return DebuggerNode.getLocalizedString ("CTL_Breakpoints_view");
    }

    public Image getIcon () {
        return new ImageIcon (BreakpointsView.class.getResource (
            "/org/netbeans/core/resources/breakpoints.gif" // NOI18N
        )).getImage ();
    }
}
